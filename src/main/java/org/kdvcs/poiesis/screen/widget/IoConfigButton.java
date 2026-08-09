package org.kdvcs.poiesis.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;

public final class IoConfigButton extends AbstractButton {

    private static final int SIZE = 10;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/io_config.png"
            );

    private static final int NORMAL_COLOR = 0x6FA8DC;
    private static final int HOVER_COLOR = 0x9CCBFF;

    private final Runnable pressAction;

    public IoConfigButton(
            int x,
            int y,
            Runnable pressAction
    ) {
        super(
                x,
                y,
                SIZE,
                SIZE,
                Component.empty()
        );

        this.pressAction = pressAction;
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
        int color = isHoveredOrFocused()
                ? HOVER_COLOR
                : NORMAL_COLOR;

        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        RenderSystem.setShaderColor(
                red,
                green,
                blue,
                1.0F
        );

        graphics.blit(
                TEXTURE,
                getX(),
                getY(),
                0,
                0,
                SIZE,
                SIZE,
                SIZE,
                SIZE
        );

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }
}