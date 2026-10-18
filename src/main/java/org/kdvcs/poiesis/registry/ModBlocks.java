package org.kdvcs.poiesis.registry;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.LiquidBlock;
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
import org.kdvcs.poiesis.block.GasGeneratorBlock;
import org.kdvcs.poiesis.block.EnergyPipeBlock;
import org.kdvcs.poiesis.block.FluidPipeBlock;
import org.kdvcs.poiesis.block.ItemPipeBlock;
import org.kdvcs.poiesis.block.LeacherBlock;
import org.kdvcs.poiesis.block.FluidDistributorBlock;
import org.kdvcs.poiesis.block.GreenhouseBlock;
import org.kdvcs.poiesis.block.MechanicalProcessorBlock;
import org.kdvcs.poiesis.block.MicrobialFuelCellBlock;
import org.kdvcs.poiesis.block.ModuleFabricatorBlock;
import org.kdvcs.poiesis.block.SeparatorBlock;
import org.kdvcs.poiesis.block.entity.CompounderBlockEntity;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.block.entity.BiomassGasifierBlockEntity;
import org.kdvcs.poiesis.block.entity.GasGeneratorBlockEntity;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.block.entity.MicrobialFuelCellBlockEntity;
import org.kdvcs.poiesis.block.entity.LeacherBlockEntity;
import org.kdvcs.poiesis.block.entity.GreenhouseBlockEntity;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.block.entity.SeparatorBlockEntity;
import org.kdvcs.poiesis.block.wood.FlammableLeavesBlock;
import org.kdvcs.poiesis.block.wood.FlammableButtonBlock;
import org.kdvcs.poiesis.block.wood.FlammableDoorBlock;
import org.kdvcs.poiesis.block.wood.FlammableFenceBlock;
import org.kdvcs.poiesis.block.wood.FlammableFenceGateBlock;
import org.kdvcs.poiesis.block.wood.FlammablePillarBlock;
import org.kdvcs.poiesis.block.wood.FlammableSlabBlock;
import org.kdvcs.poiesis.block.wood.FlammableStairBlock;
import org.kdvcs.poiesis.block.wood.FlammableTrapDoorBlock;
import org.kdvcs.poiesis.block.wood.FlammableWoodBlock;
import org.kdvcs.poiesis.item.PipeBlockItem;
import org.kdvcs.poiesis.item.TooltipBlockItem;
import org.kdvcs.poiesis.item.tooltip.Tooltips;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.worldgen.tree.AlderTreeGrower;

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

    public static final RegistryObject<Block> GRAPHITE_BLOCK =
            registerBlock(
                    "graphite_block",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(
                                    Blocks.COAL_BLOCK
                            ).requiresCorrectToolForDrops()
                    )
            );

    /**
     * Neutral concrete variants.  Copying the vanilla white variants keeps
     * hardness, sound, tool requirements and all other block behaviour in
     * lockstep with vanilla concrete.  The powder uses the vanilla
     * ConcretePowderBlock implementation so it hardens when it touches water.
     */
    public static final RegistryObject<Block> PLAIN_CONCRETE =
            registerBlock(
                    "plain_concrete",
                    () -> new Block(BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE))
            );

    public static final RegistryObject<Block> PLAIN_CONCRETE_POWDER =
            registerBlock(
                    "plain_concrete_powder",
                    () -> new ConcretePowderBlock(
                            PLAIN_CONCRETE.get(),
                            BlockBehaviour.Properties.copy(Blocks.GRAY_CONCRETE_POWDER)
                    )
            );

    // ALDER TREE
    public static final RegistryObject<FlammablePillarBlock> ALDER_LOG =
            registerBlock(
                    "alder_log",
                    () -> new FlammablePillarBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_LOG),
                            5,
                            5
                    )
            );

    public static final RegistryObject<FlammablePillarBlock> ALDER_WOOD =
            registerBlock(
                    "alder_wood",
                    () -> new FlammablePillarBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_WOOD),
                            5,
                            5
                    )
            );

    public static final RegistryObject<FlammablePillarBlock> STRIPPED_ALDER_LOG =
            registerBlock(
                    "stripped_alder_log",
                    () -> new FlammablePillarBlock(
                            BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG),
                            5,
                            5
                    )
            );

    public static final RegistryObject<FlammablePillarBlock> STRIPPED_ALDER_WOOD =
            registerBlock(
                    "stripped_alder_wood",
                    () -> new FlammablePillarBlock(
                            BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD),
                            5,
                            5
                    )
            );

    public static final RegistryObject<FlammableWoodBlock> ALDER_PLANKS =
            registerBlock(
                    "alder_planks",
                    () -> new FlammableWoodBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS),
                            5,
                            20
                    )
            );

    /**
     * Underground root-system block used by alder trees. The temporary
     * casing-like texture makes generated roots easy to locate while the
     * final root texture is being prepared.
     */
    public static final RegistryObject<Block> ALDER_ROOT =
            registerBlock(
                    "alder_root",
                    () -> new Block(
                            BlockBehaviour.Properties.copy(Blocks.ROOTED_DIRT)
                    )
            );

    public static final RegistryObject<FlammableStairBlock> ALDER_STAIRS =
            registerBlock(
                    "alder_stairs",
                    () -> new FlammableStairBlock(
                            ALDER_PLANKS.get().defaultBlockState(),
                            BlockBehaviour.Properties.copy(Blocks.OAK_STAIRS),
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableSlabBlock> ALDER_SLAB =
            registerBlock(
                    "alder_slab",
                    () -> new FlammableSlabBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_SLAB),
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableFenceBlock> ALDER_FENCE =
            registerBlock(
                    "alder_fence",
                    () -> new FlammableFenceBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE),
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableFenceGateBlock> ALDER_FENCE_GATE =
            registerBlock(
                    "alder_fence_gate",
                    () -> new FlammableFenceGateBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_FENCE_GATE),
                            WoodType.OAK,
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableButtonBlock> ALDER_BUTTON =
            registerBlock(
                    "alder_button",
                    () -> new FlammableButtonBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_BUTTON),
                            BlockSetType.OAK,
                            30,
                            true,
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableDoorBlock> ALDER_DOOR =
            registerBlock(
                    "alder_door",
                    () -> new FlammableDoorBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_DOOR),
                            BlockSetType.OAK,
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableTrapDoorBlock> ALDER_TRAPDOOR =
            registerBlock(
                    "alder_trapdoor",
                    () -> new FlammableTrapDoorBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_TRAPDOOR),
                            BlockSetType.OAK,
                            5,
                            20
                    )
            );

    public static final RegistryObject<FlammableLeavesBlock> ALDER_LEAVES =
            registerBlock(
                    "alder_leaves",
                    () -> new FlammableLeavesBlock(
                            BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)
                    )
            );

    public static final RegistryObject<SaplingBlock> ALDER_SAPLING =
            registerBlock(
                    "alder_sapling",
                    () -> new SaplingBlock(
                            new AlderTreeGrower(),
                            BlockBehaviour.Properties.copy(Blocks.OAK_SAPLING)
                    )
            );

    public static final RegistryObject<FlowerPotBlock> POTTED_ALDER_SAPLING =
            BLOCKS.register(
                    "potted_alder_sapling",
                    () -> new FlowerPotBlock(
                            () -> (FlowerPotBlock) Blocks.FLOWER_POT,
                            ALDER_SAPLING,
                            BlockBehaviour.Properties.copy(Blocks.POTTED_OAK_SAPLING)
                                    .noOcclusion()
                    )
            );

    public static final RegistryObject<LiquidBlock> REFINED_ALBICHROME_SLURRY =
            BLOCKS.register("refined_albichrome_slurry", () -> new LiquidBlock(
                    ModFluids.REFINED_ALBICHROME_SLURRY,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

    public static final RegistryObject<LiquidBlock> CHROMIUM_BEARING_WASTE =
            BLOCKS.register("chromium_bearing_waste", () -> new LiquidBlock(
                    ModFluids.CHROMIUM_BEARING_WASTE,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

    public static final RegistryObject<LiquidBlock> NUTRIENT_SOLUTION =
            BLOCKS.register("nutrient_solution", () -> new LiquidBlock(
                    ModFluids.NUTRIENT_SOLUTION,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

    public static final RegistryObject<LiquidBlock> ORGANIC_NUTRIENT_SOLUTION =
            BLOCKS.register("organic_nutrient_solution", () -> new LiquidBlock(
                    ModFluids.ORGANIC_NUTRIENT_SOLUTION,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

    public static final RegistryObject<LiquidBlock> VITAMIN_SOLUTION =
            BLOCKS.register("vitamin_solution", () -> new LiquidBlock(
                    ModFluids.VITAMIN_SOLUTION,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

    public static final RegistryObject<LiquidBlock> WASTEWATER =
            BLOCKS.register("wastewater", () -> new LiquidBlock(
                    ModFluids.WASTEWATER,
                    BlockBehaviour.Properties.copy(Blocks.WATER)
            ));

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
                    () -> ModItems.WET_SEPARATION_MODULE.get(),
                    () -> ModItems.RINSING_MODULE.get(),
                    () -> ModItems.ELECTROSTATIC_SEPARATION_MODULE.get()
            );

    public static final RegistryObject<LeacherBlock> LEACHER =
            registerMachineBlock(
                    "leacher",
                    LeacherBlock::new,
                    () -> LeacherBlockEntity.DEFINITION
            );

    public static final RegistryObject<BiomassGasifierBlock>
            BIOMASS_GASIFIER =
            registerMachineBlock(
                    "biomass_gasifier",
                    BiomassGasifierBlock::new,
                    () -> BiomassGasifierBlockEntity.DEFINITION
            );

    public static final RegistryObject<GasGeneratorBlock> GAS_GENERATOR =
            registerMachineBlock(
                    "gas_generator",
                    GasGeneratorBlock::new,
                    () -> GasGeneratorBlockEntity.DEFINITION
            );

    public static final RegistryObject<MicrobialFuelCellBlock> MICROBIAL_FUEL_CELL =
            registerMachineBlock(
                    "microbial_fuel_cell",
                    MicrobialFuelCellBlock::new,
                    () -> MicrobialFuelCellBlockEntity.DEFINITION
            );

    public static final RegistryObject<AlloyFactoryBlock>
            ALLOY_FACTORY =
            registerMachineBlock(
                    "alloy_factory",
                    AlloyFactoryBlock::new,
                    () -> AlloyFactoryBlockEntity.DEFINITION
            );

    public static final RegistryObject<GreenhouseBlock>
            GREENHOUSE =
            registerMachineBlock(
                    "greenhouse",
                    GreenhouseBlock::new,
                    () -> GreenhouseBlockEntity.DEFINITION
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
            Supplier<? extends ItemLike>...
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
