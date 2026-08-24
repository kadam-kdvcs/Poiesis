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
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

public final class GasificationRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final CountedIngredient input;
    private final ItemStack result;
    private final FluidStack gasOutput;
    private final int duration;
    private final int minimumTemperature;

    public GasificationRecipe(
            ResourceLocation id,
            CountedIngredient input,
            ItemStack result,
            FluidStack gasOutput,
            int duration,
            int minimumTemperature
    ) {
        if (id == null || input == null || result == null
                || gasOutput == null || gasOutput.isEmpty()) {
            throw new IllegalArgumentException("Gasification recipe values cannot be empty");
        }
        if (duration <= 0 || minimumTemperature <= 0 || minimumTemperature > 900) {
            throw new IllegalArgumentException("Invalid gasification duration or temperature");
        }

        this.id = id;
        this.input = input;
        this.result = result.copy();
        this.gasOutput = gasOutput.copy();
        this.duration = duration;
        this.minimumTemperature = minimumTemperature;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() > 0 && input.test(container.getItem(0));
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
        return ModRecipeSerializers.GASIFICATION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.GASIFICATION.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    public CountedIngredient input() {
        return input;
    }

    public ItemStack result() {
        return result.copy();
    }

    public FluidStack gasOutput() {
        return gasOutput.copy();
    }

    public int duration() {
        return duration;
    }

    public int minimumTemperature() {
        return minimumTemperature;
    }
}
