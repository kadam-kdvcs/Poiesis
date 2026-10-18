package org.kdvcs.poiesis.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.AdsorptionBedBlock;
import org.kdvcs.poiesis.block.FluidDistributorBlock;
import org.kdvcs.poiesis.registry.ModBlocks;

/** Applies the formed/working appearance of adsorption-column parts. */
public final class AdsorptionColumnVisualController {
    private AdsorptionColumnVisualController() {
    }

    public static void updateParts(
            Level level,
            BlockPos controllerPos,
            int bedCount,
            @Nullable BlockPos distributorPos,
            boolean formed,
            boolean working
    ) {
        for (int index = 1; index <= bedCount; index++) {
            BlockPos bedPos = controllerPos.above(index);
            BlockState bedState = level.getBlockState(bedPos);
            if (!bedState.is(ModBlocks.ADSORPTION_BED.get())) {
                continue;
            }
            BlockState next = bedState
                    .setValue(AdsorptionBedBlock.FORMED, formed)
                    .setValue(AdsorptionBedBlock.BOTTOM, formed && index == 1)
                    .setValue(AdsorptionBedBlock.WORKING, formed && working);
            if (!next.equals(bedState)) {
                level.setBlock(bedPos, next, 2);
            }
        }

        if (distributorPos == null) {
            return;
        }
        BlockState distributorState = level.getBlockState(distributorPos);
        if (distributorState.is(ModBlocks.FLUID_DISTRIBUTOR.get())) {
            BlockState next = distributorState.setValue(
                    FluidDistributorBlock.FORMED, formed
            );
            if (!next.equals(distributorState)) {
                level.setBlock(distributorPos, next, 2);
            }
        }
    }

    public static void emitFormationParticles(
            ServerLevel level,
            BlockPos controllerPos,
            int bedCount
    ) {
        int height = bedCount + 2;
        for (int offset = 0; offset < height; offset++) {
            double x = controllerPos.getX() + 0.5D;
            double y = controllerPos.getY() + offset + 0.5D;
            double z = controllerPos.getZ() + 0.5D;
            level.sendParticles(DustParticleOptions.REDSTONE, x - 0.58D, y, z,
                    8, 0.08D, 0.46D, 0.46D, 0.0D);
            level.sendParticles(DustParticleOptions.REDSTONE, x + 0.58D, y, z,
                    8, 0.08D, 0.46D, 0.46D, 0.0D);
            level.sendParticles(DustParticleOptions.REDSTONE, x, y, z - 0.58D,
                    8, 0.46D, 0.46D, 0.08D, 0.0D);
            level.sendParticles(DustParticleOptions.REDSTONE, x, y, z + 0.58D,
                    8, 0.46D, 0.46D, 0.08D, 0.0D);
        }
    }
}
