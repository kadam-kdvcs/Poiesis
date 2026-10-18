package org.kdvcs.poiesis.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.network.packet.OpenMachineConfigPacket;
import org.kdvcs.poiesis.network.packet.SetAutoIoPacket;
import org.kdvcs.poiesis.network.packet.SetMachineSideModePacket;
import org.kdvcs.poiesis.network.packet.SelectModuleFabricationRecipePacket;
import org.kdvcs.poiesis.network.packet.StartModuleFabricationPacket;
import org.kdvcs.poiesis.network.packet.ToggleModuleFabricatorAutoPacket;
import org.kdvcs.poiesis.network.packet.SeparatorFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.AdsorptionColumnFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.BiomassGasifierFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.LeacherFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.SelectAlloyFactoryRecipePacket;
import org.kdvcs.poiesis.network.packet.StartAlloyFactoryPacket;
import org.kdvcs.poiesis.network.packet.ToggleAlloyFactoryAutoPacket;
import org.kdvcs.poiesis.network.packet.GreenhouseFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.GasGeneratorFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.MicrobialFuelCellFluidInteractPacket;
import org.kdvcs.poiesis.network.packet.MicrobialFuelCellSwapPacket;

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

        CHANNEL.registerMessage(
                nextId++, SelectModuleFabricationRecipePacket.class,
                SelectModuleFabricationRecipePacket::encode,
                SelectModuleFabricationRecipePacket::decode,
                SelectModuleFabricationRecipePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, StartModuleFabricationPacket.class,
                StartModuleFabricationPacket::encode,
                StartModuleFabricationPacket::decode,
                StartModuleFabricationPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, ToggleModuleFabricatorAutoPacket.class,
                ToggleModuleFabricatorAutoPacket::encode,
                ToggleModuleFabricatorAutoPacket::decode,
                ToggleModuleFabricatorAutoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, SeparatorFluidInteractPacket.class,
                SeparatorFluidInteractPacket::encode,
                SeparatorFluidInteractPacket::decode,
                SeparatorFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, AdsorptionColumnFluidInteractPacket.class,
                AdsorptionColumnFluidInteractPacket::encode,
                AdsorptionColumnFluidInteractPacket::decode,
                AdsorptionColumnFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, LeacherFluidInteractPacket.class,
                LeacherFluidInteractPacket::encode,
                LeacherFluidInteractPacket::decode,
                LeacherFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, BiomassGasifierFluidInteractPacket.class,
                BiomassGasifierFluidInteractPacket::encode,
                BiomassGasifierFluidInteractPacket::decode,
                BiomassGasifierFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, GasGeneratorFluidInteractPacket.class,
                GasGeneratorFluidInteractPacket::encode,
                GasGeneratorFluidInteractPacket::decode,
                GasGeneratorFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, MicrobialFuelCellFluidInteractPacket.class,
                MicrobialFuelCellFluidInteractPacket::encode,
                MicrobialFuelCellFluidInteractPacket::decode,
                MicrobialFuelCellFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, MicrobialFuelCellSwapPacket.class,
                MicrobialFuelCellSwapPacket::encode,
                MicrobialFuelCellSwapPacket::decode,
                MicrobialFuelCellSwapPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, SelectAlloyFactoryRecipePacket.class,
                SelectAlloyFactoryRecipePacket::encode,
                SelectAlloyFactoryRecipePacket::decode,
                SelectAlloyFactoryRecipePacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, StartAlloyFactoryPacket.class,
                StartAlloyFactoryPacket::encode,
                StartAlloyFactoryPacket::decode,
                StartAlloyFactoryPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, ToggleAlloyFactoryAutoPacket.class,
                ToggleAlloyFactoryAutoPacket::encode,
                ToggleAlloyFactoryAutoPacket::decode,
                ToggleAlloyFactoryAutoPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
        CHANNEL.registerMessage(
                nextId++, GreenhouseFluidInteractPacket.class,
                GreenhouseFluidInteractPacket::encode,
                GreenhouseFluidInteractPacket::decode,
                GreenhouseFluidInteractPacket::handle,
                Optional.of(NetworkDirection.PLAY_TO_SERVER)
        );
    }

    public static void sendToServer(Object message) {
        CHANNEL.sendToServer(message);
    }
}
