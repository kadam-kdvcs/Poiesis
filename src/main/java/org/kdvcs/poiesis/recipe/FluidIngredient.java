package org.kdvcs.poiesis.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.kdvcs.poiesis.util.FluidTagUtil;

import java.util.List;

/** Reusable JSON/network fluid ingredient with direct-fluid and fluid-tag forms. */
public record FluidIngredient(
        ResourceLocation id,
        boolean tag,
        int amount
) {
    public FluidIngredient {
        if (id == null) {
            throw new IllegalArgumentException("Fluid ingredient id cannot be null");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Fluid ingredient amount must be greater than zero");
        }
        if (!tag && !ForgeRegistries.FLUIDS.containsKey(id)) {
            throw new IllegalArgumentException("Unknown fluid: " + id);
        }
    }

    public static FluidIngredient fromJson(JsonObject json) {
        boolean hasFluid = json.has("fluid");
        boolean hasTag = json.has("tag");

        if (hasFluid == hasTag) {
            throw new JsonSyntaxException(
                    "Fluid ingredient requires exactly one of 'fluid' or 'tag'"
            );
        }

        ResourceLocation id = new ResourceLocation(
                GsonHelper.getAsString(json, hasTag ? "tag" : "fluid")
        );

        return new FluidIngredient(
                id,
                hasTag,
                GsonHelper.getAsInt(json, "amount", 1_000)
        );
    }

    public static FluidIngredient fromNetwork(FriendlyByteBuf buffer) {
        return new FluidIngredient(
                buffer.readResourceLocation(),
                buffer.readBoolean(),
                buffer.readVarInt()
        );
    }

    public void toNetwork(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeBoolean(tag);
        buffer.writeVarInt(amount);
    }

    public boolean matches(FluidStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() < amount) {
            return false;
        }

        if (tag) {
            return FluidTagUtil.matches(stack, id);
        }

        return stack.getFluid() == ForgeRegistries.FLUIDS.getValue(id);
    }

    public List<FluidStack> displayStacks() {
        List<Fluid> fluids = tag
                ? FluidTagUtil.values(id)
                : List.of(ForgeRegistries.FLUIDS.getValue(id));

        return fluids.stream()
                .filter(fluid -> fluid != null)
                .map(fluid -> new FluidStack(fluid, amount))
                .toList();
    }
}
