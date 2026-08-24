package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.crafting.CraftingHelper;

public record ChanceOutput(ItemStack stack, double chance) {
    public ChanceOutput {
        if (stack == null || stack.isEmpty()) {
            throw new IllegalArgumentException("Separator output cannot be empty");
        }
        if (!Double.isFinite(chance) || chance < 0.0D || chance > 1.0D) {
            throw new IllegalArgumentException("Separator output chance must be between 0 and 1");
        }
        stack = stack.copy();
    }

    public static ChanceOutput fromJson(JsonObject json) {
        return new ChanceOutput(
                CraftingHelper.getItemStack(json, true),
                GsonHelper.getAsDouble(json, "chance", 1.0D)
        );
    }

    public static ChanceOutput fromNetwork(FriendlyByteBuf buffer) {
        return new ChanceOutput(buffer.readItem(), buffer.readDouble());
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeItem(stack);
        buffer.writeDouble(chance);
    }

    @Override
    public ItemStack stack() {
        return stack.copy();
    }
}
