package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.jei.category.CompounderCategory;
import org.kdvcs.poiesis.compat.jei.category.MechanicalProcessingCategory;
import org.kdvcs.poiesis.compat.jei.handler.CompounderGuiHandler;
import org.kdvcs.poiesis.compat.jei.handler.MechanicalProcessorGuiHandler;
import org.kdvcs.poiesis.recipe.CompounderRecipe;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModRecipeTypes;
import org.kdvcs.poiesis.screen.CompounderScreen;
import org.kdvcs.poiesis.screen.MechanicalProcessorScreen;

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

        registration.addRecipeCategories(
                new CompounderCategory(
                        guiHelper
                )
        );
    }

    @Override
    public void registerRecipes(
            IRecipeRegistration registration
    ) {
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

        registration.addRecipes(
                PoiesisJeiRecipeTypes.COMPOUNDING,
                compounderRecipes
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

        registration.addRecipeCatalyst(
                ModBlocks.COMPOUNDER.get(),
                PoiesisJeiRecipeTypes.COMPOUNDING
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
    }
}
