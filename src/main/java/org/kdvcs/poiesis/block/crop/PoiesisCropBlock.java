package org.kdvcs.poiesis.block.crop;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.registries.ForgeRegistries;

public final class PoiesisCropBlock extends CropBlock {

    public static final int MAX_AGE = 5;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_5;

    /** Base speed plus a capped bonus for nearby crops in the same field. */
    private static final float BASE_GROWTH_MULTIPLIER = 2.25F;
    private static final float NEIGHBOR_GROWTH_BONUS = 0.50F;
    private static final float MAX_GROUP_GROWTH_BONUS = 4.0F;
    private static final int GROUP_RADIUS = 2;

    private final ResourceLocation seedId;
    private final ResourceLocation productId;

    public PoiesisCropBlock(
            Properties properties,
            ResourceLocation seedId,
            ResourceLocation productId
    ) {
        super(properties);
        this.seedId = seedId;
        this.productId = productId;
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
                        * growthMultiplier(level, pos);

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

    private float growthMultiplier(Level level, BlockPos pos) {
        int neighbors = 0;
        for (int x = -GROUP_RADIUS; x <= GROUP_RADIUS; x++) {
            for (int z = -GROUP_RADIUS; z <= GROUP_RADIUS; z++) {
                if ((x != 0 || z != 0)
                        && level.getBlockState(pos.offset(x, 0, z)).is(this)) {
                    neighbors++;
                }
            }
        }
        return BASE_GROWTH_MULTIPLIER + Math.min(
                MAX_GROUP_GROWTH_BONUS,
                neighbors * NEIGHBOR_GROWTH_BONUS
        );
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
        if (!isMaxAge(state)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(
                Enchantments.BLOCK_FORTUNE,
                player.getItemInHand(hand)
        );
        int productCount = 1 + level.random.nextInt(fortuneLevel * 2 + 1);
        giveOrDrop(level, pos, player, stack(productId, productCount));

        // Match the mature-crop loot table: right-click harvesting also gives
        // 1-3 seeds, with Fortune adding a uniform bonus.
        int seedCount = 1 + level.random.nextInt(3)
                + level.random.nextInt(fortuneLevel + 1);
        giveOrDrop(level, pos, player, stack(seedId, seedCount));
        level.setBlock(pos, getStateForAge(0), 2);
        return InteractionResult.CONSUME;
    }

    private static ItemStack stack(ResourceLocation itemId, int count) {
        Item item = ForgeRegistries.ITEMS.getValue(itemId);
        if (item == null) {
            throw new IllegalStateException("Missing crop item: " + itemId);
        }
        return new ItemStack(item, count);
    }

    private static void giveOrDrop(
            Level level,
            BlockPos pos,
            Player player,
            ItemStack stack
    ) {
        player.addItem(stack);
        if (!stack.isEmpty()) {
            Block.popResource(level, pos, stack);
        }
    }

    @Override
    protected void createBlockStateDefinition(
            StateDefinition.Builder<Block, BlockState> builder
    ) {
        builder.add(AGE);
    }
}
