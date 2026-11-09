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
 * 관리자 API 보안 설정. 서버 세션과 CSRF 토큰을 쓴다(#83).
 * 고객 API는 방문자 식별 방식을 정할 때까지 열어 둔다.
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
                // 세션이 없는 고객 API는 CSRF 대상이 아니다.
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

    /** 로그인 API가 인증 결과를 세션에 저장할 때도 쓴다. */
    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    /** 로그인 API가 로그인 후 토큰을 지울 때도 쓴다. */
    @Bean
    fun csrfTokenRepository(): CsrfTokenRepository = HttpSessionCsrfTokenRepository()

    private companion object {
        val PUBLIC_ADMIN_PATHS = arrayOf("/admin/v1/auth/owner-login", "/admin/v1/auth/staff-login", "/admin/v1/auth/csrf")
    }
}
