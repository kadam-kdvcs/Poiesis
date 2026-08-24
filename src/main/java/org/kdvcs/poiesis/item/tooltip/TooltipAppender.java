package org.kdvcs.poiesis.item.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Small, composable unit that appends one kind of tooltip content.
 *
 * Item classes only delegate here; tooltip semantics stay reusable and
 * independent from individual item registrations.
 */
@FunctionalInterface
public interface TooltipAppender {

    void append(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    );
}
