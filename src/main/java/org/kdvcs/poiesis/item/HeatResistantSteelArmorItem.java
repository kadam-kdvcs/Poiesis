package org.kdvcs.poiesis.item;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Armor piece that cannot accept Fire Protection. */
public final class HeatResistantSteelArmorItem extends ArmorItem {
    public HeatResistantSteelArmorItem(
            HeatResistantSteelArmorMaterial material,
            Type type,
            Properties properties
    ) {
        super(material, type, properties);
    }

    @Override
    public boolean canApplyAtEnchantingTable(
            ItemStack stack,
            Enchantment enchantment
    ) {
        return enchantment != Enchantments.FIRE_PROTECTION
                && super.canApplyAtEnchantingTable(stack, enchantment);
    }

    @Override
    public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
        if (book.getItem() instanceof EnchantedBookItem
                && book.getAllEnchantments().containsKey(Enchantments.FIRE_PROTECTION)) {
            return false;
        }
        return super.isBookEnchantable(stack, book);
    }
}
