package org.kdvcs.poiesis.compat.mekanism;

import mekanism.api.MekanismAPI;
import mekanism.api.chemical.slurry.Slurry;
import mekanism.api.chemical.slurry.SlurryBuilder;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.tag.ModTags;

/**
 * Mekanism's item processing stages are data driven, but slurry types are
 * registry entries. Keeping the direct Mekanism references in this class lets
 * Poiesis remain loadable when the optional dependency is absent.
 */
public final class MekanismAlbichromeCompat {
    private static final int ALBICHROME_TINT = 0xDDE5EA;

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, Poiesis.MOD_ID);

    private static final DeferredRegister<Slurry> SLURRIES =
            DeferredRegister.create(MekanismAPI.SLURRY_REGISTRY_NAME, Poiesis.MOD_ID);

    public static final RegistryObject<Item> DIRTY_ALBICHROME_DUST = registerItem("dirty_albichrome_dust");
    public static final RegistryObject<Item> ALBICHROME_CLUMP = registerItem("albichrome_clump");
    public static final RegistryObject<Item> ALBICHROME_SHARD = registerItem("albichrome_shard");
    public static final RegistryObject<Item> ALBICHROME_CRYSTAL = registerItem("albichrome_crystal");

    public static final RegistryObject<Slurry> DIRTY_ALBICHROME = SLURRIES.register(
            "dirty_albichrome",
            () -> new Slurry(
                    SlurryBuilder.dirty()
                            .tint(ALBICHROME_TINT)
                            .ore(ModTags.Items.ORES_ALBICHROME)
            )
    );

    public static final RegistryObject<Slurry> CLEAN_ALBICHROME = SLURRIES.register(
            "clean_albichrome",
            () -> new Slurry(
                    SlurryBuilder.clean()
                            .tint(ALBICHROME_TINT)
                            .ore(ModTags.Items.ORES_ALBICHROME)
            )
    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        SLURRIES.register(eventBus);
    }

    private static RegistryObject<Item> registerItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    private MekanismAlbichromeCompat() {
    }
}
