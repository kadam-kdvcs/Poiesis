package org.kdvcs.poiesis.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class SwapFluidsButton extends AbstractButton {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 5;

    private static final ResourceLocation TEXTURE = new ResourceLocation(
            Poiesis.MOD_ID, "textures/container/widget/swap.png"
    );
    private final Runnable pressAction;

    public SwapFluidsButton(int x, int y, Runnable pressAction) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.pressAction = pressAction;
    }

    @Override public void onPress() { pressAction.run(); }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.blit(TEXTURE, getX(), getY(), 0, 0, WIDTH, HEIGHT, WIDTH, HEIGHT);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
