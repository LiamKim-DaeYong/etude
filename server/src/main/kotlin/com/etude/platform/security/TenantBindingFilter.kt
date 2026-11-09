package com.etude.platform.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Runs the rest of each request as the signed-in user's tenant (ADR-0006). It sits after the security filters, so
 * the signed-in user is known, and before every controller and transaction, so Hibernate sees the tenant.
 */
class TenantBindingFilter : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        // TODO: Run the rest of the request as the signed-in user's tenant.
        //  - The signed-in user: SecurityContextHolder.getContext().authentication?.principal
        //  - If it is a TenantPrincipal, call filterChain.doFilter(request, response) inside TenantContext.runAs(it.tenantId) { }
        //  - Otherwise call filterChain.doFilter(request, response) as is: sign-up and sign-in bind platform work themselves.
        filterChain.doFilter(request, response)
    }
}
