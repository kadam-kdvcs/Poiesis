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

            add("gui.poiesis.energy", "能量：%s FE / %s FE");
            add("gui.poiesis.power", "功率：%s FE/t");
            add("gui.poiesis.mechanical_processor.specialization_hint", "需要专业化模块！");

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

        add("gui.poiesis.energy", "Energy: %s FE / %s FE");
        add("gui.poiesis.power", "Power: %s FE/t");
        add("gui.poiesis.mechanical_processor.specialization_hint", "Needs a specialization module!");

        add("itemGroup.poiesis.main", "Poiesis");
    }
}
