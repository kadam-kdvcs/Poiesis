package org.kdvcs.poiesis.screen.menu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MenuDataCodecTest {
    @Test
    void roundTripsResourceSizedIntegers() {
        int[] values = {
                0,
                32_767,
                32_768,
                64_000,
                128_000,
                256_000,
                Integer.MAX_VALUE
        };

        for (int value : values) {
            int decoded = MenuDataCodec.int32(
                    MenuDataCodec.lowWord(value),
                    MenuDataCodec.highWord(value)
            );
            assertEquals(value, decoded, "failed to round-trip " + value);
        }
    }

    @Test
    void decodesSignedNetworkWordsAsUnsignedHalves() {
        int value = 64_000;
        short transmittedLow = (short) MenuDataCodec.lowWord(value);
        short transmittedHigh = (short) MenuDataCodec.highWord(value);

        assertEquals(value, MenuDataCodec.int32(transmittedLow, transmittedHigh));
    }
}
