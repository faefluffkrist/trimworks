package com.faefluffkrist.trimworks;

import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import com.faefluffkrist.trimworks.gameplay.TrimGameplay;
import com.faefluffkrist.trimworks.network.TrimConfigSync;
import net.fabricmc.api.ModInitializer;

public final class TrimEffectsExtension implements ModInitializer {
    @Override
    public void onInitialize() {
        TrimEffectsConfigManager.load();
        TrimConfigSync.registerCommon();
        TrimGameplay.register();
    }
}
