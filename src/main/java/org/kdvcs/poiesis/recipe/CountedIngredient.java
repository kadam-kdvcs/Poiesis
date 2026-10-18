package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public record CountedIngredient(
        Ingredient ingredient,
        int count,
        boolean consumes
) {

    public CountedIngredient(Ingredient ingredient, int count) {
        this(ingredient, count, true);
    }

    public CountedIngredient {
        if (ingredient == null || ingredient.isEmpty()) {
            throw new IllegalArgumentException(
                    "Counted ingredient cannot be empty"
            );
        }

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Ingredient count must be greater than zero"
            );
        }
    }

    public static CountedIngredient fromJson(
            JsonObject json
    ) {
        Ingredient ingredient =
                Ingredient.fromJson(json);

        int count = GsonHelper.getAsInt(
                json,
                "count",
                1
        );
        boolean consumes = GsonHelper.getAsBoolean(
                json,
                "consume",
                true
        );

        return new CountedIngredient(
                ingredient,
                count,
                consumes
        );
    }

    public static CountedIngredient fromNetwork(
            FriendlyByteBuf buffer
    ) {
        Ingredient ingredient =
                Ingredient.fromNetwork(buffer);

        int count = buffer.readVarInt();

        return new CountedIngredient(
                ingredient,
                count,
                buffer.readBoolean()
        );
    }

    public void toNetwork(
            FriendlyByteBuf buffer
    ) {
        ingredient.toNetwork(buffer);
        buffer.writeVarInt(count);
        buffer.writeBoolean(consumes);
    }

    public boolean test(ItemStack stack) {
        return ingredient.test(stack)
                && stack.getCount() >= count;
    }
}
