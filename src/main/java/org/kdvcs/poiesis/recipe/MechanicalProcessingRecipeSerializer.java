package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.CraftingHelper;

public final class MechanicalProcessingRecipeSerializer
        implements RecipeSerializer<MechanicalProcessingRecipe> {

    @Override
    public MechanicalProcessingRecipe fromJson(
            ResourceLocation id,
            JsonObject json
    ) {
        CountedIngredient input =
                CountedIngredient.fromJson(
                        GsonHelper.getAsJsonObject(
                                json,
                                "input"
                        )
                );

        ItemStack result =
                CraftingHelper.getItemStack(
                        GsonHelper.getAsJsonObject(
                                json,
                                "result"
                        ),
                        true
                );

        ResourceLocation requiredCapability =
                new ResourceLocation(
                        GsonHelper.getAsString(
                                json,
                                "required_capability"
                        )
                );

        int duration = GsonHelper.getAsInt(
                json,
                "duration",
                200
        );

        int energyPerTick = GsonHelper.getAsInt(
                json,
                "energy_per_tick",
                20
        );

        return new MechanicalProcessingRecipe(
                id,
                input,
                result,
                requiredCapability,
                duration,
                energyPerTick
        );
    }

    @Override
    public MechanicalProcessingRecipe fromNetwork(
            ResourceLocation id,
            FriendlyByteBuf buffer
    ) {
        CountedIngredient input =
                CountedIngredient.fromNetwork(buffer);

        ItemStack result =
                buffer.readItem();

        ResourceLocation requiredCapability =
                buffer.readResourceLocation();

        int duration =
                buffer.readVarInt();

        int energyPerTick =
                buffer.readVarInt();

        return new MechanicalProcessingRecipe(
                id,
                input,
                result,
                requiredCapability,
                duration,
                energyPerTick
        );
    }

    @Override
    public void toNetwork(
            FriendlyByteBuf buffer,
            MechanicalProcessingRecipe recipe
    ) {
        recipe.input().toNetwork(buffer);

        buffer.writeItem(
                recipe.result()
        );

        buffer.writeResourceLocation(
                recipe.requiredCapability()
        );

        buffer.writeVarInt(
                recipe.duration()
        );

        buffer.writeVarInt(
                recipe.baseEnergyPerTick()
        );
    }
}