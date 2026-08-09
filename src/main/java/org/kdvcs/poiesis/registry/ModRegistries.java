package org.kdvcs.poiesis.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register(IEventBus eventBus) {
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModCreativeTabs.register(eventBus);
        ModEntities.register(eventBus);
        ModParticles.register(eventBus);
        ModFluids.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipeTypes.register(eventBus);
        ModRecipeSerializers.register(eventBus);
        ModSounds.register(eventBus);
        ModMobEffects.register(eventBus);
        ModPotions.register(eventBus);
        ModEnchantments.register(eventBus);
        ModWorldgenFeatures.register(eventBus);
    }
}
