package org.kdvcs.poiesis.machine;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.fluid.FluidComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.FluidAutoTransfer;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.machine.process.LeachingProcess;
import org.kdvcs.poiesis.recipe.LeachingRecipe;

import java.util.List;

public final class LeacherRuntime extends MachineRuntime {
    private static final int FLUID_TRANSFER_PER_CYCLE = 1_000;
    private final ModuleComponent modules;
    private final OutputFluidComponent outputFluid;
    private final LeachingProcess process;

    public LeacherRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<LeachingRecipe> recipeType
    ) {
        super(owner, definition);
        ModuleLayout layout = ModuleLayout.of(
                ModuleLayout.process(module -> false),
                ModuleLayout.performance(),
                ModuleLayout.performance()
        );
        modules = addComponent(ModuleComponent.class,
                new ModuleComponent(layout, owner::markPersistentDirty));
        outputFluid = addComponent(OutputFluidComponent.class,
                new OutputFluidComponent(definition.fluid().capacity(), owner::markPersistentDirty));
        process = addComponent(LeachingProcess.class, new LeachingProcess(
                owner, inventory(), fluid().orElseThrow(), outputFluid, modules,
                energy(), new VoltageOverclockPolicy(), recipeType
        ));
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected void runAdditionalAutoInputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.INPUT)) {
            FluidAutoTransfer.pullInputs(owner, fluid().orElseThrow().tank(),
                    sideConfiguration().orElseThrow(), FLUID_TRANSFER_PER_CYCLE);
        }
    }

    @Override
    protected void runAdditionalAutoOutputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.OUTPUT)) {
            FluidAutoTransfer.pushOutputs(owner, outputFluid.tank(),
                    sideConfiguration().orElseThrow(), FLUID_TRANSFER_PER_CYCLE);
        }
    }

    @Override protected List<IItemHandler> additionalDropHandlers() { return List.of(modules.items()); }
    public ModuleComponent modules() { return modules; }
    public OutputFluidComponent outputFluid() { return outputFluid; }
    public FluidComponent runtimeInputFluid() { return fluid().orElseThrow(); }
    public LeachingProcess process() { return process; }
}
