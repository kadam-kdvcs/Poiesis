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
import org.kdvcs.poiesis.network.packet.BiomassGasifierFluidInteractPacket;
import org.kdvcs.poiesis.screen.base.AbstractMachineScreen;
import org.kdvcs.poiesis.screen.layout.BiomassGasifierGuiLayout;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class BiomassGasifierScreen
        extends AbstractMachineScreen<BiomassGasifierMenu> {
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 172;
    private static final NumberFormat NUMBER_FORMAT =
            NumberFormat.getIntegerInstance(Locale.US);

    private static final ResourceLocation BACKGROUND =
            containerTexture("biomass_gasifier");
    private static final ResourceLocation TEMPERATURE_BAR =
            containerTexture("widget/temperature_bar");
    private static final ResourceLocation FIRE =
            containerTexture("widget/fire");
    private static final ResourceLocation GAS_OVERLAY =
            containerTexture("widget/fluid_slot");

    public BiomassGasifierScreen(
            BiomassGasifierMenu menu,
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
        renderTemperature(graphics);
        renderFire(graphics);
        renderGas(graphics, menu.gasStack());
        graphics.blit(
                GAS_OVERLAY,
                leftPos + BiomassGasifierGuiLayout.GAS_X,
                topPos + BiomassGasifierGuiLayout.GAS_Y,
                0,
                0,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE
        );
    }

    private void renderFire(GuiGraphics graphics) {
        int maximum = menu.maxProgress();
        if (maximum <= 0) {
            return;
        }

        int depletedHeight = scale(
                menu.progress(),
                maximum,
                BiomassGasifierGuiLayout.FIRE_SIZE
        );
        int visibleHeight = BiomassGasifierGuiLayout.FIRE_SIZE - depletedHeight;
        if (visibleHeight <= 0) {
            return;
        }

        int sourceY = BiomassGasifierGuiLayout.FIRE_SIZE - visibleHeight;
        graphics.blit(
                FIRE,
                leftPos + BiomassGasifierGuiLayout.FIRE_X,
                topPos + BiomassGasifierGuiLayout.FIRE_Y + sourceY,
                0,
                sourceY,
                BiomassGasifierGuiLayout.FIRE_SIZE,
                visibleHeight,
                BiomassGasifierGuiLayout.FIRE_SIZE,
                BiomassGasifierGuiLayout.FIRE_SIZE
        );
    }

    private void renderTemperature(GuiGraphics graphics) {
        int visibleHeight = scale(
                menu.temperature(),
                menu.maximumTemperature(),
                BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT
        );
        if (visibleHeight <= 0) {
            return;
        }

        int sourceY = BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT - visibleHeight;
        graphics.blit(
                TEMPERATURE_BAR,
                leftPos + BiomassGasifierGuiLayout.TEMPERATURE_X,
                topPos + BiomassGasifierGuiLayout.TEMPERATURE_Y + sourceY,
                0,
                sourceY,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                visibleHeight,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT
        );
    }

    private void renderGas(GuiGraphics graphics, FluidStack stack) {
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
                leftPos + BiomassGasifierGuiLayout.GAS_X,
                topPos + BiomassGasifierGuiLayout.GAS_Y,
                0,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE,
                sprite
        );
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if ((button == 0 || button == 1) && isHovering(
                BiomassGasifierGuiLayout.GAS_X,
                BiomassGasifierGuiLayout.GAS_Y,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE,
                mouseX,
                mouseY
        )) {
            ModNetwork.sendToServer(
                    new BiomassGasifierFluidInteractPacket(menu.machinePos())
            );
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderTooltip(graphics, mouseX, mouseY);

        if (isHovering(
                BiomassGasifierGuiLayout.GAS_X,
                BiomassGasifierGuiLayout.GAS_Y,
                BiomassGasifierGuiLayout.GAS_SIZE,
                BiomassGasifierGuiLayout.GAS_SIZE,
                mouseX,
                mouseY
        )) {
            FluidStack gas = menu.gasStack();
            Component name = gas.isEmpty()
                    ? Component.translatable("gui.poiesis.separator.empty_fluid")
                    : gas.getDisplayName();
            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            name,
                            Component.translatable(
                                    "gui.poiesis.separator.fluid_amount",
                                    format(menu.gasAmount()),
                                    format(menu.gasCapacity())
                            )
                    ),
                    mouseX,
                    mouseY
            );
        }

        if (isHovering(
                BiomassGasifierGuiLayout.TEMPERATURE_X,
                BiomassGasifierGuiLayout.TEMPERATURE_Y,
                BiomassGasifierGuiLayout.TEMPERATURE_WIDTH,
                BiomassGasifierGuiLayout.TEMPERATURE_HEIGHT,
                mouseX,
                mouseY
        )) {
            graphics.renderComponentTooltip(
                    font,
                    List.of(Component.translatable(
                            "gui.poiesis.gasifier.temperature",
                            format(menu.temperature()),
                            format(menu.maximumTemperature())
                    )),
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
