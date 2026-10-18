package org.kdvcs.poiesis.screen;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.kdvcs.poiesis.feedback.FeedbackStore;
import org.kdvcs.poiesis.feedback.FeedbackSender;

import java.io.IOException;
import java.util.Locale;

public final class FeedbackScreen extends AbstractContainerScreen<FeedbackMenu> {
    private static final int WIDTH = 260;
    private static final int HEIGHT = 210;

    private EditBox titleBox;
    private MultiLineEditBox contentBox;
    private Component status;

    public FeedbackScreen(FeedbackMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = 10;
        titleLabelY = 8;
        inventoryLabelX = 8;
        inventoryLabelY = 102;

        titleBox = new EditBox(font, leftPos + 10, topPos + 25, imageWidth - 20, 20,
                Component.translatable("gui.poiesis.feedback.subject"));
        titleBox.setMaxLength(120);
        titleBox.setFocused(true);
        addRenderableWidget(titleBox);

        contentBox = new MultiLineEditBox(font, leftPos + 10, topPos + 50,
                imageWidth - 20, 46,
                Component.translatable("gui.poiesis.feedback.content"),
                Component.translatable("gui.poiesis.feedback.content_hint"));
        contentBox.setCharacterLimit(4000);
        addRenderableWidget(contentBox);

        addRenderableWidget(Button.builder(
                Component.translatable("gui.poiesis.feedback.save_close"),
                button -> save(false)
        ).bounds(leftPos + 10, topPos + 190, 105, 20).build());
        addRenderableWidget(Button.builder(
                Component.translatable("gui.poiesis.feedback.send"),
                button -> save(true)
        ).bounds(leftPos + 125, topPos + 190, 105, 20).build());
    }

    private void save(boolean send) {
        try {
            var file = FeedbackStore.save(
                    titleBox.getValue(),
                    contentBox.getValue(),
                    false,
                    playerContext()
            );
            if (send) {
                FeedbackSender.enqueue(file);
            }
            status = Component.translatable(send
                    ? "gui.poiesis.feedback.queued"
                    : "gui.poiesis.feedback.saved");
            onClose();
        } catch (IllegalArgumentException | IOException exception) {
            status = Component.translatable("gui.poiesis.feedback.failed");
        }
    }

    private static JsonObject playerContext() {
        JsonObject context = new JsonObject();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return context;
        }

        context.addProperty("player_name", minecraft.player.getGameProfile().getName());
        context.addProperty("player_uuid", minecraft.player.getUUID().toString());
        context.addProperty(
                "login_type",
                minecraft.getUser().getType().name().toLowerCase(Locale.ROOT)
        );
        context.addProperty(
                "world",
                minecraft.player.level().dimension().location().toString()
        );

        JsonObject position = new JsonObject();
        position.addProperty("x", minecraft.player.getX());
        position.addProperty("y", minecraft.player.getY());
        position.addProperty("z", minecraft.player.getZ());
        context.add("position", position);

        JsonObject blockPosition = new JsonObject();
        blockPosition.addProperty("x", minecraft.player.blockPosition().getX());
        blockPosition.addProperty("y", minecraft.player.blockPosition().getY());
        blockPosition.addProperty("z", minecraft.player.blockPosition().getZ());
        context.add("block_position", blockPosition);
        return context;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Prevent the inventory key (normally E) from closing this input screen.
        if (Minecraft.getInstance().options.keyInventory.matches(keyCode, scanCode)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        graphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF20252B);
        graphics.fill(leftPos + 5, topPos + 112, leftPos + imageWidth - 5, topPos + imageHeight - 5, 0xFF15181C);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        if (status != null) {
            graphics.drawString(font, status, leftPos + 10, topPos + 180, 0xFFB8C7D9, false);
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}
