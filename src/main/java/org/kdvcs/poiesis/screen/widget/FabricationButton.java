package org.kdvcs.poiesis.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class FabricationButton extends AbstractButton {
    public static final int WIDTH = 20;
    public static final int HEIGHT = 14;

    private static final int TEXTURE_WIDTH = 20;
    private static final int TEXTURE_HEIGHT = 42;
    private static final int SELECTED_V = 14;
    private static final int PRESSED_V = 28;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/fabrication.png"
            );

    private final Runnable pressAction;
    private boolean selected;
    private boolean pressed;
    private boolean processing;

    public FabricationButton(
            int x,
            int y,
            Runnable pressAction
    ) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.pressAction = pressAction;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
        if (!processing) {
            this.pressed = false;
        }
    }

    @Override
    public void onPress() {
        pressAction.run();
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        pressed = true;
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        pressed = false;
        super.onRelease(mouseX, mouseY);
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int v = pressed || processing ? PRESSED_V : selected ? SELECTED_V : 0;

        graphics.blit(
                TEXTURE,
                getX(),
                getY(),
                0,
                v,
                WIDTH,
                HEIGHT,
                TEXTURE_WIDTH,
                TEXTURE_HEIGHT
        );
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }
}
