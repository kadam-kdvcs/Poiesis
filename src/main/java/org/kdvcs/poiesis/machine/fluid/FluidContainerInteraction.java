package org.kdvcs.poiesis.machine.fluid;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.templates.FluidTank;

/** Generic cursor-stack interaction for buckets, cells and other fluid containers. */
public final class FluidContainerInteraction {
    private FluidContainerInteraction() {
    }

    public static boolean interact(
            ServerPlayer player,
            AbstractContainerMenu menu,
            FluidTank tank
    ) {
        return interact(player, menu, tank, true, true);
    }

    public static boolean interact(
            ServerPlayer player,
            AbstractContainerMenu menu,
            FluidTank tank,
            boolean allowFillTank,
            boolean allowDrainTank
    ) {
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty()) {
            return false;
        }

        ItemStack single = carried.copy();
        single.setCount(1);
        IFluidHandlerItem container = FluidUtil.getFluidHandler(single).orElse(null);
        if (container == null) {
            return false;
        }

        FluidStack tankFluid = tank.getFluid();
        FluidStack containerFluid = firstFluid(container);
        boolean moved;

        if (containerFluid.isEmpty()) {
            moved = allowDrainTank
                    && !tankFluid.isEmpty()
                    && fillContainerFromTank(container, tank);
        } else if (tankFluid.isEmpty()
                || tankFluid.isFluidEqual(containerFluid)) {
            moved = allowFillTank && emptyContainerIntoTank(
                    container,
                    containerFluid,
                    tank
            );
        } else {
            return false;
        }

        if (!moved) {
            return false;
        }

        ItemStack result = container.getContainer().copy();
        if (carried.getCount() == 1) {
            menu.setCarried(result);
        } else {
            carried.shrink(1);
            menu.setCarried(carried);
            player.getInventory().placeItemBackInInventory(result);
        }

        menu.broadcastChanges();
        return true;
    }

    private static FluidStack firstFluid(IFluidHandlerItem container) {
        for (int tank = 0; tank < container.getTanks(); tank++) {
            FluidStack stack = container.getFluidInTank(tank);
            if (!stack.isEmpty()) {
                // Some handlers expose their mutable internal FluidStack.
                // Keep the interaction's fluid identity stable while the
                // container is drained in several rate-limited operations.
                return stack.copy();
            }
        }
        return FluidStack.EMPTY;
    }

    private static boolean fillContainerFromTank(
            IFluidHandlerItem container,
            FluidTank tank
    ) {
        FluidStack available = tank.drain(
                tank.getFluidAmount(),
                IFluidHandler.FluidAction.SIMULATE
        );
        int accepted = container.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return false;
        }

        /*
         * Some large portable tanks expose their transfer rate as the maximum
         * accepted by one fill call. A direct GUI interaction should instead
         * fill as much of the item's capacity as possible in this one click.
         */
        FluidStack drained = tank.drain(
                available.getAmount(),
                IFluidHandler.FluidAction.EXECUTE
        );
        int filled = fillCompletely(container, drained);

        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            tank.fill(remainder, IFluidHandler.FluidAction.EXECUTE);
        }
        return filled > 0;
    }

    private static boolean emptyContainerIntoTank(
            IFluidHandlerItem container,
            FluidStack containerFluid,
            FluidTank tank
    ) {
        int space = tank.getCapacity() - tank.getFluidAmount();
        if (space <= 0) {
            return false;
        }

        FluidStack requested = containerFluid.copy();
        requested.setAmount(space);
        FluidStack available = container.drain(
                requested,
                IFluidHandler.FluidAction.SIMULATE
        );
        int accepted = tank.fill(available, IFluidHandler.FluidAction.SIMULATE);
        if (accepted <= 0) {
            return false;
        }

        FluidStack drained = drainCompletely(
                container,
                containerFluid,
                space
        );
        int filled = tank.fill(drained, IFluidHandler.FluidAction.EXECUTE);

        if (filled < drained.getAmount()) {
            FluidStack remainder = drained.copy();
            remainder.setAmount(drained.getAmount() - filled);
            fillCompletely(container, remainder);
        }
        return filled > 0;
    }

    /**
     * Repeats rate-limited item fills as one atomic player interaction.
     */
    private static int fillCompletely(
            IFluidHandlerItem container,
            FluidStack resource
    ) {
        FluidStack remaining = resource.copy();
        int total = 0;

        while (!remaining.isEmpty()) {
            int filled = container.fill(
                    remaining,
                    IFluidHandler.FluidAction.EXECUTE
            );
            if (filled <= 0) {
                break;
            }

            int moved = Math.min(filled, remaining.getAmount());
            total += moved;
            remaining.shrink(moved);
        }

        return total;
    }

    /**
     * Repeats rate-limited item drains without mixing fluids from other tanks.
     */
    private static FluidStack drainCompletely(
            IFluidHandlerItem container,
            FluidStack fluid,
            int maximum
    ) {
        FluidStack expectedFluid = fluid.copy();
        FluidStack drainedTotal = FluidStack.EMPTY;
        int remaining = maximum;

        while (remaining > 0) {
            FluidStack requested = expectedFluid.copy();
            requested.setAmount(remaining);

            FluidStack drained = container.drain(
                    requested,
                    IFluidHandler.FluidAction.EXECUTE
            );
            if (drained.isEmpty()
                    || !drained.isFluidEqual(expectedFluid)) {
                break;
            }

            int moved = Math.min(drained.getAmount(), remaining);
            if (drainedTotal.isEmpty()) {
                drainedTotal = drained.copy();
                drainedTotal.setAmount(moved);
            } else {
                drainedTotal.grow(moved);
            }
            remaining -= moved;
        }

        return drainedTotal;
    }
}
