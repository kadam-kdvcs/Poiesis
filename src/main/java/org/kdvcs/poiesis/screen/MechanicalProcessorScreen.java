package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.MechanicalProcessorGuiLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class MechanicalProcessorScreen
        extends AbstractMachineScreen<MechanicalProcessorMenu> {

        private static final int GUI_WIDTH = 176;
        private static final int GUI_HEIGHT = 172;

        private static final int TITLE_Y = 6;
        private static final int INVENTORY_LABEL_Y = 79;

        private static final int ENERGY_X = 10;
        private static final int ENERGY_BOTTOM_Y = 55;
        private static final int ENERGY_WIDTH = 12;
        private static final int ENERGY_HEIGHT = 45;

        private static final int SPECIALIZATION_HINT_X = 80;
        private static final int SPECIALIZATION_HINT_Y = 34;
        private static final int SPECIALIZATION_HINT_WIDTH = 16;
        private static final int SPECIALIZATION_HINT_HEIGHT = 16;

        private static final NumberFormat NUMBER_FORMAT =
                NumberFormat.getIntegerInstance(Locale.US);

        private static final ResourceLocation BACKGROUND =
                containerTexture("mechanical_processor");

        private static final ResourceLocation ENERGY_TEXTURE =
                containerTexture(
                        "widget/energy_bar"
                );

        private static final ResourceLocation EMPTY_SPECIALIZATION_TEXTURE =
                containerTexture(
                        "widget/empty"
                );

        public MechanicalProcessorScreen(
                MechanicalProcessorMenu menu,
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

        public MechanicalMode mode() {
                return menu.mode();
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
                drawEnergyBarFrame(graphics, ENERGY_X, ENERGY_BOTTOM_Y - 1);
                drawBatterySlot(graphics, 8, 59);
                renderProgress(graphics);
                renderEnergy(graphics);
        }

        private void renderProgress(
                GuiGraphics graphics
        ) {
                if (menu.mode() == MechanicalMode.NONE) {
                        graphics.blit(
                                EMPTY_SPECIALIZATION_TEXTURE,
                                leftPos + SPECIALIZATION_HINT_X,
                                topPos + SPECIALIZATION_HINT_Y,
                                0,
                                0,
                                SPECIALIZATION_HINT_WIDTH,
                                SPECIALIZATION_HINT_HEIGHT,
                                SPECIALIZATION_HINT_WIDTH,
                                SPECIALIZATION_HINT_HEIGHT
                        );

                        return;
                }

                drawProgressIndicator(
                        graphics,
                        MechanicalProcessorGuiLayout.progressVisual(
                                menu.mode()
                        ),
                        menu.progress(),
                        menu.maxProgress()
                );
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

                if (isHoveringEnergyBar(mouseX, mouseY)) {
                        renderEnergyTooltip(
                                graphics,
                                mouseX,
                                mouseY
                        );

                        return;
                }

                if (menu.mode() == MechanicalMode.NONE
                        && isHoveringSpecializationHint(
                        mouseX,
                        mouseY
                )) {
                        renderSpecializationHintTooltip(
                                graphics,
                                mouseX,
                                mouseY
                        );
                }
        }

        private void renderEnergyTooltip(
                GuiGraphics graphics,
                int mouseX,
                int mouseY
        ) {
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

        private void renderSpecializationHintTooltip(
                GuiGraphics graphics,
                int mouseX,
                int mouseY
        ) {
                graphics.renderTooltip(
                        font,
                        Component.translatable(
                                "gui.poiesis.mechanical_processor.specialization_hint"
                        ),
                        mouseX,
                        mouseY
                );
        }

        private boolean isHoveringEnergyBar(
                int mouseX,
                int mouseY
        ) {
                return isHovering(
                        ENERGY_X,
                        ENERGY_BOTTOM_Y - ENERGY_HEIGHT,
                        ENERGY_WIDTH,
                        ENERGY_HEIGHT,
                        mouseX,
                        mouseY
                );
        }

        private boolean isHoveringSpecializationHint(
                int mouseX,
                int mouseY
        ) {
                return isHovering(
                        SPECIALIZATION_HINT_X,
                        SPECIALIZATION_HINT_Y,
                        SPECIALIZATION_HINT_WIDTH,
                        SPECIALIZATION_HINT_HEIGHT,
                        mouseX,
                        mouseY
                );
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
