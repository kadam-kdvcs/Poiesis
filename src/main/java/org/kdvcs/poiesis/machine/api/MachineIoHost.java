package org.kdvcs.poiesis.machine.api;

import net.minecraft.network.chat.Component;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;

import java.util.Optional;

public interface MachineIoHost extends MachineContext {
    MachineDefinition machineDefinition();
    Optional<SideConfigurationComponent> sideConfiguration();
    Optional<AutoIoComponent> autoIo();
    void refreshIoCapabilities();
    Component machineDisplayName();
}
