package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.config.BuiltInBonusesConfig;
import com.faefluffkrist.trimworks.gameplay.BuiltInTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin {
    @Inject(method = "onHitEntity", at = @At("HEAD"), cancellable = true)
    private void trimeffects$deflectBoltArrow(EntityHitResult hit, CallbackInfo ci) {
        if (!BuiltInTrimBonuses.enabled()) return;
        AbstractArrow arrow = (AbstractArrow) (Object) this;
        if (arrow.getOwner() instanceof ServerPlayer attacker && hit.getEntity() instanceof net.minecraft.world.entity.Mob) {
            BuiltInTrimBonuses.applyWardSpectralMark(attacker, hit.getEntity());
        }
        if (!(hit.getEntity() instanceof ServerPlayer player)) return;
        BuiltInBonusesConfig cfg = BuiltInTrimBonuses.config();
        if (!cfg.boltProjectileDeflection || !BuiltInTrimBonuses.fullSet(player, "minecraft:bolt")) return;
        if (player.getRandom().nextDouble() >= Math.max(0.0D, Math.min(1.0D, cfg.boltProjectileDeflectionChance))) return;

        Vec3 velocity = arrow.getDeltaMovement();
        arrow.setDeltaMovement(velocity.scale(-1.0D));
        arrow.setOwner(player);
        ci.cancel();
    }
}
