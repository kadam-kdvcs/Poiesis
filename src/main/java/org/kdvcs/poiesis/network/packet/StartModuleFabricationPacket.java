package org.kdvcs.poiesis.network.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.screen.ModuleFabricatorMenu;

import java.util.function.Supplier;

public record StartModuleFabricationPacket(BlockPos pos, ResourceLocation recipeId) {
    public static void encode(StartModuleFabricationPacket packet, FriendlyByteBuf buffer) { buffer.writeBlockPos(packet.pos); buffer.writeResourceLocation(packet.recipeId); }
    public static StartModuleFabricationPacket decode(FriendlyByteBuf buffer) { return new StartModuleFabricationPacket(buffer.readBlockPos(), buffer.readResourceLocation()); }
    public static void handle(StartModuleFabricationPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !(player.containerMenu instanceof ModuleFabricatorMenu menu) || !menu.machinePos().equals(packet.pos)) return;
            BlockEntity be = player.level().getBlockEntity(packet.pos);
            if (be instanceof ModuleFabricatorBlockEntity machine) {
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
