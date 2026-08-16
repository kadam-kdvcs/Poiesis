package org.kdvcs.poiesis.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.kdvcs.poiesis.Poiesis;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModCrops;
import org.kdvcs.poiesis.registry.ModItems;

@Mod.EventBusSubscriber(modid = Poiesis.MOD_ID)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        handleSilicaGrinding(event);
    }

    private static void handleSilicaGrinding(PlayerInteractEvent.RightClickBlock event) {
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

        ItemStack silicaRock = player.getMainHandItem();
        ItemStack grindingStone = player.getOffhandItem();

        if (!silicaRock.is(ModBlocks.SILICA_ROCK.get().asItem())) {
            return;
        }

        if (!grindingStone.is(ModItems.GRINDING_STONE.get())) {
            return;
        }

        silicaRock.shrink(1);

        grindingStone.hurtAndBreak(
                1,
                player,
                brokenPlayer -> brokenPlayer.broadcastBreakEvent(InteractionHand.OFF_HAND)
        );

        int outputCount = 3 + level.random.nextInt(3);

        ItemStack result = new ItemStack(
                ModItems.SILICA_POWDER.get(),
                outputCount
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
}