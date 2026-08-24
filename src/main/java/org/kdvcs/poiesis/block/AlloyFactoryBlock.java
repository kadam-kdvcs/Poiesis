package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.AlloyFactoryBlockEntity;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class AlloyFactoryBlock extends AbstractMachineBlock<AlloyFactoryBlockEntity> {
    public AlloyFactoryBlock() {
        super(8);
    }

    @Override
    protected AlloyFactoryBlockEntity createMachineBlockEntity(BlockPos pos, BlockState state) {
        return new AlloyFactoryBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<AlloyFactoryBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.ALLOY_FACTORY.get();
    }

    @Override
    protected void openMachineScreen(
            ServerPlayer player,
            AbstractMachineBlockEntity<?> machine,
            BlockPos pos
    ) {
        NetworkHooks.openScreen(player, machine, buffer -> {
            buffer.writeBlockPos(pos);
            if (machine instanceof AlloyFactoryBlockEntity factory) {
                var selected = factory.runtime().process().selectedRecipeId();
                buffer.writeUtf(selected == null ? "" : selected.toString());
            } else {
                buffer.writeUtf("");
            }
        });
    }
}
