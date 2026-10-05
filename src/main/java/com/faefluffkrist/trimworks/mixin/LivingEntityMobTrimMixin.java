package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.BuiltInTrimBonuses;
import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import com.faefluffkrist.trimworks.gameplay.MobTrimCompatibility;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Non-player damage/combat hooks; the original player hooks remain unchanged. */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMobTrimMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private float trimworks$copperMobResistance(float amount, ServerLevel level, DamageSource source) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof Mob) || !MobTrimCompatibility.installed()) return amount;
        var cfg = MaterialTrimBonuses.config();
        if (cfg.enabled && cfg.copperLightningResistance && source.is(DamageTypes.LIGHTNING_BOLT)
                && MaterialTrimBonuses.hasAtLeast(self, "minecraft:copper", 4)) {
            return amount * (float)(1.0D - Math.min(100.0D, cfg.copperDamageResistancePercent) / 100.0D);
        }
        return amount;
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void trimworks$mobAttackBonuses(ServerLevel level, DamageSource source, float amount,
                                           CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        // Successful player-attributed damage includes arrows, tridents and thrown potions.
        if (source.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            com.faefluffkrist.trimworks.gameplay.SentryEncounters.attacked(level, (LivingEntity)(Object)this, player);
        }
        if (!MobTrimCompatibility.installed()) return;
        LivingEntity target = (LivingEntity)(Object)this;
        // Damage-source attribution covers vanilla and modded mobs without overriding their AI.
        if (!(source.getEntity() instanceof Mob attacker)) return;
        BuiltInTrimBonuses.markProvoked(target, attacker);
        if (!BuiltInTrimBonuses.enabled()) return;
        BuiltInTrimBonuses.applyWardSpectralMark(attacker, target);
        if (source.getDirectEntity() != attacker) return; // Bolt's extra knockback is melee only.
        var cfg = BuiltInTrimBonuses.config();
        int pieces = BuiltInTrimBonuses.pieces(attacker, "minecraft:bolt");
        if (!cfg.boltMeleeKnockback || pieces < 2) return;
        target.knockback(pieces >= 4 ? cfg.boltKnockbackFourPieces : cfg.boltKnockbackTwoPieces,
                attacker.getX() - target.getX(), attacker.getZ() - target.getZ(), source, 0.0F, false);
    }
}
