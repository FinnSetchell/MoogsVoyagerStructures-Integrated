package com.finndog.mvsi.conditions;

import com.finndog.mvsi.MVSICommon;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

/**
 * Registry of condition type ids to their codecs. New condition types are added here.
 */
public final class MvsiConditions {

    public static final ResourceLocation MOD_LOADED =
            new ResourceLocation(MVSICommon.MODID, "mod_loaded");

    private static final Map<ResourceLocation, Codec<? extends MvsiCondition>> CODECS = new HashMap<>();

    static {
        CODECS.put(MOD_LOADED, ModLoadedCondition.CODEC);
    }

    private MvsiConditions() {}

    public static Codec<? extends MvsiCondition> codecById(ResourceLocation id) {
        return CODECS.get(id);
    }
}
