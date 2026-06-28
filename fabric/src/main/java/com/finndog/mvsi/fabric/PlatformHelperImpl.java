package com.finndog.mvsi.fabric;

import com.finndog.mvsi.platform.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class PlatformHelperImpl implements IPlatformHelper {

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
}
