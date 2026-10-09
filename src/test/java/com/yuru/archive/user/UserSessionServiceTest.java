package com.yuru.archive.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {

    @Mock
    private SessionRegistry sessionRegistry;

    @Test
    void expiresOtherSessionsButKeepsCurrentSession() {
        UserDetails principal = User.withUsername("tester").password("ignored").roles("USER").build();
        SessionInformation current = new SessionInformation(principal, "session-A", new Date());
        SessionInformation other = new SessionInformation(principal, "session-B", new Date());

        when(sessionRegistry.getAllPrincipals()).thenReturn(List.of(principal));
        when(sessionRegistry.getAllSessions(principal, false)).thenReturn(List.of(current, other));

        UserSessionService service = new UserSessionService(sessionRegistry);
        int expired = service.expireOtherSessions("tester", "session-A");

        assertEquals(1, expired);
        assertFalse(current.isExpired());
        assertTrue(other.isExpired());
    }
}
