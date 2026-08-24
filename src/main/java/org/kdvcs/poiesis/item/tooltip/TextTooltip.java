package org.kdvcs.poiesis.item.tooltip;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Generic one-line translated tooltip.
 */
public final class TextTooltip implements TooltipAppender {

    private final String translationKey;
    private final ChatFormatting color;

    public TextTooltip(
            String translationKey,
            ChatFormatting color
    ) {
        this.translationKey = translationKey;
        this.color = color;
    }

    @Override
    public void append(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        tooltip.add(
                Component.translatable(
                        translationKey
                ).withStyle(color)
        );
    }
}
