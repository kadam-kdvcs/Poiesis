package org.kdvcs.poiesis.machine.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

public interface MachineContext {
    Level level();
    BlockPos pos();
    Direction facing();
    void markPersistentDirty();
    void markSyncDirty();
    void setWorking(boolean working);
}
