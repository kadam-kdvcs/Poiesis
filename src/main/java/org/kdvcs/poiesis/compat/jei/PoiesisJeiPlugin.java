package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.category.CompounderCategory;
import org.kdvcs.poiesis.compat.jei.category.AdsorptionColumnCategory;
import org.kdvcs.poiesis.compat.jei.category.GasificationCategory;
import org.kdvcs.poiesis.compat.jei.category.GasGeneratorCategory;
import org.kdvcs.poiesis.compat.jei.category.MechanicalProcessingCategory;
import org.kdvcs.poiesis.compat.jei.category.MicrobialFuelCellCategory;
import org.kdvcs.poiesis.compat.jei.category.ModuleFabricationCategory;
import org.kdvcs.poiesis.compat.jei.category.SeparatorCategory;
import org.kdvcs.poiesis.compat.jei.category.LeacherCategory;
import org.kdvcs.poiesis.compat.jei.category.GreenhouseCategory;
import org.kdvcs.poiesis.compat.jei.category.AlloyFactoryCategory;
import org.kdvcs.poiesis.compat.jei.handler.CompounderGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.AdsorptionColumnGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.BiomassGasifierGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.GasGeneratorGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.MechanicalProcessorGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.ModuleFabricatorGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.MicrobialFuelCellGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.SeparatorGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.LeacherGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.GreenhouseGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.AlloyFactoryGuiHandler;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;
import org.kdvcs.poiesis.recipe.GasificationRecipe;
import org.kdvcs.poiesis.recipe.GasGenerationRecipe;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.recipe.LeachingRecipe;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;
import org.kdvcs.poiesis.recipe.alloy.AlloyTableRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.registry.ModMenuTypes;
import org.kdvcs.poiesis.screen.CompounderScreen;
import org.kdvcs.poiesis.screen.AdsorptionColumnScreen;
import org.kdvcs.poiesis.screen.BiomassGasifierScreen;
import org.kdvcs.poiesis.screen.GasGeneratorScreen;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;
import org.kdvcs.poiesis.screen.ModuleFabricatorScreen;
import org.kdvcs.poiesis.screen.MicrobialFuelCellScreen;
import org.kdvcs.poiesis.screen.SeparatorScreen;
import org.kdvcs.poiesis.screen.LeacherScreen;
import org.kdvcs.poiesis.screen.GreenhouseScreen;
import org.kdvcs.poiesis.screen.AlloyFactoryScreen;
import org.kdvcs.poiesis.screen.AlloyFactoryMenu;

import java.util.List;

/**
 * Central JEI integration entry point for Poiesis.
 *
 * Keep all future JEI-facing registration here and delegate category/layout
 * details into the compat.jei package. This prevents machine classes from
 * acquiring JEI dependencies and keeps future integrations additive.
 */
@JeiPlugin
public final class PoiesisJeiPlugin implements IModPlugin {
    private static IIngredientManager ingredientManager;

