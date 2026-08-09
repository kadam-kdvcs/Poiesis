package org.kdvcs.poiesis.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.tag.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public final class ModRecipeProvider extends RecipeProvider {
    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    private static final List<ItemLike> ALBICHROME_ORES = List.of(ModBlocks.ALBICHROME_ORE.get(),
            ModBlocks.DEEPSLATE_ALBICHROME_ORE.get());

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {

        //  GENERATE A SMELTING RECIPE, p250789(Exp); p252144(Ticks), p251687(Group)
        oreSmelting(consumer, ALBICHROME_ORES, RecipeCategory.MISC, ModItems.ALBICHROME_INGOT.get(),
                0.1f,130,"albichrome");

        //  GENERATE A BLASTING RECIPE
        oreBlasting(consumer, ALBICHROME_ORES, RecipeCategory.MISC, ModItems.ALBICHROME_INGOT.get(),
                0.1f,65,"albichrome");

        smeltingWithCount(
                consumer,
                Ingredient.of(ModItems.GLASS_BATCH.get()),
                new ItemStack(Items.GLASS),
                0.1F,
                75,
                new ResourceLocation(Poiesis.MOD_ID, "glass_from_glass_batch")
        );

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ALBICHROME_CASING.get())
                .pattern("#A#")
                .pattern("ABA")
                .pattern("#A#")
                .define('#', ModTags.Items.INGOTS_ALBICHROME)
                .define('A', Items.IRON_INGOT)
                .define('B', Tags.Items.GLASS)
                .unlockedBy(getHasName(ModBlocks.ALBICHROME_CASING.get()), has(ModBlocks.ALBICHROME_CASING.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GRINDING_STONE.get())
                .pattern("#A#")
                .define('#', Items.FLINT)
                .define('A', Items.IRON_INGOT)
                .unlockedBy(getHasName(ModItems.GRINDING_STONE.get()), has(ModItems.GRINDING_STONE.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.GLASS_BATCH.get(),4)
                .pattern("##")
                .pattern("#A")
                .define('#', ModItems.SILICA_POWDER.get())
                .define('A', Items.CLAY_BALL)
                .unlockedBy(getHasName(ModItems.GLASS_BATCH.get()), has(ModItems.GLASS_BATCH.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BASIC_ENERGY_PIPE.get(),16)
                .pattern("#A#")
                .pattern("B B")
                .pattern("#A#")
                .define('#', ModTags.Items.INGOTS_ALBICHROME)
                .define('A', Tags.Items.GLASS_PANES)
                .define('B', Items.REDSTONE)
                .unlockedBy(getHasName(ModBlocks.BASIC_ENERGY_PIPE.get()), has(ModBlocks.BASIC_ENERGY_PIPE.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BASIC_ITEM_PIPE.get(),16)
                .pattern("#A#")
                .pattern("B B")
                .pattern("#A#")
                .define('#', ModTags.Items.INGOTS_ALBICHROME)
                .define('A', Tags.Items.GLASS_PANES)
                .define('B', Tags.Items.CHESTS)
                .unlockedBy(getHasName(ModBlocks.BASIC_ITEM_PIPE.get()), has(ModBlocks.BASIC_ITEM_PIPE.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BASIC_FLUID_PIPE.get(),16)
                .pattern("#A#")
                .pattern("B B")
                .pattern("#A#")
                .define('#', ModTags.Items.INGOTS_ALBICHROME)
                .define('A', Tags.Items.GLASS_PANES)
                .define('B', Items.BUCKET)
                .unlockedBy(getHasName(ModBlocks.BASIC_FLUID_PIPE.get()), has(ModBlocks.BASIC_FLUID_PIPE.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MODULE_FABRICATOR.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("#D#")
                .define('D', ModItems.CONTROL_ELEMENT.get())
                .define('#', ModItems.MACHINE_UNIT.get())
                .define('A', Items.PISTON)
                .define('B', ModBlocks.ALBICHROME_CASING.get())
                .define('C', Items.REDSTONE)
                .unlockedBy(getHasName(ModBlocks.MODULE_FABRICATOR.get()), has(ModBlocks.MODULE_FABRICATOR.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MECHANICAL_PROCESSOR.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("#D#")
                .define('D', ModItems.CONTROL_ELEMENT.get())
                .define('#', ModItems.MACHINE_UNIT.get())
                .define('A', Items.ANVIL)
                .define('B', ModBlocks.ALBICHROME_CASING.get())
                .define('C', Items.FLINT)
                .unlockedBy(getHasName(ModBlocks.MODULE_FABRICATOR.get()), has(ModBlocks.MODULE_FABRICATOR.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CONTROL_ELEMENT.get())
                .pattern(" A ")
                .pattern("ABA")
                .pattern(" A ")
                .define('A', Items.REDSTONE)
                .define('B', ModTags.Items.INGOTS_ALBICHROME)
                .unlockedBy(getHasName(ModItems.CONTROL_ELEMENT.get()), has(ModItems.CONTROL_ELEMENT.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.MACHINE_UNIT.get())
                .pattern("A A")
                .pattern("BCB")
                .pattern(" B ")
                .define('A', Items.COPPER_INGOT)
                .define('B', Items.IRON_INGOT)
                .define('C', ModTags.Items.INGOTS_ALBICHROME)
                .unlockedBy(getHasName(ModItems.MACHINE_UNIT.get()), has(ModItems.MACHINE_UNIT.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, ModItems.PIPE_CONFIGURATOR.get())
                .pattern(" R ")
                .pattern(" I ")
                .pattern(" A ")
                .define('R', Items.REDSTONE_TORCH)
                .define('I', Items.IRON_INGOT)
                .define('A', ModItems.ALBICHROME_NUGGET.get())
                .unlockedBy(getHasName(ModItems.ALBICHROME_NUGGET.get()), has(ModItems.ALBICHROME_NUGGET.get()))
                .save(consumer);

        //  ALBICHROME BLOCK
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ALBICHROME_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.ALBICHROME_INGOT.get())
                .unlockedBy(getHasName(ModBlocks.ALBICHROME_BLOCK.get()), has(ModBlocks.ALBICHROME_BLOCK.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.RAW_ALBICHROME_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.RAW_ALBICHROME.get())
                .unlockedBy(getHasName(ModBlocks.RAW_ALBICHROME_BLOCK.get()), has(ModBlocks.RAW_ALBICHROME_BLOCK.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ALBICHROME_INGOT.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.ALBICHROME_NUGGET.get())
                .unlockedBy(getHasName(ModItems.ALBICHROME_INGOT.get()), has(ModItems.ALBICHROME_INGOT.get()))
                .save(consumer);

        //  unpack
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ALBICHROME_NUGGET.get(),9)
                .requires(ModItems.ALBICHROME_INGOT.get())
                .unlockedBy(getHasName(ModItems.ALBICHROME_NUGGET.get()), has(ModItems.ALBICHROME_NUGGET.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.RAW_ALBICHROME.get(),9)
                .requires(ModBlocks.RAW_ALBICHROME_BLOCK.get())
                .unlockedBy(getHasName(ModItems.RAW_ALBICHROME.get()), has(ModItems.RAW_ALBICHROME.get()))
                .save(consumer);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.ALBICHROME_INGOT.get(),9)
                .requires(ModBlocks.ALBICHROME_BLOCK.get())
                .unlockedBy(getHasName(ModItems.ALBICHROME_INGOT.get()), has(ModItems.ALBICHROME_INGOT.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "albichrome_ingot_from_unpacking"));

    }

    protected static void oreSmelting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients,
                                      RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.SMELTING_RECIPE,
                ingredients, category, result, experience, cookingTime, group,
                "_from_smelting");
    }

    protected static void oreBlasting(Consumer<FinishedRecipe> consumer, List<ItemLike> ingredients,
                                      RecipeCategory category, ItemLike result, float experience, int cookingTime, String group) {
        oreCooking(consumer, RecipeSerializer.BLASTING_RECIPE, ingredients, category, result, experience, cookingTime, group,
                "_from_blasting");
    }

    protected static void oreCooking(Consumer<FinishedRecipe> consumer, RecipeSerializer<? extends AbstractCookingRecipe> serializer, List<ItemLike> ingredients, RecipeCategory category, ItemLike result, float experience, int cookingTime, String group, String recipeSuffix) {
        for(ItemLike itemlike : ingredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), category, result, experience, cookingTime, serializer)
                    .group(group).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(consumer, Poiesis.MOD_ID + ":" + getItemName(result) + recipeSuffix + "_" + getItemName(itemlike));
        }

    }

    private static void smeltingWithCount(
            Consumer<FinishedRecipe> out,
            Ingredient ingredient,
            ItemStack result,
            float xp,
            int time,
            ResourceLocation id
    ) {
        out.accept(new FinishedRecipe() {
            @Override
            public void serializeRecipeData(JsonObject json) {
                json.addProperty("type", "minecraft:smelting");
                json.add("ingredient", ingredient.toJson());

                JsonObject res = new JsonObject();
                res.addProperty("item", BuiltInRegistries.ITEM.getKey(result.getItem()).toString());
                if (result.getCount() != 1) res.addProperty("count", result.getCount());
                json.add("result", res);

                json.addProperty("experience", xp);
                json.addProperty("cookingtime", time);
            }

            @Override public ResourceLocation getId() { return id; }
            @Override public RecipeSerializer<?> getType() { return RecipeSerializer.SMELTING_RECIPE; }

            @Override public @Nullable JsonObject serializeAdvancement() { return null; }
            @Override public @Nullable ResourceLocation getAdvancementId() { return null; }
        });
    }
}
