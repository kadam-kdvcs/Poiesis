package org.kdvcs.poiesis.machine.generator;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.RelativeSide;
import org.kdvcs.poiesis.machine.io.SideIoMode;
import org.kdvcs.poiesis.recipe.MicrobialFuelCellRecipe;

public final class MicrobialFuelCellRuntime extends AbstractGeneratorRuntime {
    public static final int TANK_CAPACITY = 256_000;
    private static final int FLUID_AUTO_INPUT_PER_CYCLE = 4_000;

    private final RecipeType<MicrobialFuelCellRecipe> recipeType;
    private final MfcFluidComponent anolyte;
    private final MfcFluidComponent catholyte;
    private final MicrobialFuelCellProcess process;
    private final ElectrochemicalConfiguration configuration;

    public MicrobialFuelCellRuntime(
            MachineContext owner,
            GeneratorDefinition generatorDefinition,
            MachineDefinition machineDefinition,
            RecipeType<MicrobialFuelCellRecipe> recipeType
    ) {
        super(owner, generatorDefinition, machineDefinition);
        this.recipeType = recipeType;
        configuration = ElectrochemicalConfiguration.DEFAULT;
        anolyte = addComponent(MfcAnolyteComponent.class, new MfcAnolyteComponent(
                TANK_CAPACITY, this::acceptsAnolyte, owner::markPersistentDirty
        ));
        catholyte = addComponent(MfcCatholyteComponent.class, new MfcCatholyteComponent(
                TANK_CAPACITY, this::acceptsCatholyte, owner::markPersistentDirty
        ));
        process = addComponent(MicrobialFuelCellProcess.class, new MicrobialFuelCellProcess(
                owner, energy(), anolyte, catholyte, recipeType
        ));
    }

    @Override
    protected void tickGeneration() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected void runAdditionalAutoInputs() {
        if (!autoIo().orElseThrow().enabled(IoChannel.FLUID, AutoIoDirection.INPUT)) return;

        var configuration = sideConfiguration().orElseThrow();
        for (RelativeSide side : RelativeSide.values()) {
            SideIoMode mode = configuration.mode(IoChannel.FLUID, side);
            IFluidHandler destination = switch (mode) {
                case INPUT_ANODE -> anolyte.tank();
                case INPUT_CATHODE -> catholyte.tank();
                default -> null;
            };
            if (destination == null) continue;

            Direction worldSide = side.toWorldSide(owner.facing());
            BlockPos sourcePos = owner.pos().relative(worldSide);
            BlockEntity blockEntity = owner.level().getBlockEntity(sourcePos);
            if (blockEntity == null) continue;

            blockEntity.getCapability(
                    ForgeCapabilities.FLUID_HANDLER,
                    worldSide.getOpposite()
            ).ifPresent(source -> FluidUtil.tryFluidTransfer(
                    destination,
                    source,
                    FLUID_AUTO_INPUT_PER_CYCLE,
                    true
            ));
        }
    }

    public boolean acceptsAnolyte(FluidStack stack) {
        return acceptsAnyMfcFluid(stack);
    }

    public boolean acceptsCatholyte(FluidStack stack) {
        return acceptsAnyMfcFluid(stack);
    }

    private boolean acceptsAnyMfcFluid(FluidStack stack) {
        // MFC chambers are intentionally unrestricted. Recipes only decide
        // whether the ordered pair can generate power; they do not decide
        // which fluids may be stored in either chamber.
        return stack != null && !stack.isEmpty();
    }

    public MfcFluidComponent anolyte() { return anolyte; }
    public MfcFluidComponent catholyte() { return catholyte; }
    public MicrobialFuelCellProcess process() { return process; }
    public ElectrochemicalConfiguration electrochemicalConfiguration() { return configuration; }

    /** Swaps the two chambers before a reaction starts. */
    public boolean swapFluids() {
        if (process.hasActiveReaction()) return false;
        FluidStack left = anolyte.tank().getFluid().copy();
        FluidStack right = catholyte.tank().getFluid().copy();
        anolyte.tank().drain(anolyte.tank().getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
        catholyte.tank().drain(catholyte.tank().getFluidAmount(), IFluidHandler.FluidAction.EXECUTE);
        anolyte.tank().fill(right, IFluidHandler.FluidAction.EXECUTE);
        catholyte.tank().fill(left, IFluidHandler.FluidAction.EXECUTE);
        owner.markPersistentDirty();
        return true;
    }
}
