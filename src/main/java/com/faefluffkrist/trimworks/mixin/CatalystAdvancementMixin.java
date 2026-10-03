package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.entity.SculkCatalystBlockEntity;
@Mixin(SculkCatalystBlockEntity.CatalystListener.class)
public abstract class CatalystAdvancementMixin {
    // Vanilla invokes this only after feeding XP into the catalyst's spreader.
    @Inject(method="tryAwardItSpreadsAdvancement",at=@At("HEAD"))
    private void trimworks$spread(Level level,LivingEntity victim,CallbackInfo ci) {
        if(victim instanceof Enemy&&victim.getLastHurtByMob() instanceof ServerPlayer player&&TrimAdvancements.fullPattern(player,"silence"))
            TrimAdvancements.award(player,"pattern/silence_sculk");
    }
}
