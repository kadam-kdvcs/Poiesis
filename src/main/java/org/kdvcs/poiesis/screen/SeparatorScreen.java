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
        drawProgressIndicator(
                graphics,
                SeparatorGuiLayout.PROGRESS,
                menu.progress(),
                menu.maxProgress()
        );
        renderEnergy(graphics);
        renderFluidSlot(graphics);
    }

    private void renderFluidSlot(GuiGraphics graphics) {
        ResourceLocation frame = menu.fluidSlotEnabled()
                ? FLUID_SLOT_FRAME
                : LOCKED_FLUID_SLOT_FRAME;

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
        if (stack.isEmpty()) {
            return;
        }

        IClientFluidTypeExtensions extensions =
                IClientFluidTypeExtensions.of(stack.getFluid());
        ResourceLocation still = extensions.getStillTexture(stack);
        if (still == null) {
            return;
        }

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(still);

        int color = extensions.getTintColor(stack);
        float alpha = ((color >> 24) & 0xFF) / 255.0F;
        if (alpha <= 0.0F) {
            alpha = 1.0F;
        }

        RenderSystem.setShaderColor(
                ((color >> 16) & 0xFF) / 255.0F,
                ((color >> 8) & 0xFF) / 255.0F,
                (color & 0xFF) / 255.0F,
                alpha
        );
        graphics.blit(
                leftPos + SeparatorGuiLayout.FLUID_X,
                topPos + SeparatorGuiLayout.FLUID_Y,
                0,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                sprite
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1)
                && isHovering(
                SeparatorGuiLayout.FLUID_X,
                SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            if (menu.fluidSlotEnabled()) {
                ModNetwork.sendToServer(new SeparatorFluidInteractPacket(menu.machinePos()));
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(
                SeparatorGuiLayout.FLUID_X,
                SeparatorGuiLayout.FLUID_Y,
                SeparatorGuiLayout.FLUID_SIZE,
                SeparatorGuiLayout.FLUID_SIZE,
                mouseX,
                mouseY
        )) {
            if (!menu.fluidSlotEnabled()) {
                graphics.renderComponentTooltip(
                        font,
                        List.of(Component.translatable(
                                "gui.poiesis.separator.needs_specialization_module"
                        )),
                        mouseX,
                        mouseY
                );
            } else {
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

    @Override
    protected int ioButtonX() {
        return 162;
    }

    @Override
    protected int ioButtonY() {
        return 4;
    }
}
