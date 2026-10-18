package org.kdvcs.poiesis.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.worldgen.feature.ReplaceSurfaceTopBlockFeature;
import org.kdvcs.poiesis.worldgen.feature.ReplaceTopBlockConfig;
import org.kdvcs.poiesis.worldgen.tree.AlderTrunkPlacer;

public final class ModWorldgenFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, Poiesis.MOD_ID);

    public static final DeferredRegister<TrunkPlacerType<?>> TRUNK_PLACER_TYPES =
            DeferredRegister.create(Registries.TRUNK_PLACER_TYPE, Poiesis.MOD_ID);

    public static final RegistryObject<TrunkPlacerType<AlderTrunkPlacer>> ALDER_TRUNK_PLACER =
            TRUNK_PLACER_TYPES.register("alder_trunk_placer", () -> new TrunkPlacerType<>(AlderTrunkPlacer.CODEC));

    public static final RegistryObject<Feature<ReplaceTopBlockConfig>> REPLACE_SURFACE_TOP_BLOCK =
            FEATURES.register("replace_surface_top_block", ReplaceSurfaceTopBlockFeature::new);

    private ModWorldgenFeatures() {
    }

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
        TRUNK_PLACER_TYPES.register(eventBus);
    }
}
