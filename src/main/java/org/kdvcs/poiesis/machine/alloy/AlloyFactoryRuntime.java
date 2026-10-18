package org.kdvcs.poiesis.machine.alloy;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.recipe.alloy.AlloyDefinition;
import org.kdvcs.poiesis.recipe.alloy.AlloyRecipeIndex;

import java.util.List;
import java.util.Optional;

public final class AlloyFactoryRuntime extends MachineRuntime {
    private final ModuleComponent modules;
    private final AlloyFactoryProcess process;

    public AlloyFactoryRuntime(MachineContext owner, MachineDefinition definition) {
        // The input inventory is intentionally unrestricted. Recipe matching
        // decides what can actually be processed, while players and item
        // automation may use the 18 input slots as a general material buffer.
        super(owner, definition);

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
                AlloyFactoryProcess.class,
                new AlloyFactoryProcess(inventory(), modules, energy(), this::findRecipeById)
        );
    }

    public Optional<AlloyDefinition> findRecipeById(ResourceLocation id) {
        return AlloyRecipeIndex.find(owner.level(), id);
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items());
    }

    public ModuleComponent modules() { return modules; }
    public AlloyFactoryProcess process() { return process; }
}
