package org.kdvcs.poiesis.screen.config;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
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

    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 180;

    private static final int TAB_Y = 20;
    private static final int TAB_WIDTH = 48;
    private static final int TAB_HEIGHT = 18;

    private static final int FACE_SIZE = 28;
    private static final int FACE_GAP = 2;
    private static final int DIAGRAM_WIDTH = FACE_SIZE * 3 + FACE_GAP * 2;
    private static final int DIAGRAM_X = (GUI_WIDTH - DIAGRAM_WIDTH) / 2;
    private static final int DIAGRAM_Y = 52;

    private static final int AUTO_BUTTON_Y = 146;
    private static final int AUTO_BUTTON_WIDTH = 72;
    private static final int AUTO_BUTTON_HEIGHT = 18;
    private static final int AUTO_BUTTON_GAP = 4;
    private static final int AUTO_BUTTONS_WIDTH =
            AUTO_BUTTON_WIDTH * 2 + AUTO_BUTTON_GAP;
    private static final int AUTO_BUTTONS_X =
            (GUI_WIDTH - AUTO_BUTTONS_WIDTH) / 2;

    private final EnumMap<RelativeSide, SideModeButton> sideButtons =
            new EnumMap<>(RelativeSide.class);

    private IoChannel selectedChannel;
    private Button autoInputButton;
    private Button autoOutputButton;

    public MachineSideConfigScreen(
            MachineSideConfigMenu menu,
            Inventory inventory,
            Component title
    ) {
        super(menu, inventory, title);

        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        inventoryLabelY = 10_000;
    }

    @Override
    protected void init() {
        super.init();

        selectedChannel = menu.defaultPage();

        if (!menu.supports(selectedChannel)) {
            selectedChannel = firstSupportedChannel();
        }

        addTabs();
        addSideButtons();
        addAutoIoButtons();
        updatePageControls();
    }

    private void addTabs() {
        int tabX = leftPos + 12;

        for (IoChannel channel : IoChannel.values()) {
            if (!menu.supports(channel)) {
                continue;
            }

            Button tab = Button.builder(
                            Component.translatable(
                                    "gui.poiesis.io_channel."
                                            + channel.name().toLowerCase()
                            ),
                            button -> {
                                selectedChannel = channel;
                                updatePageControls();
                            }
                    )
                    .bounds(
                            tabX,
                            topPos + TAB_Y,
                            TAB_WIDTH,
                            TAB_HEIGHT
                    )
                    .build();

            addRenderableWidget(tab);
            tabX += TAB_WIDTH + 3;
        }
    }

    private void addSideButtons() {
        //  X    TOP    X
        // LEFT FRONT RIGHT
        // BACK BOTTOM   X
        addSideButton(RelativeSide.TOP, 1, 0);
        addSideButton(RelativeSide.LEFT, 0, 1);
        addSideButton(RelativeSide.FRONT, 1, 1);
        addSideButton(RelativeSide.RIGHT, 2, 1);
        addSideButton(RelativeSide.BACK, 0, 2);
        addSideButton(RelativeSide.BOTTOM, 1, 2);
    }

    private void addSideButton(
            RelativeSide side,
            int column,
            int row
    ) {
        int x = leftPos
                + DIAGRAM_X
                + column * (FACE_SIZE + FACE_GAP);

        int y = topPos
                + DIAGRAM_Y
                + row * (FACE_SIZE + FACE_GAP);

        SideModeButton button = new SideModeButton(
                x,
                y,
                FACE_SIZE,
                FACE_SIZE,
                side,
                () -> menu.mode(selectedChannel, side),
                () -> neighborIcon(side),
                reverse -> ModNetwork.sendToServer(
                        new SetMachineSideModePacket(
                                menu.machinePos(),
                                selectedChannel,
                                side,
                                reverse
                        )
                )
        );

        sideButtons.put(side, button);
        addRenderableWidget(button);
    }

    private void addAutoIoButtons() {
        autoInputButton = Button.builder(
                        Component.empty(),
                        button -> ModNetwork.sendToServer(
                                new SetAutoIoPacket(
                                        menu.machinePos(),
                                        selectedChannel,
                                        AutoIoDirection.INPUT
                                )
                        )
                )
                .bounds(
                        leftPos + AUTO_BUTTONS_X,
                        topPos + AUTO_BUTTON_Y,
                        AUTO_BUTTON_WIDTH,
                        AUTO_BUTTON_HEIGHT
                )
                .build();

        autoOutputButton = Button.builder(
                        Component.empty(),
                        button -> ModNetwork.sendToServer(
                                new SetAutoIoPacket(
                                        menu.machinePos(),
                                        selectedChannel,
                                        AutoIoDirection.OUTPUT
                                )
                        )
                )
                .bounds(
                        leftPos
                                + AUTO_BUTTONS_X
                                + AUTO_BUTTON_WIDTH
                                + AUTO_BUTTON_GAP,
                        topPos + AUTO_BUTTON_Y,
                        AUTO_BUTTON_WIDTH,
                        AUTO_BUTTON_HEIGHT
                )
                .build();

        addRenderableWidget(autoInputButton);
        addRenderableWidget(autoOutputButton);
    }

    private ItemStack neighborIcon(RelativeSide side) {
        Level level = Minecraft.getInstance().level;

        if (level == null) {
            return ItemStack.EMPTY;
        }

        Direction facing = machineFacing(level);
        Direction worldSide = side.toWorldSide(facing);
        BlockPos neighborPos = menu.machinePos().relative(worldSide);
        BlockState neighborState = level.getBlockState(neighborPos);

        if (neighborState.isAir()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = new ItemStack(neighborState.getBlock().asItem());
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    private Direction machineFacing(Level level) {
        BlockState machineState = level.getBlockState(menu.machinePos());

        if (machineState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return machineState.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }

        return Direction.NORTH;
    }

    private void updatePageControls() {
        boolean supported = menu.supports(selectedChannel);
        sideButtons.values().forEach(button -> button.active = supported);
        updateAutoIoButtons();
    }

    private void updateAutoIoButtons() {
        if (autoInputButton == null || autoOutputButton == null) {
            return;
        }

        boolean inputSupported = menu.supportsAuto(
                selectedChannel,
                AutoIoDirection.INPUT
        );

        boolean outputSupported = menu.supportsAuto(
                selectedChannel,
                AutoIoDirection.OUTPUT
        );

        autoInputButton.visible = inputSupported;
        autoInputButton.active = inputSupported;
        autoInputButton.setMessage(
                autoButtonLabel(
                        AutoIoDirection.INPUT,
                        menu.autoEnabled(
                                selectedChannel,
                                AutoIoDirection.INPUT
                        )
                )
        );

        autoOutputButton.visible = outputSupported;
        autoOutputButton.active = outputSupported;
        autoOutputButton.setMessage(
                autoButtonLabel(
                        AutoIoDirection.OUTPUT,
                        menu.autoEnabled(
                                selectedChannel,
                                AutoIoDirection.OUTPUT
                        )
                )
        );
    }

    private Component autoButtonLabel(
            AutoIoDirection direction,
            boolean enabled
    ) {
        Component action = Component.translatable(
                direction == AutoIoDirection.INPUT
                        ? "gui.poiesis.auto_io.input"
                        : "gui.poiesis.auto_io.output"
        );

        Component state = Component.translatable(
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
        for (IoChannel channel : IoChannel.values()) {
            if (menu.supports(channel)) {
                return channel;
            }
        }

        return IoChannel.ITEM;
    }

    @Override
    protected void renderBg(
            GuiGraphics graphics,
            float partialTick,
            int mouseX,
            int mouseY
    ) {
        graphics.fill(
                leftPos,
                topPos,
                leftPos + imageWidth,
                topPos + imageHeight,
                0xFF202020
        );

        graphics.fill(
                leftPos + 4,
                topPos + 4,
                leftPos + imageWidth - 4,
                topPos + imageHeight - 4,
                0xFF303030
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
                title,
                imageWidth / 2,
                7,
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
        super.render(graphics, mouseX, mouseY, partialTick);

        for (SideModeButton button : sideButtons.values()) {
            if (!button.isMouseOver(mouseX, mouseY)) {
                continue;
            }

            graphics.renderComponentTooltip(
                    font,
                    List.of(
                            Component.literal(button.side().name()),
                            Component.literal(button.mode().name())
                    ),
                    mouseX,
                    mouseY
            );
            break;
        }
    }
}
