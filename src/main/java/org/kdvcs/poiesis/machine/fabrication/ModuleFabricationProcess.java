package org.kdvcs.poiesis.machine.fabrication;

import org.kdvcs.poiesis.recipe.MaterialPlan;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.api.MachineComponent;
import org.kdvcs.poiesis.machine.energy.EnergyComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;

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

        if (!hasBase(active) || !hasMaterials(active) || !canAcceptResult(active.result())) {
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

        if (!hasBase(active) || !hasMaterials(active) || !canAcceptResult(active.result())) {
            workingSetter.accept(false);
            return;
        }

        commit(active);
        activeRecipeId = null;
        progress = 0;
        maxProgress = 0;
        activeEnergyPerTick = 0;
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
        ItemStack output = inventory.items().getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) return true;
        if (!ItemStack.isSameItemSameTags(output, result)) return false;
        return output.getCount() + result.getCount() <= output.getMaxStackSize();
    }

    private void commit(ModuleFabricationRecipe recipe) {
        int[] plan = MaterialPlan.find(
                inventory.items(),
                FIRST_MATERIAL_SLOT,
                MATERIAL_SLOT_END,
                recipe.materials()
        );
        if (plan == null) return;

        inventory.items().extractItem(MODULE_BASE_SLOT, recipe.moduleBase().count(), false);
        for (int i = 0; i < plan.length; i++) {
            if (plan[i] > 0) {
                inventory.items().extractItem(FIRST_MATERIAL_SLOT + i, plan[i], false);
            }
        }

        ItemStack result = recipe.result();
        ItemStack output = inventory.items().getStackInSlot(OUTPUT_SLOT);
        if (output.isEmpty()) {
            inventory.items().setStackInSlot(OUTPUT_SLOT, result);
        } else {
            ItemStack merged = output.copy();
            merged.grow(result.getCount());
            inventory.items().setStackInSlot(OUTPUT_SLOT, merged);
        }
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
