package org.kdvcs.poiesis.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;
import org.kdvcs.poiesis.screen.ModuleFabricatorScreen;
import org.kdvcs.poiesis.screen.config.MachineSideConfigScreen;

@Mod.EventBusSubscriber(modid = Poiesis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() { }
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(
                    ModMenuTypes.MECHANICAL_PROCESSOR.get(),
                    MechanicalProcessorScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.MODULE_FABRICATOR.get(),
                    ModuleFabricatorScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.MACHINE_SIDE_CONFIG.get(),
                    MachineSideConfigScreen::new
            );
        });
    }
}
