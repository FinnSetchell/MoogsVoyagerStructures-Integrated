package com.finndog.mvsi.neoforge;

import com.finndog.mvsi.platform.IPlatformHelper;
import net.neoforged.fml.ModList;

public class PlatformHelperImpl implements IPlatformHelper {

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
