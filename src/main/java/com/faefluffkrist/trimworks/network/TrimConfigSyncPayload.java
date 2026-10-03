package com.faefluffkrist.trimworks.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record TrimConfigSyncPayload(String json) implements CustomPacketPayload {
    public static final Type<TrimConfigSyncPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("trimworks", "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TrimConfigSyncPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            TrimConfigSyncPayload::json,
            TrimConfigSyncPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
