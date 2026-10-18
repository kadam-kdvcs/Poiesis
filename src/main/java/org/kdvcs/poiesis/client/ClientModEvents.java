package org.kdvcs.poiesis.client;

import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.FoliageColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.AdsorptionColumnScreen;
import org.kdvcs.poiesis.screen.AlloyFactoryScreen;
import org.kdvcs.poiesis.screen.BiomassGasifierScreen;
import org.kdvcs.poiesis.screen.GasGeneratorScreen;
import org.kdvcs.poiesis.screen.CompounderScreen;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;
import org.kdvcs.poiesis.screen.MicrobialFuelCellScreen;
import org.kdvcs.poiesis.screen.LeacherScreen;
import org.kdvcs.poiesis.screen.ModuleFabricatorScreen;
import org.kdvcs.poiesis.screen.SeparatorScreen;
import org.kdvcs.poiesis.screen.config.MachineSideConfigScreen;
import org.kdvcs.poiesis.screen.FeedbackScreen;
import org.kdvcs.poiesis.screen.GreenhouseScreen;
import org.kdvcs.poiesis.feedback.FeedbackSender;

@Mod.EventBusSubscriber(modid = Poiesis.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {
    private ClientModEvents() { }

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register(
                (state, level, pos, tintIndex) -> level != null && pos != null
                        ? BiomeColors.getAverageFoliageColor(level, pos)
                        : FoliageColor.getDefaultColor(),
                ModBlocks.ALDER_LEAVES.get()
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> FoliageColor.getDefaultColor(),
                ModBlocks.ALDER_LEAVES.get()
        );
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        FeedbackSender.retryPending();
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ALDER_DOOR.get(),
                    RenderType.cutout()
            );
            ItemBlockRenderTypes.setRenderLayer(
                    ModBlocks.ALDER_TRAPDOOR.get(),
                    RenderType.cutout()
            );

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
                    ModMenuTypes.LEACHER.get(),
                    LeacherScreen::new
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
                    ModMenuTypes.GAS_GENERATOR.get(),
                    GasGeneratorScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.MICROBIAL_FUEL_CELL.get(),
                    MicrobialFuelCellScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.ALLOY_FACTORY.get(),
                    AlloyFactoryScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.GREENHOUSE.get(),
                    GreenhouseScreen::new
            );

            MenuScreens.register(
                    ModMenuTypes.MACHINE_SIDE_CONFIG.get(),
                    MachineSideConfigScreen::new
            );
            MenuScreens.register(
                    ModMenuTypes.FEEDBACK.get(),
                    FeedbackScreen::new
            );
        });
    }
}
