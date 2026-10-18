package org.kdvcs.poiesis.recipe.alloy;

import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.kdvcs.poiesis.recipe.CountedIngredient;

import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

/** One canonical material whose ingot and dust forms are interchangeable. */
public record AlloyMaterial(String material, int count) {
    public AlloyMaterial {
        String value = material == null ? "" : material;
        boolean valid = switch (descriptorType(value)) {
            case "item", "tag" -> ResourceLocation.tryParse(descriptorValue(value)) != null;
            case "items", "tags" -> Arrays.stream(descriptorValue(value).split(","))
                    .map(String::trim)
                    .allMatch(id -> ResourceLocation.tryParse(id) != null);
            default -> !descriptorValue(value).isBlank()
                    && ResourceLocation.isValidPath(descriptorValue(value));
        };
        if (!valid) {
            throw new IllegalArgumentException("Invalid alloy material: " + material);
        }
        if (count <= 0) {
            throw new IllegalArgumentException("Alloy material count must be positive");
        }
    }

    public CountedIngredient asIngredient() {
        if (material.startsWith("tags:")) {
            List<Ingredient> alternatives = Arrays.stream(
                            material.substring("tags:".length()).split(","))
                    .map(String::trim)
                    .map(ResourceLocation::tryParse)
                    .map(id -> Ingredient.of(TagKey.create(Registries.ITEM, id)))
                    .collect(Collectors.toList());
            return new CountedIngredient(mergeAvailable(alternatives), count);
        }
        if (material.startsWith("items:")) {
            List<Ingredient> alternatives = Arrays.stream(
                            material.substring("items:".length()).split(","))
                    .map(String::trim)
                    .map(ResourceLocation::tryParse)
                    .map(id -> {
                        if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                            throw new IllegalArgumentException("Unknown alloy item ingredient: " + material);
                        }
                        return Ingredient.of(BuiltInRegistries.ITEM.get(id));
                    })
                    .collect(Collectors.toList());
            return new CountedIngredient(mergeAvailable(alternatives), count);
        }
        if (material.startsWith("item:")) {
            ResourceLocation id = ResourceLocation.tryParse(material.substring("item:".length()));
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                throw new IllegalArgumentException("Unknown alloy item ingredient: " + material);
            }
            return new CountedIngredient(
                    Ingredient.of(BuiltInRegistries.ITEM.get(id)),
                    count
            );
        }
        if (material.startsWith("tag:")) {
            ResourceLocation id = ResourceLocation.tryParse(material.substring("tag:".length()));
            if (id == null) {
                throw new IllegalArgumentException("Invalid alloy tag ingredient: " + material);
            }
            return new CountedIngredient(
                    Ingredient.of(TagKey.create(Registries.ITEM, id)),
                    count
            );
        }

        String canonical = material.startsWith("material:")
                ? material.substring("material:".length())
                : material;
        TagKey<Item> ingots = tag("ingots/" + canonical);
        TagKey<Item> dusts = tag("dusts/" + canonical);
        return new CountedIngredient(
                mergeAvailable(List.of(
                        Ingredient.of(ingots),
                        Ingredient.of(dusts)
                )),
                count
        );
    }

    /**
     * Optional compatibility tags may not exist in the current mod set.
     * Drop empty alternatives so JEI never receives a blank ingredient entry.
     */
    private static Ingredient mergeAvailable(List<Ingredient> alternatives) {
        // Ingredient#isEmpty() can remain false for a tag-backed ingredient
        // until its tag values are dissolved. Resolve the stacks here so an
        // optional compatibility tag with no registered items is truly
        // removed before it reaches the machine or JEI.
        List<Ingredient> available = alternatives.stream()
                .filter(ingredient -> ingredient.getItems().length > 0)
                .toList();
        if (available.isEmpty()) {
            throw new IllegalArgumentException("Alloy material has no registered item alternatives");
        }
        return available.size() == 1
                ? available.get(0)
                : Ingredient.merge(available);
    }

    private static String descriptorType(String value) {
        if (value.startsWith("tags:")) return "tags";
        if (value.startsWith("items:")) return "items";
        if (value.startsWith("item:")) return "item";
        if (value.startsWith("tag:")) return "tag";
        return "material";
    }

    private static String descriptorValue(String value) {
        int separator = value.indexOf(':');
        return separator >= 0 ? value.substring(separator + 1) : value;
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(
                Registries.ITEM,
                new ResourceLocation("forge", path)
        );
    }
}
