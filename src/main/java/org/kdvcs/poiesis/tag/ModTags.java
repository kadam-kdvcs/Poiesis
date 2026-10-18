package org.kdvcs.poiesis.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.biome.Biome;
import org.kdvcs.poiesis.Poiesis;

public final class ModTags {

    private ModTags() {
    }

    public static final class Blocks {

        public static final TagKey<Block> ORES_ALBICHROME =
                forgeBlockTag(
                        "ores/albichrome"
                );

        public static final TagKey<Block> MACHINE_CASINGS =
                modBlockTag(
                        "machine_casings"
                );

        public static final TagKey<Block> STORAGE_BLOCKS_HEAT_RESISTANT_STEEL =
                forgeBlockTag(
                        "storage_blocks/heat_resistant_steel"
                );

        public static final TagKey<Block> ALDER_LOGS =
                modBlockTag("alder_logs");


    }

    public static final class Items {

        public static final TagKey<Item> ORES_ALBICHROME =
                forgeItemTag(
                        "ores/albichrome"
                );

        public static final TagKey<Item> RAW_ALBICHROME =
                forgeItemTag(
                        "raw_materials/albichrome"
                );

        public static final TagKey<Item> INGOTS_ALBICHROME =
                forgeItemTag(
                        "ingots/albichrome"
                );

        public static final TagKey<Item> NUGGETS_ALBICHROME =
                forgeItemTag(
                        "nuggets/albichrome"
                );

        public static final TagKey<Item> DUSTS_ALBICHROME =
                forgeItemTag(
                        "dusts/albichrome"
                );

        public static final TagKey<Item> DUSTS_IRON =
                forgeItemTag(
                        "dusts/iron"
                );

        public static final TagKey<Item> DUSTS_HEAT_RESISTANT_STEEL =
                forgeItemTag(
                        "dusts/heat_resistant_steel"
                );

        public static final TagKey<Item> DUSTS_GRAPHITE =
                forgeItemTag("dusts/graphite");

        public static final TagKey<Item> PLATES_GRAPHITE =
                forgeItemTag("plates/graphite");

        public static final TagKey<Item> ELECTRODES =
                modItemTag("electrodes");

        public static final TagKey<Item> INGOTS_HEAT_RESISTANT_STEEL =
                forgeItemTag(
                        "ingots/heat_resistant_steel"
                );

        public static final TagKey<Item> DIRTY_DUSTS_ALBICHROME =
                mekanismItemTag(
                        "dirty_dusts/albichrome"
                );

        public static final TagKey<Item> CLUMPS_ALBICHROME =
                mekanismItemTag(
                        "clumps/albichrome"
                );

        public static final TagKey<Item> SHARDS_ALBICHROME =
                mekanismItemTag(
                        "shards/albichrome"
                );

        public static final TagKey<Item> CRYSTALS_ALBICHROME =
                mekanismItemTag(
                        "crystals/albichrome"
                );

        public static final TagKey<Item> STORAGE_BLOCKS_ALBICHROME =
                forgeItemTag(
                        "storage_blocks/albichrome"
                );

        public static final TagKey<Item> STORAGE_BLOCKS_HEAT_RESISTANT_STEEL =
                forgeItemTag(
                        "storage_blocks/heat_resistant_steel"
                );


        public static final TagKey<Item> MACHINE_CASING_MATERIALS =
                modItemTag(
                        "machine_casing_materials"
                );

        public static final TagKey<Item> MODULE_MATERIALS =
                modItemTag(
                        "module_materials"
                );

        public static final TagKey<Item> STORAGE_BLOCKS_RAW_ALBICHROME =
                forgeItemTag(
                        "storage_blocks/raw_albichrome"
                );

        public static final TagKey<Item> COLORED_CONCRETE_POWDER =
                modItemTag("colored_concrete_powder");

        public static final TagKey<Item> COLORED_CONCRETE =
                modItemTag("colored_concrete");

        public static final TagKey<Item> COLORED_WOOL =
                modItemTag("colored_wool");

        public static final TagKey<Item> ALDER_LOGS =
                modItemTag("alder_logs");

    }

    public static final class Biomes {
        public static final TagKey<Biome> HAS_ALDER_TREES = TagKey.create(
                Registries.BIOME,
                new ResourceLocation(Poiesis.MOD_ID, "has_alder_trees")
        );

        private Biomes() {
        }
    }



    private static TagKey<Block> forgeBlockTag(
            String name
    ) {
        return BlockTags.create(
                new ResourceLocation(
                        "forge",
                        name
                )
        );
    }


    private static TagKey<Item> forgeItemTag(
            String name
    ) {
        return ItemTags.create(
                new ResourceLocation(
                        "forge",
                        name
                )
        );
    }

    private static TagKey<Item> mekanismItemTag(
            String name
    ) {
        return ItemTags.create(
                new ResourceLocation(
                        "mekanism",
                        name
                )
        );
    }


    private static TagKey<Block> modBlockTag(
            String name
    ) {
        return BlockTags.create(
                new ResourceLocation(
                        Poiesis.MOD_ID,
                        name
                )
        );
    }


    private static TagKey<Item> modItemTag(
            String name
    ) {
        return ItemTags.create(
                new ResourceLocation(
                        Poiesis.MOD_ID,
                        name
                )
        );
    }

}
