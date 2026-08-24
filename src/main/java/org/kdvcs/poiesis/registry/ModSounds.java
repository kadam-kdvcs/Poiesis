package org.kdvcs.poiesis.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;

public final class ModSounds {
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Poiesis.MOD_ID);

    public static final RegistryObject<SoundEvent> ALLOY_FACTORY_WORKING =
            register("alloy_factory_working");
    public static final RegistryObject<SoundEvent> BIOMASS_GASIFIER_WORKING =
            register("biomass_gasifier_working");
    public static final RegistryObject<SoundEvent> COMPOUNDER_WORKING =
            register("compounder_working");
    public static final RegistryObject<SoundEvent> MECHANICAL_PROCESSOR_WORKING =
            register("mechanical_processor_working");
    public static final RegistryObject<SoundEvent> MODULE_FABRICATOR_WORKING =
            register("module_fabricator_working");
    public static final RegistryObject<SoundEvent> SEPARATOR_WORKING =
            register("separator_working");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(
                new ResourceLocation(Poiesis.MOD_ID, name)
        ));
    }

    public static void register(IEventBus eventBus) {
        SOUNDS.register(eventBus);
    }
}
