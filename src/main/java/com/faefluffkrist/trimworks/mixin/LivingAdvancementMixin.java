package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import org.spongepowered.asm.mixin.Unique;
@Mixin(LivingEntity.class)
public abstract class LivingAdvancementMixin {
    @Unique private boolean trimworks$deathRecorded;
    @Inject(method="die",at=@At("HEAD"))
    private void trimworks$kill(DamageSource source,CallbackInfo ci) {
        LivingEntity self=(LivingEntity)(Object)this;
        if(trimworks$deathRecorded||self.level().isClientSide())return;
        trimworks$deathRecorded=true;
        var credit=source.getEntity() instanceof ServerPlayer direct?direct:self.getKillCredit();
        if(credit instanceof ServerPlayer player)TrimAdvancements.killed(player,self);
    }
}
