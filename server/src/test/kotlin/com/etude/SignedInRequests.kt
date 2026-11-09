package com.etude

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * 로그인한 직원으로, 올바른 CSRF 토큰과 함께 요청을 보낸다. 로그인 자체를 다루지 않는 관리자 API 테스트에 쓴다.
 * 이 테스트 사용자는 회사에 속하지 않아 회사가 걸리지 않는다.
 */
fun MockMvcTester.MockMvcRequestBuilder.asStaff(): MockMvcTester.MockMvcRequestBuilder = with(user("staff").roles("STAFF")).with(csrf())
