package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class SeparatorRecipeSerializer
        implements RecipeSerializer<SeparatorRecipe> {

    @Override
    public SeparatorRecipe fromJson(ResourceLocation id, JsonObject json) {
        List<CountedIngredient> inputs = new ArrayList<>();
        JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
        for (JsonElement element : inputArray) {
            inputs.add(CountedIngredient.fromJson(
                    GsonHelper.convertToJsonObject(element, "input")
            ));
        }

        List<ChanceOutput> outputs = new ArrayList<>();
        JsonArray outputArray = GsonHelper.getAsJsonArray(json, "outputs");
        for (JsonElement element : outputArray) {
            outputs.add(ChanceOutput.fromJson(
                    GsonHelper.convertToJsonObject(element, "output")
            ));
        }

        FluidIngredient fluid = json.has("fluid")
                ? FluidIngredient.fromJson(GsonHelper.getAsJsonObject(json, "fluid"))
                : null;

        ResourceLocation requiredCapability = null;
        if (json.has("required_capability")) {
            String value = GsonHelper.getAsString(json, "required_capability", "");
            if (!value.isBlank()) {
                requiredCapability = new ResourceLocation(value);
            }
        } else if (fluid != null) {
            // Existing fluid recipes remain wet-specialization recipes.
            requiredCapability = ModItems.WET_SEPARATION;
        }

        return new SeparatorRecipe(
                id,
                inputs,
                outputs,
                fluid,
                requiredCapability,
                GsonHelper.getAsInt(json, "duration", 200),
                GsonHelper.getAsInt(json, "energy_per_tick", 20)
        );
    }

    @Override
    public SeparatorRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int inputCount = buffer.readVarInt();
        List<CountedIngredient> inputs = new ArrayList<>(inputCount);
        for (int i = 0; i < inputCount; i++) {
            inputs.add(CountedIngredient.fromNetwork(buffer));
        }

        int outputCount = buffer.readVarInt();
        List<ChanceOutput> outputs = new ArrayList<>(outputCount);
        for (int i = 0; i < outputCount; i++) {
            outputs.add(ChanceOutput.fromNetwork(buffer));
        }

        FluidIngredient fluid = buffer.readBoolean()
                ? FluidIngredient.fromNetwork(buffer)
                : null;

        ResourceLocation requiredCapability = buffer.readBoolean()
                ? buffer.readResourceLocation()
                : null;

        return new SeparatorRecipe(
                id,
                inputs,
                outputs,
                fluid,
                requiredCapability,
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, SeparatorRecipe recipe) {
        buffer.writeVarInt(recipe.inputs().size());
        recipe.inputs().forEach(input -> input.toNetwork(buffer));

        buffer.writeVarInt(recipe.outputs().size());
        recipe.outputs().forEach(output -> output.toNetwork(buffer));

        buffer.writeBoolean(recipe.fluid() != null);
        if (recipe.fluid() != null) {
            recipe.fluid().toNetwork(buffer);
        }

        buffer.writeBoolean(recipe.requiredCapability() != null);
        if (recipe.requiredCapability() != null) {
            buffer.writeResourceLocation(recipe.requiredCapability());
        }

        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.baseEnergyPerTick());
    }
}
