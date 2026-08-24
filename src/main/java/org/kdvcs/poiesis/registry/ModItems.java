package org.kdvcs.poiesis.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.item.HeatResistantSteelArmorMaterial;
import org.kdvcs.poiesis.item.HeatResistantSteelArmorItem;
import org.kdvcs.poiesis.item.PipeConfiguratorItem;
import net.minecraft.world.item.ArmorItem;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.machine.module.ModuleKind;

import java.util.Set;
import java.util.function.Function;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(
                    ForgeRegistries.ITEMS,
                    Poiesis.MOD_ID
            );

    //  register capabilities of specialization modules
    public static final ResourceLocation PRESSING = capability("pressing");
    public static final ResourceLocation GRINDING = capability("grinding");
    public static final ResourceLocation EXTRUSION = capability("extrusion");
    public static final ResourceLocation HIGH_SHEAR = capability("high_shear");
    public static final ResourceLocation WET_SEPARATION = capability("wet_separation");

    public static final RegistryObject<Item> SILICA_POWDER = registerItem("silica_powder");
    public static final RegistryObject<Item> IRON_DUST = registerItem("iron_dust");
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_DUST = registerItem("heat_resistant_steel_dust");
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_INGOT = registerItem("heat_resistant_steel_ingot");
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_HELMET = registerItem(
            "heat_resistant_steel_helmet",
            properties -> new HeatResistantSteelArmorItem(
                    HeatResistantSteelArmorMaterial.INSTANCE,
                    ArmorItem.Type.HELMET,
                    properties
            )
    );
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_CHESTPLATE = registerItem(
            "heat_resistant_steel_chestplate",
            properties -> new HeatResistantSteelArmorItem(
                    HeatResistantSteelArmorMaterial.INSTANCE,
                    ArmorItem.Type.CHESTPLATE,
                    properties
            )
    );
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_LEGGINGS = registerItem(
            "heat_resistant_steel_leggings",
            properties -> new HeatResistantSteelArmorItem(
                    HeatResistantSteelArmorMaterial.INSTANCE,
                    ArmorItem.Type.LEGGINGS,
                    properties
            )
    );
    public static final RegistryObject<Item> HEAT_RESISTANT_STEEL_BOOTS = registerItem(
            "heat_resistant_steel_boots",
            properties -> new HeatResistantSteelArmorItem(
                    HeatResistantSteelArmorMaterial.INSTANCE,
                    ArmorItem.Type.BOOTS,
                    properties
            )
    );
    public static final RegistryObject<Item> KENAF_FIBER = registerItem("kenaf_fiber");
    public static final RegistryObject<Item> KENAF_CORE = registerItem("kenaf_core");
    public static final RegistryObject<Item> RAW_KENAF_BAST = registerItem("raw_kenaf_bast");
    public static final RegistryObject<Item> GLASS_BATCH = registerItem("glass_batch");
    public static final RegistryObject<Item> FIBER_COMPOSITE = registerItem("fiber_composite");
    public static final RegistryObject<Item> FILTER_PLATE = registerItem("filter_plate");
    public static final RegistryObject<Item> SPECIALIZATION_MODULE_SUBSTRATE = registerItem("specialization_module_substrate");
    public static final RegistryObject<Item> PERFORMANCE_MODULE_SUBSTRATE = registerItem("performance_module_substrate");
    public static final RegistryObject<Item> ALBICHROME_INGOT = registerItem("albichrome_ingot");
    public static final RegistryObject<Item> RAW_ALBICHROME = registerItem("raw_albichrome");
    public static final RegistryObject<Item> ALBICHROME_NUGGET = registerItem("albichrome_nugget");
    public static final RegistryObject<Item> ALBICHROME_DUST = registerItem("albichrome_dust");
    public static final RegistryObject<Item> MACHINE_UNIT = registerItem("machine_unit");
    public static final RegistryObject<Item> CONTROL_ELEMENT = registerItem("control_element");
    public static final RegistryObject<PipeConfiguratorItem> PIPE_CONFIGURATOR = registerItem("pipe_configurator", PipeConfiguratorItem::new);

    public static final RegistryObject<Item> GRINDING_STONE = registerItem(
            "grinding_stone",
            properties -> new Item(properties.durability(64))
    );

    public static final RegistryObject<MachineModuleItem> PRESSING_MODULE =
            registerModule(
                    "pressing_module",
                    ModuleKind.PROCESS,
                    MechanicalMode.PRESSING,
                    Set.of(PRESSING),
                    0,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> GRINDING_MODULE =
            registerModule(
                    "grinding_module",
                    ModuleKind.PROCESS,
                    MechanicalMode.GRINDING,
                    Set.of(GRINDING),
                    0,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> EXTRUSION_MODULE =
            registerModule(
                    "extrusion_module",
                    ModuleKind.PROCESS,
                    MechanicalMode.EXTRUSION,
                    Set.of(EXTRUSION),
                    0,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> WET_SEPARATION_MODULE =
            registerModule(
                    "wet_separation_module",
                    ModuleKind.PROCESS,
                    MechanicalMode.NONE,
                    Set.of(WET_SEPARATION),
                    0,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> HIGH_SHEAR_MODULE =
            registerModule(
                    "high_shear_module",
                    ModuleKind.PROCESS,
                    MechanicalMode.NONE,
                    Set.of(HIGH_SHEAR),
                    0,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> SPEED_MODULE =
            registerModule(
                    "speed_module",
                    ModuleKind.PERFORMANCE,
                    MechanicalMode.NONE,
                    Set.of(),
                    1,
                    1.0D
            );

    public static final RegistryObject<MachineModuleItem> EFFICIENCY_MODULE =
            registerModule(
                    "efficiency_module",
                    ModuleKind.PERFORMANCE,
                    MechanicalMode.NONE,
                    Set.of(),
                    0,
                    0.80D
            );

    private static RegistryObject<Item> registerItem(
            String name
    ) {
        return registerItem(
                name,
                Item::new
        );
    }

    private static <T extends Item> RegistryObject<T> registerItem(
            String name,
            Function<Item.Properties, T> factory
    ) {
        return ITEMS.register(
                name,
                () -> factory.apply(
                        new Item.Properties()
                )
        );
    }

    private static RegistryObject<MachineModuleItem> registerModule(
            String name,
            ModuleKind kind,
            MechanicalMode mode,
            Set<ResourceLocation> capabilities,
            int speedBonus,
            double energyMultiplier
    ) {
        return registerItem(
                name,
                properties -> new MachineModuleItem(
                        properties,
                        kind,
                        mode,
                        capabilities,
                        speedBonus,
                        energyMultiplier
                )
        );
    }

    private static ResourceLocation capability(
            String path
    ) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                path
        );
    }

    public static void register(
            IEventBus eventBus
    ) {
        ITEMS.register(eventBus);
    }

    private ModItems() {
    }
}
