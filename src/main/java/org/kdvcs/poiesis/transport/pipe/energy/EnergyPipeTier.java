package org.kdvcs.poiesis.transport.pipe.energy;

public enum EnergyPipeTier {
    BASIC(1_000),
    ADVANCED(4_000),
    ELITE(16_000);

    private final int transferPerTick;

    EnergyPipeTier(int transferPerTick) {
        this.transferPerTick = transferPerTick;
    }

    public int transferPerTick() {
        return transferPerTick;
    }
}
