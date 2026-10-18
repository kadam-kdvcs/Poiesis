package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.Objects;

/** Keeps standard, wet and electrostatic Separator recipes on distinct JEI pages. */
public enum SeparatorJeiMode {
    DRY(
            PoiesisJeiRecipeTypes.DRY_SEPARATING,
            null,
            false,
            "jei.poiesis.category.separator.dry"
    ),
    WET(
            PoiesisJeiRecipeTypes.WET_SEPARATING,
            ModItems.WET_SEPARATION,
            true,
            "jei.poiesis.category.separator.wet"
    ),
    RINSING(
            PoiesisJeiRecipeTypes.RINSING_SEPARATING,
            ModItems.RINSING,
            true,
            "jei.poiesis.category.separator.rinsing"
    ),
    ELECTROSTATIC(
            PoiesisJeiRecipeTypes.ELECTROSTATIC_SEPARATING,
            ModItems.ELECTROSTATIC_SEPARATION,
            false,
            "jei.poiesis.category.separator.electrostatic"
    );

    private final RecipeType<SeparatorRecipe> recipeType;
    private final ResourceLocation requiredCapability;
    private final boolean wet;
    private final String titleKey;

    SeparatorJeiMode(
            RecipeType<SeparatorRecipe> recipeType,
            ResourceLocation requiredCapability,
            boolean wet,
            String titleKey
    ) {
        this.recipeType = recipeType;
        this.requiredCapability = requiredCapability;
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
        return Objects.equals(recipe.requiredCapability(), requiredCapability);
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public ItemStack iconStack() {
        if (ModItems.WET_SEPARATION.equals(requiredCapability)) {
            return ModItems.WET_SEPARATION_MODULE.get().getDefaultInstance();
        }
        if (ModItems.RINSING.equals(requiredCapability)) {
            return ModItems.RINSING_MODULE.get().getDefaultInstance();
        }
        if (ModItems.ELECTROSTATIC_SEPARATION.equals(requiredCapability)) {
            return ModItems.ELECTROSTATIC_SEPARATION_MODULE.get().getDefaultInstance();
        }
        return ModBlocks.SEPARATOR.get().asItem().getDefaultInstance();
    }

    public ItemStack requiredModuleStack() {
        return requiredCapability == null ? ItemStack.EMPTY : iconStack();
    }

    public boolean electrostatic() {
        return ModItems.ELECTROSTATIC_SEPARATION.equals(requiredCapability);
    }

    public boolean rinsing() {
        return ModItems.RINSING.equals(requiredCapability);
    }
}
