package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.recipe.GasificationRecipe;
import org.kdvcs.poiesis.recipe.GasGenerationRecipe;
import org.kdvcs.poiesis.recipe.LeachingRecipe;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipe;

/**
 * JEI recipe types are deliberately separate from Minecraft RecipeType.
 *
 * Minecraft recipe types and JEI recipe pages are kept separate here.
 *
 * Mechanical Processing and Compounder recipes are split into specialization
 * pages without leaking JEI types into runtime recipe classes.
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
                    "compounder/standard",
                    CompounderRecipe.class
            );

    public static final RecipeType<CompounderRecipe> HIGH_SHEAR_COMPOUNDING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "compounder/high_shear",
                    CompounderRecipe.class
            );

    public static final RecipeType<SeparatorRecipe> DRY_SEPARATING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "separator/dry",
                    SeparatorRecipe.class
            );

    public static final RecipeType<SeparatorRecipe> WET_SEPARATING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "separator/wet",
                    SeparatorRecipe.class
            );

    public static final RecipeType<SeparatorRecipe> RINSING_SEPARATING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "separator/rinsing",
                    SeparatorRecipe.class
            );

    public static final RecipeType<SeparatorRecipe> ELECTROSTATIC_SEPARATING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "separator/electrostatic",
                    SeparatorRecipe.class
            );

    public static final RecipeType<ModuleFabricationRecipe> SPECIALIZATION_MODULE_FABRICATION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "module_fabricator/specialization",
                    ModuleFabricationRecipe.class
            );

    public static final RecipeType<ModuleFabricationRecipe> PERFORMANCE_MODULE_FABRICATION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "module_fabricator/performance",
                    ModuleFabricationRecipe.class
            );

    public static final RecipeType<AdsorptionRecipe> ADSORPTION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "adsorption_column",
                    AdsorptionRecipe.class
            );

    public static final RecipeType<GasificationRecipe> GASIFICATION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "gasifier",
                    GasificationRecipe.class
            );

    public static final RecipeType<GasGenerationRecipe> GAS_GENERATION =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "gas_generator",
                    GasGenerationRecipe.class
            );

    public static final RecipeType<MicrobialFuelCellRecipe> MICROBIAL_FUEL_CELL =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "microbial_fuel_cell",
                    MicrobialFuelCellRecipe.class
            );

    public static final RecipeType<LeachingRecipe> LEACHING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "leacher",
                    LeachingRecipe.class
            );

    public static final RecipeType<GreenhouseRecipe> GREENHOUSE =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "greenhouse",
                    GreenhouseRecipe.class
            );

    public static final RecipeType<AlloyTableRecipe> ALLOYING =
            RecipeType.create(
                    Poiesis.MOD_ID,
                    "alloy_factory",
                    AlloyTableRecipe.class
            );

    private PoiesisJeiRecipeTypes() {
    }
}
