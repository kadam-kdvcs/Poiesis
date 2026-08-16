package org.kdvcs.poiesis.machine.module;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

/**
 * Immutable description of the module slots exposed by a machine.
 *
 * The runtime owns one generic ModuleComponent; machine-specific rules live
 * here instead of requiring a dedicated component class for every machine.
 */
public final class ModuleLayout {

    public enum Role {
        PROCESS,
        PERFORMANCE,
        PROTECTION
    }

    public record SlotRule(
            Role role,
            Predicate<MachineModuleItem> validator
    ) {
        public SlotRule {
            if (role == null || validator == null) {
                throw new IllegalArgumentException("Module slot rule cannot contain null values");
            }
        }

        boolean accepts(ItemStack stack) {
            return stack.getItem() instanceof MachineModuleItem module
                    && validator.test(module);
        }
    }

    private final List<SlotRule> slots;

    private ModuleLayout(List<SlotRule> slots) {
        if (slots == null || slots.isEmpty()) {
            throw new IllegalArgumentException("Module layout must contain at least one slot");
        }
        this.slots = List.copyOf(slots);
    }

    public static ModuleLayout of(SlotRule... slots) {
        return new ModuleLayout(List.of(slots));
    }

    public static SlotRule process(Predicate<MachineModuleItem> validator) {
        return new SlotRule(Role.PROCESS, validator);
    }

    public static SlotRule performance() {
        return new SlotRule(
                Role.PERFORMANCE,
                module -> module.kind() == ModuleKind.PERFORMANCE
                        && module.mode() == MechanicalMode.NONE
        );
    }

    public static SlotRule protection() {
        return new SlotRule(
                Role.PROTECTION,
                module -> module.kind() == ModuleKind.PROTECTION
        );
    }

    /** Standard layout used by the mechanical processor. */
    public static ModuleLayout mechanicalProcessing() {
        return of(
                process(module -> module.kind() == ModuleKind.PROCESS
                        && module.mode() != MechanicalMode.NONE),
                performance(),
                performance()
        );
    }

    /** Standard layout used by machines whose process slot is a specialization capability. */
    public static ModuleLayout specialization() {
        return of(
                process(module -> module.kind() == ModuleKind.PROCESS
                        && module.mode() == MechanicalMode.NONE
                        && !module.capabilities().isEmpty()),
                performance(),
                performance()
        );
    }

    public int size() {
        return slots.size();
    }

    public SlotRule slot(int index) {
        return slots.get(index);
    }

    public Role role(int index) {
        return slot(index).role();
    }

    public boolean accepts(int index, ItemStack stack) {
        return index >= 0
                && index < slots.size()
                && slot(index).accepts(stack);
    }
}
