package org.kdvcs.poiesis.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.ModuleFabricatorBlock;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.definition.BatterySpec;
import org.kdvcs.poiesis.machine.definition.EnergySpec;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.fabrication.ModuleFabricatorRuntime;
import org.kdvcs.poiesis.machine.io.AutoIoComponent;
import org.kdvcs.poiesis.machine.io.MachineIoSpec;
import org.kdvcs.poiesis.machine.io.SideConfigurationComponent;
import org.kdvcs.poiesis.registry.ModBlockEntities;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.screen.ModuleFabricatorMenu;

import java.util.Optional;

public final class ModuleFabricatorBlockEntity extends BlockEntity implements MenuProvider, MachineContext, MachineIoHost {
    public static final MachineDefinition DEFINITION = new MachineDefinition(
            "module_fabricator",
            new EnergySpec(40_000, 640, 20, 40, 320, 20),
            10,
            1,
            0,
            MachineIoSpec.builder().item().energyInput().build(),
            new BatterySpec(400)
    );

    private final ModuleFabricatorRuntime runtime = new ModuleFabricatorRuntime(this, DEFINITION);
    private final ContainerData data = new ContainerData() {
        @Override public int get(int index) {
            return switch (index) {
                case 0 -> runtime.process().progress();
                case 1 -> runtime.process().maxProgress();
                case 2 -> runtime.energy().stored();
                case 3 -> runtime.energy().capacity();
                case 4 -> runtime.process().activeEnergyPerTick();
                case 5 -> runtime.process().autoCrafting() ? 1 : 0;
                case 6 -> runtime.process().isProcessing() ? 1 : 0;
                default -> 0;
            };
        }
        @Override public void set(int index, int value) {}
        @Override public int getCount() { return 7; }
    };

    public ModuleFabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MODULE_FABRICATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ModuleFabricatorBlockEntity blockEntity) {
        blockEntity.runtime.tickServer();
    }

    public ModuleFabricatorRuntime runtime() { return runtime; }
    @Override public MachineDefinition machineDefinition() { return DEFINITION; }
    @Override public Optional<SideConfigurationComponent> sideConfiguration() { return runtime.sideConfiguration(); }
    @Override public Optional<AutoIoComponent> autoIo() { return runtime.autoIo(); }
    @Override
    public void refreshIoCapabilities() {
        runtime.refreshIoCapabilities();

        if (level != null && !level.isClientSide) {
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        }
    }
    @Override public Component machineDisplayName() { return getDisplayName(); }
    @Override public Level level() { return level; }
    @Override public BlockPos pos() { return worldPosition; }
    @Override public Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(ModuleFabricatorBlock.FACING) ? state.getValue(ModuleFabricatorBlock.FACING) : Direction.NORTH;
    }
    @Override public void markPersistentDirty() { setChanged(); }
    @Override public void markSyncDirty() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public void setWorking(boolean working) {
        if (level == null) return;
        BlockState state = getBlockState();
        if (state.getValue(ModuleFabricatorBlock.WORKING) != working) {
            level.setBlock(worldPosition, state.setValue(ModuleFabricatorBlock.WORKING, working), 3);
        }
    }

    public SimpleContainer allDrops() {
        int inventorySlots =
                runtime.inventory()
                        .items()
                        .getSlots();

        int batterySlots =
                runtime.battery()
                        .isPresent()
                        ? 1
                        : 0;

        SimpleContainer drops =
                new SimpleContainer(
                        inventorySlots + batterySlots
                );

        for (int slot = 0; slot < inventorySlots; slot++) {
            drops.setItem(
                    slot,
                    runtime.inventory()
                            .items()
                            .getStackInSlot(slot)
            );
        }

        runtime.battery().ifPresent(
                battery ->
                        drops.setItem(
                                inventorySlots,
                                battery.items()
                                        .getStackInSlot(0)
                        )
        );

        return drops;
    }

    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag); runtime.save(tag); }
    @Override public void load(CompoundTag tag) { super.load(tag); runtime.load(tag); }
    @Override public void invalidateCaps() { super.invalidateCaps(); runtime.invalidate(); }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction side) {
        if (capability == ForgeCapabilities.ITEM_HANDLER) return runtime.capabilityRouter().itemCapability(side).cast();
        if (capability == ForgeCapabilities.ENERGY) return runtime.capabilityRouter().energyCapability(side).cast();
        return super.getCapability(capability, side);
    }

    @Override public Component getDisplayName() { return ModBlocks.MODULE_FABRICATOR.get().getName(); }
    @Nullable @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new ModuleFabricatorMenu(id, inventory, this, data, runtime.process().selectedRecipeId());
    }
}
