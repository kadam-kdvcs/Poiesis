package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.screen.config.MachineSideConfigMenu;

import java.util.function.Supplier;

public record OpenMachineConfigPacket(BlockPos pos) {

    public static void encode(
            OpenMachineConfigPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.pos);
    }

    public static OpenMachineConfigPacket decode(
            FriendlyByteBuf buffer
    ) {
        return new OpenMachineConfigPacket(
                buffer.readBlockPos()
        );
    }

    public static void handle(
            OpenMachineConfigPacket packet,
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

            if (!(blockEntity instanceof MachineIoHost host)
                    || host.machineDefinition().io().isEmpty()
                    || host.sideConfiguration()
                    .isEmpty()) {
                return;
            }

            NetworkHooks.openScreen(
                    player,
                    MachineSideConfigMenu.provider(host),
                    buffer -> MachineSideConfigMenu.writeOpeningData(
                            buffer,
                            host
                    )
            );
        });

        context.setPacketHandled(true);
    }
}
