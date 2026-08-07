package org.kdvcs.poiesis.machine.io;

import net.minecraft.core.Direction;

public enum RelativeSide {
    FRONT,
    BACK,
    LEFT,
    RIGHT,
    TOP,
    BOTTOM;

    public static RelativeSide fromWorldSide(
            Direction worldSide,
            Direction machineFacing
    ) {
        if (worldSide == Direction.UP) {
            return TOP;
        }

        if (worldSide == Direction.DOWN) {
            return BOTTOM;
        }

        Direction horizontalFacing = machineFacing != null
                && machineFacing.getAxis().isHorizontal()
                ? machineFacing
                : Direction.NORTH;

        if (worldSide == horizontalFacing) {
            return FRONT;
        }

        if (worldSide == horizontalFacing.getOpposite()) {
            return BACK;
        }

        if (worldSide == horizontalFacing.getClockWise()) {
            return LEFT;
        }

        return RIGHT;
    }

    public Direction toWorldSide(Direction machineFacing) {
        Direction horizontalFacing = machineFacing != null
                && machineFacing.getAxis().isHorizontal()
                ? machineFacing
                : Direction.NORTH;

        return switch (this) {
            case FRONT -> horizontalFacing;
            case BACK -> horizontalFacing.getOpposite();
            case LEFT -> horizontalFacing.getClockWise();
            case RIGHT -> horizontalFacing.getCounterClockWise();
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
        };
    }
}
