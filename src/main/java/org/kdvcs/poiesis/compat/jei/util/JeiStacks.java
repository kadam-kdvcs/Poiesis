package org.kdvcs.poiesis.compat.jei.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.kdvcs.poiesis.recipe.CountedIngredient;

import java.util.Arrays;
import java.util.Comparator;
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
                .filter(stack -> !stack.isEmpty())
                .map(stack -> {
                    ItemStack copy = stack.copy();
                    copy.setCount(ingredient.count());
                    return copy;
                })
                .toList();
    }

    /** Returns every currently bound item in a recipe output tag for JEI cycling. */
    public static List<ItemStack> tagStacks(
            ResourceLocation tagId,
            int count
    ) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, tagId);
        return BuiltInRegistries.ITEM.getTag(tag)
                .map(values -> values.stream()
                        .map(holder -> new ItemStack(holder.value(), count))
                        .sorted(Comparator.comparing(stack ->
                                BuiltInRegistries.ITEM.getKey(stack.getItem()).toString()
                        ))
                        .toList())
                .orElse(List.of());
    }

    private JeiStacks() {
    }
}
