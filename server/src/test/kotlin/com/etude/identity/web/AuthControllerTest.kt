package com.etude.identity.web

import com.etude.IntegrationTest
import com.etude.identity.application.SignUpCommand
import com.etude.identity.application.SignUpResult
import com.etude.identity.application.SignUpService
import com.etude.identity.domain.User
import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import com.etude.platform.TenantContext
import com.etude.platform.security.CurrentTenantTestController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.util.UUID

@IntegrationTest
class AuthControllerTest(
    @Autowired private val mvc: MockMvcTester,
    @Autowired private val signUpService: SignUpService,
    @Autowired private val userRepository: UserRepository,
    @Autowired private val passwordEncoder: PasswordEncoder,
) {

    @Test
    fun `owner signs in with email and password`() {
        val company = signUp()

        val result = ownerLogin(company.email.uppercase(), PASSWORD)

        assertThat(result).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"userId":"${company.result.userId}","role":"OWNER","username":"owner"}""")
        assertThat(mvc.get().uri("/admin/v1/auth/session").session(sessionOf(result)).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"role":"OWNER","username":"owner"}""")
    }

    @Test
    fun `staff signs in with company code, username and password`() {
        val company = signUp()
        val staffId = registerStaff(company.result.tenantId, "kim")

        val result = staffLogin(company.result.tenantCode, "kim", PASSWORD)

        assertThat(result).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"userId":"$staffId","role":"STAFF","username":"kim"}""")
    }

    @Test
    fun `rejects a wrong password and an unknown email alike`() {
        val company = signUp()

        assertInvalidCredentials(ownerLogin(company.email, "wrong-password"))
        assertInvalidCredentials(ownerLogin("nobody-${UUID.randomUUID()}@example.com", PASSWORD))
    }

    @Test
    fun `rejects staff sign-in with a wrong company code, username or password`() {
        val company = signUp()
        registerStaff(company.result.tenantId, "kim")
        val otherCompany = signUp()

        assertInvalidCredentials(staffLogin(otherCompany.result.tenantCode, "kim", PASSWORD))
        assertInvalidCredentials(staffLogin(company.result.tenantCode, "lee", PASSWORD))
        assertInvalidCredentials(staffLogin(company.result.tenantCode, "kim", "wrong-password"))
    }

    @Test
    fun `an owner cannot use staff sign-in`() {
        val company = signUp()

        assertInvalidCredentials(staffLogin(company.result.tenantCode, "owner", PASSWORD))
    }

    @Test
    fun `changes the session id on sign-in`() {
        val company = signUp()
        val csrf = mvc.get().uri("/admin/v1/auth/csrf").exchange()
        val anonymousSession = sessionOf(csrf)
        val idBefore = anonymousSession.id

        val result = mvc.post().uri("/admin/v1/auth/owner-login")
            .session(anonymousSession)
            .header(csrfHeader(csrf), csrfToken(csrf))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"${company.email}","password":"$PASSWORD"}""")
            .exchange()

        assertThat(result).hasStatusOk()
        assertThat(sessionOf(result).id).isNotEqualTo(idBefore)
    }

    @Test
    fun `requires a CSRF token to sign in`() {
        val company = signUp()

        val result = mvc.post().uri("/admin/v1/auth/owner-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"${company.email}","password":"$PASSWORD"}""")
            .exchange()

        assertProblem(result, HttpStatus.FORBIDDEN, "CSRF_TOKEN_INVALID")
    }

    @Test
    fun `requires sign-in for the admin API`() {
        assertProblem(mvc.get().uri("/admin/v1/auth/session").exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
        assertProblem(mvc.get().uri("/admin/v1/inventory-items/{id}", UUID.randomUUID()).exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
    }

    @Test
    fun `signs out`() {
        val company = signUp()
        val session = sessionOf(ownerLogin(company.email, PASSWORD))

        assertThat(mvc.post().uri("/admin/v1/auth/logout").session(session).with(csrf()).exchange()).hasStatus(HttpStatus.NO_CONTENT)

        assertProblem(mvc.get().uri("/admin/v1/auth/session").session(session).exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
    }

    @Test
    fun `runs each request as the signed-in user's company`() {
        val company = signUp()
        registerStaff(company.result.tenantId, "kim")
        val session = sessionOf(staffLogin(company.result.tenantCode, "kim", PASSWORD))

        assertThat(mvc.get().uri(CurrentTenantTestController.PATH).session(session).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"tenantId":"${company.result.tenantId}"}""")
        assertThat(mvc.get().uri(CurrentTenantTestController.PATH).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"tenantId":null}""")
    }

    private data class Company(val email: String, val result: SignUpResult)

    private fun signUp(): Company {
        val email = "owner-${UUID.randomUUID()}@example.com"
        return Company(email, signUpService.signUp(SignUpCommand("Acme Film", email, "owner", PASSWORD)))
    }

    private fun registerStaff(tenantId: UUID, username: String): UUID = TenantContext.runAsPlatform {
        checkNotNull(userRepository.save(User(tenantId, UserRole.STAFF, username, null, checkNotNull(passwordEncoder.encode(PASSWORD)))).id)
    }

    private fun ownerLogin(email: String, password: String): MvcTestResult = mvc.post().uri("/admin/v1/auth/owner-login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"email":"$email","password":"$password"}""")
        .exchange()

    private fun staffLogin(companyCode: String, username: String, password: String): MvcTestResult = mvc.post().uri("/admin/v1/auth/staff-login")
        .with(csrf())
        .contentType(MediaType.APPLICATION_JSON)
        .content("""{"companyCode":"$companyCode","username":"$username","password":"$password"}""")
        .exchange()

    private fun sessionOf(result: MvcTestResult): MockHttpSession = result.mvcResult.request.getSession(false) as MockHttpSession

    private fun csrfHeader(result: MvcTestResult): String = Regex(""""headerName":"([^"]+)"""").find(result.mvcResult.response.contentAsString)!!.groupValues[1]

    private fun csrfToken(result: MvcTestResult): String = Regex(""""token":"([^"]+)"""").find(result.mvcResult.response.contentAsString)!!.groupValues[1]

    private fun assertInvalidCredentials(result: MvcTestResult) = assertProblem(result, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS")

    private fun assertProblem(result: MvcTestResult, status: HttpStatus, code: String) = assertThat(result)
        .hasStatus(status)
        .bodyJson().isLenientlyEqualTo("""{"code":"$code"}""")

    private companion object {
        const val PASSWORD = "s3cret-pass"
    }
}
