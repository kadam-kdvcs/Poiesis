package org.kdvcs.poiesis.worldgen.feature;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;

public class ReplaceSurfaceTopBlockFeature extends Feature<ReplaceTopBlockConfig> {

    public ReplaceSurfaceTopBlockFeature() {
        super(ReplaceTopBlockConfig.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<ReplaceTopBlockConfig> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        BlockPos origin = context.origin();
        ReplaceTopBlockConfig config = context.config();

        int surfaceY = level.getHeight(
                Heightmap.Types.WORLD_SURFACE_WG,
                origin.getX(),
                origin.getZ()
        );

        BlockPos topPos = new BlockPos(
                origin.getX(),
                surfaceY - 1,
                origin.getZ()
        );

        if (!level.getBlockState(topPos.above()).isAir()) {
            return false;
        }

        if (!level.getBlockState(topPos).is(config.target())) {
            return false;
        }

        if (random.nextFloat() >= config.chance()) {
            return false;
        }

        level.setBlock(
                topPos,
                config.replace().defaultBlockState(),
                2
        );

        return true;
    }
}