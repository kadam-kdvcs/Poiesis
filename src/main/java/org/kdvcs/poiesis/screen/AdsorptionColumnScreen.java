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
import org.kdvcs.poiesis.network.packet.AdsorptionColumnFluidInteractPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.AdsorptionColumnGuiLayout;
import org.kdvcs.poiesis.screen.widget.FluidTankWidget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class AdsorptionColumnScreen extends AbstractMachineScreen<AdsorptionColumnMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 172;
    private static final int ENERGY_X = 10;
    private static final int ENERGY_BOTTOM_Y = 58;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private static final ResourceLocation BACKGROUND = containerTexture("adsorption_column");
    private static final ResourceLocation ENERGY_TEXTURE = containerTexture("widget/energy_bar");
    private static final ResourceLocation FLUID_OVERLAY = containerTexture("widget/fluid_slot");

    public AdsorptionColumnScreen(
            AdsorptionColumnMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title, GUI_WIDTH, GUI_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        titleLabelY = 6;
        inventoryLabelY = 82;
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
        drawBatterySlot(graphics, AdsorptionColumnGuiLayout.BATTERY_X, AdsorptionColumnGuiLayout.BATTERY_Y);
        renderProgress(graphics);
        renderEnergy(graphics);
        renderFluidSlot(
                graphics,
                menu.inputFluidStack(),
                AdsorptionColumnGuiLayout.FLUID_INPUT_X,
                AdsorptionColumnGuiLayout.FLUID_INPUT_Y
        );
        renderFluidSlot(
                graphics,
                menu.outputFluidStack(),
                AdsorptionColumnGuiLayout.FLUID_OUTPUT_X,
                AdsorptionColumnGuiLayout.FLUID_OUTPUT_Y
        );
    }

    private void renderProgress(GuiGraphics graphics) {
        drawProgressIndicator(
                graphics,
                AdsorptionColumnGuiLayout.PROGRESS,
                menu.progress(),
                menu.maxProgress()
        );
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

    private void renderFluidSlot(
            GuiGraphics graphics,
            FluidStack stack,
            int x,
            int y
    ) {
        FluidTankWidget.render(
                graphics, leftPos, topPos, x, y,
                AdsorptionColumnGuiLayout.FLUID_SIZE,
                AdsorptionColumnGuiLayout.FLUID_SIZE,
                stack, FLUID_OVERLAY
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            if (isHoveringFluid(
                    AdsorptionColumnGuiLayout.FLUID_INPUT_X,
                    AdsorptionColumnGuiLayout.FLUID_INPUT_Y,
                    mouseX,
                    mouseY
            )) {
                ModNetwork.sendToServer(new AdsorptionColumnFluidInteractPacket(
                        menu.machinePos(),
                        false
                ));
                return true;
            }
            if (isHoveringFluid(
                    AdsorptionColumnGuiLayout.FLUID_OUTPUT_X,
                    AdsorptionColumnGuiLayout.FLUID_OUTPUT_Y,
                    mouseX,
                    mouseY
            )) {
                ModNetwork.sendToServer(new AdsorptionColumnFluidInteractPacket(
                        menu.machinePos(),
                        true
                ));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);
        if (isHoveringFluid(
                AdsorptionColumnGuiLayout.FLUID_INPUT_X,
                AdsorptionColumnGuiLayout.FLUID_INPUT_Y,
                mouseX,
                mouseY
        )) {
            renderFluidTooltip(
                    graphics,
                    menu.inputFluidStack(),
                    menu.inputFluidAmount(),
                    menu.inputFluidCapacity(),
                    mouseX,
                    mouseY
            );
        } else if (isHoveringFluid(
                AdsorptionColumnGuiLayout.FLUID_OUTPUT_X,
                AdsorptionColumnGuiLayout.FLUID_OUTPUT_Y,
                mouseX,
                mouseY
        )) {
            renderFluidTooltip(
                    graphics,
                    menu.outputFluidStack(),
                    menu.outputFluidAmount(),
                    menu.outputFluidCapacity(),
                    mouseX,
                    mouseY
            );
        } else if (isHovering(
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
                                    format(menu.energy()),
                                    format(menu.energyCapacity())
                            ),
                            Component.translatable(
                                    "gui.poiesis.power",
                                    format(menu.activeEnergyPerTick())
                            )
                    ),
                    mouseX,
                    mouseY
            );
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

    private boolean isHoveringFluid(int x, int y, double mouseX, double mouseY) {
        return FluidTankWidget.isHovered(
                leftPos, topPos, x, y,
                AdsorptionColumnGuiLayout.FLUID_SIZE,
                AdsorptionColumnGuiLayout.FLUID_SIZE,
                mouseX, mouseY
        );
    }

    private static String format(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
    }
}
