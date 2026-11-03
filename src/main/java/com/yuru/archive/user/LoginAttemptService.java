package com.yuru.archive.user;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Caffeine;

/*
 * ログイン失敗回数の管理。
 *
 * 2026-11-03 修正（doc/11-03checkCode.md 参照）
 * - 従来の「ユーザー名|IP」単位の制限に加え、「IP」単位の制限を追加した。
 *   ユーザー名を変えながら試行するパスワードスプレー攻撃を抑止する。
 * - 保存先を上限なしの ConcurrentHashMap から、件数上限・有効期限付きの Caffeine キャッシュへ変更した。
 *   ランダムなユーザー名で失敗を繰り返されてもメモリが増え続けないようにする。
 */
@Service
public class LoginAttemptService {

    static final int MAX_FAILURES = 5;
    static final int MAX_FAILURES_PER_IP = 20;
    static final Duration FAILURE_WINDOW = Duration.ofMinutes(10);
    static final Duration LOCK_DURATION = Duration.ofMinutes(10);
    static final long MAX_TRACKED_KEYS = 100_000;

    private final ConcurrentMap<String, AttemptState> attempts = Caffeine.newBuilder()
            .maximumSize(MAX_TRACKED_KEYS)
            .expireAfterWrite(FAILURE_WINDOW.plus(LOCK_DURATION))
            .<String, AttemptState>build()
            .asMap();

    public boolean loginFailed(String username, String remoteAddress) {
        Instant now = Instant.now();
        AttemptState userState = recordFailure(userKey(username, remoteAddress), MAX_FAILURES, now);
        AttemptState ipState = recordFailure(ipKey(remoteAddress), MAX_FAILURES_PER_IP, now);
        return userState.isLocked(now) || ipState.isLocked(now);
    }

    public boolean isBlocked(String username, String remoteAddress) {
        Instant now = Instant.now();
        return isLocked(userKey(username, remoteAddress), now) || isLocked(ipKey(remoteAddress), now);
    }

    public void loginSucceeded(String username, String remoteAddress) {
        // IP単位のカウンターは、成功1回でリセットされるとスプレー攻撃の抑止にならないため残す
        attempts.remove(userKey(username, remoteAddress));
    }

    private AttemptState recordFailure(String key, int maxFailures, Instant now) {
        return attempts.compute(key, (ignored, current) -> {
            if (current == null || current.shouldReset(now)) {
                return new AttemptState(1, now, maxFailures <= 1 ? now.plus(LOCK_DURATION) : null);
            }

            int failures = current.failures() + 1;
            Instant lockedUntil = failures >= maxFailures
                    ? now.plus(LOCK_DURATION)
                    : current.lockedUntil();
            return new AttemptState(failures, now, lockedUntil);
        });
    }

    private boolean isLocked(String key, Instant now) {
        AttemptState state = attempts.get(key);
        if (state == null) {
            return false;
        }
        if (state.shouldReset(now)) {
            attempts.remove(key, state);
            return false;
        }
        return state.isLocked(now);
    }

    private String userKey(String username, String remoteAddress) {
        String normalizedUser = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return "user:" + normalizedUser + '|' + normalizeAddress(remoteAddress);
    }

    private String ipKey(String remoteAddress) {
        return "ip:" + normalizeAddress(remoteAddress);
    }

    private String normalizeAddress(String remoteAddress) {
        return remoteAddress == null ? "unknown" : remoteAddress.trim();
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
