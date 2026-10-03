package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(EnderDragon.class)
public abstract class DragonAdvancementMixin {
    @Inject(method="tickDeath",at=@At("HEAD"))
    private void trimworks$dragonDeath(CallbackInfo ci) {
        EnderDragon self=(EnderDragon)(Object)this;
        if(self.dragonDeathTime==0&&self.getKillCredit() instanceof ServerPlayer player&&TrimAdvancements.fullPattern(player,"spire"))
            TrimAdvancements.award(player,"pattern/spire_boss");
    }
}
