package org.kdvcs.poiesis.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModItems;

import java.util.function.Supplier;

/**
 * Armor material for the Thermal Steel Suit.
 *
 * Its protection is deliberately close to iron: the material's defining
 * advantage is heat resistance, not general-purpose armor strength.
 */
public enum HeatResistantSteelArmorMaterial implements ArmorMaterial {
    INSTANCE(
            Poiesis.MOD_ID + ":heat_resistant_steel",
            30,
            new int[]{3, 6, 7, 3},
            14,
            SoundEvents.ARMOR_EQUIP_IRON,
            1.5F,
            0.0F,
            () -> Ingredient.of(ModItems.HEAT_RESISTANT_STEEL_INGOT.get())
    );

    private static final int[] HEALTH_PER_SLOT = {13, 15, 16, 11};

    private final String name;
    private final int durabilityMultiplier;
    private final int[] defense;
    private final int enchantmentValue;
    private final SoundEvent equipSound;
    private final float toughness;
    private final float knockbackResistance;
    private final Supplier<Ingredient> repairIngredient;

    HeatResistantSteelArmorMaterial(
            String name,
            int durabilityMultiplier,
            int[] defense,
            int enchantmentValue,
            SoundEvent equipSound,
            float toughness,
            float knockbackResistance,
            Supplier<Ingredient> repairIngredient
    ) {
        this.name = name;
        this.durabilityMultiplier = durabilityMultiplier;
        this.defense = defense;
        this.enchantmentValue = enchantmentValue;
        this.equipSound = equipSound;
        this.toughness = toughness;
        this.knockbackResistance = knockbackResistance;
        this.repairIngredient = repairIngredient;
    }

    @Override
    public int getDurabilityForType(ArmorItem.Type type) {
        return HEALTH_PER_SLOT[type.getSlot().getIndex()] * durabilityMultiplier;
    }

    @Override
    public int getDefenseForType(ArmorItem.Type type) {
        return defense[type.getSlot().getIndex()];
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public SoundEvent getEquipSound() {
        return equipSound;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public float getToughness() {
        return toughness;
    }

    @Override
    public float getKnockbackResistance() {
        return knockbackResistance;
    }
}
