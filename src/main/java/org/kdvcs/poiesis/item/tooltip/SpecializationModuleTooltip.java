package org.kdvcs.poiesis.item.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.Poiesis;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Lists the machines that accept a specialization module.  The presentation
 * intentionally mirrors the machine tooltip's available-specializations list.
 */
public final class SpecializationModuleTooltip implements TooltipAppender {
    private static final ResourceLocation PROCESSOR = machine("mechanical_processor");
    private static final ResourceLocation COMPOUNDER = machine("compounder");
    private static final ResourceLocation SEPARATOR = machine("separator");

    private static final Map<String, ResourceLocation> MACHINES_BY_CAPABILITY = Map.of(
            "pressing", PROCESSOR,
            "grinding", PROCESSOR,
            "extrusion", PROCESSOR,
            "high_shear", COMPOUNDER,
            "wet_separation", SEPARATOR,
            "rinsing", SEPARATOR,
            "electrostatic_separation", SEPARATOR
    );

    private final Set<ResourceLocation> capabilities;

    public SpecializationModuleTooltip(Set<ResourceLocation> capabilities) {
        this.capabilities = Set.copyOf(capabilities);
    }

    @Override
    public void append(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        Set<ResourceLocation> machineIds = new LinkedHashSet<>();
        for (ResourceLocation capability : capabilities) {
            ResourceLocation machine = MACHINES_BY_CAPABILITY.get(capability.getPath());
            if (machine != null) {
                machineIds.add(machine);
            }
        }

        if (machineIds.isEmpty()) {
            return;
        }

        tooltip.add(
                Component.translatable(
                        "tooltip.poiesis.module.machines"
                ).withStyle(ChatFormatting.AQUA)
        );

        for (ResourceLocation machineId : machineIds) {
            Item machine = ForgeRegistries.ITEMS.getValue(machineId);
            if (machine == null) {
                continue;
            }
            tooltip.add(
                    Component.translatable(
                            "tooltip.poiesis.module.machine_entry",
                            machine.getDescription()
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
    }

    private static ResourceLocation machine(String path) {
        return new ResourceLocation(Poiesis.MOD_ID, path);
    }
}
