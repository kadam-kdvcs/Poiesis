package org.kdvcs.poiesis.machine.definition;

import org.kdvcs.poiesis.machine.io.MachineIoSpec;

public record MachineDefinition(
        String id,
        EnergySpec energy,
        int inputSlots,
        int outputSlots,
        int moduleSlots,
        MachineIoSpec io,
        BatterySpec battery
) {
    public MachineDefinition {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException(
                    "Machine id cannot be blank"
            );
        }

        if (energy == null || io == null) {
            throw new IllegalArgumentException(
                    "Machine definition values cannot be null"
            );
        }

        if (inputSlots < 0
                || outputSlots < 0
                || moduleSlots < 0) {
            throw new IllegalArgumentException(
                    "Slot counts cannot be negative"
            );
        }
    }

    /*
     * 保留旧的六参数构造器。
     *
     * 这样你之前已经写好的机器定义
     * 不需要全部立刻修改。
     */
    public MachineDefinition(
            String id,
            EnergySpec energy,
            int inputSlots,
            int outputSlots,
            int moduleSlots,
            MachineIoSpec io
    ) {
        this(
                id,
                energy,
                inputSlots,
                outputSlots,
                moduleSlots,
                io,
                null
        );
    }

    /*
     * 保留最早的五参数构造器。
     */
    public MachineDefinition(
            String id,
            EnergySpec energy,
            int inputSlots,
            int outputSlots,
            int moduleSlots
    ) {
        this(
                id,
                energy,
                inputSlots,
                outputSlots,
                moduleSlots,
                MachineIoSpec.none(),
                null
        );
    }

    public boolean hasBatterySlot() {
        return battery != null;
    }
}