package org.kdvcs.poiesis.machine.generator;

import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.FluidAutoTransfer;
import org.kdvcs.poiesis.recipe.FluidGeneratorFuelRecipe;

/** Infrastructure shared by all Poiesis fluid-fuel generators. */
public abstract class GeneratorRuntime<R extends Recipe<Container> & FluidGeneratorFuelRecipe>
        extends AbstractGeneratorRuntime {
    private static final int FLUID_AUTO_INPUT_PER_CYCLE = 1_000;

    private final FluidGeneratorProcess<R> process;

    protected GeneratorRuntime(
            MachineContext owner,
            GeneratorDefinition generatorDefinition,
            MachineDefinition machineDefinition,
            RecipeType<R> recipeType
    ) {
        super(owner, generatorDefinition, machineDefinition);
        process = addComponent(
                FluidGeneratorProcess.class,
                new FluidGeneratorProcess<>(
                        owner,
                        energy(),
                        fluid().orElseThrow(() -> new IllegalArgumentException(
                                "Fluid generator requires a fuel tank"
                        )),
                        recipeType,
                        generatorDefinition.baseGenerationPerTick()
                )
        );
    }

    @Override
    protected final void tickGeneration() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected void runAdditionalAutoInputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.INPUT)) {
            FluidAutoTransfer.pullInputs(
                    owner,
                    fluid().orElseThrow().tank(),
                    sideConfiguration().orElseThrow(),
                    FLUID_AUTO_INPUT_PER_CYCLE
            );
        }
    }

    public final FluidGeneratorProcess<R> process() {
        return process;
    }
}
