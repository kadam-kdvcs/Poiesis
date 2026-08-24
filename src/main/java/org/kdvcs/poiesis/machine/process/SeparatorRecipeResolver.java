package org.kdvcs.poiesis.machine.process;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.inventory.InventoryComponent;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleSnapshot;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;

import java.util.List;
import java.util.Optional;

/** Strictly isolates the Separator's dry and wet recipe modes. */
public final class SeparatorRecipeResolver {
    private final MachineContext owner;
    private final InventoryComponent inventory;
    private final FluidComponent fluid;
    private final ModuleComponent modules;
    private final RecipeType<SeparatorRecipe> recipeType;
    private final ResourceLocation wetCapability;

    private long lastInventoryVersion = Long.MIN_VALUE;
    private long lastFluidVersion = Long.MIN_VALUE;
    private long lastCapabilityVersion = Long.MIN_VALUE;
    private Optional<SeparatorRecipe> cached = Optional.empty();

    public SeparatorRecipeResolver(
            MachineContext owner,
            InventoryComponent inventory,
            FluidComponent fluid,
            ModuleComponent modules,
            RecipeType<SeparatorRecipe> recipeType,
            ResourceLocation wetCapability
    ) {
        this.owner = owner;
        this.inventory = inventory;
        this.fluid = fluid;
        this.modules = modules;
        this.recipeType = recipeType;
        this.wetCapability = wetCapability;
    }

    public Optional<SeparatorRecipe> resolve(ModuleSnapshot snapshot) {
        if (lastInventoryVersion == inventory.version()
                && lastFluidVersion == fluid.version()
                && lastCapabilityVersion == modules.capabilityVersion()) {
            return cached;
        }

        lastInventoryVersion = inventory.version();
        lastFluidVersion = fluid.version();
        lastCapabilityVersion = modules.capabilityVersion();
        cached = find(snapshot);
        return cached;
    }

    public void invalidate() {
        lastInventoryVersion = Long.MIN_VALUE;
        lastFluidVersion = Long.MIN_VALUE;
        lastCapabilityVersion = Long.MIN_VALUE;
        cached = Optional.empty();
    }

    private Optional<SeparatorRecipe> find(ModuleSnapshot snapshot) {
        if (owner.level() == null) {
            return Optional.empty();
        }

        List<SeparatorRecipe> recipes = owner.level()
                .getRecipeManager()
                .getAllRecipesFor(recipeType);

        boolean wetMode =
                snapshot.capabilities().contains(wetCapability);

        return recipes.stream()
                .filter(recipe -> recipe.isWet() == wetMode)
                .filter(this::matchesItems)
                .filter(recipe -> !wetMode
                        || recipe.fluid().matches(fluid.fluid()))
                .findFirst();
    }

    private boolean matchesItems(SeparatorRecipe recipe) {
        return MaterialPlan.canSatisfy(
                inventory.items(),
                0,
                SeparatorRecipe.MAX_INPUTS,
                recipe.inputs()
        );
    }
}
