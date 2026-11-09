package com.etude.platform.security

import com.etude.platform.TenantContext
import com.etude.platform.TenantPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * 요청을 로그인한 사용자의 회사로 실행한다(ADR-0006). 보안 필터 뒤, 트랜잭션 앞에 있어야 한다.
 */
class TenantBindingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        if (principal is TenantPrincipal) {
            TenantContext.runAs(principal.tenantId) { filterChain.doFilter(request, response) }
        } else {
            // 회원가입과 로그인은 각자 플랫폼 블록을 건다.
            filterChain.doFilter(request, response)
        }
    }
}
