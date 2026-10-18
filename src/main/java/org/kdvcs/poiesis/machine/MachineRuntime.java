package org.kdvcs.poiesis.machine;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.ComponentContainer;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.MachineCapabilityRouter;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import org.kdvcs.poiesis.machine.io.transfer.EnergyAutoTransfer;
import org.kdvcs.poiesis.machine.io.transfer.ItemAutoTransfer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

/**
 * Common runtime shell for single-block machines.
 *
 * It owns infrastructure that must not be reimplemented by each machine:
 * inventory, energy, battery charging, I/O configuration, capability routing,
 * automatic transfer, persistence and invalidation. Subclasses only provide
 * their domain process and optional extra drop inventories.
 */
public abstract class MachineRuntime {

    protected final MachineContext owner;
    protected final MachineDefinition definition;
    protected final ComponentContainer components = new ComponentContainer();

    private final InventoryComponent inventory;
    private final EnergyComponent energy;
    private final BatterySlotComponent battery;
    private final FluidComponent fluid;
    private final SideConfigurationComponent sideConfiguration;
    private final AutoIoComponent autoIo;
    private final MachineCapabilityRouter capabilityRouter;

    protected MachineRuntime(
            MachineContext owner,
            MachineDefinition definition
    ) {
        this(owner, definition, (slot, stack) -> true);
    }

    protected MachineRuntime(
            MachineContext owner,
            MachineDefinition definition,
            BiPredicate<Integer, ItemStack> inputValidator
    ) {
        this.owner = owner;
        this.definition = definition;

        Runnable changed = owner::markPersistentDirty;

        inventory = components.add(
                InventoryComponent.class,
                new InventoryComponent(
                        definition.inputSlots() + definition.outputSlots(),
                        slot -> slot >= definition.inputSlots(),
                        inputValidator,
                        changed
                )
        );

        energy = components.add(
                EnergyComponent.class,
                new EnergyComponent(
                        definition.energy(),
                        changed
                )
        );

        battery = definition.hasBatterySlot()
                ? components.add(
                BatterySlotComponent.class,
                createBatteryComponent(definition.battery(), energy, changed)
        )
                : null;

        fluid = definition.hasFluidTank()
                ? components.add(
                FluidComponent.class,
                new FluidComponent(definition.fluid(), changed)
        )
                : null;

        if (definition.io().isEmpty()) {
            sideConfiguration = null;
            autoIo = null;
            capabilityRouter = null;
        } else {
            sideConfiguration = components.add(
                    SideConfigurationComponent.class,
                    new SideConfigurationComponent(definition.io(), changed)
            );

            autoIo = components.add(
                    AutoIoComponent.class,
                    new AutoIoComponent(definition.io(), changed)
            );

            capabilityRouter = new MachineCapabilityRouter(
                    owner,
                    definition,
                    inventory,
                    energy,
                    fluid,
                    sideConfiguration,
                    this::isFluidCapabilityEnabled
            );
        }
    }

    /** Allows specialized machines to reverse the direction of battery transfer. */
    protected BatterySlotComponent createBatteryComponent(
            BatterySpec spec,
            EnergyComponent energy,
            Runnable changed
    ) {
        return new BatterySlotComponent(spec, energy, changed);
    }

    /** Called by the constructor of a subclass after its own process is ready. */
    protected final <T extends MachineComponent> T addComponent(
            Class<T> type,
            T component
    ) {
        return components.add(type, component);
    }

    public final void tickServer() {
        boolean transferTick = shouldRunAutoIo();

        if (transferTick) {
            runAutoInputs();
        }

        if (battery != null) {
            battery.tick();
        }

        tickProcess();

        if (transferTick) {
            runAutoOutputs();
        }
    }

    protected abstract void tickProcess();

    /** Machines with module-gated tanks override this dynamic capability lock. */
    protected boolean isFluidCapabilityEnabled() {
        return fluid != null;
    }

