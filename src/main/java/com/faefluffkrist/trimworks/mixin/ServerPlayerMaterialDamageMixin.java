package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMaterialDamageMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float trimeffects$copperTrimReducesLightning(float amount, ServerLevel level, DamageSource source) {
        ServerPlayer self = (ServerPlayer)(Object)this;
        var cfg = MaterialTrimBonuses.config();
        if (cfg.enabled && cfg.copperLightningResistance && source.is(DamageTypes.LIGHTNING_BOLT)
                && MaterialTrimBonuses.hasAtLeast(self, "minecraft:copper", 4)) return amount * (float)(1.0D - Math.min(100.0D, cfg.copperDamageResistancePercent) / 100.0D);
        return amount;
    }
}
