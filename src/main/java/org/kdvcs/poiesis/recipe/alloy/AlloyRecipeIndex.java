package org.kdvcs.poiesis.recipe.alloy;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

/** Index of Poiesis' manually maintained, data-driven alloy table. */
public final class AlloyRecipeIndex {
    private AlloyRecipeIndex() {
    }

    public static List<AlloyDefinition> discover(Level level) {
        if (level == null) {
            return List.of();
        }
        return level.getRecipeManager()
                .getAllRecipesFor(ModRecipeTypes.ALLOYING.get())
                .stream()
                .flatMap(recipe -> expandOutputVariants(recipe).stream())
                .sorted(Comparator
                        .comparing((AlloyDefinition value) -> value.familyId().toString())
                        .thenComparing(value -> sourcePriority(value.result()))
                        .thenComparing(value -> value.recipeId().toString()))
                .toList();
    }

    public static Optional<AlloyDefinition> find(Level level, ResourceLocation id) {
        if (level == null || id == null) {
            return Optional.empty();
        }

        VariantKey variant = parseVariantId(id).orElse(null);
        if (variant != null) {
            return level.getRecipeManager().byKey(variant.sourceRecipeId())
                    .filter(recipe -> recipe.getType() == ModRecipeTypes.ALLOYING.get())
                    .map(recipe -> (AlloyTableRecipe) recipe)
                    .flatMap(recipe -> withTarget(recipe, variant.targetItemId(), id));
        }

        return level.getRecipeManager().byKey(id)
                .filter(recipe -> recipe.getType() == ModRecipeTypes.ALLOYING.get())
                .map(recipe -> (AlloyTableRecipe) recipe)
                .flatMap(recipe -> expandOutputVariants(recipe).stream().findFirst());
    }

    private static List<AlloyDefinition> expandOutputVariants(AlloyTableRecipe recipe) {
        TagKey<Item> outputTag = TagKey.create(Registries.ITEM, recipe.resultTag());
        return BuiltInRegistries.ITEM.getTag(outputTag)
                .map(values -> values.stream()
                        .map(holder -> BuiltInRegistries.ITEM.getKey(holder.value()))
                        .sorted(Comparator.comparing(ResourceLocation::toString))
                        .map(targetId -> withTarget(
                                recipe,
                                targetId,
                                variantId(recipe.getId(), targetId)
                        ).orElse(null))
                        .filter(value -> value != null)
                        .sorted(Comparator
                                .comparing((AlloyDefinition value) -> sourcePriority(value.result()))
                                .thenComparing(value -> BuiltInRegistries.ITEM
                                        .getKey(value.result().getItem()).toString()))
                        .toList())
                .orElse(List.of());
    }

    private static Optional<AlloyDefinition> withTarget(
            AlloyTableRecipe recipe,
            ResourceLocation targetItemId,
            ResourceLocation selectionId
    ) {
        Item target = BuiltInRegistries.ITEM.get(targetItemId);
        if (target == null || target == Items.AIR) {
            return Optional.empty();
        }
        ItemStack output = new ItemStack(target, recipe.resultCount());
        if (!output.is(TagKey.create(Registries.ITEM, recipe.resultTag()))) {
            return Optional.empty();
        }
        return Optional.of(new AlloyDefinition(
                selectionId,
                recipe.resultTag(),
                recipe.inputs(),
                output,
                recipe.duration(),
                recipe.energyPerTick()
        ));
    }

    private static ResourceLocation variantId(ResourceLocation source, ResourceLocation target) {
        return new ResourceLocation(
                Poiesis.MOD_ID,
                "alloy_variant/" + encodeId(source) + "/" + encodeId(target)
        );
    }

    private static Optional<VariantKey> parseVariantId(ResourceLocation id) {
        if (!id.getNamespace().equals(Poiesis.MOD_ID)
                || !id.getPath().startsWith("alloy_variant/")) {
            return Optional.empty();
        }
        String[] parts = id.getPath().substring("alloy_variant/".length()).split("/", -1);
        if (parts.length != 2) {
            return Optional.empty();
        }
        try {
            ResourceLocation source = ResourceLocation.tryParse(decodeId(parts[0]));
            ResourceLocation target = ResourceLocation.tryParse(decodeId(parts[1]));
            return source == null || target == null
                    ? Optional.empty()
                    : Optional.of(new VariantKey(source, target));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }

    private static String encodeId(ResourceLocation id) {
        return HexFormat.of().formatHex(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static String decodeId(String encoded) {
        return new String(HexFormat.of().parseHex(encoded), StandardCharsets.UTF_8);
    }

    private static int sourcePriority(ItemStack result) {
        return BuiltInRegistries.ITEM.getKey(result.getItem())
                .getNamespace().equals(Poiesis.MOD_ID) ? 0 : 1;
    }

    private record VariantKey(ResourceLocation sourceRecipeId, ResourceLocation targetItemId) {
    }
}
