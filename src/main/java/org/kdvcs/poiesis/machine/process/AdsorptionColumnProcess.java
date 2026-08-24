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
import org.kdvcs.poiesis.machine.fluid.AdsorptionOutputFluidComponent;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;

import java.util.Optional;
import java.util.function.Consumer;

/** Strict full-batch adsorption that commits both inputs only on completion. */
public final class AdsorptionColumnProcess implements MachineComponent {
    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final FluidComponent inputFluid;
    private final AdsorptionOutputFluidComponent outputFluid;
    private final EnergyComponent energy;
    private final OperatingPowerPolicy powerPolicy;
    private final RecipeType<AdsorptionRecipe> recipeType;

    private int progress;
    private int maxProgress;
    private ProcessState state = ProcessState.IDLE;
    private int activeEnergyPerTick;
    private int activeVoltage;
    private ResourceLocation activeRecipeId;

    public AdsorptionColumnProcess(
            MachineContext owner,
            InventoryComponent inventory,
            FluidComponent inputFluid,
            AdsorptionOutputFluidComponent outputFluid,
            EnergyComponent energy,
            OperatingPowerPolicy powerPolicy,
            RecipeType<AdsorptionRecipe> recipeType
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.inputFluid = inputFluid;
        this.outputFluid = outputFluid;
        this.energy = energy;
        this.powerPolicy = powerPolicy;
        this.recipeType = recipeType;
    }

    @Override
    public String key() {
        return "process";
    }

    public void tick(
            boolean formed,
            int bedCount,
            Runnable changed,
            Consumer<Boolean> workingSetter
    ) {
        if (!formed) {
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        AdsorptionRecipe recipe = resolveRecipe(bedCount);
        if (recipe == null) {
            resetRecipe();
            pause(ProcessState.IDLE, workingSetter);
            return;
        }

        if (!canAcceptOutputs(recipe)) {
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        PowerAllocation allocation = powerPolicy.allocate(
                energy,
                energy.spec(),
                recipe.baseEnergyPerTick(),
                1,
                1.0D
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

        if (!commitBatch(recipe)) {
            resetRecipe();
            pause(ProcessState.BLOCKED_OUTPUT, workingSetter);
            return;
        }

        progress = 0;
        maxProgress = 0;
        activeRecipeId = null;
        workingSetter.accept(false);
        changed.run();
    }

    private AdsorptionRecipe resolveRecipe(int bedCount) {
        if (owner.level() == null) {
            return null;
        }

        if (activeRecipeId != null) {
            Optional<AdsorptionRecipe> active = owner.level().getRecipeManager()
                    .byKey(activeRecipeId)
                    .filter(recipe -> recipe.getType() == recipeType)
                    .map(recipe -> (AdsorptionRecipe) recipe);
            if (active.isPresent()) {
                AdsorptionRecipe recipe = active.get();
                if (recipe.requiredBeds() <= bedCount
                        && matchesInputs(recipe)) {
                    maxProgress = Math.max(1, recipe.duration());
                    return recipe;
                }
            }
            activeRecipeId = null;
            progress = 0;
        }

        for (AdsorptionRecipe recipe : owner.level().getRecipeManager().getAllRecipesFor(recipeType)) {
            if (recipe.requiredBeds() <= bedCount && matchesInputs(recipe)) {
                activeRecipeId = recipe.getId();
                maxProgress = Math.max(1, recipe.duration());
                return recipe;
            }
        }
        return null;
    }

    private boolean matchesInputs(AdsorptionRecipe recipe) {
        return recipe.adsorbent().test(inventory.items().getStackInSlot(0))
                && recipe.fluidInput().matches(inputFluid.fluid());
    }

    private boolean canAcceptOutputs(AdsorptionRecipe recipe) {
        ItemOutputTransaction.Plan itemPlan = ItemOutputTransaction.plan(
                inventory.items(),
                1,
                2,
                java.util.List.of(recipe.result())
        ).orElse(null);
        FluidStack fluidResult = recipe.fluidOutput();
        return itemPlan != null
                && outputFluid.tank().fill(
                fluidResult,
                IFluidHandler.FluidAction.SIMULATE
        ) == fluidResult.getAmount();
    }

    private boolean commitBatch(AdsorptionRecipe recipe) {
        if (!matchesInputs(recipe)) {
            return false;
        }

        ItemOutputTransaction.Plan itemPlan = ItemOutputTransaction.plan(
                inventory.items(),
                1,
                2,
                java.util.List.of(recipe.result())
        ).orElse(null);
        FluidStack fluidResult = recipe.fluidOutput();
        if (itemPlan == null
                || outputFluid.tank().fill(
                fluidResult,
                IFluidHandler.FluidAction.SIMULATE
                ) != fluidResult.getAmount()) {
            return false;
        }

        ItemStack simulatedItemInput = inventory.items().extractItem(
                0,
                recipe.adsorbent().count(),
                true
        );
        FluidStack simulatedFluidInput = inputFluid.tank().drain(
                recipe.fluidInput().amount(),
                IFluidHandler.FluidAction.SIMULATE
        );
        if (simulatedItemInput.getCount() != recipe.adsorbent().count()
                || !recipe.fluidInput().matches(simulatedFluidInput)) {
            return false;
        }

        ItemStack itemInputBefore = inventory.items().getStackInSlot(0).copy();
        ItemStack itemOutputBefore = inventory.items().getStackInSlot(1).copy();
        FluidStack fluidInputBefore = inputFluid.fluid().copy();
        FluidStack fluidOutputBefore = outputFluid.fluid().copy();

        ItemStack consumedItem = inventory.items().extractItem(
                0,
                recipe.adsorbent().count(),
                false
        );
        FluidStack consumedFluid = inputFluid.tank().drain(
                recipe.fluidInput().amount(),
                IFluidHandler.FluidAction.EXECUTE
        );
        boolean committed = consumedItem.getCount() == recipe.adsorbent().count()
                && recipe.fluidInput().matches(consumedFluid)
                && itemPlan.commit(inventory.items());

        int filled = outputFluid.tank().fill(
                fluidResult,
                IFluidHandler.FluidAction.EXECUTE
        );
        committed = committed && filled == fluidResult.getAmount();

        if (!committed) {
            inventory.items().setStackInSlot(0, itemInputBefore);
            inventory.items().setStackInSlot(1, itemOutputBefore);
            inputFluid.tank().setFluid(fluidInputBefore);
            outputFluid.tank().setFluid(fluidOutputBefore);
            return false;
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
        activeRecipeId = tag.contains("active_recipe")
                ? ResourceLocation.tryParse(tag.getString("active_recipe"))
                : null;
        state = ProcessState.IDLE;
        activeEnergyPerTick = 0;
        activeVoltage = 0;
    }
}
