package org.kdvcs.poiesis.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Poiesis.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.poiesis.main"))
            .icon(() -> ModBlocks.MECHANICAL_PROCESSOR.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (ModCrops.CropEntry crop : ModCrops.all()) {
                    output.accept(crop.seeds().get());
                    output.accept(crop.product().get());
                }

                output.accept(ModItems.RAW_KENAF_BAST.get());
                output.accept(ModItems.KENAF_CORE.get());
                output.accept(ModItems.FIBER_COMPOSITE.get());
                output.accept(ModItems.FILTER_PLATE.get());

                output.accept(ModBlocks.ALBICHROME_ORE.get());
                output.accept(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get());
                output.accept(ModBlocks.SILICA_ROCK.get());
                output.accept(ModItems.SILICA_POWDER.get());
                output.accept(ModItems.GLASS_BATCH.get());

                output.accept(ModItems.RAW_ALBICHROME.get());
                output.accept(ModBlocks.RAW_ALBICHROME_BLOCK.get());
                output.accept(ModItems.ALBICHROME_INGOT.get());
                output.accept(ModBlocks.ALBICHROME_BLOCK.get());
                output.accept(ModItems.ALBICHROME_NUGGET.get());

                output.accept(ModItems.CONTROL_ELEMENT.get());
                output.accept(ModItems.GRINDING_STONE.get());
                output.accept(ModItems.PIPE_CONFIGURATOR.get());
                output.accept(ModItems.MACHINE_UNIT.get());

                output.accept(ModBlocks.ALBICHROME_CASING.get());
                output.accept(ModBlocks.MODULE_FABRICATOR.get());
                output.accept(ModBlocks.MECHANICAL_PROCESSOR.get());
                output.accept(ModBlocks.COMPOUNDER.get());
                output.accept(ModBlocks.BASIC_ENERGY_PIPE.get());
                output.accept(ModBlocks.BASIC_ITEM_PIPE.get());
                output.accept(ModBlocks.BASIC_FLUID_PIPE.get());

                output.accept(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get());
                output.accept(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get());
                output.accept(ModItems.PRESSING_MODULE.get());
                output.accept(ModItems.GRINDING_MODULE.get());
                output.accept(ModItems.EXTRUSION_MODULE.get());
                output.accept(ModItems.SPEED_MODULE.get());
                output.accept(ModItems.EFFICIENCY_MODULE.get());
            }).build());
    private ModCreativeTabs() { }
    public static void register(IEventBus eventBus) { TABS.register(eventBus); }
}
