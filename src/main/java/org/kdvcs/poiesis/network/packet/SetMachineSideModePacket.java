package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.screen.config.MachineSideConfigMenu;

import java.util.function.Supplier;

public record SetMachineSideModePacket(
        BlockPos pos,
        IoChannel channel,
        RelativeSide side,
        boolean reverse
) {
    public static void encode(
            SetMachineSideModePacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.channel);
        buffer.writeEnum(packet.side);
        buffer.writeBoolean(packet.reverse);
    }

    public static SetMachineSideModePacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SetMachineSideModePacket(
                buffer.readBlockPos(),
                buffer.readEnum(IoChannel.class),
                buffer.readEnum(RelativeSide.class),
                buffer.readBoolean()
        );
    }

    public static void handle(
            SetMachineSideModePacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null
                    || !(player.containerMenu
                    instanceof MachineSideConfigMenu menu)
                    || !menu.machinePos().equals(packet.pos)) {
                return;
            }

            BlockEntity blockEntity =
                    player.level().getBlockEntity(packet.pos);

            if (!(blockEntity instanceof MachineIoHost host)) {
                return;
            }

            host.machineRuntime()
                    .sideConfiguration()
                    .ifPresent(configuration -> {
                        if (!configuration.spec()
                                .supports(packet.channel)) {
                            return;
                        }

                        configuration.cycleMode(
                                packet.channel,
                                packet.side,
                                packet.reverse
                        );

                        host.machineRuntime()
                                .refreshIoCapabilities();
                    });
        });

        context.setPacketHandled(true);
    }
}
