package org.kdvcs.poiesis.transport.pipe.fluid;

public enum FluidPipeTier {
    BASIC(250),
    ADVANCED(1_000),
    ELITE(4_000);

    private final int milliBucketsPerTick;

    FluidPipeTier(int milliBucketsPerTick) {
        this.milliBucketsPerTick = milliBucketsPerTick;
    }

    public int milliBucketsPerTick() {
        return milliBucketsPerTick;
    }
}
