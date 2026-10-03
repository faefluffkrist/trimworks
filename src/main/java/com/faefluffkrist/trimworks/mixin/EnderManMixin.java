package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.BuiltInTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EnderMan.class)
public abstract class EnderManMixin {
    @Inject(method = "isBeingStaredBy", at = @At("HEAD"), cancellable = true)
    private void trimeffects$eyeGazeImmunity(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (!(player instanceof ServerPlayer serverPlayer) || !BuiltInTrimBonuses.enabled()) return;
        var cfg = BuiltInTrimBonuses.config();
        if (cfg.eyeEndermanGazeImmunity && BuiltInTrimBonuses.fullSet(serverPlayer, "minecraft:eye")) {
            cir.setReturnValue(false);
        }
    }
}
