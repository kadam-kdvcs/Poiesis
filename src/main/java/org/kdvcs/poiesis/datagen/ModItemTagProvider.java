package org.kdvcs.poiesis.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
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


        tag(
                ModTags.Items.STORAGE_BLOCKS_ALBICHROME
        )
                .add(
                        ModBlocks.ALBICHROME_BLOCK.get().asItem()
                );


        tag(
                ModTags.Items.STORAGE_BLOCKS_RAW_ALBICHROME
        )
                .add(
                        ModBlocks.RAW_ALBICHROME_BLOCK.get().asItem()
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
