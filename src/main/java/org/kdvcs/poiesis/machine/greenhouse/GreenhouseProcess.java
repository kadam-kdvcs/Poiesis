package org.kdvcs.poiesis.machine.greenhouse;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.OperatingPowerPolicy;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.transaction.MachineBatchTransaction;
import org.kdvcs.poiesis.machine.process.ProcessState;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;
import org.kdvcs.poiesis.recipe.MaterialPlan;

import java.util.Optional;
import java.util.function.Consumer;

/** Atomic cultivation batch for the heavy Greenhouse. */
public final class GreenhouseProcess implements MachineComponent {
    public static final int FIRST_PLANTING_SLOT = 0;
    public static final int PLANTING_SLOT_END = 4;
    public static final int NUTRIENT_SLOT = 4;
    public static final int FIRST_OUTPUT_SLOT = 5;
    public static final int OUTPUT_SLOT_END = 7;

    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final GreenhouseWaterComponent water;
    private final GreenhouseNutrientFluidComponent nutrientFluid;
    private final OutputFluidComponent outputFluid;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;
    private final RecipeType<GreenhouseRecipe> recipeType;

    private ResourceLocation activeRecipeId;
    private int progress;
    private int maxProgress;
    private int activeEnergyPerTick;
    private ProcessState state = ProcessState.IDLE;

    public GreenhouseProcess(
            MachineContext owner,
            InventoryComponent inventory,
            GreenhouseWaterComponent water,
            GreenhouseNutrientFluidComponent nutrientFluid,
            OutputFluidComponent outputFluid,
            ModuleComponent modules,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            RecipeType<GreenhouseRecipe> recipeType
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.water = water;
        this.nutrientFluid = nutrientFluid;
        this.outputFluid = outputFluid;
        this.modules = modules;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeType = recipeType;
    }

    @Override
    public String key() {
        return "greenhouse_process";
    }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        GreenhouseRecipe recipe = resolveRecipe();
        if (recipe == null) {
            resetRecipe();
            pause(ProcessState.IDLE, workingSetter);
            return;
        }
        if (!canAcceptOutputs(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        var snapshot = modules.snapshot();
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
        maxProgress = Math.max(1, recipe.duration());
        progress = Math.min(maxProgress, progress + allocation.progressPerTick());
        workingSetter.accept(true);
        changed.run();

        if (progress < maxProgress) {
            return;
        }
        if (!commitBatch(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
        changed.run();
    }

    private GreenhouseRecipe resolveRecipe() {
        if (owner.level() == null) {
            return null;
        }
        if (activeRecipeId != null) {
            Optional<GreenhouseRecipe> active = owner.level().getRecipeManager()
                    .byKey(activeRecipeId)
                    .filter(recipe -> recipe.getType() == recipeType)
                    .map(recipe -> (GreenhouseRecipe) recipe);
            if (active.isPresent() && matchesInputs(active.get())) {
                maxProgress = Math.max(1, active.get().duration());
                return active.get();
            }
            activeRecipeId = null;
            progress = 0;
        }

        for (GreenhouseRecipe recipe : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (matchesInputs(recipe)) {
                activeRecipeId = recipe.getId();
                maxProgress = Math.max(1, recipe.duration());
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesInputs(GreenhouseRecipe recipe) {
        return MaterialPlan.canSatisfy(
                inventory.items(), FIRST_PLANTING_SLOT, PLANTING_SLOT_END, recipe.inputs()
        )
                && recipe.nutrient().test(inventory.items().getStackInSlot(NUTRIENT_SLOT))
                && water.fluid().getAmount() >= recipe.waterAmount()
                && water.fluid().getFluid() == Fluids.WATER
                && recipe.nutrientFluid().matches(nutrientFluid.fluid());
    }

    private boolean canAcceptOutputs(GreenhouseRecipe recipe) {
        if (ItemOutputTransaction.plan(
                inventory.items(), FIRST_OUTPUT_SLOT, OUTPUT_SLOT_END, recipe.results()
        ).isEmpty()) {
            return false;
        }
        FluidStack result = recipe.fluidOutput();
        return outputFluid.tank().fill(result, IFluidHandler.FluidAction.SIMULATE)
                == result.getAmount();
    }

    private boolean commitBatch(GreenhouseRecipe recipe) {
        if (!matchesInputs(recipe)) {
            return false;
        }
        int[] plantingPlan = MaterialPlan.find(
                inventory.items(), FIRST_PLANTING_SLOT, PLANTING_SLOT_END, recipe.inputs()
        );
        ItemOutputTransaction.Plan outputPlan = ItemOutputTransaction.plan(
                inventory.items(), FIRST_OUTPUT_SLOT, OUTPUT_SLOT_END, recipe.results()
        ).orElse(null);
        FluidStack fluidResult = recipe.fluidOutput();
        if (plantingPlan == null || outputPlan == null
                || outputFluid.tank().fill(fluidResult, IFluidHandler.FluidAction.SIMULATE)
                != fluidResult.getAmount()) {
            return false;
        }

        ItemStackHandler items = inventory.items();
        return MachineBatchTransaction.builder()
                .watchItemRange(items, 0, OUTPUT_SLOT_END)
                .watchFluid(water.tank())
                .watchFluid(nutrientFluid.tank())
                .watchFluid(outputFluid.tank())
                .mutate(() -> consumePlantingInputs(items, plantingPlan))
                .mutate(() -> items.extractItem(
                        NUTRIENT_SLOT, recipe.nutrient().count(), false
                ).getCount() == recipe.nutrient().count())
                .mutate(() -> water.tank().drain(
                        recipe.waterAmount(), IFluidHandler.FluidAction.EXECUTE
                ).getAmount() == recipe.waterAmount())
                .mutate(() -> recipe.nutrientFluid().matches(nutrientFluid.tank().drain(
                        recipe.nutrientFluid().amount(), IFluidHandler.FluidAction.EXECUTE
                )))
                .mutate(() -> outputPlan.commit(items))
                .mutate(() -> outputFluid.tank().fill(
                        fluidResult, IFluidHandler.FluidAction.EXECUTE
                ) == fluidResult.getAmount())
                .build()
                .commit();
    }

    private static boolean consumePlantingInputs(ItemStackHandler items, int[] plantingPlan) {
        for (int slot = 0; slot < plantingPlan.length; slot++) {
            int count = plantingPlan[slot];
            if (count > 0 && items.extractItem(
                    FIRST_PLANTING_SLOT + slot, count, false
            ).getCount() != count) {
                return false;
            }
        }
        return true;
    }

    private void resetRecipe() {
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
    }

    private void pause(ProcessState next, Consumer<Boolean> workingSetter) {
        state = next;
        activeEnergyPerTick = 0;
        workingSetter.accept(false);
    }

    public int progress() { return progress; }
    public int maxProgress() { return maxProgress; }
    public int activeEnergyPerTick() { return activeEnergyPerTick; }
    public ProcessState state() { return state; }

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
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe"))
                : null;
        activeEnergyPerTick = 0;
        state = ProcessState.IDLE;
    }
}
