package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

public final class LeachingRecipeSerializer implements RecipeSerializer<LeachingRecipe> {
    @Override
    public LeachingRecipe fromJson(ResourceLocation id, JsonObject json) {
        return new LeachingRecipe(
                id,
                FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "fluid_input")),
                CountedIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input")),
                fluidStackFromJson(GsonHelper.getAsJsonObject(json, "fluid_output")),
                itemStackFromJson(json),
                GsonHelper.getAsInt(json, "duration", 300),
                GsonHelper.getAsInt(json, "energy_per_tick", 30)
        );
    }

    @Override
    public LeachingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        return new LeachingRecipe(
                id,
                FluidIngredient.fromNetwork(buffer),
                CountedIngredient.fromNetwork(buffer),
                buffer.readFluidStack(),
                buffer.readItem(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, LeachingRecipe recipe) {
        recipe.fluidInput().toNetwork(buffer);
        recipe.input().toNetwork(buffer);
        buffer.writeFluidStack(recipe.fluidOutput());
        buffer.writeItem(recipe.result());
        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.baseEnergyPerTick());
    }

    private static FluidStack fluidStackFromJson(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) throw new JsonSyntaxException("Unknown output fluid '" + id + "'");
        int amount = GsonHelper.getAsInt(json, "amount");
        if (amount <= 0) throw new JsonSyntaxException("Fluid output amount must be positive");
        return new FluidStack(fluid, amount);
    }

    private static ItemStack itemStackFromJson(JsonObject recipeJson) {
        if (!recipeJson.has("result") || recipeJson.get("result").isJsonNull()) {
            return ItemStack.EMPTY;
        }
        JsonObject json = GsonHelper.getAsJsonObject(recipeJson, "result");
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        var item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) throw new JsonSyntaxException("Unknown result item '" + id + "'");
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count <= 0) throw new JsonSyntaxException("Result count must be positive");
        return new ItemStack(item, count);
    }
}
