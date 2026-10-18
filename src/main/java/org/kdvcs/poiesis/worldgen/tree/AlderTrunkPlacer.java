package org.kdvcs.poiesis.worldgen.tree;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;
import org.kdvcs.poiesis.registry.ModBlocks;
import org.kdvcs.poiesis.registry.ModWorldgenFeatures;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Tall, straight alder trunk with tiered side branches.
 *
 * <p>The branch chance and branch size both increase with height.  This keeps
 * the lower trunk readable while giving the upper crown the dense, spreading
 * silhouette expected from a mature alder.</p>
 */
public class AlderTrunkPlacer extends TrunkPlacer {

    public static final Codec<AlderTrunkPlacer> CODEC = RecordCodecBuilder.create(instance ->
            trunkPlacerParts(instance).apply(instance, AlderTrunkPlacer::new));

    /**
     * Root coordinates extracted from root.nbt.  The origin is the uppermost
     * root block that touches the trunk; negative Y values descend into the
     * soil.  Keeping this as a coordinate template makes the in-game roots
     * match the hand-built reference instead of relying on a loose random
     * approximation.
     */
    private static final int[][] ALDER_ROOT_PATTERN = {
            {0, -9, -7}, {-3, -9, -6}, {2, -9, -4}, {-5, -9, -2},
            {4, -9, -2}, {-9, -9, 0}, {6, -9, 0},
            {0, -8, -7}, {-3, -8, -6}, {1, -8, -4}, {-5, -8, -2},
            {4, -8, -2}, {-7, -8, -1}, {-9, -8, 0}, {6, -8, 0},
            {4, -8, 2}, {-4, -8, 3}, {0, -8, 3}, {2, -8, 3},
            {-4, -8, 4}, {-1, -8, 6},
            {0, -7, -6}, {-3, -7, -5}, {-3, -7, -4}, {1, -7, -4},
            {3, -7, -2}, {-5, -7, -1}, {-3, -7, -1}, {-8, -7, 0},
            {-7, -7, 0}, {-1, -7, 0}, {3, -7, 0}, {6, -7, 0},
            {-4, -7, 2}, {0, -7, 2}, {4, -7, 2}, {2, -7, 3},
            {-1, -7, 5},
            {0, -6, -5}, {0, -6, -4}, {-3, -6, -3}, {-1, -6, -2},
            {2, -6, -2}, {-3, -6, -1}, {-6, -6, 0}, {-5, -6, 0},
            {-3, -6, 0}, {3, -6, 0}, {5, -6, 0}, {-4, -6, 1},
            {-1, -6, 1}, {-2, -6, 2}, {1, -6, 2}, {2, -6, 2},
            {3, -6, 2}, {-1, -6, 5},
            {-3, -5, -3}, {0, -5, -3}, {-4, -5, -2}, {-1, -5, -2},
            {1, -5, -2}, {-4, -5, 0}, {3, -5, 0}, {4, -5, 0},
            {-1, -5, 1}, {-2, -5, 2}, {2, -5, 2}, {-1, -5, 3},
            {-1, -5, 4},
            {-3, -4, -2}, {0, -4, -2}, {-3, -4, -1}, {-4, -4, 0},
            {-1, -4, 0}, {0, -4, 0}, {2, -4, 0}, {2, -4, 1},
            {-1, -4, 2},
            {0, -3, -2}, {-3, -3, 0}, {0, -3, 0}, {2, -3, 0},
            {-1, -3, 2},
            {0, -2, -1}, {-3, -2, 0}, {1, -2, 0}, {-1, -2, 1},
            {-2, -1, 0}, {-1, -1, 0}, {1, -1, 0}, {0, 0, 0}
    };

    public AlderTrunkPlacer(int baseHeight, int heightRandA, int heightRandB) {
        super(baseHeight, heightRandA, heightRandB);
    }

    @Override
    protected TrunkPlacerType<?> type() {
        return ModWorldgenFeatures.ALDER_TRUNK_PLACER.get();
    }

    @Override
    public List<FoliagePlacer.FoliageAttachment> placeTrunk(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            RandomSource random,
            int height,
            BlockPos start,
            TreeConfiguration config
    ) {
        setDirtAt(level, replacer, random, start.below(), config);
        placeRootSystem(level, replacer, start);

        List<FoliagePlacer.FoliageAttachment> foliage = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            placeLog(level, replacer, random, start.above(y), config);
        }

