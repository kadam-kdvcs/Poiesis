package org.kdvcs.poiesis.block.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.multiblock.MultiblockDefinition;
import org.kdvcs.poiesis.multiblock.MultiblockState;

import java.util.Objects;
import java.util.function.Function;

/**
 * Minecraft-facing shell shared by controller-owned multiblocks.
 *
 * <p>The base owns validation state, persistence and load-time revalidation.
 * Machine-specific visuals, capacities and ports remain in the concrete
 * controller through {@link #onStructureStateChanged}.</p>
 */
public abstract class AbstractMultiblockControllerBlockEntity<
        R extends MachineRuntime,
        S extends MultiblockState
        > extends AbstractMachineBlockEntity<R> {
    private final MultiblockDefinition<S> multiblockDefinition;
    private S structureState;

    protected AbstractMultiblockControllerBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            MachineDefinition machineDefinition,
            Function<MachineContext, R> runtimeFactory,
            MultiblockDefinition<S> multiblockDefinition
    ) {
        super(type, pos, state, machineDefinition, runtimeFactory);
        this.multiblockDefinition = Objects.requireNonNull(multiblockDefinition);
        structureState = multiblockDefinition.invalidState();
    }

    public final boolean isFormed() {
        return structureState.formed();
    }

    public final S structureState() {
        return structureState;
    }

    public final void validateStructure() {
        if (level != null && !level.isClientSide) {
            applyStructureState(multiblockDefinition.validate(level, worldPosition));
        }
    }

    public final void unform() {
        applyStructureState(multiblockDefinition.invalidState());
    }

    public final void applyStructureState(S nextState) {
        if (level == null || level.isClientSide) {
            return;
        }
        S previousState = structureState;
        structureState = Objects.requireNonNull(nextState);
        onStructureStateChanged(previousState, nextState);
    }

    /** Used while the controller block itself is already being removed. */
    protected final S clearStructureStateForRemoval() {
        S previousState = structureState;
        structureState = multiblockDefinition.invalidState();
        return previousState;
    }

    protected abstract void onStructureStateChanged(S previousState, S nextState);

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        multiblockDefinition.save(tag, structureState);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        structureState = multiblockDefinition.load(tag);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }
}
