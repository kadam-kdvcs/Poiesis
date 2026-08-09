package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;

public final class ModLanguageProvider extends LanguageProvider {
    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, Poiesis.MOD_ID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if ("zh_cn".equals(locale)) {
            add(ModItems.EXAMPLE_ITEM.get(), "示例材料");
            add(ModItems.PRESSED_PLATE.get(), "压制板材");

            add(ModItems.PRESSING_MODULE.get(), "压制模块");
            add(ModItems.EXTRUSION_MODULE.get(), "挤出模块");
            add(ModItems.GRINDING_MODULE.get(), "研磨模块");

            add(ModItems.SPEED_MODULE.get(), "超频模块");
            add(ModItems.EFFICIENCY_MODULE.get(), "效率模块");

            add(ModBlocks.EXAMPLE_BLOCK.get(), "示例方块");
            add(ModBlocks.MECHANICAL_PROCESSOR.get(), "加工站");
            add(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get(), "专业化模块基板");
            add(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(), "性能模块基板");
            add(ModBlocks.MODULE_FABRICATOR.get(), "模块装配台");

            add(ModBlocks.ALBICHROME_ORE.get(), "白铬矿石");
            add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(), "深层白铬矿石");

            add(ModItems.RAW_ALBICHROME.get(), "粗白铬");
            add(ModItems.ALBICHROME_INGOT.get(), "白铬锭");
            add(ModItems.ALBICHROME_NUGGET.get(), "白铬粒");

            add(ModBlocks.ALBICHROME_BLOCK.get(), "白铬块");
            add(ModBlocks.RAW_ALBICHROME_BLOCK.get(), "粗白铬块");

            add(ModBlocks.ALBICHROME_CASING.get(), "白铬外壳");
            add(ModItems.MACHINE_UNIT.get(), "机械单元");
            add(ModItems.CONTROL_ELEMENT.get(), "机械控制元件");
            add(ModItems.PIPE_CONFIGURATOR.get(), "管道配置器");
            add(ModBlocks.BASIC_ENERGY_PIPE.get(), "能量管道");
            add(ModBlocks.BASIC_ITEM_PIPE.get(), "物品管道");
            add(ModBlocks.BASIC_FLUID_PIPE.get(), "流体管道");

            add(ModBlocks.SILICA_ROCK.get(), "硅石");
            add(ModItems.SILICA_POWDER.get(), "硅石粉");
            add(ModItems.GRINDING_STONE.get(), "磨石");
            add(ModItems.GLASS_BATCH.get(), "玻璃料");

            add("gui.poiesis.module_fabricator.fabricate", "加工");
            add("gui.poiesis.module_fabricator.craft", "加工");
            add("gui.poiesis.module_fabricator.auto_craft", "自动合成");
            add("gui.poiesis.module_fabricator.auto_on", "已启用");
            add("gui.poiesis.module_fabricator.auto_off", "已禁用");
            add("gui.poiesis.module_fabricator.required_count", "需要：×%s");
            add("gui.poiesis.module_fabricator.insert_substrate", "放入模块基板来开始加工。");

            add("gui.poiesis.energy", "能量：%s FE / %s FE");
            add("gui.poiesis.power", "功率：%s FE/t");
            add("gui.poiesis.mechanical_processor.specialization_hint", "需要专业化模块！");

            add("gui.poiesis.io_button", "I/O");
            add("gui.poiesis.open_side_config", "配置机器输入/输出");
            add("gui.poiesis.machine_side_config.title", "%s - 输入/输出配置");
            add("gui.poiesis.io_channel.item", "物品");
            add("gui.poiesis.io_channel.fluid", "流体");
            add("gui.poiesis.io_channel.energy", "能量");
            add("gui.poiesis.relative_side.front", "前面");
            add("gui.poiesis.relative_side.back", "后面");
            add("gui.poiesis.relative_side.left", "左面");
            add("gui.poiesis.relative_side.right", "右面");
            add("gui.poiesis.relative_side.top", "上面");
            add("gui.poiesis.relative_side.bottom", "下面");
            add("gui.poiesis.side_config.left_click", "左键：切换到下一模式");
            add("gui.poiesis.side_config.right_click", "右键：切换到上一模式");
            add("gui.poiesis.side_mode.none", "无");
            add("gui.poiesis.side_mode.input", "输入");
            add("gui.poiesis.side_mode.output", "输出");
            add("gui.poiesis.side_mode.input_output", "输入/输出");
            add("gui.poiesis.auto_io.input", "自动输入");
            add("gui.poiesis.auto_io.output", "自动输出");
            add("gui.poiesis.auto_io.on", "开");
            add("gui.poiesis.auto_io.off", "关");
            add("gui.poiesis.auto_io.toggle", "%s：%s");

            add("gui.poiesis.io_config.title", "配置");
            add("tooltip.poiesis.pipe.transfer_rate", "传输速率：%s%s");
            add("message.poiesis.pipe_endpoint_mode", "管道端口：%s");
            add("message.poiesis.pipe_mode.insert", "输入");
            add("message.poiesis.pipe_mode.extract", "抽取");
            add("message.poiesis.pipe_mode.disabled", "禁用");

