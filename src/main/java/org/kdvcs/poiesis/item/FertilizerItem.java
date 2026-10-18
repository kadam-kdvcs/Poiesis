package org.kdvcs.poiesis.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;

/**
 * Base item for fertilizers that can accelerate bonemealable blocks.
 * Subclasses can override the success chance and particle count as their
 * fertilizer quality differs.
 */
public class FertilizerItem extends Item {
    public FertilizerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();

        if (!(level.getBlockState(pos).getBlock() instanceof BonemealableBlock growable)
                || !growable.isValidBonemealTarget(level, pos, level.getBlockState(pos),
                level.isClientSide)) {
            return super.useOn(context);
        }

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            if (growable.isBonemealSuccess(level, level.random, pos, level.getBlockState(pos))) {
                growable.performBonemeal(serverLevel, level.random, pos, level.getBlockState(pos));
            }
            level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS,
                    1.0F, 1.0F);
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                    particleCount(), 0.3D, 0.3D, 0.3D, 0.05D
            );
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected int particleCount() {
        return 10;
    }
}
