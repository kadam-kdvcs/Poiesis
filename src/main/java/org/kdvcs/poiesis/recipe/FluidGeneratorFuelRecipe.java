package org.kdvcs.poiesis.recipe;

import net.minecraft.resources.ResourceLocation;

/** Contract consumed by the common fluid-generator runtime. */
public interface FluidGeneratorFuelRecipe {
    ResourceLocation getId();

    FluidIngredient fluid();

    int energyPerMb();

    int energyPerTick();
}
