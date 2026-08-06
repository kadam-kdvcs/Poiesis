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
                output.accept(ModBlocks.MECHANICAL_PROCESSOR.get());
                output.accept(ModItems.PRESSING_MODULE.get());
                output.accept(ModItems.GRINDING_MODULE.get());
                output.accept(ModItems.EXTRUSION_MODULE.get());
                output.accept(ModItems.SPEED_MODULE.get());
                output.accept(ModItems.EFFICIENCY_MODULE.get());
            }).build());
    private ModCreativeTabs() { }
    public static void register(IEventBus eventBus) { TABS.register(eventBus); }
}
