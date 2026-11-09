package com.etude

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * 로그인한 직원과 CSRF 토큰으로 요청한다. 회사에 속하지 않는 사용자라 회사는 설정되지 않는다.
 */
fun MockMvcTester.MockMvcRequestBuilder.asStaff(): MockMvcTester.MockMvcRequestBuilder = with(user("staff").roles("STAFF")).with(csrf())
