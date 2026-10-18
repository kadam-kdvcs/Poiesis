package org.kdvcs.poiesis.machine.module;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.item.tooltip.SpecializationModuleTooltip;

import java.util.List;
import java.util.Set;

public class MachineModuleItem extends Item {
    private final ModuleKind kind;
    private final MechanicalMode mode;
    private final Set<ResourceLocation> capabilities;
    private final int speedBonus;
    private final double energyMultiplier;
    private final SpecializationModuleTooltip specializationTooltip;

    public MachineModuleItem(
            Properties properties,
            ModuleKind kind,
            MechanicalMode mode,
            Set<ResourceLocation> capabilities,
            int speedBonus,
            double energyMultiplier
    ) {
        super(properties.stacksTo(1));
        this.kind = kind;
        this.mode = mode;
        this.capabilities = Set.copyOf(capabilities);
        this.speedBonus = speedBonus;
        this.energyMultiplier = energyMultiplier;
        this.specializationTooltip = kind == ModuleKind.PROCESS
                ? new SpecializationModuleTooltip(this.capabilities)
                : null;
    }

    public ModuleKind kind() {
        return kind;
    }

    public MechanicalMode mode() {
        return mode;
    }

    public Set<ResourceLocation> capabilities() {
        return capabilities;
    }

    public int speedBonus() {
        return speedBonus;
    }

    public double energyMultiplier() {
        return energyMultiplier;
    }

    @Override
    public Component getName(ItemStack stack) {
        ChatFormatting color = switch (kind) {
            case PROCESS -> ChatFormatting.LIGHT_PURPLE;
            case PERFORMANCE -> ChatFormatting.AQUA;
            default -> null;
        };
        return color == null
                ? super.getName(stack)
                : super.getName(stack).copy().withStyle(color);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (specializationTooltip != null) {
            specializationTooltip.append(stack, level, tooltip, flag);
        }
    }
}
