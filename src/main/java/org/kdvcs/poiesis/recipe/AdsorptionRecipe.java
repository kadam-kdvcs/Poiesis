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

public final class AdsorptionRecipe implements Recipe<Container> {
    public static final int FLUID_PER_BED = 8_000;
    public static final int MAX_BEDS = 6;

    private final ResourceLocation id;
    private final FluidIngredient fluidInput;
    private final CountedIngredient adsorbent;
    private final FluidStack fluidOutput;
    private final ItemStack result;
    private final int duration;
    private final int baseEnergyPerTick;

    public AdsorptionRecipe(
            ResourceLocation id,
            FluidIngredient fluidInput,
            CountedIngredient adsorbent,
            FluidStack fluidOutput,
            ItemStack result,
            int duration,
            int baseEnergyPerTick
    ) {
        if (id == null || fluidInput == null || adsorbent == null
                || fluidOutput == null || fluidOutput.isEmpty()
                || result == null || result.isEmpty()) {
            throw new IllegalArgumentException("Adsorption recipe values cannot be empty");
        }
        if (requiredBeds(fluidInput.amount()) > MAX_BEDS) {
            throw new IllegalArgumentException("Adsorption fluid input exceeds six beds");
        }
        if (duration <= 0 || baseEnergyPerTick <= 0) {
            throw new IllegalArgumentException("Invalid adsorption duration or energy cost");
        }

        this.id = id;
        this.fluidInput = fluidInput;
        this.adsorbent = adsorbent;
        this.fluidOutput = fluidOutput.copy();
        this.result = result.copy();
        this.duration = duration;
        this.baseEnergyPerTick = baseEnergyPerTick;
    }

    public static int requiredBeds(int fluidAmount) {
        return Math.max(1, (fluidAmount + FLUID_PER_BED - 1) / FLUID_PER_BED);
    }

    @Override
    public boolean matches(Container container, Level level) {
        return container.getContainerSize() > 0 && adsorbent.test(container.getItem(0));
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
        return ModRecipeSerializers.ADSORPTION.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipeTypes.ADSORPTION.get();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(adsorbent.ingredient());
        return ingredients;
    }

    public FluidIngredient fluidInput() {
        return fluidInput;
    }

    public CountedIngredient adsorbent() {
        return adsorbent;
    }

    public FluidStack fluidOutput() {
        return fluidOutput.copy();
    }

    public ItemStack result() {
        return result.copy();
    }

    public int requiredBeds() {
        return requiredBeds(fluidInput.amount());
    }

    public int duration() {
        return duration;
    }

    public int baseEnergyPerTick() {
        return baseEnergyPerTick;
    }
}
