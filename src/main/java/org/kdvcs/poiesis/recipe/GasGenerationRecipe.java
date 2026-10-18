package org.kdvcs.poiesis.recipe;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

public final class GasGenerationRecipe
        implements Recipe<Container>, FluidGeneratorFuelRecipe {
    private final ResourceLocation id;
    private final FluidIngredient fluid;
    private final int energyPerMb;
    private final int energyPerTick;

    public GasGenerationRecipe(
            ResourceLocation id,
            FluidIngredient fluid,
            int energyPerMb,
            int energyPerTick
    ) {
        if (id == null || fluid == null || energyPerMb <= 0 || energyPerTick <= 0) {
            throw new IllegalArgumentException("Invalid gas generation recipe values");
        }
        this.id = id;
        this.fluid = fluid;
        this.energyPerMb = energyPerMb;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipeSerializers.GAS_GENERATION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.GAS_GENERATION.get();
    }

    @Override
    public FluidIngredient fluid() {
        return fluid;
    }

    @Override
    public int energyPerMb() {
        return energyPerMb;
    }

    @Override
    public int energyPerTick() {
        return energyPerTick;
    }
}
