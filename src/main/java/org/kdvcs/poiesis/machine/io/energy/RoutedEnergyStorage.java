package org.kdvcs.poiesis.machine.io.energy;

import net.minecraftforge.energy.IEnergyStorage;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

import java.util.function.Supplier;

public final class RoutedEnergyStorage implements IEnergyStorage {
    private final IEnergyStorage delegate;
    private final SideConfigurationComponent configuration;
    private final Supplier<RelativeSide> sideSupplier;

    public RoutedEnergyStorage(
            IEnergyStorage delegate,
            SideConfigurationComponent configuration,
            Supplier<RelativeSide> sideSupplier
    ) {
        this.delegate = delegate;
        this.configuration = configuration;
        this.sideSupplier = sideSupplier;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!configuration.allowsInput(IoChannel.ENERGY, sideSupplier.get())) {
            return 0;
        }

        return delegate.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        if (!configuration.allowsOutput(IoChannel.ENERGY, sideSupplier.get())) {
            return 0;
        }

        return delegate.extractEnergy(maxExtract, simulate);
    }

    @Override
    public int getEnergyStored() {
        return delegate.getEnergyStored();
    }

    @Override
    public int getMaxEnergyStored() {
        return delegate.getMaxEnergyStored();
    }

    @Override
    public boolean canExtract() {
        return configuration.allowsOutput(IoChannel.ENERGY, sideSupplier.get())
                && delegate.canExtract();
    }

    @Override
    public boolean canReceive() {
        return configuration.allowsInput(IoChannel.ENERGY, sideSupplier.get())
                && delegate.canReceive();
    }
}
