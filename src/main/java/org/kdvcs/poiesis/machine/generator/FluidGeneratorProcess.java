package org.kdvcs.poiesis.machine.generator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.process.ProcessState;
import org.kdvcs.poiesis.recipe.FluidGeneratorFuelRecipe;

import java.util.Optional;
import java.util.function.Consumer;

/** Common continuous process for generators that convert a fluid into FE. */
public final class FluidGeneratorProcess<R extends Recipe<Container> & FluidGeneratorFuelRecipe>
        implements MachineComponent {
    private final MachineContext owner;
    private final EnergyComponent energy;
    private final FluidComponent fuel;
    private final RecipeType<R> recipeType;
    private final int defaultGenerationPerTick;

    private ResourceLocation activeRecipeId;
    private long remainingFuelEnergy;
    private long totalFuelEnergy;
    private int activeGenerationPerTick;
    private ProcessState state = ProcessState.IDLE;

    public FluidGeneratorProcess(
            MachineContext owner,
            EnergyComponent energy,
            FluidComponent fuel,
            RecipeType<R> recipeType,
            int defaultGenerationPerTick
    ) {
        this.owner = owner;
        this.energy = energy;
        this.fuel = fuel;
        this.recipeType = recipeType;
        this.defaultGenerationPerTick = Math.max(1, defaultGenerationPerTick);
    }

    @Override
    public String key() {
        return "generator_process";
    }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        R recipe = activeRecipe();
        if (remainingFuelEnergy <= 0) {
            recipe = findFuelRecipe();
            if (recipe == null || !consumeFuelBatch(recipe)) {
                clearBurn();
                state = ProcessState.IDLE;
                activeGenerationPerTick = 0;
                workingSetter.accept(false);
                return;
            }
            changed.run();
        }

        if (recipe == null) {
            clearBurn();
            state = ProcessState.IDLE;
            activeGenerationPerTick = 0;
            workingSetter.accept(false);
            changed.run();
            return;
        }

        int rate = Math.max(1, recipe.energyPerTick() > 0
                ? recipe.energyPerTick()
                : defaultGenerationPerTick);
        int requested = (int) Math.min(remainingFuelEnergy, rate);
        int generated = energy.produce(requested, false);
        if (generated <= 0) {
            state = ProcessState.BLOCKED_OUTPUT;
            activeGenerationPerTick = 0;
            workingSetter.accept(false);
            return;
        }

        remainingFuelEnergy -= generated;
        activeGenerationPerTick = generated;
        state = ProcessState.RUNNING;
        workingSetter.accept(true);
        changed.run();
    }

    private R activeRecipe() {
        if (owner.level() == null || activeRecipeId == null) {
            return null;
        }
        Optional<?> recipe = owner.level().getRecipeManager().byKey(activeRecipeId);
        if (recipe.isEmpty() || !(recipe.get() instanceof FluidGeneratorFuelRecipe)) {
            return null;
        }
        @SuppressWarnings("unchecked")
        R result = (R) recipe.get();
        return result.getType() == recipeType ? result : null;
    }

    private R findFuelRecipe() {
        if (owner.level() == null || fuel.fluid().isEmpty()) {
            return null;
        }
        for (R recipe : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (recipe.fluid().matches(fuel.fluid())) {
                return recipe;
            }
        }
        return null;
    }

    private boolean consumeFuelBatch(R recipe) {
        int amount = recipe.fluid().amount();
        if (!recipe.fluid().matches(fuel.fluid())) {
            return false;
        }
        var drained = fuel.tank().drain(amount, IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() != amount) {
            return false;
        }

        activeRecipeId = recipe.getId();
        totalFuelEnergy = Math.multiplyExact((long) amount, recipe.energyPerMb());
        remainingFuelEnergy = totalFuelEnergy;
        return true;
    }

    private void clearBurn() {
        activeRecipeId = null;
        remainingFuelEnergy = 0;
        totalFuelEnergy = 0;
    }

    public int burnRemaining() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, remainingFuelEnergy));
    }

    public int burnTotal() {
        return (int) Math.min(Integer.MAX_VALUE, Math.max(0, totalFuelEnergy));
    }

    public int activeGenerationPerTick() {
        return activeGenerationPerTick;
    }

    public ProcessState state() {
        return state;
    }

    @Override
    public void save(CompoundTag tag) {
        if (activeRecipeId != null) {
            tag.putString("active_recipe", activeRecipeId.toString());
        }
        tag.putLong("remaining_fuel_energy", remainingFuelEnergy);
        tag.putLong("total_fuel_energy", totalFuelEnergy);
        tag.putInt("active_generation_per_tick", activeGenerationPerTick);
        tag.putInt("state", state.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe"))
                : null;
        remainingFuelEnergy = Math.max(0L, tag.getLong("remaining_fuel_energy"));
        totalFuelEnergy = Math.max(remainingFuelEnergy, tag.getLong("total_fuel_energy"));
        activeGenerationPerTick = Math.max(0, tag.getInt("active_generation_per_tick"));
        int ordinal = tag.getInt("state");
        state = ordinal >= 0 && ordinal < ProcessState.values().length
                ? ProcessState.values()[ordinal]
                : ProcessState.IDLE;
    }
}
