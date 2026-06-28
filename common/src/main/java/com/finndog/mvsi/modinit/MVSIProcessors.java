package com.finndog.mvsi.modinit;

import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.processors.ModCompatReplaceProcessor;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import java.util.function.Supplier;

/**
 * Registers MVSI's structure processor types into the static
 * {@link BuiltInRegistries#STRUCTURE_PROCESSOR} registry. {@link #init()} is driven from
 * {@link MVSICommon#init()}, which every loader entrypoint invokes, so the types are
 * registered identically on Fabric, NeoForge, and Forge.
 */
public final class MVSIProcessors {

    public static final Supplier<StructureProcessorType<ModCompatReplaceProcessor>> MOD_COMPAT_REPLACE =
            register("mod_compat_replace", () -> ModCompatReplaceProcessor.CODEC);

    private MVSIProcessors() {}

    /**
     * Touching this class triggers its static initializers, registering every type.
     */
    public static void init() {
    }

    private static <P extends net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor>
    Supplier<StructureProcessorType<P>> register(String name, StructureProcessorType<P> type) {
        StructureProcessorType<P> registered = Registry.register(
                BuiltInRegistries.STRUCTURE_PROCESSOR,
                ResourceLocation.fromNamespaceAndPath(MVSICommon.MODID, name),
                type);
        return () -> registered;
    }
}
