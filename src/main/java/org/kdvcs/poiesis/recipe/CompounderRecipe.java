package org.kdvcs.poiesis.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.List;

public final class CompounderRecipe implements MaterialProcessRecipe {

    public static final int MAX_INPUTS = 2;

    private final ResourceLocation id;
    private final List<CountedIngredient> inputs;
    private final ItemStack result;
    private final ResourceLocation requiredCapability;
    private final int duration;
    private final int baseEnergyPerTick;

    public CompounderRecipe(
            ResourceLocation id,
            List<CountedIngredient> inputs,
            ItemStack result,
            ResourceLocation requiredCapability,
            int duration,
            int baseEnergyPerTick
    ) {
        if (id == null || inputs == null || inputs.isEmpty() || inputs.size() > MAX_INPUTS) {
            throw new IllegalArgumentException("Compounder recipe requires 1-" + MAX_INPUTS + " inputs");
        }

        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Compounder result cannot be empty");
        }

        if (duration <= 0) {
            throw new IllegalArgumentException("Compounder duration must be greater than zero");
        }

        if (baseEnergyPerTick < 0) {
            throw new IllegalArgumentException("Compounder energy cannot be negative");
        }

        this.id = id;
        this.inputs = List.copyOf(inputs);
        this.result = result.copy();
        this.requiredCapability = requiredCapability;
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
        return ModRecipeSerializers.COMPOUNDING.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.COMPOUNDING.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();

        for (CountedIngredient input : inputs) {
            ingredients.add(input.ingredient());
        }

        return ingredients;
    }

    public List<CountedIngredient> inputs() {
        return inputs;
    }

    public ItemStack result() {
        return result.copy();
    }

    public ResourceLocation requiredCapability() {
        return requiredCapability;
    }

    public boolean isStandard() {
        return requiredCapability == null;
    }

    public int duration() {
        return duration;
    }

    public int baseEnergyPerTick() {
        return baseEnergyPerTick;
    }
}
