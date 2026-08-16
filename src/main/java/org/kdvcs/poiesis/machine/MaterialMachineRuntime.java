package org.kdvcs.poiesis.machine;

import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.items.IItemHandler;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.machine.energy.VoltageOverclockPolicy;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.machine.module.ModuleLayout;
import org.kdvcs.poiesis.machine.process.MaterialPoweredProcess;
import org.kdvcs.poiesis.machine.process.MaterialRecipeResolver;
import org.kdvcs.poiesis.recipe.MaterialProcessRecipe;

import java.util.List;

/**
 * Runtime for the common "counted item inputs -> powered process -> item output"
 * machine family.
 *
 * Creating another machine in this family requires only a definition, a recipe
 * type and a module layout; no machine-specific Runtime/Process pair is needed.
 */
public final class MaterialMachineRuntime<R extends MaterialProcessRecipe>
        extends MachineRuntime {

    private final ModuleComponent modules;
    private final MaterialPoweredProcess<R> process;

    public MaterialMachineRuntime(
            MachineContext owner,
            MachineDefinition definition,
            RecipeType<R> recipeType,
            ModuleLayout moduleLayout
    ) {
        super(owner, definition);

        if (definition.moduleSlots() != moduleLayout.size()) {
            throw new IllegalArgumentException(
                    definition.id() + " declares " + definition.moduleSlots()
                            + " module slots but layout contains " + moduleLayout.size()
            );
        }

        modules = addComponent(
                ModuleComponent.class,
                new ModuleComponent(moduleLayout, owner::markPersistentDirty)
        );

        int inputStart = 0;
        int inputEndExclusive = definition.inputSlots();
        int outputSlot = definition.inputSlots();

        MaterialRecipeResolver<R> resolver = new MaterialRecipeResolver<>(
                owner,
                inventory(),
                modules,
                recipeType,
                inputStart,
                inputEndExclusive
        );

        process = addComponent(
                MaterialPoweredProcess.class,
                new MaterialPoweredProcess<>(
                        inventory(),
                        modules,
                        energy(),
                        new VoltageOverclockPolicy(),
                        resolver,
                        inputStart,
                        inputEndExclusive,
                        outputSlot
                )
        );
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    @Override
    protected List<IItemHandler> additionalDropHandlers() {
        return List.of(modules.items());
    }

    public ModuleComponent modules() {
        return modules;
    }

    public MaterialPoweredProcess<R> process() {
        return process;
    }
}
