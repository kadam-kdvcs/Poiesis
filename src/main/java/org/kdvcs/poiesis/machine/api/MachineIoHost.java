package org.kdvcs.poiesis.machine.api;

import net.minecraft.network.chat.Component;
import org.kdvcs.poiesis.machine.MechanicalMachineRuntime;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;

public interface MachineIoHost extends MachineContext {
    MachineDefinition machineDefinition();
    MechanicalMachineRuntime machineRuntime();
    Component machineDisplayName();
}
