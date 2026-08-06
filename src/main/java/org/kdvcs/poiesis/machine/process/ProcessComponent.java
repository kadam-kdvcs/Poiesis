package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class ProcessComponent
        implements MachineComponent {

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;

    private final Supplier<
            Optional<MechanicalProcessingRecipe>
            > recipeLookup;

    private int progress;
    private int maxProgress;

    private MechanicalProcessingRecipe activeRecipe;
    private ResourceLocation activeRecipeId;

    private long cachedProcessModuleVersion =
            Long.MIN_VALUE;

    private boolean loadedStatePendingValidation;

    private ProcessState state =
            ProcessState.IDLE;

    private int activeEnergyPerTick;
    private int activeVoltage;

    public ProcessComponent(
            InventoryComponent inventory,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            Supplier<
                    Optional<MechanicalProcessingRecipe>
                    > recipeLookup
    ) {
        this.inventory = inventory;
        this.modules = modules;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeLookup = recipeLookup;
    }

    @Override
    public String key() {
        return "process";
    }

    public void tick(
            Runnable changed,
            Consumer<Boolean> workingSetter
    ) {
        ModuleSnapshot moduleSnapshot =
                modules.snapshot();

        if (!moduleSnapshot.hasProcessModule()) {
            clearActiveRecipe();

            reset(
                    ProcessState.MISSING_MODULE,
                    workingSetter
            );

            return;
        }

        boolean processModuleChanged =
                cachedProcessModuleVersion
                        != modules.processVersion();

        if (processModuleChanged) {
            cachedProcessModuleVersion =
                    modules.processVersion();

            /*
             * 世界载入后的第一次验证不能直接清空进度。
             * 运行过程中更换工艺模块则必须重新选择配方。
             */
            if (!loadedStatePendingValidation) {
                clearActiveRecipe();
                progress = 0;
                maxProgress = 0;
            }
        }

        MechanicalProcessingRecipe recipe =
                resolveActiveRecipe();

        loadedStatePendingValidation = false;

        if (recipe == null) {
            pause(
                    ProcessState.IDLE,
                    workingSetter
            );

            return;
        }

        if (!moduleSnapshot.capabilities()
                .contains(recipe.requiredCapability())) {
            clearActiveRecipe();

            reset(
                    ProcessState.MISSING_MODULE,
                    workingSetter
            );

            return;
        }

        if (!hasRequiredInput(recipe)) {
            pause(
                    ProcessState.IDLE,
                    workingSetter
            );

            return;
        }

        if (!canAcceptResult(recipe.result())) {
            pause(
                    ProcessState.BLOCKED_OUTPUT,
                    workingSetter
            );

            return;
        }

        PowerAllocation allocation =
                powerPolicy.allocate(
                        energy,
                        energy.spec(),
                        recipe.baseEnergyPerTick(),
                        moduleSnapshot.requestedSpeed(),
                        moduleSnapshot.energyMultiplier()
                );

        if (!allocation.active()) {
            pause(
                    ProcessState.MISSING_ENERGY,
                    workingSetter
            );

            return;
        }

        if (!energy.consume(
                allocation.energyPerTick()
        )) {
            pause(
                    ProcessState.MISSING_ENERGY,
                    workingSetter
            );

            return;
        }

        state = ProcessState.RUNNING;
        activeEnergyPerTick =
                allocation.energyPerTick();
        activeVoltage =
                allocation.operatingVoltage();

        maxProgress = Math.max(
                1,
                recipe.duration()
        );

        progress = Math.min(
                maxProgress,
                progress
                        + allocation.progressPerTick()
        );

        workingSetter.accept(true);
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        if (!hasRequiredInput(recipe)
                || !canAcceptResult(recipe.result())) {
            pause(
                    ProcessState.BLOCKED_OUTPUT,
                    workingSetter
            );

            return;
        }

        commit(recipe);

        progress = 0;
        maxProgress = 0;

        /*
         * 加工完成后不必立即丢失 activeRecipe。
         * 如果槽内仍是同一种输入，下一轮可直接继续。
         * 下一 tick 会重新核对当前可用配方。
         */
        changed.run();
    }

    private MechanicalProcessingRecipe resolveActiveRecipe() {
        Optional<MechanicalProcessingRecipe> matched =
                recipeLookup.get();

        if (matched.isPresent()) {
            MechanicalProcessingRecipe matchedRecipe =
                    matched.get();

            ResourceLocation matchedId =
                    matchedRecipe.getId();

            if (activeRecipeId == null) {
                setActiveRecipe(matchedRecipe);
                return activeRecipe;
            }

            if (activeRecipeId.equals(matchedId)) {
                activeRecipe = matchedRecipe;

                maxProgress = Math.max(
                        1,
                        matchedRecipe.duration()
                );

                progress = Math.min(
                        Math.max(0, progress),
                        maxProgress
                );

                return activeRecipe;
            }

            /*
             * 输入现在匹配另一条配方。
             * 这才属于真正的加工目标变化。
             */
            setActiveRecipe(matchedRecipe);

            progress = 0;
            maxProgress = Math.max(
                    1,
                    matchedRecipe.duration()
            );

            return activeRecipe;
        }

        /*
         * 当前数量不足时 recipeLookup 可能查不到配方。
         * 此时保留当前加工目标与进度，等待材料补回。
         */
        if (activeRecipe != null
                && inputStillBelongsTo(activeRecipe)) {
            return activeRecipe;
        }

        /*
         * 当前输入既不匹配新配方，也不再属于旧配方。
         * 例如 A 被替换成了完全不同的 C。
         */
        if (activeRecipe != null
                && !inputSlotIsEmpty()) {
            clearActiveRecipe();
            progress = 0;
            maxProgress = 0;
        }

        return activeRecipe;
    }

    private boolean inputStillBelongsTo(
            MechanicalProcessingRecipe recipe
    ) {
        ItemStack input =
                inventory.items()
                        .getStackInSlot(INPUT_SLOT);

        if (input.isEmpty()) {
            return true;
        }

        /*
         * 这里只检查物品是否属于 Ingredient，
         * 不检查数量是否足够。
         */
        return recipe.input()
                .ingredient()
                .test(input);
    }

    private boolean hasRequiredInput(
            MechanicalProcessingRecipe recipe
    ) {
        ItemStack input =
                inventory.items()
                        .getStackInSlot(INPUT_SLOT);

        return recipe.input().test(input);
    }

    private boolean inputSlotIsEmpty() {
        return inventory.items()
                .getStackInSlot(INPUT_SLOT)
                .isEmpty();
    }

    private void setActiveRecipe(
            MechanicalProcessingRecipe recipe
    ) {
        activeRecipe = recipe;
        activeRecipeId = recipe.getId();

        maxProgress = Math.max(
                1,
                recipe.duration()
        );
    }

    private void clearActiveRecipe() {
        activeRecipe = null;
        activeRecipeId = null;
    }

    private boolean canAcceptResult(
            ItemStack result
    ) {
        ItemStack output =
                inventory.items()
                        .getStackInSlot(OUTPUT_SLOT);

        if (output.isEmpty()) {
            return result.getCount()
                    <= result.getMaxStackSize();
        }

        return ItemStack.isSameItemSameTags(
                output,
                result
        ) && output.getCount()
                + result.getCount()
                <= output.getMaxStackSize();
    }

    private void commit(
            MechanicalProcessingRecipe recipe
    ) {
        inventory.items().extractItem(
                INPUT_SLOT,
                recipe.input().count(),
                false
        );

        ItemStack output =
                inventory.items()
                        .getStackInSlot(OUTPUT_SLOT);

        ItemStack result =
                recipe.result();

        if (output.isEmpty()) {
            inventory.items().setStackInSlot(
                    OUTPUT_SLOT,
                    result.copy()
            );

            return;
        }

        ItemStack grown = output.copy();
        grown.grow(result.getCount());

        inventory.items().setStackInSlot(
                OUTPUT_SLOT,
                grown
        );
    }

    private void reset(
            ProcessState next,
            Consumer<Boolean> workingSetter
    ) {
        progress = 0;
        maxProgress = 0;

        pause(
                next,
                workingSetter
        );
    }

    private void pause(
            ProcessState next,
            Consumer<Boolean> workingSetter
    ) {
        state = next;
        activeEnergyPerTick = 0;
        activeVoltage = 0;

        workingSetter.accept(false);
    }

    public int progress() {
        return progress;
    }

    public int maxProgress() {
        return maxProgress;
    }

    public ProcessState state() {
        return state;
    }

    public int activeEnergyPerTick() {
        return activeEnergyPerTick;
    }

    public int activeVoltage() {
        return activeVoltage;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt(
                "progress",
                progress
        );

        tag.putInt(
                "max_progress",
                maxProgress
        );

        if (activeRecipeId != null) {
            tag.putString(
                    "active_recipe",
                    activeRecipeId.toString()
            );
        }
    }

    @Override
    public void load(CompoundTag tag) {
        progress = Math.max(
                0,
                tag.getInt("progress")
        );

        maxProgress = Math.max(
                0,
                tag.getInt("max_progress")
        );

        activeRecipe = null;

        if (tag.contains("active_recipe")) {
            activeRecipeId = ResourceLocation.tryParse(
                    tag.getString("active_recipe")
            );
        } else {
            activeRecipeId = null;
        }

        state = ProcessState.IDLE;
        activeEnergyPerTick = 0;
        activeVoltage = 0;

        cachedProcessModuleVersion =
                Long.MIN_VALUE;

        loadedStatePendingValidation = true;
    }
}