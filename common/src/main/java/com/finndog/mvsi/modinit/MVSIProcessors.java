package com.finndog.mvsi.modinit;

import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.processors.ModCompatReplaceProcessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

import java.util.function.BiConsumer;

/**
 * Holds MVSI's structure processor types and registers them through a loader-supplied registrar.
 *
 * <p>The types are plain constants — constructing them performs NO registry write — so this class
 * can be touched at any time without mutating a (possibly frozen) registry. Each loader calls
 * {@link #bootstrap} at the point where the {@code minecraft:worldgen/structure_processor} registry
 * is actually writable: directly during Fabric mod init, and via the mod-bus {@code RegisterEvent}
 * on NeoForge/Forge — where the vanilla {@code BuiltInRegistries} are frozen before the mod
 * constructor runs, so a direct {@code Registry.register} there throws "Registry is already frozen".
 */
public final class MVSIProcessors {

    public static final ResourceLocation MOD_COMPAT_REPLACE_ID =
            ResourceLocation.fromNamespaceAndPath(MVSICommon.MODID, "mod_compat_replace");

    public static final StructureProcessorType<ModCompatReplaceProcessor> MOD_COMPAT_REPLACE =
            () -> ModCompatReplaceProcessor.CODEC;

    private MVSIProcessors() {}

    /** Registers every MVSI processor type through the supplied registrar (loader-correct timing). */
    public static void bootstrap(BiConsumer<ResourceLocation, StructureProcessorType<?>> registrar) {
        registrar.accept(MOD_COMPAT_REPLACE_ID, MOD_COMPAT_REPLACE);
    }
}
