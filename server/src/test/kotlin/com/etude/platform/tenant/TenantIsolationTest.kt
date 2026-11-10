package com.etude.platform.tenant

import com.etude.IntegrationTest
import com.etude.TestCompanies
import com.etude.TestcontainersConfiguration.Companion.APP_ROLE
import com.etude.board.application.BoardService
import com.etude.board.domain.Board
import com.etude.count
import com.etude.inTransaction
import com.etude.platform.TenantContext
import com.etude.queryValue
import com.etude.update
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import java.sql.SQLException
import java.time.Duration
import java.util.UUID
import javax.sql.DataSource

/**
 * 테넌트 격리의 두 방어선(Hibernate `@TenantId`, RLS)과 회사가 정해지지 않은 작업의 경계(ADR-0006, ADR-0008).
 * 데이터베이스 검사는 Hibernate를 거치지 않고 애플리케이션 역할로 직접 SQL을 실행한다.
 */
@IntegrationTest
class TenantIsolationTest(
    @Autowired private val companies: TestCompanies,
    @Autowired private val boardService: BoardService,
    @Autowired private val dataSource: DataSource,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val transactionTemplate: TransactionTemplate,
    @Autowired private val entityManager: EntityManager,
) {

    @Test
    fun `the application connects as a role that row-level security applies to`() {
        val role = jdbcTemplate.queryForMap("select current_user as name, rolsuper, rolbypassrls from pg_roles where rolname = current_user")
        assertThat(role).containsEntry("name", APP_ROLE).containsEntry("rolsuper", false).containsEntry("rolbypassrls", false)

        val tables = jdbcTemplate.queryForList(
            "select c.relname, c.relrowsecurity, pg_get_userbyid(c.relowner) as owner from pg_class c where c.relname in ('board', 'post') order by c.relname",
        )
        assertThat(tables).hasSize(2).allSatisfy {
            assertThat(it["relrowsecurity"]).isEqualTo(true)
            assertThat(it["owner"]).isNotEqualTo(APP_ROLE)
        }
    }

    @Test
    fun `the database shows only the current company's rows`() {
        val acme = companyWithBoard()
        val other = companyWithBoard()

        dataSource.inTransaction { assertThat(it.count("select count(*) from board where tenant_id in (?, ?)", acme, other)).isZero() }
        dataSource.inTransaction(acme) {
            assertThat(it.count("select count(*) from board where tenant_id = ?", acme)).isEqualTo(1)
            assertThat(it.count("select count(*) from board where tenant_id = ?", other)).isZero()
        }
        dataSource.inTransaction(other) {
            assertThat(it.count("select count(*) from board where tenant_id = ?", other)).isEqualTo(1)
            assertThat(it.count("select count(*) from board where tenant_id = ?", acme)).isZero()
        }
    }

    @Test
    fun `the database rejects rows for another company or for no company`() {
        val acme = companyWithBoard()
        val other = companyWithBoard()
        val otherBoard = dataSource.inTransaction(other) { it.queryValue("select id from board where tenant_id = ?", other) as UUID }

        assertThatThrownBy { dataSource.inTransaction(acme) { insertPost(it, other, otherBoard) } }
            .isInstanceOf(SQLException::class.java).hasMessageContaining("row-level security")
        assertThatThrownBy { dataSource.inTransaction { insertPost(it, other, otherBoard) } }
            .isInstanceOf(SQLException::class.java).hasMessageContaining("row-level security")
        dataSource.inTransaction(acme) {
            assertThat(it.update("update board set name = 'Taken' where tenant_id = ?", other)).isZero()
        }
        dataSource.inTransaction(other) { assertThat(it.queryValue("select name from board where tenant_id = ?", other)).isNull() }
    }

    @Test
    fun `tenant data needs a company even though other work runs without one`() {
        companyWithBoard()

        assertThatThrownBy { boardService.listBoards() }.hasStackTraceContaining("Tenant data needs a tenant")
        assertThatThrownBy { TenantContext.runAsPlatform { boardService.listBoards() } }.hasStackTraceContaining("Tenant data needs a tenant")
        assertThatThrownBy { TenantContext.runAsPlatform { boardService.prepareDefaultBoard() } }.hasStackTraceContaining("Tenant data needs a tenant")

        // 회사로 나누지 않은 테이블은 회사 없이 그대로 쓴다(서버 시작, 로그인, 고객 API).
        assertThat(transactionTemplate.execute { jdbcTemplate.queryForObject("select count(*) from tenant", Long::class.java) }).isPositive()
    }

    @Test
    fun `the unbound marker is never a company`() {
        companyWithBoard()

        assertThatThrownBy { TenantContext.runAs(TenantIdentifierResolver.NONE) { boardService.listBoards() } }
            .hasStackTraceContaining("Tenant data needs a tenant")
        assertThatThrownBy { TenantContext.runAs(TenantIdentifierResolver.NONE) { boardService.prepareDefaultBoard() } }
            .hasStackTraceContaining("Tenant data needs a tenant")
        val setting = TenantContext.runAs(TenantIdentifierResolver.NONE) {
            transactionTemplate.execute { jdbcTemplate.queryForObject("select current_setting('app.tenant_id', true)", String::class.java) }
        }
        assertThat(setting).isEmpty()
    }

    @Test
    fun `refuses to switch the company inside an open transaction`() {
        val acme = companyWithBoard()
        val other = companyWithBoard()

        assertThatThrownBy {
            TenantContext.runAs(acme) {
                transactionTemplate.execute { TenantContext.runAs(other) { boardService.listBoards() } }
            }
        }.hasStackTraceContaining("The transaction was started for another tenant")
        assertThatThrownBy {
            transactionTemplate.execute { TenantContext.runAs(other) { boardService.listBoards() } }
        }.hasStackTraceContaining("The transaction was started for another tenant")

        // 새 트랜잭션은 바뀐 회사로 시작한다.
        val boards = TenantContext.runAs(acme) {
            transactionTemplate.execute { boardService.listBoards() }
        }
        assertThat(boards).singleElement().isEqualTo(TenantContext.runAs(acme) { boardService.findDefaultBoard() })
    }

    @Test
    fun `Hibernate refuses to save tenant data without a company`() {
        assertThatThrownBy {
            transactionTemplate.executeWithoutResult {
                entityManager.persist(Board.default())
                entityManager.flush()
            }
        }
            .hasStackTraceContaining("Cannot save tenant data without a tenant")
        assertThatThrownBy {
            TenantContext.runAsPlatform {
                transactionTemplate.executeWithoutResult {
                    entityManager.persist(Board.default())
                    entityManager.flush()
                }
            }
        }.hasStackTraceContaining("Cannot save tenant data without a tenant")
    }

    @Test
    fun `tells the database the same company as Hibernate, only for the transaction`() {
        val acme = companyWithBoard()
        val other = companyWithBoard()

        val (pid, setting) = TenantContext.runAs(acme) {
            transactionTemplate.execute {
                jdbcTemplate.queryForMap("select pg_backend_pid() as pid, current_setting('app.tenant_id', true) as tenant")
            }!!.let { it["pid"] to it["tenant"] }
        }
        assertThat(setting).isEqualTo(acme.toString())

        // 같은 스레드가 풀에서 다시 빌리면 HikariCP는 방금 돌려준 연결을 준다. 값은 트랜잭션과 함께 사라졌다.
        dataSource.connection.use { connection ->
            assertThat(connection.queryValue("select pg_backend_pid()")).isEqualTo(pid)
            assertThat(connection.queryValue("select current_setting('app.tenant_id', true)") as String?).isNullOrEmpty()
            assertThat(connection.count("select count(*) from board where tenant_id in (?, ?)", acme, other)).isZero()
        }

        val otherBoards = TenantContext.runAs(other) {
            transactionTemplate.execute {
                assertThat(jdbcTemplate.queryForObject("select pg_backend_pid()", Int::class.java)).isEqualTo(pid)
                boardService.listBoards()
            }
        }
        assertThat(otherBoards).singleElement().satisfies({ assertThat(it.isDefault).isTrue() })
        assertThat(TenantContext.runAs(other) { boardService.findDefaultBoard() }).isEqualTo(otherBoards!!.single())
    }

    /** 회사를 만들고 기본 게시판이 준비될 때까지 기다린다. */
    private fun companyWithBoard(): UUID {
        val tenantId = companies.create().tenantId
        await().atMost(Duration.ofSeconds(10)).until { TenantContext.runAs(tenantId) { boardService.findDefaultBoard() } != null }
        return tenantId
    }

    private fun insertPost(connection: java.sql.Connection, tenantId: UUID, boardId: UUID) = connection.update(
        """
        insert into post (id, tenant_id, board_id, author_id, title, body, posted_at, created_at, updated_at)
        values (?, ?, ?, ?, 'Intruder', 'Hello', now(), now(), now())
        """.trimIndent(),
        UUID.randomUUID(),
        tenantId,
        boardId,
        UUID.randomUUID(),
    )
}
