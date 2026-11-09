package com.etude.platform.security

import com.etude.platform.TenantContext
import com.etude.platform.TenantPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Runs the rest of each request as the signed-in user's tenant (ADR-0006). It sits after the security filters, so
 * the signed-in user is known, and before every controller and transaction, so Hibernate sees the tenant.
 */
class TenantBindingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        if (principal is TenantPrincipal) {
            TenantContext.runAs(principal.tenantId) { filterChain.doFilter(request, response) }
        } else {
            // Sign-up and sign-in bind platform work themselves.
            filterChain.doFilter(request, response)
        }
    }
}
