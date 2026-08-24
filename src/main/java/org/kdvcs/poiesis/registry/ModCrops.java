package org.kdvcs.poiesis.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.block.crop.PoiesisCropBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single declaration point for ordinary Poiesis crops.
 *
 * Adding another standard crop should normally require only one register(...) call here
 * plus its textures. Block/item registration, blockstate models, loot, language and the
 * creative tab are derived automatically from this list.
 */
public final class ModCrops {
    private static final List<CropEntry> CROPS = new ArrayList<>();

    public static final CropEntry KENAF = register(
            "kenaf",
            "kenaf_stalk",
            "红麻种子",
            "Kenaf Seeds",
            "红麻茎秆",
            "Kenaf Stalk",
            0.11F
    );

    private static CropEntry register(
            String name,
            String productName,
            String seedZh,
            String seedEn,
            String productZh,
            String productEn,
            float wildSeedDropChance
    ) {
        String cropName = name + "_crop";
        String seedName = name + "_seeds";
        ResourceLocation seedId = new ResourceLocation(Poiesis.MOD_ID, seedName);
        ResourceLocation productId = new ResourceLocation(Poiesis.MOD_ID, productName);

        RegistryObject<Block> crop = ModBlocks.BLOCKS.register(
                cropName,
                () -> new PoiesisCropBlock(
                        BlockBehaviour.Properties.copy(Blocks.WHEAT)
                                .noOcclusion()
                                .noCollission(),
                        seedId,
                        productId
                )
        );

        RegistryObject<Item> seeds = ModItems.ITEMS.register(
                seedName,
                () -> new ItemNameBlockItem(
                        crop.get(),
                        new Item.Properties()
                )
        );

        RegistryObject<Item> product = ModItems.ITEMS.register(
                productName,
                () -> new Item(new Item.Properties())
        );

        CropEntry entry = new CropEntry(
                name,
                crop,
                seeds,
                product,
                seedZh,
                seedEn,
                productZh,
                productEn,
                wildSeedDropChance
        );
        CROPS.add(entry);
        return entry;
    }

    public static List<CropEntry> all() {
        return Collections.unmodifiableList(CROPS);
    }

    /** Forces this class to initialize before the DeferredRegisters are attached. */
    public static void bootstrap() {
    }

    public record CropEntry(
            String name,
            RegistryObject<Block> crop,
            RegistryObject<Item> seeds,
            RegistryObject<Item> product,
            String seedZh,
            String seedEn,
            String productZh,
            String productEn,
            float wildSeedDropChance
    ) {
        public String stageModel(int age) {
            return name + "_stage" + age;
        }
    }

    private ModCrops() {
    }
}
