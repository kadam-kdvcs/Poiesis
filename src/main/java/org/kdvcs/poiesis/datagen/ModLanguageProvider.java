package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModItems;

public final class ModLanguageProvider extends LanguageProvider {
    private final String locale;
    public ModLanguageProvider(PackOutput output, String locale) { super(output, Poiesis.MOD_ID, locale); this.locale = locale; }
    @Override protected void addTranslations() {
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
            add(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(),"性能模块基板");

            add("gui.poiesis.energy", "能量：%s FE / %s FE");
            add("gui.poiesis.power", "功率：%s FE/t");
            add("gui.poiesis.mechanical_processor.specialization_hint", "需要专业化模块！");

            add("gui.poiesis.io_button", "I/O");
            add("gui.poiesis.open_side_config", "配置机器输入输出");
            add("gui.poiesis.machine_side_config.title", "%s - 输入输出配置");
            add("gui.poiesis.io_channel.item", "物品");
            add("gui.poiesis.io_channel.fluid", "流体");
            add("gui.poiesis.io_channel.energy", "能量");
            add("gui.poiesis.relative_side.front", "前");
            add("gui.poiesis.relative_side.back", "后");
            add("gui.poiesis.relative_side.left", "左");
            add("gui.poiesis.relative_side.right", "右");
            add("gui.poiesis.relative_side.top", "上");
            add("gui.poiesis.relative_side.bottom", "下");
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
        add(ModItems.PERFORMANCE_MODULE_SUBSTRATE.get(),"Performance Module Substrate");

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

        add("itemGroup.poiesis.main", "Poiesis");
    }
}
