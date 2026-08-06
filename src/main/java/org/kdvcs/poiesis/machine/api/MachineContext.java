package org.kdvcs.poiesis.machine.api;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface MachineContext {
    Level level();
    BlockPos pos();
    void markPersistentDirty();
    void markSyncDirty();
    void setWorking(boolean working);
}
