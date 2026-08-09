package org.kdvcs.poiesis.screen.base;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.OpenMachineConfigPacket;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.widget.IoConfigButton;

public abstract class AbstractMachineScreen<M extends AbstractMachineMenu>
        extends AbstractContainerScreen<M> {

    private static final int IO_BUTTON_SIZE = 10;
    private static final int IO_BUTTON_RIGHT_MARGIN = 6;
    private static final int IO_BUTTON_TOP_MARGIN = 6;

    protected AbstractMachineScreen(
            M menu,
            Inventory inventory,
            Component title,
            int imageWidth,
            int imageHeight
    ) {
        super(menu, inventory, title);

        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    protected int ioButtonX() {
        return imageWidth - IO_BUTTON_SIZE - IO_BUTTON_RIGHT_MARGIN;
    }

    protected int ioButtonY() {
        return IO_BUTTON_TOP_MARGIN;
    }

    protected void configureLabels() {
    }

    @Override
    protected void init() {
        super.init();

        configureLabels();

        if (!menu.hasSideConfiguration()) {
            return;
        }

        IoConfigButton configButton = new IoConfigButton(
                leftPos + ioButtonX(),
                topPos + ioButtonY(),
                () -> ModNetwork.sendToServer(
                        new OpenMachineConfigPacket(
                                menu.machinePos()
                        )
                )
        );

        configButton.setTooltip(
                Tooltip.create(
                        Component.translatable(
                                "gui.poiesis.open_side_config"
                        )
                )
        );

        addRenderableWidget(configButton);
    }

    protected static ResourceLocation containerTexture(
            String name
    ) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "textures/container/" + name + ".png"
        );
    }

    protected abstract ResourceLocation backgroundTexture();

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        ResourceLocation texture = backgroundTexture();

        RenderSystem.setShaderTexture(0, texture);

        graphics.blit(
                texture,
                leftPos,
                topPos,
                0,
                0,
                imageWidth,
                imageHeight
        );

        renderMachineContents(
                graphics,
                partialTick,
                mouseX,
                mouseY
        );
    }

    protected void renderMachineContents(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
    }

    protected final void drawHorizontalBar(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int value,
            int maximum,
            int color
    ) {
        int scaledWidth = scale(
                value,
                maximum,
                width
        );

        if (scaledWidth <= 0) {
            return;
        }

        graphics.fill(
                leftPos + x,
                topPos + y,
                leftPos + x + scaledWidth,
                topPos + y + height,
                color
        );
    }

    protected final void drawVerticalBar(
            GuiGraphics graphics,
            int x,
            int bottomY,
            int width,
            int height,
            int value,
            int maximum,
            int color
    ) {
        int scaledHeight = scale(
                value,
                maximum,
                height
        );

        if (scaledHeight <= 0) {
            return;
        }

        graphics.fill(
                leftPos + x,
                topPos + bottomY - scaledHeight,
                leftPos + x + width,
                topPos + bottomY,
                color
        );
    }

    protected final int scale(
            int value,
            int maximum,
            int size
    ) {
        if (value <= 0 || maximum <= 0 || size <= 0) {
            return 0;
        }

        return Math.min(
                size,
                value * size / maximum
        );
    }

    @Override
    public final void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderTooltip(
                graphics,
                mouseX,
                mouseY
        );
    }
}