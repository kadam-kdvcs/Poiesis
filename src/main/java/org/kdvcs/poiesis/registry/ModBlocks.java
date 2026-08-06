package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.MechanicalProcessorBlock;

import java.util.function.Supplier;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(
                    ForgeRegistries.BLOCKS,
                    Poiesis.MOD_ID
            );

    public static final RegistryObject<Block> EXAMPLE_BLOCK =
            registerBlock(
                    "example_block",
                    () -> new Block(
                            BlockBehaviour.Properties
                                    .of()
                                    .noLootTable()
                                    .mapColor(MapColor.METAL)
                                    .strength(3.0F, 6.0F)
                                    .sound(SoundType.METAL)
                    )
            );

    public static final RegistryObject<MechanicalProcessorBlock>
            MECHANICAL_PROCESSOR =
            registerBlock(
                    "mechanical_processor",
                    MechanicalProcessorBlock::new
            );

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