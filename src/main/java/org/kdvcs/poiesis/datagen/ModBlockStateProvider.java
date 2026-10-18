package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ConfiguredModel;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.ForgeRegistries;
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
        blockWithItem(ModBlocks.GRAPHITE_BLOCK);
        blockWithItem(ModBlocks.PLAIN_CONCRETE);
        blockWithItem(ModBlocks.PLAIN_CONCRETE_POWDER);

        alderTree();

        for (ModCrops.CropEntry crop : ModCrops.all()) {
            makeCrop(crop);
        }
    }

    private void alderTree() {
        axisBlockWithItem(
                ModBlocks.ALDER_LOG.get(),
                modLoc("block/alder_log"),
                modLoc("block/alder_log_top")
        );
        axisBlockWithItem(
                ModBlocks.ALDER_WOOD.get(),
                modLoc("block/alder_log"),
                modLoc("block/alder_log")
        );
        axisBlockWithItem(
                ModBlocks.STRIPPED_ALDER_LOG.get(),
                modLoc("block/stripped_alder_log"),
                modLoc("block/stripped_alder_log_top")
        );
        axisBlockWithItem(
                ModBlocks.STRIPPED_ALDER_WOOD.get(),
                modLoc("block/stripped_alder_log"),
                modLoc("block/stripped_alder_log")
        );
        blockWithItem(ModBlocks.ALDER_PLANKS);
        blockWithItem(ModBlocks.ALDER_ROOT);
        stairsBlock(ModBlocks.ALDER_STAIRS.get(), modLoc("block/alder_planks"));
        simpleBlockItem(ModBlocks.ALDER_STAIRS.get(), models().getBuilder("alder_stairs"));
        slabBlock(
                ModBlocks.ALDER_SLAB.get(),
                modLoc("block/alder_planks"),
                modLoc("block/alder_planks")
        );
        simpleBlockItem(ModBlocks.ALDER_SLAB.get(), models().getBuilder("alder_slab"));
        fenceBlock(ModBlocks.ALDER_FENCE.get(), modLoc("block/alder_planks"));
        itemModels().getBuilder("alder_fence")
                .parent(itemModels().getExistingFile(mcLoc("block/fence_inventory")))
                .texture("texture", modLoc("block/alder_planks"));
        fenceGateBlock(ModBlocks.ALDER_FENCE_GATE.get(), modLoc("block/alder_planks"));
        itemModels().getBuilder("alder_fence_gate")
                .parent(itemModels().getExistingFile(mcLoc("block/template_fence_gate")))
                .texture("texture", modLoc("block/alder_planks"));
        buttonBlock(ModBlocks.ALDER_BUTTON.get(), modLoc("block/alder_planks"));
        itemModels().getBuilder("alder_button")
                .parent(itemModels().getExistingFile(mcLoc("block/button_inventory")))
                .texture("texture", modLoc("block/alder_planks"));
        doorBlock(
                ModBlocks.ALDER_DOOR.get(),
                modLoc("block/alder_door_bottom"),
                modLoc("block/alder_door_top")
        );
        itemModels().getBuilder("alder_door")
                .parent(itemModels().getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/alder_door"));
        trapdoorBlock(
                ModBlocks.ALDER_TRAPDOOR.get(),
                modLoc("block/alder_trapdoor"),
                true
        );
        itemModels().getBuilder("alder_trapdoor")
                .parent(itemModels().getExistingFile(mcLoc("block/template_orientable_trapdoor_bottom")))
                .texture("texture", modLoc("block/alder_trapdoor"));

        var leavesModel = models()
                .withExistingParent("alder_leaves", mcLoc("block/leaves"))
                .texture("all", modLoc("block/alder_leaves"))
                .renderType("cutout_mipped");
        simpleBlockWithItem(ModBlocks.ALDER_LEAVES.get(), leavesModel);

        var saplingModel = models()
                .cross("alder_sapling", modLoc("block/alder_sapling"))
                .renderType("cutout");
        simpleBlock(ModBlocks.ALDER_SAPLING.get(), saplingModel);
        itemModels()
                .withExistingParent("alder_sapling", mcLoc("item/generated"))
                .texture("layer0", modLoc("block/alder_sapling"));

        var pottedSaplingModel = models()
                .withExistingParent(
                        "potted_alder_sapling",
                        mcLoc("block/flower_pot_cross")
                )
                .texture("plant", modLoc("block/alder_sapling"))
                .renderType("cutout");
        simpleBlock(ModBlocks.POTTED_ALDER_SAPLING.get(), pottedSaplingModel);
    }

    private void axisBlockWithItem(
            RotatedPillarBlock block,
            ResourceLocation side,
            ResourceLocation end
    ) {
        axisBlock(block, side, end);
        simpleBlockItem(
                block,
                models().getBuilder(key(block))
        );
    }

    private static String key(Block block) {
        return ForgeRegistries.BLOCKS
                .getKey(block)
                .getPath();
    }

    private <T extends Block> void blockWithItem(
            RegistryObject<T> blockRegistryObject
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
