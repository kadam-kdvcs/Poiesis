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

/** Solid feed and leaching liquid become a mother liquor and an insoluble residue. */
public final class LeachingRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final FluidIngredient fluidInput;
    private final CountedIngredient input;
    private final FluidStack fluidOutput;
    private final ItemStack result;
    private final int duration;
    private final int baseEnergyPerTick;

    public LeachingRecipe(
            ResourceLocation id,
            FluidIngredient fluidInput,
            CountedIngredient input,
            FluidStack fluidOutput,
            ItemStack result,
            int duration,
            int baseEnergyPerTick
    ) {
        if (id == null || fluidInput == null || input == null
                || fluidOutput == null || fluidOutput.isEmpty()
                || result == null) {
            throw new IllegalArgumentException("Leaching recipe values cannot be empty");
        }
        if (duration <= 0 || baseEnergyPerTick <= 0) {
            throw new IllegalArgumentException("Invalid leaching duration or energy cost");
        }
        this.id = id;
        this.fluidInput = fluidInput;
        this.input = input;
        this.fluidOutput = fluidOutput.copy();
        this.result = result.copy();
        this.duration = duration;
        this.baseEnergyPerTick = baseEnergyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() > 0 && input.test(container.getItem(0));
    }

    @Override public ItemStack assemble(Container container, RegistryAccess access) { return result.copy(); }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return result.copy(); }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.LEACHING.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.LEACHING.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input.ingredient());
        return ingredients;
    }

    public FluidIngredient fluidInput() { return fluidInput; }
    public CountedIngredient input() { return input; }
    public FluidStack fluidOutput() { return fluidOutput.copy(); }
    public ItemStack result() { return result.copy(); }
    public int duration() { return duration; }
    public int baseEnergyPerTick() { return baseEnergyPerTick; }
}
