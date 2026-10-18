package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.GasificationRecipe;
import org.kdvcs.poiesis.recipe.GasGenerationRecipe;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;
import org.kdvcs.poiesis.recipe.LeachingRecipe;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipe;

public final class ModRecipeTypes {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, Poiesis.MOD_ID);
    public static final RegistryObject<RecipeType<MechanicalProcessingRecipe>> MECHANICAL_PROCESSING =
            RECIPE_TYPES.register("mechanical_processing", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":mechanical_processing"; }
            });
    public static final RegistryObject<RecipeType<ModuleFabricationRecipe>> MODULE_FABRICATION =
            RECIPE_TYPES.register("module_fabrication", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":module_fabrication"; }
            });
    public static final RegistryObject<RecipeType<CompounderRecipe>> COMPOUNDING =
            RECIPE_TYPES.register("compounding", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":compounding"; }
            });
    public static final RegistryObject<RecipeType<SeparatorRecipe>> SEPARATING =
            RECIPE_TYPES.register("separating", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":separating"; }
            });
    public static final RegistryObject<RecipeType<AdsorptionRecipe>> ADSORPTION =
            RECIPE_TYPES.register("adsorption", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":adsorption"; }
            });
    public static final RegistryObject<RecipeType<LeachingRecipe>> LEACHING =
            RECIPE_TYPES.register("leaching", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":leaching"; }
            });
    public static final RegistryObject<RecipeType<GasificationRecipe>> GASIFICATION =
            RECIPE_TYPES.register("gasification", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":gasification"; }
            });
    public static final RegistryObject<RecipeType<GasGenerationRecipe>> GAS_GENERATION =
            RECIPE_TYPES.register("gas_generation", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":gas_generation"; }
            });
    public static final RegistryObject<RecipeType<MicrobialFuelCellRecipe>>
            MICROBIAL_FUEL_CELL =
            RECIPE_TYPES.register("microbial_fuel_cell", () -> new RecipeType<>() {
                @Override public String toString() {
                    return Poiesis.MOD_ID + ":microbial_fuel_cell";
                }
            });
    public static final RegistryObject<RecipeType<AlloyTableRecipe>> ALLOYING =
            RECIPE_TYPES.register("alloying", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":alloying"; }
            });
    public static final RegistryObject<RecipeType<GreenhouseRecipe>> GREENHOUSE =
            RECIPE_TYPES.register("greenhouse", () -> new RecipeType<>() {
                @Override public String toString() { return Poiesis.MOD_ID + ":greenhouse"; }
            });
    private ModRecipeTypes() { }
    public static void register(IEventBus eventBus) { RECIPE_TYPES.register(eventBus); }
}
