package org.kdvcs.poiesis.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SideModeButton extends AbstractButton {
    private static final int ICON_SIZE = 16;

    private final RelativeSide side;
    private final Supplier<SideIoMode> modeSupplier;
    private final Supplier<ItemStack> neighborIconSupplier;
    private final Consumer<Boolean> clickHandler;

    public SideModeButton(
            int x,
            int y,
            int width,
            int height,
            RelativeSide side,
            Supplier<SideIoMode> modeSupplier,
            Supplier<ItemStack> neighborIconSupplier,
            Consumer<Boolean> clickHandler
    ) {
        super(
                x,
                y,
                width,
                height,
                Component.empty()
        );

        this.side = side;
        this.modeSupplier = modeSupplier;
        this.neighborIconSupplier = neighborIconSupplier;
        this.clickHandler = clickHandler;
    }

    public RelativeSide side() {
        return side;
    }

    public SideIoMode mode() {
        return modeSupplier.get();
    }

    @Override
    public void onPress() {
        clickHandler.accept(false);
    }

    @Override
    public boolean mouseClicked(
            double mouseX,
            double mouseY,
            int button
    ) {
        if (button == 1
                && active
                && visible
                && clicked(mouseX, mouseY)) {
            playDownSound(
                    Minecraft.getInstance()
                            .getSoundManager()
            );

            clickHandler.accept(true);
            return true;
        }

        return super.mouseClicked(
                mouseX,
                mouseY,
                button
        );
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        SideIoMode mode = modeSupplier.get();

        int background = switch (mode) {
            case NONE -> 0xFF4A4A4A;
            case INPUT -> 0xFF3E7CB1;
            case OUTPUT -> 0xFFB16B3E;
            case INPUT_OUTPUT -> 0xFF6C4AA0;
        };

        if (isHoveredOrFocused()) {
            background = lighten(background);
        }

        graphics.fill(
                getX(),
                getY(),
                getX() + width,
                getY() + height,
                0xFF101010
        );

        graphics.fill(
                getX() + 1,
                getY() + 1,
                getX() + width - 1,
                getY() + height - 1,
                background
        );

        ItemStack neighborIcon = neighborIconSupplier.get();

        if (!neighborIcon.isEmpty()) {
            int iconX = getX() + (width - ICON_SIZE) / 2;
            int iconY = getY() + (height - ICON_SIZE) / 2;
            graphics.renderItem(neighborIcon, iconX, iconY);
        }
    }

    @Override
    protected void updateWidgetNarration(
            net.minecraft.client.gui.narration.NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }

    private static int lighten(int color) {
        int red = Math.min(255, ((color >> 16) & 0xFF) + 24);
        int green = Math.min(255, ((color >> 8) & 0xFF) + 24);
        int blue = Math.min(255, (color & 0xFF) + 24);

        return 0xFF000000
                | red << 16
                | green << 8
                | blue;
    }
}
