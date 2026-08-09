package org.kdvcs.poiesis.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.Set;

public final class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {

        dropSelf(
                ModBlocks.MECHANICAL_PROCESSOR.get()
        );

        dropSelf(
                ModBlocks.SILICA_ROCK.get()
        );

        dropSelf(
                ModBlocks.ALBICHROME_CASING.get()
        );

        dropSelf(
                ModBlocks.MODULE_FABRICATOR.get()
        );

        dropSelf(
                ModBlocks.BASIC_ENERGY_PIPE.get()
        );

        dropSelf(
                ModBlocks.BASIC_ITEM_PIPE.get()
        );

        dropSelf(
                ModBlocks.BASIC_FLUID_PIPE.get()
        );

        dropSelf(
                ModBlocks.ALBICHROME_BLOCK.get()
        );

        dropSelf(
                ModBlocks.RAW_ALBICHROME_BLOCK.get()
        );

        //  ORE DROPS
        add(
                ModBlocks.ALBICHROME_ORE.get(),
                block -> createOreDrop(
                        block,
                        ModItems.RAW_ALBICHROME.get()
                )
        );

        add(
                ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(),
                block -> createOreDrop(
                        block,
                        ModItems.RAW_ALBICHROME.get()
                )
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries()
                .stream()
                .map(RegistryObject::get)::iterator;
    }
}