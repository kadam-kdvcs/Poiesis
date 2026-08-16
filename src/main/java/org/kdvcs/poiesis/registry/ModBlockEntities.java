package org.kdvcs.poiesis.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.entity.CompounderBlockEntity;
import org.kdvcs.poiesis.block.entity.EnergyPipeBlockEntity;
import org.kdvcs.poiesis.block.entity.FluidPipeBlockEntity;
import org.kdvcs.poiesis.block.entity.ItemPipeBlockEntity;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Poiesis.MOD_ID);
    public static final RegistryObject<BlockEntityType<MechanicalProcessorBlockEntity>> MECHANICAL_PROCESSOR =
            BLOCK_ENTITIES.register("mechanical_processor", () ->
                    BlockEntityType.Builder.of(MechanicalProcessorBlockEntity::new, ModBlocks.MECHANICAL_PROCESSOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<ModuleFabricatorBlockEntity>> MODULE_FABRICATOR =
            BLOCK_ENTITIES.register("module_fabricator", () ->
                    BlockEntityType.Builder.of(ModuleFabricatorBlockEntity::new, ModBlocks.MODULE_FABRICATOR.get()).build(null));
    public static final RegistryObject<BlockEntityType<CompounderBlockEntity>> COMPOUNDER =
            BLOCK_ENTITIES.register("compounder", () ->
                    BlockEntityType.Builder.of(CompounderBlockEntity::new, ModBlocks.COMPOUNDER.get()).build(null));
    public static final RegistryObject<BlockEntityType<EnergyPipeBlockEntity>> ENERGY_PIPE =
            BLOCK_ENTITIES.register("energy_pipe", () ->
                    BlockEntityType.Builder.of(EnergyPipeBlockEntity::new, ModBlocks.BASIC_ENERGY_PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<ItemPipeBlockEntity>> ITEM_PIPE =
            BLOCK_ENTITIES.register("item_pipe", () ->
                    BlockEntityType.Builder.of(ItemPipeBlockEntity::new, ModBlocks.BASIC_ITEM_PIPE.get()).build(null));
    public static final RegistryObject<BlockEntityType<FluidPipeBlockEntity>> FLUID_PIPE =
            BLOCK_ENTITIES.register("fluid_pipe", () ->
                    BlockEntityType.Builder.of(FluidPipeBlockEntity::new, ModBlocks.BASIC_FLUID_PIPE.get()).build(null));
    private ModBlockEntities() { }
    public static void register(IEventBus eventBus) { BLOCK_ENTITIES.register(eventBus); }
}
