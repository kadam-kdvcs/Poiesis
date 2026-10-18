package org.kdvcs.poiesis.machine.generator;

import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.recipe.GasGenerationRecipe;

public final class GasGeneratorRuntime extends GeneratorRuntime<GasGenerationRecipe> {
    public GasGeneratorRuntime(
            MachineContext owner,
            GeneratorDefinition generatorDefinition,
            MachineDefinition machineDefinition,
            RecipeType<GasGenerationRecipe> recipeType
    ) {
        super(owner, generatorDefinition, machineDefinition, recipeType);
    }
}
