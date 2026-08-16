package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.LanguageProvider;
import org.kdvcs.poiesis.Poiesis;
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

        tr(ModItems.PRESSING_MODULE.get(), "压制模块", "Pressing Module");
        tr(ModItems.EXTRUSION_MODULE.get(), "挤出模块", "Extrusion Module");
        tr(ModItems.GRINDING_MODULE.get(), "研磨模块", "Grinding Module");
        tr(ModItems.SPEED_MODULE.get(), "超频模块", "Overclock Module");
        tr(ModItems.EFFICIENCY_MODULE.get(), "效率模块", "Efficiency Module");

        tr(ModBlocks.MECHANICAL_PROCESSOR.get(), "加工站", "Processor");
        tr(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get(), "专业化模块基板", "Specialization Module Substrate");
        tr(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(), "性能模块基板", "Performance Module Substrate");
        tr(ModBlocks.MODULE_FABRICATOR.get(), "模块装配台", "Module Fabricator");
        tr(ModBlocks.COMPOUNDER.get(), "混配机", "Compounder");

        tr(ModBlocks.ALBICHROME_ORE.get(), "白铬矿石", "Albichrome Ore");
        tr(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(), "深层白铬矿石", "Deepslate Albichrome Ore");
        tr(ModItems.RAW_ALBICHROME.get(), "粗白铬", "Raw Albichrome");
        tr(ModItems.ALBICHROME_INGOT.get(), "白铬锭", "Albichrome Ingot");
        tr(ModItems.ALBICHROME_NUGGET.get(), "白铬粒", "Albichrome Nugget");
        tr(ModBlocks.ALBICHROME_BLOCK.get(), "白铬块", "Albichrome Block");
        tr(ModBlocks.RAW_ALBICHROME_BLOCK.get(), "粗白铬块", "Raw Albichrome Block");
        tr(ModBlocks.ALBICHROME_CASING.get(), "白铬外壳", "Albichrome Casing");

        tr(ModItems.MACHINE_UNIT.get(), "机械单元", "Machine Unit");
        tr(ModItems.CONTROL_ELEMENT.get(), "机械控制元件", "Control Element");
        tr(ModItems.PIPE_CONFIGURATOR.get(), "管道配置器", "Pipe Configurator");
        tr(ModBlocks.BASIC_ENERGY_PIPE.get(), "能量管道", "Energy Pipe");
        tr(ModBlocks.BASIC_ITEM_PIPE.get(), "物品管道", "Item Pipe");
        tr(ModBlocks.BASIC_FLUID_PIPE.get(), "流体管道", "Fluid Pipe");

        tr(ModBlocks.SILICA_ROCK.get(), "硅石", "Silica Rock");
        tr(ModItems.SILICA_POWDER.get(), "硅石粉", "Silica Powder");
        tr(ModItems.GRINDING_STONE.get(), "磨石", "Grinding Stone");
        tr(ModItems.GLASS_BATCH.get(), "玻璃料", "Glass Batch");
        tr(ModItems.RAW_KENAF_BAST.get(), "粗红麻韧皮", "Raw Kenaf Bast");
        tr(ModItems.KENAF_CORE.get(), "红麻芯材", "Kenaf Core");
        tr(ModItems.FIBER_COMPOSITE.get(), "纤维复合料", "Fiber Composite");
        tr(ModItems.FILTER_PLATE.get(), "滤板", "Filter Plate");

        tr("gui.poiesis.module_fabricator.fabricate", "加工", "Fabricate");
        tr("gui.poiesis.module_fabricator.craft", "加工", "Craft");
        tr("gui.poiesis.module_fabricator.auto_craft", "自动合成", "Auto-Craft");
        tr("gui.poiesis.module_fabricator.auto_on", "已启用", "Enabled");
        tr("gui.poiesis.module_fabricator.auto_off", "已禁用", "Disabled");
        tr("gui.poiesis.module_fabricator.required_count", "需要：×%s", "Required: ×%s");
        tr("gui.poiesis.module_fabricator.insert_substrate", "放入模块基板来开始加工。", "Insert a module substrate to begin fabrication.");

        tr("gui.poiesis.energy", "能量：%s FE / %s FE", "Energy: %s FE / %s FE");
        tr("gui.poiesis.power", "功率：%s FE/t", "Power: %s FE/t");
        tr("gui.poiesis.mechanical_processor.specialization_hint", "需要专业化模块！", "Needs a specialization module!");

        tr("jei.poiesis.category.mechanical_processor.pressing", "加工站：压制", "Processor: Pressing");
        tr("jei.poiesis.category.mechanical_processor.grinding", "加工站：研磨", "Processor: Grinding");
        tr("jei.poiesis.category.mechanical_processor.extrusion", "加工站：挤出", "Processor: Extrusion");
        tr("jei.poiesis.category.compounder", "混配机", "Compounder");
        tr("jei.poiesis.recipe.duration", "加工时长：%s tick", "Processing Time: %s ticks");
        tr("jei.poiesis.recipe.base_power", "基础功率：%s FE/t", "Base Power: %s FE/t");

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
        tr("gui.poiesis.side_mode.output", "输出", "Output");
        tr("gui.poiesis.side_mode.input_output", "输入/输出", "Input/Output");
        tr("gui.poiesis.auto_io.input", "自动输入", "Auto Input");
        tr("gui.poiesis.auto_io.output", "自动输出", "Auto Output");
        tr("gui.poiesis.auto_io.on", "开", "On");
        tr("gui.poiesis.auto_io.off", "关", "Off");
        tr("gui.poiesis.auto_io.toggle", "%s：%s", "%s: %s");

        tr("gui.poiesis.io_config.title", "配置", "Config");
        tr("tooltip.poiesis.pipe.transfer_rate", "传输速率：%s%s", "Transfer Rate: %s%s");
        tr("message.poiesis.pipe_endpoint_mode", "管道端口：%s", "Pipe endpoint: %s");
        tr("message.poiesis.pipe_mode.insert", "输入", "Insert");
        tr("message.poiesis.pipe_mode.extract", "抽取", "Extract");
        tr("message.poiesis.pipe_mode.disabled", "禁用", "Disabled");
        tr("itemGroup.poiesis.main", "Poiesis", "Poiesis");
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
