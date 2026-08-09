package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModItems;

public final class ModItemModelProvider extends ItemModelProvider {

    private static final ResourceLocation GENERATED =
            new ResourceLocation("minecraft", "item/generated");
    private static final ResourceLocation HANDHELD =
            new ResourceLocation("minecraft", "item/handheld");

    public ModItemModelProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(
                output,
                Poiesis.MOD_ID,
                existingFileHelper
        );
    }

    @Override
    protected void registerModels() {
        generated(
                ModItems.PRESSING_MODULE,
                ModItems.GRINDING_MODULE,
                ModItems.EXTRUSION_MODULE,
                ModItems.SPEED_MODULE,
                ModItems.EFFICIENCY_MODULE,
                ModItems.SPECIALIZATION_MODULE_SUBSTRATE,
                ModItems.PERFORMANCE_MODULE_SUBSTRATE,
                ModItems.ALBICHROME_INGOT,
                ModItems.RAW_ALBICHROME,
                ModItems.ALBICHROME_NUGGET,
                ModItems.CONTROL_ELEMENT,
                ModItems.MACHINE_UNIT,
                ModItems.SILICA_POWDER,
                ModItems.GRINDING_STONE,
                ModItems.GLASS_BATCH
        );

        handheld(
                ModItems.PIPE_CONFIGURATOR
        );
    }

    @SafeVarargs
    private void generated(
            RegistryObject<? extends Item>... items
    ) {
        for (RegistryObject<? extends Item> item : items) {
            generated(item);
        }
    }

    private void generated(
            RegistryObject<? extends Item> item
    ) {
        String name = item.getId().getPath();

        withExistingParent(name, GENERATED)
                .texture(
                        "layer0",
                        modLoc("item/" + name)
                );
    }

    private void handheld(
            RegistryObject<? extends Item> item
    ) {
        String name = item.getId().getPath();

        withExistingParent(
                name,
                HANDHELD
        )
                .texture(
                        "layer0",
                        modLoc("item/" + name)
                );
    }
}