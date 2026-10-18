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
import org.kdvcs.poiesis.network.packet.GasGeneratorFluidInteractPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.GasGeneratorGuiLayout;
import org.kdvcs.poiesis.screen.widget.FluidTankWidget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class GasGeneratorScreen extends AbstractMachineScreen<GasGeneratorMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 172;
    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private static final ResourceLocation BACKGROUND = containerTexture("gas_generator");
    private static final ResourceLocation ENERGY_FRAME =
            containerTexture("widget/generator_energy_bar_base");
    private static final ResourceLocation ENERGY_BAR =
            containerTexture("widget/generator_energy_bar");
    private static final ResourceLocation FIRE = containerTexture("widget/fire");
    private static final ResourceLocation FLUID_OVERLAY =
            containerTexture("widget/fluid_slot");

    public GasGeneratorScreen(
            GasGeneratorMenu menu,
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
        inventoryLabelY = 79;
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
        renderEnergyFrame(graphics);
        drawBatterySlot(
                graphics,
                GasGeneratorGuiLayout.BATTERY_X,
                GasGeneratorGuiLayout.BATTERY_Y
        );
        renderEnergy(graphics);
        renderFire(graphics);
        renderFluid(graphics, menu.fluidStack());
        graphics.blit(
                FLUID_OVERLAY,
                leftPos + GasGeneratorGuiLayout.FLUID_X,
                topPos + GasGeneratorGuiLayout.FLUID_Y,
                0,
                0,
                GasGeneratorGuiLayout.FLUID_SIZE,
                GasGeneratorGuiLayout.FLUID_SIZE,
                GasGeneratorGuiLayout.FLUID_SIZE,
                GasGeneratorGuiLayout.FLUID_SIZE
        );
    }

    private void renderEnergyFrame(GuiGraphics graphics) {
        graphics.blit(
                ENERGY_FRAME,
                leftPos + GasGeneratorGuiLayout.ENERGY_X - 1,
                topPos + GasGeneratorGuiLayout.ENERGY_BOTTOM_Y - 46,
                0,
                0,
                14,
                48,
                14,
                48
        );
    }

    private void renderEnergy(GuiGraphics graphics) {
        int visibleHeight = scale(
                menu.energy(),
                menu.capacity(),
                GasGeneratorGuiLayout.ENERGY_HEIGHT
        );
        if (visibleHeight <= 0) {
            return;
        }
        int sourceY = GasGeneratorGuiLayout.ENERGY_HEIGHT - visibleHeight;
        int destinationY = GasGeneratorGuiLayout.ENERGY_BOTTOM_Y - visibleHeight;
        graphics.blit(
                ENERGY_BAR,
                leftPos + GasGeneratorGuiLayout.ENERGY_X,
                topPos + destinationY + 1,
                0,
                sourceY,
                GasGeneratorGuiLayout.ENERGY_WIDTH,
                visibleHeight,
                GasGeneratorGuiLayout.ENERGY_WIDTH,
                GasGeneratorGuiLayout.ENERGY_HEIGHT
        );
    }

    private void renderFire(GuiGraphics graphics) {
        int total = menu.burnTotal();
        if (total <= 0 || menu.burnRemaining() <= 0) {
            return;
        }
        int visibleHeight = Math.max(1, scale(
                menu.burnRemaining(),
                total,
                GasGeneratorGuiLayout.FIRE_SIZE
        ));
        int sourceY = GasGeneratorGuiLayout.FIRE_SIZE - visibleHeight;
        graphics.blit(
                FIRE,
                leftPos + GasGeneratorGuiLayout.FIRE_X,
                topPos + GasGeneratorGuiLayout.FIRE_Y + sourceY,
                0,
                sourceY,
                GasGeneratorGuiLayout.FIRE_SIZE,
                visibleHeight,
                GasGeneratorGuiLayout.FIRE_SIZE,
                GasGeneratorGuiLayout.FIRE_SIZE
        );
    }

    private void renderFluid(GuiGraphics graphics, FluidStack stack) {
        FluidTankWidget.render(
                graphics, leftPos, topPos,
                GasGeneratorGuiLayout.FLUID_X, GasGeneratorGuiLayout.FLUID_Y,
                GasGeneratorGuiLayout.FLUID_SIZE, GasGeneratorGuiLayout.FLUID_SIZE,
                stack, null
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && isHovering(
                GasGeneratorGuiLayout.FLUID_X,
                GasGeneratorGuiLayout.FLUID_Y,
                GasGeneratorGuiLayout.FLUID_SIZE,
                GasGeneratorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            ModNetwork.sendToServer(new GasGeneratorFluidInteractPacket(menu.machinePos()));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(
                GasGeneratorGuiLayout.FLUID_X,
                GasGeneratorGuiLayout.FLUID_Y,
                GasGeneratorGuiLayout.FLUID_SIZE,
                GasGeneratorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            FluidStack fluid = menu.fluidStack();
            Component name = fluid.isEmpty()
                    ? Component.translatable("gui.poiesis.separator.empty_fluid")
                    : fluid.getDisplayName();
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            name,
                            Component.translatable(
                                    "gui.poiesis.separator.fluid_amount",
                                    format(menu.fluidAmount()),
                                    format(menu.fluidCapacity())
                            )
                    ),
                    mouseX,
                    mouseY
            );
        }

        if (isHovering(
                GasGeneratorGuiLayout.ENERGY_X,
                GasGeneratorGuiLayout.ENERGY_BOTTOM_Y
                        - GasGeneratorGuiLayout.ENERGY_HEIGHT,
                GasGeneratorGuiLayout.ENERGY_WIDTH,
                GasGeneratorGuiLayout.ENERGY_HEIGHT,
                mouseX,
                mouseY
        )) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                    "gui.poiesis.energy",
                                    format(menu.energy()),
                                    format(menu.capacity())
                            ),
                            Component.translatable(
                                    "gui.poiesis.generator.generation",
                                    format(menu.generationPerTick())
                            ),
                            Component.translatable(
                                    "gui.poiesis.generator.output_power",
                                    format(menu.outputPower())
                            )
                    ),
                    mouseX,
                    mouseY
            );
        }
    }

    private static String format(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
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
