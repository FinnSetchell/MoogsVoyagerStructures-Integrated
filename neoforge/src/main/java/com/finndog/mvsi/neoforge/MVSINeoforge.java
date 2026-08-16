package com.finndog.mvsi.neoforge;

import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.modinit.MVSIProcessors;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(MVSICommon.MODID)
public class MVSINeoforge {

    public MVSINeoforge(IEventBus modEventBus, ModContainer modContainer) {
        // NeoForge freezes the vanilla registries before the mod constructor runs, so register
        // during the mod-bus RegisterEvent (when the structure_processor registry is writable).
        modEventBus.addListener((RegisterEvent event) ->
                event.register(Registries.STRUCTURE_PROCESSOR, helper ->
                        MVSIProcessors.bootstrap(helper::register)));
        NeoForge.EVENT_BUS.addListener(MVSINeoforge::onServerStarting);
    }

    private static void onServerStarting(ServerAboutToStartEvent event) {
        MVSICommon.onServerStarting(event.getServer());
    }
}
