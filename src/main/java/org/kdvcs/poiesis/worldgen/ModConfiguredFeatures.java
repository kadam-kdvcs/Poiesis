package org.kdvcs.poiesis.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModWorldgenFeatures;
import org.kdvcs.poiesis.worldgen.feature.ReplaceTopBlockConfig;

import java.util.List;

public final class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> ALBICHROME_ORE_KEY = registerKey("albichrome_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILICA_SURFACE_KEY = registerKey("silica_surface");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> albichromeOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.ALBICHROME_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.DEEPSLATE_ALBICHROME_ORE.get().defaultBlockState())
        );

        register(context, ALBICHROME_ORE_KEY, Feature.ORE, new OreConfiguration(albichromeOres, 7));
        register(
                context,
                SILICA_SURFACE_KEY,
                ModWorldgenFeatures.REPLACE_SURFACE_TOP_BLOCK.get(),
                new ReplaceTopBlockConfig(
                        net.minecraft.world.level.block.Blocks.STONE,
                        ModBlocks.SILICA_ROCK.get(),
                        0.20F
                )
        );
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(Poiesis.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    private ModConfiguredFeatures() {
    }
}