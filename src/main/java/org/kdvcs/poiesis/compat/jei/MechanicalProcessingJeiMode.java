package org.kdvcs.poiesis.compat.jei;

import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.kdvcs.poiesis.machine.module.MechanicalMode;
import org.kdvcs.poiesis.recipe.MechanicalProcessingRecipe;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Single source of truth that maps a Mechanical Processor specialization to
 * its JEI page, process capability, title, and specialization module.
 *
 * Adding another processor specialization should primarily mean adding one
 * enum constant here plus its normal machine-side recipe capability.
 */
public enum MechanicalProcessingJeiMode {
    PRESSING(
            MechanicalMode.PRESSING,
            ModItems.PRESSING,
            PoiesisJeiRecipeTypes.PRESSING,
            () -> ModItems.PRESSING_MODULE.get(),
            "jei.poiesis.category.mechanical_processor.pressing"
    ),
    GRINDING(
            MechanicalMode.GRINDING,
            ModItems.GRINDING,
            PoiesisJeiRecipeTypes.GRINDING,
            () -> ModItems.GRINDING_MODULE.get(),
            "jei.poiesis.category.mechanical_processor.grinding"
    ),
    EXTRUSION(
            MechanicalMode.EXTRUSION,
            ModItems.EXTRUSION,
            PoiesisJeiRecipeTypes.EXTRUSION,
            () -> ModItems.EXTRUSION_MODULE.get(),
            "jei.poiesis.category.mechanical_processor.extrusion"
    );

    private final MechanicalMode machineMode;
    private final ResourceLocation capability;
    private final RecipeType<MechanicalProcessingRecipe> recipeType;
    private final Supplier<? extends ItemLike> module;
    private final String titleKey;

    MechanicalProcessingJeiMode(
            MechanicalMode machineMode,
            ResourceLocation capability,
            RecipeType<MechanicalProcessingRecipe> recipeType,
            Supplier<? extends ItemLike> module,
            String titleKey
    ) {
        this.machineMode = machineMode;
        this.capability = capability;
        this.recipeType = recipeType;
        this.module = module;
        this.titleKey = titleKey;
    }

    public MechanicalMode machineMode() {
        return machineMode;
    }

    public ResourceLocation capability() {
        return capability;
    }

    public RecipeType<MechanicalProcessingRecipe> recipeType() {
        return recipeType;
    }

    public Component title() {
        return Component.translatable(titleKey);
    }

    public ItemLike module() {
        return module.get();
    }

    public ItemStack moduleStack() {
        return new ItemStack(module());
    }

    public boolean matches(
            MechanicalProcessingRecipe recipe
    ) {
        return capability.equals(
                recipe.requiredCapability()
        );
    }

    public static Optional<MechanicalProcessingJeiMode> fromMachineMode(
            MechanicalMode mode
    ) {
        return Arrays.stream(values())
                .filter(value -> value.machineMode == mode)
                .findFirst();
    }
}
