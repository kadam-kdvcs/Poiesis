package org.kdvcs.poiesis.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class CraftIconButton extends Button {

    private static final int SIZE = 18;

    private static final ItemStack CRAFTING_TABLE_ICON =
            new ItemStack(Items.CRAFTING_TABLE);

    public CraftIconButton(
            int x,
            int y,
            Runnable pressAction
    ) {
        super(
                x,
                y,
                SIZE,
                SIZE,
                Component.empty(),
                button -> pressAction.run(),
                DEFAULT_NARRATION
        );
    }

    @Override
    public void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        super.renderWidget(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        graphics.renderItem(
                CRAFTING_TABLE_ICON,
                getX() + 1,
                getY() + 1
        );
    }
}