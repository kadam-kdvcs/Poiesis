package org.kdvcs.poiesis.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.api.MachineIoHost;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleKind;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.machine.process.SeparatorPoweredProcess;
import org.kdvcs.poiesis.machine.process.SeparatorRecipeResolver;
import org.kdvcs.poiesis.machine.separator.ElectrodeComponent;
import org.kdvcs.poiesis.machine.fluid.OutputFluidComponent;
import org.kdvcs.poiesis.machine.io.AutoIoDirection;
import org.kdvcs.poiesis.machine.io.IoChannel;
import org.kdvcs.poiesis.machine.io.transfer.FluidAutoTransfer;
import org.kdvcs.poiesis.recipe.SeparatorRecipe;
import org.kdvcs.poiesis.tag.ModTags;

import java.util.List;

public final class SeparatorRuntime extends MachineRuntime {
    private static final int FLUID_TRANSFER_PER_CYCLE = 1_000;
    private final ResourceLocation wetCapability;
    private final ResourceLocation rinsingCapability;
    private final ResourceLocation electrostaticCapability;
    private final ModuleComponent modules;
    private final ElectrodeComponent electrode;
    private final OutputFluidComponent outputFluid;
    private final SeparatorPoweredProcess process;

    public SeparatorRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<SeparatorRecipe> recipeType,
            ResourceLocation wetCapability,
            ResourceLocation rinsingCapability,
            ResourceLocation electrostaticCapability
    ) {
        super(owner, definition);
        this.wetCapability = wetCapability;
        this.rinsingCapability = rinsingCapability;
        this.electrostaticCapability = electrostaticCapability;

        ModuleLayout layout = ModuleLayout.of(
                ModuleLayout.process(module -> module.kind() == ModuleKind.PROCESS
                        && (module.capabilities().contains(wetCapability)
                        || module.capabilities().contains(rinsingCapability)
                        || module.capabilities().contains(electrostaticCapability))),
                ModuleLayout.performance(),
                ModuleLayout.performance()
        );

        modules = addComponent(
                ModuleComponent.class,
                new ModuleComponent(layout, () -> {
                    owner.markPersistentDirty();
                    if (owner instanceof MachineIoHost host) {
                        host.refreshIoCapabilities();
                    }
                })
        );

        electrode = addComponent(
                ElectrodeComponent.class,
                new ElectrodeComponent(ModTags.Items.ELECTRODES, owner::markPersistentDirty)
        );

        outputFluid = addComponent(
                OutputFluidComponent.class,
                new OutputFluidComponent(
                        definition.fluid().capacity(),
                        owner::markPersistentDirty
                )
        );

        var fluid = fluid().orElseThrow(() -> new IllegalArgumentException(
                "Separator runtime requires a fluid tank"
        ));

        SeparatorRecipeResolver resolver = new SeparatorRecipeResolver(
                owner,
                inventory(),
                fluid,
                modules,
                recipeType,
                wetCapability,
                rinsingCapability,
                electrostaticCapability
        );

        process = addComponent(
                SeparatorPoweredProcess.class,
                new SeparatorPoweredProcess(
                        inventory(),
                        fluid,
                        outputFluid,
                        electrode,
                        modules,
                        energy(),
                        new VoltageOverclockPolicy(),
                        resolver
                )
        );
    }

    @Override
    protected void tickProcess() {
        if (owner.level() != null) {
            process.tick(
                    owner::markPersistentDirty,
                    owner::setWorking,
                    owner.level().random
            );
        }
    }

    @Override
    protected boolean isFluidCapabilityEnabled() {
        return modules != null
                && (modules.snapshot().capabilities().contains(wetCapability)
                || modules.snapshot().capabilities().contains(rinsingCapability));
    }

    @Override
    protected void runAdditionalAutoInputs() {
        if (fluidSlotEnabled()
                && autoIo().orElseThrow().enabled(
                IoChannel.FLUID,
                AutoIoDirection.INPUT
        )) {
            FluidAutoTransfer.pullInputs(
                    owner,
                    fluid().orElseThrow().tank(),
                    sideConfiguration().orElseThrow(),
                    FLUID_TRANSFER_PER_CYCLE
            );
        }
    }

    @Override
    protected void runAdditionalAutoOutputs() {
        if (rinsingModuleInstalled()
                && autoIo().orElseThrow().enabled(
                IoChannel.FLUID,
                AutoIoDirection.OUTPUT
        )) {
            FluidAutoTransfer.pushOutputs(
                    owner,
                    outputFluid.tank(),
                    sideConfiguration().orElseThrow(),
                    FLUID_TRANSFER_PER_CYCLE
            );
        }
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items(), electrode.items());
    }

    public boolean fluidSlotEnabled() {
        return isFluidCapabilityEnabled();
    }

    public boolean electrodeSlotEnabled() {
        return modules != null
                && modules.snapshot().capabilities().contains(electrostaticCapability);
    }

    public boolean rinsingModuleInstalled() {
        return modules != null
                && modules.snapshot().capabilities().contains(rinsingCapability);
    }

    public boolean electrodeSlotVisible() {
        return !fluidSlotEnabled()
                && (electrodeSlotEnabled() || !electrode.isEmpty());
    }

    public ElectrodeComponent electrode() {
        return electrode;
    }

    public ModuleComponent modules() {
        return modules;
    }

    public OutputFluidComponent outputFluid() {
        return outputFluid;
    }

    public SeparatorPoweredProcess process() {
        return process;
    }
}
