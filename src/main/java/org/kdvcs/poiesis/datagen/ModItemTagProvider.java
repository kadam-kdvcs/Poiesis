package org.kdvcs.poiesis.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.tag.ModTags;

import java.util.concurrent.CompletableFuture;

public final class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            CompletableFuture<TagsProvider.TagLookup<Block>> blockTags,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, blockTags, Poiesis.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {

        addCropSeedTags();

        copy(ModTags.Blocks.ALDER_LOGS, ModTags.Items.ALDER_LOGS);
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.PLANKS, ItemTags.PLANKS);
        copy(BlockTags.WOODEN_STAIRS, ItemTags.WOODEN_STAIRS);
        copy(BlockTags.WOODEN_SLABS, ItemTags.WOODEN_SLABS);
        copy(BlockTags.WOODEN_FENCES, ItemTags.WOODEN_FENCES);
        copy(BlockTags.FENCES, ItemTags.FENCES);
        copy(BlockTags.FENCE_GATES, ItemTags.FENCE_GATES);
        copy(BlockTags.WOODEN_BUTTONS, ItemTags.WOODEN_BUTTONS);
        copy(BlockTags.BUTTONS, ItemTags.BUTTONS);
        copy(BlockTags.WOODEN_DOORS, ItemTags.WOODEN_DOORS);
        copy(BlockTags.DOORS, ItemTags.DOORS);
        copy(BlockTags.WOODEN_TRAPDOORS, ItemTags.WOODEN_TRAPDOORS);
        copy(BlockTags.TRAPDOORS, ItemTags.TRAPDOORS);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.SAPLINGS, ItemTags.SAPLINGS);

        copy(
                ModTags.Blocks.ORES_ALBICHROME,
                ModTags.Items.ORES_ALBICHROME
        );


        tag(
                ModTags.Items.INGOTS_ALBICHROME
        )
                .add(
                        ModItems.ALBICHROME_INGOT.get()
                );


        tag(
                ModTags.Items.RAW_ALBICHROME
        )
                .add(
                        ModItems.RAW_ALBICHROME.get()
                );


        tag(
                ModTags.Items.NUGGETS_ALBICHROME
        )
                .add(
                        ModItems.ALBICHROME_NUGGET.get()
                );

        tag(ModTags.Items.DUSTS_ALBICHROME)
                .add(ModItems.ALBICHROME_DUST.get());

        tag(ModTags.Items.DUSTS_IRON)
                .add(ModItems.IRON_DUST.get());

        tag(ModTags.Items.DUSTS_HEAT_RESISTANT_STEEL)
                .add(ModItems.HEAT_RESISTANT_STEEL_DUST.get());

        tag(ModTags.Items.DUSTS_GRAPHITE)
                .add(ModItems.GRAPHITE_DUST.get());

        tag(ModTags.Items.PLATES_GRAPHITE)
                .add(ModItems.GRAPHITE_PLATE.get());

        tag(ModTags.Items.ELECTRODES)
                .add(ModItems.GRAPHITE_ELECTRODE.get());

        tag(ModTags.Items.INGOTS_HEAT_RESISTANT_STEEL)
                .add(ModItems.HEAT_RESISTANT_STEEL_INGOT.get());

        tag(ModTags.Items.DIRTY_DUSTS_ALBICHROME)
                .addOptional(new ResourceLocation(Poiesis.MOD_ID, "dirty_albichrome_dust"));

        tag(ModTags.Items.CLUMPS_ALBICHROME)
                .addOptional(new ResourceLocation(Poiesis.MOD_ID, "albichrome_clump"));

        tag(ModTags.Items.SHARDS_ALBICHROME)
                .addOptional(new ResourceLocation(Poiesis.MOD_ID, "albichrome_shard"));

        tag(ModTags.Items.CRYSTALS_ALBICHROME)
                .addOptional(new ResourceLocation(Poiesis.MOD_ID, "albichrome_crystal"));


        tag(
                ModTags.Items.STORAGE_BLOCKS_ALBICHROME
        )
                .add(
                        ModBlocks.ALBICHROME_BLOCK.get().asItem()
                );

        tag(ModTags.Items.STORAGE_BLOCKS_HEAT_RESISTANT_STEEL)
                .add(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get().asItem());


        tag(
                ModTags.Items.STORAGE_BLOCKS_RAW_ALBICHROME
        )
                .add(
                        ModBlocks.RAW_ALBICHROME_BLOCK.get().asItem()
                );

        tag(ModTags.Items.COLORED_CONCRETE_POWDER)
                .add(
                        Blocks.WHITE_CONCRETE_POWDER.asItem(),
                        Blocks.ORANGE_CONCRETE_POWDER.asItem(),
                        Blocks.MAGENTA_CONCRETE_POWDER.asItem(),
                        Blocks.LIGHT_BLUE_CONCRETE_POWDER.asItem(),
                        Blocks.YELLOW_CONCRETE_POWDER.asItem(),
                        Blocks.LIME_CONCRETE_POWDER.asItem(),
                        Blocks.PINK_CONCRETE_POWDER.asItem(),
                        Blocks.GRAY_CONCRETE_POWDER.asItem(),
                        Blocks.LIGHT_GRAY_CONCRETE_POWDER.asItem(),
                        Blocks.CYAN_CONCRETE_POWDER.asItem(),
                        Blocks.PURPLE_CONCRETE_POWDER.asItem(),
                        Blocks.BLUE_CONCRETE_POWDER.asItem(),
                        Blocks.BROWN_CONCRETE_POWDER.asItem(),
                        Blocks.GREEN_CONCRETE_POWDER.asItem(),
                        Blocks.RED_CONCRETE_POWDER.asItem(),
                        Blocks.BLACK_CONCRETE_POWDER.asItem()
                );

        tag(ModTags.Items.COLORED_CONCRETE)
                .add(
                        Blocks.WHITE_CONCRETE.asItem(),
                        Blocks.ORANGE_CONCRETE.asItem(),
                        Blocks.MAGENTA_CONCRETE.asItem(),
                        Blocks.LIGHT_BLUE_CONCRETE.asItem(),
                        Blocks.YELLOW_CONCRETE.asItem(),
                        Blocks.LIME_CONCRETE.asItem(),
                        Blocks.PINK_CONCRETE.asItem(),
                        Blocks.GRAY_CONCRETE.asItem(),
                        Blocks.LIGHT_GRAY_CONCRETE.asItem(),
                        Blocks.CYAN_CONCRETE.asItem(),
                        Blocks.PURPLE_CONCRETE.asItem(),
                        Blocks.BLUE_CONCRETE.asItem(),
                        Blocks.BROWN_CONCRETE.asItem(),
                        Blocks.GREEN_CONCRETE.asItem(),
                        Blocks.RED_CONCRETE.asItem(),
                        Blocks.BLACK_CONCRETE.asItem()
                );

        tag(ModTags.Items.COLORED_WOOL)
                .add(
                        Blocks.ORANGE_WOOL.asItem(),
                        Blocks.MAGENTA_WOOL.asItem(),
                        Blocks.LIGHT_BLUE_WOOL.asItem(),
                        Blocks.YELLOW_WOOL.asItem(),
                        Blocks.LIME_WOOL.asItem(),
                        Blocks.PINK_WOOL.asItem(),
                        Blocks.GRAY_WOOL.asItem(),
                        Blocks.LIGHT_GRAY_WOOL.asItem(),
                        Blocks.CYAN_WOOL.asItem(),
                        Blocks.PURPLE_WOOL.asItem(),
                        Blocks.BLUE_WOOL.asItem(),
                        Blocks.BROWN_WOOL.asItem(),
                        Blocks.GREEN_WOOL.asItem(),
                        Blocks.RED_WOOL.asItem(),
                        Blocks.BLACK_WOOL.asItem()
                );

    }

    private void addCropSeedTags() {
        TagKey<Item> allSeeds = TagKey.create(
                Registries.ITEM,
                new ResourceLocation("forge", "seeds")
        );

        for (ModCrops.CropEntry crop : ModCrops.all()) {
            TagKey<Item> cropSeeds = TagKey.create(
                    Registries.ITEM,
                    new ResourceLocation(
                            "forge",
                            "seeds/" + crop.name()
                    )
            );

            tag(cropSeeds)
                    .add(crop.seeds().get());

            tag(allSeeds)
                    .addTag(cropSeeds);
        }
    }
}
