package com.etude.identity.application

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration(proxyBeanMethods = false)
class PasswordEncoderConfiguration {

    /** 해시에 알고리즘 접두어(`{bcrypt}`)를 붙여, 알고리즘을 바꿔도 기존 해시를 확인할 수 있다. */
    @Bean
    fun passwordEncoder(): PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
}
