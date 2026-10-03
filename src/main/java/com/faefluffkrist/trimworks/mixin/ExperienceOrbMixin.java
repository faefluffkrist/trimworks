package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ExperienceOrb.class)
public abstract class ExperienceOrbMixin {
    @Unique private static final java.util.Map<Player, Double> trimeffects$xpRemainders = new java.util.WeakHashMap<>();
    @Redirect(method = "playerTouch", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;giveExperiencePoints(I)V"))
    private void trimeffects$lapisExperienceBonus(Player player, int amount) {
        if (player instanceof ServerPlayer serverPlayer) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.lapisExperienceBoost && MaterialTrimBonuses.hasAtLeast(serverPlayer, "minecraft:lapis", 4)) {
                // Carry fractions between orbs: ten 1-XP pickups grant exactly one extra XP,
                // instead of randomly varying with orb size. Weak keys do not retain departed players.
                double exactBonus = amount * Math.max(-100.0D, cfg.lapisExperiencePercent) / 100.0D
                        + trimeffects$xpRemainders.getOrDefault(player, 0.0D);
                int bonus = (int)Math.floor(exactBonus + 1.0E-9D);
                trimeffects$xpRemainders.put(player, exactBonus - bonus);
                amount = Math.max(0, amount + bonus);
            }
        }
        player.giveExperiencePoints(amount);
    }
}
