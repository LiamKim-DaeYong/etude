package com.etude.identity.application

import com.etude.IntegrationTest
import com.etude.identity.application.SignUpRejectedException.Reason
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@IntegrationTest
class SignUpServiceTest(
    @Autowired private val signUpService: SignUpService,
    @Autowired private val passwordEncoder: PasswordEncoder,
    @Autowired private val jdbcTemplate: JdbcTemplate,
) {

    @Test
    fun `creates a company with an 8-digit code and its owner`() {
        val email = uniqueEmail()

        val result = signUpService.signUp(SignUpCommand("Acme Film", email, "owner", "s3cret-pass"))

        assertThat(result.tenantCode).matches("[1-9][0-9]{7}")
        val tenant = jdbcTemplate.queryForMap("select name, code from tenant where id = ?", result.tenantId)
        assertThat(tenant["name"]).isEqualTo("Acme Film")
        assertThat(tenant["code"]).isEqualTo(result.tenantCode)

        val owner = jdbcTemplate.queryForMap("select * from users where id = ?", result.userId)
        assertThat(owner["tenant_id"]).isEqualTo(result.tenantId)
        assertThat(owner["role"]).isEqualTo("OWNER")
        assertThat(owner["username"]).isEqualTo("owner")
        assertThat(owner["email"]).isEqualTo(email)
    }

    @Test
    fun `stores a hash instead of the raw password`() {
        val result = signUpService.signUp(SignUpCommand("Acme Film", uniqueEmail(), "owner", "s3cret-pass"))

        val hash = jdbcTemplate.queryForObject("select password_hash from users where id = ?", String::class.java, result.userId)
        assertThat(hash).isNotEqualTo("s3cret-pass").startsWith("{bcrypt}")
        assertThat(passwordEncoder.matches("s3cret-pass", hash)).isTrue()
    }

    @Test
    fun `stores the email in lowercase`() {
        val email = uniqueEmail()

        val result = signUpService.signUp(SignUpCommand("Acme Film", email.uppercase(), "owner", "s3cret-pass"))

        val stored = jdbcTemplate.queryForObject("select email from users where id = ?", String::class.java, result.userId)
        assertThat(stored).isEqualTo(email)
    }

    @Test
    fun `rejects an email that is already used, ignoring case`() {
        val email = uniqueEmail()
        signUpService.signUp(SignUpCommand("Acme Film", email, "owner", "s3cret-pass"))
        val tenantsBefore = countTenants()

        assertThatThrownBy { signUpService.signUp(SignUpCommand("Other Co", email.uppercase(), "owner", "s3cret-pass")) }
            .isInstanceOfSatisfying(SignUpRejectedException::class.java) { assertThat(it.reason).isEqualTo(Reason.EMAIL_ALREADY_USED) }
        assertThat(countTenants()).isEqualTo(tenantsBefore)
    }

    private fun uniqueEmail() = "owner-${UUID.randomUUID()}@example.com"

    private fun countTenants() = jdbcTemplate.queryForObject("select count(*) from tenant", Long::class.java)
}
