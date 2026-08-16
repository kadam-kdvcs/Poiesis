package org.kdvcs.poiesis.machine.fabrication;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.api.MachineContext;
import org.kdvcs.poiesis.machine.definition.MachineDefinition;
import org.kdvcs.poiesis.recipe.ModuleFabricationRecipe;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.registry.ModRecipeTypes;

import java.util.Optional;

/**
 * Specialized runtime for the recipe-selected module fabricator.
 * Infrastructure is inherited from MachineRuntime; only fabrication-specific
 * input validation, recipe lookup and process ticking remain here.
 */
public final class ModuleFabricatorRuntime extends MachineRuntime {

    private final ModuleFabricationProcess process;

    public ModuleFabricatorRuntime(
            MachineContext owner,
            MachineDefinition definition
    ) {
        super(owner, definition, ModuleFabricatorRuntime::isValidInput);

        process = addComponent(
                ModuleFabricationProcess.class,
                new ModuleFabricationProcess(
                        inventory(),
                        energy(),
                        this::findRecipeById
                )
        );
    }

    private static boolean isValidInput(int slot, ItemStack stack) {
        if (slot == ModuleFabricationProcess.MODULE_BASE_SLOT) {
            return stack.is(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get())
                    || stack.is(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get());
        }

        if (slot >= ModuleFabricationProcess.FIRST_MATERIAL_SLOT
                && slot < ModuleFabricationProcess.MATERIAL_SLOT_END) {
            return !stack.is(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get())
                    && !stack.is(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get());
        }

        return false;
    }

    public Optional<ModuleFabricationRecipe> findRecipeById(ResourceLocation id) {
        if (owner.level() == null || id == null) {
            return Optional.empty();
        }

        return owner.level()
                .getRecipeManager()
                .byKey(id)
                .filter(recipe -> recipe.getType() == ModRecipeTypes.MODULE_FABRICATION.get())
                .map(recipe -> (ModuleFabricationRecipe) recipe);
    }

    @Override
    protected void tickProcess() {
        process.tick(owner::markPersistentDirty, owner::setWorking);
    }

    public ModuleFabricationProcess process() {
        return process;
    }
}
