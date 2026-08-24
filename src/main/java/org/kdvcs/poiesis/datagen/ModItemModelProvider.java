package org.kdvcs.poiesis.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.compat.mekanism.MekanismAlbichromeCompat;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.Set;

/**
 * Convention-based item model generation.
 *
 * New ordinary items automatically receive item/generated + item/<registry>
 * instead of requiring another line in this provider. BlockItems are skipped
 * because their models are owned by the block model pipeline.
 */
public final class ModItemModelProvider extends ItemModelProvider {

    private static final ResourceLocation GENERATED =
            new ResourceLocation("minecraft", "item/generated");
    private static final ResourceLocation HANDHELD =
            new ResourceLocation("minecraft", "item/handheld");

    private static final Set<String> HANDHELD_ITEMS = Set.of(
            "pipe_configurator"
    );

    private static final Set<String> MEKANISM_ALBICHROME_ITEMS = Set.of(
            "dirty_albichrome_dust",
            "albichrome_clump",
            "albichrome_shard",
            "albichrome_crystal"
    );

    public ModItemModelProvider(
            PackOutput output,
            ExistingFileHelper existingFileHelper
    ) {
        super(output, Poiesis.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        registerModels(ModItems.ITEMS.getEntries());
        registerModels(MekanismAlbichromeCompat.ITEMS.getEntries());
    }

    private void registerModels(Iterable<RegistryObject<Item>> entries) {
        for (RegistryObject<Item> registered : entries) {
            Item item = registered.get();
            if (item instanceof BlockItem
                    && !(item instanceof ItemNameBlockItem)) {
                continue;
            }

            String name = registered.getId().getPath();
            ResourceLocation parent = HANDHELD_ITEMS.contains(name)
                    ? HANDHELD
                    : GENERATED;

            ResourceLocation texture = MEKANISM_ALBICHROME_ITEMS.contains(name)
                    ? modLoc("item/mek/" + name)
                    : modLoc("item/" + name);

            withExistingParent(name, parent)
                    .texture("layer0", texture);
        }
    }
}
