package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
@Mixin(Mob.class)
public abstract class HappyGhastAdvancementMixin {
    @Unique private boolean trimworks$hadHarness;
    @Unique private boolean trimworks$alreadyLeashed;
    @Inject(method="interact",at=@At("HEAD"))
    private void trimworks$beforeGhast(Player player,InteractionHand hand,Vec3 hit,CallbackInfoReturnable<InteractionResult> cir) {
        if((Object)this instanceof HappyGhast ghast){trimworks$hadHarness=ghast.isWearingBodyArmor();trimworks$alreadyLeashed=ghast.getLeashHolder()==player;}
    }
    @Inject(method="interact",at=@At("RETURN"))
    private void trimworks$afterGhast(Player player,InteractionHand hand,Vec3 hit,CallbackInfoReturnable<InteractionResult> cir) {
        if((Object)this instanceof HappyGhast ghast&&player instanceof ServerPlayer server&&cir.getReturnValue().consumesAction()
                &&TrimAdvancements.activeMaterial(server,"quartz")
                &&((!trimworks$hadHarness&&ghast.isWearingBodyArmor())||(!trimworks$alreadyLeashed&&ghast.getLeashHolder()==player)))
            TrimAdvancements.award(server,"material/quartz_ghast");
    }
}
