package org.kdvcs.poiesis.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.kdvcs.poiesis.block.base.AbstractMachineBlock;

/** A positional looping sound that follows a machine's working block state. */
final class MachineWorkingSound extends AbstractTickableSoundInstance {
    private final BlockPos pos;

    MachineWorkingSound(SoundEvent sound, BlockPos pos) {
        super(sound, SoundSource.BLOCKS, RandomSource.create());
        this.pos = pos.immutable();
        this.looping = true;
        this.delay = 0;
        this.volume = 0.65F;
        this.x = pos.getX() + 0.5D;
        this.y = pos.getY() + 0.5D;
        this.z = pos.getZ() + 0.5D;
    }

    void halt() {
        stop();
    }

    @Override
    public void tick() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            stop();
            return;
        }
        BlockState state = minecraft.level.getBlockState(pos);
        if (!state.hasProperty(AbstractMachineBlock.WORKING)
                || !state.getValue(AbstractMachineBlock.WORKING)) {
            stop();
        }
    }
}
