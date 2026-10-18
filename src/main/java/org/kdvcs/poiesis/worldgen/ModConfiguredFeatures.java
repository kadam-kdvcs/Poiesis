package org.kdvcs.poiesis.worldgen;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FancyFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModWorldgenFeatures;
import org.kdvcs.poiesis.worldgen.feature.ReplaceTopBlockConfig;
import org.kdvcs.poiesis.worldgen.tree.AlderTrunkPlacer;

import java.util.List;
import java.util.OptionalInt;

public final class ModConfiguredFeatures {

    public static final ResourceKey<ConfiguredFeature<?, ?>> ALBICHROME_ORE_KEY = registerKey("albichrome_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> SILICA_SURFACE_KEY = registerKey("silica_surface");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ALDER_TREE_KEY = registerKey("alder_tree");

    public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);

        List<OreConfiguration.TargetBlockState> albichromeOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.ALBICHROME_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.DEEPSLATE_ALBICHROME_ORE.get().defaultBlockState())
        );

        register(context, ALBICHROME_ORE_KEY, Feature.ORE, new OreConfiguration(albichromeOres, 8));
        register(
                context,
                ALDER_TREE_KEY,
                Feature.TREE,
                new TreeConfiguration.TreeConfigurationBuilder(
                        BlockStateProvider.simple(ModBlocks.ALDER_LOG.get()),
                        // A tall straight trunk with tiered side branches.  The
                        // custom placer keeps low branches sparse and gives the
                        // upper crown progressively more branching weight.
                        new AlderTrunkPlacer(10, 3, 2),
                        BlockStateProvider.simple(ModBlocks.ALDER_LEAVES.get()),
                        // Fancy foliage is the same layered, irregular canopy used by
                        // vanilla's large oak; it avoids the single spherical blob made
                        // by BlobFoliagePlacer and gives each branch its own leaf mass.
                        new FancyFoliagePlacer(
                                ConstantInt.of(3),
                                ConstantInt.of(3),
                                4
                        ),
                        new TwoLayersFeatureSize(0, 0, 0, OptionalInt.of(4))
                )
                        .ignoreVines()
                        .build()
        );
        register(
                context,
                SILICA_SURFACE_KEY,
                ModWorldgenFeatures.REPLACE_SURFACE_TOP_BLOCK.get(),
                new ReplaceTopBlockConfig(
                        Blocks.STONE,
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
