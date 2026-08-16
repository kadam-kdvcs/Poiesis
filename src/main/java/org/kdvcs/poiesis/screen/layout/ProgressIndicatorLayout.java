package org.kdvcs.poiesis.screen.layout;

import net.minecraft.resources.ResourceLocation;

/**
 * Shared description of a layered machine progress indicator.
 *
 * A progress indicator consists of:
 *  - one static base layer;
 *  - one foreground progress layer;
 *  - the real GUI position where both layers are drawn.
 *
 * Texture dimensions are stored explicitly so JEI never falls back to the
 * legacy 256x256 assumption for small widget textures.
 */
public record ProgressIndicatorLayout(
        TextureSpec baseTexture,
        TextureSpec progressTexture,
        int x,
        int y,
        SpriteRegion base,
        SpriteRegion progress
) {

    public ProgressIndicatorLayout {
        if (baseTexture == null
                || progressTexture == null
                || base == null
                || progress == null) {
            throw new IllegalArgumentException(
                    "Progress indicator layout cannot contain null values"
            );
        }
    }

    public int width() {
        return Math.max(
                base.width(),
                progress.width()
        );
    }

    public int height() {
        return Math.max(
                base.height(),
                progress.height()
        );
    }

    public record TextureSpec(
            ResourceLocation location,
            int width,
            int height
    ) {
        public TextureSpec {
            if (location == null || width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Progress texture must have a location and positive dimensions"
                );
            }
        }
    }

    public record SpriteRegion(
            int u,
            int v,
            int width,
            int height
    ) {
        public SpriteRegion {
            if (u < 0 || v < 0 || width <= 0 || height <= 0) {
                throw new IllegalArgumentException(
                        "Progress sprite region must have non-negative UVs and positive dimensions"
                );
            }
        }
    }
}
