package org.kdvcs.poiesis.machine.greenhouse;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.FluidAutoTransfer;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.recipe.GreenhouseRecipe;

import java.util.List;

public final class GreenhouseRuntime extends MachineRuntime {
    private static final int FLUID_TRANSFER_PER_CYCLE = 4_000;
    public static final int FLUID_CAPACITY = 64_000;

    private final GreenhouseWaterComponent water;
    private final GreenhouseNutrientFluidComponent nutrientFluid;
    private final OutputFluidComponent outputFluid;
    private final ModuleComponent modules;
    private final GreenhouseProcess process;

    public GreenhouseRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<GreenhouseRecipe> recipeType
    ) {
        super(owner, definition);
        water = addComponent(
                GreenhouseWaterComponent.class,
                new GreenhouseWaterComponent(FLUID_CAPACITY, owner::markPersistentDirty)
        );
        nutrientFluid = addComponent(
                GreenhouseNutrientFluidComponent.class,
                new GreenhouseNutrientFluidComponent(FLUID_CAPACITY, owner::markPersistentDirty)
        );
        outputFluid = addComponent(
                OutputFluidComponent.class,
                new OutputFluidComponent(FLUID_CAPACITY, owner::markPersistentDirty)
        );
        modules = addComponent(
                ModuleComponent.class,
                new ModuleComponent(
                        ModuleLayout.of(
                                ModuleLayout.process(module -> false),
                                ModuleLayout.performance(),
                                ModuleLayout.performance()
                        ),
                        owner::markPersistentDirty
                )
        );
        process = addComponent(
                GreenhouseProcess.class,
                new GreenhouseProcess(
                        owner,
                        inventory(),
                        water,
                        nutrientFluid,
                        outputFluid,
                        modules,
                        energy(),
                        new VoltageOverclockPolicy(),
                        recipeType
                )
        );
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected void runAdditionalAutoInputs() {
        if (!autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.INPUT)) {
            return;
        }
        FluidAutoTransfer.pullInputs(
                owner, water.tank(), sideConfiguration().orElseThrow(), FLUID_TRANSFER_PER_CYCLE
        );
        FluidAutoTransfer.pullInputs(
                owner, nutrientFluid.tank(), sideConfiguration().orElseThrow(),
                FLUID_TRANSFER_PER_CYCLE
        );
    }

    @Override
    protected void runAdditionalAutoOutputs() {
        if (autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.OUTPUT)) {
            FluidAutoTransfer.pushOutputs(
                    owner, outputFluid.tank(), sideConfiguration().orElseThrow(),
                    FLUID_TRANSFER_PER_CYCLE
            );
        }
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items());
    }

    public GreenhouseWaterComponent water() { return water; }
    public GreenhouseNutrientFluidComponent nutrientFluid() { return nutrientFluid; }
    public OutputFluidComponent outputFluid() { return outputFluid; }
    public ModuleComponent modules() { return modules; }
    public GreenhouseProcess process() { return process; }
}
