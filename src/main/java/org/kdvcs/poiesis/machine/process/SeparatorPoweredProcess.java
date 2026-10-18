package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.machine.separator.ElectrodeComponent;
import org.kdvcs.poiesis.machine.transaction.MachineBatchTransaction;
import org.kdvcs.poiesis.recipe.ChanceOutput;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModFluids;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public final class SeparatorPoweredProcess implements MachineComponent {
    private final InventoryComponent inventory;
    private final FluidComponent fluid;
    private final OutputFluidComponent outputFluid;
    private final ElectrodeComponent electrode;
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
            OutputFluidComponent outputFluid,
            ElectrodeComponent electrode,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            SeparatorRecipeResolver recipeResolver
    ) {
        this.inventory = inventory;
        this.fluid = fluid;
        this.outputFluid = outputFluid;
        this.electrode = electrode;
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

        if (consumptionPlan == null
                || !hasRequiredFluid(recipe)
                || !hasRequiredElectrode(recipe)) {
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
                || !hasRequiredElectrode(recipe)
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
            clearActiveRecipe();
            progress = 0;
            maxProgress = 0;
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

    private boolean hasRequiredElectrode(SeparatorRecipe recipe) {
        return !ModItems.ELECTROSTATIC_SEPARATION.equals(recipe.requiredCapability())
                || electrode.hasElectrode();
    }

    private boolean canAcceptPotentialOutputs(SeparatorRecipe recipe) {
        List<ItemStack> potentialOutputs = new ArrayList<>();
        for (ChanceOutput output : recipe.outputs()) {
            if (output.chance() > 0.0D) {
                potentialOutputs.add(output.stack());
            }
        }

        return planOutputs(recipe, potentialOutputs).isPresent()
                && canAcceptWastewater(recipe);
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
        return planOutputs(recipe, rolledOutputs);
    }

    private Optional<ItemOutputTransaction.Plan> planOutputs(
            SeparatorRecipe recipe,
            Iterable<ItemStack> outputs
    ) {
        int outputSlots = isRinsing(recipe)
                ? SeparatorRecipe.MAX_OUTPUTS - 1
                : SeparatorRecipe.MAX_OUTPUTS;
        return ItemOutputTransaction.plan(
                inventory.items(),
                SeparatorRecipe.MAX_INPUTS,
                SeparatorRecipe.MAX_INPUTS + outputSlots,
                outputs
        );
    }

    private boolean isRinsing(SeparatorRecipe recipe) {
        return ModItems.RINSING.equals(recipe.requiredCapability());
    }

    private FluidStack wastewater(SeparatorRecipe recipe) {
        if (!isRinsing(recipe) || recipe.fluid() == null) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(
                ModFluids.WASTEWATER.get(),
                recipe.fluid().amount()
        );
    }

    private boolean canAcceptWastewater(SeparatorRecipe recipe) {
        FluidStack waste = wastewater(recipe);
        return waste.isEmpty()
                || outputFluid.tank().fill(
                waste,
                IFluidHandler.FluidAction.SIMULATE
        ) == waste.getAmount();
    }

    private boolean commit(
            SeparatorRecipe recipe,
            int[] consumptionPlan,
            ItemOutputTransaction.Plan outputPlan
    ) {
        FluidStack waste = wastewater(recipe);
        if (!outputPlan.matches(inventory.items())
                || !canAcceptWastewater(recipe)) {
            return false;
        }

        if (recipe.fluid() != null) {
            FluidStack simulated = fluid.tank().drain(
                    recipe.fluid().amount(),
                    IFluidHandler.FluidAction.SIMULATE
            );
            if (!recipe.fluid().matches(simulated)) {
                return false;
            }
        }

        MachineBatchTransaction.Builder transaction = MachineBatchTransaction.builder()
                .watchItemRange(
                        inventory.items(),
                        0,
                        SeparatorRecipe.MAX_INPUTS + SeparatorRecipe.MAX_OUTPUTS
                )
                .watchFluid(fluid.tank())
                .watchFluid(outputFluid.tank());

        for (int slot = 0; slot < consumptionPlan.length; slot++) {
            int amount = consumptionPlan[slot];
            if (amount <= 0) {
                continue;
            }
            final int inputSlot = slot;
            transaction.mutate(() -> inventory.items()
                    .extractItem(inputSlot, amount, false)
                    .getCount() == amount);
        }

        if (recipe.fluid() != null) {
            transaction.mutate(() -> recipe.fluid().matches(
                    fluid.tank().drain(
                            recipe.fluid().amount(),
                            IFluidHandler.FluidAction.EXECUTE
                    )
            ));
        }

        transaction.mutate(() -> outputPlan.commit(inventory.items()));
        if (!waste.isEmpty()) {
            transaction.mutate(() -> outputFluid.tank().fill(
                    waste,
                    IFluidHandler.FluidAction.EXECUTE
            ) == waste.getAmount());
        }
        return transaction.build().commit();
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
