package org.kdvcs.poiesis.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.AdsorptionBedBlock;
import org.kdvcs.poiesis.block.AdsorptionColumnControllerBlock;
import org.kdvcs.poiesis.block.FluidDistributorBlock;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.block.entity.FluidDistributorBlockEntity;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.machine.AdsorptionColumnRuntime;
import org.kdvcs.poiesis.machine.BiomassGasifierRuntime;
import org.kdvcs.poiesis.machine.MaterialMachineRuntime;
import org.kdvcs.poiesis.machine.LeacherRuntime;
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.SeparatorRuntime;
import org.kdvcs.poiesis.machine.alloy.AlloyFactoryRuntime;
import org.kdvcs.poiesis.machine.fabrication.ModuleFabricatorRuntime;
import org.kdvcs.poiesis.machine.greenhouse.GreenhouseRuntime;
import org.kdvcs.poiesis.machine.generator.GeneratorRuntime;
import org.kdvcs.poiesis.machine.module.ModuleComponent;
import org.kdvcs.poiesis.multiblock.AdsorptionColumnStructure;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.Identifiers;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.kdvcs.poiesis.compat.jade.JadeDataKeys.*;

/** Optional Jade integration. Jade discovers this class without a Poiesis-side hard reference. */
@WailaPlugin
public final class PoiesisJadePlugin implements IWailaPlugin {
    private static final MachineProvider MACHINE_PROVIDER = new MachineProvider();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(
                MACHINE_PROVIDER,
                AbstractMachineBlockEntity.class
        );
        registration.registerBlockDataProvider(
                MACHINE_PROVIDER,
                FluidDistributorBlockEntity.class
        );
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MACHINE_PROVIDER, AbstractMachineBlock.class);
        registration.registerBlockComponent(MACHINE_PROVIDER, AdsorptionColumnControllerBlock.class);
        registration.registerBlockComponent(MACHINE_PROVIDER, AdsorptionBedBlock.class);
        registration.registerBlockComponent(MACHINE_PROVIDER, FluidDistributorBlock.class);
    }

    private static final class MachineProvider
            implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
        private static final ResourceLocation UID =
                new ResourceLocation(Poiesis.MOD_ID, "machine_information");
        @Override
        public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
            AbstractMachineBlockEntity<?> machine = null;
            if (accessor.getBlockEntity() instanceof AbstractMachineBlockEntity<?> direct) {
                machine = direct;
            } else if (accessor.getBlockEntity() instanceof FluidDistributorBlockEntity) {
                machine = AdsorptionColumnStructure.findControllerEntity(
                        accessor.getLevel(),
                        accessor.getPosition()
                );
            }

            if (machine == null) {
                return;
            }

            tag.putBoolean(PRESENT, true);
            JadeMachineAdapters.append(tag, machine);
        }

        @Override
        public void appendTooltip(
                ITooltip tooltip,
                BlockAccessor accessor,
                IPluginConfig config
        ) {
            moveEnergyBarToTop(tooltip);

            CompoundTag data = accessor.getServerData();
            appendStructureInformation(tooltip, accessor, data);

            if (data.contains(FLUID_INPUT_CAPACITY)) {
                removeMergedFluidStorage(tooltip);
                appendFluidStorage(tooltip, data);
            }
            if (data.contains(GREENHOUSE_WATER_CAPACITY)) {
                removeMergedFluidStorage(tooltip);
                appendGreenhouseFluidStorage(tooltip, data);
            }
            if (data.contains(MFC_ANOLYTE_CAPACITY)) {
                removeMergedFluidStorage(tooltip);
                appendMfcFluidStorage(tooltip, data);
            }

            if (!data.getBoolean(PRESENT)) {
                return;
            }

            int maxProgress = data.getInt(MAX_PROGRESS);
            if (maxProgress > 0) {
                int percent = Math.min(
                        100,
                        Math.max(0, Math.round(data.getInt(PROGRESS) * 100.0F / maxProgress))
                );
                tooltip.add(Component.translatable(
                        "jade.poiesis.progress",
                        percent
                ).withStyle(ChatFormatting.AQUA));
            }

            int power = data.getInt(POWER);
            if (power > 0) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.power",
                        power
                ).withStyle(ChatFormatting.GRAY));
            }

            appendModules(tooltip, data);

            if (data.contains(TEMPERATURE)) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.temperature",
                        data.getInt(TEMPERATURE)
                ).withStyle(ChatFormatting.GOLD));
            }

            if (data.contains(AUTO_CRAFTING)) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.auto_crafting",
                        Component.translatable(data.getBoolean(AUTO_CRAFTING)
                                ? "jade.poiesis.enabled"
                                : "jade.poiesis.disabled")
                ).withStyle(ChatFormatting.GRAY));
            }
            if (data.contains(AUTO_SMELTING)) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.auto_smelting",
                        Component.translatable(data.getBoolean(AUTO_SMELTING)
                                ? "jade.poiesis.enabled"
                                : "jade.poiesis.disabled")
                ).withStyle(ChatFormatting.GRAY));
            }
        }

        private static void moveEnergyBarToTop(ITooltip tooltip) {
            List<IElement> energyElements = List.copyOf(
                    tooltip.get(Identifiers.UNIVERSAL_ENERGY_STORAGE)
            );
            if (energyElements.isEmpty()) {
                return;
            }

            tooltip.remove(Identifiers.UNIVERSAL_ENERGY_STORAGE);
            int insertAt = tooltip.get(Identifiers.CORE_OBJECT_NAME).isEmpty() ? 0 : 1;
            for (IElement energyElement : energyElements) {
                tooltip.add(insertAt++, energyElement);
            }
        }

        private static void removeMergedFluidStorage(ITooltip tooltip) {
            tooltip.remove(Identifiers.UNIVERSAL_FLUID_STORAGE);
            tooltip.remove(Identifiers.UNIVERSAL_FLUID_STORAGE_DETAILED);
        }

        private static void appendFluidStorage(ITooltip tooltip, CompoundTag data) {
            tooltip.add(Component.translatable(
                    "jade.poiesis.fluid_input",
                    fluidName(data.getInt(FLUID_INPUT_ID)),
                    data.getInt(FLUID_INPUT_AMOUNT),
                    data.getInt(FLUID_INPUT_CAPACITY)
            ).withStyle(ChatFormatting.DARK_AQUA));
            tooltip.add(Component.translatable(
                    "jade.poiesis.fluid_output",
                    fluidName(data.getInt(FLUID_OUTPUT_ID)),
                    data.getInt(FLUID_OUTPUT_AMOUNT),
                    data.getInt(FLUID_OUTPUT_CAPACITY)
            ).withStyle(ChatFormatting.DARK_AQUA));
        }

        private static void appendGreenhouseFluidStorage(ITooltip tooltip, CompoundTag data) {
            appendFluidLine(tooltip, data, "jade.poiesis.greenhouse_water",
                    GREENHOUSE_WATER_ID, GREENHOUSE_WATER_AMOUNT, GREENHOUSE_WATER_CAPACITY);
            appendFluidLine(tooltip, data, "jade.poiesis.greenhouse_nutrient",
                    GREENHOUSE_NUTRIENT_ID, GREENHOUSE_NUTRIENT_AMOUNT,
                    GREENHOUSE_NUTRIENT_CAPACITY);
            appendFluidLine(tooltip, data, "jade.poiesis.greenhouse_output",
                    GREENHOUSE_OUTPUT_ID, GREENHOUSE_OUTPUT_AMOUNT, GREENHOUSE_OUTPUT_CAPACITY);
        }

        private static void appendMfcFluidStorage(ITooltip tooltip, CompoundTag data) {
            appendFluidLine(tooltip, data, "jade.poiesis.mfc_anolyte",
                    MFC_ANOLYTE_ID, MFC_ANOLYTE_AMOUNT, MFC_ANOLYTE_CAPACITY);
            appendFluidLine(tooltip, data, "jade.poiesis.mfc_catholyte",
                    MFC_CATHOLYTE_ID, MFC_CATHOLYTE_AMOUNT, MFC_CATHOLYTE_CAPACITY);
        }

        private static void appendFluidLine(
                ITooltip tooltip,
                CompoundTag data,
                String translationKey,
                String idKey,
                String amountKey,
                String capacityKey
        ) {
            tooltip.add(Component.translatable(
                    translationKey,
                    fluidName(data.getInt(idKey)),
                    data.getInt(amountKey),
                    data.getInt(capacityKey)
            ).withStyle(ChatFormatting.DARK_AQUA));
        }

        private static Component fluidName(int registryId) {
            Fluid fluid = BuiltInRegistries.FLUID.byId(registryId);
            return fluid == null || fluid == Fluids.EMPTY
                    ? Component.translatable("gui.poiesis.separator.empty_fluid")
                    : Component.translatable(fluid.getFluidType().getDescriptionId());
        }

        private static void appendStructureInformation(
                ITooltip tooltip,
                BlockAccessor accessor,
                CompoundTag data
        ) {
            BlockState state = accessor.getBlockState();
            if (!(state.getBlock() instanceof AdsorptionColumnControllerBlock)
                    && !(state.getBlock() instanceof AdsorptionBedBlock)
                    && !(state.getBlock() instanceof FluidDistributorBlock)) {
                return;
            }

            boolean formed;
            int bedCount;
            if (data.contains(FORMED)) {
                formed = data.getBoolean(FORMED);
                bedCount = data.getInt(BED_COUNT);
            } else {
                BlockPos controllerPos = AdsorptionColumnStructure.findController(
                        accessor.getLevel(),
                        accessor.getPosition()
                );
                formed = controllerPos != null
                        && accessor.getLevel().getBlockState(controllerPos)
                        .getOptionalValue(AdsorptionColumnControllerBlock.FORMED)
                        .orElse(false);
                bedCount = controllerPos == null
                        ? 0
                        : AdsorptionColumnStructure.validate(
                                accessor.getLevel(),
                                controllerPos
                        ).bedCount();
            }

            tooltip.add(Component.translatable(
                    formed
                            ? "jade.poiesis.structure.formed"
                            : "jade.poiesis.structure.invalid",
                    bedCount
            ).withStyle(formed ? ChatFormatting.GREEN : ChatFormatting.RED));

            if (state.getBlock() instanceof FluidDistributorBlock) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.port.distributor"
                ).withStyle(ChatFormatting.DARK_AQUA));
            } else if (state.getBlock() instanceof AdsorptionBedBlock) {
                tooltip.add(Component.translatable(
                        "jade.poiesis.port.bed"
                ).withStyle(ChatFormatting.DARK_GRAY));
            }
        }

        private static void appendModules(ITooltip tooltip, CompoundTag data) {
            if (!data.contains(MODULES, Tag.TAG_LIST)) {
                return;
            }

            ListTag modules = data.getList(MODULES, Tag.TAG_COMPOUND);
            Map<Item, Integer> moduleCounts = new LinkedHashMap<>();
            for (int index = 0; index < modules.size(); index++) {
                ItemStack stack = ItemStack.of(modules.getCompound(index));
                if (!stack.isEmpty()) {
                    moduleCounts.merge(stack.getItem(), stack.getCount(), Integer::sum);
                }
            }

            if (moduleCounts.isEmpty()) {
                return;
            }

            tooltip.add(Component.translatable(
                    "jade.poiesis.modules"
            ).withStyle(ChatFormatting.GRAY));
            moduleCounts.forEach((item, count) -> {
                MutableComponent line = Component.literal("  • ")
                        .withStyle(ChatFormatting.DARK_GRAY)
                        .append(new ItemStack(item).getHoverName().copy()
                                .withStyle(ChatFormatting.WHITE));
                if (count > 1) {
                    line.append(Component.literal(" ×" + count)
                            .withStyle(ChatFormatting.GRAY));
                }
                tooltip.add(line);
            });
        }

        @Override
        public ResourceLocation getUid() {
            return UID;
        }

        @Override
        public int getDefaultPriority() {
            // Jade's universal storage providers use priority 1000. Run after
            // them so their existing energy element can be moved, not redrawn.
            return 2000;
        }
    }
}