        // The main crown sits above the straight trunk.  Side branches are
        // added below it so that the trunk remains visually dominant.
        foliage.add(new FoliagePlacer.FoliageAttachment(start.above(height), 1, false));

        int branchCount = 0;
        int maxBranches = 6;
        for (int y = 2; y < height - 1 && branchCount < maxBranches; y++) {
            float normalizedHeight = (float) y / (float) Math.max(1, height - 1);
            // Low branches are deliberately sparse; the upper trunk carries
            // most of the branching weight.
            float chance = 0.08F + normalizedHeight * 0.78F;
            if (random.nextFloat() >= chance) {
                continue;
            }

            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int length = normalizedHeight < 0.35F
                    ? 1
                    : (normalizedHeight < 0.70F ? 1 + random.nextInt(2) : 2 + random.nextInt(2));
            int rise = normalizedHeight < 0.50F ? 0 : 1;

            BlockPos branchEnd = placeBranch(level, replacer, random, start.above(y), direction, length, rise, config);
            if (branchEnd == null) {
                continue;
            }

            int radiusOffset = normalizedHeight < 0.35F
                    ? -2
                    : (normalizedHeight < 0.70F ? -1 : 0);
            foliage.add(new FoliagePlacer.FoliageAttachment(branchEnd.above(), radiusOffset, false));
            branchCount++;
        }

        // A pair of stronger upper limbs makes the crown spread instead of
        // becoming a single round ball, while the random tier branches above
        // provide the smaller foliage clusters along the trunk.
        int upperY = Math.max(2, height - 3);
        Direction first = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Direction second = first.getClockWise();
        BlockPos firstEnd = placeBranch(level, replacer, random, start.above(upperY), first, 2, 1, config);
        BlockPos secondEnd = placeBranch(level, replacer, random, start.above(upperY - 1), second, 2, 1, config);
        if (firstEnd != null) {
            foliage.add(new FoliagePlacer.FoliageAttachment(firstEnd.above(), 0, false));
        }
        if (secondEnd != null) {
            foliage.add(new FoliagePlacer.FoliageAttachment(secondEnd.above(), 0, false));
        }

        return foliage;
    }

    /**
     * Places a broad, branching root system directly beneath the trunk.
     * Keeping it in the trunk placer means naturally spawned and sapling-grown
     * alder trees receive the same radial roots, staggered depths, and hanging
     * terminal roots.
     */
    private void placeRootSystem(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            BlockPos trunkBase
    ) {
        BlockState rootState = ModBlocks.ALDER_ROOT.get().defaultBlockState();
        BlockPos rootOrigin = trunkBase.below();

        for (int[] offset : ALDER_ROOT_PATTERN) {
            placeRootBlock(
                    level,
                    replacer,
                    rootOrigin.offset(offset[0], offset[1], offset[2]),
                    rootState
            );
        }
    }

    private boolean placeRootBlock(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            BlockPos pos,
            BlockState rootState
    ) {
        if (!Feature.isGrassOrDirt(level, pos)) {
            return false;
        }
        replacer.accept(pos, rootState);
        return true;
    }

    private BlockPos placeBranch(
            LevelSimulatedReader level,
            BiConsumer<BlockPos, BlockState> replacer,
            RandomSource random,
            BlockPos branchStart,
            Direction direction,
            int length,
            int rise,
            TreeConfiguration config
    ) {
        BlockPos.MutableBlockPos cursor = branchStart.mutable();
        BlockPos lastPlaced = null;
        for (int step = 1; step <= length; step++) {
            cursor.move(direction);
            if (rise > 0 && step == length) {
                cursor.move(Direction.UP, rise);
            }

            BlockPos target = cursor.immutable();
            boolean placed = placeLog(
                    level,
                    replacer,
                    random,
                    target,
                    config,
                    state -> state.trySetValue(RotatedPillarBlock.AXIS, direction.getAxis())
            );
            if (!placed) {
                break;
            }
            lastPlaced = target;
        }
        return lastPlaced;
    }
}
