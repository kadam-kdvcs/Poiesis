package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

import java.util.function.Supplier;

public record StartAlloyFactoryPacket(BlockPos pos, ResourceLocation recipeId) {
    public static void encode(StartAlloyFactoryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos); buffer.writeResourceLocation(packet.recipeId);
    }
    public static StartAlloyFactoryPacket decode(FriendlyByteBuf buffer) {
        return new StartAlloyFactoryPacket(buffer.readBlockPos(), buffer.readResourceLocation());
    }
    public static void handle(StartAlloyFactoryPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof AlloyFactoryMenu menu)
                    || !menu.machinePos().equals(packet.pos)) return;
            if (player.level().getBlockEntity(packet.pos) instanceof AlloyFactoryBlockEntity machine) {
                machine.runtime().findRecipeById(packet.recipeId).ifPresent(recipe -> {
                    if (machine.runtime().process().canStart(recipe)) {
                        machine.runtime().process().requestManualStart(packet.recipeId);
                        machine.markPersistentDirty();
                    }
                });
            }
        });
        context.setPacketHandled(true);
    }
}
