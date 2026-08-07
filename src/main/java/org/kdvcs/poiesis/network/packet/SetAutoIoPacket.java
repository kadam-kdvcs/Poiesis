package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;

import java.util.function.Supplier;

public record SetAutoIoPacket(
        BlockPos pos,
        IoChannel channel,
        AutoIoDirection direction
) {
    public static void encode(
            SetAutoIoPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.channel);
        buffer.writeEnum(packet.direction);
    }

    public static SetAutoIoPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new SetAutoIoPacket(
                buffer.readBlockPos(),
                buffer.readEnum(IoChannel.class),
                buffer.readEnum(AutoIoDirection.class)
        );
    }

    public static void handle(
            SetAutoIoPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();

            if (player == null
                    || player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) > 64.0D) {
                return;
            }

            BlockEntity blockEntity =
                    player.level().getBlockEntity(packet.pos);

            if (!(blockEntity instanceof MachineIoHost host)) {
                return;
            }

            host.machineRuntime()
                    .autoIo()
                    .ifPresent(component ->
                            component.toggle(packet.channel, packet.direction)
                    );

        });

        context.setPacketHandled(true);
    }
}
