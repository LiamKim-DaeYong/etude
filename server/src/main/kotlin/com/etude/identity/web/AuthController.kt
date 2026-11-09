package com.etude.identity.web

import com.etude.identity.application.SignedInUser
import com.etude.identity.application.StaffAuthenticationToken
import com.etude.platform.TenantContext
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Auth")
@RequestMapping("/admin/v1/auth")
class AuthController(
    private val authenticationManager: AuthenticationManager,
    private val securityContextRepository: SecurityContextRepository,
    private val csrfTokenRepository: CsrfTokenRepository,
) {

    @PostMapping("/owner-login")
    @Operation(summary = "Sign in as an owner", description = "Email and password. Fetch a new CSRF token afterwards.")
    fun loginAsOwner(
        @Valid @RequestBody body: OwnerLoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SessionResponse = signIn(UsernamePasswordAuthenticationToken.unauthenticated(body.email.lowercase(), body.password), request, response)

    @PostMapping("/staff-login")
    @Operation(summary = "Sign in as staff", description = "Company code, username and password. Fetch a new CSRF token afterwards.")
    fun loginAsStaff(
        @Valid @RequestBody body: StaffLoginRequest,
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): SessionResponse = signIn(StaffAuthenticationToken(body.companyCode, body.username, body.password), request, response)

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sign out")
    fun logout(
        request: HttpServletRequest,
        response: HttpServletResponse,
        @Parameter(hidden = true) authentication: Authentication?,
    ) {
        SecurityContextLogoutHandler().logout(request, response, authentication)
    }

    @GetMapping("/session")
    @Operation(summary = "Get the signed-in user")
    fun getSession(@Parameter(hidden = true) @AuthenticationPrincipal user: SignedInUser): SessionResponse = SessionResponse.from(user)

    @GetMapping("/csrf")
    @Operation(summary = "Get a CSRF token", description = "Send it in the given header with every state-changing request.")
    fun getCsrfToken(@Parameter(hidden = true) token: CsrfToken): CsrfTokenResponse = CsrfTokenResponse(token.headerName, token.token)

    /**
     * Checks the credentials as platform work (the tenant is not known yet), then keeps the user in a new session.
     * Changing the session id stops session fixation; dropping the CSRF token makes the client fetch a new one.
     */
    private fun signIn(token: Authentication, request: HttpServletRequest, response: HttpServletResponse): SessionResponse {
        val authentication = TenantContext.runAsPlatform { authenticationManager.authenticate(token) }

        request.getSession(true)
        request.changeSessionId()
        val context = SecurityContextHolder.createEmptyContext().apply { this.authentication = authentication }
        SecurityContextHolder.setContext(context)
        securityContextRepository.saveContext(context, request, response)
        csrfTokenRepository.saveToken(null, request, response)

        return SessionResponse.from(authentication.principal as SignedInUser)
    }
}
