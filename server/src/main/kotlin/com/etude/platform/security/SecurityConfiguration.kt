package com.etude.platform.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.security.web.context.SecurityContextRepository
import org.springframework.security.web.csrf.CsrfTokenRepository
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository

/**
 * Server-side session sign-in for the admin API (#83): the browser holds only the session cookie, and every state
 * change needs a CSRF token. Every `/admin/v1` path needs a signed-in user except the sign-in endpoints; the customer API
 * stays open until the homepage slice decides how visitors are identified.
 */
@Configuration(proxyBeanMethods = false)
class SecurityConfiguration {

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        problems: SecurityProblemWriter,
        securityContextRepository: SecurityContextRepository,
        csrfTokenRepository: CsrfTokenRepository,
    ): SecurityFilterChain = http
        .authorizeHttpRequests {
            it.requestMatchers(*PUBLIC_ADMIN_PATHS).permitAll()
                .requestMatchers("/admin/v1/**").authenticated()
                .anyRequest().permitAll()
        }
        .csrf {
            it.csrfTokenRepository(csrfTokenRepository)
                // The customer API has no sign-in yet, so there is no session for a forged request to ride on.
                .ignoringRequestMatchers("/api/v1/**")
        }
        .securityContext { it.securityContextRepository(securityContextRepository) }
        .exceptionHandling {
            it.authenticationEntryPoint(problems::authenticationRequired)
                .accessDeniedHandler(problems::accessDenied)
        }
        .formLogin { it.disable() }
        .httpBasic { it.disable() }
        .logout { it.disable() }
        .requestCache { it.disable() }
        .addFilterAfter(TenantBindingFilter(), AuthorizationFilter::class.java)
        .build()

    /** Shared with the sign-in endpoints, which save the signed-in user to the session themselves. */
    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    /** Shared with the sign-in endpoints, which drop the token so the client fetches a new one after signing in. */
    @Bean
    fun csrfTokenRepository(): CsrfTokenRepository = HttpSessionCsrfTokenRepository()

    private companion object {
        val PUBLIC_ADMIN_PATHS = arrayOf("/admin/v1/auth/owner-login", "/admin/v1/auth/staff-login", "/admin/v1/auth/csrf")
    }
}
