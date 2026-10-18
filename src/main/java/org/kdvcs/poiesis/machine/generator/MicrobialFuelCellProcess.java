package org.kdvcs.poiesis.machine.generator;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.process.ProcessState;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;

import java.util.Optional;
import java.util.function.Consumer;

/** Converts paired anolyte/catholyte batches into stored FE. */
public final class MicrobialFuelCellProcess implements MachineComponent {
    private final MachineContext owner;
    private final EnergyComponent energy;
    private final MfcFluidComponent anolyte;
    private final MfcFluidComponent catholyte;
    private final RecipeType<MicrobialFuelCellRecipe> recipeType;

    private ResourceLocation activeRecipeId;
    private int remainingEnergy;
    private int totalEnergy;
    private int activeGenerationPerTick;
    private ProcessState state = ProcessState.IDLE;

    public MicrobialFuelCellProcess(
            MachineContext owner,
            EnergyComponent energy,
            MfcFluidComponent anolyte,
            MfcFluidComponent catholyte,
            RecipeType<MicrobialFuelCellRecipe> recipeType
    ) {
        this.owner = owner;
        this.energy = energy;
        this.anolyte = anolyte;
        this.catholyte = catholyte;
        this.recipeType = recipeType;
    }

    @Override public String key() { return "microbial_fuel_cell_process"; }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        MicrobialFuelCellRecipe recipe = activeRecipe();
        if (remainingEnergy <= 0) {
            recipe = findRecipe();
            if (recipe == null || !consumeBatch(recipe)) {
                clearReaction();
                state = ProcessState.IDLE;
                activeGenerationPerTick = 0;
                workingSetter.accept(false);
                return;
            }
            changed.run();
        }

        if (recipe == null) {
            clearReaction();
            state = ProcessState.IDLE;
            activeGenerationPerTick = 0;
            workingSetter.accept(false);
            changed.run();
            return;
        }

        int requested = Math.min(remainingEnergy, recipe.energyPerTick());
        int generated = energy.produce(requested, false);
        if (generated <= 0) {
            state = ProcessState.BLOCKED_OUTPUT;
            activeGenerationPerTick = 0;
            workingSetter.accept(false);
            return;
        }

        remainingEnergy -= generated;
        activeGenerationPerTick = generated;
        state = ProcessState.RUNNING;
        workingSetter.accept(true);
        changed.run();
    }

    private MicrobialFuelCellRecipe findRecipe() {
        if (owner.level() == null || anolyte.fluid().isEmpty() || catholyte.fluid().isEmpty()) {
            return null;
        }
        for (MicrobialFuelCellRecipe recipe
                : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (recipe.anolyte().matches(anolyte.fluid())
                    && recipe.catholyte().matches(catholyte.fluid())) {
                return recipe;
            }
        }
        return null;
    }

    private MicrobialFuelCellRecipe activeRecipe() {
        if (owner.level() == null || activeRecipeId == null) {
            return null;
        }
        Optional<?> recipe = owner.level().getRecipeManager().byKey(activeRecipeId);
        if (recipe.isEmpty() || !(recipe.get() instanceof MicrobialFuelCellRecipe result)) {
            return null;
        }
        return result.getType() == recipeType ? result : null;
    }

    private boolean consumeBatch(MicrobialFuelCellRecipe recipe) {
        int anolyteAmount = recipe.anolyte().amount();
        int catholyteAmount = recipe.catholyte().amount();
        if (!recipe.anolyte().matches(anolyte.fluid())
                || !recipe.catholyte().matches(catholyte.fluid())
                || anolyte.tank().drain(anolyteAmount, IFluidHandler.FluidAction.SIMULATE)
                .getAmount() != anolyteAmount
                || catholyte.tank().drain(catholyteAmount, IFluidHandler.FluidAction.SIMULATE)
                .getAmount() != catholyteAmount) {
            return false;
        }

        anolyte.tank().drain(anolyteAmount, IFluidHandler.FluidAction.EXECUTE);
        catholyte.tank().drain(catholyteAmount, IFluidHandler.FluidAction.EXECUTE);
        activeRecipeId = recipe.getId();
        totalEnergy = recipe.totalEnergy();
        remainingEnergy = totalEnergy;
        return true;
    }

    private void clearReaction() {
        activeRecipeId = null;
        remainingEnergy = 0;
        totalEnergy = 0;
    }

    public int progress() { return Math.max(0, totalEnergy - remainingEnergy); }
    public int maxProgress() { return totalEnergy; }
    public int activeGenerationPerTick() { return activeGenerationPerTick; }
    public ProcessState state() { return state; }
    public boolean hasActiveReaction() { return remainingEnergy > 0; }

    @Override
    public void save(CompoundTag tag) {
        if (activeRecipeId != null) tag.putString("active_recipe", activeRecipeId.toString());
        tag.putInt("remaining_energy", remainingEnergy);
        tag.putInt("total_energy", totalEnergy);
        tag.putInt("active_generation_per_tick", activeGenerationPerTick);
        tag.putInt("state", state.ordinal());
    }

    @Override
    public void load(CompoundTag tag) {
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe")) : null;
        remainingEnergy = Math.max(0, tag.getInt("remaining_energy"));
        totalEnergy = Math.max(remainingEnergy, tag.getInt("total_energy"));
        activeGenerationPerTick = Math.max(0, tag.getInt("active_generation_per_tick"));
        int ordinal = tag.getInt("state");
        state = ordinal >= 0 && ordinal < ProcessState.values().length
                ? ProcessState.values()[ordinal] : ProcessState.IDLE;
    }
}
