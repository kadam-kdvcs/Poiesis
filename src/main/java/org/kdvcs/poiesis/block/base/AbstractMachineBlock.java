package org.kdvcs.poiesis.block.base;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;

/** Common block lifecycle for single-block machines. */
public abstract class AbstractMachineBlock<T extends AbstractMachineBlockEntity<?>>
        extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    protected AbstractMachineBlock(int workingLight) {
        super(machineProperties(workingLight));
        registerDefaultState(
                stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(WORKING, false)
        );
    }

    protected static BlockBehaviour.Properties machineProperties(int workingLight) {
        int light = Math.max(0, Math.min(15, workingLight));
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .strength(4.0F, 8.0F)
                .sound(SoundType.METAL)
                .requiresCorrectToolForDrops()
                .lightLevel(state -> state.getValue(WORKING) ? light : 0);
    }

    protected abstract T createMachineBlockEntity(BlockPos pos, BlockState state);

    protected abstract BlockEntityType<T> machineBlockEntityType();

    @Override
    public final RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public final BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return createMachineBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public final BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(
                FACING,
                context.getHorizontalDirection().getOpposite()
        );
    }

    @Override
    protected final void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(FACING, WORKING);
    }

    @Override
    public final InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof AbstractMachineBlockEntity<?> machine) {
                openMachineScreen(serverPlayer, machine, pos);
            }
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }


    protected void openMachineScreen(
            ServerPlayer player,
            AbstractMachineBlockEntity<?> machine,
            BlockPos pos
    ) {
        NetworkHooks.openScreen(player, machine, pos);
    }

    @Override
    public final void onRemove(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState nextState,
            boolean moving
    ) {
        if (!state.is(nextState.getBlock())) {
            if (!level.isClientSide
                    && level.getBlockEntity(pos) instanceof AbstractMachineBlockEntity<?> machine) {
                Containers.dropContents(level, pos, machine.allDrops());
            }

            super.onRemove(state, level, pos, nextState, moving);
        }
    }

    @Nullable
    @Override
    public final <E extends BlockEntity> BlockEntityTicker<E> getTicker(
            Level level,
            BlockState state,
            BlockEntityType<E> type
    ) {
        if (level.isClientSide) {
            return createTickerHelper(
                    type,
                    machineBlockEntityType(),
                    (tickerLevel, pos, tickerState, blockEntity) -> blockEntity.tickClient()
            );
        }

        return createTickerHelper(
                type,
                machineBlockEntityType(),
                (tickerLevel, pos, tickerState, blockEntity) -> blockEntity.tickServer()
        );
    }
}
