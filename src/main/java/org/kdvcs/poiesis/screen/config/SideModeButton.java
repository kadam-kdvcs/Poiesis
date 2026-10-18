package org.kdvcs.poiesis.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class SideModeButton extends AbstractButton {

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/widget/io_button.png"
            );

    private static final int BUTTON_SIZE = 24;
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
        int color = modeColor(mode());

        if (isHoveredOrFocused()) {
            color = lighten(color);
        }

        float red =
                ((color >> 16) & 0xFF) / 255.0F;

        float green =
                ((color >> 8) & 0xFF) / 255.0F;

        float blue =
                (color & 0xFF) / 255.0F;

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
                BUTTON_SIZE,
                BUTTON_SIZE,
                BUTTON_SIZE,
                BUTTON_SIZE
        );

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        ItemStack icon =
                neighborIconSupplier.get();

        if (!icon.isEmpty()) {

            int iconX =
                    getX()
                            + (BUTTON_SIZE - ICON_SIZE) / 2;

            int iconY =
                    getY()
                            + (BUTTON_SIZE - ICON_SIZE) / 2;

            graphics.renderItem(
                    icon,
                    iconX,
                    iconY
            );
        }
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }

    private static int modeColor(
            SideIoMode mode
    ) {
        return switch (mode) {
            case NONE -> 0xFF4A4A4A;
            case INPUT -> 0xFF3E7CB1;
            case INPUT_ANODE -> 0xFF3E7CB1;
            case INPUT_CATHODE -> 0xFF3E9B86;
            case OUTPUT -> 0xFFB16B3E;
            case INPUT_OUTPUT -> 0xFF6C4AA0;
        };
    }

    private static int lighten(
            int color
    ) {
        int red =
                Math.min(
                        255,
                        ((color >> 16) & 0xFF) + 24
                );

        int green =
                Math.min(
                        255,
                        ((color >> 8) & 0xFF) + 24
                );

        int blue =
                Math.min(
                        255,
                        (color & 0xFF) + 24
                );

        return 0xFF000000
                | red << 16
                | green << 8
                | blue;
    }
}
