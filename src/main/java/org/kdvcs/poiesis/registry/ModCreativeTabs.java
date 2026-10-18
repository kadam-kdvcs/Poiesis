package org.kdvcs.poiesis.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.mekanism.MekanismAlbichromeCompat;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Poiesis.MOD_ID);
    public static final RegistryObject<CreativeModeTab> MAIN = TABS.register("main", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.poiesis.main"))
            .icon(() -> ModBlocks.MECHANICAL_PROCESSOR.get().asItem().getDefaultInstance())
            .displayItems((parameters, output) -> {
                for (ModCrops.CropEntry crop : ModCrops.all()) {
                    output.accept(crop.seeds().get());
                    output.accept(crop.product().get());
                }

                output.accept(ModItems.RAW_KENAF_BAST.get());
                output.accept(ModItems.KENAF_FIBER.get());
                output.accept(ModItems.KENAF_CORE.get());
                output.accept(ModItems.FIBER_COMPOSITE.get());
                output.accept(ModItems.FILTER_PLATE.get());

                output.accept(ModBlocks.ALDER_ROOT.get());
                output.accept(ModBlocks.ALDER_LOG.get());
                output.accept(ModBlocks.ALDER_WOOD.get());
                output.accept(ModBlocks.STRIPPED_ALDER_LOG.get());
                output.accept(ModBlocks.STRIPPED_ALDER_WOOD.get());
                output.accept(ModBlocks.ALDER_PLANKS.get());
                output.accept(ModBlocks.ALDER_STAIRS.get());
                output.accept(ModBlocks.ALDER_SLAB.get());
                output.accept(ModBlocks.ALDER_FENCE.get());
                output.accept(ModBlocks.ALDER_FENCE_GATE.get());
                output.accept(ModBlocks.ALDER_BUTTON.get());
                output.accept(ModBlocks.ALDER_DOOR.get());
                output.accept(ModBlocks.ALDER_TRAPDOOR.get());
                output.accept(ModBlocks.ALDER_LEAVES.get());
                output.accept(ModBlocks.ALDER_SAPLING.get());

                output.accept(ModBlocks.ALBICHROME_ORE.get());
                output.accept(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get());
                output.accept(ModBlocks.SILICA_ROCK.get());
                output.accept(ModItems.SILICA_POWDER.get());
                output.accept(ModItems.GLASS_BATCH.get());
                output.accept(ModItems.ACTIVATED_CARBON.get());
                output.accept(ModItems.HUMICS.get());
                output.accept(ModItems.VITAMIN_POWDER.get());

                output.accept(ModItems.GRAPHITE_DUST.get());
                output.accept(ModBlocks.GRAPHITE_BLOCK.get());
                output.accept(ModItems.GRAPHITE_PLATE.get());
                output.accept(ModItems.GRAPHITE_ELECTRODE.get());
                output.accept(ModBlocks.PLAIN_CONCRETE_POWDER.get());
                output.accept(ModBlocks.PLAIN_CONCRETE.get());

                output.accept(ModItems.RAW_ALBICHROME.get());
                output.accept(ModBlocks.RAW_ALBICHROME_BLOCK.get());
                output.accept(ModItems.ALBICHROME_INGOT.get());
                output.accept(ModBlocks.ALBICHROME_BLOCK.get());
                output.accept(ModItems.ALBICHROME_NUGGET.get());
                output.accept(ModItems.ALBICHROME_DUST.get());
                output.accept(ModItems.REFINED_ALBICHROME_SLURRY_BUCKET.get());
                output.accept(ModItems.CHROMIUM_BEARING_WASTE_BUCKET.get());
                output.accept(ModItems.NUTRIENT_SOLUTION_BUCKET.get());
                output.accept(ModItems.ORGANIC_NUTRIENT_SOLUTION_BUCKET.get());
                output.accept(ModItems.VITAMIN_SOLUTION_BUCKET.get());
                output.accept(ModItems.WASTEWATER_BUCKET.get());
                output.accept(ModItems.IRON_DUST.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_DUST.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_INGOT.get());
                output.accept(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_HELMET.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get());
                output.accept(ModItems.HEAT_RESISTANT_STEEL_BOOTS.get());
                if (ModList.get().isLoaded("mekanism")) {
                    output.accept(MekanismAlbichromeCompat.DIRTY_ALBICHROME_DUST.get());
                    output.accept(MekanismAlbichromeCompat.ALBICHROME_CLUMP.get());
                    output.accept(MekanismAlbichromeCompat.ALBICHROME_SHARD.get());
                    output.accept(MekanismAlbichromeCompat.ALBICHROME_CRYSTAL.get());
                }

                output.accept(ModItems.CONTROL_ELEMENT.get());
                output.accept(ModItems.GRINDING_STONE.get());
                output.accept(ModItems.PIPE_CONFIGURATOR.get());
                output.accept(ModItems.MACHINE_UNIT.get());
                output.accept(ModItems.CONTROL_CORE.get());
                output.accept(ModItems.MACHINE_INTERFACE.get());
                output.accept(ModItems.POWER_COUPLER.get());
                output.accept(ModItems.PROCESS_ASSEMBLY.get());
                output.accept(ModItems.CALIBRATION_UNIT.get());
                output.accept(ModItems.FEEDBACK_LETTER.get());

                output.accept(ModBlocks.BIOMASS_GASIFIER.get());
                output.accept(ModBlocks.GAS_GENERATOR.get());
                output.accept(ModBlocks.MICROBIAL_FUEL_CELL.get());
                output.accept(ModBlocks.ALBICHROME_CASING.get());
                output.accept(ModBlocks.MODULE_FABRICATOR.get());
                output.accept(ModBlocks.MECHANICAL_PROCESSOR.get());
                output.accept(ModBlocks.COMPOUNDER.get());
                output.accept(ModBlocks.SEPARATOR.get());
                output.accept(ModBlocks.LEACHER.get());
                output.accept(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get());
                output.accept(ModBlocks.ADSORPTION_BED.get());
                output.accept(ModBlocks.FLUID_DISTRIBUTOR.get());
                output.accept(ModBlocks.HEAVY_MACHINE_CASING.get());
                output.accept(ModBlocks.ALLOY_FACTORY.get());
                output.accept(ModBlocks.GREENHOUSE.get());
                output.accept(ModBlocks.BASIC_ENERGY_PIPE.get());
                output.accept(ModBlocks.BASIC_ITEM_PIPE.get());
                output.accept(ModBlocks.BASIC_FLUID_PIPE.get());

                output.accept(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get());
                output.accept(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get());
                output.accept(ModItems.PRESSING_MODULE.get());
                output.accept(ModItems.GRINDING_MODULE.get());
                output.accept(ModItems.EXTRUSION_MODULE.get());
                output.accept(ModItems.HIGH_SHEAR_MODULE.get());
                output.accept(ModItems.WET_SEPARATION_MODULE.get());
                output.accept(ModItems.RINSING_MODULE.get());
                output.accept(ModItems.ELECTROSTATIC_SEPARATION_MODULE.get());
                output.accept(ModItems.SPEED_MODULE.get());
                output.accept(ModItems.EFFICIENCY_MODULE.get());
            }).build());
    private ModCreativeTabs() { }
    public static void register(IEventBus eventBus) { TABS.register(eventBus); }
}
