package org.kdvcs.poiesis.screen.config;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.network.ModNetwork;
import org.kdvcs.poiesis.network.packet.SetAutoIoPacket;
import org.kdvcs.poiesis.network.packet.SetMachineSideModePacket;

import java.util.EnumMap;
import java.util.List;

public final class MachineSideConfigScreen
        extends AbstractContainerScreen<MachineSideConfigMenu> {

    private static final ResourceLocation BACKGROUND_TEXTURE =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "textures/container/io_config_page.png"
            );

    private static final int GUI_WIDTH = 130;
    private static final int GUI_HEIGHT = 135;

    private static final int CHANNEL_BUTTON_SIZE = 18;
    private static final int CHANNEL_BUTTON_GAP = 2;
    private static final int CHANNEL_BUTTON_Y = 17;

    private static final int SIDE_AREA_X = 26;
    private static final int SIDE_AREA_Y = 41;
    private static final int SIDE_AREA_WIDTH = 78;
    private static final int SIDE_AREA_HEIGHT = 78;

    private static final int SIDE_BUTTON_SIZE = 24;
    private static final int SIDE_BUTTON_GAP = 2;

    private static final int SIDE_GRID_WIDTH =
            SIDE_BUTTON_SIZE * 3 + SIDE_BUTTON_GAP * 2;

    private static final int SIDE_GRID_HEIGHT =
            SIDE_BUTTON_SIZE * 3 + SIDE_BUTTON_GAP * 2;

    private static final int SIDE_GRID_X =
            SIDE_AREA_X
                    + (SIDE_AREA_WIDTH - SIDE_GRID_WIDTH) / 2;

    private static final int SIDE_GRID_Y =
            SIDE_AREA_Y
                    + (SIDE_AREA_HEIGHT - SIDE_GRID_HEIGHT) / 2;

    private static final int AUTO_BUTTON_SIZE = 18;
    private static final int AUTO_BUTTON_GAP = 2;

    private static final int AUTO_BUTTON_X =
            -AUTO_BUTTON_SIZE - 2;

    private static final int AUTO_BUTTON_Y = 41;

    private final EnumMap<IoChannel, Button> channelButtons =
            new EnumMap<>(IoChannel.class);

    private final EnumMap<RelativeSide, SideModeButton> sideButtons =
            new EnumMap<>(RelativeSide.class);

    private IoChannel selectedChannel;

    private AutoIoButton autoInputButton;
    private AutoIoButton autoOutputButton;

    public MachineSideConfigScreen(
            MachineSideConfigMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(
                menu,
                inventory,
                title
        );

        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;

        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();

        selectedChannel =
                menu.defaultPage();

        if (!menu.supports(selectedChannel)) {
            selectedChannel =
                    firstSupportedChannel();
        }

        addChannelButtons();
        addSideButtons();
        addAutoIoButtons();

        updatePageControls();
    }

    private void addChannelButtons() {
        int supportedCount = 0;

        for (IoChannel channel : IoChannel.values()) {
            if (menu.supports(channel)) {
                supportedCount++;
            }
        }

        int totalWidth =
                supportedCount * CHANNEL_BUTTON_SIZE
                        + Math.max(
                        0,
                        supportedCount - 1
                ) * CHANNEL_BUTTON_GAP;

        int x =
                leftPos
                        + (GUI_WIDTH - totalWidth) / 2;

        for (IoChannel channel : IoChannel.values()) {
            if (!menu.supports(channel)) {
                continue;
            }

            Button button =
                    Button.builder(
                                    Component.empty(),
                                    pressed -> {
                                        selectedChannel = channel;
                                        updatePageControls();
                                    }
                            )
                            .bounds(
                                    x,
                                    topPos + CHANNEL_BUTTON_Y,
                                    CHANNEL_BUTTON_SIZE,
                                    CHANNEL_BUTTON_SIZE
                            )
                            .build();

            button.setTooltip(
                    Tooltip.create(
                            Component.translatable(
                                    "gui.poiesis.io_channel."
                                            + channel.name().toLowerCase()
                            )
                    )
            );

            channelButtons.put(
                    channel,
                    button
            );

            addRenderableWidget(button);

            x +=
                    CHANNEL_BUTTON_SIZE
                            + CHANNEL_BUTTON_GAP;
        }
    }

    private void addSideButtons() {
        /*
         *   X    TOP     X
         * LEFT  FRONT  RIGHT
         * BACK BOTTOM    X
         */

        addSideButton(
                RelativeSide.TOP,
                1,
                0
        );

        addSideButton(
                RelativeSide.LEFT,
                0,
                1
        );

        addSideButton(
                RelativeSide.FRONT,
                1,
                1
        );

        addSideButton(
                RelativeSide.RIGHT,
                2,
                1
        );

        addSideButton(
                RelativeSide.BACK,
                0,
                2
        );

        addSideButton(
                RelativeSide.BOTTOM,
                1,
                2
        );
    }

    private void addSideButton(
            RelativeSide side,
            int column,
            int row
    ) {
        int x =
                leftPos
                        + sideButtonX(column);

        int y =
                topPos
                        + sideButtonY(row);

        SideModeButton button =
                new SideModeButton(
                        x,
                        y,
                        SIDE_BUTTON_SIZE,
                        SIDE_BUTTON_SIZE,
                        side,
                        () -> menu.mode(
                                selectedChannel,
                                side
                        ),
                        () -> neighborIcon(side),
                        reverse ->
                                ModNetwork.sendToServer(
                                        new SetMachineSideModePacket(
                                                menu.machinePos(),
                                                selectedChannel,
                                                side,
                                                reverse
                                        )
                                )
                );

        sideButtons.put(
                side,
                button
        );

        addRenderableWidget(button);
    }

    private static int sideButtonX(
            int column
    ) {
        return SIDE_GRID_X
                + column
                * (SIDE_BUTTON_SIZE + SIDE_BUTTON_GAP);
    }

    private static int sideButtonY(
            int row
    ) {
        return SIDE_GRID_Y
                + row
                * (SIDE_BUTTON_SIZE + SIDE_BUTTON_GAP);
    }

    private void addAutoIoButtons() {
        autoInputButton =
                new AutoIoButton(
                        leftPos + AUTO_BUTTON_X,
                        topPos + AUTO_BUTTON_Y,
                        new ItemStack(
                                Items.HOPPER
                        ),
                        () -> menu.autoEnabled(
                                selectedChannel,
                                AutoIoDirection.INPUT
                        ),
                        button ->
                                ModNetwork.sendToServer(
                                        new SetAutoIoPacket(
                                                menu.machinePos(),
                                                selectedChannel,
                                                AutoIoDirection.INPUT
                                        )
                                )
                );

        autoInputButton.setTooltip(
                Tooltip.create(
                        Component.translatable(
                                "gui.poiesis.auto_io.input"
                        )
                )
        );

        autoOutputButton =
                new AutoIoButton(
                        leftPos + AUTO_BUTTON_X,
                        topPos
                                + AUTO_BUTTON_Y
                                + AUTO_BUTTON_SIZE
                                + AUTO_BUTTON_GAP,
                        new ItemStack(
                                Items.PISTON
                        ),
                        () -> menu.autoEnabled(
                                selectedChannel,
                                AutoIoDirection.OUTPUT
                        ),
                        button ->
                                ModNetwork.sendToServer(
                                        new SetAutoIoPacket(
                                                menu.machinePos(),
                                                selectedChannel,
                                                AutoIoDirection.OUTPUT
                                        )
                                )
                );

        autoOutputButton.setTooltip(
                Tooltip.create(
                        Component.translatable(
                                "gui.poiesis.auto_io.output"
                        )
                )
        );

        addRenderableWidget(
                autoInputButton
        );

        addRenderableWidget(
                autoOutputButton
        );
    }

    private ItemStack neighborIcon(
            RelativeSide side
    ) {
        Level level =
                Minecraft.getInstance().level;

        if (level == null) {
            return ItemStack.EMPTY;
        }

        Direction facing =
                machineFacing(level);

        Direction worldSide =
                side.toWorldSide(facing);

        BlockPos neighborPos =
                menu.machinePos()
                        .relative(worldSide);

        BlockState neighborState =
                level.getBlockState(neighborPos);

        if (neighborState.isAir()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack =
                new ItemStack(
                        neighborState
                                .getBlock()
                                .asItem()
                );

        return stack.isEmpty()
                ? ItemStack.EMPTY
                : stack;
    }

    private Direction machineFacing(
            Level level
    ) {
        BlockState machineState =
                level.getBlockState(
                        menu.machinePos()
                );

        if (machineState.hasProperty(
                BlockStateProperties.HORIZONTAL_FACING
        )) {
            return machineState.getValue(
                    BlockStateProperties.HORIZONTAL_FACING
            );
        }

        return Direction.NORTH;
    }

    private void updatePageControls() {
        for (var entry
                : channelButtons.entrySet()) {

            entry.getValue().active =
                    entry.getKey()
                            != selectedChannel;
        }

        boolean supported =
                menu.supports(
                        selectedChannel
                );

        sideButtons.values()
                .forEach(
                        button ->
                                button.active = supported
                );

        updateAutoIoButtons();
    }

    private void updateAutoIoButtons() {
        if (autoInputButton == null
                || autoOutputButton == null) {
            return;
        }

        boolean inputSupported =
                menu.supportsAuto(
                        selectedChannel,
                        AutoIoDirection.INPUT
                );

        boolean outputSupported =
                menu.supportsAuto(
                        selectedChannel,
                        AutoIoDirection.OUTPUT
                );

        autoInputButton.visible =
                inputSupported;

        autoInputButton.active =
                inputSupported;

        autoOutputButton.visible =
                outputSupported;

        autoOutputButton.active =
                outputSupported;
    }

    private Component autoButtonLabel(
            AutoIoDirection direction,
            boolean enabled
    ) {
        Component action =
                Component.translatable(
                        direction
                                == AutoIoDirection.INPUT
                                ? "gui.poiesis.auto_io.input"
                                : "gui.poiesis.auto_io.output"
                );

        Component state =
                Component.translatable(
                        enabled
                                ? "gui.poiesis.auto_io.on"
                                : "gui.poiesis.auto_io.off"
                );

        return Component.translatable(
                "gui.poiesis.auto_io.toggle",
                action,
                state
        );
    }

    private IoChannel firstSupportedChannel() {
        for (IoChannel channel
                : IoChannel.values()) {

            if (menu.supports(channel)) {
                return channel;
            }
        }

        return IoChannel.ITEM;
    }

    private static ItemStack channelIcon(
            IoChannel channel
    ) {
        return switch (channel) {
            case ITEM ->
                    new ItemStack(
                            Items.IRON_INGOT
                    );

            case ENERGY ->
                    new ItemStack(
                            Items.REDSTONE
                    );

            case FLUID ->
                    new ItemStack(
                            Items.WATER_BUCKET
                    );
        };
    }

    private void renderChannelIcons(
            GuiGraphics graphics
    ) {
        for (var entry
                : channelButtons.entrySet()) {

            IoChannel channel =
                    entry.getKey();

            Button button =
                    entry.getValue();

            if (!button.visible) {
                continue;
            }

            ItemStack icon =
                    channelIcon(channel);

            int iconX =
                    button.getX() + 1;

            int iconY =
                    button.getY() + 1;

            graphics.renderItem(
                    icon,
                    iconX,
                    iconY
            );

            if (!button.active) {
                graphics.fill(
                        iconX,
                        iconY,
                        iconX + 16,
                        iconY + 16,
                        0x88000000
                );
            }
        }
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        RenderSystem.setShaderTexture(
                0,
                BACKGROUND_TEXTURE
        );

        graphics.blit(
                BACKGROUND_TEXTURE,
                leftPos,
                topPos,
                0,
                0,
                GUI_WIDTH,
                GUI_HEIGHT,
                GUI_WIDTH,
                GUI_HEIGHT
        );
    }

    @Override
    protected void renderLabels(
            GuiGraphics graphics,
            int mouseX,
            int mouseY
    ) {
        graphics.drawCenteredString(
                font,
                Component.translatable(
                        "gui.poiesis.io_config.title"
                ),
                imageWidth / 2,
                6,
                0xFFFFFFFF
        );
    }

    @Override
    public void render(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        renderBackground(graphics);

        updateAutoIoButtons();

        super.render(
                graphics,
                mouseX,
                mouseY,
                partialTick
        );

        renderChannelIcons(graphics);

        for (SideModeButton button
                : sideButtons.values()) {

            if (!button.isMouseOver(
                    mouseX,
                    mouseY
            )) {
                continue;
            }

            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.translatable(
                                    "gui.poiesis.relative_side."
                                            + button.side()
                                            .name()
                                            .toLowerCase()
                            ),
                            Component.translatable(
                                    "gui.poiesis.side_mode."
                                            + button.mode()
                                            .name()
                                            .toLowerCase()
                            )
                    ),
                    mouseX,
                    mouseY
            );

            break;
        }
    }
}