package org.kdvcs.poiesis.machine.module;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;

import java.util.HashSet;
import java.util.Set;

public final class ModuleComponent implements MachineComponent {

    public static final int PROCESS_SLOT = 0;
    public static final int FIRST_PERFORMANCE_SLOT = 1;
    public static final int SECOND_PERFORMANCE_SLOT = 2;
    public static final int REQUIRED_SLOT_COUNT = 3;

    private final ItemStackHandler modules;

    private long version;
    private long processVersion;
    private long performanceVersion;

    public ModuleComponent(
            int slots,
            Runnable changed
    ) {
        if (slots != REQUIRED_SLOT_COUNT) {
            throw new IllegalArgumentException(
                    "Mechanical processor requires exactly "
                            + REQUIRED_SLOT_COUNT
                            + " module slots"
            );
        }

        modules = new ItemStackHandler(slots) {

            @Override
            public boolean isItemValid(
                    int slot,
                    ItemStack stack
            ) {
                if (!(stack.getItem()
                        instanceof MachineModuleItem module)) {
                    return false;
                }

                return switch (slot) {
                    case PROCESS_SLOT ->
                            module.kind() == ModuleKind.PROCESS
                                    && module.mode()
                                    != MechanicalMode.NONE;

                    case FIRST_PERFORMANCE_SLOT,
                         SECOND_PERFORMANCE_SLOT ->
                            module.kind() == ModuleKind.PERFORMANCE
                                    && module.mode()
                                    == MechanicalMode.NONE;

                    default -> false;
                };
            }

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }

            @Override
            protected void onContentsChanged(int slot) {
                version++;

                if (slot == PROCESS_SLOT) {
                    processVersion++;
                } else if (slot == FIRST_PERFORMANCE_SLOT
                        || slot == SECOND_PERFORMANCE_SLOT) {
                    performanceVersion++;
                }

                changed.run();
            }
        };
    }

    @Override
    public String key() {
        return "modules";
    }

    public ItemStackHandler items() {
        return modules;
    }

    /**
     * 任意模块槽发生变化时递增。
     */
    public long version() {
        return version;
    }

    /**
     * 只有工艺模块槽发生变化时递增。
     *
     * 配方缓存应监听这个版本。
     */
    public long processVersion() {
        return processVersion;
    }

    /**
     * 只有性能模块槽发生变化时递增。
     */
    public long performanceVersion() {
        return performanceVersion;
    }

    public MechanicalMode currentMode() {
        ItemStack stack =
                modules.getStackInSlot(PROCESS_SLOT);

        if (stack.getItem()
                instanceof MachineModuleItem module
                && module.kind() == ModuleKind.PROCESS) {
            return module.mode();
        }

        return MechanicalMode.NONE;
    }

    public ModuleSnapshot snapshot() {
        ItemStack processStack =
                modules.getStackInSlot(PROCESS_SLOT);

        if (!(processStack.getItem()
                instanceof MachineModuleItem processModule)
                || processModule.kind() != ModuleKind.PROCESS
                || processModule.mode() == MechanicalMode.NONE) {
            return ModuleSnapshot.empty();
        }

        Set<ResourceLocation> capabilities =
                new HashSet<>(
                        processModule.capabilities()
                );

        int requestedSpeed = 1;
        double energyMultiplier = 1.0D;

        for (
                int slot = FIRST_PERFORMANCE_SLOT;
                slot <= SECOND_PERFORMANCE_SLOT;
                slot++
        ) {
            ItemStack stack =
                    modules.getStackInSlot(slot);

            if (stack.getItem()
                    instanceof MachineModuleItem module
                    && module.kind()
                    == ModuleKind.PERFORMANCE) {

                requestedSpeed +=
                        module.speedBonus();

                energyMultiplier *=
                        module.energyMultiplier();
            }
        }

        return new ModuleSnapshot(
                processModule.mode(),
                Set.copyOf(capabilities),
                Math.max(1, requestedSpeed),
                Math.max(0.25D, energyMultiplier)
        );
    }

    @Override
    public void save(CompoundTag tag) {
        tag.put(
                "items",
                modules.serializeNBT()
        );

        tag.putLong(
                "version",
                version
        );

        tag.putLong(
                "process_version",
                processVersion
        );

        tag.putLong(
                "performance_version",
                performanceVersion
        );
    }

    @Override
    public void load(CompoundTag tag) {
        modules.deserializeNBT(
                tag.getCompound("items")
        );

        version = tag.getLong("version");
        processVersion =
                tag.getLong("process_version");
        performanceVersion =
                tag.getLong("performance_version");
    }
}