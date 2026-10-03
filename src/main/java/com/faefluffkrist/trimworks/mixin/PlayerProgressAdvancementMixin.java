package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;
@Mixin(ServerPlayer.class)
public abstract class PlayerProgressAdvancementMixin {
    @Unique private int trimworks$beforeXpLevel;
    @Inject(method="giveExperiencePoints",at=@At("HEAD"))
    private void trimworks$beforeXp(int amount,CallbackInfo ci) {
        trimworks$beforeXpLevel=((ServerPlayer)(Object)this).experienceLevel;
    }
    @Inject(method="giveExperiencePoints",at=@At("RETURN"))
    private void trimworks$afterXp(int amount,CallbackInfo ci) {
        ServerPlayer self=(ServerPlayer)(Object)this;
        if(amount>0&&TrimAdvancements.activeMaterial(self,"lapis"))
            TrimAdvancements.addProgress(self,"lapis_levels",com.faefluffkrist.trimworks.advancement.AdvancementProgressRules.earnedLevels(trimworks$beforeXpLevel,self.experienceLevel,amount));
    }
    @Inject(method="die",at=@At("HEAD"))
    private void trimworks$death(net.minecraft.world.damagesource.DamageSource source,CallbackInfo ci) {
        TrimAdvancements.died((ServerPlayer)(Object)this,source);
    }
    @Inject(method="checkFallDamage",at=@At("HEAD"))
    private void trimworks$fall(double dy,boolean grounded,net.minecraft.world.level.block.state.BlockState state,net.minecraft.core.BlockPos pos,CallbackInfo ci) {
        TrimAdvancements.fell((ServerPlayer)(Object)this,dy,grounded);
    }
    @Inject(method={"checkMovementStatistics","checkRidingStatistics"},at=@At("TAIL"))
    private void trimworks$travel(double dx,double dy,double dz,CallbackInfo ci) {
        TrimAdvancements.moved((ServerPlayer)(Object)this,dx,dy,dz);
    }
}
