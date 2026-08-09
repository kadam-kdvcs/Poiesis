package org.kdvcs.poiesis.worldgen.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public record ReplaceTopBlockConfig(Block target, Block replace, float chance) implements FeatureConfiguration {

    public static final Codec<ReplaceTopBlockConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("target").forGetter(ReplaceTopBlockConfig::target),
            BuiltInRegistries.BLOCK.byNameCodec().fieldOf("replace").forGetter(ReplaceTopBlockConfig::replace),
            Codec.FLOAT.fieldOf("chance").forGetter(ReplaceTopBlockConfig::chance)
    ).apply(instance, ReplaceTopBlockConfig::new));
}