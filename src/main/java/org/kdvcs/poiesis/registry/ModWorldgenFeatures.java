package org.kdvcs.poiesis.registry;

import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.worldgen.feature.ReplaceSurfaceTopBlockFeature;
import org.kdvcs.poiesis.worldgen.feature.ReplaceTopBlockConfig;

public final class ModWorldgenFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Poiesis.MOD_ID);

    public static final RegistryObject<Feature<ReplaceTopBlockConfig>> REPLACE_SURFACE_TOP_BLOCK =
            FEATURES.register("replace_surface_top_block", ReplaceSurfaceTopBlockFeature::new);

    private ModWorldgenFeatures() {
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}