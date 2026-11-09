package experiment.tenant

import com.etude.TestcontainersConfiguration
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.HibernateException
import org.hibernate.PropertyValueException
import org.hibernate.SessionFactory
import org.hibernate.annotations.TenantId
import org.hibernate.cfg.AvailableSettings
import org.hibernate.cfg.Configuration
import org.hibernate.context.spi.CurrentTenantIdentifierResolver
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.testcontainers.postgresql.PostgreSQLContainer
import java.sql.Connection
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * ADR-0006의 가정을 순수 Hibernate와 JDBC로 확인하는 실험. 엔티티 스캔을 피하려고 `com.etude` 밖에 둔다.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TenantIsolationExperimentTest {

    private val postgres = PostgreSQLContainer(TestcontainersConfiguration.POSTGRES_IMAGE)
    private val tenantA: UUID = UUID.randomUUID()
    private val tenantB: UUID = UUID.randomUUID()

    @BeforeAll
    fun start() = postgres.start()

    @AfterAll
    fun stop() = postgres.stop()

    @Entity(name = "Note")
    @Table(name = "note")
    class Note(var text: String) {
        @Id
        var id: UUID = UUID.randomUUID()

        @TenantId
        var tenantId: UUID? = null
    }

    object CurrentTenant : CurrentTenantIdentifierResolver<UUID> {
        val value = ThreadLocal<UUID?>()

        override fun resolveCurrentTenantIdentifier(): UUID? = value.get()

        override fun validateExistingCurrentSessions() = false

        override fun isRoot(tenantId: UUID?) = tenantId == ROOT
    }

    companion object {
        /** 플랫폼 작업(root 테넌트). */
        val ROOT = UUID(0, 0)
    }

    private fun <T> asTenant(tenant: UUID?, block: () -> T): T {
        CurrentTenant.value.set(tenant)
        try {
            return block()
        } finally {
            CurrentTenant.value.remove()
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class HibernateTenantId {

        private lateinit var sessionFactory: SessionFactory
        private lateinit var noteOfA: UUID

        @BeforeAll
        fun setUp() {
            sessionFactory = Configuration()
                .addAnnotatedClass(Note::class.java)
                .setProperty(AvailableSettings.JAKARTA_JDBC_URL, postgres.jdbcUrl)
                .setProperty(AvailableSettings.JAKARTA_JDBC_USER, postgres.username)
                .setProperty(AvailableSettings.JAKARTA_JDBC_PASSWORD, postgres.password)
                .setProperty(AvailableSettings.HBM2DDL_AUTO, "create")
                .setProperty(AvailableSettings.SHOW_SQL, "true")
                .setCurrentTenantIdentifierResolver(CurrentTenant)
                .buildSessionFactory()
            val a = Note("a")
            asTenant(tenantA) { sessionFactory.inTransaction { it.persist(a) } }
            noteOfA = a.id
            asTenant(tenantB) { sessionFactory.inTransaction { it.persist(Note("b")) } }
        }

        @AfterAll
        fun tearDown() = sessionFactory.close()

        @Test
        fun `persist fills the tenant id from the resolver`() {
            val tenant = asTenant(tenantA) { sessionFactory.fromSession { it.find(Note::class.java, noteOfA)?.tenantId } }
            println("[observed] tenantId after persist = $tenant (expected $tenantA)")
            assertEquals(tenantA, tenant)
        }

        @Test
        fun `persist with a tenant id set by the application`() {
            val result = runCatching {
                asTenant(tenantA) {
                    sessionFactory.fromTransaction { s ->
                        Note("forged").also {
                            it.tenantId = tenantB
                            s.persist(it)
                        }
                    }
                }
            }
            println("[observed] persist with tenantId=B while current=A -> ${result.fold({ "saved, tenantId=${it.tenantId}" }, { "${it::class.simpleName}: ${it.message}" })}")
            assertIs<PropertyValueException>(result.exceptionOrNull())
        }

        @Test
        fun `query returns only the current tenant`() {
            val texts = asTenant(tenantB) {
                sessionFactory.fromSession { it.createSelectionQuery("select text from Note where text in ('a', 'b')", String::class.java).resultList }
            }
            println("[observed] HQL as B -> $texts")
            assertEquals(listOf("b"), texts)
        }

        @Test
        fun `find by id of another tenant`() {
            val found = asTenant(tenantB) { sessionFactory.fromSession { it.find(Note::class.java, noteOfA) } }
            println("[observed] find(A's note) as B -> ${found?.text ?: "null"}")
            assertNull(found)
        }

        @Test
        fun `native query is not filtered`() {
            val count = asTenant(tenantB) {
                sessionFactory.fromSession { it.createNativeQuery("select count(*) from note where text in ('a', 'b')", Long::class.java).singleResult }
            }
            println("[observed] native count(*) as B -> $count")
            assertEquals(2L, count, "native SQL bypasses the @TenantId filter")
        }

        @Test
        fun `root sees every tenant`() {
            val texts = asTenant(ROOT) {
                sessionFactory.fromSession { it.createSelectionQuery("select text from Note where text in ('a', 'b') order by text", String::class.java).resultList }
            }
            println("[observed] HQL as root -> $texts")
            assertEquals(listOf("a", "b"), texts)
        }

        @Test
        fun `root persists without a tenant id`() {
            val result = runCatching { asTenant(ROOT) { sessionFactory.fromTransaction { s -> Note("root").also { s.persist(it) } } } }
            println("[observed] persist as root without tenantId -> ${result.fold({ "saved, tenantId=${it.tenantId}" }, { "${it::class.simpleName}: ${it.message}" })}")
            assertEquals(ROOT, result.getOrThrow().tenantId, "root silently becomes the row's tenant")
        }

        @Test
        fun `root persists with an assigned tenant id`() {
            val result = runCatching {
                asTenant(ROOT) {
                    sessionFactory.fromTransaction { s ->
                        Note("root-assigned").also {
                            it.tenantId = tenantB
                            s.persist(it)
                        }
                    }
                }
            }
            println("[observed] persist as root with tenantId=B -> ${result.fold({ "saved, tenantId=${it.tenantId}" }, { "${it::class.simpleName}: ${it.message}" })}")
            assertEquals(tenantB, result.getOrThrow().tenantId, "root may write into any tenant")
        }

        @Test
        fun `no current tenant`() {
            val result = runCatching {
                asTenant(null) { sessionFactory.fromSession { it.createSelectionQuery("select text from Note", String::class.java).resultList } }
            }
            println("[observed] HQL without tenant -> ${result.fold({ "rows=$it" }, { "${it::class.simpleName}: ${it.message}" })}")
            assertIs<HibernateException>(result.exceptionOrNull())
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    inner class RowLevelSecurity {

        private lateinit var appPool: HikariDataSource

        private fun owner(): Connection = postgres.createConnection("")

        @BeforeAll
        fun setUp() {
            owner().use { c ->
                c.createStatement().use {
                    it.execute("create table rls_note (id uuid primary key, tenant_id uuid not null, text text not null)")
                    it.execute("alter table rls_note enable row level security")
                    // 트랜잭션 범위 set_config가 끝나면 값이 NULL이 아니라 ''로 남는다.
                    it.execute(
                        """
                        create policy tenant_isolation on rls_note
                        using (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid)
                        with check (tenant_id = nullif(current_setting('app.tenant_id', true), '')::uuid)
                        """.trimIndent(),
                    )
                    it.execute("create table rls_naive (id uuid primary key, tenant_id uuid not null)")
                    it.execute("alter table rls_naive enable row level security")
                    it.execute("create policy naive on rls_naive using (tenant_id = current_setting('app.tenant_id', true)::uuid)")
                    it.execute("create role app_user login password 'app'")
                    it.execute("grant select, insert, update on rls_note, rls_naive to app_user")
                    it.execute("insert into rls_note values ('${UUID.randomUUID()}', '$tenantA', 'a'), ('${UUID.randomUUID()}', '$tenantB', 'b')")
                    it.execute("insert into rls_naive values ('${UUID.randomUUID()}', '$tenantA')")
                }
            }
            appPool = HikariDataSource(
                HikariConfig().apply {
                    jdbcUrl = postgres.jdbcUrl
                    username = "app_user"
                    password = "app"
                    maximumPoolSize = 1
                    isAutoCommit = false
                },
            )
        }

        @AfterAll
        fun tearDown() = appPool.close()

        private fun <T> inTransaction(tenant: UUID?, block: (Connection) -> T): T = appPool.connection.use { c ->
            try {
                if (tenant != null) {
                    c.prepareStatement("select set_config('app.tenant_id', ?, true)").use {
                        it.setString(1, tenant.toString())
                        it.execute()
                    }
                }
                block(c).also { c.commit() }
            } catch (e: Exception) {
                c.rollback()
                throw e
            }
        }

        private fun Connection.texts(sql: String): List<String> = createStatement().use { s ->
            s.executeQuery(sql).use { rs -> generateSequence { if (rs.next()) rs.getString(1) else null }.toList() }
        }

        @Test
        fun `app role sees only the current tenant`() {
            val texts = inTransaction(tenantB) { it.texts("select text from rls_note") }
            println("[observed] app_user as B -> $texts")
            assertEquals(listOf("b"), texts)
        }

        @Test
        fun `setting does not survive on the pooled connection`() {
            inTransaction(tenantA) { it.texts("select text from rls_note") }
            val (setting, texts) = inTransaction(null) {
                it.texts("select coalesce(current_setting('app.tenant_id', true), '<null>')").single() to it.texts("select text from rls_note")
            }
            println("[observed] next transaction on the same pooled connection: setting='$setting', rows=$texts")
            assertEquals(emptyList(), texts)
        }

        @Test
        fun `naive policy after the setting ends`() {
            inTransaction(tenantA) { it.texts("select count(*) from rls_naive") }
            val result = runCatching { inTransaction(null) { it.texts("select count(*) from rls_naive") } }
            println("[observed] naive policy (no nullif) after transaction-local setting ended -> ${result.fold({ "rows=$it" }, { it.message })}")
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("invalid input syntax for type uuid"))
        }

        @Test
        fun `insert for another tenant is rejected`() {
            val result = runCatching {
                inTransaction(tenantA) { c ->
                    c.createStatement().use { it.executeUpdate("insert into rls_note values ('${UUID.randomUUID()}', '$tenantB', 'forged')") }
                }
            }
            println("[observed] insert tenant B row as A -> ${result.fold({ "inserted $it" }, { it.message })}")
            assertEquals(true, result.isFailure)
        }

        @Test
        fun `row lock query sees only the current tenant`() {
            val texts = inTransaction(tenantB) { it.texts("select text from rls_note for no key update") }
            println("[observed] FOR NO KEY UPDATE as B -> $texts")
            assertEquals(listOf("b"), texts)
        }

        @Test
        fun `table owner bypasses the policy`() {
            val texts = owner().use { it.texts("select text from rls_note order by text") }
            println("[observed] owner/superuser -> $texts")
            assertEquals(listOf("a", "b"), texts)
        }
    }
}
