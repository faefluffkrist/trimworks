package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.config.TrimEffectsConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/** Optional interoperability through vanilla equipment data; no Naturally Trimmed classes linked. */
public final class MobTrimCompatibility {
    private MobTrimCompatibility() {}
    public static boolean installed() {
        return FabricLoader.getInstance().isModLoaded("naturally_trimmed");
    }
    public static boolean availableInMenu(TrimEffectsConfig display) {
        return TrimEffectsConfigManager.hasServerSync() && display.detectedNaturallyTrimmed != null
                ? display.detectedNaturallyTrimmed : installed();
    }
    public static boolean allows(LivingEntity wearer, String id, int category) {
        if (wearer instanceof Player) return true;
        var cfg = TrimEffectsConfigManager.getServerConfig().mobBonuses;
        return wearer instanceof Mob && installed() && cfg != null && cfg.allows(id, category);
    }
}
