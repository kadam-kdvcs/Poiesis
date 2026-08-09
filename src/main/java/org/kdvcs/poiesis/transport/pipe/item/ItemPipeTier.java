package org.kdvcs.poiesis.transport.pipe.item;

public enum ItemPipeTier {
    BASIC(8),
    ADVANCED(32),
    ELITE(64);

    private final int itemsPerTick;

    ItemPipeTier(int itemsPerTick) {
        this.itemsPerTick = itemsPerTick;
    }

    public int itemsPerTick() {
        return itemsPerTick;
    }
}
