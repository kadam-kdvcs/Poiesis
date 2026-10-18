package org.kdvcs.poiesis.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;

import java.util.List;

public final class ModPlacedFeatures {

    public static final ResourceKey<PlacedFeature> ALBICHROME_ORE_PLACED_KEY = registerKey("albichrome_ore");
    public static final ResourceKey<PlacedFeature> SILICA_SURFACE_PLACED_KEY = registerKey("silica_surface");
    public static final ResourceKey<PlacedFeature> ALDER_TREE_PLACED_KEY = registerKey("alder_tree");

    public static void bootstrap(BootstapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(
                context,
                ALBICHROME_ORE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ALBICHROME_ORE_KEY),
                List.of(
                        CountPlacement.of(18),
                        InSquarePlacement.spread(),
                        HeightRangePlacement.uniform(VerticalAnchor.absolute(-48), VerticalAnchor.absolute(48)),
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                SILICA_SURFACE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.SILICA_SURFACE_KEY),
                List.of(
                        CountPlacement.of(8),
                        InSquarePlacement.spread(),
                        BiomeFilter.biome()
                )
        );

        register(
                context,
                ALDER_TREE_PLACED_KEY,
                configuredFeatures.getOrThrow(ModConfiguredFeatures.ALDER_TREE_KEY),
                VegetationPlacements.treePlacement(
                        PlacementUtils.countExtra(0, 1.0F / 3.0F, 1),
                        ModBlocks.ALDER_SAPLING.get()
                )
        );
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, new ResourceLocation(Poiesis.MOD_ID, name));
    }

    private static void register(BootstapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }

    private ModPlacedFeatures() {
    }
}
