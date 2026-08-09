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

public final class ModuleFabricationRecipeSerializer implements RecipeSerializer<ModuleFabricationRecipe> {
    @Override
    public ModuleFabricationRecipe fromJson(ResourceLocation id, JsonObject json) {
        CountedIngredient base = CountedIngredient.fromJson(
                GsonHelper.getAsJsonObject(json, "module_base")
        );

        List<CountedIngredient> materials = new ArrayList<>();
        JsonArray array = GsonHelper.getAsJsonArray(json, "materials");
        for (JsonElement element : array) {
            materials.add(CountedIngredient.fromJson(element.getAsJsonObject()));
        }

        ItemStack result = CraftingHelper.getItemStack(
                GsonHelper.getAsJsonObject(json, "result"), true
        );
        int duration = GsonHelper.getAsInt(json, "duration", 200);
        int energyPerTick = GsonHelper.getAsInt(json, "energy_per_tick", 20);

        return new ModuleFabricationRecipe(id, base, materials, result, duration, energyPerTick);
    }

    @Override
    public ModuleFabricationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        CountedIngredient base = CountedIngredient.fromNetwork(buffer);
        int size = buffer.readVarInt();
        List<CountedIngredient> materials = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            materials.add(CountedIngredient.fromNetwork(buffer));
        }
        ItemStack result = buffer.readItem();
        int duration = buffer.readVarInt();
        int energyPerTick = buffer.readVarInt();
        return new ModuleFabricationRecipe(id, base, materials, result, duration, energyPerTick);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, ModuleFabricationRecipe recipe) {
        recipe.moduleBase().toNetwork(buffer);
        buffer.writeVarInt(recipe.materials().size());
        for (CountedIngredient material : recipe.materials()) {
            material.toNetwork(buffer);
        }
        buffer.writeItem(recipe.result());
        buffer.writeVarInt(recipe.duration());
        buffer.writeVarInt(recipe.energyPerTick());
    }
}
