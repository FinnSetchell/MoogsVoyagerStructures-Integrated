package com.finndog.mvsi.merger;

import com.finndog.moogs_structures.mixins.structures.StructurePoolAccessor;
import com.finndog.moogs_structures.utils.GeneralUtils;
import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.data.PoolAddition;
import com.finndog.mvsi.data.PoolAdditionsFile;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Scans every datapack for {@code mvsi_pool_additions} files and injects each qualifying element
 * into its target {@link StructureTemplatePool} at server start.
 *
 * <p>Injection mutates the resolved pool objects in {@link Registries#TEMPLATE_POOL} via MSL's
 * {@link StructurePoolAccessor} mixin, which is the same registry MSL's jigsaw assembler reads.
 *
 * <p><b>Idempotency.</b> Worldgen registries (including the template-pool registry) are rebuilt
 * from datapacks at each world load, so the base pool lists are fresh per load. As belt-and-braces
 * against a repeated fire on the same registry instance, the unmodified base lists are captured the
 * first time a pool is touched (keyed on the pool instance) and every injection rebuilds from that
 * captured base — so re-running never compounds weights.
 */
public final class PoolAdditionsMerger {

    private static final Gson GSON = new GsonBuilder().setLenient().disableHtmlEscaping().create();
    private static final String DATA_TYPE = "mvsi_pool_additions";
    private static final int FILE_SUFFIX_LENGTH = ".json".length();

    /** Per-pool-instance snapshot of the untouched base templates, so injection is rebuildable. */
    private record BasePool(List<Pair<StructurePoolElement, Integer>> rawTemplates,
                            List<StructurePoolElement> templates) {}

    private static final Map<StructureTemplatePool, BasePool> BASE_CACHE = new WeakHashMap<>();

    private PoolAdditionsMerger() {}

    public static void inject(MinecraftServer server) {
        RegistryAccess access = server.registryAccess();
        ResourceManager resourceManager = server.getResourceManager();
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, access);
        Registry<StructureTemplatePool> pools = access.registryOrThrow(Registries.TEMPLATE_POOL);

        Map<ResourceLocation, List<JsonElement>> files =
                GeneralUtils.getAllDatapacksJSONElement(resourceManager, GSON, DATA_TYPE, FILE_SUFFIX_LENGTH);

        // Pools reset to their captured base during THIS run, so multiple files targeting the same
        // pool accumulate rather than clobber each other (each pool is reset once, then appended to).
        Set<StructureTemplatePool> resetThisRun = Collections.newSetFromMap(new IdentityHashMap<>());

        int injectedCount = 0;
        for (List<JsonElement> jsonList : files.values()) {
            for (JsonElement json : jsonList) {
                PoolAdditionsFile parsed = PoolAdditionsFile.CODEC.parse(ops, json)
                        .resultOrPartial(error -> MVSICommon.LOGGER.error(
                                "(MVSI pool additions) Failed to parse pool additions file: {}", error))
                        .orElse(null);
                if (parsed == null) {
                    continue;
                }

                StructureTemplatePool pool = pools.get(parsed.targetPool());
                if (pool == null) {
                    MVSICommon.LOGGER.warn("(MVSI pool additions) Unknown target_pool {}", parsed.targetPool());
                    continue;
                }

                injectedCount += injectInto(pool, parsed, resetThisRun);
            }
        }

        if (injectedCount > 0) {
            MVSICommon.LOGGER.info("(MVSI pool additions) Injected {} element(s) into template pools.", injectedCount);
        }
    }

    private static int injectInto(StructureTemplatePool pool, PoolAdditionsFile parsed,
                                  Set<StructureTemplatePool> resetThisRun) {
        StructurePoolAccessor accessor = (StructurePoolAccessor) pool;

        // Capture the untouched base the first time we ever touch this pool instance.
        BasePool base = BASE_CACHE.computeIfAbsent(pool, p -> new BasePool(
                List.copyOf(accessor.moogs_structures_getRawTemplates()),
                List.copyOf(accessor.moogs_structures_getTemplates())));

        // The first time this run touches the pool, reset it to base (drops any prior run's injection
        // on the same instance — non-compounding). Later files in the same run append to that.
        if (resetThisRun.add(pool)) {
            accessor.moogs_structures_setRawTemplates(new ArrayList<>(base.rawTemplates()));
            accessor.moogs_structures_setTemplates(new ObjectArrayList<>(base.templates()));
        }

        List<Pair<StructurePoolElement, Integer>> rawTemplates =
                new ArrayList<>(accessor.moogs_structures_getRawTemplates());
        ObjectArrayList<StructurePoolElement> templates =
                new ObjectArrayList<>(accessor.moogs_structures_getTemplates());

        int injected = 0;
        for (PoolAddition addition : parsed.additions()) {
            if (!addition.conditionsPass()) {
                continue;
            }
            rawTemplates.add(Pair.of(addition.element(), addition.weight()));
            for (int i = 0; i < addition.weight(); i++) {
                templates.add(addition.element());
            }
            injected++;
        }

        accessor.moogs_structures_setRawTemplates(rawTemplates);
        accessor.moogs_structures_setTemplates(templates);
        return injected;
    }
}
