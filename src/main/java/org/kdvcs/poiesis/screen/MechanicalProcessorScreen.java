package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;

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

        private static final int PROGRESS_TEXTURE_WIDTH = 22;
        private static final int PROGRESS_TEXTURE_HEIGHT = 48;

        private static final NumberFormat NUMBER_FORMAT =
                NumberFormat.getIntegerInstance(Locale.US);

        private static final ResourceLocation NONE_TEXTURE =
                containerTexture("mechanical_processor_none");

        private static final ResourceLocation PRESSING_TEXTURE =
                containerTexture("mechanical_processor_pressing");

        private static final ResourceLocation GRINDING_TEXTURE =
                containerTexture("mechanical_processor_grinding");

        private static final ResourceLocation EXTRUSION_TEXTURE =
                containerTexture("mechanical_processor_extrusion");

        private static final ResourceLocation PROGRESS_TEXTURE =
                containerTexture(
                        "widget/mechanical_processor_progress_arrows"
                );

        private static final ResourceLocation ENERGY_TEXTURE =
                containerTexture(
                        "widget/energy_bar"
                );

        private static final ProgressSprite PRESSING_PROGRESS =
                new ProgressSprite(
                        78,
                        35,
                        0,
                        0,
                        22,
                        15
                );

        private static final ProgressSprite GRINDING_PROGRESS =
                new ProgressSprite(
                        79,
                        34,
                        0,
                        31,
                        22,
                        17
                );

        private static final ProgressSprite EXTRUSION_PROGRESS =
                new ProgressSprite(
                        79,
                        34,
                        0,
                        15,
                        22,
                        16
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

        @Override
        protected ResourceLocation backgroundTexture() {
                return switch (menu.mode()) {
                        case PRESSING -> PRESSING_TEXTURE;
                        case GRINDING -> GRINDING_TEXTURE;
                        case EXTRUSION -> EXTRUSION_TEXTURE;
                        case NONE -> NONE_TEXTURE;
                };
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
                ProgressSprite sprite =
                        currentProgressSprite();

                if (sprite == null) {
                        return;
                }

                int visibleWidth = scale(
                        menu.progress(),
                        menu.maxProgress(),
                        sprite.width()
                );

                if (visibleWidth <= 0) {
                        return;
                }

                graphics.blit(
                        PROGRESS_TEXTURE,
                        leftPos + sprite.x(),
                        topPos + sprite.y(),
                        sprite.u(),
                        sprite.v(),
                        visibleWidth,
                        sprite.height(),
                        PROGRESS_TEXTURE_WIDTH,
                        PROGRESS_TEXTURE_HEIGHT
                );
        }

        private ProgressSprite currentProgressSprite() {
                return switch (menu.mode()) {
                        case PRESSING -> PRESSING_PROGRESS;
                        case GRINDING -> GRINDING_PROGRESS;
                        case EXTRUSION -> EXTRUSION_PROGRESS;
                        case NONE -> null;
                };
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

        private record ProgressSprite(
                int x,
                int y,
                int u,
                int v,
                int width,
                int height
        ) {
        }
}