package org.kdvcs.poiesis.registry;

import net.minecraft.ChatFormatting;
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
import org.kdvcs.poiesis.block.AdsorptionBedBlock;
import org.kdvcs.poiesis.block.AlloyFactoryBlock;
import org.kdvcs.poiesis.block.AdsorptionColumnControllerBlock;
import org.kdvcs.poiesis.block.CompounderBlock;
import org.kdvcs.poiesis.block.BiomassGasifierBlock;
import org.kdvcs.poiesis.block.EnergyPipeBlock;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.block.FluidDistributorBlock;
import org.kdvcs.poiesis.block.MechanicalProcessorBlock;
import org.kdvcs.poiesis.block.ModuleFabricatorBlock;
import org.kdvcs.poiesis.block.SeparatorBlock;
import org.kdvcs.poiesis.block.entity.CompounderBlockEntity;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.block.entity.BiomassGasifierBlockEntity;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.block.entity.SeparatorBlockEntity;
import org.kdvcs.poiesis.item.PipeBlockItem;
import org.kdvcs.poiesis.item.TooltipBlockItem;
import org.kdvcs.poiesis.item.tooltip.Tooltips;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;

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

    public static final RegistryObject<Block> HEAT_RESISTANT_STEEL_BLOCK =
            registerBlock(
                    "heat_resistant_steel_block",
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

    public static final RegistryObject<Block> HEAVY_MACHINE_CASING =
            registerBlock(
                    "heavy_machine_casing",
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
            registerMachineBlock(
                    "mechanical_processor",
                    MechanicalProcessorBlock::new,
                    () -> MechanicalProcessorBlockEntity.DEFINITION,
                    () -> ModItems.PRESSING_MODULE.get(),
                    () -> ModItems.GRINDING_MODULE.get(),
                    () -> ModItems.EXTRUSION_MODULE.get()
            );

    public static final RegistryObject<ModuleFabricatorBlock>
            MODULE_FABRICATOR =
            registerMachineBlock(
                    "module_fabricator",
                    ModuleFabricatorBlock::new,
                    () -> ModuleFabricatorBlockEntity.DEFINITION
            );

    public static final RegistryObject<CompounderBlock>
            COMPOUNDER =
            registerMachineBlock(
                    "compounder",
                    CompounderBlock::new,
                    () -> CompounderBlockEntity.DEFINITION,
                    () -> ModItems.HIGH_SHEAR_MODULE.get()
            );

    public static final RegistryObject<SeparatorBlock>
            SEPARATOR =
            registerMachineBlock(
                    "separator",
                    SeparatorBlock::new,
                    () -> SeparatorBlockEntity.DEFINITION,
                    () -> ModItems.WET_SEPARATION_MODULE.get()
            );

    public static final RegistryObject<BiomassGasifierBlock>
            BIOMASS_GASIFIER =
            registerMachineBlock(
                    "biomass_gasifier",
                    BiomassGasifierBlock::new,
                    () -> BiomassGasifierBlockEntity.DEFINITION
            );

    public static final RegistryObject<AlloyFactoryBlock>
            ALLOY_FACTORY =
            registerMachineBlock(
                    "alloy_factory",
                    AlloyFactoryBlock::new,
                    () -> AlloyFactoryBlockEntity.DEFINITION
            );

    //  MODULAR ADSORPTION COLUMN
    public static final RegistryObject<AdsorptionColumnControllerBlock>
            ADSORPTION_COLUMN_CONTROLLER =
            registerBlock(
                    "adsorption_column_controller",
                    AdsorptionColumnControllerBlock::new
            );

    public static final RegistryObject<AdsorptionBedBlock>
            ADSORPTION_BED =
            registerBlock(
                    "adsorption_bed",
                    AdsorptionBedBlock::new
            );

    public static final RegistryObject<FluidDistributorBlock>
            FLUID_DISTRIBUTOR =
            registerBlock(
                    "fluid_distributor",
                    FluidDistributorBlock::new
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

    @SafeVarargs
    private static <T extends Block> RegistryObject<T>
    registerMachineBlock(
            String name,
            Supplier<T> blockFactory,
            Supplier<MachineDefinition> definition,
            Supplier<? extends net.minecraft.world.level.ItemLike>...
                    specializationModules
    ) {
        RegistryObject<T> block =
                BLOCKS.register(
                        name,
                        blockFactory
                );

        ModItems.ITEMS.register(
                name,
                () -> new TooltipBlockItem(
                        block.get(),
                        new Item.Properties(),
                        ChatFormatting.YELLOW,
                        Tooltips.machine(
                                definition,
                                specializationModules
                        )
                )
        );

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
