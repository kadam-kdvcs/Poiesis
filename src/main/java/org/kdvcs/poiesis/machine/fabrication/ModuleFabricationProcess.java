package org.kdvcs.poiesis.machine.fabrication;

import org.kdvcs.poiesis.recipe.MaterialPlan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.inventory.ItemOutputTransaction;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public final class ModuleFabricationProcess implements MachineComponent {
    public static final int MODULE_BASE_SLOT = 0;
    public static final int FIRST_MATERIAL_SLOT = 1;
    public static final int MATERIAL_SLOT_COUNT = 9;
    public static final int MATERIAL_SLOT_END = FIRST_MATERIAL_SLOT + MATERIAL_SLOT_COUNT;
    public static final int OUTPUT_SLOT = MATERIAL_SLOT_END;

    private final InventoryComponent inventory;
    private final EnergyComponent energy;
    private final Function<ResourceLocation, Optional<ModuleFabricationRecipe>> recipeById;

    private ResourceLocation selectedRecipeId;
    private ResourceLocation activeRecipeId;
    private int progress;
    private int maxProgress;
    private boolean autoCrafting;
    private boolean manualStartRequested;
    private int activeEnergyPerTick;

    public ModuleFabricationProcess(
            InventoryComponent inventory,
            EnergyComponent energy,
            Function<ResourceLocation, Optional<ModuleFabricationRecipe>> recipeById
    ) {
        this.inventory = inventory;
        this.energy = energy;
        this.recipeById = recipeById;
    }

    @Override
    public String key() {
        return "module_fabrication_process";
    }

    public void select(ResourceLocation recipeId) {
        selectedRecipeId = recipeId;
    }

    public void requestManualStart(ResourceLocation recipeId) {
        if (activeRecipeId != null) return;
        selectedRecipeId = recipeId;
        manualStartRequested = true;
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
            ModuleFabricationRecipe selected = selectedRecipe().orElse(null);
            if (selected == null || !canStart(selected)) {
                return;
            }

            activeRecipeId = selected.getId();
            progress = 0;
            maxProgress = selected.duration();
            changed.run();
        }

        ModuleFabricationRecipe active = recipeById.apply(activeRecipeId).orElse(null);
        if (active == null) {
            cancelActive(workingSetter, changed);
            return;
        }

        if (!hasBase(active) || !hasMaterials(active)) {
            cancelActive(workingSetter, changed);
            return;
        }
        if (!canAcceptResult(active.result())) {
            workingSetter.accept(false);
            activeEnergyPerTick = 0;
            return;
        }

        int energyPerTick = active.energyPerTick();
        if (energyPerTick > 0 && !energy.consume(energyPerTick)) {
            workingSetter.accept(false);
            activeEnergyPerTick = 0;
            return;
        }

        activeEnergyPerTick = energyPerTick;
        workingSetter.accept(true);
        progress++;
        maxProgress = active.duration();
        changed.run();

        if (progress < maxProgress) {
            return;
        }

        ItemOutputTransaction.Plan outputPlan = planResult(
                active.result()
        ).orElse(null);
        if (!hasBase(active) || !hasMaterials(active) || outputPlan == null) {
            workingSetter.accept(false);
            return;
        }

        if (!commit(active, outputPlan)) {
            workingSetter.accept(false);
            return;
        }
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
        if (autoCrafting) {
            ModuleFabricationRecipe next = selectedRecipe().orElse(null);
            if (next != null && canStart(next)) {
                activeRecipeId = next.getId();
                maxProgress = next.duration();
                workingSetter.accept(true);
                changed.run();
                return;
            }
        }
        workingSetter.accept(false);
        changed.run();
    }

    public boolean canStart(ModuleFabricationRecipe recipe) {
        return activeRecipeId == null
                && hasBase(recipe)
                && hasMaterials(recipe)
                && canAcceptResult(recipe.result());
    }

    public boolean hasMaterials(ModuleFabricationRecipe recipe) {
        return MaterialPlan.canSatisfy(
                inventory.items(),
                FIRST_MATERIAL_SLOT,
                MATERIAL_SLOT_END,
                recipe.materials()
        );
    }

    private boolean hasBase(ModuleFabricationRecipe recipe) {
        return recipe.moduleBase().test(inventory.items().getStackInSlot(MODULE_BASE_SLOT));
    }

    private boolean canAcceptResult(ItemStack result) {
        return planResult(result).isPresent();
    }

    private Optional<ItemOutputTransaction.Plan> planResult(ItemStack result) {
        return ItemOutputTransaction.plan(
                inventory.items(),
                OUTPUT_SLOT,
                OUTPUT_SLOT + 1,
                List.of(result)
        );
    }

    private boolean commit(
            ModuleFabricationRecipe recipe,
            ItemOutputTransaction.Plan outputPlan
    ) {
        int[] plan = MaterialPlan.find(
                inventory.items(),
                FIRST_MATERIAL_SLOT,
                MATERIAL_SLOT_END,
                recipe.materials()
        );
        if (plan == null
                || !hasBase(recipe)
                || !outputPlan.matches(inventory.items())) {
            return false;
        }

        inventory.items().extractItem(MODULE_BASE_SLOT, recipe.moduleBase().count(), false);
        for (int i = 0; i < plan.length; i++) {
            if (plan[i] > 0) {
                inventory.items().extractItem(FIRST_MATERIAL_SLOT + i, plan[i], false);
            }
        }

        if (!outputPlan.commit(inventory.items())) {
            throw new IllegalStateException(
                    "Module fabricator output changed during an atomic commit"
            );
        }
        return true;
    }

    private void cancelActive(Consumer<Boolean> workingSetter, Runnable changed) {
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
        workingSetter.accept(false);
        changed.run();
    }

    public Optional<ModuleFabricationRecipe> selectedRecipe() {
        return selectedRecipeId == null ? Optional.empty() : recipeById.apply(selectedRecipeId);
    }

    public ResourceLocation selectedRecipeId() { return selectedRecipeId; }
    public ResourceLocation activeRecipeId() { return activeRecipeId; }
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
        if (!tag.contains(key)) return null;
        return ResourceLocation.tryParse(tag.getString(key));
    }
}
