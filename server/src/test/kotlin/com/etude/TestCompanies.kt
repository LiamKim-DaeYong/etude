package com.etude

import com.etude.identity.application.SignUpCommand
import com.etude.identity.application.SignUpService
import com.etude.identity.domain.User
import com.etude.identity.domain.UserRepository
import com.etude.identity.domain.UserRole
import com.etude.platform.TenantContext
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.stereotype.Component
import org.springframework.test.web.servlet.assertj.MockMvcTester
import java.util.UUID

/** 테스트에서 쓸 회사를 만들고, 그 회사 사용자로 로그인한 세션을 돌려준다. */
@Component
class TestCompanies(
    private val signUpService: SignUpService,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val mvc: MockMvcTester,
) {

    /** 회원가입으로 회사와 대표(아이디 `owner`)를 만든다. 이메일은 매번 다르다. */
    fun create(name: String = "Acme Film"): TestCompany {
        val email = "owner-${UUID.randomUUID()}@example.com"
        val result = signUpService.signUp(SignUpCommand(name, email, OWNER_USERNAME, PASSWORD))
        return TestCompany(result.tenantId, result.tenantCode, result.userId, email)
    }

    /** 회사에 직원을 등록한다. 직원 등록 API가 생기기 전까지 저장소로 직접 만든다. */
    fun registerStaff(company: TestCompany, username: String): UUID = TenantContext.runAsPlatform {
        val user = User(company.tenantId, UserRole.STAFF, username, null, checkNotNull(passwordEncoder.encode(PASSWORD)))
        checkNotNull(userRepository.save(user).id)
    }

    /** 대표로 로그인한 세션. */
    fun ownerSession(company: TestCompany): MockHttpSession = signIn("/admin/v1/auth/owner-login", """{"email":"${company.ownerEmail}","password":"$PASSWORD"}""")

    /** 직원을 등록하고 그 직원으로 로그인한 세션. */
    fun staffSession(company: TestCompany, username: String = "staff"): MockHttpSession {
        registerStaff(company, username)
        return signIn("/admin/v1/auth/staff-login", """{"companyCode":"${company.code}","username":"$username","password":"$PASSWORD"}""")
    }

    private fun signIn(path: String, body: String): MockHttpSession {
        val result = mvc.post().uri(path).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(body).exchange()
        check(result.mvcResult.response.status == 200) { "Sign-in failed: ${result.mvcResult.response.contentAsString}" }
        return result.mvcResult.request.getSession(false) as MockHttpSession
    }

    companion object {
        const val OWNER_USERNAME = "owner"
        const val PASSWORD = "s3cret-pass"
    }
}

data class TestCompany(
    val tenantId: UUID,
    val code: String,
    val ownerId: UUID,
    val ownerEmail: String,
)