            add("itemGroup.poiesis.main", "Poiesis");
            return;
        }

        add(ModItems.EXAMPLE_ITEM.get(), "Example Material");
        add(ModItems.PRESSED_PLATE.get(), "Pressed Plate");

        add(ModItems.PRESSING_MODULE.get(), "Pressing Module");
        add(ModItems.EXTRUSION_MODULE.get(), "Extrusion Module");
        add(ModItems.GRINDING_MODULE.get(), "Grinding Module");

        add(ModItems.SPEED_MODULE.get(), "Overclock Module");
        add(ModItems.EFFICIENCY_MODULE.get(), "Efficiency Module");

        add(ModBlocks.EXAMPLE_BLOCK.get(), "Example Block");
        add(ModBlocks.MECHANICAL_PROCESSOR.get(), "Processor");
        add(ModItems.SPECIALIZATION_MODULE_SUBSTRATE.get(), "Specialization Module Substrate");
        add(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(), "Performance Module Substrate");
        add(ModBlocks.MODULE_FABRICATOR.get(), "Module Fabricator");

        add(ModBlocks.ALBICHROME_ORE.get(), "Albichrome Ore");
        add(ModBlocks.DEEPSLATE_ALBICHROME_ORE.get(), "Deepslate Albichrome Ore");

        add(ModItems.RAW_ALBICHROME.get(), "Raw Albichrome");
        add(ModItems.ALBICHROME_INGOT.get(), "Albichrome Ingot");
        add(ModItems.ALBICHROME_NUGGET.get(), "Albichrome Nugget");

        add(ModBlocks.ALBICHROME_BLOCK.get(), "Albichrome Block");
        add(ModBlocks.RAW_ALBICHROME_BLOCK.get(), "Raw Albichrome Block");

        add(ModBlocks.ALBICHROME_CASING.get(), "Albichrome Casing");

        add(ModItems.MACHINE_UNIT.get(), "Machine Unit");
        add(ModItems.CONTROL_ELEMENT.get(), "Control Element");
        add(ModItems.PIPE_CONFIGURATOR.get(), "Pipe Configurator");
        add(ModBlocks.BASIC_ENERGY_PIPE.get(), "Energy Pipe");
        add(ModBlocks.BASIC_ITEM_PIPE.get(), "Item Pipe");
        add(ModBlocks.BASIC_FLUID_PIPE.get(), "Fluid Pipe");

        add(ModBlocks.SILICA_ROCK.get(), "Silica Rock");
        add(ModItems.SILICA_POWDER.get(), "Silica Powder");

        add(ModItems.GRINDING_STONE.get(), "Grinding Stone");
        add(ModItems.GLASS_BATCH.get(), "Glass Batch");

        add("gui.poiesis.module_fabricator.fabricate", "Fabricate");
        add("gui.poiesis.module_fabricator.craft", "Craft");
        add("gui.poiesis.module_fabricator.auto_craft", "Auto-Craft");
        add("gui.poiesis.module_fabricator.auto_on", "Enabled");
        add("gui.poiesis.module_fabricator.auto_off", "Disabled");
        add("gui.poiesis.module_fabricator.required_count", "Required: ×%s");
        add("gui.poiesis.module_fabricator.insert_substrate", "Insert a module substrate to begin fabrication.");

        add("gui.poiesis.energy", "Energy: %s FE / %s FE");
        add("gui.poiesis.power", "Power: %s FE/t");
        add("gui.poiesis.mechanical_processor.specialization_hint", "Needs a specialization module!");

        add("gui.poiesis.io_button", "I/O");
        add("gui.poiesis.open_side_config", "Configure machine I/O");
        add("gui.poiesis.machine_side_config.title", "%s - I/O Configuration");
        add("gui.poiesis.io_channel.item", "Items");
        add("gui.poiesis.io_channel.fluid", "Fluids");
        add("gui.poiesis.io_channel.energy", "Energy");
        add("gui.poiesis.relative_side.front", "Front");
        add("gui.poiesis.relative_side.back", "Back");
        add("gui.poiesis.relative_side.left", "Left");
        add("gui.poiesis.relative_side.right", "Right");
        add("gui.poiesis.relative_side.top", "Top");
        add("gui.poiesis.relative_side.bottom", "Bottom");
        add("gui.poiesis.side_config.left_click", "Left click: next mode");
        add("gui.poiesis.side_config.right_click", "Right click: previous mode");
        add("gui.poiesis.side_mode.none", "None");
        add("gui.poiesis.side_mode.input", "Input");
        add("gui.poiesis.side_mode.output", "Output");
        add("gui.poiesis.side_mode.input_output", "Input/Output");
        add("gui.poiesis.auto_io.input", "Auto Input");
        add("gui.poiesis.auto_io.output", "Auto Output");
        add("gui.poiesis.auto_io.on", "On");
        add("gui.poiesis.auto_io.off", "Off");
        add("gui.poiesis.auto_io.toggle", "%s: %s");

        add("gui.poiesis.io_config.title", "Config");
        add("tooltip.poiesis.pipe.transfer_rate", "Transfer Rate: %s%s");
        add("message.poiesis.pipe_endpoint_mode", "Pipe endpoint: %s");
        add("message.poiesis.pipe_mode.insert", "Insert");
        add("message.poiesis.pipe_mode.extract", "Extract");
        add("message.poiesis.pipe_mode.disabled", "Disabled");

        add("itemGroup.poiesis.main", "Poiesis");
    }
}