package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void trimeffects$goldTrimCountsAsSafeArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof ServerPlayer player) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.goldPiglinNeutrality && MaterialTrimBonuses.hasAtLeast(player, "minecraft:gold", 4)) {
                cir.setReturnValue(true);
            }
        }
    }
}
