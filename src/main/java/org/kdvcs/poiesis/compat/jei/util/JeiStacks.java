package org.kdvcs.poiesis.compat.jei.util;

import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.CountedIngredient;

import java.util.Arrays;
import java.util.List;

/**
 * Shared JEI stack helpers.
 *
 * Vanilla Ingredient examples all have a count of one, while Poiesis recipes
 * can require counted ingredients. JEI must therefore receive display stacks
 * with the recipe's real required count applied.
 */
public final class JeiStacks {

    public static List<ItemStack> displayStacks(
            CountedIngredient ingredient
    ) {
        return Arrays.stream(
                        ingredient.ingredient().getItems()
                )
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(ingredient.count());
                    return copy;
                })
                .toList();
    }

    private JeiStacks() {
    }
}
