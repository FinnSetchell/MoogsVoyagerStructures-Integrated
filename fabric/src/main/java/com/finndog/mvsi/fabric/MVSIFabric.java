package com.finndog.mvsi.fabric;

import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.modinit.MVSIProcessors;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;

public class MVSIFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Fabric keeps the vanilla registries writable during mod init, so register directly.
        MVSIProcessors.bootstrap((id, type) ->
                Registry.register(BuiltInRegistries.STRUCTURE_PROCESSOR, id, type));
        ServerLifecycleEvents.SERVER_STARTING.register(MVSICommon::onServerStarting);
    }
}
