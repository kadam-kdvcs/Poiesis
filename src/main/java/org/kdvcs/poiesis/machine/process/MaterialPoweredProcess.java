package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.MaterialProcessRecipe;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Shared state machine for powered, counted-item material processing.
 *
 * It owns progress, recipe continuity, output blocking, power allocation and
 * atomic consumption. A machine-specific runtime only supplies its recipe type,
 * slot layout and module layout.
 */
public final class MaterialPoweredProcess<R extends MaterialProcessRecipe>
        implements MachineComponent {

    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;
    private final MaterialRecipeResolver<R> recipeResolver;
    private final int inputStart;
    private final int inputEndExclusive;
    private final int outputSlot;

    private int progress;
    private int maxProgress;

    private R activeRecipe;
    private ResourceLocation activeRecipeId;

    private long cachedCapabilityVersion = Long.MIN_VALUE;
    private boolean loadedStatePendingValidation;

    private ProcessState state = ProcessState.IDLE;
    private int activeEnergyPerTick;
    private int activeVoltage;

    public MaterialPoweredProcess(
            InventoryComponent inventory,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            MaterialRecipeResolver<R> recipeResolver,
            int inputStart,
            int inputEndExclusive,
            int outputSlot
    ) {
        this.inventory = inventory;
        this.modules = modules;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeResolver = recipeResolver;
        this.inputStart = inputStart;
        this.inputEndExclusive = inputEndExclusive;
        this.outputSlot = outputSlot;
    }

    @Override
    public String key() {
        return "process";
    }

    public void tick(
            Runnable changed,
            Consumer<Boolean> workingSetter
    ) {
        ModuleSnapshot moduleSnapshot = modules.snapshot();

        boolean capabilitiesChanged =
                cachedCapabilityVersion != modules.capabilityVersion();

        if (capabilitiesChanged) {
            cachedCapabilityVersion = modules.capabilityVersion();
            recipeResolver.invalidate();

            if (!loadedStatePendingValidation) {
                clearActiveRecipe();
                progress = 0;
                maxProgress = 0;
            }
        }

        R recipe = resolveActiveRecipe(moduleSnapshot);
        loadedStatePendingValidation = false;

        if (recipe == null) {
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        if (!hasRequiredCapability(recipe, moduleSnapshot)) {
            clearActiveRecipe();
            reset(ProcessState.MISSING_MODULE, workingSetter);
            return;
        }

        int[] consumptionPlan = findConsumptionPlan(recipe);
        if (consumptionPlan == null) {
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        if (!canAcceptResult(recipe.result())) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        PowerAllocation allocation = powerPolicy.allocate(
                energy,
                energy.spec(),
                recipe.baseEnergyPerTick(),
                moduleSnapshot.requestedSpeed(),
                moduleSnapshot.energyMultiplier()
        );

        if (!allocation.active()
                || !energy.consume(allocation.energyPerTick())) {
            pause(ProcessState.MISSING_ENERGY, workingSetter);
            return;
        }

        state = ProcessState.RUNNING;
        activeEnergyPerTick = allocation.energyPerTick();
        activeVoltage = allocation.operatingVoltage();

        maxProgress = Math.max(1, recipe.duration());
        progress = Math.min(
                maxProgress,
                progress + allocation.progressPerTick()
        );

        workingSetter.accept(true);
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        // Re-plan immediately before commit so external automation cannot cause
        // a partial consume/output transaction.
        consumptionPlan = findConsumptionPlan(recipe);
        if (consumptionPlan == null
                || !canAcceptResult(recipe.result())) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        commit(recipe, consumptionPlan);
        progress = 0;
        maxProgress = 0;
        changed.run();
    }

    private R resolveActiveRecipe(ModuleSnapshot moduleSnapshot) {
        Optional<R> matched = recipeResolver.resolve(moduleSnapshot);

        if (matched.isPresent()) {
            R matchedRecipe = matched.get();
            ResourceLocation matchedId = matchedRecipe.getId();

            if (activeRecipeId == null) {
                setActiveRecipe(matchedRecipe);
                return activeRecipe;
            }

            if (activeRecipeId.equals(matchedId)) {
                activeRecipe = matchedRecipe;
                maxProgress = Math.max(1, matchedRecipe.duration());
                progress = Math.min(Math.max(0, progress), maxProgress);
                return activeRecipe;
            }

            setActiveRecipe(matchedRecipe);
            progress = 0;
            return activeRecipe;
        }

        if (activeRecipe != null && inputsStillBelongTo(activeRecipe)) {
            return activeRecipe;
        }

        if (activeRecipe != null && !allInputSlotsEmpty()) {
            clearActiveRecipe();
            progress = 0;
            maxProgress = 0;
        }

        return activeRecipe;
    }

    private boolean hasRequiredCapability(
            R recipe,
            ModuleSnapshot snapshot
    ) {
        return recipe.requiredCapability() == null
                || snapshot.capabilities().contains(recipe.requiredCapability());
    }

    private boolean inputsStillBelongTo(R recipe) {
        for (int slot = inputStart; slot < inputEndExclusive; slot++) {
            ItemStack stack = inventory.items().getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            boolean belongs = recipe.inputs().stream()
                    .anyMatch(input -> input.ingredient().test(stack));

            if (!belongs) {
                return false;
            }
        }

        return true;
    }

    private boolean allInputSlotsEmpty() {
        for (int slot = inputStart; slot < inputEndExclusive; slot++) {
            if (!inventory.items().getStackInSlot(slot).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private int[] findConsumptionPlan(R recipe) {
        return MaterialPlan.find(
                inventory.items(),
                inputStart,
                inputEndExclusive,
                recipe.inputs()
        );
    }

    private void setActiveRecipe(R recipe) {
        activeRecipe = recipe;
        activeRecipeId = recipe.getId();
        maxProgress = Math.max(1, recipe.duration());
    }

    private void clearActiveRecipe() {
        activeRecipe = null;
        activeRecipeId = null;
    }

    private boolean canAcceptResult(ItemStack result) {
        ItemStack output = inventory.items().getStackInSlot(outputSlot);

        if (output.isEmpty()) {
            return result.getCount() <= result.getMaxStackSize();
        }

        return ItemStack.isSameItemSameTags(output, result)
                && output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void commit(R recipe, int[] consumptionPlan) {
        for (int offset = 0; offset < consumptionPlan.length; offset++) {
            int amount = consumptionPlan[offset];
            if (amount > 0) {
                inventory.items().extractItem(inputStart + offset, amount, false);
            }
        }

        ItemStack result = recipe.result();
        ItemStack output = inventory.items().getStackInSlot(outputSlot);

        if (output.isEmpty()) {
            inventory.items().setStackInSlot(outputSlot, result.copy());
            return;
        }

        ItemStack grown = output.copy();
        grown.grow(result.getCount());
        inventory.items().setStackInSlot(outputSlot, grown);
    }

    private void reset(ProcessState next, Consumer<Boolean> workingSetter) {
        progress = 0;
        maxProgress = 0;
        pause(next, workingSetter);
    }

    private void pause(ProcessState next, Consumer<Boolean> workingSetter) {
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
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        if (activeRecipeId != null) {
            tag.putString("active_recipe", activeRecipeId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(0, tag.getInt("max_progress"));
        activeRecipe = null;
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe"))
                : null;

        state = ProcessState.IDLE;
        activeEnergyPerTick = 0;
        activeVoltage = 0;
        cachedCapabilityVersion = Long.MIN_VALUE;
        loadedStatePendingValidation = true;
        recipeResolver.invalidate();
    }
}
