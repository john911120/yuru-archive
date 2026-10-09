package com.yuru.archive.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    @Test
    void blocksAfterFiveFailuresAndSuccessClearsState() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < 4; i++) {
            assertFalse(service.loginFailed("tester", "127.0.0.1"));
        }
        assertTrue(service.loginFailed("tester", "127.0.0.1"));
        assertTrue(service.isBlocked("tester", "127.0.0.1"));

        service.loginSucceeded("tester", "127.0.0.1");
        assertFalse(service.isBlocked("tester", "127.0.0.1"));
    }

    @Test
    void doesNotMixDifferentUsers() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < 5; i++) {
            service.loginFailed("user-a", "127.0.0.1");
        }

        assertTrue(service.isBlocked("user-a", "127.0.0.1"));
        assertFalse(service.isBlocked("user-b", "127.0.0.1"));
    }
}
