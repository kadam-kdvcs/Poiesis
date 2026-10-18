package org.kdvcs.poiesis.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.world.SimpleMenuProvider;
import org.kdvcs.poiesis.screen.FeedbackMenu;

public final class FeedbackLetterItem extends Item {
    public FeedbackLetterItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider(
                    (containerId, inventory, ignoredPlayer) -> new FeedbackMenu(containerId, inventory),
                    Component.translatable("gui.poiesis.feedback.title")
            ));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
