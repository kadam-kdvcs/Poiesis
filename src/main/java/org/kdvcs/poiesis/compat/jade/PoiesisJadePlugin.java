package org.kdvcs.poiesis.compat.jade;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
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
import org.kdvcs.poiesis.machine.MachineRuntime;
import org.kdvcs.poiesis.machine.SeparatorRuntime;
import org.kdvcs.poiesis.machine.alloy.AlloyFactoryRuntime;
import org.kdvcs.poiesis.machine.fabrication.ModuleFabricatorRuntime;
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
        private static final String PRESENT = "poiesis_machine_present";
        private static final String PROGRESS = "poiesis_progress";
        private static final String MAX_PROGRESS = "poiesis_max_progress";
        private static final String POWER = "poiesis_power";
        private static final String MODULES = "poiesis_modules";
        private static final String FORMED = "poiesis_formed";
        private static final String BED_COUNT = "poiesis_bed_count";
        private static final String AUTO_CRAFTING = "poiesis_auto_crafting";
        private static final String TEMPERATURE = "poiesis_temperature";

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

            MachineRuntime runtime = machine.runtime();
            tag.putBoolean(PRESENT, true);

            if (runtime instanceof AlloyFactoryRuntime alloyFactory) {
                putProcess(
                        tag,
                        alloyFactory.process().progress(),
                        alloyFactory.process().maxProgress(),
                        alloyFactory.process().activeEnergyPerTick()
                );
                putModules(tag, alloyFactory.modules());
                tag.putBoolean(AUTO_CRAFTING, alloyFactory.process().autoCrafting());
            } else if (runtime instanceof MaterialMachineRuntime<?> material) {
                putProcess(
                        tag,
                        material.process().progress(),
                        material.process().maxProgress(),
                        material.process().activeEnergyPerTick()
                );
                putModules(tag, material.modules());
            } else if (runtime instanceof SeparatorRuntime separator) {
                putProcess(
                        tag,
                        separator.process().progress(),
                        separator.process().maxProgress(),
                        separator.process().activeEnergyPerTick()
                );
                putModules(tag, separator.modules());
            } else if (runtime instanceof ModuleFabricatorRuntime fabricator) {
                var process = fabricator.process();
                tag.putInt(PROGRESS, process.progress());
                tag.putInt(MAX_PROGRESS, process.maxProgress());
                tag.putInt(POWER, process.activeEnergyPerTick());
                tag.putBoolean(AUTO_CRAFTING, process.autoCrafting());
            } else if (runtime instanceof AdsorptionColumnRuntime adsorber
                    && machine instanceof AdsorptionColumnControllerBlockEntity controller) {
                putProcess(
                        tag,
                        adsorber.process().progress(),
                        adsorber.process().maxProgress(),
                        adsorber.process().activeEnergyPerTick()
                );
                tag.putBoolean(FORMED, controller.isFormed());
                tag.putInt(BED_COUNT, controller.bedCount());
            } else if (runtime instanceof BiomassGasifierRuntime gasifier) {
                putProcess(
                        tag,
                        gasifier.process().progress(),
                        gasifier.process().maxProgress(),
                        0
                );
                tag.putInt(TEMPERATURE, gasifier.process().temperature());
            }
        }

        private static void putProcess(
                CompoundTag tag,
                int progress,
                int maxProgress,
                int power
        ) {
            tag.putInt(PROGRESS, progress);
            tag.putInt(MAX_PROGRESS, maxProgress);
            tag.putInt(POWER, power);
        }

        private static void putModules(CompoundTag tag, ModuleComponent modules) {
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

        @Override
        public void appendTooltip(
                ITooltip tooltip,
                BlockAccessor accessor,
                IPluginConfig config
        ) {
            moveEnergyBarToTop(tooltip);

            CompoundTag data = accessor.getServerData();
            appendStructureInformation(tooltip, accessor, data);

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
