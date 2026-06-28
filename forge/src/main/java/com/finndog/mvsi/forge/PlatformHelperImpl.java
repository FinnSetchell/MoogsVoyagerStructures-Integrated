package com.finndog.mvsi.forge;

import com.finndog.mvsi.platform.IPlatformHelper;
import net.minecraftforge.fml.ModList;

public class PlatformHelperImpl implements IPlatformHelper {

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
