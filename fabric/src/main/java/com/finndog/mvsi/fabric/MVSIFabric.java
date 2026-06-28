package com.finndog.mvsi.fabric;

import com.finndog.mvsi.MVSICommon;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public class MVSIFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MVSICommon.init();
        ServerLifecycleEvents.SERVER_STARTING.register(MVSICommon::onServerStarting);
    }
}
