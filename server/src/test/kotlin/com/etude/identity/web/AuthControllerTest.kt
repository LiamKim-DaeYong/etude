package com.etude.identity.web

import com.etude.IntegrationTest
import com.etude.TestCompanies
import com.etude.TestCompanies.Companion.PASSWORD
import com.etude.platform.security.CurrentTenantTestController
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.assertj.MockMvcTester
import org.springframework.test.web.servlet.assertj.MvcTestResult
import java.util.UUID

@IntegrationTest
class AuthControllerTest(
    @Autowired private val mvc: MockMvcTester,
    @Autowired private val companies: TestCompanies,
) {

    @Test
    fun `owner signs in with email and password`() {
        val company = companies.create()

        val result = ownerLogin(company.ownerEmail.uppercase(), PASSWORD)

        assertThat(result).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"userId":"${company.ownerId}","role":"OWNER","username":"owner"}""")
        assertThat(mvc.get().uri("/admin/v1/auth/session").session(sessionOf(result)).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"role":"OWNER","username":"owner"}""")
    }

    @Test
    fun `staff signs in with company code, username and password`() {
        val company = companies.create()
        val staffId = companies.registerStaff(company, "kim")

        val result = staffLogin(company.code, "kim", PASSWORD)

        assertThat(result).hasStatusOk().bodyJson().isLenientlyEqualTo("""{"userId":"$staffId","role":"STAFF","username":"kim"}""")
    }

    @Test
    fun `rejects a wrong password and an unknown email alike`() {
        val company = companies.create()

        assertInvalidCredentials(ownerLogin(company.ownerEmail, "wrong-password"))
        assertInvalidCredentials(ownerLogin("nobody-${UUID.randomUUID()}@example.com", PASSWORD))
    }

    @Test
    fun `rejects staff sign-in with a wrong company code, username or password`() {
        val company = companies.create()
        companies.registerStaff(company, "kim")
        val otherCompany = companies.create()

        assertInvalidCredentials(staffLogin(otherCompany.code, "kim", PASSWORD))
        assertInvalidCredentials(staffLogin(company.code, "lee", PASSWORD))
        assertInvalidCredentials(staffLogin(company.code, "kim", "wrong-password"))
    }

    @Test
    fun `an owner cannot use staff sign-in`() {
        val company = companies.create()

        assertInvalidCredentials(staffLogin(company.code, TestCompanies.OWNER_USERNAME, PASSWORD))
    }

    @Test
    fun `changes the session id on sign-in`() {
        val company = companies.create()
        val csrf = mvc.get().uri("/admin/v1/auth/csrf").exchange()
        val anonymousSession = sessionOf(csrf)
        val idBefore = anonymousSession.id

        val result = mvc.post().uri("/admin/v1/auth/owner-login")
            .session(anonymousSession)
            .header(jsonField(csrf, "headerName"), jsonField(csrf, "token"))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"${company.ownerEmail}","password":"$PASSWORD"}""")
            .exchange()

        assertThat(result).hasStatusOk()
        assertThat(sessionOf(result).id).isNotEqualTo(idBefore)
    }

    @Test
    fun `requires a CSRF token to sign in`() {
        val company = companies.create()

        val result = mvc.post().uri("/admin/v1/auth/owner-login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"email":"${company.ownerEmail}","password":"$PASSWORD"}""")
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
        val session = companies.ownerSession(companies.create())

        assertThat(mvc.post().uri("/admin/v1/auth/logout").session(session).with(csrf()).exchange()).hasStatus(HttpStatus.NO_CONTENT)

        assertProblem(mvc.get().uri("/admin/v1/auth/session").session(session).exchange(), HttpStatus.UNAUTHORIZED, "AUTHENTICATION_REQUIRED")
    }

    @Test
    fun `runs each request as the signed-in user's company`() {
        val company = companies.create()
        val session = companies.staffSession(company)

        assertThat(mvc.get().uri(CurrentTenantTestController.PATH).session(session).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"tenantId":"${company.tenantId}"}""")
        assertThat(mvc.get().uri(CurrentTenantTestController.PATH).exchange())
            .hasStatusOk()
            .bodyJson().isLenientlyEqualTo("""{"tenantId":null}""")
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

    private fun jsonField(result: MvcTestResult, name: String): String = Regex(""""$name":"([^"]+)"""").find(result.mvcResult.response.contentAsString)!!.groupValues[1]

    private fun assertInvalidCredentials(result: MvcTestResult) = assertProblem(result, HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS")

    private fun assertProblem(result: MvcTestResult, status: HttpStatus, code: String) = assertThat(result)
        .hasStatus(status)
        .bodyJson().isLenientlyEqualTo("""{"code":"$code"}""")
}
