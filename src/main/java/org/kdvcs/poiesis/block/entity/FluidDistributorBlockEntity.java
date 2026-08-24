package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.multiblock.AdsorptionColumnStructure;
import org.kdvcs.poiesis.registry.ModBlockEntities;

/** Storage-free top input proxy. The controller remains the sole resource owner. */
public final class FluidDistributorBlockEntity extends BlockEntity {
    private LazyOptional<IFluidHandler> inputCapability = LazyOptional.empty();

    public FluidDistributorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_DISTRIBUTOR.get(), pos, state);
        createCapabilityView();
    }

    @Nullable
    private AdsorptionColumnControllerBlockEntity formedController() {
        if (level == null) {
            return null;
        }
        AdsorptionColumnControllerBlockEntity controller =
                AdsorptionColumnStructure.findControllerEntity(level, worldPosition);
        return controller != null && controller.ownsDistributor(worldPosition)
                ? controller
                : null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(
            @NotNull Capability<T> capability,
            @Nullable Direction side
    ) {
        if (capability == ForgeCapabilities.FLUID_HANDLER
                && side == Direction.UP
                && formedController() != null) {
            return inputCapability.cast();
        }
        return super.getCapability(capability, side);
    }

    public void refreshCapabilityView() {
        inputCapability.invalidate();
        createCapabilityView();
        if (level != null) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }

    private void createCapabilityView() {
        inputCapability = LazyOptional.of(DistributorFluidHandler::new);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null) {
            AdsorptionColumnStructure.scheduleValidation(level, worldPosition);
        }
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        inputCapability.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        createCapabilityView();
    }

    private final class DistributorFluidHandler implements IFluidHandler {
        @Nullable
        private IFluidHandler delegate() {
            AdsorptionColumnControllerBlockEntity controller = formedController();
            return controller == null ? null : controller.dirtyInputTank();
        }

        @Override
        public int getTanks() {
            return delegate() == null ? 0 : 1;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            IFluidHandler delegate = delegate();
            return delegate == null ? FluidStack.EMPTY : delegate.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            IFluidHandler delegate = delegate();
            return delegate == null ? 0 : delegate.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            IFluidHandler delegate = delegate();
            return delegate != null && delegate.isFluidValid(tank, stack);
        }

        @Override
        public int fill(@NotNull FluidStack resource, FluidAction action) {
            IFluidHandler delegate = delegate();
            return delegate == null ? 0 : delegate.fill(resource, action);
        }

        @Override
        public @NotNull FluidStack drain(
                @NotNull FluidStack resource,
                FluidAction action
        ) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
