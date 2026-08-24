package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.function.Supplier;

/** Separates module fabrication recipes by the substrate they consume. */
public enum ModuleFabricationJeiMode {
    SPECIALIZATION(
            PoiesisJeiRecipeTypes.SPECIALIZATION_MODULE_FABRICATION,
            () -> ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get(),
            "jei.poiesis.category.module_fabricator.specialization"
    ),
    PERFORMANCE(
            PoiesisJeiRecipeTypes.PERFORMANCE_MODULE_FABRICATION,
            () -> ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(),
            "jei.poiesis.category.module_fabricator.performance"
    );

    private final RecipeType<ModuleFabricationRecipe> recipeType;
    private final Supplier<? extends ItemLike> substrate;
    private final String titleKey;

    ModuleFabricationJeiMode(
            RecipeType<ModuleFabricationRecipe> recipeType,
            Supplier<? extends ItemLike> substrate,
            String titleKey
    ) {
        this.recipeType = recipeType;
        this.substrate = substrate;
        this.titleKey = titleKey;
    }

    public RecipeType<ModuleFabricationRecipe> recipeType() {
        return recipeType;
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public ItemStack substrateStack() {
        return new ItemStack(substrate.get());
    }

    public boolean matches(ModuleFabricationRecipe recipe) {
        return recipe.moduleBase()
                .ingredient()
                .test(substrateStack());
    }
}
