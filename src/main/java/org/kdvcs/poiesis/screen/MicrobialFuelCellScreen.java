package org.kdvcs.poiesis.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.MicrobialFuelCellFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.MicrobialFuelCellSwapPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.MicrobialFuelCellGuiLayout;
import org.kdvcs.poiesis.screen.widget.FluidTankWidget;
import org.kdvcs.poiesis.screen.widget.SwapFluidsButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class MicrobialFuelCellScreen
        extends AbstractMachineScreen<MicrobialFuelCellMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 180;
    private static final ResourceLocation BACKGROUND = containerTexture("microbial_fuel_cell");
    private static final ResourceLocation ENERGY_FRAME =
            containerTexture("widget/generator_energy_bar_base");
    private static final ResourceLocation ENERGY_BAR =
            containerTexture("widget/generator_energy_bar");
    private static final ResourceLocation TANK_OVERLAY =
            containerTexture("widget/mfc_tank_overlay");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public MicrobialFuelCellScreen(
            MicrobialFuelCellMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SwapFluidsButton(
                leftPos + MicrobialFuelCellGuiLayout.SWAP_X,
                topPos + MicrobialFuelCellGuiLayout.SWAP_Y,
                () -> ModNetwork.sendToServer(new MicrobialFuelCellSwapPacket(menu.machinePos()))
        ));
    }

    @Override
    protected void configureLabels() {
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = MicrobialFuelCellGuiLayout.PLAYER_INVENTORY_X;
        inventoryLabelY = MicrobialFuelCellGuiLayout.PLAYER_INVENTORY_Y - 11;
    }

    @Override protected ResourceLocation backgroundTexture() { return BACKGROUND; }

    @Override
    protected void renderMachineContents(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        renderEnergyFrame(graphics);
        drawBatterySlot(
                graphics,
                MicrobialFuelCellGuiLayout.BATTERY_X,
                MicrobialFuelCellGuiLayout.BATTERY_Y
        );
        renderEnergy(graphics);
        drawProgressIndicator(
                graphics,
                MicrobialFuelCellGuiLayout.PROGRESS,
                menu.progress(),
                menu.maxProgress()
        );
        renderTank(
                graphics, menu.anolyteStack(),
                MicrobialFuelCellGuiLayout.ANOLYTE_X,
                MicrobialFuelCellGuiLayout.ANOLYTE_Y,
                MicrobialFuelCellGuiLayout.ANOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.ANOLYTE_HEIGHT,
                0
        );
        renderTank(
                graphics, menu.catholyteStack(),
                MicrobialFuelCellGuiLayout.CATHOLYTE_X,
                MicrobialFuelCellGuiLayout.CATHOLYTE_Y,
                MicrobialFuelCellGuiLayout.CATHOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.CATHOLYTE_HEIGHT,
                35
        );
    }

    private void renderEnergyFrame(GuiGraphics graphics) {
        graphics.blit(
                ENERGY_FRAME,
                leftPos + MicrobialFuelCellGuiLayout.ENERGY_X - 1,
                topPos + MicrobialFuelCellGuiLayout.ENERGY_BOTTOM_Y - 46,
                0, 0, 14, 48, 14, 48
        );
    }

    private void renderEnergy(GuiGraphics graphics) {
        int visibleHeight = scale(
                menu.energy(), menu.energyCapacity(),
                MicrobialFuelCellGuiLayout.ENERGY_HEIGHT
        );
        if (visibleHeight <= 0) return;
        int sourceY = MicrobialFuelCellGuiLayout.ENERGY_HEIGHT - visibleHeight;
        graphics.blit(
                ENERGY_BAR,
                leftPos + MicrobialFuelCellGuiLayout.ENERGY_X,
                topPos + MicrobialFuelCellGuiLayout.ENERGY_BOTTOM_Y - visibleHeight + 1,
                0, sourceY,
                MicrobialFuelCellGuiLayout.ENERGY_WIDTH, visibleHeight,
                MicrobialFuelCellGuiLayout.ENERGY_WIDTH,
                MicrobialFuelCellGuiLayout.ENERGY_HEIGHT
        );
    }

    private void renderTank(
            GuiGraphics graphics,
            FluidStack fluid,
            int x,
            int y,
            int width,
            int height,
            int overlayV
    ) {
        FluidTankWidget.renderTiled(graphics, leftPos, topPos, x, y, width, height, fluid, null);
        graphics.blit(
                TANK_OVERLAY,
                leftPos + x,
                topPos + y,
                0,
                overlayV,
                width,
                height,
                44,
                70
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            var tank = hoveredTank(mouseX, mouseY);
            if (tank != null) {
                ModNetwork.sendToServer(
                        new MicrobialFuelCellFluidInteractPacket(menu.machinePos(), tank)
                );
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        var tank = hoveredTank(mouseX, mouseY);
        if (tank == MicrobialFuelCellFluidInteractPacket.Tank.ANOLYTE) {
            renderMfcTankTooltip(
                    graphics,
                    "gui.poiesis.mfc.anolyte_chamber",
                    menu.anolyteStack(),
                    menu.anolyteAmount(),
                    menu.anolyteCapacity(),
                    mouseX,
                    mouseY
            );
        } else if (tank == MicrobialFuelCellFluidInteractPacket.Tank.CATHOLYTE) {
            renderMfcTankTooltip(
                    graphics,
                    "gui.poiesis.mfc.catholyte_chamber",
                    menu.catholyteStack(),
                    menu.catholyteAmount(),
                    menu.catholyteCapacity(),
                    mouseX,
                    mouseY
            );
        } else if (isHovering(
                MicrobialFuelCellGuiLayout.ENERGY_X,
                MicrobialFuelCellGuiLayout.ENERGY_BOTTOM_Y
                        - MicrobialFuelCellGuiLayout.ENERGY_HEIGHT,
                MicrobialFuelCellGuiLayout.ENERGY_WIDTH,
                MicrobialFuelCellGuiLayout.ENERGY_HEIGHT,
                mouseX, mouseY
        )) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(
                            "gui.poiesis.energy",
                            format(menu.energy()), format(menu.energyCapacity())
                    ),
                    Component.translatable(
                            "gui.poiesis.generator.generation",
                            format(menu.generationPerTick())
                    ),
                    Component.translatable(
                            "gui.poiesis.generator.output_power",
                            format(menu.outputPower())
                    )
            ), mouseX, mouseY);
        } else if (isHovering(
                MicrobialFuelCellGuiLayout.SWAP_X,
                MicrobialFuelCellGuiLayout.SWAP_Y,
                SwapFluidsButton.WIDTH,
                SwapFluidsButton.HEIGHT,
                mouseX, mouseY
        )) {
            graphics.renderTooltip(
                    font,
                    Component.translatable("gui.poiesis.mfc.swap_fluids"),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderMfcTankTooltip(
            GuiGraphics graphics,
            String titleKey,
            FluidStack fluid,
            int amount,
            int capacity,
            int mouseX,
            int mouseY
    ) {
        Component name = fluid.isEmpty()
                ? Component.translatable("gui.poiesis.separator.empty_fluid")
                : fluid.getDisplayName();
        graphics.renderComponentTooltip(
                font,
                List.of(
                        Component.translatable(titleKey).withStyle(ChatFormatting.AQUA),
                        name,
                        Component.translatable(
                                "gui.poiesis.separator.fluid_amount",
                                format(amount),
                                format(capacity)
                        )
                ),
                mouseX,
                mouseY
        );
    }

    private MicrobialFuelCellFluidInteractPacket.Tank hoveredTank(
            double mouseX,
            double mouseY
    ) {
        if (isHovered(
                MicrobialFuelCellGuiLayout.ANOLYTE_X,
                MicrobialFuelCellGuiLayout.ANOLYTE_Y,
                MicrobialFuelCellGuiLayout.ANOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.ANOLYTE_HEIGHT,
                mouseX, mouseY
        )) return MicrobialFuelCellFluidInteractPacket.Tank.ANOLYTE;
        if (isHovered(
                MicrobialFuelCellGuiLayout.CATHOLYTE_X,
                MicrobialFuelCellGuiLayout.CATHOLYTE_Y,
                MicrobialFuelCellGuiLayout.CATHOLYTE_WIDTH,
                MicrobialFuelCellGuiLayout.CATHOLYTE_HEIGHT,
                mouseX, mouseY
        )) return MicrobialFuelCellFluidInteractPacket.Tank.CATHOLYTE;
        return null;
    }

    private boolean isHovered(
            int x, int y, int width, int height,
            double mouseX, double mouseY
    ) {
        return FluidTankWidget.isHovered(
                leftPos, topPos, x, y, width, height, mouseX, mouseY
        );
    }

    private static String format(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
    }

    @Override protected int ioButtonX() { return 162; }
    @Override protected int ioButtonY() { return 4; }
}
