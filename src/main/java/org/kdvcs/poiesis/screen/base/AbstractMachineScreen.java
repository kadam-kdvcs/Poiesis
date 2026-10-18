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
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.widget.IoConfigButton;

public abstract class AbstractMachineScreen<M extends AbstractMachineMenu>
        extends AbstractContainerScreen<M> {

    private static final ResourceLocation ENERGY_BAR_BASE =
            containerTexture("widget/energy_bar_base");

    private static final ResourceLocation BATTERY_SLOT =
            containerTexture("widget/battery_slot");

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

    /**
     * Draws the energy bar frame as a screen widget.  The fill remains a
     * machine-specific element, but the frame is deliberately kept out of
     * every background texture so all machines share the same presentation.
     */
    protected final void drawEnergyBarFrame(
            GuiGraphics graphics,
            int energyX,
            int energyBottomY
    ) {
        graphics.blit(
                ENERGY_BAR_BASE,
                leftPos + energyX - 1,
                topPos + energyBottomY - 46,
                0,
                0,
                14,
                48,
                14,
                48
        );
    }

    /** Draws the standalone battery slot overlay behind the slot item. */
    protected final void drawBatterySlot(
            GuiGraphics graphics,
            int batteryX,
            int batteryY
    ) {
        graphics.blit(
                BATTERY_SLOT,
                leftPos + batteryX - 1,
                topPos + batteryY - 1,
                0,
                0,
                18,
                18,
                18,
                18
        );
    }

    /**
     * Draws a layered machine progress widget:
     * static base first, then a left-to-right clipped foreground layer.
     */
    protected final void drawProgressIndicator(
            GuiGraphics graphics,
            ProgressIndicatorLayout layout,
            int value,
            int maximum
    ) {
        if (layout == null) {
            return;
        }

        ProgressIndicatorLayout.TextureSpec baseTexture =
                layout.baseTexture();

        ProgressIndicatorLayout.SpriteRegion base =
                layout.base();

        graphics.blit(
                baseTexture.location(),
                leftPos + layout.x(),
                topPos + layout.y(),
                base.u(),
                base.v(),
                base.width(),
                base.height(),
                baseTexture.width(),
                baseTexture.height()
        );

        ProgressIndicatorLayout.SpriteRegion progress =
                layout.progress();

        int visibleWidth = scale(
                value,
                maximum,
                progress.width()
        );

        if (visibleWidth <= 0) {
            return;
        }

        ProgressIndicatorLayout.TextureSpec progressTexture =
                layout.progressTexture();

        graphics.blit(
                progressTexture.location(),
                leftPos + layout.x(),
                topPos + layout.y(),
                progress.u(),
                progress.v(),
                visibleWidth,
                progress.height(),
                progressTexture.width(),
                progressTexture.height()
        );
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
