package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.AdsorptionRecipeSerializer;
import org.kdvcs.poiesis.recipe.CompounderRecipeSerializer;
import org.kdvcs.poiesis.recipe.GasificationRecipeSerializer;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipeSerializer;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipeSerializer;
import org.kdvcs.poiesis.recipe.SeparatorRecipeSerializer;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipe;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipeSerializer;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Poiesis.MOD_ID);
    public static final RegistryObject<RecipeSerializer<?>> MECHANICAL_PROCESSING =
            RECIPE_SERIALIZERS.register("mechanical_processing", MechanicalProcessingRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> MODULE_FABRICATION =
            RECIPE_SERIALIZERS.register("module_fabrication", ModuleFabricationRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> COMPOUNDING =
            RECIPE_SERIALIZERS.register("compounding", CompounderRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> SEPARATING =
            RECIPE_SERIALIZERS.register("separating", SeparatorRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> ADSORPTION =
            RECIPE_SERIALIZERS.register("adsorption", AdsorptionRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> GASIFICATION =
            RECIPE_SERIALIZERS.register("gasification", GasificationRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<AlloyTableRecipe>> ALLOYING =
            RECIPE_SERIALIZERS.register("alloying", AlloyTableRecipeSerializer::new);
    private ModRecipeSerializers() { }
    public static void register(IEventBus eventBus) { RECIPE_SERIALIZERS.register(eventBus); }
}
