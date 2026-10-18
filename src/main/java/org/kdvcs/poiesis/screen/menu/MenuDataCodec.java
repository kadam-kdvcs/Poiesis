package org.kdvcs.poiesis.screen.menu;

import net.minecraft.world.inventory.ContainerData;

/**
 * Encodes values that travel through vanilla menu data slots.
 *
 * <p>Each slot is transferred as a signed 16-bit value by the 1.20.1
 * container protocol. Resource amounts therefore use two words instead of
 * relying on the sign or current capacity staying within a single word.</p>
 */
public final class MenuDataCodec {
    private MenuDataCodec() {
    }

    public static int lowWord(int value) {
        return value & 0xFFFF;
    }

    public static int highWord(int value) {
        return value >>> 16 & 0xFFFF;
    }

    public static int unsignedWord(ContainerData data, int index) {
        return data.get(index) & 0xFFFF;
    }

    public static int int32(ContainerData data, int lowIndex, int highIndex) {
        return int32(data.get(lowIndex), data.get(highIndex));
    }

    public static int int32(int lowWord, int highWord) {
        return (lowWord & 0xFFFF) | (highWord & 0xFFFF) << 16;
    }
}
