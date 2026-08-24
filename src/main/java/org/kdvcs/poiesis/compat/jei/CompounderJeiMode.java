package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.screen.layout.CompounderGuiLayout;
import org.kdvcs.poiesis.screen.layout.ProgressIndicatorLayout;

/** Maps Compounder specialization capabilities to their JEI presentation. */
public enum CompounderJeiMode {
    STANDARD(
            null,
            PoiesisJeiRecipeTypes.COMPOUNDING,
            "jei.poiesis.category.compounder"
    ),
    HIGH_SHEAR(
            ModItems.HIGH_SHEAR,
            PoiesisJeiRecipeTypes.HIGH_SHEAR_COMPOUNDING,
            "jei.poiesis.category.compounder.high_shear"
    );

    private final ResourceLocation capability;
    private final RecipeType<CompounderRecipe> recipeType;
    private final String titleKey;

    CompounderJeiMode(
            ResourceLocation capability,
            RecipeType<CompounderRecipe> recipeType,
            String titleKey
    ) {
        this.capability = capability;
        this.recipeType = recipeType;
        this.titleKey = titleKey;
    }

    public RecipeType<CompounderRecipe> recipeType() {
        return recipeType;
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public boolean specialized() {
        return capability != null;
    }

    public boolean matches(CompounderRecipe recipe) {
        if (capability == null) {
            return recipe.isStandard();
        }
        return capability.equals(recipe.requiredCapability());
    }

    public ItemStack iconStack() {
        return specialized()
                ? ModItems.HIGH_SHEAR_MODULE.get().getDefaultInstance()
                : ModBlocks.COMPOUNDER.get().asItem().getDefaultInstance();
    }

    public ItemStack moduleStack() {
        return specialized()
                ? ModItems.HIGH_SHEAR_MODULE.get().getDefaultInstance()
                : ItemStack.EMPTY;
    }

    public ProgressIndicatorLayout progressVisual() {
        return CompounderGuiLayout.progressVisual(
                this == HIGH_SHEAR
        );
    }

    public static CompounderJeiMode fromHighShear(
            boolean highShear
    ) {
        return highShear ? HIGH_SHEAR : STANDARD;
    }
}
