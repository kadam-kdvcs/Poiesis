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
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.List;

public final class SeparatorRecipe implements Recipe<Container> {
    public static final int MAX_INPUTS = 2;
    public static final int MAX_OUTPUTS = 4;

    private final ResourceLocation id;
    private final List<CountedIngredient> inputs;
    private final List<ChanceOutput> outputs;
    private final FluidIngredient fluid;
    private final int duration;
    private final int baseEnergyPerTick;

    public SeparatorRecipe(
            ResourceLocation id,
            List<CountedIngredient> inputs,
            List<ChanceOutput> outputs,
            @Nullable FluidIngredient fluid,
            int duration,
            int baseEnergyPerTick
    ) {
        if (id == null || inputs == null || inputs.isEmpty() || inputs.size() > MAX_INPUTS) {
            throw new IllegalArgumentException("Separator recipe requires 1-" + MAX_INPUTS + " item inputs");
        }
        if (outputs == null || outputs.isEmpty() || outputs.size() > MAX_OUTPUTS) {
            throw new IllegalArgumentException("Separator recipe requires 1-" + MAX_OUTPUTS + " outputs");
        }
        if (duration <= 0 || baseEnergyPerTick < 0) {
            throw new IllegalArgumentException("Invalid Separator duration or energy cost");
        }

        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.fluid = fluid;
        this.duration = duration;
        this.baseEnergyPerTick = baseEnergyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        int end = Math.min(container.getContainerSize(), MAX_INPUTS);
        return MaterialPlan.canSatisfy(container, 0, end, inputs);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return outputs.get(0).stack();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputs.get(0).stack();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.SEPARATING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.SEPARATING.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        inputs.forEach(input -> ingredients.add(input.ingredient()));
        return ingredients;
    }

    public List<CountedIngredient> inputs() {
        return inputs;
    }

    public List<ChanceOutput> outputs() {
        return outputs;
    }

    @Nullable
    public FluidIngredient fluid() {
        return fluid;
    }

    public boolean isWet() {
        return fluid != null;
    }

    public int duration() {
        return duration;
    }

    public int baseEnergyPerTick() {
        return baseEnergyPerTick;
    }
}
