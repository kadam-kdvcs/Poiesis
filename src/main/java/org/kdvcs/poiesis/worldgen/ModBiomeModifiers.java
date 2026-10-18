package org.kdvcs.poiesis.worldgen;

import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ForgeBiomeModifiers;
import net.minecraftforge.registries.ForgeRegistries;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.tag.ModTags;

public final class ModBiomeModifiers {

    public static final ResourceKey<BiomeModifier> ADD_ALBICHROME_ORE = registerKey("add_albichrome_ore");
    public static final ResourceKey<BiomeModifier> ADD_SILICA_SURFACE = registerKey("add_silica_surface");
    public static final ResourceKey<BiomeModifier> ADD_ALDER_TREES = registerKey("add_alder_trees");

    public static void bootstrap(BootstapContext<BiomeModifier> context) {
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderGetter<PlacedFeature> placedFeatures = context.lookup(Registries.PLACED_FEATURE);

        context.register(
                ADD_ALBICHROME_ORE,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.ALBICHROME_ORE_PLACED_KEY)),
                        GenerationStep.Decoration.UNDERGROUND_ORES
                )
        );
        context.register(
                ADD_SILICA_SURFACE,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                        HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.SILICA_SURFACE_PLACED_KEY)),
                        GenerationStep.Decoration.TOP_LAYER_MODIFICATION
                )
        );
        context.register(
                ADD_ALDER_TREES,
                new ForgeBiomeModifiers.AddFeaturesBiomeModifier(
                        biomes.getOrThrow(ModTags.Biomes.HAS_ALDER_TREES),
                        HolderSet.direct(
                                placedFeatures.getOrThrow(
                                        ModPlacedFeatures.ALDER_TREE_PLACED_KEY
                                )
                        ),
                        GenerationStep.Decoration.VEGETAL_DECORATION
                )
        );
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS, new ResourceLocation(Poiesis.MOD_ID, name));
    }

    private ModBiomeModifiers() {
    }
}
