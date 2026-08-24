package org.kdvcs.poiesis.machine.alloy;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        super(owner, definition, (slot, stack) ->
                isValidAlloyMaterial(owner, slot, stack));

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

    private static boolean isValidAlloyMaterial(
            MachineContext owner,
            int slot,
            ItemStack stack
    ) {
        if (slot < AlloyFactoryProcess.FIRST_INPUT_SLOT
                || slot >= AlloyFactoryProcess.INPUT_SLOT_END) {
            return false;
        }
        return AlloyRecipeIndex.acceptsInput(owner.level(), stack);
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
