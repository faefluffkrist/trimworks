package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.config.BuiltInBonusesConfig;
import com.faefluffkrist.trimworks.gameplay.BuiltInTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerAttackMixin {
    @Inject(method = "attack", at = @At("TAIL"))
    private void trimeffects$afterAttack(Entity target, CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        BuiltInTrimBonuses.markProvoked(target, player);
        if (!BuiltInTrimBonuses.enabled()) return;
        BuiltInTrimBonuses.applyWardSpectralMark(player, target);
        BuiltInBonusesConfig cfg = BuiltInTrimBonuses.config();
        int boltPieces = BuiltInTrimBonuses.pieces(player, "minecraft:bolt");
        int boltLevel = boltPieces >= 4 ? 2 : boltPieces >= 2 ? 1 : 0;
        if (!cfg.boltMeleeKnockback || boltLevel <= 0 || !(target instanceof LivingEntity living)) return;

        double x = player.getX() - living.getX();
        double z = player.getZ() - living.getZ();
        living.knockback(boltPieces >= 4 ? cfg.boltKnockbackFourPieces : cfg.boltKnockbackTwoPieces, x, z, player.damageSources().playerAttack(player), 0.0F, false);
    }
}
