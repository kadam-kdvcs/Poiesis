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
            case "items" -> Arrays.stream(descriptorValue(value).split(","))
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
            return new CountedIngredient(Ingredient.merge(alternatives), count);
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
                Ingredient.merge(List.of(
                        Ingredient.of(ingots),
                        Ingredient.of(dusts)
                )),
                count
        );
    }

    private static String descriptorType(String value) {
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
