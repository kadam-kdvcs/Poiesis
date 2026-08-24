package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.block.entity.FluidDistributorBlockEntity;
import org.kdvcs.poiesis.multiblock.AdsorptionColumnStructure;

public final class FluidDistributorBlock extends BaseEntityBlock {
    public static final BooleanProperty FORMED = BooleanProperty.create("formed");

    public FluidDistributorBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.0F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(FORMED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FORMED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidDistributorBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(
            Level level,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack stack
    ) {
        super.setPlacedBy(level, pos, state, placer, stack);
        AdsorptionColumnStructure.scheduleValidation(level, pos);
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean moving
    ) {
        AdsorptionColumnStructure.scheduleValidation(level, pos);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        BlockPos controllerPos = AdsorptionColumnStructure.findController(level, pos);
        if (controllerPos == null
                || !level.getBlockState(controllerPos)
                .getValue(AdsorptionColumnControllerBlock.FORMED)) {
            return InteractionResult.PASS;
        }

        AdsorptionColumnControllerBlockEntity controller =
                AdsorptionColumnStructure.findControllerEntity(level, pos);
        if (!level.isClientSide
                && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                && controller != null
                && controller.isFormed()) {
            NetworkHooks.openScreen(serverPlayer, controller, controller.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState nextState,
            boolean moving
    ) {
        if (!state.is(nextState.getBlock())) {
            AdsorptionColumnStructure.invalidateFromPart(level, pos);
            super.onRemove(state, level, pos, nextState, moving);
        }
    }
}
