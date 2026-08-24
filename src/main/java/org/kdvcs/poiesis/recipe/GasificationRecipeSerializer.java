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

public final class GasificationRecipeSerializer
        implements RecipeSerializer<GasificationRecipe> {

    @Override
    public GasificationRecipe fromJson(ResourceLocation id, JsonObject json) {
        return new GasificationRecipe(
                id,
                CountedIngredient.fromJson(GsonHelper.getAsJsonObject(json, "input")),
                json.has("result")
                        ? itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"))
                        : ItemStack.EMPTY,
                fluidStackFromJson(GsonHelper.getAsJsonObject(json, "gas_output")),
                GsonHelper.getAsInt(json, "duration", 200),
                GsonHelper.getAsInt(json, "minimum_temperature", 650)
        );
    }

    @Override
    public GasificationRecipe fromNetwork(
            ResourceLocation id,
            FriendlyByteBuf buffer
    ) {
        return new GasificationRecipe(
                id,
                CountedIngredient.fromNetwork(buffer),
                buffer.readItem(),
                buffer.readFluidStack(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, GasificationRecipe recipe) {
        recipe.input().toNetwork(buffer);
        buffer.writeItem(recipe.result());
        buffer.writeFluidStack(recipe.gasOutput());
        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.minimumTemperature());
    }

    private static FluidStack fluidStackFromJson(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null) {
            throw new JsonSyntaxException("Unknown gasification output fluid '" + id + "'");
        }
        int amount = GsonHelper.getAsInt(json, "amount");
        if (amount <= 0) {
            throw new JsonSyntaxException("Gasification fluid output must be positive");
        }
        return new FluidStack(fluid, amount);
    }

    private static ItemStack itemStackFromJson(JsonObject json) {
        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "item"));
        var item = ForgeRegistries.ITEMS.getValue(id);
        if (item == null) {
            throw new JsonSyntaxException("Unknown gasification result item '" + id + "'");
        }
        int count = GsonHelper.getAsInt(json, "count", 1);
        if (count <= 0) {
            throw new JsonSyntaxException("Gasification result count must be positive");
        }
        return new ItemStack(item, count);
    }
}
