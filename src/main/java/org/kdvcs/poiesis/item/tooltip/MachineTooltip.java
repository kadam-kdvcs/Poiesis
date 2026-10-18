package org.kdvcs.poiesis.item.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.IoChannel;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Standard machine "nameplate" tooltip.
 *
 * Capacity is read from the real MachineDefinition, so the tooltip cannot drift
 * away from the machine's runtime energy configuration.
 */
public final class MachineTooltip implements TooltipAppender {

    private static final String MODULE_PREFIX = "模块：";

    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private final Supplier<MachineDefinition> definition;
    private final List<Supplier<? extends ItemLike>>
            specializationModules;

    public MachineTooltip(
            Supplier<MachineDefinition> definition,
            List<Supplier<? extends ItemLike>>
                    specializationModules
    ) {
        this.definition = definition;
        this.specializationModules =
                List.copyOf(specializationModules);
    }

    @Override
    public void append(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        MachineDefinition machine = definition.get();

        if (machine.io().supports(IoChannel.ENERGY)) {
            tooltip.add(
                    Component.translatable(
                            "tooltip.poiesis.machine.max_energy",
                            Component.literal(
                                    NUMBER_FORMAT.format(
                                            machine.energy().capacity()
                                    ) + " FE"
                            ).withStyle(ChatFormatting.GRAY)
                    ).withStyle(ChatFormatting.GREEN)
            );
        }

        if (specializationModules.isEmpty()) {
            return;
        }

        tooltip.add(
                Component.translatable(
                        "tooltip.poiesis.machine.specializations"
                ).withStyle(ChatFormatting.AQUA)
        );

        for (Supplier<? extends ItemLike> module
                : specializationModules) {
            Component moduleDescription =
                    module.get().asItem().getDescription();
            String moduleName = moduleDescription.getString();
            if (moduleName.startsWith(MODULE_PREFIX)) {
                moduleName = moduleName.substring(MODULE_PREFIX.length());
            }

            tooltip.add(
                    Component.translatable(
                            "tooltip.poiesis.machine.specialization_entry",
                            moduleName
                    ).withStyle(ChatFormatting.GRAY)
            );
        }
    }
}
