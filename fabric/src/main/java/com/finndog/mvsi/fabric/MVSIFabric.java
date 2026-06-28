package com.finndog.mvsi.fabric;

import com.finndog.mvsi.MVSICommon;
import net.fabricmc.api.ModInitializer;

public class MVSIFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        MVSICommon.init();
    }
}
