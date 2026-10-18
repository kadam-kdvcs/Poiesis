package org.kdvcs.poiesis.block.entity.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.client.sound.MachineWorkingSounds;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import net.minecraft.sounds.SoundEvent;

import java.util.Optional;
import java.util.function.Function;

/** Minecraft-facing shell shared by ordinary Poiesis machines. */
public abstract class AbstractMachineBlockEntity<R extends MachineRuntime>
        extends BlockEntity
        implements MenuProvider, MachineContext, MachineIoHost {

    private final MachineDefinition definition;
    private final R runtime;

    protected AbstractMachineBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state,
            MachineDefinition definition,
            Function<MachineContext, R> runtimeFactory
    ) {
        super(type, pos, state);
        this.definition = definition;
        this.runtime = runtimeFactory.apply(this);
    }

    public final R runtime() {
        return runtime;
    }

    public final void tickServer() {
        runtime.tickServer();
    }

    public final void tickClient() {
        if (level == null || !level.isClientSide) return;
        boolean working = getBlockState().hasProperty(AbstractMachineBlock.WORKING)
                && getBlockState().getValue(AbstractMachineBlock.WORKING);
        if (working != clientSoundWorking) {
            clientSoundWorking = working;
            SoundEvent sound = workingSound();
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () ->
                    MachineWorkingSounds.update(worldPosition, sound, working));
        }
    }

    private boolean clientSoundWorking;

    /** Sound used while this machine's working block state is active. */
    protected SoundEvent workingSound() {
        return null;
    }

    @Override
    public final MachineDefinition machineDefinition() {
        return definition;
    }

    @Override
    public final Optional<SideConfigurationComponent> sideConfiguration() {
        return runtime.sideConfiguration();
    }

    @Override
    public final Optional<AutoIoComponent> autoIo() {
        return runtime.autoIo();
    }

    @Override
    public final void refreshIoCapabilities() {
        runtime.refreshIoCapabilities();
        onIoCapabilitiesRefreshed();

        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    /** Hook for machines that expose capabilities outside the shared router. */
    protected void onIoCapabilitiesRefreshed() {
    }

    @Override
    public final Component machineDisplayName() {
        return getDisplayName();
    }

    @Override
    public final Level level() {
        return level;
    }

    @Override
    public final BlockPos pos() {
        return worldPosition;
    }

    @Override
    public final Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(AbstractMachineBlock.FACING)
                ? state.getValue(AbstractMachineBlock.FACING)
                : Direction.NORTH;
    }

    @Override
    public final void markPersistentDirty() {
        setChanged();
    }

    @Override
    public final void markSyncDirty() {
        setChanged();

        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, 3);
        }
    }

    @Override
    public final void setWorking(boolean working) {
        if (level == null) {
            return;
        }

        BlockState state = getBlockState();
        if (state.hasProperty(AbstractMachineBlock.WORKING)
                && state.getValue(AbstractMachineBlock.WORKING) != working) {
            level.setBlock(
                    worldPosition,
                    state.setValue(AbstractMachineBlock.WORKING, working),
                    3
            );
            onWorkingStateChanged(working);
        }
    }

    /** Allows multiblock shells to mirror working visuals onto their parts. */
    protected void onWorkingStateChanged(boolean working) {
    }

    public final SimpleContainer allDrops() {
        return runtime.allDrops();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        runtime.save(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        runtime.load(tag);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        runtime.invalidate();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) {
            var router = runtime.capabilityRouter();
            if (router.isPresent()) {
                return router.orElseThrow().itemCapability(side).cast();
            }
            return runtime.inventory().capability().cast();
        }

        if (capability == ForgeCapabilities.ENERGY) {
            var router = runtime.capabilityRouter();
            if (router.isPresent()) {
                return router.orElseThrow().energyCapability(side).cast();
            }
            return runtime.energy().capability().cast();
        }

        if (capability == ForgeCapabilities.FLUID_HANDLER) {
            var router = runtime.capabilityRouter();
            if (router.isPresent()) {
                return router.orElseThrow().fluidCapability(side).cast();
            }
        }

        return super.getCapability(capability, side);
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }
}
