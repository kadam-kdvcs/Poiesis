package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.screen.ModuleFabricatorMenu;

import java.util.function.Supplier;

public record ToggleModuleFabricatorAutoPacket(BlockPos pos) {
    public static void encode(ToggleModuleFabricatorAutoPacket packet, FriendlyByteBuf buffer) { buffer.writeBlockPos(packet.pos); }
    public static ToggleModuleFabricatorAutoPacket decode(FriendlyByteBuf buffer) { return new ToggleModuleFabricatorAutoPacket(buffer.readBlockPos()); }
    public static void handle(ToggleModuleFabricatorAutoPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof ModuleFabricatorMenu menu) || !menu.machinePos().equals(packet.pos)) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof ModuleFabricatorBlockEntity machine) {
                machine.runtime().process().toggleAutoCrafting();
                machine.markPersistentDirty();
            }
        });
        context.setPacketHandled(true);
    }
}