    private static final ResourceLocation UID =
            new ResourceLocation(
                    Poiesis.MOD_ID,
                    "jei_plugin"
            );

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        ingredientManager = jeiRuntime.getIngredientManager();
    }

    public static IIngredientManager ingredientManager() {
        return ingredientManager;
    }

    @Override
    public void registerCategories(
            IRecipeCategoryRegistration registration
    ) {
        var guiHelper = registration
                .getJeiHelpers()
                .getGuiHelper();

        for (MechanicalProcessingJeiMode mode
                : MechanicalProcessingJeiMode.values()) {
            registration.addRecipeCategories(
                    new MechanicalProcessingCategory(
                            guiHelper,
                            mode
                    )
            );
        }

        for (CompounderJeiMode mode : CompounderJeiMode.values()) {
            registration.addRecipeCategories(
                    new CompounderCategory(guiHelper, mode)
            );
        }

        for (SeparatorJeiMode mode : SeparatorJeiMode.values()) {
            registration.addRecipeCategories(
                    new SeparatorCategory(guiHelper, mode)
            );
        }

        for (ModuleFabricationJeiMode mode
                : ModuleFabricationJeiMode.values()) {
            registration.addRecipeCategories(
                    new ModuleFabricationCategory(guiHelper, mode)
            );
        }

        registration.addRecipeCategories(
                new AdsorptionColumnCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new GasificationCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new GasGeneratorCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new MicrobialFuelCellCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new LeacherCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new GreenhouseCategory(guiHelper)
        );
        registration.addRecipeCategories(
                new AlloyFactoryCategory(guiHelper)
        );
    }

    @Override
    public void registerRecipes(
            IRecipeRegistration registration
    ) {
        registerEarlyObtainmentInfo(
                registration,
                ModItems.SILICA_POWDER.get(),
                "jei.poiesis.info.silica_powder.early_obtainment"
        );
        registerEarlyObtainmentInfo(
                registration,
                ModItems.IRON_DUST.get(),
                "jei.poiesis.info.iron_dust.early_obtainment"
        );
        registerEarlyObtainmentInfo(
                registration,
                ModItems.ALBICHROME_DUST.get(),
                "jei.poiesis.info.albichrome_dust.early_obtainment"
        );

        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null) {
            Poiesis.LOGGER.warn(
                    "JEI recipe registration skipped because no client level is available"
            );
            return;
        }

        List<MechanicalProcessingRecipe> recipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(
                                ModRecipeTypes.MECHANICAL_PROCESSING.get()
                        );

        for (MechanicalProcessingJeiMode mode
                : MechanicalProcessingJeiMode.values()) {
            List<MechanicalProcessingRecipe> modeRecipes =
                    recipes.stream()
                            .filter(mode::matches)
                            .toList();

            registration.addRecipes(
                    mode.recipeType(),
                    modeRecipes
            );
        }

        List<CompounderRecipe> compounderRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(
                                ModRecipeTypes.COMPOUNDING.get()
                        );

        for (CompounderJeiMode mode : CompounderJeiMode.values()) {
            registration.addRecipes(
                    mode.recipeType(),
                    compounderRecipes.stream()
                            .filter(mode::matches)
                            .toList()
            );
        }

        List<SeparatorRecipe> separatorRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(
                                ModRecipeTypes.SEPARATING.get()
                        );

        for (SeparatorJeiMode mode : SeparatorJeiMode.values()) {
            registration.addRecipes(
                    mode.recipeType(),
                    separatorRecipes.stream()
                            .filter(mode::matches)
                            .toList()
            );
        }

        List<ModuleFabricationRecipe> moduleFabricationRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(
                                ModRecipeTypes.MODULE_FABRICATION.get()
                        );

        for (ModuleFabricationJeiMode mode
                : ModuleFabricationJeiMode.values()) {
            registration.addRecipes(
                    mode.recipeType(),
                    moduleFabricationRecipes.stream()
                            .filter(mode::matches)
                            .toList()
            );
        }

        List<AdsorptionRecipe> adsorptionRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.ADSORPTION.get());
        registration.addRecipes(
                PoiesisJeiRecipeTypes.ADSORPTION,
                adsorptionRecipes
        );

        List<GasificationRecipe> gasificationRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.GASIFICATION.get());
        registration.addRecipes(
                PoiesisJeiRecipeTypes.GASIFICATION,
                gasificationRecipes
        );

        List<GasGenerationRecipe> gasGenerationRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.GAS_GENERATION.get());
        registration.addRecipes(
                PoiesisJeiRecipeTypes.GAS_GENERATION,
                gasGenerationRecipes
        );

        List<MicrobialFuelCellRecipe> microbialFuelCellRecipes =
                minecraft.level
                        .getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.MICROBIAL_FUEL_CELL.get());
        registration.addRecipes(
                PoiesisJeiRecipeTypes.MICROBIAL_FUEL_CELL,
                microbialFuelCellRecipes
        );

        List<LeachingRecipe> leachingRecipes =
                minecraft.level.getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.LEACHING.get());
        registration.addRecipes(PoiesisJeiRecipeTypes.LEACHING, leachingRecipes);

        List<GreenhouseRecipe> greenhouseRecipes =
                minecraft.level.getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.GREENHOUSE.get());
        registration.addRecipes(PoiesisJeiRecipeTypes.GREENHOUSE, greenhouseRecipes);

        List<AlloyTableRecipe> alloyRecipes =
                minecraft.level.getRecipeManager()
                        .getAllRecipesFor(ModRecipeTypes.ALLOYING.get())
                        .stream()
                        .filter(recipe -> !recipe.getResultItem(
                                minecraft.level.registryAccess()
                        ).isEmpty())
                        .filter(recipe -> recipe.inputs().stream()
                                .allMatch(input -> input.ingredient().getItems().length > 0))
                        .toList();
        registration.addRecipes(
                PoiesisJeiRecipeTypes.ALLOYING,
                alloyRecipes
        );
    }

    private static void registerEarlyObtainmentInfo(
            IRecipeRegistration registration,
            Item item,
            String translationKey
    ) {
        registration.getIngredientManager()
                .createTypedIngredient(
                        VanillaTypes.ITEM_STACK,
                        item.getDefaultInstance()
                )
                .ifPresent(ingredient ->
                        registration.addRecipes(
                                RecipeTypes.INFORMATION,
                                List.of(
                                        new TranslatableIngredientInfoRecipe(
                                                List.of(ingredient),
                                                translationKey
                                        )
                                )
                        )
                );
    }

    @Override
    public void registerRecipeCatalysts(
            IRecipeCatalystRegistration registration
    ) {
        for (MechanicalProcessingJeiMode mode
                : MechanicalProcessingJeiMode.values()) {
            registration.addRecipeCatalyst(
                    ModBlocks.MECHANICAL_PROCESSOR.get(),
                    mode.recipeType()
            );

            registration.addRecipeCatalyst(
                    mode.module(),
                    mode.recipeType()
            );
        }

        for (CompounderJeiMode mode : CompounderJeiMode.values()) {
            registration.addRecipeCatalyst(
                    ModBlocks.COMPOUNDER.get(),
                    mode.recipeType()
            );
        }

        registration.addRecipeCatalyst(
                ModItems.HIGH_SHEAR_MODULE.get(),
                PoiesisJeiRecipeTypes.HIGH_SHEAR_COMPOUNDING
        );

        for (SeparatorJeiMode mode : SeparatorJeiMode.values()) {
            registration.addRecipeCatalyst(
                    ModBlocks.SEPARATOR.get(),
                    mode.recipeType()
            );
        }

        registration.addRecipeCatalyst(
                ModItems.WET_SEPARATION_MODULE.get(),
                PoiesisJeiRecipeTypes.WET_SEPARATING
        );

        registration.addRecipeCatalyst(
                ModItems.RINSING_MODULE.get(),
                PoiesisJeiRecipeTypes.RINSING_SEPARATING
        );

        registration.addRecipeCatalyst(
                ModItems.ELECTROSTATIC_SEPARATION_MODULE.get(),
                PoiesisJeiRecipeTypes.ELECTROSTATIC_SEPARATING
        );

        for (ModuleFabricationJeiMode mode
                : ModuleFabricationJeiMode.values()) {
            registration.addRecipeCatalyst(
                    ModBlocks.MODULE_FABRICATOR.get(),
                    mode.recipeType()
            );
        }

        registration.addRecipeCatalyst(
                ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get(),
                PoiesisJeiRecipeTypes.ADSORPTION
        );

        registration.addRecipeCatalyst(
                ModBlocks.BIOMASS_GASIFIER.get(),
                PoiesisJeiRecipeTypes.GASIFICATION
        );
        registration.addRecipeCatalyst(
                ModBlocks.GAS_GENERATOR.get(),
                PoiesisJeiRecipeTypes.GAS_GENERATION
        );
        registration.addRecipeCatalyst(
                ModBlocks.MICROBIAL_FUEL_CELL.get(),
                PoiesisJeiRecipeTypes.MICROBIAL_FUEL_CELL
        );
        registration.addRecipeCatalyst(
                ModBlocks.LEACHER.get(),
                PoiesisJeiRecipeTypes.LEACHING
        );
        registration.addRecipeCatalyst(
                ModBlocks.GREENHOUSE.get(),
                PoiesisJeiRecipeTypes.GREENHOUSE
        );
        registration.addRecipeCatalyst(
                ModBlocks.ALLOY_FACTORY.get(),
                PoiesisJeiRecipeTypes.ALLOYING
        );
    }

    @Override
    public void registerRecipeTransferHandlers(
            IRecipeTransferRegistration registration
    ) {
        registration.addRecipeTransferHandler(
                AlloyFactoryMenu.class,
                ModMenuTypes.ALLOY_FACTORY.get(),
                PoiesisJeiRecipeTypes.ALLOYING,
                AlloyFactoryMenu.FIRST_INPUT_MENU_SLOT,
                AlloyFactoryMenu.INPUT_MENU_SLOT_COUNT,
                AlloyFactoryMenu.PLAYER_INVENTORY_MENU_SLOT,
                AlloyFactoryMenu.PLAYER_INVENTORY_SLOT_COUNT
        );
    }

    @Override
    public void registerGuiHandlers(
            IGuiHandlerRegistration registration
    ) {
        registration.addGuiContainerHandler(
                MechanicalProcessorScreen.class,
                new MechanicalProcessorGuiHandler()
        );

        registration.addGuiContainerHandler(
                CompounderScreen.class,
                new CompounderGuiHandler()
        );

        registration.addGuiContainerHandler(
                SeparatorScreen.class,
                new SeparatorGuiHandler()
        );

        registration.addGuiContainerHandler(
                ModuleFabricatorScreen.class,
                new ModuleFabricatorGuiHandler()
        );

        registration.addGuiContainerHandler(
                AdsorptionColumnScreen.class,
                new AdsorptionColumnGuiHandler()
        );

        registration.addGuiContainerHandler(
                BiomassGasifierScreen.class,
                new BiomassGasifierGuiHandler()
        );

        registration.addGuiContainerHandler(
                GasGeneratorScreen.class,
                new GasGeneratorGuiHandler()
        );

        registration.addGuiContainerHandler(
                MicrobialFuelCellScreen.class,
                new MicrobialFuelCellGuiHandler()
        );

        registration.addGuiContainerHandler(
                LeacherScreen.class,
                new LeacherGuiHandler()
        );

        registration.addGuiContainerHandler(
                GreenhouseScreen.class,
                new GreenhouseGuiHandler()
        );

        registration.addGuiContainerHandler(
                AlloyFactoryScreen.class,
                new AlloyFactoryGuiHandler()
        );
    }
}
