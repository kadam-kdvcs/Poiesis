package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.GasGeneratorBlockEntity;
import org.kdvcs.poiesis.machine.fluid.FluidContainerInteraction;
import org.kdvcs.poiesis.screen.GasGeneratorMenu;

import java.util.function.Supplier;

public record GasGeneratorFluidInteractPacket(BlockPos pos) {
    public static void encode(GasGeneratorFluidInteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
    }

    public static GasGeneratorFluidInteractPacket decode(FriendlyByteBuf buffer) {
        return new GasGeneratorFluidInteractPacket(buffer.readBlockPos());
    }

    public static void handle(
            GasGeneratorFluidInteractPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof GasGeneratorMenu menu)
                    || !menu.machinePos().equals(packet.pos)
                    || player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) > 64.0D) {
                return;
            }
            if (!(player.level().getBlockEntity(packet.pos)
                    instanceof GasGeneratorBlockEntity generator)) {
                return;
            }
            FluidContainerInteraction.interact(
                    player,
                    menu,
                    generator.runtime().fluid().orElseThrow().tank(),
                    true,
                    true
            );
        });
        context.setPacketHandled(true);
    }
}
