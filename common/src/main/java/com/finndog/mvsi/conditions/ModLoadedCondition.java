package com.finndog.mvsi.conditions;

import com.finndog.mvsi.platform.Services;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code mvsintegrated:mod_loaded} — passes when the given mod id is installed.
 */
public record ModLoadedCondition(String modId) implements MvsiCondition {

    public static final Codec<ModLoadedCondition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("modid").forGetter(ModLoadedCondition::modId)
    ).apply(instance, ModLoadedCondition::new));

    @Override
    public boolean test() {
        return Services.isModLoaded(modId);
    }

    @Override
    public ResourceLocation typeId() {
        return MvsiConditions.MOD_LOADED;
    }

    @Override
    public Codec<? extends MvsiCondition> codec() {
        return CODEC;
    }
}
