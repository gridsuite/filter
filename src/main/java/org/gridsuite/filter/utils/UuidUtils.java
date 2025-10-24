/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.google.common.annotations.VisibleForTesting;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Utility class for UUID to permit during tests to mock {@link UUID} generation.
 * @apiNote This class is to permit tests to intercept {@link UUID} generation
 */
public final class UuidUtils {
    private UuidUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static Supplier<UUID> uuidSupplier = UUID::randomUUID;

    public static UUID generateUUID() {
        return uuidSupplier.get();
    }

    /**
     * Like {@link UUID#randomUUID()} generate UUID v4 variant 2, but not random.<br/>
     * This utility function is intended to be used in tests to have predictable UUID.
     * <p>Some information on UUID:
     * <pre>
     * textual format: xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
     *          bytes:  0 1 2 3  4 5  6 7  8 9 10 ↓12 ↓14 ↓
     *                                           11  13  15
     * size: 4b-2b-2b-2b-6b
     * </pre>
     * Where<ul>
     * <li>M = version field (4 bits).</li>
     * <li>N = variant field (3 bits).</li>
     * <li>All other bits = random (for v4).</li></ul></p>
     *
     * <p>UUID bytes structure:
     * <pre>
     *  /-------mostSigBits-------\   /--------leastSigBits---------\
     *  b0 b1 b2 b3   b4 b5   b6 b7   b8 b9   b10 b11 b12 b13 b14 b15
     * |-----------| |-----| |-----| |-----| |-----------------------|
     *    random    random v4+random variant+random    random
     * b6 = [0100 LLLL] ⇒ version 4
     * b8 = [10xL LLLL] → [1000 LLLL] ⇒ RFC 4122 / IETF / Leach–Salz variant
     * </pre></p>
     *
     * @param b0to3             the bits from b0 to b3
     * @param b4to5             the bits from b4 and b5
     * @param b7                the bits of b7
     * @param nibblesB6lowB8low the upper nibble is for the lower half of b6
     *                          and the lower nibble is for the lower half of b8
     * @param b9                the bits of b9
     * @param b10to11           the bits for b10 and b11
     * @param b12to15           the bits for b12 to b15
     * @return a UUID v4 in variant 2 with the bits passed
     * @implNote Bits 5–4 of b8 are left to {@code 0} for simplicity,
     * so the UUID is of the form {@code xxxxxxxx-xxxx-4xxx-8xxx-xxxxxxxxxxxx}.
     */
    // regexp: ^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-8[0-9a-f]{3}-[0-9a-f]{12}$
    @VisibleForTesting
    public static UUID createUUID(final int b0to3, final short b4to5, final byte b7,
                                  final byte nibblesB6lowB8low,
                                  final byte b9, final short b10to11, final int b12to15) {
        return new UUID(
                // Construct mostSigBits directly
                (b0to3 & 0xFFFFFFFFL) << 32
                        | (b4to5 & 0xFFFFL) << 16
                        | (0x40L | ((nibblesB6lowB8low >>> 4) & 0x0FL)) << 8 // version 4
                        | (b7 & 0xFFL),
                // Construct leastSigBits directly
                (0x80L | (nibblesB6lowB8low & 0xFL)) << 56 // variant 2
                        | (b9 & 0xFFL) << 48
                        | (b10to11 & 0xFFFFL) << 32
                        | (b12to15 & 0xFFFFFFFFL)
        );
    }

    /**
     * @see #createUUID(int, short, byte, byte, byte, short, int)
     */
    @VisibleForTesting
    public static UUID createUUID(final int b12to15) {
        return createUUID(0, (short) 0, (byte) 0, (byte) 0, (byte) 0, (short) 0, b12to15);
    }
}
