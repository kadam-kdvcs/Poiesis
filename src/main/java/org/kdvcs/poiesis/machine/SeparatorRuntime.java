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
import org.kdvcs.poiesis.recipe.SeparatorRecipe;

import java.util.List;

public final class SeparatorRuntime extends MachineRuntime {
    private final ResourceLocation wetCapability;
    private final ModuleComponent modules;
    private final SeparatorPoweredProcess process;

    public SeparatorRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<SeparatorRecipe> recipeType,
            ResourceLocation wetCapability
    ) {
        super(owner, definition);
        this.wetCapability = wetCapability;

        ModuleLayout layout = ModuleLayout.of(
                ModuleLayout.process(module -> module.kind() == ModuleKind.PROCESS
                        && module.capabilities().contains(wetCapability)),
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

        var fluid = fluid().orElseThrow(() -> new IllegalArgumentException(
                "Separator runtime requires a fluid tank"
        ));

        SeparatorRecipeResolver resolver = new SeparatorRecipeResolver(
                owner,
                inventory(),
                fluid,
                modules,
                recipeType,
                wetCapability
        );

        process = addComponent(
                SeparatorPoweredProcess.class,
                new SeparatorPoweredProcess(
                        inventory(),
                        fluid,
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
                && modules.snapshot().capabilities().contains(wetCapability);
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items());
    }

    public boolean fluidSlotEnabled() {
        return isFluidCapabilityEnabled();
    }

    public ModuleComponent modules() {
        return modules;
    }

    public SeparatorPoweredProcess process() {
        return process;
    }
}
