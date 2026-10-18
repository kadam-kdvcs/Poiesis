package org.kdvcs.poiesis.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraftforge.client.extensions.common.IClientFluidTypeExtensions;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.util.List;

/** Shared renderer, hit target and tooltip for machine fluid slots. */
public final class FluidTankWidget {
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance();

    private FluidTankWidget() {
    }

    public static void render(
            GuiGraphics graphics,
            int guiLeft,
            int guiTop,
            int x,
            int y,
            int width,
            int height,
            FluidStack stack,
            @Nullable ResourceLocation overlay
    ) {
        if (!stack.isEmpty()) {
            IClientFluidTypeExtensions extensions =
                    IClientFluidTypeExtensions.of(stack.getFluid());
            ResourceLocation still = extensions.getStillTexture(stack);
            if (still != null) {
                TextureAtlasSprite sprite = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(still);
                int color = extensions.getTintColor(stack);
                float alpha = ((color >>> 24) & 0xFF) / 255.0F;
                RenderSystem.setShaderColor(
                        ((color >>> 16) & 0xFF) / 255.0F,
                        ((color >>> 8) & 0xFF) / 255.0F,
                        (color & 0xFF) / 255.0F,
                        alpha <= 0.0F ? 1.0F : alpha
                );
                graphics.blit(guiLeft + x, guiTop + y, 0, width, height, sprite);
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        if (overlay != null) {
            graphics.blit(
                    overlay,
                    guiLeft + x,
                    guiTop + y,
                    0,
                    0,
                    width,
                    height,
                    width,
                    height
            );
        }
    }

    /**
     * Renders a fluid texture at its native 16px block scale, repeating it
     * across the widget instead of stretching one sprite over the whole area.
     * The overlay is still drawn once over the complete widget.
     */
    public static void renderTiled(
            GuiGraphics graphics,
            int guiLeft,
            int guiTop,
            int x,
            int y,
            int width,
            int height,
            FluidStack stack,
            @Nullable ResourceLocation overlay
    ) {
        if (!stack.isEmpty()) {
            IClientFluidTypeExtensions extensions =
                    IClientFluidTypeExtensions.of(stack.getFluid());
            ResourceLocation still = extensions.getStillTexture(stack);
            if (still != null) {
                TextureAtlasSprite sprite = Minecraft.getInstance()
                        .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                        .apply(still);
                int color = extensions.getTintColor(stack);
                float alpha = ((color >>> 24) & 0xFF) / 255.0F;
                RenderSystem.setShaderColor(
                        ((color >>> 16) & 0xFF) / 255.0F,
                        ((color >>> 8) & 0xFF) / 255.0F,
                        (color & 0xFF) / 255.0F,
                        alpha <= 0.0F ? 1.0F : alpha
                );
                for (int tileY = 0; tileY < height; tileY += 16) {
                    int tileHeight = Math.min(16, height - tileY);
                    for (int tileX = 0; tileX < width; tileX += 16) {
                        int tileWidth = Math.min(16, width - tileX);
                        graphics.blit(
                                guiLeft + x + tileX,
                                guiTop + y + tileY,
                                0,
                                tileWidth,
                                tileHeight,
                                sprite
                        );
                    }
                }
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        if (overlay != null) {
            graphics.blit(
                    overlay,
                    guiLeft + x,
                    guiTop + y,
                    0,
                    0,
                    width,
                    height,
                    width,
                    height
            );
        }
    }

    public static boolean isHovered(
            int guiLeft,
            int guiTop,
            int x,
            int y,
            int width,
            int height,
            double mouseX,
            double mouseY
    ) {
        return mouseX >= guiLeft + x
                && mouseX < guiLeft + x + width
                && mouseY >= guiTop + y
                && mouseY < guiTop + y + height;
    }

    public static void renderTooltip(
            GuiGraphics graphics,
            Font font,
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
                        name,
                        Component.translatable(
                                "gui.poiesis.separator.fluid_amount",
                                NUMBER_FORMAT.format(Math.max(0, amount)),
                                NUMBER_FORMAT.format(Math.max(0, capacity))
                        )
                ),
                mouseX,
                mouseY
        );
    }
}
