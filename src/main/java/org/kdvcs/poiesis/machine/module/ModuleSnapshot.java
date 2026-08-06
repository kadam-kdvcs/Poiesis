package org.kdvcs.poiesis.machine.module;

import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public record ModuleSnapshot(
        MechanicalMode mode,
        Set<ResourceLocation> capabilities,
        int requestedSpeed,
        double energyMultiplier
) {
    public static ModuleSnapshot empty() {
        return new ModuleSnapshot(
                MechanicalMode.NONE,
                Set.of(),
                1,
                1.0D
        );
    }

    public boolean hasProcessModule() {
        return mode != MechanicalMode.NONE;
    }
}
