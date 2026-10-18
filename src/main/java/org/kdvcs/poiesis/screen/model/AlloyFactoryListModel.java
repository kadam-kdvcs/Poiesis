package org.kdvcs.poiesis.screen.model;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.recipe.alloy.AlloyDefinition;
import org.kdvcs.poiesis.recipe.alloy.AlloyRecipeIndex;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Owns discovery, grouping, variant selection and input-sensitive caching. */
public final class AlloyFactoryListModel {
    private final AlloyFactoryMenu menu;
    private final Map<ResourceLocation, ResourceLocation> chosenVariants = new LinkedHashMap<>();
    private List<AlloyFamily> cachedCatalog = List.of();
    private long nextRecipeRefreshTick = Long.MIN_VALUE;
    private List<AlloyFamily> availableCatalog = List.of();
    private List<AlloyFamily> cachedAvailable = List.of();
    private Map<ResourceLocation, Integer> craftableCounts = Map.of();
    private long cachedInputSignature = Long.MIN_VALUE;

    public AlloyFactoryListModel(AlloyFactoryMenu menu) {
        this.menu = menu;
    }

    public List<AlloyFamily> availableFamilies(ClientLevel level) {
        List<AlloyFamily> catalog = catalog(level);
        long signature = inputSignature();
        if (catalog == availableCatalog && signature == cachedInputSignature) {
            return cachedAvailable;
        }

        Map<ResourceLocation, Integer> counts = new LinkedHashMap<>();
        cachedAvailable = catalog.stream()
                .map(family -> new AlloyFamily(family.familyId(), family.variants().stream()
                        .filter(recipe -> {
                            int batches = menu.craftableBatches(recipe);
                            counts.put(recipe.recipeId(), batches);
                            return batches > 0;
                        })
                        .toList()))
                .filter(family -> !family.variants().isEmpty())
                .toList();
        availableCatalog = catalog;
        cachedInputSignature = signature;
        craftableCounts = Map.copyOf(counts);
        return cachedAvailable;
    }

    public AlloyDefinition chosen(AlloyFamily family) {
        ResourceLocation id = chosenVariants.get(family.familyId());
        if (id != null) {
            for (AlloyDefinition variant : family.variants()) {
                if (variant.recipeId().equals(id)) return variant;
            }
        }
        return family.variants().get(0);
    }

    public void choose(AlloyDefinition recipe) {
        chosenVariants.put(recipe.familyId(), recipe.recipeId());
    }

    public int craftableBatches(AlloyDefinition recipe) {
        return craftableCounts.getOrDefault(recipe.recipeId(), 0);
    }

    private List<AlloyFamily> catalog(ClientLevel level) {
        if (level == null) return List.of();
        long gameTime = level.getGameTime();
        if (gameTime < nextRecipeRefreshTick) return cachedCatalog;

        Map<ResourceLocation, LinkedHashMap<ResourceLocation, AlloyDefinition>> grouped = new LinkedHashMap<>();
        for (AlloyDefinition recipe : AlloyRecipeIndex.discover(level)) {
            ResourceLocation outputId = BuiltInRegistries.ITEM.getKey(recipe.result().getItem());
            grouped.computeIfAbsent(recipe.familyId(), ignored -> new LinkedHashMap<>())
                    .putIfAbsent(outputId, recipe);
        }
        cachedCatalog = grouped.entrySet().stream()
                .map(entry -> new AlloyFamily(entry.getKey(), List.copyOf(entry.getValue().values())))
                .sorted(Comparator.comparing(value -> value.familyId().toString()))
                .toList();

        ResourceLocation selected = menu.selectedRecipeId();
        if (selected != null) {
            cachedCatalog.stream().flatMap(value -> value.variants().stream())
                    .filter(value -> value.recipeId().equals(selected))
                    .findFirst().ifPresent(this::choose);
        }
        nextRecipeRefreshTick = gameTime + 100L;
        return cachedCatalog;
    }

    private long inputSignature() {
        long hash = 1L;
        for (int slot = 0; slot < 18; slot++) {
            ItemStack stack = menu.getSlot(slot).getItem();
            hash = hash * 31L + BuiltInRegistries.ITEM.getId(stack.getItem());
            hash = hash * 31L + stack.getCount();
            hash = hash * 31L + (stack.hasTag() ? stack.getTag().hashCode() : 0);
        }
        return hash;
    }

    public record AlloyFamily(ResourceLocation familyId, List<AlloyDefinition> variants) { }
}
