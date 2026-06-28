package com.finndog.mvsi.forge;

import com.finndog.mvsi.MVSICommon;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(MVSICommon.MODID)
public class MVSIForge {

    public MVSIForge() {
        MVSICommon.init();
        MinecraftForge.EVENT_BUS.addListener(MVSIForge::onServerStarting);
    }

    private static void onServerStarting(ServerAboutToStartEvent event) {
        MVSICommon.onServerStarting(event.getServer());
    }
}
