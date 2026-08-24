package org.kdvcs.poiesis.machine;

import net.minecraft.world.item.crafting.RecipeType;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.fluid.AdsorptionOutputFluidComponent;
import org.kdvcs.poiesis.machine.process.AdsorptionColumnProcess;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.AdsorptionFluidAutoTransfer;
import org.kdvcs.poiesis.recipe.AdsorptionRecipe;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

public final class AdsorptionColumnRuntime extends MachineRuntime {
    private final BooleanSupplier formed;
    private final IntSupplier bedCount;
    private final AdsorptionOutputFluidComponent outputFluid;
    private final AdsorptionColumnProcess process;

    public AdsorptionColumnRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<AdsorptionRecipe> recipeType,
            BooleanSupplier formed,
            IntSupplier bedCount
    ) {
        super(owner, definition);
        this.formed = formed;
        this.bedCount = bedCount;

        outputFluid = addComponent(
                AdsorptionOutputFluidComponent.class,
                new AdsorptionOutputFluidComponent(
                        definition.fluid().capacity(),
                        owner::markPersistentDirty
                )
        );
        process = addComponent(
                AdsorptionColumnProcess.class,
                new AdsorptionColumnProcess(
                        owner,
                        inventory(),
                        fluid().orElseThrow(),
                        outputFluid,
                        energy(),
                        new VoltageOverclockPolicy(),
                        recipeType
                )
        );
    }

    @Override
    protected void tickProcess() {
        process.tick(
                formed.getAsBoolean(),
                bedCount.getAsInt(),
                owner::markPersistentDirty,
                owner::setWorking
        );
    }

    @Override
    protected boolean canRunAutoIo() {
        return formed.getAsBoolean();
    }

    @Override
    protected boolean canUseAutoIoSide(RelativeSide side) {
        return side != RelativeSide.TOP && side != RelativeSide.BOTTOM;
    }

    @Override
    protected void runAdditionalAutoOutputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.OUTPUT)) {
            AdsorptionFluidAutoTransfer.pushFromControllerSides(
                    owner,
                    outputFluid.tank(),
                    sideConfiguration().orElseThrow()
            );
        }
    }

    public AdsorptionOutputFluidComponent outputFluid() {
        return outputFluid;
    }

    public AdsorptionColumnProcess process() {
        return process;
    }
}
