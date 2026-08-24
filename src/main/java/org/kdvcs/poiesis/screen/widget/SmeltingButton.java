package org.kdvcs.poiesis.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class SmeltingButton extends AbstractButton {
    public static final int WIDTH = 14;
    public static final int HEIGHT = 14;

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Poiesis.MOD_ID,
            "textures/container/widget/smelting.png"
    );

    private final Runnable pressAction;
    private boolean selected;
    private boolean pressed;
    private boolean processing;

    public SmeltingButton(int x, int y, Runnable pressAction) {
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

    @Override public void onPress() { pressAction.run(); }

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
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int v = pressed || processing ? HEIGHT * 2 : selected ? HEIGHT : 0;
        graphics.blit(
                TEXTURE,
                getX(),
                getY(),
                0,
                v,
                WIDTH,
                HEIGHT,
                WIDTH,
                HEIGHT * 3
        );
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
