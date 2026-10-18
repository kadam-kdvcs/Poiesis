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

/** One electrochemical batch consumed by the Microbial Fuel Cell. */
public final class MicrobialFuelCellRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final FluidIngredient anolyte;
    private final FluidIngredient catholyte;
    private final int totalEnergy;
    private final int energyPerTick;

    public MicrobialFuelCellRecipe(
            ResourceLocation id,
            FluidIngredient anolyte,
            FluidIngredient catholyte,
            int totalEnergy,
            int energyPerTick
    ) {
        if (id == null || anolyte == null || catholyte == null
                || totalEnergy <= 0 || energyPerTick <= 0) {
            throw new IllegalArgumentException("Invalid microbial fuel cell recipe values");
        }
        this.id = id;
        this.anolyte = anolyte;
        this.catholyte = catholyte;
        this.totalEnergy = totalEnergy;
        this.energyPerTick = energyPerTick;
    }

    @Override public boolean matches(Container container, Level level) { return false; }
    @Override public ItemStack assemble(Container container, RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public boolean canCraftInDimensions(int width, int height) { return true; }
    @Override public ItemStack getResultItem(RegistryAccess access) { return ItemStack.EMPTY; }
    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.MICROBIAL_FUEL_CELL.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.MICROBIAL_FUEL_CELL.get(); }

    public FluidIngredient anolyte() { return anolyte; }
    public FluidIngredient catholyte() { return catholyte; }
    public int totalEnergy() { return totalEnergy; }
    public int energyPerTick() { return energyPerTick; }
}
