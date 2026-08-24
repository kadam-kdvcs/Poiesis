package org.kdvcs.poiesis.machine;

import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.FluidAutoTransfer;
import org.kdvcs.poiesis.machine.process.GasifierProcess;
import org.kdvcs.poiesis.recipe.GasificationRecipe;

public final class BiomassGasifierRuntime extends MachineRuntime {
    private static final int GAS_AUTO_OUTPUT_PER_CYCLE = 1_000;

    private final GasifierProcess process;

    public BiomassGasifierRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<GasificationRecipe> recipeType
    ) {
        super(owner, definition);
        process = addComponent(
                GasifierProcess.class,
                new GasifierProcess(
                        owner,
                        inventory(),
                        fluid().orElseThrow(() -> new IllegalArgumentException(
                                "Biomass gasifier requires a gas output tank"
                        )),
                        recipeType
                )
        );
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected void runAdditionalAutoOutputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.OUTPUT)) {
            FluidAutoTransfer.pushOutputs(
                    owner,
                    fluid().orElseThrow().tank(),
                    sideConfiguration().orElseThrow(),
                    GAS_AUTO_OUTPUT_PER_CYCLE
            );
        }
    }

    public GasifierProcess process() {
        return process;
    }
}
