package org.kdvcs.poiesis.recipe;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

/**
 * Common contract for powered material-processing recipes.
 *
 * Execution belongs to the generic process runtime; concrete recipe classes
 * only describe data and their serializer/type identity.
 */
public interface MaterialProcessRecipe extends Recipe<Container> {
    List<CountedIngredient> inputs();
    ItemStack result();
    ResourceLocation requiredCapability();
    int duration();
    int baseEnergyPerTick();

    default boolean isStandard() {
        return requiredCapability() == null;
    }
}
