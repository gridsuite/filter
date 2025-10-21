/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import lombok.Getter;

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

    @Getter
    private static Clock clock = Clock.systemUTC();

    public static Instant now() {
        return Instant.now(clock);
    }

    public static Date nowAsDate() {
        return Date.from(now());
    }
}
