package com.yuru.archive.user;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/*
 * 2026-11-03 追加: IP単位の試行制限（パスワードスプレー対策）の確認
 */
class LoginAttemptServiceIpLimitTest {

    @Test
    void blocksIpAfterManyFailuresAcrossDifferentUsernames() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < LoginAttemptService.MAX_FAILURES_PER_IP; i++) {
            service.loginFailed("user-" + i, "10.0.0.1");
        }

        // 一度も失敗していないユーザー名でも、同じIPからは試行できない
        assertTrue(service.isBlocked("brand-new-user", "10.0.0.1"));
        // 他のIPには影響しない
        assertFalse(service.isBlocked("brand-new-user", "10.0.0.2"));
    }

    @Test
    void ipCounterIsNotClearedBySingleSuccess() {
        LoginAttemptService service = new LoginAttemptService();

        for (int i = 0; i < LoginAttemptService.MAX_FAILURES_PER_IP - 1; i++) {
            service.loginFailed("user-" + i, "10.0.0.3");
        }
        service.loginSucceeded("user-0", "10.0.0.3");

        assertTrue(service.loginFailed("another-user", "10.0.0.3"));
    }
}
