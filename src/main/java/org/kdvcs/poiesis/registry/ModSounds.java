package org.kdvcs.poiesis.registry;

import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.kdvcs.poiesis.Poiesis;

public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Poiesis.MOD_ID);

    private ModSounds() {
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
