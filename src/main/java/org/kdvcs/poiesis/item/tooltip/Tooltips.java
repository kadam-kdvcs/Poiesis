package org.kdvcs.poiesis.item.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.world.level.ItemLike;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Central factory for reusable tooltip types.
 *
 * Add future tooltip kinds here instead of teaching every Item subclass how
 * to build its own tooltip.
 */
public final class Tooltips {

    public static TooltipAppender text(
            String translationKey
    ) {
        return text(
                translationKey,
                ChatFormatting.GRAY
        );
    }

    public static TooltipAppender text(
            String translationKey,
            ChatFormatting color
    ) {
        return new TextTooltip(
                translationKey,
                color
        );
    }

    @SafeVarargs
    public static TooltipAppender machine(
            Supplier<MachineDefinition> definition,
            Supplier<? extends ItemLike>...
                    specializationModules
    ) {
        return new MachineTooltip(
                definition,
                Arrays.asList(
                        specializationModules
                )
        );
    }

    public static TooltipAppender machine(
            Supplier<MachineDefinition> definition
    ) {
        return new MachineTooltip(
                definition,
                List.of()
        );
    }

    private Tooltips() {
    }
}
