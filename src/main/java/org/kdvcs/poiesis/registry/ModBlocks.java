package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.CompounderBlock;
import org.kdvcs.poiesis.block.EnergyPipeBlock;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.block.MechanicalProcessorBlock;
import org.kdvcs.poiesis.block.ModuleFabricatorBlock;
import org.kdvcs.poiesis.item.PipeBlockItem;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    Poiesis.MOD_ID
            );

    public static final RegistryObject<Block> SILICA_ROCK =
            registerBlock(
                    "silica_rock",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .strength(3.0F, 6.0F)
                                    .sound(SoundType.STONE)
                                    .requiresCorrectToolForDrops()
                    )
            );

    //  ALBICHROME ORES
    public static final RegistryObject<Block> ALBICHROME_ORE =
            registerBlock(
                    "albichrome_ore",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.IRON_ORE
                            ).requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> DEEPSLATE_ALBICHROME_ORE =
            registerBlock(
                    "deepslate_albichrome_ore",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.DEEPSLATE_IRON_ORE
                            ).requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> ALBICHROME_BLOCK =
            registerBlock(
                    "albichrome_block",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.IRON_BLOCK
                            ).requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> RAW_ALBICHROME_BLOCK =
            registerBlock(
                    "raw_albichrome_block",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.RAW_IRON_BLOCK
                            ).requiresCorrectToolForDrops()
                    )
            );

    public static final RegistryObject<Block> ALBICHROME_CASING =
            registerBlock(
                    "albichrome_casing",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.DIAMOND_BLOCK
                            ).requiresCorrectToolForDrops()
                    )
            );

    //  ENERGY PIPES
    public static final RegistryObject<EnergyPipeBlock> BASIC_ENERGY_PIPE =
            registerPipeBlock(
                    "basic_energy_pipe",
                    EnergyPipeBlock::new,
                    () -> EnergyPipeBlock.TRANSFER_RATE,
                    " FE/t"
            );

    //  ITEM PIPES
    public static final RegistryObject<ItemPipeBlock> BASIC_ITEM_PIPE =
            registerPipeBlock(
                    "basic_item_pipe",
                    ItemPipeBlock::new,
                    () -> ItemPipeBlock.TRANSFER_RATE,
                    ""
            );

    //  FLUID PIPES
    public static final RegistryObject<FluidPipeBlock> BASIC_FLUID_PIPE =
            registerPipeBlock(
                    "basic_fluid_pipe",
                    FluidPipeBlock::new,
                    () -> FluidPipeBlock.TRANSFER_RATE,
                    " mB/t"
            );

    //  MACHINE BLOCKS
    public static final RegistryObject<MechanicalProcessorBlock>
            MECHANICAL_PROCESSOR =
            registerBlock(
                    "mechanical_processor",
                    MechanicalProcessorBlock::new
            );

    public static final RegistryObject<ModuleFabricatorBlock>
            MODULE_FABRICATOR =
            registerBlock(
                    "module_fabricator",
                    ModuleFabricatorBlock::new
            );

    public static final RegistryObject<CompounderBlock>
            COMPOUNDER =
            registerBlock(
                    "compounder",
                    CompounderBlock::new
            );


    private static <T extends Block> RegistryObject<T> registerPipeBlock(
            String name,
            Supplier<T> blockFactory,
            IntSupplier transferRate,
            String unit
    ) {
        RegistryObject<T> block = BLOCKS.register(name, blockFactory);
        ModItems.ITEMS.register(name, () -> new PipeBlockItem(block.get(), new Item.Properties(), transferRate, unit));
        return block;
    }

    private static <T extends Block> RegistryObject<T> registerBlock(
            String name,
            Supplier<T> blockFactory
    ) {
        RegistryObject<T> block =
                BLOCKS.register(
                        name,
                        blockFactory
                );

        registerBlockItem(
                name,
                block
        );

        return block;
    }

    private static <T extends Block> RegistryObject<Item>
    registerBlockItem(
            String name,
            RegistryObject<T> block
    ) {
        return ModItems.ITEMS.register(
                name,
                () -> new BlockItem(
                        block.get(),
                        new Item.Properties()
                )
        );
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    private ModBlocks() {
    }
}