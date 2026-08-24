package org.kdvcs.poiesis.registry;

import net.minecraftforge.eventbus.api.IEventBus;

public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register(IEventBus eventBus) {
        ModCrops.bootstrap();
        ModFluids.register(eventBus);
        ModBlocks.register(eventBus);
        ModItems.register(eventBus);
        ModCreativeTabs.register(eventBus);
        ModBlockEntities.register(eventBus);
        ModMenuTypes.register(eventBus);
        ModRecipeTypes.register(eventBus);
        ModRecipeSerializers.register(eventBus);
        ModSounds.register(eventBus);
        ModLootModifiers.register(eventBus);
        ModWorldgenFeatures.register(eventBus);
    }
}
