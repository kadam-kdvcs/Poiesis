package org.kdvcs.poiesis.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class AutoCraftToggleButton extends AbstractButton {
    private static final int WIDTH = 16;
    private static final int HEIGHT = 8;

    private static final int TEXTURE_WIDTH = 16;
    private static final int TEXTURE_HEIGHT = 16;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/autocraft_button.png"
            );

    private final Runnable pressAction;
    private boolean toggled;

    public AutoCraftToggleButton(
            int x,
            int y,
            boolean toggled,
            Runnable pressAction
    ) {
        super(
                x,
                y,
                WIDTH,
                HEIGHT,
                Component.empty()
        );
        this.toggled = toggled;
        this.pressAction = pressAction;
    }

    public void setToggled(boolean toggled) {
        this.toggled = toggled;
    }

    public boolean toggled() {
        return toggled;
    }

    @Override
    public void onPress() {
        pressAction.run();
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int v = toggled ? 8 : 0;

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

        if (!active) {
            graphics.fill(
                    getX(),
                    getY(),
                    getX() + width,
                    getY() + height,
                    0x88000000
            );
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }
}