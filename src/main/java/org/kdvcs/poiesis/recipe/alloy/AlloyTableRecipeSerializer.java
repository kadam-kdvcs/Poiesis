package org.kdvcs.poiesis.recipe.alloy;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.List;

public final class AlloyTableRecipeSerializer implements RecipeSerializer<AlloyTableRecipe> {
    @Override
    public AlloyTableRecipe fromJson(ResourceLocation id, JsonObject json) {
        List<AlloyMaterial> materials = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(json, "materials");
        for (JsonElement element : array) {
            JsonObject material = element.getAsJsonObject();
            int count = GsonHelper.getAsInt(material, "count", 1);
            String descriptor;
            if (material.has("material")) {
                descriptor = "material:" + GsonHelper.getAsString(material, "material");
            } else if (material.has("item")) {
                descriptor = "item:" + GsonHelper.getAsString(material, "item");
            } else if (material.has("tag")) {
                descriptor = "tag:" + GsonHelper.getAsString(material, "tag");
            } else if (material.has("items")) {
                JsonArray items = GsonHelper.getAsJsonArray(material, "items");
                if (items.isEmpty()) {
                    throw new IllegalArgumentException("Alloy material items array cannot be empty");
                }
                StringBuilder ids = new StringBuilder("items:");
                for (JsonElement item : items) {
                    if (!item.isJsonPrimitive() || !item.getAsJsonPrimitive().isString()) {
                        throw new IllegalArgumentException("Alloy material items must be item id strings");
                    }
                    if (ids.length() > "items:".length()) ids.append(',');
                    ids.append(item.getAsString());
                }
                descriptor = ids.toString();
            } else if (material.has("tags")) {
                JsonArray tags = GsonHelper.getAsJsonArray(material, "tags");
                if (tags.isEmpty()) {
                    throw new IllegalArgumentException("Alloy material tags array cannot be empty");
                }
                StringBuilder ids = new StringBuilder("tags:");
                for (JsonElement tag : tags) {
                    if (!tag.isJsonPrimitive() || !tag.getAsJsonPrimitive().isString()) {
                        throw new IllegalArgumentException("Alloy material tags must be tag id strings");
                    }
                    if (ids.length() > "tags:".length()) ids.append(',');
                    ids.append(tag.getAsString());
                }
                descriptor = ids.toString();
            } else {
                throw new IllegalArgumentException(
                        "Alloy material must define material, item, tag, items, or tags"
                );
            }
            materials.add(new AlloyMaterial(descriptor, count));
        }

        JsonObject result = GsonHelper.getAsJsonObject(json, "result");
        ResourceLocation resultTag = new ResourceLocation(
                GsonHelper.getAsString(result, "tag")
        );
        int resultCount = GsonHelper.getAsInt(result, "count", 1);
        int duration = GsonHelper.getAsInt(json, "duration", 240);
        int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 80);
        return new AlloyTableRecipe(
                id, materials, resultTag, resultCount, duration, energyPerTick
        );
    }

    @Override
    public AlloyTableRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        List<AlloyMaterial> materials = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            materials.add(new AlloyMaterial(buffer.readUtf(), buffer.readVarInt()));
        }
        return new AlloyTableRecipe(
                id,
                materials,
                buffer.readResourceLocation(),
                buffer.readVarInt(),
                buffer.readVarInt(),
                buffer.readVarInt()
        );
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, AlloyTableRecipe recipe) {
        buffer.writeVarInt(recipe.materials().size());
        for (AlloyMaterial material : recipe.materials()) {
            buffer.writeUtf(material.material());
            buffer.writeVarInt(material.count());
        }
        buffer.writeResourceLocation(recipe.resultTag());
        buffer.writeVarInt(recipe.resultCount());
        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.energyPerTick());
    }
}
