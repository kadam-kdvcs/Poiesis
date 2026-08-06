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
                ModItems.EFFICIENCY_MODULE
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
}