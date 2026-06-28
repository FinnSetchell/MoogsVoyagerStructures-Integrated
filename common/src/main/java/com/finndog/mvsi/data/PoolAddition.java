package com.finndog.mvsi.data;

import com.finndog.mvsi.conditions.MvsiCondition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;

import java.util.List;

/**
 * One element to inject into a target template pool, with its weight and gating conditions.
 *
 * <p>{@code element} decodes via the full {@link StructurePoolElement#CODEC} dispatch (an object
 * carrying {@code element_type}), so any vanilla or MSL element type can be injected and can carry
 * its own processors / per-piece settings. Decoding therefore requires {@code RegistryOps} so the
 * element's processor holders resolve.
 */
public record PoolAddition(StructurePoolElement element, int weight, List<MvsiCondition> conditions) {

    public static final Codec<PoolAddition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            StructurePoolElement.CODEC.fieldOf("element").forGetter(PoolAddition::element),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(PoolAddition::weight),
            MvsiCondition.CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(PoolAddition::conditions)
    ).apply(instance, PoolAddition::new));

    public boolean conditionsPass() {
        return conditions.stream().allMatch(MvsiCondition::test);
    }
}
