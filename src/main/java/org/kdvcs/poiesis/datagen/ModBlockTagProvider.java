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
                .add(ModBlocks.BIOMASS_GASIFIER.get())
                .add(ModBlocks.ALLOY_FACTORY.get())
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
                .add(ModBlocks.HEAVY_MACHINE_CASING.get());


        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.ALBICHROME_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get())
                .add(ModBlocks.ALBICHROME_BLOCK.get())
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .add(ModBlocks.RAW_ALBICHROME_BLOCK.get())
                .add(ModBlocks.ALBICHROME_CASING.get());


        tag(ModTags.Blocks.ORES_ALBICHROME)
                .add(ModBlocks.ALBICHROME_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get());

        tag(ModTags.Blocks.STORAGE_BLOCKS_HEAT_RESISTANT_STEEL)
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get());

    }
}
