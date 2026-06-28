package com.finndog.mvsi.neoforge;

import com.finndog.mvsi.MVSICommon;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(MVSICommon.MODID)
public class MVSINeoforge {

    public MVSINeoforge(IEventBus modEventBus, ModContainer modContainer) {
        MVSICommon.init();
    }
}
