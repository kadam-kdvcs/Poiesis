package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

import java.util.function.Supplier;

public record ToggleAlloyFactoryAutoPacket(BlockPos pos) {
    public static void encode(ToggleAlloyFactoryAutoPacket packet, FriendlyByteBuf buffer) { buffer.writeBlockPos(packet.pos); }
    public static ToggleAlloyFactoryAutoPacket decode(FriendlyByteBuf buffer) { return new ToggleAlloyFactoryAutoPacket(buffer.readBlockPos()); }
    public static void handle(ToggleAlloyFactoryAutoPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof AlloyFactoryMenu menu)
                    || !menu.machinePos().equals(packet.pos)) return;
            if (player.level().getBlockEntity(packet.pos) instanceof AlloyFactoryBlockEntity machine) {
                machine.runtime().process().toggleAutoCrafting();
                machine.markPersistentDirty();
            }
        });
        context.setPacketHandled(true);
    }
}
