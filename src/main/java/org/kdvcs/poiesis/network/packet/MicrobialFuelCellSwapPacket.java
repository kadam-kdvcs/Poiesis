package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.MicrobialFuelCellBlockEntity;
import org.kdvcs.poiesis.screen.MicrobialFuelCellMenu;

import java.util.function.Supplier;

public record MicrobialFuelCellSwapPacket(BlockPos pos) {
    public static void encode(MicrobialFuelCellSwapPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static MicrobialFuelCellSwapPacket decode(FriendlyByteBuf buffer) {
        return new MicrobialFuelCellSwapPacket(buffer.readBlockPos());
    }

    public static void handle(
            MicrobialFuelCellSwapPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof MicrobialFuelCellMenu menu)
                    || !menu.machinePos().equals(packet.pos)
                    || player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) > 64.0D
                    || !(player.level().getBlockEntity(packet.pos)
                    instanceof MicrobialFuelCellBlockEntity fuelCell)) {
                return;
            }
            fuelCell.runtime().swapFluids();
        });
        context.setPacketHandled(true);
    }
}
