package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.machine.transaction.MachineBatchTransaction;
import org.kdvcs.poiesis.recipe.LeachingRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Atomic item/fluid batch processing for the Leacher. */
public final class LeachingProcess implements MachineComponent {
    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final FluidComponent inputFluid;
    private final OutputFluidComponent outputFluid;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;
    private final RecipeType<LeachingRecipe> recipeType;

    private int progress;
    private int maxProgress;
    private ProcessState state = ProcessState.IDLE;
    private int activeEnergyPerTick;
    private int activeVoltage;
    private ResourceLocation activeRecipeId;

    public LeachingProcess(
            MachineContext owner,
            InventoryComponent inventory,
            FluidComponent inputFluid,
            OutputFluidComponent outputFluid,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            RecipeType<LeachingRecipe> recipeType
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.modules = modules;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeType = recipeType;
    }

    @Override public String key() { return "process"; }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        LeachingRecipe recipe = resolveRecipe();
        if (recipe == null) {
            resetRecipe();
            pause(ProcessState.IDLE, workingSetter);
            return;
        }
        if (!canAcceptOutputs(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        ModuleSnapshot snapshot = modules.snapshot();
        PowerAllocation allocation = powerPolicy.allocate(
                energy, energy.spec(), recipe.baseEnergyPerTick(),
                snapshot.requestedSpeed(), snapshot.energyMultiplier()
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

        if (progress < maxProgress) return;
        if (!commitBatch(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        progress = 0;
        maxProgress = 0;
        activeRecipeId = null;
        changed.run();
    }

    private LeachingRecipe resolveRecipe() {
        if (owner.level() == null) return null;
        if (activeRecipeId != null) {
            Optional<LeachingRecipe> active = owner.level().getRecipeManager()
                    .byKey(activeRecipeId)
                    .filter(recipe -> recipe.getType() == recipeType)
                    .map(recipe -> (LeachingRecipe) recipe);
            if (active.isPresent() && matchesInputs(active.get())) {
                maxProgress = Math.max(1, active.get().duration());
                return active.get();
            }
            activeRecipeId = null;
            progress = 0;
        }
        for (LeachingRecipe recipe : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (matchesInputs(recipe)) {
                activeRecipeId = recipe.getId();
                maxProgress = Math.max(1, recipe.duration());
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesInputs(LeachingRecipe recipe) {
        return recipe.input().test(inventory.items().getStackInSlot(0))
                && recipe.fluidInput().matches(inputFluid.fluid());
    }

    private boolean canAcceptOutputs(LeachingRecipe recipe) {
        ItemOutputTransaction.Plan itemPlan = planItemOutput(recipe);
        FluidStack fluidResult = recipe.fluidOutput();
        return (recipe.result().isEmpty() || itemPlan != null) && outputFluid.tank().fill(
                fluidResult, IFluidHandler.FluidAction.SIMULATE
        ) == fluidResult.getAmount();
    }

    private boolean commitBatch(LeachingRecipe recipe) {
        if (!matchesInputs(recipe)) return false;
        ItemOutputTransaction.Plan itemPlan = planItemOutput(recipe);
        FluidStack fluidResult = recipe.fluidOutput();
        if ((!recipe.result().isEmpty() && itemPlan == null) || outputFluid.tank().fill(
                fluidResult, IFluidHandler.FluidAction.SIMULATE
        ) != fluidResult.getAmount()) return false;

        ItemStack simulatedItem = inventory.items().extractItem(0, recipe.input().count(), true);
        FluidStack simulatedFluid = inputFluid.tank().drain(
                recipe.fluidInput().amount(), IFluidHandler.FluidAction.SIMULATE
        );
        if (simulatedItem.getCount() != recipe.input().count()
                || !recipe.fluidInput().matches(simulatedFluid)) return false;

        MachineBatchTransaction.Builder transaction = MachineBatchTransaction.builder()
                .watchItems(inventory.items(), 0, 1)
                .watchFluid(inputFluid.tank())
                .watchFluid(outputFluid.tank());
        transaction.mutate(() -> inventory.items()
                        .extractItem(0, recipe.input().count(), false)
                        .getCount() == recipe.input().count())
                .mutate(() -> recipe.fluidInput().matches(inputFluid.tank().drain(
                        recipe.fluidInput().amount(), IFluidHandler.FluidAction.EXECUTE
                )));
        if (itemPlan != null) {
            transaction.mutate(() -> itemPlan.commit(inventory.items()));
        }
        return transaction
                .mutate(() -> outputFluid.tank().fill(
                        fluidResult, IFluidHandler.FluidAction.EXECUTE
                ) == fluidResult.getAmount())
                .build()
                .commit();
    }

    private ItemOutputTransaction.Plan planItemOutput(LeachingRecipe recipe) {
        if (recipe.result().isEmpty()) {
            return null;
        }
        return ItemOutputTransaction.plan(
                inventory.items(), 1, 2, List.of(recipe.result())
        ).orElse(null);
    }

    private void resetRecipe() { activeRecipeId = null; progress = 0; maxProgress = 0; }
    private void pause(ProcessState next, Consumer<Boolean> setter) {
        state = next;
        activeEnergyPerTick = 0;
        activeVoltage = 0;
        setter.accept(false);
    }

    public int progress() { return progress; }
    public int maxProgress() { return maxProgress; }
    public ProcessState state() { return state; }
    public int activeEnergyPerTick() { return activeEnergyPerTick; }
    public int activeVoltage() { return activeVoltage; }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        if (activeRecipeId != null) tag.putString("active_recipe", activeRecipeId.toString());
    }

    @Override
    public void load(CompoundTag tag) {
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(0, tag.getInt("max_progress"));
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe")) : null;
        state = ProcessState.IDLE;
        activeEnergyPerTick = 0;
        activeVoltage = 0;
    }
}
