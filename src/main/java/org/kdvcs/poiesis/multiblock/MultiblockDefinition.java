package org.kdvcs.poiesis.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;

/** Structural rules and persistence codec for one multiblock family. */
public interface MultiblockDefinition<S extends MultiblockState> {
    S invalidState();

    S validate(LevelReader level, BlockPos controllerPos);

    @Nullable
    BlockPos findController(LevelReader level, BlockPos partPos);

    void save(CompoundTag tag, S state);

    S load(CompoundTag tag);
}
