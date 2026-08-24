package org.kdvcs.poiesis.machine.io.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

import java.util.function.Predicate;

public final class EnergyAutoTransfer {

    private EnergyAutoTransfer() {
    }

    public static void pullEnergy(
            MachineContext owner,
            EnergyComponent energy,
            SideConfigurationComponent configuration,
            Predicate<RelativeSide> sideAllowed
    ) {
        Level level = owner.level();

        if (level == null || level.isClientSide) {
            return;
        }

        for (RelativeSide relativeSide : RelativeSide.values()) {
            if (!sideAllowed.test(relativeSide)
                    || !configuration.allowsInput(IoChannel.ENERGY, relativeSide)) {
                continue;
            }

            Direction worldSide = relativeSide.toWorldSide(owner.facing());
            IEnergyStorage source = neighborEnergyStorage(
                    level,
                    owner.pos(),
                    worldSide
            );

            if (source == null || !source.canExtract()) {
                continue;
            }

            int requested = energy.spec().maxReceive();
            int available = source.extractEnergy(requested, true);

            if (available <= 0) {
                continue;
            }

            int accepted = energy.view().receiveEnergy(available, true);

            if (accepted <= 0) {
                continue;
            }

            int extracted = source.extractEnergy(accepted, false);

            if (extracted > 0) {
                energy.view().receiveEnergy(extracted, false);
            }
        }
    }

    private static IEnergyStorage neighborEnergyStorage(
            Level level,
            BlockPos machinePos,
            Direction worldSide
    ) {
        BlockEntity neighbor = level.getBlockEntity(
                machinePos.relative(worldSide)
        );

        if (neighbor == null) {
            return null;
        }

        return neighbor.getCapability(
                        ForgeCapabilities.ENERGY,
                        worldSide.getOpposite()
                )
                .resolve()
                .orElse(null);
    }
}
