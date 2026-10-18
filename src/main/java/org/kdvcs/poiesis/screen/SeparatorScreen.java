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
import org.kdvcs.poiesis.network.packet.SeparatorFluidInteractPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.SeparatorGuiLayout;
import org.kdvcs.poiesis.screen.widget.FluidTankWidget;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class SeparatorScreen extends AbstractMachineScreen<SeparatorMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 172;
    private static final int ENERGY_X = 10;
    private static final int ENERGY_BOTTOM_Y = 55;
    private static final int ENERGY_WIDTH = 12;
    private static final int ENERGY_HEIGHT = 45;
    private static final int FLUID_SLOT_FRAME_OFFSET = 1;
    private static final int FLUID_SLOT_FRAME_SIZE = 18;
    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private static final ResourceLocation BACKGROUND = containerTexture("separator");
    private static final ResourceLocation ENERGY_TEXTURE = containerTexture("widget/energy_bar");
    private static final ResourceLocation FLUID_SLOT_FRAME =
            containerTexture("widget/slot");
    private static final ResourceLocation LOCKED_FLUID_SLOT_FRAME =
            containerTexture("widget/locked_slot");
    private static final ResourceLocation ELECTRODE_SLOT_FRAME =
            containerTexture("widget/electrode_slot");
    private static final ResourceLocation FLUID_SLOT_OVERLAY =
            containerTexture("widget/fluid_slot");

    public SeparatorScreen(SeparatorMenu menu, Inventory inventory, Component title) {
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
        drawEnergyBarFrame(graphics, ENERGY_X, ENERGY_BOTTOM_Y - 1);
        drawBatterySlot(graphics, SeparatorGuiLayout.BATTERY_X, SeparatorGuiLayout.BATTERY_Y);
        drawProgressIndicator(
                graphics,
                SeparatorGuiLayout.PROGRESS,
                menu.progress(),
                menu.maxProgress()
        );
        renderEnergy(graphics);
        renderSpecializationSlot(graphics);
        if (menu.rinsingModuleInstalled()) {
            renderWastewaterSlot(graphics);
        }
    }

    private void renderSpecializationSlot(GuiGraphics graphics) {
        ResourceLocation frame;
        if (menu.fluidSlotEnabled()) {
            frame = FLUID_SLOT_FRAME;
        } else if (menu.electrodeSlotVisible()) {
            frame = ELECTRODE_SLOT_FRAME;
        } else {
            frame = LOCKED_FLUID_SLOT_FRAME;
        }

        graphics.blit(
                frame,
                leftPos + SeparatorGuiLayout.FLUID_X - FLUID_SLOT_FRAME_OFFSET,
                topPos + SeparatorGuiLayout.FLUID_Y - FLUID_SLOT_FRAME_OFFSET,
                0,
                0,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE
        );

        if (menu.fluidSlotEnabled()) {
            renderFluid(graphics, menu.fluidStack());
            graphics.blit(
                    FLUID_SLOT_OVERLAY,
                    leftPos + SeparatorGuiLayout.FLUID_X,
                    topPos + SeparatorGuiLayout.FLUID_Y,
                    0,
                    0,
                    SeparatorGuiLayout.FLUID_SIZE,
                    SeparatorGuiLayout.FLUID_SIZE,
                    SeparatorGuiLayout.FLUID_SIZE,
                    SeparatorGuiLayout.FLUID_SIZE
            );
        }
    }

    private void renderEnergy(GuiGraphics graphics) {
        int visibleHeight = scale(menu.energy(), menu.capacity(), ENERGY_HEIGHT);
        if (visibleHeight <= 0) {
            return;
        }

        int sourceY = ENERGY_HEIGHT - visibleHeight;
        int destinationY = ENERGY_BOTTOM_Y - visibleHeight;
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

    private void renderFluid(GuiGraphics graphics, FluidStack stack) {
        FluidTankWidget.render(
                graphics, leftPos, topPos,
                SeparatorGuiLayout.FLUID_X, SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE, SeparatorGuiLayout.FLUID_SIZE,
                stack, null
        );
    }

    private void renderWastewaterSlot(GuiGraphics graphics) {
        int x = SeparatorGuiLayout.OUTPUT_X[3];
        int y = SeparatorGuiLayout.OUTPUT_Y[3];
        graphics.blit(
                FLUID_SLOT_FRAME,
                leftPos + x - FLUID_SLOT_FRAME_OFFSET,
                topPos + y - FLUID_SLOT_FRAME_OFFSET,
                0,
                0,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE,
                FLUID_SLOT_FRAME_SIZE
        );
        FluidTankWidget.render(
                graphics,
                leftPos,
                topPos,
                x,
                y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                menu.wastewaterStack(),
                null
        );
        graphics.blit(
                FLUID_SLOT_OVERLAY,
                leftPos + x,
                topPos + y,
                0,
                0,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE
        );
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu.fluidSlotEnabled()
                && (button == 0 || button == 1)
                && isHovering(
                SeparatorGuiLayout.FLUID_X,
                SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            ModNetwork.sendToServer(new SeparatorFluidInteractPacket(menu.machinePos(), false));
            return true;
        }
        if (menu.rinsingModuleInstalled()
                && (button == 0 || button == 1)
                && isHovering(
                SeparatorGuiLayout.OUTPUT_X[3],
                SeparatorGuiLayout.OUTPUT_Y[3],
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            ModNetwork.sendToServer(new SeparatorFluidInteractPacket(
                    menu.machinePos(),
                    true
            ));
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (menu.rinsingModuleInstalled()
                && isHovering(
                SeparatorGuiLayout.OUTPUT_X[3],
                SeparatorGuiLayout.OUTPUT_Y[3],
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            renderFluidTooltip(
                    graphics,
                    menu.wastewaterStack(),
                    menu.wastewaterAmount(),
                    menu.wastewaterCapacity(),
                    mouseX,
                    mouseY
            );
        } else if (isHovering(
                SeparatorGuiLayout.FLUID_X,
                SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            if (menu.fluidSlotEnabled()) {
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
            } else if (!menu.electrodeSlotVisible()) {
                graphics.renderComponentTooltip(
                        font,
                        List.of(Component.translatable(
                                "gui.poiesis.separator.needs_specialization_module"
                        )),
                        mouseX,
                        mouseY
                );
            }
        }

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
                                    format(menu.energy()),
                                    format(menu.capacity())
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

    private static String format(int value) {
        return NUMBER_FORMAT.format(Math.max(0, value));
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
                graphics,
                font,
                fluid,
                amount,
                capacity,
                mouseX,
                mouseY
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
