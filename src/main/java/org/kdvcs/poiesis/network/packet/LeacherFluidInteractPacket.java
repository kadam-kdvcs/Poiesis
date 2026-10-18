package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.LeacherBlockEntity;
import org.kdvcs.poiesis.machine.fluid.FluidContainerInteraction;
import org.kdvcs.poiesis.screen.LeacherMenu;

import java.util.function.Supplier;

public record LeacherFluidInteractPacket(BlockPos pos, boolean output) {
    public static void encode(LeacherFluidInteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeBoolean(packet.output);
    }

    public static LeacherFluidInteractPacket decode(FriendlyByteBuf buffer) {
        return new LeacherFluidInteractPacket(buffer.readBlockPos(), buffer.readBoolean());
    }

    public static void handle(
            LeacherFluidInteractPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof LeacherMenu menu)
                    || !menu.machinePos().equals(packet.pos)
                    || player.distanceToSqr(packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D, packet.pos.getZ() + 0.5D) > 64.0D
                    || !(player.level().getBlockEntity(packet.pos)
                    instanceof LeacherBlockEntity leacher)) return;

            FluidContainerInteraction.interact(
                    player, menu,
                    packet.output ? leacher.outputTank() : leacher.inputTank(),
                    !packet.output,
                    packet.output
            );
        });
        context.setPacketHandled(true);
    }
}
