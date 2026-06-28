package com.finndog.mvsi;

import com.finndog.mvsi.merger.PoolAdditionsMerger;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MVSICommon {
    public static final String MODID = "mvsintegrated";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
    }

    /**
     * Runs the pool-additions merger. Each loader calls this from its server-starting event,
     * after the worldgen registries are built and before structure placement begins.
     */
    public static void onServerStarting(MinecraftServer server) {
        PoolAdditionsMerger.inject(server);
    }
}
