package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.BuiltInTrimBonuses;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Prevent attack goals from acquiring protected wearers before the end-of-tick fallback. */
@Mixin(Mob.class)
public abstract class SentryTargetMixin {
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void trimworks$sentryTarget(LivingEntity target, CallbackInfo ci) {
        Mob mob = (Mob)(Object)this;
        if (!mob.level().isClientSide() && target != null && (com.faefluffkrist.trimworks.gameplay.SentryTheftReports.blocksTarget(mob, target)
                || BuiltInTrimBonuses.shouldIgnoreSentry(mob, target))) {
            mob.setTarget(null);
            ci.cancel();
        }
    }
}
