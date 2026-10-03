package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void trimeffects$lapisExperienceBonus(Player player, int amount) {
        if (player instanceof ServerPlayer serverPlayer) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.lapisExperienceBoost && MaterialTrimBonuses.hasAtLeast(serverPlayer, "minecraft:lapis", 4)) {
                double exactBonus = amount * 0.05D;
                int bonus = (int)Math.floor(exactBonus);
                if (player.getRandom().nextDouble() < exactBonus - bonus) bonus++;
                amount += bonus;
            }
        }
        player.giveExperiencePoints(amount);
    }
}
