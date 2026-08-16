package org.kdvcs.poiesis.registry;

import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.loot.AddItemLootModifier;

public final class ModLootModifiers {

    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>>
            LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(
                    ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS,
                    Poiesis.MOD_ID
            );

    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM =
            LOOT_MODIFIER_SERIALIZERS.register(
                    "add_item",
                    () -> AddItemLootModifier.CODEC
            );

    public static void register(IEventBus eventBus) {
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }

    private ModLootModifiers() {
    }
}