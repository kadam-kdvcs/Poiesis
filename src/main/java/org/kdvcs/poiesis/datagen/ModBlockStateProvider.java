package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.crop.PoiesisCropBlock;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;

public final class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, Poiesis.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.ALBICHROME_ORE);
        blockWithItem(ModBlocks.DEEPSLATE_ALBICHROME_ORE);
        blockWithItem(ModBlocks.ALBICHROME_BLOCK);
        blockWithItem(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK);
        blockWithItem(ModBlocks.RAW_ALBICHROME_BLOCK);
        blockWithItem(ModBlocks.SILICA_ROCK);

        for (ModCrops.CropEntry crop : ModCrops.all()) {
            makeCrop(crop);
        }
    }

    private void blockWithItem(
            RegistryObject<Block> blockRegistryObject
    ) {
        simpleBlockWithItem(
                blockRegistryObject.get(),
                cubeAll(blockRegistryObject.get())
        );
    }

    private void makeCrop(ModCrops.CropEntry entry) {
        CropBlock crop = (CropBlock) entry.crop().get();

        getVariantBuilder(crop).forAllStates(
                state -> cropState(entry, crop, state)
        );
    }

    private ConfiguredModel[] cropState(
            ModCrops.CropEntry entry,
            CropBlock crop,
            BlockState state
    ) {
        int age = state.getValue(PoiesisCropBlock.AGE);
        String stage = entry.stageModel(age);

        return new ConfiguredModel[]{
                new ConfiguredModel(
                        models().crop(
                                stage,
                                new ResourceLocation(
                                        Poiesis.MOD_ID,
                                        "block/crop/" + stage
                                )
                        ).renderType("cutout")
                )
        };
    }
}
