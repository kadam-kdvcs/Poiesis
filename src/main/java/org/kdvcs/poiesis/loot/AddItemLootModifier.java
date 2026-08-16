package org.kdvcs.poiesis.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.kdvcs.poiesis.registry.ModLootModifiers;

/**
 * Generic GLM that appends a fixed item stack after its loot conditions pass.
 *
 * Chance, source loot table, tool checks, etc. belong in the conditions rather
 * than being hard-coded here, so this modifier can be reused by future content.
 */
public final class AddItemLootModifier extends LootModifier {

    public static final Codec<AddItemLootModifier> CODEC =
            RecordCodecBuilder.create(instance ->
                    LootModifier.codecStart(instance)
                            .and(
                                    instance.group(
                                            ForgeRegistries.ITEMS.getCodec()
                                                    .fieldOf("item")
                                                    .forGetter(modifier -> modifier.item),
                                            Codec.INT
                                                    .optionalFieldOf("count", 1)
                                                    .forGetter(modifier -> modifier.count)
                                    )
                            )
                            .apply(instance, AddItemLootModifier::new)
            );

    private final Item item;
    private final int count;

    public AddItemLootModifier(
            LootItemCondition[] conditions,
            Item item,
            int count
    ) {
        super(conditions);
        this.item = item;
        this.count = Math.max(1, count);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        generatedLoot.add(new ItemStack(item, count));
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_ITEM.get();
    }
}
