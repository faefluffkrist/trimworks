package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void trimeffects$netheriteTrimSurvivesLava(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity self = (ItemEntity)(Object)this;
        var cfg = MaterialTrimBonuses.config();
        if (!cfg.enabled || !cfg.netheriteFireproofPiece || !MaterialTrimBonuses.hasMaterial(self.getItem(), "minecraft:netherite")) return;
        if (source.is(DamageTypes.LAVA) || (self.isInLava() && source.is(DamageTypes.ON_FIRE))) cir.setReturnValue(false);
    }
}
