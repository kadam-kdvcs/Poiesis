package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;

/**
 * JEI recipe types are deliberately separate from Minecraft RecipeType.
 *
 * Minecraft recipe types and JEI recipe pages are kept separate here.
 *
 * MechanicalProcessingRecipe is split into specialization pages, while the
 * Compounder currently has one general JEI page. Future machine recipe pages
 * should be added here without leaking JEI types into runtime recipe classes.
 */
public final class PoiesisJeiRecipeTypes {

    public static final RecipeType<MechanicalProcessingRecipe> PRESSING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "mechanical_processor/pressing",
                    MechanicalProcessingRecipe.class
            );

    public static final RecipeType<MechanicalProcessingRecipe> GRINDING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "mechanical_processor/grinding",
                    MechanicalProcessingRecipe.class
            );

    public static final RecipeType<MechanicalProcessingRecipe> EXTRUSION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "mechanical_processor/extrusion",
                    MechanicalProcessingRecipe.class
            );

    public static final RecipeType<CompounderRecipe> COMPOUNDING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "compounder",
                    CompounderRecipe.class
            );

    private PoiesisJeiRecipeTypes() {
    }
}
