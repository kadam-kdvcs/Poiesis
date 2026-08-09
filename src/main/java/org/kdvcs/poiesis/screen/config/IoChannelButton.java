package org.kdvcs.poiesis.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.kdvcs.poiesis.machine.io.IoChannel;

import java.util.function.Consumer;

public final class IoChannelButton extends AbstractButton {

    private static final ResourceLocation WIDGETS =
            new ResourceLocation(
                    "minecraft",
                    "textures/gui/widgets.png"
            );

    private static final int SIZE = 18;
    private static final int ICON_SIZE = 16;

    private final IoChannel channel;
    private final Consumer<IoChannel> clickHandler;

    public IoChannelButton(
            int x,
            int y,
            IoChannel channel,
            Consumer<IoChannel> clickHandler
    ) {
        super(
                x,
                y,
                SIZE,
                SIZE,
                Component.translatable(
                        "gui.poiesis.io_channel."
                                + channel.name().toLowerCase()
                )
        );

        this.channel = channel;
        this.clickHandler = clickHandler;
    }

    public IoChannel channel() {
        return channel;
    }

    @Override
    public void onPress() {
        if (!active) {
            return;
        }

        clickHandler.accept(channel);
    }

    @Override
    protected void renderWidget(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        int v;

        if (!active) {
            v = 46;
        } else if (isHoveredOrFocused()) {
            v = 86;
        } else {
            v = 66;
        }

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );

        graphics.blit(
                WIDGETS,
                getX(),
                getY(),
                0,
                v,
                SIZE / 2,
                SIZE,
                256,
                256
        );

        graphics.blit(
                WIDGETS,
                getX() + SIZE / 2,
                getY(),
                200 - SIZE / 2,
                v,
                SIZE / 2,
                SIZE,
                256,
                256
        );

        ItemStack icon = icon();

        if (icon.isEmpty()) {
            return;
        }

        if (!active) {
            RenderSystem.setShaderColor(
                    0.45F,
                    0.45F,
                    0.45F,
                    1.0F
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

        RenderSystem.setShaderColor(
                1.0F,
                1.0F,
                1.0F,
                1.0F
        );
    }

    private ItemStack icon() {
        return switch (channel) {
            case ITEM ->
                    new ItemStack(
                            Items.IRON_INGOT
                    );

            case ENERGY ->
                    new ItemStack(
                            Items.REDSTONE
                    );

            case FLUID ->
                    new ItemStack(
                            Items.WATER_BUCKET
                    );
        };
    }

    @Override
    protected void updateWidgetNarration(
            NarrationElementOutput output
    ) {
        defaultButtonNarrationText(output);
    }
}