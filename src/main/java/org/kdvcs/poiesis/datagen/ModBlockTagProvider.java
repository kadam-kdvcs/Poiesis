package org.kdvcs.poiesis.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.tag.ModTags;

import java.util.concurrent.CompletableFuture;

public final class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, Poiesis.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.MECHANICAL_PROCESSOR.get())
                .add(ModBlocks.COMPOUNDER.get())
                .add(ModBlocks.LEACHER.get())
                .add(ModBlocks.BIOMASS_GASIFIER.get())
                .add(ModBlocks.GAS_GENERATOR.get())
                .add(ModBlocks.MICROBIAL_FUEL_CELL.get())
                .add(ModBlocks.ALLOY_FACTORY.get())
                .add(ModBlocks.GREENHOUSE.get())
                .add(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get())
                .add(ModBlocks.ADSORPTION_BED.get())
                .add(ModBlocks.FLUID_DISTRIBUTOR.get())
                .add(ModBlocks.ALBICHROME_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get())
                .add(ModBlocks.ALBICHROME_BLOCK.get())
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .add(ModBlocks.RAW_ALBICHROME_BLOCK.get())
                .add(ModBlocks.ALBICHROME_CASING.get())
                .add(ModBlocks.BASIC_ENERGY_PIPE.get())
                .add(ModBlocks.BASIC_ITEM_PIPE.get())
                .add(ModBlocks.BASIC_FLUID_PIPE.get())
                .add(ModBlocks.SILICA_ROCK.get())
                .add(ModBlocks.HEAVY_MACHINE_CASING.get())
                .add(ModBlocks.GRAPHITE_BLOCK.get())
                .add(ModBlocks.PLAIN_CONCRETE.get());

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.PLAIN_CONCRETE_POWDER.get());

        tag(BlockTags.MINEABLE_WITH_AXE)
                .add(
                        ModBlocks.ALDER_LOG.get(),
                        ModBlocks.ALDER_WOOD.get(),
                        ModBlocks.STRIPPED_ALDER_LOG.get(),
                        ModBlocks.STRIPPED_ALDER_WOOD.get(),
                        ModBlocks.ALDER_PLANKS.get(),
                        ModBlocks.ALDER_STAIRS.get(),
                        ModBlocks.ALDER_SLAB.get(),
                        ModBlocks.ALDER_FENCE.get(),
                        ModBlocks.ALDER_FENCE_GATE.get(),
                        ModBlocks.ALDER_BUTTON.get(),
                        ModBlocks.ALDER_DOOR.get(),
                        ModBlocks.ALDER_TRAPDOOR.get()
                );

        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.ALDER_ROOT.get());

        tag(ModTags.Blocks.ALDER_LOGS)
                .add(
                        ModBlocks.ALDER_LOG.get(),
                        ModBlocks.ALDER_WOOD.get(),
                        ModBlocks.STRIPPED_ALDER_LOG.get(),
                        ModBlocks.STRIPPED_ALDER_WOOD.get()
                );

        tag(BlockTags.LOGS_THAT_BURN)
                .addTag(ModTags.Blocks.ALDER_LOGS);

        tag(BlockTags.OVERWORLD_NATURAL_LOGS)
                .add(ModBlocks.ALDER_LOG.get());

        tag(BlockTags.PLANKS)
                .add(ModBlocks.ALDER_PLANKS.get());

        tag(BlockTags.WOODEN_STAIRS)
                .add(ModBlocks.ALDER_STAIRS.get());

        tag(BlockTags.WOODEN_SLABS)
                .add(ModBlocks.ALDER_SLAB.get());

        tag(BlockTags.WOODEN_FENCES)
                .add(ModBlocks.ALDER_FENCE.get());

        tag(BlockTags.FENCES)
                .add(ModBlocks.ALDER_FENCE.get());

        tag(BlockTags.FENCE_GATES)
                .add(ModBlocks.ALDER_FENCE_GATE.get());

        tag(BlockTags.WOODEN_BUTTONS)
                .add(ModBlocks.ALDER_BUTTON.get());

        tag(BlockTags.BUTTONS)
                .add(ModBlocks.ALDER_BUTTON.get());

        tag(BlockTags.WOODEN_DOORS)
                .add(ModBlocks.ALDER_DOOR.get());

        tag(BlockTags.DOORS)
                .add(ModBlocks.ALDER_DOOR.get());

        tag(BlockTags.WOODEN_TRAPDOORS)
                .add(ModBlocks.ALDER_TRAPDOOR.get());

        tag(BlockTags.TRAPDOORS)
                .add(ModBlocks.ALDER_TRAPDOOR.get());

        tag(BlockTags.LEAVES)
                .add(ModBlocks.ALDER_LEAVES.get());

        tag(BlockTags.SAPLINGS)
                .add(ModBlocks.ALDER_SAPLING.get());


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.ALBICHROME_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get())
                .add(ModBlocks.ALBICHROME_BLOCK.get())
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .add(ModBlocks.RAW_ALBICHROME_BLOCK.get())
                .add(ModBlocks.ALBICHROME_CASING.get())
                .add(ModBlocks.GRAPHITE_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.PLAIN_CONCRETE.get());


        tag(ModTags.Blocks.ORES_ALBICHROME)
                .add(ModBlocks.ALBICHROME_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get());

        tag(ModTags.Blocks.STORAGE_BLOCKS_HEAT_RESISTANT_STEEL)
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get());

    }
}
