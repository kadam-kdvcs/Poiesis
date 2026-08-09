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

    /*
     * 一次性能源物品被消耗后，
     * 尚未完全注入机器的剩余能量。
     *
     * 这样即使一个物品价值 10000 FE，
     * 机器每 tick 只能接收 400 FE，
     * 也不会损失剩余的 9600 FE。
     */
    private int pendingEnergy;

    public BatterySlotComponent(
            BatterySpec spec,
            EnergyComponent energy,
            Runnable changed
    ) {
        this.spec = spec;
        this.energy = energy;
        this.changed = changed;

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
                return isValidBatteryItem(stack);
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
        /*
         * 如果之前已经消耗了一个固定能源物品，
         * 优先把它剩余的能量送入机器。
         */
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

        /*
         * 优先识别真正拥有 Forge ENERGY Capability
         * 的能源物品。
         */
        if (tryTransferEnergyItem(stack)) {
            return;
        }

        /*
         * 没有 ENERGY Capability 时，
         * 再查询固定能量物品注册表。
         */
        tryConsumeFixedEnergyItem(stack);
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

        /*
         * 先询问机器这一 tick 实际能接收多少。
         * 这样仍然遵守 EnergySpec.maxReceive。
         */
        int machineAccepts =
                energy.view()
                        .receiveEnergy(
                                request,
                                true
                        );

        if (machineAccepts <= 0) {
            return true;
        }

        /*
         * 再询问电池能实际提供多少。
         */
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
            /*
             * ItemStack 内部能源 NBT 改变，
             * 但这并不会触发 ItemStackHandler 的
             * onContentsChanged，所以手动标脏。
             */
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

        /*
         * 当前还有缓存能量时不继续吃下一个。
         */
        if (pendingEnergy > 0) {
            return;
        }

        stack.shrink(1);

        pendingEnergy = value;

        changed.run();

        /*
         * 消耗后当前 tick 就可以开始送电。
         */
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
    public void invalidate() {
    }
}