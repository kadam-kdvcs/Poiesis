package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.mekanism.MekanismAlbichromeCompat;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;
import org.kdvcs.poiesis.registry.ModItems;

/**
 * Bilingual language provider with a single declaration per translation key.
 * This prevents en_us / zh_cn lists from silently drifting apart.
 */
public final class ModLanguageProvider extends LanguageProvider {
    private final boolean chinese;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Poiesis.MOD_ID, locale);
        this.chinese = "zh_cn".equals(locale);
    }

    @Override
    protected void addTranslations() {
        for (ModCrops.CropEntry crop : ModCrops.all()) {
            tr(crop.seeds().get(), crop.seedZh(), crop.seedEn());
            tr(crop.product().get(), crop.productZh(), crop.productEn());
        }

        tr(ModItems.PRESSING_MODULE.get(), "模块：高压致密成形", "Pressing Module");
        tr(ModItems.EXTRUSION_MODULE.get(), "模块：连续塑性挤压", "Extrusion Module");
        tr(ModItems.GRINDING_MODULE.get(), "模块：颗粒机械解离", "Grinding Module");
        tr(ModItems.SPEED_MODULE.get(), "超频模块", "Overclock Module");
        tr(ModItems.EFFICIENCY_MODULE.get(), "效率模块", "Efficiency Module");
        tr(ModItems.HIGH_SHEAR_MODULE.get(), "模块：高剪切流变学", "High Shear Module");
        tr(ModItems.WET_SEPARATION_MODULE.get(), "模块：液相选择分离", "Wet Separation Module");
        tr(ModItems.RINSING_MODULE.get(), "模块：水相杂质洗脱", "Rinsing Module");
        tr(ModItems.ELECTROSTATIC_SEPARATION_MODULE.get(), "模块：静电富集分选", "Electrostatic Separation Module");

        tr(ModBlocks.MECHANICAL_PROCESSOR.get(), "加工站", "Processor");
        tr(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get(), "专业化模块基板", "Specialization Module Substrate");
        tr(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(), "性能模块基板", "Performance Module Substrate");
        tr(ModBlocks.MODULE_FABRICATOR.get(), "装配台", "Fabricator");
        tr(ModBlocks.COMPOUNDER.get(), "混配机", "Compounder");
        tr(ModBlocks.SEPARATOR.get(), "分离机", "Separator");
        tr(ModBlocks.LEACHER.get(), "浸取机", "Leacher");
        tr(ModBlocks.BIOMASS_GASIFIER.get(), "气化炉", "Gasifier");
        tr(ModBlocks.GAS_GENERATOR.get(), "煤气发电机", "Gas Generator");
        tr(ModBlocks.MICROBIAL_FUEL_CELL.get(), "微生物燃料电池", "MFC");
        tr("gui.poiesis.mfc.swap_fluids", "交换液体", "Swap Fluids");
        tr("gui.poiesis.mfc.anolyte_chamber", "阳极室", "Anode Chamber");
        tr("gui.poiesis.mfc.catholyte_chamber", "阴极室", "Cathode Chamber");
        tr(ModBlocks.ALLOY_FACTORY.get(), "合金铸造工厂", "Alloy Factory");
        tr(ModBlocks.GREENHOUSE.get(), "温室", "Greenhouse");
        tr(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get(), "吸附塔控制器", "Adsorption Column Controller");
        tr(ModBlocks.ADSORPTION_BED.get(), "吸附床", "Adsorption Bed");
        tr(ModBlocks.FLUID_DISTRIBUTOR.get(), "流体分布器", "Fluid Distributor");

        tr(ModBlocks.ALDER_LOG.get(), "桤木原木", "Alder Log");
        tr(ModBlocks.ALDER_WOOD.get(), "桤木", "Alder Wood");
        tr(ModBlocks.STRIPPED_ALDER_LOG.get(), "去皮桤木原木", "Stripped Alder Log");
        tr(ModBlocks.STRIPPED_ALDER_WOOD.get(), "去皮桤木", "Stripped Alder Wood");
        tr(ModBlocks.ALDER_PLANKS.get(), "桤木木板", "Alder Planks");
        tr(ModBlocks.ALDER_ROOT.get(), "桤木根", "Alder Root");
        tr(ModBlocks.ALDER_STAIRS.get(), "桤木楼梯", "Alder Stairs");
        tr(ModBlocks.ALDER_SLAB.get(), "桤木台阶", "Alder Slab");
        tr(ModBlocks.ALDER_FENCE.get(), "桤木栅栏", "Alder Fence");
        tr(ModBlocks.ALDER_FENCE_GATE.get(), "桤木栅栏门", "Alder Fence Gate");
        tr(ModBlocks.ALDER_BUTTON.get(), "桤木按钮", "Alder Button");
        tr(ModBlocks.ALDER_DOOR.get(), "桤木门", "Alder Door");
        tr(ModBlocks.ALDER_TRAPDOOR.get(), "桤木活板门", "Alder Trapdoor");
        tr(ModBlocks.ALDER_LEAVES.get(), "桤木树叶", "Alder Leaves");
        tr(ModBlocks.ALDER_SAPLING.get(), "桤木树苗", "Alder Sapling");

        tr(ModBlocks.ALBICHROME_ORE.get(), "白铬矿石", "Albichrome Ore");
        tr(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(), "深层白铬矿石", "Deepslate Albichrome Ore");
        tr(ModItems.RAW_ALBICHROME.get(), "粗白铬", "Raw Albichrome");
        tr(ModItems.ALBICHROME_INGOT.get(), "白铬锭", "Albichrome Ingot");
        tr(ModItems.ALBICHROME_NUGGET.get(), "白铬粒", "Albichrome Nugget");
        tr(ModItems.ALBICHROME_DUST.get(), "白铬粉", "Albichrome Dust");
        tr(ModItems.REFINED_ALBICHROME_SLURRY_BUCKET.get(), "白铬精制浆液桶", "Refined Albichrome Slurry Bucket");
        tr(ModItems.CHROMIUM_BEARING_WASTE_BUCKET.get(), "含铬废液桶", "Chromium-Bearing Waste Bucket");
        tr(ModItems.NUTRIENT_SOLUTION_BUCKET.get(), "标准营养液桶", "Nutrient Solution Bucket");
        tr(ModItems.ORGANIC_NUTRIENT_SOLUTION_BUCKET.get(), "有机营养液桶", "Organic Nutrient Solution Bucket");
        tr(ModItems.VITAMIN_SOLUTION_BUCKET.get(), "维生素溶液桶", "Vitamin Solution Bucket");
        tr(ModItems.WASTEWATER_BUCKET.get(), "污水桶", "Wastewater Bucket");
        tr(ModItems.IRON_DUST.get(), "铁粉", "Iron Dust");
        tr(ModItems.HEAT_RESISTANT_STEEL_DUST.get(), "耐热钢粉", "Heat-Resistant Steel Dust");
        tr(ModItems.HEAT_RESISTANT_STEEL_INGOT.get(), "耐热钢锭", "Heat-Resistant Steel Ingot");
        tr(ModItems.GRAPHITE_DUST.get(), "石墨粉", "Graphite Dust");
        tr(ModItems.VITAMIN_POWDER.get(), "维生素粉", "Vitamin Powder");
        tr(ModItems.GRAPHITE_ELECTRODE.get(), "石墨电极", "Graphite Electrode");
        tr(ModItems.GRAPHITE_PLATE.get(), "石墨板", "Graphite Plate");
        tr(ModItems.HEAT_RESISTANT_STEEL_HELMET.get(), "耐热钢头盔", "Heat-Resistant Steel Helmet");
        tr(ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get(), "耐热钢胸甲", "Heat-Resistant Steel Chestplate");
        tr(ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get(), "耐热钢护腿", "Heat-Resistant Steel Leggings");
        tr(ModItems.HEAT_RESISTANT_STEEL_BOOTS.get(), "耐热钢靴子", "Heat-Resistant Steel Boots");
        tr(MekanismAlbichromeCompat.DIRTY_ALBICHROME_DUST.get(), "污浊白铬粉", "Dirty Albichrome Dust");
        tr(MekanismAlbichromeCompat.ALBICHROME_CLUMP.get(), "白铬碎块", "Albichrome Clump");
        tr(MekanismAlbichromeCompat.ALBICHROME_SHARD.get(), "白铬碎片", "Albichrome Shard");
        tr(MekanismAlbichromeCompat.ALBICHROME_CRYSTAL.get(), "白铬晶体", "Albichrome Crystal");
        tr(ModBlocks.ALBICHROME_BLOCK.get(), "白铬块", "Albichrome Block");
        tr(ModBlocks.HEAT_RESISTANT_STEEL_BLOCK.get(), "耐热钢块", "Heat-Resistant Steel Block");
        tr(ModBlocks.GRAPHITE_BLOCK.get(), "石墨块", "Graphite Block");
        tr(ModBlocks.PLAIN_CONCRETE.get(), "素混凝土", "Plain Concrete");
        tr(ModBlocks.PLAIN_CONCRETE_POWDER.get(), "素混凝土粉末", "Plain Concrete Powder");
        tr(ModBlocks.RAW_ALBICHROME_BLOCK.get(), "粗白铬块", "Raw Albichrome Block");
        tr(ModBlocks.ALBICHROME_CASING.get(), "白铬外壳", "Albichrome Casing");
        tr(ModBlocks.HEAVY_MACHINE_CASING.get(), "重型机械外壳", "Heavy Machine Casing");

        tr(ModItems.MACHINE_UNIT.get(), "机械单元", "Machine Unit");
        tr(ModItems.FEEDBACK_LETTER.get(), "邮件", "Feedback Letter");
        tr(ModItems.CONTROL_ELEMENT.get(), "机械控制元件", "Control Element");
        tr(ModItems.CONTROL_CORE.get(), "控制核心", "Control Core");
        tr(ModItems.MACHINE_INTERFACE.get(), "机器接口", "Machine Interface");
        tr(ModItems.POWER_COUPLER.get(), "能量耦合器", "Power Coupler");
        tr(ModItems.PROCESS_ASSEMBLY.get(), "工艺组件", "Process Assembly");
        tr(ModItems.CALIBRATION_UNIT.get(), "精密校准件", "Calibration Unit");
        tr(ModItems.PIPE_CONFIGURATOR.get(), "管道配置器", "Pipe Configurator");
        tr(ModBlocks.BASIC_ENERGY_PIPE.get(), "能量管道", "Energy Pipe");
        tr(ModBlocks.BASIC_ITEM_PIPE.get(), "物品管道", "Item Pipe");
        tr(ModBlocks.BASIC_FLUID_PIPE.get(), "流体管道", "Fluid Pipe");

        tr(ModBlocks.SILICA_ROCK.get(), "硅石", "Silica Rock");
        tr(ModItems.SILICA_POWDER.get(), "硅石粉", "Silica Powder");
        tr(ModItems.GRINDING_STONE.get(), "磨石", "Grinding Stone");
        tr(
                "tooltip.poiesis.grinding_stone.remaining_uses",
                "剩余可用次数：%s",
                "Remaining uses: %s"
        );
        tr(ModItems.GLASS_BATCH.get(), "玻璃料", "Glass Batch");
        tr(ModItems.RAW_KENAF_BAST.get(), "粗红麻韧皮", "Raw Kenaf Bast");
        tr(ModItems.KENAF_CORE.get(), "红麻芯材", "Kenaf Core");
        tr(ModItems.FIBER_COMPOSITE.get(), "纤维复合料", "Fiber Composite");
        tr(ModItems.FILTER_PLATE.get(), "滤板", "Filter Plate");
        tr(ModItems.KENAF_FIBER.get(), "红麻纤维", "Kenaf Fiber");
        tr(ModItems.ACTIVATED_CARBON.get(), "活性炭", "Activated Carbon");
        tr(ModItems.HUMICS.get(), "腐殖质", "Humics");
        tr("fluid_type.poiesis.producer_gas", "发生炉煤气", "Producer Gas");

        tr("fluid_type.poiesis.refined_albichrome_slurry", "白铬精制浆液", "Refined Albichrome Slurry");
        tr("fluid_type.poiesis.chromium_bearing_waste", "含铬废液", "Chromium-Bearing Waste");
        tr("fluid_type.poiesis.nutrient_solution", "标准营养液", "Nutrient Solution");
        tr("fluid_type.poiesis.organic_nutrient_solution", "有机营养液", "Organic Nutrient Solution");
        tr("fluid_type.poiesis.vitamin_solution", "维生素溶液", "Vitamin Solution");
        tr("fluid_type.poiesis.wastewater", "污水", "Wastewater");
        tr("block.poiesis.refined_albichrome_slurry", "白铬精制浆液", "Refined Albichrome Slurry");
        tr("block.poiesis.chromium_bearing_waste", "含铬废液", "Chromium-Bearing Waste");
        tr("block.poiesis.nutrient_solution", "标准营养液", "Nutrient Solution");
        tr("block.poiesis.organic_nutrient_solution", "有机营养液", "Organic Nutrient Solution");
        tr("block.poiesis.vitamin_solution", "维生素溶液", "Vitamin Solution");
        tr("block.poiesis.wastewater", "污水", "Wastewater");

        tr("gui.poiesis.module_fabricator.fabricate", "加工", "Fabricate");
        tr("gui.poiesis.module_fabricator.craft", "加工", "Craft");
        tr("gui.poiesis.module_fabricator.auto_craft", "自动合成", "Auto-Craft");
        tr("gui.poiesis.alloy_factory.auto_smelting", "自动熔炼", "Auto-Smelting");
        tr("gui.poiesis.module_fabricator.auto_on", "已启用", "Enabled");
        tr("gui.poiesis.module_fabricator.auto_off", "已禁用", "Disabled");
        tr("gui.poiesis.module_fabricator.required_count", "需要：×%s", "Required: ×%s");
        tr("gui.poiesis.module_fabricator.insert_substrate", "放入模块基板来开始加工。", "Insert a module substrate to begin fabrication.");
        tr("gui.poiesis.alloy_factory.craft", "熔炼", "Smelt");
        tr("gui.poiesis.alloy_factory.auto_craft", "自动熔炼", "Auto-Smelting");
        tr("gui.poiesis.alloy_factory.no_alloys", "当前模组环境中没有发现可用的合金配方。", "No supported alloy recipes were found in the current mod set.");
        tr("gui.poiesis.alloy_factory.change_source", "切换目标产物来源", "Change target output source");
        tr("gui.poiesis.alloy_factory.current_source", "当前来源：%s", "Current source: %s");
        tr("gui.poiesis.alloy_factory.craftable_count", "当前可合成数量：%s", "Currently craftable: %s");
        tr("gui.poiesis.alloy_factory.switch_variant", "鼠标单击切换，左键下一个，右键上一个", "Click to switch: left-click for next, right-click for previous");

        tr("gui.poiesis.energy", "能量：%s FE / %s FE", "Energy: %s FE / %s FE");
        tr("gui.poiesis.power", "功率：%s FE/t", "Power: %s FE/t");
        tr("gui.poiesis.generator.generation", "当前发电：%s FE/t", "Generation: %s FE/t");
        tr("gui.poiesis.generator.output_power", "最大输出：%s FE/t", "Maximum Output: %s FE/t");
        tr("gui.poiesis.adsorber", "吸附器", "Adsorber");
        tr("gui.poiesis.separator.empty_fluid", "空", "Empty");
        tr("gui.poiesis.separator.fluid_amount", "%s mB / %s mB", "%s mB / %s mB");
        tr("gui.poiesis.separator.needs_specialization_module", "需要专业化模块！", "Needs a specialization module!");
        tr("gui.poiesis.gasifier.temperature", "炉温：%s°C / %s°C", "Temperature: %s°C / %s°C");
        tr("gui.poiesis.mechanical_processor.specialization_hint", "需要专业化模块！", "Needs a specialization module!");

        tr("jei.poiesis.category.mechanical_processor.pressing", "加工站：压制", "Processor: Pressing");
        tr("jei.poiesis.category.mechanical_processor.grinding", "加工站：研磨", "Processor: Grinding");
        tr("jei.poiesis.category.mechanical_processor.extrusion", "加工站：挤出", "Processor: Extrusion");
        tr("jei.poiesis.category.compounder", "混配机", "Compounder");
        tr("jei.poiesis.category.compounder.high_shear", "混配机：高剪切", "Compounder: High Shear");
        tr("jei.poiesis.category.separator.dry", "分离机：干式分离", "Separator: Dry Separation");
        tr("jei.poiesis.category.separator.wet", "分离机：湿式分离", "Separator: Wet Separation");
        tr("jei.poiesis.category.separator.rinsing", "分离机：漂洗", "Separator: Rinsing");
        tr("jei.poiesis.category.separator.electrostatic", "分离机：静电分离", "Separator: Electrostatic Separation");
        tr("jei.poiesis.category.module_fabricator.specialization", "Fabricator：专业化模块", "Fabricator: Specialization Modules");
        tr("jei.poiesis.category.module_fabricator.performance", "Fabricator：性能模块", "Fabricator: Performance Modules");
        tr("jei.poiesis.category.adsorption_column", "吸附塔", "Adsorption Column");
        tr("jei.poiesis.category.gasifier", "气化炉", "Gasifier");
        tr("jei.poiesis.category.gas_generator", "煤气发电机", "Gas Generator");
        tr("jei.poiesis.category.microbial_fuel_cell", "微生物燃料电池", "MFC");
        tr("jei.poiesis.category.leacher", "浸取机", "Leacher");
        tr("jei.poiesis.category.greenhouse", "温室", "Greenhouse");
        tr("jei.poiesis.category.alloy_factory", "合金铸造工厂", "Alloy Factory");
        tr("jei.poiesis.recipe.duration", "加工时长：%s tick", "Processing Time: %s ticks");
        tr("jei.poiesis.recipe.base_power", "基础功率：%s FE/t", "Base Power: %s FE/t");
        tr("jei.poiesis.recipe.required_beds", "至少需要：%s 层吸附床", "Required: at least %s adsorption bed(s)");
        tr("jei.poiesis.recipe.output_chance", "产出概率：%s", "Output Chance: %s");
        tr("jei.poiesis.recipe.input_not_consumed", "不消耗", "Not Consumed");
        tr("jei.poiesis.recipe.minimum_temperature", "最低炉温：%s°C", "Minimum Temperature: %s°C");
        tr("jei.poiesis.recipe.energy_per_mb", "能量密度：%s FE/mB", "Energy Density: %s FE/mB");
        tr("jei.poiesis.recipe.generation_rate", "发电功率：%s FE/t", "Generation Rate: %s FE/t");
        tr("jei.poiesis.recipe.total_energy", "总发电量：%s FE", "Total Generation: %s FE");
        tr(
                "jei.poiesis.info.silica_powder.early_obtainment",
                "早期获取：主手持有硅石，副手持有磨石，然后潜行并右击原版砂轮，即可将硅石研磨成3—5个硅石粉。该操作会消耗硅石并损耗磨石耐久。制造研磨模块后，可以改用加工站稳定产出7个硅石粉。",
                "Early acquisition: Hold Silica Rock in your main hand and a Grinding Stone in your off hand, then sneak and right-click a vanilla Grindstone. This grinds the rock into 3–5 Silica Powder, consuming the rock and damaging the Grinding Stone. Once you have made a Grinding Module, a Processor can produce 7 Silica Powder consistently."
        );
        tr(
                "jei.poiesis.info.iron_dust.early_obtainment",
                "早期获取：主手持有铁锭，副手持有磨石，然后潜行并右击原版砂轮，即可将1个铁锭稳定研磨成1个铁粉。该操作会消耗铁锭并损耗磨石耐久。制造研磨模块后，也可以改用加工站处理。",
                "Early acquisition: Hold an Iron Ingot in your main hand and a Grinding Stone in your off hand, then sneak and right-click a vanilla Grindstone. This consistently grinds 1 Iron Ingot into 1 Iron Dust, consuming the ingot and damaging the Grinding Stone. Once you have made a Grinding Module, you can use a Processor instead."
        );
        tr(
                "jei.poiesis.info.albichrome_dust.early_obtainment",
                "早期获取：主手持有白铬锭，副手持有磨石，然后潜行并右击原版砂轮，即可将1个白铬锭稳定研磨成1个白铬粉。该操作会消耗白铬锭并损耗磨石耐久。制造研磨模块后，也可以改用加工站处理。",
                "Early acquisition: Hold an Albichrome Ingot in your main hand and a Grinding Stone in your off hand, then sneak and right-click a vanilla Grindstone. This consistently grinds 1 Albichrome Ingot into 1 Albichrome Dust, consuming the ingot and damaging the Grinding Stone. Once you have made a Grinding Module, you can use a Processor instead."
        );

        tr("gui.poiesis.io_button", "I/O", "I/O");
        tr("gui.poiesis.open_side_config", "配置机器输入/输出", "Configure machine I/O");
        tr("gui.poiesis.machine_side_config.title", "%s - 输入/输出配置", "%s - I/O Configuration");
        tr("gui.poiesis.io_channel.item", "物品", "Items");
        tr("gui.poiesis.io_channel.fluid", "流体", "Fluids");
        tr("gui.poiesis.io_channel.energy", "能量", "Energy");
        tr("gui.poiesis.relative_side.front", "前面", "Front");
        tr("gui.poiesis.relative_side.back", "后面", "Back");
        tr("gui.poiesis.relative_side.left", "左面", "Left");
        tr("gui.poiesis.relative_side.right", "右面", "Right");
        tr("gui.poiesis.relative_side.top", "上面", "Top");
        tr("gui.poiesis.relative_side.bottom", "下面", "Bottom");
        tr("gui.poiesis.side_config.left_click", "左键：切换到下一模式", "Left click: next mode");
        tr("gui.poiesis.side_config.right_click", "右键：切换到上一模式", "Right click: previous mode");
        tr("gui.poiesis.side_mode.none", "无", "None");
        tr("gui.poiesis.side_mode.input", "输入", "Input");
        tr("gui.poiesis.side_mode.input_anode", "输入（阳极室）", "Input (Anode)");
        tr("gui.poiesis.side_mode.input_cathode", "输入（阴极室）", "Input (Cathode)");
        tr("gui.poiesis.side_mode.output", "输出", "Output");
        tr("gui.poiesis.side_mode.input_output", "输入/输出", "Input/Output");
        tr("gui.poiesis.auto_io.input", "自动输入", "Auto Input");
        tr("gui.poiesis.auto_io.output", "自动输出", "Auto Output");
        tr("gui.poiesis.auto_io.on", "开", "On");
        tr("gui.poiesis.auto_io.off", "关", "Off");
        tr("gui.poiesis.auto_io.toggle", "%s：%s", "%s: %s");

        tr("jade.poiesis.progress", "进度：%s%%", "Progress: %s%%");
        tr("jade.poiesis.power", "当前功率：%s FE/t", "Current Power: %s FE/t");
        tr("jade.poiesis.fluid_input", "输入流体：%s %s/%s mB", "Input fluid: %s %s/%s mB");
        tr("jade.poiesis.fluid_output", "输出流体：%s %s/%s mB", "Output fluid: %s %s/%s mB");
        tr("jade.poiesis.greenhouse_water", "水储槽：%s %s/%s mB", "Water reservoir: %s %s/%s mB");
        tr("jade.poiesis.greenhouse_nutrient", "营养液槽：%s %s/%s mB", "Nutrient reservoir: %s %s/%s mB");
        tr("jade.poiesis.greenhouse_output", "产物流体槽：%s %s/%s mB", "Fluid output: %s %s/%s mB");
        tr("jade.poiesis.mfc_anolyte", "阳极液槽：%s %s/%s mB", "Anolyte reservoir: %s %s/%s mB");
        tr("jade.poiesis.mfc_catholyte", "阴极液槽：%s %s/%s mB", "Catholyte reservoir: %s %s/%s mB");
        tr("jade.poiesis.temperature", "炉温：%s°C", "Temperature: %s°C");
        tr("jade.poiesis.modules", "已安装模块：", "Installed Modules: ");
        tr("jade.poiesis.auto_crafting", "自动合成：%s", "Auto-Crafting: %s");
        tr("jade.poiesis.auto_smelting", "自动熔炼：%s", "Auto-Smelting: %s");
        tr("jade.poiesis.enabled", "开启", "Enabled");
        tr("jade.poiesis.disabled", "关闭", "Disabled");
        tr("jade.poiesis.structure.formed", "结构已成型（%s 层吸附床）", "Structure formed (%s adsorption beds)");
        tr("jade.poiesis.structure.invalid", "结构未成型", "Structure invalid");
        tr("jade.poiesis.port.distributor", "流体输入：仅顶面", "Fluid input: top face only");
        tr("jade.poiesis.port.bed", "无可用接口", "No accessible ports");
        tr(
                "config.jade.plugin_poiesis.machine_information",
                "Poiesis 机器信息",
                "Poiesis Machine Information"
        );

        tr("gui.poiesis.io_config.title", "配置", "Config");
        tr("gui.poiesis.feedback.title", "玩家反馈", "Player Feedback");
        tr("gui.poiesis.feedback.subject", "标题", "Subject");
        tr("gui.poiesis.feedback.content", "内容", "Content");
        tr("gui.poiesis.feedback.content_hint", "请输入你的想法、建议或问题", "Enter your idea, suggestion, or issue");
        tr("gui.poiesis.feedback.save_close", "保存并关闭", "Save & Close");
        tr("gui.poiesis.feedback.send", "发送", "Send");
        tr("gui.poiesis.feedback.saved", "反馈已保存到本地", "Feedback saved locally");
        tr("gui.poiesis.feedback.queued", "反馈已加入发送队列", "Feedback queued for sending");
        tr("gui.poiesis.feedback.failed", "保存失败，请稍后重试", "Could not save feedback; try again later");
        tr("tooltip.poiesis.pipe.transfer_rate", "传输速率：%s%s", "Transfer Rate: %s%s");
        tr("tooltip.poiesis.machine.max_energy", "最大储能：%s", "Maximum Energy: %s");
        tr("tooltip.poiesis.machine.specializations", "可用专业化模块：", "Available Specialization Modules:");
        tr("tooltip.poiesis.machine.specialization_entry", "-%s", "- %s");
        tr("tooltip.poiesis.module.machines", "可用机器：", "Available Machines:");
        tr("tooltip.poiesis.module.machine_entry", "-%s", "- %s");
        tr("message.poiesis.pipe_endpoint_mode", "管道端口：%s", "Pipe endpoint: %s");
        tr("message.poiesis.pipe_mode.insert", "输入", "Insert");
        tr("message.poiesis.pipe_mode.extract", "抽取", "Extract");
        tr("message.poiesis.pipe_mode.disabled", "禁用", "Disabled");
        tr(
                "message.poiesis.adsorption_column.formed",
                "吸附塔结构已成型（吸附床：%s）",
                "Adsorption column formed (%s adsorption beds)"
        );
        tr("itemGroup.poiesis.main", "Poiesis", "Poiesis");
        tr("slurry.poiesis.dirty_albichrome", "污浊白铬浆液", "Dirty Albichrome Slurry");
        tr("slurry.poiesis.clean_albichrome", "纯净白铬浆液", "Clean Albichrome Slurry");
    }

    private void tr(Item item, String zh, String en) {
        add(item, text(zh, en));
    }

    private void tr(Block block, String zh, String en) {
        add(block, text(zh, en));
    }

    private void tr(String key, String zh, String en) {
        add(key, text(zh, en));
    }

    private String text(String zh, String en) {
        return chinese ? zh : en;
    }
}
