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
 * 관리자 API의 서버 세션 로그인(#83). 브라우저는 세션 쿠키만 갖고, 상태를 바꾸는 요청에는 CSRF 토큰이 필요하다.
 * `/admin/v1` 경로는 로그인 API를 빼고 모두 로그인이 필요하다. 고객 API는 홈페이지 슬라이스에서 방문자 식별 방식을
 * 정할 때까지 열어 둔다.
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
                // 고객 API에는 아직 로그인이 없어서, 위조 요청이 올라탈 세션이 없다.
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

    /** 로그인 API와 함께 쓴다. 로그인 API가 로그인한 사용자를 직접 세션에 저장한다. */
    @Bean
    fun securityContextRepository(): SecurityContextRepository = HttpSessionSecurityContextRepository()

    /** 로그인 API와 함께 쓴다. 로그인 API가 토큰을 지워 클라이언트가 로그인 뒤 새 토큰을 받게 한다. */
    @Bean
    fun csrfTokenRepository(): CsrfTokenRepository = HttpSessionCsrfTokenRepository()

    private companion object {
        val PUBLIC_ADMIN_PATHS = arrayOf("/admin/v1/auth/owner-login", "/admin/v1/auth/staff-login", "/admin/v1/auth/csrf")
    }
}
