package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.recipe.ChanceOutput;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class SeparatorPoweredProcess implements MachineComponent {
    private final InventoryComponent inventory;
    private final FluidComponent fluid;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;
    private final SeparatorRecipeResolver recipeResolver;

    private int progress;
    private int maxProgress;
    private SeparatorRecipe activeRecipe;
    private ResourceLocation activeRecipeId;
    private long cachedCapabilityVersion = Long.MIN_VALUE;
    private boolean loadedStatePendingValidation;
    private ProcessState state = ProcessState.IDLE;
    private int activeEnergyPerTick;
    private int activeVoltage;

    public SeparatorPoweredProcess(
            InventoryComponent inventory,
            FluidComponent fluid,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            SeparatorRecipeResolver recipeResolver
    ) {
        this.inventory = inventory;
        this.fluid = fluid;
        this.modules = modules;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeResolver = recipeResolver;
    }

    @Override
    public String key() {
        return "process";
    }

    public void tick(
            Runnable changed,
            Consumer<Boolean> workingSetter,
            RandomSource random
    ) {
        ModuleSnapshot snapshot = modules.snapshot();
        if (cachedCapabilityVersion != modules.capabilityVersion()) {
            cachedCapabilityVersion = modules.capabilityVersion();
            recipeResolver.invalidate();

            if (!loadedStatePendingValidation) {
                clearActiveRecipe();
                progress = 0;
                maxProgress = 0;
            }
        }

        SeparatorRecipe recipe = resolveActiveRecipe(snapshot);
        loadedStatePendingValidation = false;

        if (recipe == null) {
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        int[] consumptionPlan = MaterialPlan.find(
                inventory.items(),
                0,
                SeparatorRecipe.MAX_INPUTS,
                recipe.inputs()
        );

        if (consumptionPlan == null || !hasRequiredFluid(recipe)) {
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        if (!canAcceptPotentialOutputs(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        PowerAllocation allocation = powerPolicy.allocate(
                energy,
                energy.spec(),
                recipe.baseEnergyPerTick(),
                snapshot.requestedSpeed(),
                snapshot.energyMultiplier()
        );

        if (!allocation.active() || !energy.consume(allocation.energyPerTick())) {
            pause(ProcessState.MISSING_ENERGY, workingSetter);
            return;
        }

        state = ProcessState.RUNNING;
        activeEnergyPerTick = allocation.energyPerTick();
        activeVoltage = allocation.operatingVoltage();
        maxProgress = Math.max(1, recipe.duration());
        progress = Math.min(maxProgress, progress + allocation.progressPerTick());
        workingSetter.accept(true);
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        consumptionPlan = MaterialPlan.find(
                inventory.items(),
                0,
                SeparatorRecipe.MAX_INPUTS,
                recipe.inputs()
        );

        if (consumptionPlan == null
                || !hasRequiredFluid(recipe)
                || !canAcceptPotentialOutputs(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        ItemOutputTransaction.Plan outputPlan = planRolledOutputs(
                recipe,
                random
        ).orElse(null);
        if (outputPlan == null
                || !commit(recipe, consumptionPlan, outputPlan)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        progress = 0;
        maxProgress = 0;
        recipeResolver.invalidate();
        changed.run();
    }

    private SeparatorRecipe resolveActiveRecipe(ModuleSnapshot snapshot) {
        Optional<SeparatorRecipe> matched = recipeResolver.resolve(snapshot);
        if (matched.isEmpty()) {
            return null;
        }

        SeparatorRecipe recipe = matched.get();
        if (activeRecipeId == null || !activeRecipeId.equals(recipe.getId())) {
            activeRecipe = recipe;
            activeRecipeId = recipe.getId();
            progress = 0;
        } else {
            activeRecipe = recipe;
        }

        maxProgress = Math.max(1, recipe.duration());
        progress = Math.min(Math.max(0, progress), maxProgress);
        return activeRecipe;
    }

    private boolean hasRequiredFluid(SeparatorRecipe recipe) {
        return recipe.fluid() == null || recipe.fluid().matches(fluid.fluid());
    }

    private boolean canAcceptPotentialOutputs(SeparatorRecipe recipe) {
        List<ItemStack> potentialOutputs = new ArrayList<>();
        for (ChanceOutput output : recipe.outputs()) {
            if (output.chance() > 0.0D) {
                potentialOutputs.add(output.stack());
            }
        }

        return planOutputs(potentialOutputs).isPresent();
    }

    private Optional<ItemOutputTransaction.Plan> planRolledOutputs(
            SeparatorRecipe recipe,
            RandomSource random
    ) {
        List<ItemStack> rolledOutputs = new ArrayList<>();
        for (ChanceOutput output : recipe.outputs()) {
            if (output.chance() <= 0.0D) {
                continue;
            }
            if (output.chance() >= 1.0D
                    || random.nextDouble() < output.chance()) {
                rolledOutputs.add(output.stack());
            }
        }
        return planOutputs(rolledOutputs);
    }

    private Optional<ItemOutputTransaction.Plan> planOutputs(
            Iterable<ItemStack> outputs
    ) {
        return ItemOutputTransaction.plan(
                inventory.items(),
                SeparatorRecipe.MAX_INPUTS,
                SeparatorRecipe.MAX_INPUTS + SeparatorRecipe.MAX_OUTPUTS,
                outputs
        );
    }

    private boolean commit(
            SeparatorRecipe recipe,
            int[] consumptionPlan,
            ItemOutputTransaction.Plan outputPlan
    ) {
        if (!outputPlan.matches(inventory.items())) {
            return false;
        }

        for (int slot = 0; slot < consumptionPlan.length; slot++) {
            int amount = consumptionPlan[slot];
            if (amount > 0) {
                inventory.items().extractItem(slot, amount, false);
            }
        }

        if (recipe.fluid() != null) {
            fluid.tank().drain(
                    recipe.fluid().amount(),
                    IFluidHandler.FluidAction.EXECUTE
            );
        }

        if (!outputPlan.commit(inventory.items())) {
            throw new IllegalStateException(
                    "Separator output inventory changed during an atomic commit"
            );
        }
        return true;
    }

    private void clearActiveRecipe() {
        activeRecipe = null;
        activeRecipeId = null;
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
