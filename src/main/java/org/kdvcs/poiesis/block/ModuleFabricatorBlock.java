package org.kdvcs.poiesis.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.NetworkHooks;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;
import org.kdvcs.poiesis.block.entity.ModuleFabricatorBlockEntity;
import org.kdvcs.poiesis.block.entity.base.AbstractMachineBlockEntity;
import org.kdvcs.poiesis.registry.ModBlockEntities;

public final class ModuleFabricatorBlock
        extends AbstractMachineBlock<ModuleFabricatorBlockEntity> {

    public ModuleFabricatorBlock() {
        super(8);
    }

    @Override
    protected ModuleFabricatorBlockEntity createMachineBlockEntity(
            BlockPos pos,
            BlockState state
    ) {
        return new ModuleFabricatorBlockEntity(pos, state);
    }

    @Override
    protected BlockEntityType<ModuleFabricatorBlockEntity> machineBlockEntityType() {
        return ModBlockEntities.MODULE_FABRICATOR.get();
    }

    @Override
    protected void openMachineScreen(
            ServerPlayer player,
            AbstractMachineBlockEntity<?> machine,
            BlockPos pos
    ) {
        NetworkHooks.openScreen(player, machine, buffer -> {
            buffer.writeBlockPos(pos);

            if (machine instanceof ModuleFabricatorBlockEntity fabricator) {
                var selected = fabricator.runtime().process().selectedRecipeId();
                buffer.writeUtf(selected == null ? "" : selected.toString());
            } else {
                buffer.writeUtf("");
            }
        });
    }
}
