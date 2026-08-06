package org.kdvcs.poiesis.machine.api;

import net.minecraft.nbt.CompoundTag;

public interface MachineComponent {
    String key();

    default void tickServer(MachineContext context) {
    }

    default void save(CompoundTag tag) {
    }

    default void load(CompoundTag tag) {
    }

    default void invalidate() {
    }
}
