package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class MicrobialFuelCellRecipeSerializer
        implements RecipeSerializer<MicrobialFuelCellRecipe> {
    @Override
    public MicrobialFuelCellRecipe fromJson(ResourceLocation id, JsonObject json) {
        return new MicrobialFuelCellRecipe(
                id,
                FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "anolyte")),
                FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "catholyte")),
                GsonHelper.getAsInt(json, "total_energy"),
                GsonHelper.getAsInt(json, "energy_per_tick")
        );
    }

    @Override
    public MicrobialFuelCellRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return new MicrobialFuelCellRecipe(
                id,
                FluidIngredient.fromNetwork(buffer),
                FluidIngredient.fromNetwork(buffer),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, MicrobialFuelCellRecipe recipe) {
        recipe.anolyte().toNetwork(buffer);
        recipe.catholyte().toNetwork(buffer);
        buffer.writeVarInt(recipe.totalEnergy());
        buffer.writeVarInt(recipe.energyPerTick());
    }
}
