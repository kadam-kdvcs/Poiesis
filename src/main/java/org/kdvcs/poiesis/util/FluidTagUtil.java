package org.kdvcs.poiesis.util;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

/** Shared fluid-tag lookup used by recipes, JEI integrations and future machines. */
public final class FluidTagUtil {
    private FluidTagUtil() {
    }

    public static TagKey<Fluid> tag(ResourceLocation id) {
        if (id == null) {
            throw new IllegalArgumentException("Fluid tag id cannot be null");
        }
        return TagKey.create(BuiltInRegistries.FLUID.key(), id);
    }

    public static boolean matches(FluidStack stack, ResourceLocation tagId) {
        return stack != null
                && !stack.isEmpty()
                && stack.getFluid().is(tag(tagId));
    }

    public static List<Fluid> values(ResourceLocation tagId) {
        return BuiltInRegistries.FLUID.getTag(tag(tagId))
                .stream()
                .flatMap(named -> named.stream().map(Holder::value))
                .toList();
    }
}
