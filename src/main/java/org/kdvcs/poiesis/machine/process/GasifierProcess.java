package org.kdvcs.poiesis.machine.process;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.transaction.MachineBatchTransaction;
import org.kdvcs.poiesis.recipe.GasificationRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/** Temperature-driven batch process for the non-powered biomass gasifier. */
public final class GasifierProcess implements MachineComponent {
    public static final int AMBIENT_TEMPERATURE = 20;
    public static final int MAX_TEMPERATURE = 900;

    private static final int HEATING_INTERVAL_TICKS = 4;
    private static final int COOLING_INTERVAL_TICKS = 6;

    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final FluidComponent gasTank;
    private final RecipeType<GasificationRecipe> recipeType;

    private int progress;
    private int maxProgress;
    private int temperature = AMBIENT_TEMPERATURE;
    private ProcessState state = ProcessState.IDLE;
    private ResourceLocation activeRecipeId;

    public GasifierProcess(
            MachineContext owner,
            InventoryComponent inventory,
            FluidComponent gasTank,
            RecipeType<GasificationRecipe> recipeType
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.gasTank = gasTank;
        this.recipeType = recipeType;
    }

    @Override
    public String key() {
        return "gasifier_process";
    }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        GasificationRecipe recipe = resolveRecipe();
        if (recipe == null) {
            resetRecipe();
            cool(changed);
            state = ProcessState.IDLE;
            workingSetter.accept(false);
            return;
        }

        if (!canAcceptOutputs(recipe)) {
            state = ProcessState.BLOCKED_OUTPUT;
            cool(changed);
            workingSetter.accept(false);
            return;
        }

        state = ProcessState.RUNNING;
        workingSetter.accept(true);

        if (temperature < recipe.minimumTemperature()) {
            heatTowards(recipe.minimumTemperature(), changed);
            return;
        }

        maxProgress = Math.max(1, recipe.duration());
        progress = Math.min(maxProgress, progress + 1);
        heatTowards(MAX_TEMPERATURE, changed);
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        if (!commitBatch(recipe)) {
            state = ProcessState.BLOCKED_OUTPUT;
            workingSetter.accept(false);
            return;
        }

        progress = 0;
        maxProgress = 0;
        activeRecipeId = null;
        changed.run();
    }

    private GasificationRecipe resolveRecipe() {
        if (owner.level() == null) {
            return null;
        }

        if (activeRecipeId != null) {
            Optional<GasificationRecipe> active = owner.level().getRecipeManager()
                    .byKey(activeRecipeId)
                    .filter(recipe -> recipe.getType() == recipeType)
                    .map(recipe -> (GasificationRecipe) recipe);
            if (active.isPresent() && matchesInput(active.get())) {
                maxProgress = Math.max(1, active.get().duration());
                return active.get();
            }
            resetRecipe();
        }

        for (GasificationRecipe recipe
                : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (matchesInput(recipe)) {
                activeRecipeId = recipe.getId();
                maxProgress = Math.max(1, recipe.duration());
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesInput(GasificationRecipe recipe) {
        return recipe.input().test(inventory.items().getStackInSlot(0));
    }

    private boolean canAcceptOutputs(GasificationRecipe recipe) {
        return ItemOutputTransaction.canFit(
                inventory.items(),
                1,
                2,
                List.of(recipe.result())
        ) && gasTank.tank().fill(
                recipe.gasOutput(),
                IFluidHandler.FluidAction.SIMULATE
        ) == recipe.gasOutput().getAmount();
    }

    private boolean commitBatch(GasificationRecipe recipe) {
        if (!matchesInput(recipe)) {
            return false;
        }

        ItemOutputTransaction.Plan itemPlan = ItemOutputTransaction.plan(
                inventory.items(),
                1,
                2,
                List.of(recipe.result())
        ).orElse(null);
        FluidStack gasOutput = recipe.gasOutput();
        if (itemPlan == null || gasTank.tank().fill(
                gasOutput,
                IFluidHandler.FluidAction.SIMULATE
        ) != gasOutput.getAmount()) {
            return false;
        }

        ItemStack simulatedInput = inventory.items().extractItem(
                0,
                recipe.input().count(),
                true
        );
        if (simulatedInput.getCount() != recipe.input().count()
                || !recipe.input().ingredient().test(simulatedInput)) {
            return false;
        }

        return MachineBatchTransaction.builder()
                .watchItems(inventory.items(), 0, 1)
                .watchFluid(gasTank.tank())
                .mutate(() -> inventory.items().extractItem(
                        0, recipe.input().count(), false
                ).getCount() == recipe.input().count())
                .mutate(() -> itemPlan.commit(inventory.items()))
                .mutate(() -> gasTank.tank().fill(
                        gasOutput, IFluidHandler.FluidAction.EXECUTE
                ) == gasOutput.getAmount())
                .build()
                .commit();
    }

    private void cool(Runnable changed) {
        if (temperature > AMBIENT_TEMPERATURE
                && isTemperatureStep(COOLING_INTERVAL_TICKS)) {
            setTemperature(
                    temperature - 1,
                    changed
            );
        }
    }

    private void heatTowards(int target, Runnable changed) {
        int clampedTarget = Math.min(MAX_TEMPERATURE, target);
        if (temperature < clampedTarget
                && isTemperatureStep(HEATING_INTERVAL_TICKS)) {
            setTemperature(
                    Math.min(clampedTarget, temperature + 1),
                    changed
            );
        }
    }

    private boolean isTemperatureStep(int interval) {
        return owner.level() != null
                && owner.level().getGameTime() % interval == 0L;
    }

    private void setTemperature(int value, Runnable changed) {
        int clamped = Math.max(
                AMBIENT_TEMPERATURE,
                Math.min(MAX_TEMPERATURE, value)
        );
        if (temperature != clamped) {
            temperature = clamped;
            changed.run();
        }
    }

    private void resetRecipe() {
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
    }

    public int progress() {
        return progress;
    }

    public int maxProgress() {
        return maxProgress;
    }

    public int temperature() {
        return temperature;
    }

    public ProcessState state() {
        return state;
    }

    @Override
    public void save(CompoundTag tag) {
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        tag.putInt("temperature", temperature);
        tag.putInt("state", state.ordinal());
        if (activeRecipeId != null) {
            tag.putString("active_recipe", activeRecipeId.toString());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(0, tag.getInt("max_progress"));
        temperature = Math.max(
                AMBIENT_TEMPERATURE,
                Math.min(MAX_TEMPERATURE, tag.getInt("temperature"))
        );
        int ordinal = tag.getInt("state");
        state = ordinal >= 0 && ordinal < ProcessState.values().length
                ? ProcessState.values()[ordinal]
                : ProcessState.IDLE;
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe"))
                : null;
    }
}
