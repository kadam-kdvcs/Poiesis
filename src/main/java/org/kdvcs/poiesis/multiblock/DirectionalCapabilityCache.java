package org.kdvcs.poiesis.multiblock;

import net.minecraft.core.Direction;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumMap;
import java.util.function.Function;

/** Rebuildable per-face capability views for multiblock ports. */
public final class DirectionalCapabilityCache<T> {
    private final EnumMap<Direction, LazyOptional<T>> views =
            new EnumMap<>(Direction.class);

    public LazyOptional<T> get(Direction side, Function<Direction, T> factory) {
        return views.computeIfAbsent(
                side,
                direction -> LazyOptional.of(() -> factory.apply(direction))
        );
    }

    public void invalidate() {
        views.values().forEach(LazyOptional::invalidate);
        views.clear();
    }
}
