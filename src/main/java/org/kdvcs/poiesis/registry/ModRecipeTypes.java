package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;

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
    private ModRecipeTypes() { }
    public static void register(IEventBus eventBus) { RECIPE_TYPES.register(eventBus); }
}
