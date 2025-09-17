package org.gridsuite.filter.utils;

import com.google.common.annotations.VisibleForTesting;
import lombok.Setter;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utility class for UUID to permit during tests to mock {@link UUID} generation..
 */
@VisibleForTesting
public final class TimeUtils {
    private TimeUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    @Setter
    private static Supplier<UUID> uuidSupplier = UUID::randomUUID;

    public static UUID generateUUID() {
        return uuidSupplier.get();
    }
}
