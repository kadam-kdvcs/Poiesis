package org.kdvcs.poiesis.recipe.alloy;

import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.kdvcs.poiesis.recipe.CountedIngredient;
import org.kdvcs.poiesis.recipe.MaterialPlan;
import org.kdvcs.poiesis.registry.ModRecipeSerializers;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.List;

/** A manually maintained Poiesis alloy-table entry. */
public final class AlloyTableRecipe implements Recipe<Container> {
    private final ResourceLocation id;
    private final List<AlloyMaterial> materials;
    private final List<CountedIngredient> inputs;
    private final ResourceLocation resultTag;
    private final int resultCount;
    private final int duration;
    private final int energyPerTick;

    public AlloyTableRecipe(
            ResourceLocation id,
            List<AlloyMaterial> materials,
            ResourceLocation resultTag,
            int resultCount,
            int duration,
            int energyPerTick
    ) {
        if (id == null || materials == null || materials.size() < 2
                || resultTag == null || resultCount <= 0
                || duration <= 0 || energyPerTick < 0) {
            throw new IllegalArgumentException("Invalid alloy table recipe");
        }
        this.id = id;
        this.materials = List.copyOf(materials);
        this.inputs = materials.stream().map(AlloyMaterial::asIngredient).toList();
        this.resultTag = resultTag;
        this.resultCount = resultCount;
        this.duration = duration;
        this.energyPerTick = energyPerTick;
    }

    @Override
    public boolean matches(Container container, Level level) {
        return MaterialPlan.canSatisfy(container, 0, container.getContainerSize(), inputs);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return getResultItem(access);
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return true; }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        TagKey<Item> tag = TagKey.create(Registries.ITEM, resultTag);
        return BuiltInRegistries.ITEM.getTag(tag)
                .flatMap(values -> values.stream().findFirst())
                .map(holder -> new ItemStack(holder.value(), resultCount))
                .orElse(ItemStack.EMPTY);
    }

    @Override public ResourceLocation getId() { return id; }
    @Override public RecipeSerializer<?> getSerializer() { return ModRecipeSerializers.ALLOYING.get(); }
    @Override public RecipeType<?> getType() { return ModRecipeTypes.ALLOYING.get(); }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        inputs.forEach(value -> result.add(value.ingredient()));
        return result;
    }

    public List<AlloyMaterial> materials() { return materials; }
    public List<CountedIngredient> inputs() { return inputs; }
    public ResourceLocation resultTag() { return resultTag; }
    public int resultCount() { return resultCount; }
    public int duration() { return duration; }
    public int energyPerTick() { return energyPerTick; }
}
