package org.kdvcs.poiesis.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.IntSupplier;

public final class PipeBlockItem extends BlockItem {
    private final IntSupplier transferRate;
    private final String unit;

    public PipeBlockItem(Block block, Properties properties, IntSupplier transferRate, String unit) {
        super(block, properties);
        this.transferRate = transferRate;
        this.unit = unit;
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(ChatFormatting.AQUA);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        String formattedRate = String.format(Locale.ROOT, "%,d", transferRate.getAsInt());
        tooltip.add(Component.translatable("tooltip.poiesis.pipe.transfer_rate", formattedRate, unit).withStyle(ChatFormatting.GRAY));
    }
}
