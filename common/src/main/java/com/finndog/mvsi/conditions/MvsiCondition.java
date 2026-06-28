package com.finndog.mvsi.conditions;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;

/**
 * A single condition gating a pool addition. Conditions are evaluated at server start.
 * The {@code conditions} array on a {@link com.finndog.mvsi.data.PoolAddition} is ANDed:
 * the addition is kept only if every condition's {@link #test()} returns true.
 */
public interface MvsiCondition {

    Codec<MvsiCondition> CODEC = ResourceLocation.CODEC
            .dispatch("type", MvsiCondition::typeId, MvsiConditions::codecById);

    boolean test();

    ResourceLocation typeId();

    MapCodec<? extends MvsiCondition> codec();
}
