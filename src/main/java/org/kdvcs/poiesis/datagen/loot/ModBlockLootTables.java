package org.kdvcs.poiesis.datagen.loot;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.block.crop.PoiesisCropBlock;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.Set;

public final class ModBlockLootTables extends BlockLootSubProvider {

    public ModBlockLootTables() {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags());
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.MECHANICAL_PROCESSOR.get());
        dropSelf(ModBlocks.SILICA_ROCK.get());
        dropSelf(ModBlocks.ALBICHROME_CASING.get());
        dropSelf(ModBlocks.MODULE_FABRICATOR.get());
        dropSelf(ModBlocks.COMPOUNDER.get());
        dropSelf(ModBlocks.SEPARATOR.get());
        dropSelf(ModBlocks.LEACHER.get());
        dropSelf(ModBlocks.BIOMASS_GASIFIER.get());
        dropSelf(ModBlocks.GAS_GENERATOR.get());
        dropSelf(ModBlocks.MICROBIAL_FUEL_CELL.get());
        dropSelf(ModBlocks.ALLOY_FACTORY.get());
        dropSelf(ModBlocks.GREENHOUSE.get());
        dropSelf(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get());
        dropSelf(ModBlocks.ADSORPTION_BED.get());
        dropSelf(ModBlocks.FLUID_DISTRIBUTOR.get());
        dropSelf(ModBlocks.BASIC_ENERGY_PIPE.get());
        dropSelf(ModBlocks.BASIC_ITEM_PIPE.get());
        dropSelf(ModBlocks.BASIC_FLUID_PIPE.get());
        dropSelf(ModBlocks.ALBICHROME_BLOCK.get());
        dropSelf(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get());
        dropSelf(ModBlocks.RAW_ALBICHROME_BLOCK.get());
        dropSelf(ModBlocks.HEAVY_MACHINE_CASING.get());
        dropSelf(ModBlocks.GRAPHITE_BLOCK.get());
        dropSelf(ModBlocks.PLAIN_CONCRETE.get());
        dropSelf(ModBlocks.PLAIN_CONCRETE_POWDER.get());
        dropSelf(ModBlocks.ALDER_LOG.get());
        dropSelf(ModBlocks.ALDER_WOOD.get());
        dropSelf(ModBlocks.STRIPPED_ALDER_LOG.get());
        dropSelf(ModBlocks.STRIPPED_ALDER_WOOD.get());
        dropSelf(ModBlocks.ALDER_PLANKS.get());
        dropSelf(ModBlocks.ALDER_ROOT.get());
        dropSelf(ModBlocks.ALDER_STAIRS.get());
        dropSelf(ModBlocks.ALDER_SLAB.get());
        dropSelf(ModBlocks.ALDER_FENCE.get());
        dropSelf(ModBlocks.ALDER_FENCE_GATE.get());
        dropSelf(ModBlocks.ALDER_BUTTON.get());
        add(ModBlocks.ALDER_DOOR.get(), block -> createDoorTable(block));
        dropSelf(ModBlocks.ALDER_TRAPDOOR.get());
        dropSelf(ModBlocks.ALDER_SAPLING.get());
        add(
                ModBlocks.ALDER_LEAVES.get(),
                block -> createLeavesDrops(
                        block,
                        ModBlocks.ALDER_SAPLING.get(),
                        NORMAL_LEAVES_SAPLING_CHANCES
                )
        );
        dropPottedContents(ModBlocks.POTTED_ALDER_SAPLING.get());

        add(
                ModBlocks.ALBICHROME_ORE.get(),
                block -> createOreDrop(
                        block,
                        ModItems.RAW_ALBICHROME.get()
                )
        );

        add(
                ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(),
                block -> createOreDrop(
                        block,
                        ModItems.RAW_ALBICHROME.get()
                )
        );

        for (ModCrops.CropEntry entry : ModCrops.all()) {
            CropBlock crop = (CropBlock) entry.crop().get();
            add(
                    crop,
                    createCropDrops(
                            crop,
                            entry.product().get(),
                            entry.seeds().get()
                    )
            );
        }
    }

    private static LootTable.Builder createCropDrops(
            CropBlock crop,
            ItemLike product,
            ItemLike seeds
    ) {
        int maxAge = crop.getMaxAge();

        LootItemCondition.Builder mature = LootItemBlockStatePropertyCondition
                .hasBlockStateProperties(crop)
                .setProperties(
                        StatePropertiesPredicate.Builder.properties()
                                .hasProperty(PoiesisCropBlock.AGE, maxAge)
                );

        LootItemCondition.Builder immature = createImmatureCondition(
                crop,
                maxAge
        );

        return LootTable.lootTable()
                .withPool(
                        LootPool.lootPool()
                                .when(mature)
                                .add(
                                        LootItem.lootTableItem(product)
                                                .apply(
                                                        ApplyBonusCount.addUniformBonusCount(
                                                                Enchantments.BLOCK_FORTUNE,
                                                                2
                                                        )
                                                )
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .when(mature)
                                .add(
                                        LootItem.lootTableItem(seeds)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                UniformGenerator.between(
                                                                        1.0F,
                                                                        3.0F
                                                                )
                                                        )
                                                )
                                                .apply(
                                                        ApplyBonusCount.addUniformBonusCount(
                                                                Enchantments.BLOCK_FORTUNE,
                                                                1
                                                        )
                                                )
                                )
                )
                .withPool(
                        LootPool.lootPool()
                                .when(immature)
                                .add(
                                        LootItem.lootTableItem(seeds)
                                                .apply(
                                                        SetItemCountFunction.setCount(
                                                                ConstantValue.exactly(1)
                                                        )
                                                )
                                )
                );
    }

    private static LootItemCondition.Builder createImmatureCondition(
            CropBlock crop,
            int maxAge
    ) {
        LootItemCondition.Builder condition = null;

        for (int age = 0; age < maxAge; age++) {
            LootItemCondition.Builder ageCondition =
                    LootItemBlockStatePropertyCondition
                            .hasBlockStateProperties(crop)
                            .setProperties(
                                    StatePropertiesPredicate.Builder.properties()
                                            .hasProperty(PoiesisCropBlock.AGE, age)
                            );

            condition = condition == null
                    ? ageCondition
                    : condition.or(ageCondition);
        }

        return condition;
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries()
                .stream()
                .map(RegistryObject::get)
                .filter(block -> !(block instanceof LiquidBlock))::iterator;
    }
}
