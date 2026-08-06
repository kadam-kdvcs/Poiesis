package org.kdvcs.poiesis.machine.module;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.Set;

public class MachineModuleItem extends Item {
    private final ModuleKind kind;
    private final MechanicalMode mode;
    private final Set<ResourceLocation> capabilities;
    private final int speedBonus;
    private final double energyMultiplier;

    public MachineModuleItem(
            Properties properties,
            ModuleKind kind,
            MechanicalMode mode,
            Set<ResourceLocation> capabilities,
            int speedBonus,
            double energyMultiplier
    ) {
        super(properties.stacksTo(1));
        this.kind = kind;
        this.mode = mode;
        this.capabilities = Set.copyOf(capabilities);
        this.speedBonus = speedBonus;
        this.energyMultiplier = energyMultiplier;
    }

    public ModuleKind kind() {
        return kind;
    }

    public MechanicalMode mode() {
        return mode;
    }

    public Set<ResourceLocation> capabilities() {
        return capabilities;
    }

    public int speedBonus() {
        return speedBonus;
    }

    public double energyMultiplier() {
        return energyMultiplier;
    }
}
