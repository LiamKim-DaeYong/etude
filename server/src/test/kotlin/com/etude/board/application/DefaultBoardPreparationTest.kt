package com.etude.board.application

import com.etude.IntegrationTest
import com.etude.TestCompanies
import com.etude.TestCompany
import com.etude.count
import com.etude.inTransaction
import com.etude.platform.TenantContext
import com.etude.tenant.TenantRegistered
import com.etude.update
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.modulith.events.IncompleteEventPublications
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.time.Duration
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import javax.sql.DataSource

@IntegrationTest
class DefaultBoardPreparationTest(
    @Autowired private val mvc: MockMvcTester,
    @Autowired private val companies: TestCompanies,
    @Autowired private val boardService: BoardService,
    @Autowired private val incompletePublications: IncompleteEventPublications,
    @Autowired private val jdbcTemplate: JdbcTemplate,
    @Autowired private val dataSource: DataSource,
    @Autowired private val passwordEncoder: PasswordEncoder,
) {

    @Test
    fun `prepares one default board when a company is registered`() {
        val company = companies.create()

        awaitPublication(company.tenantId, "COMPLETED")
        assertThat(boardCount(company.tenantId)).isEqualTo(1)
        assertThat(mvc.get().uri("/admin/v1/boards").session(companies.ownerSession(company)).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""[{"name":null,"default":true}]""")
    }

    @Test
    fun `listing boards never creates one`() {
        val company = companyWithoutBoard()
        val session = companies.ownerSession(company)

        assertThat(mvc.get().uri("/admin/v1/boards").session(session).exchange()).hasStatusOk().bodyJson().isEqualTo("[]")
        assertThat(mvc.get().uri("/admin/v1/posts").session(session).exchange()).hasStatusOk().bodyJson().isEqualTo("[]")

        assertThat(boardCount(company.tenantId)).isZero()
    }

    @Test
    fun `preparing the default board again keeps one board`() {
        val company = companyWithoutBoard()

        val first = TenantContext.runAs(company.tenantId) { boardService.prepareDefaultBoard() }
        val second = TenantContext.runAs(company.tenantId) { boardService.prepareDefaultBoard() }

        assertThat(second).isEqualTo(first)
        assertThat(first.isDefault).isTrue()
        assertThat(boardCount(company.tenantId)).isEqualTo(1)
    }

    @Test
    fun `concurrent preparation leaves one default board`() {
        val company = companyWithoutBoard()
        val requests = 8
        val prepared = AtomicInteger()
        val failures = Collections.synchronizedList(mutableListOf<Throwable>())
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(requests)

        repeat(requests) {
            executor.submit {
                start.await()
                try {
                    TenantContext.runAs(company.tenantId) { boardService.prepareDefaultBoard() }
                    prepared.incrementAndGet()
                } catch (ex: Throwable) {
                    failures += ex
                }
            }
        }
        start.countDown()
        executor.shutdown()
        assertThat(executor.awaitTermination(30, TimeUnit.SECONDS)).isTrue()

        assertThat(prepared.get()).isPositive()
        // 늦게 저장한 쪽은 유일 인덱스에 막힌다. 리스너는 이 예외를 받으면 이미 있는 게시판을 쓴다.
        assertThat(failures).allSatisfy { assertThat(it).isInstanceOf(DataIntegrityViolationException::class.java) }
        assertThat(boardCount(company.tenantId)).isEqualTo(1)
    }

    /** 이벤트는 최소 한 번 전달되므로 다시 올 수 있다. 완료된 발행 기록을 실패로 바꿔 재전달을 흉내 낸다. */
    @Test
    fun `keeps one default board when the registration event is delivered again`() {
        val company = companies.create()
        awaitPublication(company.tenantId, "COMPLETED")

        jdbcTemplate.update(
            "update event_publication set status = 'FAILED', completion_date = null where serialized_event like ?",
            "%${company.tenantId}%",
        )
        incompletePublications.resubmitIncompletePublications { (it.event as? TenantRegistered)?.tenantId == company.tenantId }

        awaitPublication(company.tenantId, "COMPLETED")
        assertThat(boardCount(company.tenantId)).isEqualTo(1)
    }

    /** 회사 등록 이벤트 없이 회사와 대표를 만든다. 기본 게시판이 없는 기존 회사를 흉내 낸다. */
    private fun companyWithoutBoard(): TestCompany {
        val tenantId = UUID.randomUUID()
        val ownerId = UUID.randomUUID()
        val code = (10_000_000..99_999_999).random().toString()
        val email = "owner-$ownerId@example.com"
        dataSource.inTransaction {
            it.update("insert into tenant (id, name, code, created_at, updated_at) values (?, 'No Board Film', ?, now(), now())", tenantId, code)
            it.update(
                "insert into users (id, tenant_id, role, username, email, password_hash, created_at, updated_at) values (?, ?, 'OWNER', ?, ?, ?, now(), now())",
                ownerId,
                tenantId,
                TestCompanies.OWNER_USERNAME,
                email,
                passwordEncoder.encode(TestCompanies.PASSWORD),
            )
        }
        return TestCompany(tenantId, code, ownerId, email)
    }

    /** 그 회사로 설정한 애플리케이션 역할 연결에서 센다(RLS). */
    private fun boardCount(tenantId: UUID): Long = dataSource.inTransaction(tenantId) { it.count("select count(*) from board where tenant_id = ?", tenantId) }

    private fun awaitPublication(tenantId: UUID, status: String) = await().atMost(Duration.ofSeconds(10)).untilAsserted {
        val statuses = jdbcTemplate.queryForList(
            "select status from event_publication where event_type = ? and serialized_event like ?",
            String::class.java,
            TenantRegistered::class.java.name,
            "%$tenantId%",
        )
        assertThat(statuses).containsExactly(status)
    }
}
