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

public final class MechanicalProcessingRecipe
        implements Recipe<Container> {

    private final ResourceLocation id;
    private final CountedIngredient input;
    private final ItemStack result;
    private final ResourceLocation requiredCapability;
    private final int duration;
    private final int baseEnergyPerTick;

    public MechanicalProcessingRecipe(
            ResourceLocation id,
            CountedIngredient input,
            ItemStack result,
            ResourceLocation requiredCapability,
            int duration,
            int baseEnergyPerTick
    ) {
        if (result.isEmpty()) {
            throw new IllegalArgumentException(
                    "Mechanical processing result cannot be empty"
            );
        }

        if (duration <= 0) {
            throw new IllegalArgumentException(
                    "Mechanical processing duration must be greater than zero"
            );
        }

        if (baseEnergyPerTick < 0) {
            throw new IllegalArgumentException(
                    "Mechanical processing energy cannot be negative"
            );
        }

        this.id = id;
        this.input = input;
        this.result = result.copy();
        this.requiredCapability = requiredCapability;
        this.duration = duration;
        this.baseEnergyPerTick = baseEnergyPerTick;
    }

    @Override
    public boolean matches(
            Container container,
            Level level
    ) {
        if (container.getContainerSize() <= 0) {
            return false;
        }

        return input.test(
                container.getItem(0)
        );
    }

    @Override
    public ItemStack assemble(
            Container container,
            RegistryAccess registryAccess
    ) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(
            int width,
            int height
    ) {
        return true;
    }

    @Override
    public ItemStack getResultItem(
            RegistryAccess registryAccess
    ) {
        return result.copy();
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers
                .MECHANICAL_PROCESSING
                .get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes
                .MECHANICAL_PROCESSING
                .get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(
                Ingredient.EMPTY,
                input.ingredient()
        );
    }

    public CountedIngredient input() {
        return input;
    }

    public ItemStack result() {
        return result.copy();
    }

    public ResourceLocation requiredCapability() {
        return requiredCapability;
    }

    public int duration() {
        return duration;
    }

    public int baseEnergyPerTick() {
        return baseEnergyPerTick;
    }
}