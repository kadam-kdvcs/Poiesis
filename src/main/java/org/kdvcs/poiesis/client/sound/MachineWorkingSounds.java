package org.kdvcs.poiesis.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;

import java.util.HashMap;
import java.util.Map;

/** Client-side owner for one looping working sound per machine position. */
public final class MachineWorkingSounds {
    private static final Map<BlockPos, MachineWorkingSound> ACTIVE = new HashMap<>();

    private MachineWorkingSounds() {
    }

    public static void update(BlockPos pos, SoundEvent sound, boolean working) {
        if (sound == null) return;

        MachineWorkingSound current = ACTIVE.get(pos);
        if (!working) {
            if (current != null) current.halt();
            ACTIVE.remove(pos);
            return;
        }

        if (current != null && !current.isStopped()) return;

        MachineWorkingSound next = new MachineWorkingSound(sound, pos);
        ACTIVE.put(pos.immutable(), next);
        Minecraft.getInstance().getSoundManager().play(next);
    }
}
