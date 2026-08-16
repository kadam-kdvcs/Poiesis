package org.kdvcs.poiesis.compat.jei.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.machine.module.ModuleKind;

import java.util.List;

/**
 * JEI-side discovery for generic specialization modules.
 *
 * This mirrors ModuleLayout.specialization(): PROCESS modules with no
 * Mechanical Processor mode and at least one capability.
 */
public final class JeiSpecializationModules {

    public static List<ItemStack> forCapability(
            ResourceLocation capability
    ) {
        if (capability == null) {
            return List.of();
        }

        return ForgeRegistries.ITEMS
                .getValues()
                .stream()
                .filter(item ->
                        item instanceof MachineModuleItem module
                                && module.kind() == ModuleKind.PROCESS
                                && module.mode() == MechanicalMode.NONE
                                && module.capabilities()
                                .contains(capability)
                )
                .map(ItemStack::new)
                .toList();
    }

    private JeiSpecializationModules() {
    }
}
