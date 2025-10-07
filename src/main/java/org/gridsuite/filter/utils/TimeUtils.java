package org.gridsuite.filter.utils;

import com.google.common.annotations.VisibleForTesting;
import lombok.Getter;
import lombok.Setter;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

/**
 * Utility class for datetime to permit during tests to mock time related operations.
 * @apiNote This class is to permit tests to intercept date/time
 */

public final class TimeUtils {
    private TimeUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    @Setter(onMethod_ = {@VisibleForTesting})
    @Getter
    private static Clock clock = Clock.systemUTC();

    public static Instant now() {
        return Instant.now(clock);
    }

    public static Date nowAsDate() {
        return Date.from(now());
    }
}
