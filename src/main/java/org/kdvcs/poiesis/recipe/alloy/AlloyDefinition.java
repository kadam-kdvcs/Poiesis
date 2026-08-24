package org.kdvcs.poiesis.recipe.alloy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.CountedIngredient;

import java.util.List;

/** A normalized alloy recipe imported from the currently loaded mod set. */
public record AlloyDefinition(
        ResourceLocation recipeId,
        ResourceLocation familyId,
        List<CountedIngredient> inputs,
        ItemStack result,
        int duration,
        int energyPerTick
) {
    public AlloyDefinition {
        inputs = List.copyOf(inputs);
        result = result.copy();
    }
}
