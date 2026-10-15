package com.hotelbooking.auth;

import com.hotelbooking.auth.service.LoginAttemptService;
import com.hotelbooking.common.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptServiceTest {

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptService service = new LoginAttemptService(3, Duration.ofMinutes(15), clock);

    @Test
    void locksAfterMaxFailuresUntilLockoutEnds() {
        fail(3);
        assertThatThrownBy(() -> service.checkAllowed("a@x.com"))
                .isInstanceOf(TooManyRequestsException.class);

        clock.advance(Duration.ofMinutes(14));
        assertThatThrownBy(() -> service.checkAllowed("a@x.com"))
                .isInstanceOf(TooManyRequestsException.class);

        clock.advance(Duration.ofMinutes(1));
        assertThatCode(() -> service.checkAllowed("a@x.com")).doesNotThrowAnyException();
    }

    @Test
    void failuresOutsideTheWindowDoNotAddUp() {
        fail(2);
        clock.advance(Duration.ofMinutes(16));
        fail(2);
        assertThatCode(() -> service.checkAllowed("a@x.com")).doesNotThrowAnyException();
    }

    @Test
    void successResetsTheCount() {
        fail(2);
        service.recordSuccess("a@x.com");
        fail(2);
        assertThatCode(() -> service.checkAllowed("a@x.com")).doesNotThrowAnyException();
    }

    @Test
    void lockoutIsPerEmail() {
        fail(3);
        assertThatCode(() -> service.checkAllowed("b@x.com")).doesNotThrowAnyException();
    }

    private void fail(int times) {
        for (int i = 0; i < times; i++) {
            service.recordFailure("a@x.com");
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-01-01T00:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
