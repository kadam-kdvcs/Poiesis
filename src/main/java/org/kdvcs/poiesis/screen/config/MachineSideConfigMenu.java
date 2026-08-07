package org.kdvcs.poiesis.screen.config;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.registry.ModMenuTypes;

public final class MachineSideConfigMenu extends AbstractContainerMenu {

    private static final int MODE_DATA_COUNT =
            IoChannel.values().length * RelativeSide.values().length;

    private static final int AUTO_DATA_COUNT =
            IoChannel.values().length * AutoIoDirection.values().length;

    private static final int DATA_COUNT =
            MODE_DATA_COUNT + AUTO_DATA_COUNT;

    private final BlockPos machinePos;
    private final int supportedChannelMask;
    private final int autoInputMask;
    private final int autoOutputMask;
    private final IoChannel defaultPage;
    private final ContainerData data;

    public MachineSideConfigMenu(
            int containerId,
            Inventory inventory,
            FriendlyByteBuf buffer
    ) {
        this(
                containerId,
                inventory,
                buffer.readBlockPos(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                readChannel(buffer.readVarInt()),
                new SimpleContainerData(DATA_COUNT)
        );
    }

    private MachineSideConfigMenu(
            int containerId,
            Inventory inventory,
            BlockPos machinePos,
            int supportedChannelMask,
            int autoInputMask,
            int autoOutputMask,
            IoChannel defaultPage,
            ContainerData data
    ) {
        super(ModMenuTypes.MACHINE_SIDE_CONFIG.get(), containerId);

        this.machinePos = machinePos;
        this.supportedChannelMask = supportedChannelMask;
        this.autoInputMask = autoInputMask;
        this.autoOutputMask = autoOutputMask;
        this.defaultPage = defaultPage;
        this.data = data;

        checkContainerDataCount(data, DATA_COUNT);
        addDataSlots(data);
    }

    public static MenuProvider provider(MachineIoHost host) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.translatable(
                        "gui.poiesis.machine_side_config.title",
                        host.machineDisplayName()
                );
            }

            @Nullable
            @Override
            public AbstractContainerMenu createMenu(
                    int containerId,
                    Inventory inventory,
                    Player player
            ) {
                SideConfigurationComponent configuration =
                        host.machineRuntime()
                                .sideConfiguration()
                                .orElseThrow();

                AutoIoComponent autoIo =
                        host.machineRuntime()
                                .autoIo()
                                .orElseThrow();

                return new MachineSideConfigMenu(
                        containerId,
                        inventory,
                        host.pos(),
                        createSupportedMask(configuration),
                        createAutoMask(autoIo, AutoIoDirection.INPUT),
                        createAutoMask(autoIo, AutoIoDirection.OUTPUT),
                        configuration.spec().defaultPage(),
                        createServerData(configuration, autoIo)
                );
            }
        };
    }

    public static void writeOpeningData(
            FriendlyByteBuf buffer,
            MachineIoHost host
    ) {
        SideConfigurationComponent configuration =
                host.machineRuntime()
                        .sideConfiguration()
                        .orElseThrow();

        AutoIoComponent autoIo =
                host.machineRuntime()
                        .autoIo()
                        .orElseThrow();

        buffer.writeBlockPos(host.pos());
        buffer.writeVarInt(createSupportedMask(configuration));
        buffer.writeVarInt(createAutoMask(autoIo, AutoIoDirection.INPUT));
        buffer.writeVarInt(createAutoMask(autoIo, AutoIoDirection.OUTPUT));
        buffer.writeVarInt(configuration.spec().defaultPage().ordinal());
    }

    private static int createSupportedMask(
            SideConfigurationComponent configuration
    ) {
        int mask = 0;

        for (IoChannel channel : IoChannel.values()) {
            if (configuration.spec().supports(channel)) {
                mask |= 1 << channel.ordinal();
            }
        }

        return mask;
    }

    private static int createAutoMask(
            AutoIoComponent autoIo,
            AutoIoDirection direction
    ) {
        int mask = 0;

        for (IoChannel channel : IoChannel.values()) {
            if (autoIo.supports(channel, direction)) {
                mask |= 1 << channel.ordinal();
            }
        }

        return mask;
    }

    private static ContainerData createServerData(
            SideConfigurationComponent configuration,
            AutoIoComponent autoIo
    ) {
        return new ContainerData() {
            @Override
            public int get(int index) {
                if (index < 0 || index >= DATA_COUNT) {
                    return 0;
                }

                if (index < MODE_DATA_COUNT) {
                    IoChannel channel = IoChannel.values()[
                            index / RelativeSide.values().length
                            ];

                    RelativeSide side = RelativeSide.values()[
                            index % RelativeSide.values().length
                            ];

                    return configuration.mode(channel, side).ordinal();
                }

                int autoIndex = index - MODE_DATA_COUNT;
                IoChannel channel = IoChannel.values()[
                        autoIndex / AutoIoDirection.values().length
                        ];

                AutoIoDirection direction = AutoIoDirection.values()[
                        autoIndex % AutoIoDirection.values().length
                        ];

                return autoIo.enabled(channel, direction) ? 1 : 0;
            }

            @Override
            public void set(int index, int value) {
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    private static IoChannel readChannel(int ordinal) {
        if (ordinal < 0 || ordinal >= IoChannel.values().length) {
            return IoChannel.ITEM;
        }

        return IoChannel.values()[ordinal];
    }

    public BlockPos machinePos() {
        return machinePos;
    }

    public boolean supports(IoChannel channel) {
        return (supportedChannelMask & (1 << channel.ordinal())) != 0;
    }

    public boolean supportsAuto(
            IoChannel channel,
            AutoIoDirection direction
    ) {
        int mask = switch (direction) {
            case INPUT -> autoInputMask;
            case OUTPUT -> autoOutputMask;
        };

        return (mask & (1 << channel.ordinal())) != 0;
    }

    public boolean autoEnabled(
            IoChannel channel,
            AutoIoDirection direction
    ) {
        if (!supportsAuto(channel, direction)) {
            return false;
        }

        int index = MODE_DATA_COUNT
                + channel.ordinal() * AutoIoDirection.values().length
                + direction.ordinal();

        return data.get(index) != 0;
    }

    public IoChannel defaultPage() {
        return defaultPage;
    }

    public SideIoMode mode(
            IoChannel channel,
            RelativeSide side
    ) {
        int index = channel.ordinal()
                * RelativeSide.values().length
                + side.ordinal();

        int ordinal = data.get(index);

        if (ordinal < 0 || ordinal >= SideIoMode.values().length) {
            return SideIoMode.NONE;
        }

        return SideIoMode.values()[ordinal];
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        if (player.distanceToSqr(
                machinePos.getX() + 0.5D,
                machinePos.getY() + 0.5D,
                machinePos.getZ() + 0.5D
        ) > 64.0D) {
            return false;
        }

        BlockEntity blockEntity =
                player.level().getBlockEntity(machinePos);

        return blockEntity instanceof MachineIoHost;
    }
}
