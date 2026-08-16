package com.finndog.mvsi.forge;

import com.finndog.mvsi.MVSICommon;
import com.finndog.mvsi.modinit.MVSIProcessors;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;

@Mod(MVSICommon.MODID)
public class MVSIForge {

    public MVSIForge() {
        // Forge freezes the vanilla registries before the mod constructor runs, so register
        // during the mod-bus RegisterEvent (when the structure_processor registry is writable).
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener((RegisterEvent event) ->
                event.register(Registries.STRUCTURE_PROCESSOR, helper ->
                        MVSIProcessors.bootstrap(helper::register)));
        MinecraftForge.EVENT_BUS.addListener(MVSIForge::onServerStarting);
    }

    private static void onServerStarting(ServerAboutToStartEvent event) {
        MVSICommon.onServerStarting(event.getServer());
    }
}
