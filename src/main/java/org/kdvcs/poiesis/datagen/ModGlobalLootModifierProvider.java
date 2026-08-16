package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraftforge.common.data.GlobalLootModifierProvider;
import net.minecraftforge.common.loot.LootTableIdCondition;
import net.minecraftforge.registries.ForgeRegistries;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.loot.AddItemLootModifier;
import org.kdvcs.poiesis.registry.ModCrops;

import java.util.List;

/**
 * Data-driven global loot additions.
 *
 * Crop seed discovery is derived from ModCrops: a crop with a positive
 * wildSeedDropChance automatically receives seed drops from wild grass-like
 * vanilla plants without event-bus code.
 */
public final class ModGlobalLootModifierProvider
        extends GlobalLootModifierProvider {

    private static final List<Block> WILD_SEED_SOURCES = List.of(
            Blocks.GRASS,
            Blocks.TALL_GRASS,
            Blocks.FERN,
            Blocks.LARGE_FERN
    );

    public ModGlobalLootModifierProvider(
            PackOutput output
    ) {
        super(
                output,
                Poiesis.MOD_ID
        );
    }

    @Override
    protected void start() {
        for (ModCrops.CropEntry crop : ModCrops.all()) {
            if (crop.wildSeedDropChance() <= 0.0F) {
                continue;
            }

            for (Block source : WILD_SEED_SOURCES) {
                addWildSeedDrop(crop, source);
            }
        }
    }

    private void addWildSeedDrop(
            ModCrops.CropEntry crop,
            Block source
    ) {
        String sourceName =
                ForgeRegistries.BLOCKS
                        .getKey(source)
                        .getPath();

        add(
                crop.name()
                        + "_seeds_from_"
                        + sourceName,
                new AddItemLootModifier(
                        new LootItemCondition[]{
                                LootTableIdCondition
                                        .builder(source.getLootTable())
                                        .build(),
                                LootItemRandomChanceCondition
                                        .randomChance(
                                                crop.wildSeedDropChance()
                                        )
                                        .build()
                        },
                        crop.seeds().get(),
                        1
                )
        );
    }
}
