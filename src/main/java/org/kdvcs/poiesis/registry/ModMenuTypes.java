package org.kdvcs.poiesis.registry;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.screen.AdsorptionColumnMenu;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;
import org.kdvcs.poiesis.screen.BiomassGasifierMenu;
import org.kdvcs.poiesis.screen.CompounderMenu;
import org.kdvcs.poiesis.screen.MechanicalProcessorMenu;
import org.kdvcs.poiesis.screen.ModuleFabricatorMenu;
import org.kdvcs.poiesis.screen.SeparatorMenu;
import org.kdvcs.poiesis.screen.config.MachineSideConfigMenu;

public final class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, Poiesis.MOD_ID);
    public static final RegistryObject<MenuType<MechanicalProcessorMenu>> MECHANICAL_PROCESSOR =
            MENUS.register("mechanical_processor", () -> IForgeMenuType.create(MechanicalProcessorMenu::new));

    public static final RegistryObject<MenuType<ModuleFabricatorMenu>> MODULE_FABRICATOR =
            MENUS.register("module_fabricator", () -> IForgeMenuType.create(ModuleFabricatorMenu::new));

    public static final RegistryObject<MenuType<CompounderMenu>> COMPOUNDER =
            MENUS.register("compounder", () -> IForgeMenuType.create(CompounderMenu::new));

    public static final RegistryObject<MenuType<SeparatorMenu>> SEPARATOR =
            MENUS.register("separator", () -> IForgeMenuType.create(SeparatorMenu::new));

    public static final RegistryObject<MenuType<AdsorptionColumnMenu>> ADSORPTION_COLUMN =
            MENUS.register("adsorption_column", () -> IForgeMenuType.create(AdsorptionColumnMenu::new));

    public static final RegistryObject<MenuType<BiomassGasifierMenu>> BIOMASS_GASIFIER =
            MENUS.register("biomass_gasifier", () -> IForgeMenuType.create(BiomassGasifierMenu::new));

    public static final RegistryObject<MenuType<AlloyFactoryMenu>> ALLOY_FACTORY =
            MENUS.register("alloy_factory", () -> IForgeMenuType.create(AlloyFactoryMenu::new));

    public static final RegistryObject<MenuType<MachineSideConfigMenu>> MACHINE_SIDE_CONFIG =
            MENUS.register("machine_side_config", () -> IForgeMenuType.create(MachineSideConfigMenu::new));
    private ModMenuTypes() { }
    public static void register(IEventBus eventBus) { MENUS.register(eventBus); }
}
