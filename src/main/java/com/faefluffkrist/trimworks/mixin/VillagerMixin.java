package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Villager.class)
public abstract class VillagerMixin {
    @Inject(method = "getPlayerReputation", at = @At("RETURN"), cancellable = true)
    private void trimeffects$emeraldTrimCuringStyleDiscount(Player player, CallbackInfoReturnable<Integer> cir) {
        if (player instanceof ServerPlayer serverPlayer) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.emeraldVillagerDiscount && MaterialTrimBonuses.hasAtLeast(serverPlayer, "minecraft:emerald", 4)) {
                // Equivalent to one MAJOR_POSITIVE curing-style reputation event: 20 value * weight 5 = +100 reputation.
                cir.setReturnValue(cir.getReturnValue() + cfg.emeraldReputation);
            }
        }
    }
}
