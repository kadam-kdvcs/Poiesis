package org.kdvcs.poiesis.machine.definition;

public record FluidSpec(int capacity) {
    public FluidSpec {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Fluid capacity must be greater than zero"
            );
        }
    }
}
