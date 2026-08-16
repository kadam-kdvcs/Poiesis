package org.kdvcs.poiesis.machine.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.HashSet;
import java.util.Set;

/**
 * Generic module inventory shared by all modular machines.
 *
 * Slot acceptance and semantic roles are supplied by ModuleLayout. Adding a
 * machine with a different specialization rule therefore does not require a
 * second ModuleComponent implementation.
 */
public final class ModuleComponent implements MachineComponent {

    // Compatibility constants for the current three-slot machine GUIs.
    public static final int PROCESS_SLOT = 0;
    public static final int FIRST_PERFORMANCE_SLOT = 1;
    public static final int SECOND_PERFORMANCE_SLOT = 2;
    public static final int REQUIRED_SLOT_COUNT = 3;

    private final ModuleLayout layout;
    private final ItemStackHandler modules;

    private long version;
    private long capabilityVersion;
    private long performanceVersion;

    public ModuleComponent(
            ModuleLayout layout,
            Runnable changed
    ) {
        if (layout == null) {
            throw new IllegalArgumentException("Module layout cannot be null");
        }

        this.layout = layout;
        this.modules = new ItemStackHandler(layout.size()) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return ModuleComponent.this.layout.accepts(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                version++;

                switch (ModuleComponent.this.layout.role(slot)) {
                    case PROCESS -> capabilityVersion++;
                    case PERFORMANCE -> performanceVersion++;
                    case PROTECTION -> capabilityVersion++;
                }

                changed.run();
            }
        };
    }

    /**
     * Legacy constructor retained for source compatibility. It represents the
     * mechanical processor's original three-slot layout.
     */
    public ModuleComponent(int slots, Runnable changed) {
        this(ModuleLayout.mechanicalProcessing(), changed);

        if (slots != layout.size()) {
            throw new IllegalArgumentException(
                    "Module layout requires exactly " + layout.size() + " slots"
            );
        }
    }

    @Override
    public String key() {
        return "modules";
    }

    public ModuleLayout layout() {
        return layout;
    }

    public ItemStackHandler items() {
        return modules;
    }

    public long version() {
        return version;
    }

    /** Version of slots that can change process capabilities. */
    public long capabilityVersion() {
        return capabilityVersion;
    }

    /** Compatibility alias for the old mechanical-process name. */
    public long processVersion() {
        return capabilityVersion;
    }

    /** Compatibility alias for the old Compounder specialization name. */
    public long specializationVersion() {
        return capabilityVersion;
    }

    public long performanceVersion() {
        return performanceVersion;
    }

    public MechanicalMode currentMode() {
        return snapshot().mode();
    }

    public ModuleSnapshot snapshot() {
        Set<ResourceLocation> capabilities = new HashSet<>();
        MechanicalMode mode = MechanicalMode.NONE;
        int requestedSpeed = 1;
        double energyMultiplier = 1.0D;

        for (int slot = 0; slot < modules.getSlots(); slot++) {
            ItemStack stack = modules.getStackInSlot(slot);
            if (!(stack.getItem() instanceof MachineModuleItem module)) {
                continue;
            }

            switch (layout.role(slot)) {
                case PROCESS, PROTECTION -> {
                    capabilities.addAll(module.capabilities());
                    if (mode == MechanicalMode.NONE
                            && module.mode() != MechanicalMode.NONE) {
                        mode = module.mode();
                    }
                }
                case PERFORMANCE -> {
                    requestedSpeed += module.speedBonus();
                    energyMultiplier *= module.energyMultiplier();
                }
            }
        }

        return new ModuleSnapshot(
                mode,
                Set.copyOf(capabilities),
                Math.max(1, requestedSpeed),
                Math.max(0.25D, energyMultiplier)
        );
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put("items", modules.serializeNBT());
        tag.putLong("version", version);
        tag.putLong("capability_version", capabilityVersion);
        tag.putLong("performance_version", performanceVersion);
    }

    @Override
    public void load(CompoundTag tag) {
        modules.deserializeNBT(tag.getCompound("items"));
        version = tag.getLong("version");

        if (tag.contains("capability_version")) {
            capabilityVersion = tag.getLong("capability_version");
        } else if (tag.contains("process_version")) {
            capabilityVersion = tag.getLong("process_version");
        } else {
            // Compatibility with the old Compounder component.
            capabilityVersion = tag.getLong("specialization_version");
        }

        performanceVersion = tag.getLong("performance_version");
    }
}
