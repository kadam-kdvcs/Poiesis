package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.CompounderGuiLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class CompounderScreen
        extends AbstractMachineScreen<CompounderMenu> {

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 172;

    private static final int TITLE_Y = 6;
    private static final int INVENTORY_LABEL_Y = 79;

    private static final int ENERGY_X = 10;
    private static final int ENERGY_BOTTOM_Y = 55;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;


    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private static final ResourceLocation BACKGROUND =
            containerTexture("compounder");


    private static final ResourceLocation ENERGY_TEXTURE =
            containerTexture(
                    "widget/energy_bar"
            );

    public CompounderScreen(
            CompounderMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title,
                GUI_WIDTH,
                GUI_HEIGHT
        );
    }

    @Override
    protected void init() {
        super.init();

        titleLabelX =
                (imageWidth - font.width(title)) / 2;

        titleLabelY = TITLE_Y;
        inventoryLabelY = INVENTORY_LABEL_Y;
    }

    @Override
    protected ResourceLocation backgroundTexture() {
        return BACKGROUND;
    }

    @Override
    protected void renderMachineContents(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        renderProgress(graphics);
        renderEnergy(graphics);
    }

    private void renderProgress(
            GuiGraphics graphics
    ) {
        drawProgressIndicator(
                graphics,
                CompounderGuiLayout.progressVisual(
                        menu.highShearEnabled()
                ),
                menu.progress(),
                menu.maxProgress()
        );
    }

    public boolean highShearEnabled() {
        return menu.highShearEnabled();
    }

    private void renderEnergy(
            GuiGraphics graphics
    ) {
        int visibleHeight = scale(
                menu.energy(),
                menu.capacity(),
                ENERGY_HEIGHT
        );

        if (visibleHeight <= 0) {
            return;
        }

        int sourceY =
                ENERGY_HEIGHT - visibleHeight;

        int destinationY =
                ENERGY_BOTTOM_Y - visibleHeight;

        graphics.blit(
                ENERGY_TEXTURE,
                leftPos + ENERGY_X,
                topPos + destinationY,
                0,
                sourceY,
                ENERGY_WIDTH,
                visibleHeight,
                ENERGY_WIDTH,
                ENERGY_HEIGHT
        );
    }

    @Override
    protected void renderTooltip(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        super.renderTooltip(
                graphics,
                mouseX,
                mouseY
        );

        if (isHovering(
                ENERGY_X,
                ENERGY_BOTTOM_Y - ENERGY_HEIGHT,
                ENERGY_WIDTH,
                ENERGY_HEIGHT,
                mouseX,
                mouseY
        )) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                    "gui.poiesis.energy",
                                    formatNumber(menu.energy()),
                                    formatNumber(menu.capacity())
                            ),
                            Component.translatable(
                                    "gui.poiesis.power",
                                    formatNumber(
                                            menu.activeEnergyPerTick()
                                    )
                            )
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private static String formatNumber(
            int value
    ) {
        return NUMBER_FORMAT.format(
                Math.max(0, value)
        );
    }

    @Override
    protected int ioButtonX() {
        return 162;
    }

    @Override
    protected int ioButtonY() {
        return 4;
    }
}
