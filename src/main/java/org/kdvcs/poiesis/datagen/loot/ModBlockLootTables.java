package org.kdvcs.poiesis.datagen.loot;

import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.registry.ModBlocks;

import java.util.Set;

public final class ModBlockLootTables extends BlockLootSubProvider {
    public ModBlockLootTables() { super(Set.of(), FeatureFlags.REGISTRY.allFlags()); }
    @Override protected void generate() {
        dropSelf(ModBlocks.MECHANICAL_PROCESSOR.get());
    }
    @Override protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(RegistryObject::get)::iterator;
    }
}
