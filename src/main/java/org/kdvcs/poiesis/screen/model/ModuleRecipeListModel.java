package org.kdvcs.poiesis.screen.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.Comparator;
import java.util.List;

/**
 * Client-side view model for the fabricator recipe list.
 *
 * <p>The screen asks this model for rows; recipe discovery and cache invalidation
 * stay out of the rendering code.</p>
 */
public final class ModuleRecipeListModel {
    private ItemStack cachedBase = ItemStack.EMPTY;
    private long nextRecipeRefreshTick = Long.MIN_VALUE;
    private List<ModuleFabricationRecipe> cachedRecipes = List.of();

    public List<ModuleFabricationRecipe> recipes(ClientLevel level, ItemStack base) {
        if (level == null || base.isEmpty()) {
            cachedBase = ItemStack.EMPTY;
            cachedRecipes = List.of();
            return cachedRecipes;
        }

        long gameTime = level.getGameTime();
        if (ItemStack.isSameItemSameTags(cachedBase, base)
                && gameTime < nextRecipeRefreshTick) {
            return cachedRecipes;
        }

        cachedBase = base.copyWithCount(1);
        cachedRecipes = level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.MODULE_FABRICATION.get())
                .stream()
                .filter(recipe -> recipe.moduleBase().ingredient().test(base))
                .sorted(Comparator.comparing(recipe -> recipe.getId().toString()))
                .toList();
        nextRecipeRefreshTick = gameTime + 20L;
        return cachedRecipes;
    }
}
