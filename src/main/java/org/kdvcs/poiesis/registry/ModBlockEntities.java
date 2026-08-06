package org.kdvcs.poiesis.registry;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.entity.MechanicalProcessorBlockEntity;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Poiesis.MOD_ID);
    public static final RegistryObject<BlockEntityType<MechanicalProcessorBlockEntity>> MECHANICAL_PROCESSOR =
            BLOCK_ENTITIES.register("mechanical_processor", () ->
                    BlockEntityType.Builder.of(MechanicalProcessorBlockEntity::new, ModBlocks.MECHANICAL_PROCESSOR.get()).build(null));
    private ModBlockEntities() { }
    public static void register(IEventBus eventBus) { BLOCK_ENTITIES.register(eventBus); }
}
