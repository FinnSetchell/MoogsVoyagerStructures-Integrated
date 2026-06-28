package com.finndog.mvsi.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * One {@code mvsi_pool_additions} file: a target pool id plus the elements to inject into it.
 */
public record PoolAdditionsFile(ResourceLocation targetPool, List<PoolAddition> additions) {

    public static final Codec<PoolAdditionsFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("target_pool").forGetter(PoolAdditionsFile::targetPool),
            PoolAddition.CODEC.listOf().fieldOf("additions").forGetter(PoolAdditionsFile::additions)
    ).apply(instance, PoolAdditionsFile::new));
}
