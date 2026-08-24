package org.kdvcs.poiesis.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.AdsorptionColumnScreen;
import org.kdvcs.poiesis.screen.AlloyFactoryScreen;
import org.kdvcs.poiesis.screen.BiomassGasifierScreen;
import org.kdvcs.poiesis.screen.CompounderScreen;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;
import org.kdvcs.poiesis.screen.ModuleFabricatorScreen;
import org.kdvcs.poiesis.screen.SeparatorScreen;
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
                    ModMenuTypes.COMPOUNDER.get(),
                    CompounderScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.SEPARATOR.get(),
                    SeparatorScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.ADSORPTION_COLUMN.get(),
                    AdsorptionColumnScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.BIOMASS_GASIFIER.get(),
                    BiomassGasifierScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.ALLOY_FACTORY.get(),
                    AlloyFactoryScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.MACHINE_SIDE_CONFIG.get(),
                    MachineSideConfigScreen::new
            );
        });
    }
}
