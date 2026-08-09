package org.kdvcs.poiesis.registry;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipeSerializer;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipeSerializer;

public final class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, Poiesis.MOD_ID);
    public static final RegistryObject<RecipeSerializer<?>> MECHANICAL_PROCESSING =
            RECIPE_SERIALIZERS.register("mechanical_processing", MechanicalProcessingRecipeSerializer::new);
    public static final RegistryObject<RecipeSerializer<?>> MODULE_FABRICATION =
            RECIPE_SERIALIZERS.register("module_fabrication", ModuleFabricationRecipeSerializer::new);
    private ModRecipeSerializers() { }
    public static void register(IEventBus eventBus) { RECIPE_SERIALIZERS.register(eventBus); }
}
