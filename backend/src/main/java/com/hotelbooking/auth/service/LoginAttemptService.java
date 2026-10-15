package com.hotelbooking.auth.service;

import com.hotelbooking.common.exception.TooManyRequestsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Locks an email out of login for {@code lockout} after {@code maxAttempts} failed attempts within
 * that same period. Kept in memory (enough for a single instance) and counted for unknown emails
 * too, so the lockout does not reveal which accounts exist.
 */
@Service
public class LoginAttemptService {

    /** Above this many tracked emails, expired entries are purged to bound memory. */
    private static final int PURGE_THRESHOLD = 10_000;

    private final int maxAttempts;
    private final Duration lockout;
    private final Clock clock;
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public LoginAttemptService(@Value("${app.security.login.max-attempts:5}") int maxAttempts,
                               @Value("${app.security.login.lockout:15m}") Duration lockout,
                               Clock clock) {
        this.maxAttempts = maxAttempts;
        this.lockout = lockout;
        this.clock = clock;
    }

    /** Throws {@link TooManyRequestsException} while the email is locked out. */
    public void checkAllowed(String email) {
        Attempts entry = attempts.get(email);
        Instant now = clock.instant();
        if (entry != null && entry.lockedUntil != null && now.isBefore(entry.lockedUntil)) {
            throw new TooManyRequestsException(
                    "Too many failed login attempts. Try again later.",
                    Duration.between(now, entry.lockedUntil));
        }
    }

    public void recordFailure(String email) {
        Instant now = clock.instant();
        attempts.compute(email, (key, entry) -> {
            if (entry == null || entry.isExpired(now)) {
                entry = new Attempts(now);
            }
            entry.failures++;
            if (entry.failures >= maxAttempts) {
                entry.lockedUntil = now.plus(lockout);
            }
            return entry;
        });
        if (attempts.size() > PURGE_THRESHOLD) {
            attempts.values().removeIf(entry -> entry.isExpired(now));
        }
    }

    public void recordSuccess(String email) {
        attempts.remove(email);
    }

    private final class Attempts {
        private final Instant windowStart;
        private int failures;
        private Instant lockedUntil;

        private Attempts(Instant windowStart) {
            this.windowStart = windowStart;
        }

        /** The counting window has passed and any lockout is over. */
        private boolean isExpired(Instant now) {
            boolean windowOver = !now.isBefore(windowStart.plus(lockout));
            boolean lockOver = lockedUntil == null || !now.isBefore(lockedUntil);
            return windowOver && lockOver;
        }
    }
}
