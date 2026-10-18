package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

public final class GasGenerationRecipeSerializer
        implements RecipeSerializer<GasGenerationRecipe> {
    @Override
    public GasGenerationRecipe fromJson(ResourceLocation id, JsonObject json) {
        return new GasGenerationRecipe(
                id,
                FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "fluid")),
                GsonHelper.getAsInt(json, "energy_per_mb"),
                GsonHelper.getAsInt(json, "energy_per_tick")
        );
    }

    @Override
    public GasGenerationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return new GasGenerationRecipe(
                id,
                FluidIngredient.fromNetwork(buffer),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GasGenerationRecipe recipe) {
        recipe.fluid().toNetwork(buffer);
        buffer.writeVarInt(recipe.energyPerMb());
        buffer.writeVarInt(recipe.energyPerTick());
    }
}
