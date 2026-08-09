package org.kdvcs.poiesis.tag;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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

        public static final TagKey<Item> STORAGE_BLOCKS_ALBICHROME =
                forgeItemTag(
                        "storage_blocks/albichrome"
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