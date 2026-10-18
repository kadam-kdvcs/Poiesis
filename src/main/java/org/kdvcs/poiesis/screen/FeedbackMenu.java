package org.kdvcs.poiesis.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.registry.ModMenuTypes;

public final class FeedbackMenu extends AbstractContainerMenu {
    public FeedbackMenu(int containerId, Inventory inventory) {
        this(ModMenuTypes.FEEDBACK.get(), containerId, inventory);
    }

    public FeedbackMenu(int containerId, Inventory inventory, FriendlyByteBuf ignored) {
        this(ModMenuTypes.FEEDBACK.get(), containerId, inventory);
    }

    private FeedbackMenu(MenuType<?> type, int containerId, Inventory inventory) {
        super(type, containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
