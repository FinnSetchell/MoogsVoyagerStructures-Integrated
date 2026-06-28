package com.finndog.mvsi.neoforge;

import com.finndog.mvsi.MVSICommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(MVSICommon.MODID)
public class MVSINeoforge {

    public MVSINeoforge(IEventBus modEventBus, ModContainer modContainer) {
        MVSICommon.init();
        NeoForge.EVENT_BUS.addListener(MVSINeoforge::onServerStarting);
    }

    private static void onServerStarting(ServerAboutToStartEvent event) {
        MVSICommon.onServerStarting(event.getServer());
    }
}
