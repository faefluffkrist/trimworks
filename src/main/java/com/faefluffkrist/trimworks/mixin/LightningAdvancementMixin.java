package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@Mixin(Entity.class)
public abstract class LightningAdvancementMixin {
    @Inject(method="thunderHit",at=@At("HEAD"))
    private void trimworks$struck(ServerLevel level,LightningBolt lightning,CallbackInfo ci) {
        if((Object)this instanceof ServerPlayer player&&TrimAdvancements.activeMaterial(player,"copper"))
            TrimAdvancements.award(player,"material/copper_lightning");
    }
}
