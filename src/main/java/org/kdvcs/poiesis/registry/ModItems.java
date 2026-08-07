package org.kdvcs.poiesis.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
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

    public static final ResourceLocation PRESSING =
            capability("pressing");

    public static final ResourceLocation GRINDING =
            capability("grinding");

    public static final ResourceLocation EXTRUSION =
            capability("extrusion");

    public static final RegistryObject<Item> EXAMPLE_ITEM =
            registerItem("example_item");

    public static final RegistryObject<Item> PRESSED_PLATE =
            registerItem("pressed_plate");

    public static final RegistryObject<Item> SPECIALIZATION_MODULE_SUBSTRATE =
            registerItem("specialization_module_substrate");

    public static final RegistryObject<Item> PERFORMANCE_MODULE_SUBSTRATE =
            registerItem("performance_module_substrate");

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