    private boolean shouldRunAutoIo() {
        return owner.level() != null
                && !owner.level().isClientSide
                && canRunAutoIo()
                && owner.level().getGameTime() % 5L == 0L;
    }

    /** Multiblock runtimes can suspend active transfer while their structure is invalid. */
    protected boolean canRunAutoIo() {
        return true;
    }

    /** Allows machines with restricted ports to exclude physical sides from active transfer. */
    protected boolean canUseAutoIoSide(RelativeSide side) {
        return true;
    }

    private void runAutoInputs() {
        if (autoIo == null || sideConfiguration == null) {
            return;
        }

        if (autoIo.enabled(IoChannel.ITEM, AutoIoDirection.INPUT)) {
            ItemAutoTransfer.pullInputs(
                    owner,
                    definition,
                    inventory,
                    sideConfiguration,
                    this::canUseAutoIoSide
            );
        }

        if (autoIo.enabled(IoChannel.ENERGY, AutoIoDirection.INPUT)) {
            EnergyAutoTransfer.pullEnergy(
                    owner,
                    energy,
                    sideConfiguration,
                    this::canUseAutoIoSide
            );
        }

        runAdditionalAutoInputs();
    }

    private void runAutoOutputs() {
        if (autoIo == null || sideConfiguration == null) {
            return;
        }

        if (autoIo.enabled(IoChannel.ITEM, AutoIoDirection.OUTPUT)) {
            ItemAutoTransfer.pushOutputs(
                    owner,
                    definition,
                    inventory,
                    sideConfiguration,
                    this::canUseAutoIoSide
            );
        }

        runAdditionalAutoOutputs();
    }

    /** Optional machine-specific active inputs, such as a multiblock's fixed fluid port. */
    protected void runAdditionalAutoInputs() {
    }

    /** Optional machine-specific active outputs backed by nonstandard tanks. */
    protected void runAdditionalAutoOutputs() {
    }

    public void save(CompoundTag tag) {
        tag.putInt("machine_data_version", dataVersion());
        components.save(tag);
    }

    protected int dataVersion() {
        return 5;
    }

    public void load(CompoundTag tag) {
        components.load(tag);
    }

    public void invalidate() {
        if (capabilityRouter != null) {
            capabilityRouter.invalidate();
        }
        components.invalidate();
    }

    public void refreshIoCapabilities() {
        if (capabilityRouter != null) {
            capabilityRouter.invalidate();
        }
        owner.markSyncDirty();
    }

    public SimpleContainer allDrops() {
        List<IItemHandler> handlers = new ArrayList<>();
        handlers.add(inventory.items());
        handlers.addAll(additionalDropHandlers());
        if (battery != null) {
            handlers.add(battery.items());
        }

        int slots = handlers.stream().mapToInt(IItemHandler::getSlots).sum();
        SimpleContainer drops = new SimpleContainer(slots);

        int index = 0;
        for (IItemHandler handler : handlers) {
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                drops.setItem(index++, handler.getStackInSlot(slot));
            }
        }

        return drops;
    }

    protected List<IItemHandler> additionalDropHandlers() {
        return List.of();
    }

    public InventoryComponent inventory() {
        return inventory;
    }

    public EnergyComponent energy() {
        return energy;
    }

    public Optional<BatterySlotComponent> battery() {
        return Optional.ofNullable(battery);
    }

    public Optional<FluidComponent> fluid() {
        return Optional.ofNullable(fluid);
    }

    public Optional<SideConfigurationComponent> sideConfiguration() {
        return Optional.ofNullable(sideConfiguration);
    }

    public Optional<AutoIoComponent> autoIo() {
        return Optional.ofNullable(autoIo);
    }

    public Optional<MachineCapabilityRouter> capabilityRouter() {
        return Optional.ofNullable(capabilityRouter);
    }

    public MachineDefinition definition() {
        return definition;
    }
}
