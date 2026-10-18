package org.kdvcs.poiesis.machine.energy.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;

public final class BatterySlotComponent
        implements MachineComponent {

    private static final int SLOT = 0;

    private final BatterySpec spec;
    private final EnergyComponent energy;
    private final ItemStackHandler items;
    private final Runnable changed;
    private final boolean machineChargesItem;

    private int pendingEnergy;

    public BatterySlotComponent(
            BatterySpec spec,
            EnergyComponent energy,
            Runnable changed
    ) {
        this(spec, energy, changed, false);
    }

    public BatterySlotComponent(
            BatterySpec spec,
            EnergyComponent energy,
            Runnable changed,
            boolean machineChargesItem
    ) {
        this.spec = spec;
        this.energy = energy;
        this.changed = changed;
        this.machineChargesItem = machineChargesItem;

        this.items = new ItemStackHandler(1) {
            @Override
            protected void onContentsChanged(
                    int slot
            ) {
                changed.run();
            }

            @Override
            public boolean isItemValid(
                    int slot,
                    ItemStack stack
            ) {
                return machineChargesItem
                        ? isChargeableBatteryItem(stack)
                        : isValidBatteryItem(stack);
            }

            @Override
            public int getSlotLimit(
                    int slot
            ) {
                return 64;
            }
        };
    }

    @Override
    public String key() {
        return "battery";
    }

    public ItemStackHandler items() {
        return items;
    }

    public int pendingEnergy() {
        return pendingEnergy;
    }

    public void tick() {
        if (machineChargesItem) {
            tickMachineChargesItem();
            return;
        }

        if (pendingEnergy > 0) {
            transferPendingEnergy();

            if (pendingEnergy > 0) {
                return;
            }
        }

        ItemStack stack =
                items.getStackInSlot(SLOT);

        if (stack.isEmpty()) {
            return;
        }

        if (tryTransferEnergyItem(stack)) {
            return;
        }

        tryConsumeFixedEnergyItem(stack);
    }

    private void tickMachineChargesItem() {
        ItemStack stack = items.getStackInSlot(SLOT);
        if (stack.isEmpty()) {
            return;
        }

        var optional = stack.getCapability(ForgeCapabilities.ENERGY);
        if (!optional.isPresent()) {
            return;
        }
        var itemEnergy = optional.orElseThrow(IllegalStateException::new);
        if (!itemEnergy.canReceive()) {
            return;
        }

        int itemCapacity = itemEnergy.receiveEnergy(Integer.MAX_VALUE, true);
        int request = Math.min(
                Math.min(energy.stored(), spec.transferPerTick()),
                Math.max(0, itemCapacity)
        );
        if (request <= 0) {
            return;
        }

        int itemAccepts = itemEnergy.receiveEnergy(request, true);
        int available = energy.extractInternalEnergy(itemAccepts, true);
        int transfer = Math.min(itemAccepts, available);
        if (transfer <= 0) {
            return;
        }

        int extracted = energy.extractInternalEnergy(transfer, false);
        int received = itemEnergy.receiveEnergy(extracted, false);
        if (received < extracted) {
            energy.produce(extracted - received, false);
        }
        if (received > 0 || extracted != received) {
            changed.run();
        }
    }

    private boolean tryTransferEnergyItem(
            ItemStack stack
    ) {
        var optional =
                stack.getCapability(
                        ForgeCapabilities.ENERGY
                );

        if (!optional.isPresent()) {
            return false;
        }

        var itemEnergy =
                optional.orElseThrow(
                        IllegalStateException::new
                );

        if (!itemEnergy.canExtract()) {
            return true;
        }

        int missing =
                energy.capacity()
                        - energy.stored();

        if (missing <= 0) {
            return true;
        }

        int request =
                Math.min(
                        missing,
                        spec.transferPerTick()
                );

        int machineAccepts =
                energy.view()
                        .receiveEnergy(
                                request,
                                true
                        );

        if (machineAccepts <= 0) {
            return true;
        }

        int available =
                itemEnergy.extractEnergy(
                        machineAccepts,
                        true
                );

        int transfer =
                Math.min(
                        machineAccepts,
                        available
                );

        if (transfer <= 0) {
            return true;
        }

        int extracted =
                itemEnergy.extractEnergy(
                        transfer,
                        false
                );

        if (extracted <= 0) {
            return true;
        }

        int received =
                energy.view()
                        .receiveEnergy(
                                extracted,
                                false
                        );

        if (received > 0) {
            changed.run();
        }

        return true;
    }

    private void tryConsumeFixedEnergyItem(
            ItemStack stack
    ) {
        int value =
                ConsumableEnergyRegistry
                        .energyValue(stack);

        if (value <= 0) {
            return;
        }

        if (pendingEnergy > 0) {
            return;
        }

        stack.shrink(1);

        pendingEnergy = value;

        changed.run();

        transferPendingEnergy();
    }

    private void transferPendingEnergy() {
        if (pendingEnergy <= 0) {
            return;
        }

        int request =
                Math.min(
                        pendingEnergy,
                        spec.transferPerTick()
                );

        int received =
                energy.view()
                        .receiveEnergy(
                                request,
                                false
                        );

        if (received <= 0) {
            return;
        }

        pendingEnergy -= received;

        changed.run();
    }

    public static boolean isValidBatteryItem(
            ItemStack stack
    ) {
        if (stack.isEmpty()) {
            return false;
        }

        if (ConsumableEnergyRegistry.contains(stack)) {
            return true;
        }

        return stack.getCapability(
                ForgeCapabilities.ENERGY
        ).map(
                energy -> energy.canExtract()
        ).orElse(false);
    }

    public static boolean isChargeableBatteryItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stack.getCapability(ForgeCapabilities.ENERGY)
                .map(energy -> energy.canReceive()
                        && energy.receiveEnergy(Integer.MAX_VALUE, true) > 0)
                .orElse(false);
    }

    @Override
    public void save(
            CompoundTag tag
    ) {
        tag.put(
                "item",
                items.serializeNBT()
        );

        tag.putInt(
                "pending_energy",
                pendingEnergy
        );
    }

    @Override
    public void load(
            CompoundTag tag
    ) {
        items.deserializeNBT(
                tag.getCompound("item")
        );

        pendingEnergy =
                tag.getInt(
                        "pending_energy"
                );
    }

    @Override
    public void invalidate() {}
}
