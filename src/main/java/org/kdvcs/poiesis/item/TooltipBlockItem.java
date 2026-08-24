package org.kdvcs.poiesis.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.item.tooltip.TooltipAppender;

import java.util.List;

/**
 * Reusable BlockItem shell for styled names and composable tooltips.
 */
public class TooltipBlockItem extends BlockItem {

    private final ChatFormatting nameColor;
    private final List<TooltipAppender> tooltipAppenders;

    public TooltipBlockItem(
            Block block,
            Properties properties,
            ChatFormatting nameColor,
            TooltipAppender... tooltipAppenders
    ) {
        super(
                block,
                properties
        );
        this.nameColor = nameColor;
        this.tooltipAppenders =
                List.of(tooltipAppenders);
    }

    @Override
    public Component getName(
            ItemStack stack
    ) {
        Component name = super.getName(stack);

        if (nameColor == null) {
            return name;
        }

        return name.copy()
                .withStyle(nameColor);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(
                stack,
                level,
                tooltip,
                flag
        );

        for (TooltipAppender appender
                : tooltipAppenders) {
            appender.append(
                    stack,
                    level,
                    tooltip,
                    flag
            );
        }
    }
}
