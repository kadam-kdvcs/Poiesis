package org.kdvcs.poiesis.machine.definition;

public record BatterySpec(
        int transferPerTick
) {
    public BatterySpec {
        if (transferPerTick <= 0) {
            throw new IllegalArgumentException(
                    "Battery transfer per tick must be positive"
            );
        }
    }
}