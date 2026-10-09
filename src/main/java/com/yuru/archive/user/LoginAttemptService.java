package com.yuru.archive.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);
    static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    private final Map<String, AttemptState> attempts = new ConcurrentHashMap<>();

    public boolean loginFailed(String username, String remoteAddress) {
        String key = key(username, remoteAddress);
        Instant now = Instant.now();

        AttemptState updated = attempts.compute(key, (ignored, current) -> {
            if (current == null || current.shouldReset(now)) {
                return new AttemptState(1, now, null);
            }

            int failures = current.failures() + 1;
            Instant lockedUntil = failures >= MAX_FAILURES
                    ? now.plus(LOCK_DURATION)
                    : current.lockedUntil();
            return new AttemptState(failures, now, lockedUntil);
        });

        return updated.isLocked(now);
    }

    public boolean isBlocked(String username, String remoteAddress) {
        String key = key(username, remoteAddress);
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }

        Instant now = Instant.now();
        if (state.shouldReset(now)) {
            attempts.remove(key, state);
            return false;
        }
        return state.isLocked(now);
    }

    public void loginSucceeded(String username, String remoteAddress) {
        attempts.remove(key(username, remoteAddress));
    }

    private String key(String username, String remoteAddress) {
        String normalizedUser = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        String normalizedAddress = remoteAddress == null ? "unknown" : remoteAddress.trim();
        return normalizedUser + '|' + normalizedAddress;
    }

    private record AttemptState(int failures, Instant lastFailure, Instant lockedUntil) {
        boolean isLocked(Instant now) {
            return lockedUntil != null && now.isBefore(lockedUntil);
        }

        boolean shouldReset(Instant now) {
            if (lockedUntil != null) {
                return !now.isBefore(lockedUntil);
            }
            return lastFailure.plus(FAILURE_WINDOW).isBefore(now);
        }
    }
}
