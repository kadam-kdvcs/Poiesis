package org.kdvcs.poiesis.event;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;
import org.kdvcs.poiesis.registry.ModItems;
import org.kdvcs.poiesis.transport.network.energy.EnergyNetworkManager;
import org.kdvcs.poiesis.transport.network.fluid.FluidNetworkManager;
import org.kdvcs.poiesis.transport.network.item.ItemNetworkManager;

@Mod.EventBusSubscriber(modid = Poiesis.MOD_ID)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleEarlyGrinding(event);
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        DamageSource source = event.getSource();
        if (!source.is(DamageTypeTags.IS_FIRE)) {
            return;
        }

        LivingEntity entity = event.getEntity();
        int pieces = 0;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.is(ModItems.HEAT_RESISTANT_STEEL_HELMET.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_BOOTS.get())) {
                pieces++;
            }
        }

        if (pieces > 0) {
            event.setAmount(event.getAmount() * (1.0F - 0.23F * pieces));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.isOnFire() || entity.getRemainingFireTicks() <= 0) {
            return;
        }

        int pieces = heatResistantSteelPieces(entity);
        if (pieces > 0) {
            int extraDecay = pieces == 4 ? 2 : 1;
            entity.setRemainingFireTicks(
                    Math.max(0, entity.getRemainingFireTicks() - extraDecay)
            );
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END
                || event.player.level().isClientSide
                || !event.player.isInLava()
                || !wearsCompleteHeatResistantSteelSet(event.player)) {
            return;
        }

        // Lava movement normally builds exhaustion every tick. Reduce it by
        // roughly 60% while the complete insulated suit is worn.
        var food = event.player.getFoodData();
        food.setExhaustion(Math.max(0.0F, food.getExhaustionLevel() - 0.006F));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel level)) {
            return;
        }
        EnergyNetworkManager.get(level).tickAll(level);
        FluidNetworkManager.get(level).tickAll(level);
        ItemNetworkManager.get(level).tickAll(level);
    }

    @SubscribeEvent
    public static void onBlockToolModification(
            BlockEvent.BlockToolModificationEvent event
    ) {
        if (event.getToolAction() != ToolActions.AXE_STRIP) {
            return;
        }

        BlockState state = event.getState();
        BlockState stripped = null;
        if (state.is(ModBlocks.ALDER_LOG.get())) {
            stripped = ModBlocks.STRIPPED_ALDER_LOG.get().defaultBlockState();
        } else if (state.is(ModBlocks.ALDER_WOOD.get())) {
            stripped = ModBlocks.STRIPPED_ALDER_WOOD.get().defaultBlockState();
        }

        if (stripped != null) {
            event.setFinalState(stripped.setValue(
                    RotatedPillarBlock.AXIS,
                    state.getValue(RotatedPillarBlock.AXIS)
            ));
        }
    }

    private static boolean wearsCompleteHeatResistantSteelSet(LivingEntity entity) {
        return entity.getItemBySlot(EquipmentSlot.HEAD)
                        .is(ModItems.HEAT_RESISTANT_STEEL_HELMET.get())
                && entity.getItemBySlot(EquipmentSlot.CHEST)
                        .is(ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get())
                && entity.getItemBySlot(EquipmentSlot.LEGS)
                        .is(ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get())
                && entity.getItemBySlot(EquipmentSlot.FEET)
                        .is(ModItems.HEAT_RESISTANT_STEEL_BOOTS.get());
    }

    private static int heatResistantSteelPieces(LivingEntity entity) {
        int pieces = 0;
        for (ItemStack armor : entity.getArmorSlots()) {
            if (armor.is(ModItems.HEAT_RESISTANT_STEEL_HELMET.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_CHESTPLATE.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_LEGGINGS.get())
                    || armor.is(ModItems.HEAT_RESISTANT_STEEL_BOOTS.get())) {
                pieces++;
            }
        }
        return pieces;
    }

    private static void handleEarlyGrinding(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        Player player = event.getEntity();

        if (!player.isShiftKeyDown()) {
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();

        if (!level.getBlockState(pos).is(Blocks.GRINDSTONE)) {
            return;
        }

        ItemStack input = player.getMainHandItem();
        ItemStack grindingStone = player.getOffhandItem();

        if (!grindingStone.is(ModItems.GRINDING_STONE.get())) {
            return;
        }

        ItemStack result = earlyGrindingResult(input, level);
        if (result.isEmpty()) {
            return;
        }

        input.shrink(1);

        grindingStone.hurtAndBreak(
                1,
                player,
                brokenPlayer -> brokenPlayer.broadcastBreakEvent(InteractionHand.OFF_HAND)
        );

        if (!player.getInventory().add(result)) {
            player.drop(result, false);
        }

        level.playSound(
                null,
                pos,
                SoundEvents.GRINDSTONE_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    private static ItemStack earlyGrindingResult(
            ItemStack input,
            ServerLevel level
    ) {
        if (input.is(ModBlocks.SILICA_ROCK.get().asItem())) {
            return new ItemStack(
                    ModItems.SILICA_POWDER.get(),
                    3 + level.random.nextInt(3)
            );
        }
        if (input.is(Items.IRON_INGOT)) {
            return new ItemStack(ModItems.IRON_DUST.get());
        }
        if (input.is(ModItems.ALBICHROME_INGOT.get())) {
            return new ItemStack(ModItems.ALBICHROME_DUST.get());
        }
        return ItemStack.EMPTY;
    }
}
