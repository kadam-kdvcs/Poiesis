package org.kdvcs.poiesis.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.GreenhouseFluidInteractPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.GreenhouseGuiLayout;
import org.kdvcs.poiesis.screen.widget.FluidTankWidget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class GreenhouseScreen extends AbstractMachineScreen<GreenhouseMenu> {
    private static final int GUI_WIDTH = 256;
    private static final int GUI_HEIGHT = 179;
    private static final int ENERGY_X = 10;
    private static final int ENERGY_BOTTOM_Y = 62;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;

    private static final ResourceLocation BACKGROUND = containerTexture("greenhouse");
    private static final ResourceLocation ENERGY_TEXTURE = containerTexture("widget/energy_bar");
    private static final ResourceLocation FLUID_OVERLAY = containerTexture("widget/fluid_slot");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public GreenhouseScreen(GreenhouseMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void configureLabels() {
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelX = GreenhouseGuiLayout.PLAYER_INVENTORY_X;
        inventoryLabelY = GreenhouseGuiLayout.PLAYER_INVENTORY_Y - 11;
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
        drawBatterySlot(graphics, GreenhouseGuiLayout.BATTERY_X, GreenhouseGuiLayout.BATTERY_Y);
        drawProgressIndicator(
                graphics,
                GreenhouseGuiLayout.PROGRESS,
                menu.progress(),
                menu.maxProgress()
        );
        renderEnergy(graphics);
        renderFluidSlot(graphics, menu.waterStack(),
                GreenhouseGuiLayout.WATER_X, GreenhouseGuiLayout.WATER_Y);
        renderFluidSlot(graphics, menu.nutrientFluidStack(),
                GreenhouseGuiLayout.NUTRIENT_FLUID_X, GreenhouseGuiLayout.NUTRIENT_FLUID_Y);
        renderFluidSlot(graphics, menu.outputFluidStack(),
                GreenhouseGuiLayout.FLUID_OUTPUT_X, GreenhouseGuiLayout.FLUID_OUTPUT_Y);
    }

    private void renderEnergy(GuiGraphics graphics) {
        int visibleHeight = scale(menu.energy(), menu.energyCapacity(), ENERGY_HEIGHT);
        if (visibleHeight <= 0) {
            return;
        }
        graphics.blit(
                ENERGY_TEXTURE,
                leftPos + ENERGY_X,
                topPos + ENERGY_BOTTOM_Y - visibleHeight,
                0,
                ENERGY_HEIGHT - visibleHeight,
                ENERGY_WIDTH,
                visibleHeight,
                ENERGY_WIDTH,
                ENERGY_HEIGHT
        );
    }

    private void renderFluidSlot(GuiGraphics graphics, FluidStack stack, int x, int y) {
        FluidTankWidget.render(
                graphics, leftPos, topPos, x, y,
                GreenhouseGuiLayout.FLUID_SIZE, GreenhouseGuiLayout.FLUID_SIZE,
                stack, FLUID_OVERLAY
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            GreenhouseFluidInteractPacket.Tank tank = hoveredTank(mouseX, mouseY);
            if (tank != null) {
                ModNetwork.sendToServer(new GreenhouseFluidInteractPacket(menu.machinePos(), tank));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        GreenhouseFluidInteractPacket.Tank tank = hoveredTank(mouseX, mouseY);
        if (tank != null) {
            switch (tank) {
                case WATER -> renderFluidTooltip(
                        graphics, menu.waterStack(), menu.waterAmount(), menu.waterCapacity(), mouseX, mouseY
                );
                case NUTRIENT -> renderFluidTooltip(
                        graphics, menu.nutrientFluidStack(), menu.nutrientFluidAmount(),
                        menu.nutrientFluidCapacity(), mouseX, mouseY
                );
                case OUTPUT -> renderFluidTooltip(
                        graphics, menu.outputFluidStack(), menu.outputFluidAmount(),
                        menu.outputFluidCapacity(), mouseX, mouseY
                );
            }
        } else if (isHovering(
                ENERGY_X,
                ENERGY_BOTTOM_Y - ENERGY_HEIGHT,
                ENERGY_WIDTH,
                ENERGY_HEIGHT,
                mouseX,
                mouseY
        )) {
            graphics.renderComponentTooltip(font, List.of(
                    Component.translatable(
                            "gui.poiesis.energy",
                            format(menu.energy()),
                            format(menu.energyCapacity())
                    ),
                    Component.translatable(
                            "gui.poiesis.power",
                            format(menu.activeEnergyPerTick())
                    )
            ), mouseX, mouseY);
        }
    }

    private void renderFluidTooltip(
            GuiGraphics graphics,
            FluidStack fluid,
            int amount,
            int capacity,
            int mouseX,
            int mouseY
    ) {
        FluidTankWidget.renderTooltip(
                graphics, font, fluid, amount, capacity, mouseX, mouseY
        );
    }

    private GreenhouseFluidInteractPacket.Tank hoveredTank(double mouseX, double mouseY) {
        if (isHoveringFluid(GreenhouseGuiLayout.WATER_X, GreenhouseGuiLayout.WATER_Y, mouseX, mouseY)) {
            return GreenhouseFluidInteractPacket.Tank.WATER;
        }
        if (isHoveringFluid(
                GreenhouseGuiLayout.NUTRIENT_FLUID_X,
                GreenhouseGuiLayout.NUTRIENT_FLUID_Y,
                mouseX,
                mouseY
        )) {
            return GreenhouseFluidInteractPacket.Tank.NUTRIENT;
        }
        if (isHoveringFluid(
                GreenhouseGuiLayout.FLUID_OUTPUT_X,
                GreenhouseGuiLayout.FLUID_OUTPUT_Y,
                mouseX,
                mouseY
        )) {
            return GreenhouseFluidInteractPacket.Tank.OUTPUT;
        }
        return null;
    }

    private boolean isHoveringFluid(int x, int y, double mouseX, double mouseY) {
        return FluidTankWidget.isHovered(
                leftPos, topPos, x, y,
                GreenhouseGuiLayout.FLUID_SIZE, GreenhouseGuiLayout.FLUID_SIZE,
                mouseX, mouseY
        );
    }

    private static String format(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
    }
}
