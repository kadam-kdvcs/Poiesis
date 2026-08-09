package org.kdvcs.poiesis.screen.config;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public final class AutoIoButton extends Button {

    private static final int SIZE = 18;
    private static final int ICON_SIZE = 16;

    private static final int ENABLED_OVERLAY = 0x663E7CB1;

    private final ItemStack icon;
    private final BooleanSupplier enabledSupplier;

    public AutoIoButton(
            int x,
            int y,
            ItemStack icon,
            BooleanSupplier enabledSupplier,
            OnPress onPress
    ) {
        super(
                x,
                y,
                SIZE,
                SIZE,
                Component.empty(),
                onPress,
                DEFAULT_NARRATION
        );

        this.icon = icon;
        this.enabledSupplier = enabledSupplier;
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(false);
    }

    @Override
    protected void renderWidget(
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

        if (enabledSupplier.getAsBoolean()) {
            graphics.fill(
                    getX() + 1,
                    getY() + 1,
                    getX() + width - 1,
                    getY() + height - 1,
                    ENABLED_OVERLAY
            );
        }

        int iconX =
                getX()
                        + (SIZE - ICON_SIZE) / 2;

        int iconY =
                getY()
                        + (SIZE - ICON_SIZE) / 2;

        graphics.renderItem(
                icon,
                iconX,
                iconY
        );
    }
}