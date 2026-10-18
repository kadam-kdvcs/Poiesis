package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.GreenhouseBlockEntity;
import org.kdvcs.poiesis.machine.fluid.FluidContainerInteraction;
import org.kdvcs.poiesis.screen.GreenhouseMenu;

import java.util.function.Supplier;

public record GreenhouseFluidInteractPacket(BlockPos pos, Tank tank) {
    public enum Tank {
        WATER,
        NUTRIENT,
        OUTPUT
    }

    public static void encode(GreenhouseFluidInteractPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos);
        buffer.writeEnum(packet.tank);
    }

    public static GreenhouseFluidInteractPacket decode(FriendlyByteBuf buffer) {
        return new GreenhouseFluidInteractPacket(buffer.readBlockPos(), buffer.readEnum(Tank.class));
    }

    public static void handle(
            GreenhouseFluidInteractPacket packet,
            Supplier<NetworkEvent.Context> contextSupplier
    ) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null
                    || !(player.containerMenu instanceof GreenhouseMenu menu)
                    || !menu.machinePos().equals(packet.pos)
                    || player.distanceToSqr(
                    packet.pos.getX() + 0.5D,
                    packet.pos.getY() + 0.5D,
                    packet.pos.getZ() + 0.5D
            ) > 64.0D
                    || !(player.level().getBlockEntity(packet.pos)
                    instanceof GreenhouseBlockEntity greenhouse)) {
                return;
            }

            FluidTank selected = switch (packet.tank) {
                case WATER -> greenhouse.waterTank();
                case NUTRIENT -> greenhouse.nutrientTank();
                case OUTPUT -> greenhouse.outputTank();
            };
            boolean output = packet.tank == Tank.OUTPUT;
            FluidContainerInteraction.interact(player, menu, selected, !output, output);
        });
        context.setPacketHandled(true);
    }
}
