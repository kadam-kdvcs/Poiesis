package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonArray;
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

import java.util.ArrayList;
import java.util.List;

public final class GreenhouseRecipeSerializer implements RecipeSerializer<GreenhouseRecipe> {
    @Override
    public GreenhouseRecipe fromJson(ResourceLocation id, JsonObject json) {
        JsonArray inputJson = GsonHelper.getAsJsonArray(json, "inputs");
        if (inputJson.isEmpty() || inputJson.size() > 4) {
            throw new JsonSyntaxException("Greenhouse recipes require 1-4 planting inputs");
        }
        List<CountedIngredient> inputs = new ArrayList<>();
        inputJson.forEach(value -> inputs.add(CountedIngredient.fromJson(value.getAsJsonObject())));

        JsonArray resultJson = GsonHelper.getAsJsonArray(json, "results");
        if (resultJson.isEmpty() || resultJson.size() > 2) {
            throw new JsonSyntaxException("Greenhouse recipes require 1-2 item results");
        }
        List<ItemStack> results = new ArrayList<>();
        resultJson.forEach(value -> results.add(itemStack(value.getAsJsonObject())));

        return new GreenhouseRecipe(
                id,
                inputs,
                CountedIngredient.fromJson(GsonHelper.getAsJsonObject(json, "nutrient")),
                GsonHelper.getAsInt(json, "water"),
                FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "nutrient_fluid")),
                results,
                fluidStack(GsonHelper.getAsJsonObject(json, "fluid_output")),
                GsonHelper.getAsInt(json, "duration", 1_200),
                GsonHelper.getAsInt(json, "energy_per_tick", 120)
        );
    }

    @Override
    public GreenhouseRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int inputCount = buffer.readVarInt();
        List<CountedIngredient> inputs = new ArrayList<>(inputCount);
        for (int index = 0; index < inputCount; index++) {
            inputs.add(CountedIngredient.fromNetwork(buffer));
        }
        CountedIngredient nutrient = CountedIngredient.fromNetwork(buffer);
        int waterAmount = buffer.readVarInt();
        FluidIngredient nutrientFluid = FluidIngredient.fromNetwork(buffer);
        int resultCount = buffer.readVarInt();
        List<ItemStack> results = new ArrayList<>(resultCount);
        for (int index = 0; index < resultCount; index++) {
            results.add(buffer.readItem());
        }
        return new GreenhouseRecipe(
                id, inputs, nutrient, waterAmount, nutrientFluid, results,
                buffer.readFluidStack(), buffer.readVarInt(), buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GreenhouseRecipe recipe) {
        buffer.writeVarInt(recipe.inputs().size());
        recipe.inputs().forEach(input -> input.toNetwork(buffer));
        recipe.nutrient().toNetwork(buffer);
        buffer.writeVarInt(recipe.waterAmount());
        recipe.nutrientFluid().toNetwork(buffer);
        buffer.writeVarInt(recipe.results().size());
        recipe.results().forEach(buffer::writeItem);
        buffer.writeFluidStack(recipe.fluidOutput());
        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.baseEnergyPerTick());
    }

    private static ItemStack itemStack(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        var item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) throw new JsonSyntaxException("Unknown Greenhouse result item '" + id + "'");
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count <= 0) throw new JsonSyntaxException("Greenhouse result count must be positive");
        return new ItemStack(item, count);
    }

    private static FluidStack fluidStack(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) throw new JsonSyntaxException("Unknown Greenhouse output fluid '" + id + "'");
        int amount = GsonHelper.getAsInt(json, "amount");
        if (amount <= 0) throw new JsonSyntaxException("Greenhouse fluid output must be positive");
        return new FluidStack(fluid, amount);
    }
}
