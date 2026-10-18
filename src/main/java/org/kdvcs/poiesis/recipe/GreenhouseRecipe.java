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

import java.util.List;

/** Industrial crop batch: planting materials, nutrient, water and nutrient fluid. */
public final class GreenhouseRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<CountedIngredient> inputs;
    private final CountedIngredient nutrient;
    private final int waterAmount;
    private final FluidIngredient nutrientFluid;
    private final List<ItemStack> results;
    private final FluidStack fluidOutput;
    private final int duration;
    private final int baseEnergyPerTick;

    public GreenhouseRecipe(
            ResourceLocation id,
            List<CountedIngredient> inputs,
            CountedIngredient nutrient,
            int waterAmount,
            FluidIngredient nutrientFluid,
            List<ItemStack> results,
            FluidStack fluidOutput,
            int duration,
            int baseEnergyPerTick
    ) {
        if (id == null || inputs == null || inputs.isEmpty() || inputs.size() > 4
                || nutrient == null || nutrientFluid == null
                || results == null || results.isEmpty() || results.size() > 2
                || results.stream().anyMatch(ItemStack::isEmpty)
                || fluidOutput == null || fluidOutput.isEmpty()) {
            throw new IllegalArgumentException("Invalid Greenhouse recipe values");
        }
        if (waterAmount <= 0 || duration <= 0 || baseEnergyPerTick <= 0) {
            throw new IllegalArgumentException("Greenhouse recipe amounts must be positive");
        }
        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.nutrient = nutrient;
        this.waterAmount = waterAmount;
        this.nutrientFluid = nutrientFluid;
        this.results = results.stream().map(ItemStack::copy).toList();
        this.fluidOutput = fluidOutput.copy();
        this.duration = duration;
        this.baseEnergyPerTick = baseEnergyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() >= 5 && nutrient.test(container.getItem(4));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return results.get(0).copy();
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return results.get(0).copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.GREENHOUSE.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.GREENHOUSE.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        inputs.forEach(input -> ingredients.add(input.ingredient()));
        ingredients.add(nutrient.ingredient());
        return ingredients;
    }

    public List<CountedIngredient> inputs() { return inputs; }
    public CountedIngredient nutrient() { return nutrient; }
    public int waterAmount() { return waterAmount; }
    public FluidIngredient nutrientFluid() { return nutrientFluid; }
    public List<ItemStack> results() { return results.stream().map(ItemStack::copy).toList(); }
    public FluidStack fluidOutput() { return fluidOutput.copy(); }
    public int duration() { return duration; }
    public int baseEnergyPerTick() { return baseEnergyPerTick; }
}
