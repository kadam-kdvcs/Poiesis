package org.kdvcs.poiesis.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.network.packet.OpenMachineConfigPacket;
import org.kdvcs.poiesis.network.packet.SetAutoIoPacket;
import org.kdvcs.poiesis.network.packet.SetMachineSideModePacket;

import java.util.Optional;

public final class ModNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.newSimpleChannel(
                    new ResourceLocation(Poiesis.MOD_ID, "main"),
                    () -> PROTOCOL_VERSION,
                    PROTOCOL_VERSION::equals,
                    PROTOCOL_VERSION::equals
            );

    private static int nextId;

    private ModNetwork() {
    }

    public static void register() {
        CHANNEL.registerMessage(
                nextId++,
                OpenMachineConfigPacket.class,
                OpenMachineConfigPacket::encode,
                OpenMachineConfigPacket::decode,
                OpenMachineConfigPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                nextId++,
                SetMachineSideModePacket.class,
                SetMachineSideModePacket::encode,
                SetMachineSideModePacket::decode,
                SetMachineSideModePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );

        CHANNEL.registerMessage(
                nextId++,
                SetAutoIoPacket.class,
                SetAutoIoPacket::encode,
                SetAutoIoPacket::decode,
                SetAutoIoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
