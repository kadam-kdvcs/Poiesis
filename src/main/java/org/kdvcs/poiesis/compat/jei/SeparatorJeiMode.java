package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;

/** Separates dry and fluid-consuming Separator recipes into distinct JEI pages. */
public enum SeparatorJeiMode {
    DRY(
            PoiesisJeiRecipeTypes.DRY_SEPARATING,
            false,
            "jei.poiesis.category.separator.dry"
    ),
    WET(
            PoiesisJeiRecipeTypes.WET_SEPARATING,
            true,
            "jei.poiesis.category.separator.wet"
    );

    private final RecipeType<SeparatorRecipe> recipeType;
    private final boolean wet;
    private final String titleKey;

    SeparatorJeiMode(
            RecipeType<SeparatorRecipe> recipeType,
            boolean wet,
            String titleKey
    ) {
        this.recipeType = recipeType;
        this.wet = wet;
        this.titleKey = titleKey;
    }

    public RecipeType<SeparatorRecipe> recipeType() {
        return recipeType;
    }

    public boolean wet() {
        return wet;
    }

    public boolean matches(SeparatorRecipe recipe) {
        return recipe.isWet() == wet;
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public ItemStack iconStack() {
        return wet
                ? ModItems.WET_SEPARATION_MODULE.get().getDefaultInstance()
                : ModBlocks.SEPARATOR.get().asItem().getDefaultInstance();
    }

    public ItemStack requiredModuleStack() {
        return wet
                ? ModItems.WET_SEPARATION_MODULE.get().getDefaultInstance()
                : ItemStack.EMPTY;
    }
}
