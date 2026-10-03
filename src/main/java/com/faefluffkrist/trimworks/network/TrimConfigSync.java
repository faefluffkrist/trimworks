package com.faefluffkrist.trimworks.network;

import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class TrimConfigSync {
    private TrimConfigSync() {}

    public static void registerCommon() {
        PayloadTypeRegistry.clientboundPlay().register(TrimConfigSyncPayload.TYPE, TrimConfigSyncPayload.CODEC);

        ServerPlayConnectionEvents.JOIN.register((listener, sender, server) -> {
            TrimEffectsConfigManager.ensureDiscoveredTrims(listener.player.level().registryAccess());
            if (ServerPlayNetworking.canSend(listener, TrimConfigSyncPayload.TYPE)) {
                ServerPlayNetworking.send(listener.player, new TrimConfigSyncPayload(TrimEffectsConfigManager.toServerJson()));
            }
        });
    }
}
