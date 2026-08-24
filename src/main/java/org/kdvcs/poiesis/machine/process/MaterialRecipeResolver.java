package org.kdvcs.poiesis.machine.process;

import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.MaterialProcessRecipe;

import java.util.List;
import java.util.Optional;

/**
 * Version-aware recipe resolver for item material processes.
 *
 * Recipe scanning occurs only when material inputs or process capabilities
 * change. Recipe modes are strictly isolated: a machine without a process
 * capability can only resolve standard recipes, while a specialized machine
 * can only resolve recipes belonging to its installed capability. There is no
 * fallback from a specialized mode to standard processing.
 */
public final class MaterialRecipeResolver<R extends MaterialProcessRecipe> {
    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final ModuleComponent modules;
    private final RecipeType<R> recipeType;
    private final int inputStart;
    private final int inputEndExclusive;

    private long lastInventoryVersion = Long.MIN_VALUE;
    private long lastCapabilityVersion = Long.MIN_VALUE;
    private Optional<R> cached = Optional.empty();

    public MaterialRecipeResolver(
            MachineContext owner,
            InventoryComponent inventory,
            ModuleComponent modules,
            RecipeType<R> recipeType,
            int inputStart,
            int inputEndExclusive
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.modules = modules;
        this.recipeType = recipeType;
        this.inputStart = inputStart;
        this.inputEndExclusive = inputEndExclusive;
    }

    public Optional<R> resolve(ModuleSnapshot snapshot) {
        long inventoryVersion = inventory.version();
        long capabilityVersion = modules.capabilityVersion();

        if (inventoryVersion == lastInventoryVersion
                && capabilityVersion == lastCapabilityVersion) {
            return cached;
        }

        lastInventoryVersion = inventoryVersion;
        lastCapabilityVersion = capabilityVersion;
        cached = find(snapshot);
        return cached;
    }

    public void invalidate() {
        lastInventoryVersion = Long.MIN_VALUE;
        lastCapabilityVersion = Long.MIN_VALUE;
        cached = Optional.empty();
    }

    private Optional<R> find(ModuleSnapshot snapshot) {
        if (owner.level() == null) {
            return Optional.empty();
        }

        List<R> recipes = owner.level()
                .getRecipeManager()
                .getAllRecipesFor(recipeType);

        return recipes.stream()
                .filter(recipe -> belongsToActiveMode(recipe, snapshot))
                .filter(this::matchesInputs)
                .findFirst();
    }

    private boolean belongsToActiveMode(
            R recipe,
            ModuleSnapshot snapshot
    ) {
        if (snapshot.capabilities().isEmpty()) {
            return recipe.isStandard();
        }

        return recipe.requiredCapability() != null
                && snapshot.capabilities().contains(
                recipe.requiredCapability()
        );
    }

    private boolean matchesInputs(R recipe) {
        return MaterialPlan.canSatisfy(
                inventory.items(),
                inputStart,
                inputEndExclusive,
                recipe.inputs()
        );
    }
}
