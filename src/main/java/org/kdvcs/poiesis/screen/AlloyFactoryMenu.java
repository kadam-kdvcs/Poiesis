package org.kdvcs.poiesis.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.SlotItemHandler;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.machine.alloy.AlloyFactoryProcess;
import org.kdvcs.poiesis.machine.energy.item.BatterySlotComponent;
import org.kdvcs.poiesis.machine.module.MachineModuleItem;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.alloy.AlloyDefinition;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.menu.AbstractMachineMenu;
import org.kdvcs.poiesis.screen.slot.BatterySlot;
import org.kdvcs.poiesis.screen.slot.ModuleSlot;
import org.kdvcs.poiesis.screen.slot.OutputSlot;

import java.util.List;

public final class AlloyFactoryMenu extends AbstractMachineMenu {
    public static final int FIRST_INPUT_MENU_SLOT = 0;
    public static final int INPUT_MENU_SLOT_COUNT = 18;
    private static final int INPUT_MENU_SLOT_END =
            FIRST_INPUT_MENU_SLOT + INPUT_MENU_SLOT_COUNT;
    private static final int FIRST_OUTPUT_MENU_SLOT = 18;
    private static final int OUTPUT_MENU_SLOT_END = 24;
    private static final int SPECIALIZATION_MENU_SLOT = 24;
    private static final int FIRST_PERFORMANCE_MENU_SLOT = 25;
    private static final int PERFORMANCE_MENU_SLOT_END = 27;
    private static final int BATTERY_MENU_SLOT = 27;
    private static final int MACHINE_SLOT_COUNT = 28;
    public static final int PLAYER_INVENTORY_MENU_SLOT = MACHINE_SLOT_COUNT;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = 36;
    private static final int DATA_COUNT = 9;

    private final AlloyFactoryBlockEntity blockEntity;
    private ResourceLocation selectedRecipeId;

    public AlloyFactoryMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(
                id,
                inventory,
                requireBlockEntity(inventory, buffer.readBlockPos()),
                new SimpleContainerData(DATA_COUNT),
                readRecipeId(buffer.readUtf())
        );
    }

    public AlloyFactoryMenu(
            int id,
            Inventory playerInventory,
            AlloyFactoryBlockEntity blockEntity,
            ContainerData data,
            ResourceLocation selectedRecipeId
    ) {
        super(
                ModMenuTypes.ALLOY_FACTORY.get(),
                id,
                playerInventory,
                data,
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()),
                ModBlocks.ALLOY_FACTORY.get(),
                MACHINE_SLOT_COUNT
        );
        this.blockEntity = blockEntity;
        this.selectedRecipeId = selectedRecipeId;
        checkContainerDataCount(data, DATA_COUNT);
        addMachineSlots();
        addPlayerInventory(48, 144, 202);
        addDataSlots(data);
    }

    private void addMachineSlots() {
        var items = blockEntity.runtime().inventory().items();
        for (int row = 0; row < 6; row++) {
            for (int column = 0; column < 3; column++) {
                int slot = row * 3 + column;
                addSlot(new SlotItemHandler(items, slot, 31 + column * 18, 19 + row * 18));
            }
        }
        for (int row = 0; row < 6; row++) {
            addSlot(new OutputSlot(
                    items,
                    AlloyFactoryProcess.FIRST_OUTPUT_SLOT + row,
                    203,
                    19 + row * 18
            ));
        }

        var modules = blockEntity.runtime().modules().items();
        addSlot(new ModuleSlot(modules, ModuleComponent.PROCESS_SLOT, 232, 19, ModuleKind.PROCESS));
        addSlot(new ModuleSlot(modules, ModuleComponent.FIRST_PERFORMANCE_SLOT, 232, 37, ModuleKind.PERFORMANCE));
        addSlot(new ModuleSlot(modules, ModuleComponent.SECOND_PERFORMANCE_SLOT, 232, 55, ModuleKind.PERFORMANCE));

        blockEntity.runtime().battery().ifPresent(battery ->
            addSlot(new BatterySlot(battery.items(), 0, 8, 82))
        );
    }

    @Override
    protected boolean movePlayerStackToMachine(ItemStack stack) {
        if (stack.getItem() instanceof MachineModuleItem module) {
            if (module.kind() == ModuleKind.PROCESS) {
                if (!blockEntity.runtime().modules().items().isItemValid(ModuleComponent.PROCESS_SLOT, stack)) {
                    return false;
                }
                return moveToMachineSlot(stack, SPECIALIZATION_MENU_SLOT);
            }
            if (module.kind() == ModuleKind.PERFORMANCE) {
                return moveToMachineSlots(stack, FIRST_PERFORMANCE_MENU_SLOT, PERFORMANCE_MENU_SLOT_END);
            }
        }
        if (BatterySlotComponent.isValidBatteryItem(stack)
                && moveToMachineSlot(stack, BATTERY_MENU_SLOT)) {
            return true;
        }
        return moveToMachineSlots(stack, FIRST_INPUT_MENU_SLOT, INPUT_MENU_SLOT_END);
    }

    public boolean canStart(AlloyDefinition recipe) {
        return !processing() && blockEntity.runtime().process().canStart(recipe);
    }

    public int craftableBatches(AlloyDefinition recipe) {
        int low = 0;
        int high = 1;
        while (high < 4096 && canSatisfy(recipe, high)) {
            low = high;
            high *= 2;
        }
        while (low + 1 < high) {
            int mid = low + (high - low) / 2;
            if (canSatisfy(recipe, mid)) low = mid;
            else high = mid;
        }
        return low;
    }

    private boolean canSatisfy(AlloyDefinition recipe, int batches) {
        List<CountedIngredient> scaled = recipe.inputs().stream()
                .map(value -> new CountedIngredient(
                        value.ingredient(),
                        Math.multiplyExact(value.count(), batches)
                ))
                .toList();
        return MaterialPlan.canSatisfy(
                blockEntity.runtime().inventory().items(),
                AlloyFactoryProcess.FIRST_INPUT_SLOT,
                AlloyFactoryProcess.INPUT_SLOT_END,
                scaled
        );
    }

    @Override public BlockPos machinePos() { return blockEntity.getBlockPos(); }
    public ResourceLocation selectedRecipeId() { return selectedRecipeId; }
    public void setSelectedRecipeId(ResourceLocation id) { selectedRecipeId = id; }
    public int progress() { return dataValue(0); }
    public int maxProgress() { return dataValue(1); }
    public int energy() { return int32DataValue(2, 7); }
    public int capacity() { return int32DataValue(3, 8); }
    public int activeEnergyPerTick() { return dataValue(4); }
    public boolean autoCrafting() { return dataValue(5) != 0; }
    public boolean processing() { return dataValue(6) != 0; }

    private static ResourceLocation readRecipeId(String value) {
        return value == null || value.isBlank() ? null : ResourceLocation.tryParse(value);
    }

    private static AlloyFactoryBlockEntity requireBlockEntity(Inventory inventory, BlockPos pos) {
        if (inventory.player.level().getBlockEntity(pos) instanceof AlloyFactoryBlockEntity factory) {
            return factory;
        }
        throw new IllegalStateException("Alloy Factory block entity is missing at " + pos);
    }
}
