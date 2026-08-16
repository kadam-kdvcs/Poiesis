package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

import java.util.ArrayList;
import java.util.List;

public final class CompounderRecipeSerializer
        implements RecipeSerializer<CompounderRecipe> {

    @Override
    public CompounderRecipe fromJson(
            ResourceLocation id,
            JsonObject json
    ) {
        JsonArray inputArray = GsonHelper.getAsJsonArray(json, "inputs");
        List<CountedIngredient> inputs = new ArrayList<>();

        for (JsonElement element : inputArray) {
            inputs.add(
                    CountedIngredient.fromJson(
                            GsonHelper.convertToJsonObject(element, "input")
                    )
            );
        }

        ItemStack result = CraftingHelper.getItemStack(
                GsonHelper.getAsJsonObject(json, "result"),
                true
        );

        ResourceLocation requiredCapability = null;

        if (json.has("required_capability")) {
            String value = GsonHelper.getAsString(json, "required_capability", "");

            if (!value.isBlank()) {
                requiredCapability = new ResourceLocation(value);
            }
        }

        int duration = GsonHelper.getAsInt(json, "duration", 200);
        int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);

        return new CompounderRecipe(
                id,
                inputs,
                result,
                requiredCapability,
                duration,
                energyPerTick
        );
    }

    @Override
    public CompounderRecipe fromNetwork(
            ResourceLocation id,
            FriendlyByteBuf buffer
    ) {
        int inputCount = buffer.readVarInt();
        List<CountedIngredient> inputs = new ArrayList<>(inputCount);

        for (int i = 0; i < inputCount; i++) {
            inputs.add(CountedIngredient.fromNetwork(buffer));
        }

        ItemStack result = buffer.readItem();

        ResourceLocation requiredCapability =
                buffer.readBoolean()
                        ? buffer.readResourceLocation()
                        : null;

        int duration = buffer.readVarInt();
        int energyPerTick = buffer.readVarInt();

        return new CompounderRecipe(
                id,
                inputs,
                result,
                requiredCapability,
                duration,
                energyPerTick
        );
    }

    @Override
    public void toNetwork(
            FriendlyByteBuf buffer,
            CompounderRecipe recipe
    ) {
        buffer.writeVarInt(recipe.inputs().size());

        for (CountedIngredient input : recipe.inputs()) {
            input.toNetwork(buffer);
        }

        buffer.writeItem(recipe.result());

        buffer.writeBoolean(recipe.requiredCapability() != null);

        if (recipe.requiredCapability() != null) {
            buffer.writeResourceLocation(recipe.requiredCapability());
        }

        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.baseEnergyPerTick());
    }
}
