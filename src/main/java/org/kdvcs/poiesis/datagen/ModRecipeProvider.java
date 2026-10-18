package org.kdvcs.poiesis.datagen;

import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
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

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_PLANKS.get(),
                        4
                )
                .requires(Ingredient.of(ModTags.Items.ALDER_LOGS))
                .group("planks")
                .unlockedBy("has_alder_logs", has(ModTags.Items.ALDER_LOGS))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_planks"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_WOOD.get(),
                        3
                )
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.ALDER_LOG.get())
                .group("bark")
                .unlockedBy(getHasName(ModBlocks.ALDER_LOG.get()), has(ModBlocks.ALDER_LOG.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_wood"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.STRIPPED_ALDER_WOOD.get(),
                        3
                )
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.STRIPPED_ALDER_LOG.get())
                .group("bark")
                .unlockedBy(
                        getHasName(ModBlocks.STRIPPED_ALDER_LOG.get()),
                        has(ModBlocks.STRIPPED_ALDER_LOG.get())
                )
                .save(
                        consumer,
                        new ResourceLocation(Poiesis.MOD_ID, "stripped_alder_wood")
                );

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_STAIRS.get(),
                        4
                )
                .pattern("#  ")
                .pattern("## ")
                .pattern("###")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .group("wooden_stairs")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_stairs"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_SLAB.get(),
                        6
                )
                .pattern("###")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .group("wooden_slab")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_slab"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_FENCE.get(),
                        3
                )
                .pattern("#S#")
                .pattern("#S#")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .define('S', Items.STICK)
                .group("wooden_fence")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_fence"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_FENCE_GATE.get()
                )
                .pattern("S#S")
                .pattern("S#S")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .define('S', Items.STICK)
                .group("wooden_fence_gate")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_fence_gate"));

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.REDSTONE,
                        ModBlocks.ALDER_BUTTON.get()
                )
                .requires(ModBlocks.ALDER_PLANKS.get())
                .group("wooden_button")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_button"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.ALDER_DOOR.get(),
                        3
                )
                .pattern("##")
                .pattern("##")
                .pattern("##")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .group("wooden_door")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_door"));

        ShapedRecipeBuilder.shaped(
                        RecipeCategory.REDSTONE,
                        ModBlocks.ALDER_TRAPDOOR.get(),
                        2
                )
                .pattern("###")
                .pattern("###")
                .define('#', ModBlocks.ALDER_PLANKS.get())
                .group("wooden_trapdoor")
                .unlockedBy(getHasName(ModBlocks.ALDER_PLANKS.get()), has(ModBlocks.ALDER_PLANKS.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "alder_trapdoor"));

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

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModTags.Items.DUSTS_IRON),
                        RecipeCategory.MISC,
                        Items.IRON_INGOT,
                        0.0F,
                        200
                )
                .unlockedBy(getHasName(ModItems.IRON_DUST.get()), has(ModItems.IRON_DUST.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "iron_ingot_from_iron_dust"));

        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(ModTags.Items.DUSTS_HEAT_RESISTANT_STEEL),
                        RecipeCategory.MISC,
                        ModItems.HEAT_RESISTANT_STEEL_INGOT.get(),
                        0.0F,
                        200
                )
                .unlockedBy(
                        getHasName(ModItems.HEAT_RESISTANT_STEEL_DUST.get()),
                        has(ModItems.HEAT_RESISTANT_STEEL_DUST.get())
                )
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "heat_resistant_steel_ingot_from_dust"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GRAPHITE_BLOCK.get())
                .pattern("##")
                .pattern("##")
                .define('#', ModTags.Items.DUSTS_GRAPHITE)
                .unlockedBy(getHasName(ModItems.GRAPHITE_DUST.get()), has(ModItems.GRAPHITE_DUST.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "graphite_block_from_dust"));

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.GRAPHITE_DUST.get(), 4)
                .requires(ModBlocks.GRAPHITE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.GRAPHITE_BLOCK.get()), has(ModBlocks.GRAPHITE_BLOCK.get()))
                .save(consumer, new ResourceLocation(Poiesis.MOD_ID, "graphite_dust_from_block"));

        // Neutral concrete follows vanilla concrete powder behaviour: it is
        // made from sand and gravel, hardens on contact with water, and can be
        // dyed into any of the sixteen vanilla concrete colours.
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.BUILDING_BLOCKS,
                        ModBlocks.PLAIN_CONCRETE_POWDER.get(),
                        8
                )
                .requires(Items.SAND, 4)
                .requires(Items.GRAVEL, 4)
                .unlockedBy(getHasName(Items.SAND), has(Items.SAND))
                .save(
                        consumer,
                        new ResourceLocation(Poiesis.MOD_ID, "plain_concrete_powder_from_sand_and_gravel")
                );

        plainConcreteDyeRecipes(consumer, forgeDyeTag("white"),
                Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE, "white");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("orange"),
                Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE, "orange");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("magenta"),
                Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE, "magenta");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("light_blue"),
                Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE, "light_blue");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("yellow"),
                Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE, "yellow");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("lime"),
                Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE, "lime");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("pink"),
                Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE, "pink");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("gray"),
                Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE, "gray");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("light_gray"),
                Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE, "light_gray");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("cyan"),
                Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE, "cyan");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("purple"),
                Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE, "purple");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("blue"),
                Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE, "blue");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("brown"),
                Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE, "brown");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("green"),
                Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE, "green");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("red"),
                Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE, "red");
        plainConcreteDyeRecipes(consumer, forgeDyeTag("black"),
                Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE, "black");

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC,
                        ModItems.HEAT_RESISTANT_STEEL_DUST.get(),
                        4
                )
                .requires(Ingredient.of(ModTags.Items.DUSTS_IRON), 3)
                .requires(Ingredient.of(ModTags.Items.DUSTS_ALBICHROME))
                .unlockedBy(getHasName(ModItems.IRON_DUST.get()), has(ModItems.IRON_DUST.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HUMICS.get(),4)
                .pattern("###")
                .pattern("AAD")
                .define('#', Items.WHEAT)
                .define('A', Items.SUGAR_CANE)
                .define('D', Blocks.DIRT)
                .unlockedBy(getHasName(ModItems.HUMICS.get()), has(ModItems.HUMICS.get()))
                .save(consumer);

        armorRecipe(
                consumer,
                ModItems.HEAT_RESISTANT_STEEL_HELMET.get(),
                "heat_resistant_steel_helmet",
                "#A#",
                "# #"
        );
        armorRecipe(
                consumer,
                ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get(),
                "heat_resistant_steel_chestplate",
                "#A#",
                "###",
                "#A#"
        );
        armorRecipe(
                consumer,
                ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get(),
                "heat_resistant_steel_leggings",
                "###",
                "#A#",
                "#A#"
        );
        armorRecipe(
                consumer,
                ModItems.HEAT_RESISTANT_STEEL_BOOTS.get(),
                "heat_resistant_steel_boots",
                "#A#",
                "#A#"
        );

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.BIOMASS_GASIFIER.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("#D#")
                .define('D', ModItems.CONTROL_ELEMENT.get())
                .define('#', ModItems.MACHINE_UNIT.get())
                .define('A', Items.HOPPER)
                .define('B', ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .define('C', Items.COPPER_INGOT)
                .unlockedBy(getHasName(ModBlocks.BIOMASS_GASIFIER.get()), has(ModBlocks.BIOMASS_GASIFIER.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HEAVY_MACHINE_CASING.get())
                .pattern("ABA")
                .pattern("CDC")
                .pattern("AEA")
                .define('A', ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
                .define('B', ModItems.FIBER_COMPOSITE.get())
                .define('C', ModItems.MACHINE_UNIT.get())
                .define('D', ModBlocks.ALBICHROME_CASING.get())
                .define('E', ModItems.CONTROL_ELEMENT.get())
                .unlockedBy(getHasName(ModBlocks.HEAVY_MACHINE_CASING.get()), has(ModBlocks.HEAVY_MACHINE_CASING.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.ALLOY_FACTORY.get())
                .pattern("QAQ")
                .pattern("#B#")
                .pattern("ZCZ")
                .define('Q', Items.DIAMOND)
                .define('A', Items.BLAST_FURNACE)
                .define('#', ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .define('B', ModBlocks.HEAVY_MACHINE_CASING.get())
                .define('C', ModItems.CONTROL_ELEMENT.get())
                .define('Z', ModItems.MACHINE_UNIT.get())
                .unlockedBy(getHasName(ModBlocks.ALLOY_FACTORY.get()), has(ModBlocks.ALLOY_FACTORY.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.GREENHOUSE.get())
                .pattern("GDG")
                .pattern("#B#")
                .pattern("HCH")
                .define('G', Items.DIAMOND)
                .define('D', Items.DAYLIGHT_DETECTOR)
                .define('#', Tags.Items.GLASS)
                .define('B', ModBlocks.HEAVY_MACHINE_CASING.get())
                .define('H', ModItems.MACHINE_UNIT.get())
                .define('C', ModItems.CONTROL_ELEMENT.get())
                .unlockedBy(getHasName(ModBlocks.HEAVY_MACHINE_CASING.get()),
                        has(ModBlocks.HEAVY_MACHINE_CASING.get()))
                .save(consumer);

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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.COMPOUNDER.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("#D#")
                .define('D', ModItems.CONTROL_ELEMENT.get())
                .define('#', ModItems.MACHINE_UNIT.get())
                .define('A', Items.HOPPER)
                .define('B', ModBlocks.ALBICHROME_CASING.get())
                .define('C', Items.PISTON)
                .unlockedBy(getHasName(ModBlocks.MODULE_FABRICATOR.get()), has(ModBlocks.MODULE_FABRICATOR.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SEPARATOR.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("#D#")
                .define('D', ModItems.CONTROL_ELEMENT.get())
                .define('#', ModItems.MACHINE_UNIT.get())
                .define('A', Items.IRON_BARS)
                .define('B', ModBlocks.ALBICHROME_CASING.get())
                .define('C', ModItems.FILTER_PLATE.get())
                .unlockedBy(getHasName(ModBlocks.SEPARATOR.get()), has(ModBlocks.SEPARATOR.get()))
                .save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Items.IRON_NUGGET)
                .define('B', ModItems.SILICA_POWDER.get())
                .define('C', ModTags.Items.INGOTS_ALBICHROME)
                .unlockedBy(getHasName(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get()), has(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get()))
                .save(consumer);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(), 3)
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Items.REDSTONE)
                .define('B', Items.GOLD_NUGGET)
                .define('C', ModTags.Items.INGOTS_ALBICHROME)
                .unlockedBy(getHasName(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get()), has(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get()))
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
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .pattern("###")
                .pattern("###")
                .pattern("###")
                .define('#', ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
                .unlockedBy(
                        getHasName(ModItems.HEAT_RESISTANT_STEEL_INGOT.get()),
                        has(ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
                )
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

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.MISC,
                        ModItems.HEAT_RESISTANT_STEEL_INGOT.get(),
                        9
                )
                .requires(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                .unlockedBy(
                        getHasName(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get()),
                        has(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get())
                )
                .save(
                        consumer,
                        new ResourceLocation(
                                Poiesis.MOD_ID,
                                "heat_resistant_steel_ingot_from_unpacking"
                        )
                );

    }

    private void plainConcreteDyeRecipes(
            Consumer<FinishedRecipe> consumer,
            TagKey<Item> dye,
            ItemLike coloredPowder,
            ItemLike coloredConcrete,
            String color
    ) {
        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.BUILDING_BLOCKS,
                        coloredPowder,
                        8
                )
                .requires(ModBlocks.PLAIN_CONCRETE_POWDER.get(), 8)
                .requires(dye)
                .unlockedBy(
                        getHasName(ModBlocks.PLAIN_CONCRETE_POWDER.get()),
                        has(ModBlocks.PLAIN_CONCRETE_POWDER.get())
                )
                .save(
                        consumer,
                        new ResourceLocation(
                                Poiesis.MOD_ID,
                                "" + color + "_concrete_powder_from_plain"
                        )
                );

        ShapelessRecipeBuilder.shapeless(
                        RecipeCategory.BUILDING_BLOCKS,
                        coloredConcrete,
                        8
                )
                .requires(ModBlocks.PLAIN_CONCRETE.get(), 8)
                .requires(dye)
                .unlockedBy(
                        getHasName(ModBlocks.PLAIN_CONCRETE.get()),
                        has(ModBlocks.PLAIN_CONCRETE.get())
                )
                .save(
                        consumer,
                        new ResourceLocation(
                                Poiesis.MOD_ID,
                                "" + color + "_concrete_from_plain"
                        )
                );
    }

    private static TagKey<Item> forgeDyeTag(String color) {
        return TagKey.create(
                Registries.ITEM,
                new ResourceLocation("forge", "dyes/" + color)
        );
    }

    private void armorRecipe(
            Consumer<FinishedRecipe> consumer,
            ItemLike result,
            String id,
            String... pattern
    ) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(
                RecipeCategory.COMBAT,
                result
        );
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('#', ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
                .define('A', ModItems.FIBER_COMPOSITE.get())
                .unlockedBy(
                        getHasName(ModItems.HEAT_RESISTANT_STEEL_INGOT.get()),
                        has(ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
                )
                .save(
                        consumer,
                        new ResourceLocation(Poiesis.MOD_ID, id)
                );
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
