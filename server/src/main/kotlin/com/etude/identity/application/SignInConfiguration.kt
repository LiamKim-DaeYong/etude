package com.etude.identity.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration(proxyBeanMethods = false)
class SignInConfiguration {

    /** 대표 로그인은 [OwnerUserDetailsService]로, 직원 로그인은 [StaffAuthenticationProvider]로 보낸다. */
    @Bean
    fun authenticationManager(
        ownerUserDetailsService: OwnerUserDetailsService,
        staffAuthenticationProvider: StaffAuthenticationProvider,
        passwordEncoder: PasswordEncoder,
    ): AuthenticationManager = ProviderManager(
        DaoAuthenticationProvider(ownerUserDetailsService).apply { setPasswordEncoder(passwordEncoder) },
        staffAuthenticationProvider,
    )
}
