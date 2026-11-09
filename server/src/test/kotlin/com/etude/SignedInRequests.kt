package com.etude

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.assertj.MockMvcTester

/**
 * Sends the request as a signed-in staff member with a valid CSRF token, for tests of admin endpoints that are not
 * about sign-in. The test user belongs to no tenant, so no tenant is bound.
 */
fun MockMvcTester.MockMvcRequestBuilder.asStaff(): MockMvcTester.MockMvcRequestBuilder = with(user("staff").roles("STAFF")).with(csrf())
