package org.kdvcs.poiesis.machine.alloy;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.energy.PowerAllocation;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.alloy.AlloyDefinition;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/** Selected-recipe, single-thread process used by the base Alloy Factory. */
public final class AlloyFactoryProcess implements MachineComponent {
    public static final int FIRST_INPUT_SLOT = 0;
    public static final int INPUT_SLOT_COUNT = 18;
    public static final int INPUT_SLOT_END = FIRST_INPUT_SLOT + INPUT_SLOT_COUNT;
    public static final int FIRST_OUTPUT_SLOT = INPUT_SLOT_END;
    public static final int OUTPUT_SLOT_COUNT = 6;
    public static final int OUTPUT_SLOT_END = FIRST_OUTPUT_SLOT + OUTPUT_SLOT_COUNT;

    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final EnergyComponent energy;
    private final VoltageOverclockPolicy powerPolicy = new VoltageOverclockPolicy();
    private final Function<ResourceLocation, Optional<AlloyDefinition>> recipeById;

    private ResourceLocation selectedRecipeId;
    private ResourceLocation activeRecipeId;
    private int progress;
    private int maxProgress;
    private boolean autoCrafting;
    private boolean manualStartRequested;
    private int activeEnergyPerTick;

    public AlloyFactoryProcess(
            InventoryComponent inventory,
            ModuleComponent modules,
            EnergyComponent energy,
            Function<ResourceLocation, Optional<AlloyDefinition>> recipeById
    ) {
        this.inventory = inventory;
        this.modules = modules;
        this.energy = energy;
        this.recipeById = recipeById;
    }

    @Override
    public String key() {
        return "alloy_factory_process";
    }

    public void select(ResourceLocation recipeId) {
        selectedRecipeId = recipeId;
    }

    public void requestManualStart(ResourceLocation recipeId) {
        if (activeRecipeId == null) {
            selectedRecipeId = recipeId;
            manualStartRequested = true;
        }
    }

    public void toggleAutoCrafting() {
        autoCrafting = !autoCrafting;
    }

    public void tick(Runnable changed, Consumer<Boolean> workingSetter) {
        if (activeRecipeId == null) {
            workingSetter.accept(false);
            activeEnergyPerTick = 0;
            if (!autoCrafting && !manualStartRequested) {
                return;
            }

            manualStartRequested = false;
            AlloyDefinition selected = selectedRecipe().orElse(null);
            if (selected == null || !canStart(selected)) {
                return;
            }
            activeRecipeId = selected.recipeId();
            progress = 0;
            maxProgress = selected.duration();
            changed.run();
        }

        AlloyDefinition active = recipeById.apply(activeRecipeId).orElse(null);
        if (active == null) {
            cancel(workingSetter, changed);
            return;
        }
        if (!hasMaterials(active)) {
            cancel(workingSetter, changed);
            return;
        }
        if (planResult(active).isEmpty()) {
            workingSetter.accept(false);
            activeEnergyPerTick = 0;
            return;
        }

        var snapshot = modules.snapshot();
        PowerAllocation allocation = powerPolicy.allocate(
                energy,
                energy.spec(),
                active.energyPerTick(),
                snapshot.requestedSpeed(),
                snapshot.energyMultiplier()
        );
        if (!allocation.active() || !energy.consume(allocation.energyPerTick())) {
            workingSetter.accept(false);
            activeEnergyPerTick = 0;
            return;
        }

        activeEnergyPerTick = allocation.energyPerTick();
        workingSetter.accept(true);
        maxProgress = active.duration();
        progress = Math.min(maxProgress, progress + allocation.progressPerTick());
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        int[] materialPlan = MaterialPlan.find(
                inventory.items(), FIRST_INPUT_SLOT, INPUT_SLOT_END, active.inputs()
        );
        ItemOutputTransaction.Plan outputPlan = planResult(active).orElse(null);
        if (materialPlan == null || outputPlan == null || !outputPlan.matches(inventory.items())) {
            workingSetter.accept(false);
            return;
        }

        for (int slot = 0; slot < materialPlan.length; slot++) {
            if (materialPlan[slot] > 0) {
                inventory.items().extractItem(FIRST_INPUT_SLOT + slot, materialPlan[slot], false);
            }
        }
        if (!outputPlan.commit(inventory.items())) {
            throw new IllegalStateException("Alloy Factory output changed during atomic commit");
        }

        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
        if (autoCrafting) {
            AlloyDefinition next = selectedRecipe().orElse(null);
            if (next != null && canStart(next)) {
                activeRecipeId = next.recipeId();
                maxProgress = next.duration();
                workingSetter.accept(true);
                changed.run();
                return;
            }
        }
        workingSetter.accept(false);
        changed.run();
    }

    public boolean canStart(AlloyDefinition recipe) {
        return activeRecipeId == null && hasMaterials(recipe) && planResult(recipe).isPresent();
    }

    public boolean hasMaterials(AlloyDefinition recipe) {
        return MaterialPlan.canSatisfy(
                inventory.items(), FIRST_INPUT_SLOT, INPUT_SLOT_END, recipe.inputs()
        );
    }

    private Optional<ItemOutputTransaction.Plan> planResult(AlloyDefinition recipe) {
        return ItemOutputTransaction.plan(
                inventory.items(), FIRST_OUTPUT_SLOT, OUTPUT_SLOT_END, List.of(recipe.result())
        );
    }

    private void cancel(Consumer<Boolean> workingSetter, Runnable changed) {
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
        workingSetter.accept(false);
        changed.run();
    }

    public Optional<AlloyDefinition> selectedRecipe() {
        return selectedRecipeId == null ? Optional.empty() : recipeById.apply(selectedRecipeId);
    }

    public ResourceLocation selectedRecipeId() { return selectedRecipeId; }
    public int progress() { return progress; }
    public int maxProgress() { return maxProgress; }
    public int activeEnergyPerTick() { return activeEnergyPerTick; }
    public boolean autoCrafting() { return autoCrafting; }
    public boolean isProcessing() { return activeRecipeId != null; }

    @Override
    public void save(CompoundTag tag) {
        if (selectedRecipeId != null) tag.putString("selected_recipe", selectedRecipeId.toString());
        if (activeRecipeId != null) tag.putString("active_recipe", activeRecipeId.toString());
        tag.putInt("progress", progress);
        tag.putInt("max_progress", maxProgress);
        tag.putBoolean("auto_crafting", autoCrafting);
    }

    @Override
    public void load(CompoundTag tag) {
        selectedRecipeId = readId(tag, "selected_recipe");
        activeRecipeId = readId(tag, "active_recipe");
        progress = Math.max(0, tag.getInt("progress"));
        maxProgress = Math.max(0, tag.getInt("max_progress"));
        autoCrafting = tag.getBoolean("auto_crafting");
    }

    private static ResourceLocation readId(CompoundTag tag, String key) {
        return tag.contains(key) ? ResourceLocation.tryParse(tag.getString(key)) : null;
    }
}
