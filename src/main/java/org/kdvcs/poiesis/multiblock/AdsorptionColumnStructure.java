package org.kdvcs.poiesis.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jetbrains.annotations.Nullable;
import org.kdvcs.poiesis.block.entity.AdsorptionColumnControllerBlockEntity;
import org.kdvcs.poiesis.registry.ModBlocks;

/** Structural rules and controller lookup for the modular adsorption column. */
public final class AdsorptionColumnStructure {
    public static final int MIN_BEDS = 1;
    public static final int MAX_BEDS = 6;

    private AdsorptionColumnStructure() {
    }

    public static ValidationResult validate(LevelReader level, BlockPos controllerPos) {
        if (!level.getBlockState(controllerPos).is(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get())) {
            return ValidationResult.invalid();
        }

        int bedCount = 0;
        while (bedCount < MAX_BEDS
                && level.getBlockState(controllerPos.above(bedCount + 1))
                .is(ModBlocks.ADSORPTION_BED.get())) {
            bedCount++;
        }

        if (bedCount < MIN_BEDS) {
            return ValidationResult.invalid();
        }

        BlockPos distributorPos = controllerPos.above(bedCount + 1);
        if (!level.getBlockState(distributorPos).is(ModBlocks.FLUID_DISTRIBUTOR.get())) {
            return ValidationResult.invalid();
        }

        return new ValidationResult(true, bedCount, distributorPos.immutable());
    }

    @Nullable
    public static BlockPos findController(LevelReader level, BlockPos partPos) {
        if (level.getBlockState(partPos).is(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get())) {
            return partPos.immutable();
        }

        BlockPos.MutableBlockPos cursor = partPos.mutable();
        for (int distance = 1; distance <= MAX_BEDS + 1; distance++) {
            cursor.move(0, -1, 0);
            if (level.getBlockState(cursor).is(ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get())) {
                return cursor.immutable();
            }
            if (!level.getBlockState(cursor).is(ModBlocks.ADSORPTION_BED.get())) {
                return null;
            }
        }

        return null;
    }

    @Nullable
    public static AdsorptionColumnControllerBlockEntity findControllerEntity(
            Level level,
            BlockPos partPos
    ) {
        BlockPos controllerPos = findController(level, partPos);
        if (controllerPos != null
                && level.getBlockEntity(controllerPos)
                instanceof AdsorptionColumnControllerBlockEntity controller) {
            return controller;
        }
        return null;
    }

    public static void scheduleValidation(Level level, BlockPos partPos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos controllerPos = findController(level, partPos);
        if (controllerPos != null) {
            serverLevel.scheduleTick(
                    controllerPos,
                    ModBlocks.ADSORPTION_COLUMN_CONTROLLER.get(),
                    1
            );
        }
    }

    public static void invalidateFromPart(Level level, BlockPos partPos) {
        if (level.isClientSide) {
            return;
        }

        AdsorptionColumnControllerBlockEntity controller =
                findControllerEntity(level, partPos);
        if (controller != null) {
            controller.applyStructureState(ValidationResult.invalid());
        }
    }

    public record ValidationResult(
            boolean formed,
            int bedCount,
            @Nullable BlockPos distributorPos
    ) {
        public static ValidationResult invalid() {
            return new ValidationResult(false, 0, null);
        }
    }
}
