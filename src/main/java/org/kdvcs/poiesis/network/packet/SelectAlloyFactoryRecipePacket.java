package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

import java.util.function.Supplier;

public record SelectAlloyFactoryRecipePacket(BlockPos pos, ResourceLocation recipeId) {
    public static void encode(SelectAlloyFactoryRecipePacket packet, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(packet.pos); buffer.writeResourceLocation(packet.recipeId);
    }
    public static SelectAlloyFactoryRecipePacket decode(FriendlyByteBuf buffer) {
        return new SelectAlloyFactoryRecipePacket(buffer.readBlockPos(), buffer.readResourceLocation());
    }
    public static void handle(SelectAlloyFactoryRecipePacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof AlloyFactoryMenu menu)
                    || !menu.machinePos().equals(packet.pos)) return;
            if (player.level().getBlockEntity(packet.pos) instanceof AlloyFactoryBlockEntity machine) {
                if (machine.runtime().process().isProcessing()) return;
                machine.runtime().findRecipeById(packet.recipeId).ifPresent(recipe -> {
                    machine.runtime().process().select(packet.recipeId);
                    machine.markPersistentDirty();
                });
            }
        });
        context.setPacketHandled(true);
    }
}
