package com.yuru.archive.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginRateLimitFilterTest {

    @Test
    void blockedLoginIsRedirectedBeforeAuthentication() throws Exception {
        LoginAttemptService attempts = new LoginAttemptService();
        for (int i = 0; i < 5; i++) {
            attempts.loginFailed("tester", "127.0.0.1");
        }

        LoginRateLimitFilter filter = new LoginRateLimitFilter(attempts);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/user/login");
        request.setServletPath("/user/login");
        request.setRemoteAddr("127.0.0.1");
        request.addParameter("username", "tester");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(302, response.getStatus());
        assertEquals("/user/login?blocked", response.getRedirectedUrl());
    }
}
