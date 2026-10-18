package org.kdvcs.poiesis.compat.jade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.machine.greenhouse.GreenhouseRuntime;
import org.kdvcs.poiesis.machine.generator.MicrobialFuelCellRuntime;
import org.kdvcs.poiesis.machine.module.ModuleComponent;

import static org.kdvcs.poiesis.compat.jade.JadeDataKeys.*;

final class JadeDataWriter {
    private JadeDataWriter() {
    }

    static void process(CompoundTag tag, int progress, int maxProgress, int power) {
        tag.putInt(PROGRESS, progress);
        tag.putInt(MAX_PROGRESS, maxProgress);
        tag.putInt(POWER, power);
    }

    static void modules(CompoundTag tag, ModuleComponent modules) {
        ListTag stacks = new ListTag();
        for (int slot = 0; slot < modules.items().getSlots(); slot++) {
            ItemStack stack = modules.items().getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack.save(new CompoundTag()));
            }
        }
        if (!stacks.isEmpty()) {
            tag.put(MODULES, stacks);
        }
    }

    static void dualFluid(
            CompoundTag tag,
            FluidStack input,
            FluidStack output,
            int inputCapacity,
            int outputCapacity
    ) {
        fluid(tag, FLUID_INPUT_AMOUNT, FLUID_INPUT_CAPACITY, FLUID_INPUT_ID,
                input, inputCapacity);
        fluid(tag, FLUID_OUTPUT_AMOUNT, FLUID_OUTPUT_CAPACITY, FLUID_OUTPUT_ID,
                output, outputCapacity);
    }

    static void greenhouse(CompoundTag tag, GreenhouseRuntime greenhouse) {
        fluid(tag, GREENHOUSE_WATER_AMOUNT, GREENHOUSE_WATER_CAPACITY,
                GREENHOUSE_WATER_ID, greenhouse.water().fluid(), greenhouse.water().capacity());
        fluid(tag, GREENHOUSE_NUTRIENT_AMOUNT, GREENHOUSE_NUTRIENT_CAPACITY,
                GREENHOUSE_NUTRIENT_ID, greenhouse.nutrientFluid().fluid(),
                greenhouse.nutrientFluid().capacity());
        fluid(tag, GREENHOUSE_OUTPUT_AMOUNT, GREENHOUSE_OUTPUT_CAPACITY,
                GREENHOUSE_OUTPUT_ID, greenhouse.outputFluid().fluid(),
                greenhouse.outputFluid().capacity());
    }

    static void microbialFuelCell(CompoundTag tag, MicrobialFuelCellRuntime fuelCell) {
        fluid(tag, MFC_ANOLYTE_AMOUNT, MFC_ANOLYTE_CAPACITY, MFC_ANOLYTE_ID,
                fuelCell.anolyte().fluid(), fuelCell.anolyte().capacity());
        fluid(tag, MFC_CATHOLYTE_AMOUNT, MFC_CATHOLYTE_CAPACITY, MFC_CATHOLYTE_ID,
                fuelCell.catholyte().fluid(), fuelCell.catholyte().capacity());
    }

    private static void fluid(
            CompoundTag tag,
            String amountKey,
            String capacityKey,
            String idKey,
            FluidStack fluid,
            int capacity
    ) {
        tag.putInt(amountKey, fluid.getAmount());
        tag.putInt(capacityKey, capacity);
        tag.putInt(idKey, BuiltInRegistries.FLUID.getId(
                fluid.isEmpty() ? Fluids.EMPTY : fluid.getFluid()
        ));
    }
}
