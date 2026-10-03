package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Husk.class)
public abstract class HuskAdvancementMixin {
    @Inject(method="doHurtTarget",at=@At("RETURN"))
    private void trimworks$hunger(ServerLevel level,Entity target,CallbackInfoReturnable<Boolean> cir) {
        if(cir.getReturnValue()&&target instanceof ServerPlayer player) {
            Husk self=(Husk)(Object)this;
            TrimAdvancements.huskHit(player,self.damageSources().mobAttack(self));
        }
    }
}
