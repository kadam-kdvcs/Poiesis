package org.kdvcs.poiesis.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.List;

public final class ModuleFabricationRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final CountedIngredient moduleBase;
    private final List<CountedIngredient> materials;
    private final ItemStack result;
    private final int duration;
    private final int energyPerTick;

    public ModuleFabricationRecipe(
            ResourceLocation id,
            CountedIngredient moduleBase,
            List<CountedIngredient> materials,
            ItemStack result,
            int duration,
            int energyPerTick
    ) {
        if (id == null || moduleBase == null || materials == null || result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Invalid module fabrication recipe");
        }
        if (duration <= 0 || energyPerTick < 0) {
            throw new IllegalArgumentException("Invalid fabrication duration or energy cost");
        }
        this.id = id;
        this.moduleBase = moduleBase;
        this.materials = List.copyOf(materials);
        this.result = result.copy();
        this.duration = duration;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() > 0 && moduleBase.test(container.getItem(0));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.MODULE_FABRICATION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.MODULE_FABRICATION.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(moduleBase.ingredient());
        for (CountedIngredient material : materials) {
            ingredients.add(material.ingredient());
        }
        return ingredients;
    }

    public CountedIngredient moduleBase() { return moduleBase; }
    public List<CountedIngredient> materials() { return materials; }
    public ItemStack result() { return result.copy(); }
    public int duration() { return duration; }
    public int energyPerTick() { return energyPerTick; }
}
