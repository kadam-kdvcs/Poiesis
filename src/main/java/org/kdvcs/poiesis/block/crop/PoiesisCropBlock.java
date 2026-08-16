package org.kdvcs.poiesis.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

public final class PoiesisCropBlock extends CropBlock {

    public static final int MAX_AGE = 5;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;

    /**
     * 1.0 = 原版速度
     * 1.5 = 约 1.5 倍
     * 2.0 = 约 2 倍
     */
    private static final float GROWTH_MULTIPLIER = 1.75F;

    private final ResourceLocation seedId;

    public PoiesisCropBlock(
            Properties properties,
            ResourceLocation seedId
    ) {
        super(properties);
        this.seedId = seedId;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        ItemLike seed = ForgeRegistries.ITEMS.getValue(seedId);

        if (seed == null) {
            throw new IllegalStateException(
                    "Missing crop seed item: " + seedId
            );
        }

        return seed;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }

        int age = getAge(state);

        if (age >= getMaxAge()) {
            return;
        }

        float growthSpeed =
                getGrowthSpeed(this, level, pos)
                        * GROWTH_MULTIPLIER;

        boolean shouldGrow =
                random.nextInt(
                        (int) (25.0F / growthSpeed) + 1
                ) == 0;

        if (ForgeHooks.onCropsGrowPre(
                level,
                pos,
                state,
                shouldGrow
        )) {
            level.setBlock(
                    pos,
                    getStateForAge(age + 1),
                    2
            );

            ForgeHooks.onCropsGrowPost(
                    level,
                    pos,
                    state
            );
        }
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AGE);
    }
}