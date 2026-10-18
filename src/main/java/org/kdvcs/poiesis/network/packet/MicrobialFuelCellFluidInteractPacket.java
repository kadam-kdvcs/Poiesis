package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.MicrobialFuelCellBlockEntity;
import org.kdvcs.poiesis.machine.fluid.FluidContainerInteraction;
import org.kdvcs.poiesis.screen.MicrobialFuelCellMenu;

import java.util.function.Supplier;

public record MicrobialFuelCellFluidInteractPacket(BlockPos pos, Tank tank) {
    public enum Tank { ANOLYTE, CATHOLYTE }

    public static void encode(
            MicrobialFuelCellFluidInteractPacket packet,
            FriendlyByteBuf buffer
    ) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.tank);
    }

    public static MicrobialFuelCellFluidInteractPacket decode(FriendlyByteBuf buffer) {
        return new MicrobialFuelCellFluidInteractPacket(
                buffer.readBlockPos(), buffer.readEnum(Tank.class)
        );
    }

    public static void handle(
            MicrobialFuelCellFluidInteractPacket packet,
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

            FluidTank selected = packet.tank == Tank.ANOLYTE
                    ? fuelCell.anolyteTank() : fuelCell.catholyteTank();
            FluidContainerInteraction.interact(player, menu, selected, true, true);
        });
        context.setPacketHandled(true);
    }
}
