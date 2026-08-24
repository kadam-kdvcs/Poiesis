package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.vanilla.IJeiIngredientInfoRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

import java.util.List;

/**
 * JEI's addIngredientInfo helper expands translated components when plugins
 * are registered. Keeping the translation key here lets Minecraft resolve it
 * against the active language whenever the information page is rendered.
 */
final class TranslatableIngredientInfoRecipe
        implements IJeiIngredientInfoRecipe {

    private final List<ITypedIngredient<?>> ingredients;
    private final String translationKey;

    TranslatableIngredientInfoRecipe(
            List<? extends ITypedIngredient<?>> ingredients,
            String translationKey
    ) {
        this.ingredients = List.copyOf(ingredients);
        this.translationKey = translationKey;
    }

    @Override
    public List<ITypedIngredient<?>> getIngredients() {
        return ingredients;
    }

    @Override
    public List<FormattedText> getDescription() {
        return List.of(
                Component.translatable(translationKey)
        );
    }
}
