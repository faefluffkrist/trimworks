package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawner;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerState;
import org.spongepowered.asm.mixin.Unique;
@Mixin(TrialSpawner.class)
public abstract class TrialCompletionAdvancementMixin {
    @Unique private boolean trimworks$wasActive;
    @Inject(method="tickServer",at=@At("HEAD"))
    private void trimworks$before(ServerLevel level,BlockPos pos,boolean ominous,CallbackInfo ci) {
        trimworks$wasActive=((TrialSpawner)(Object)this).getState()==TrialSpawnerState.ACTIVE;
    }
    @Inject(method="tickServer",at=@At("RETURN"))
    private void trimworks$completed(ServerLevel level,BlockPos pos,boolean ominous,CallbackInfo ci) {
        var self=(TrialSpawner)(Object)this;
        if(!trimworks$wasActive||self.getState()!=TrialSpawnerState.WAITING_FOR_REWARD_EJECTION)return;
        for(var id:((TrialParticipantsAccessor)self.getStateData()).trimworks$participants()) {
            var player=level.getServer().getPlayerList().getPlayer(id);
            if(player!=null&&player.level()==level&&player.distanceToSqr(pos.getX()+0.5,pos.getY()+0.5,pos.getZ()+0.5)<=4096)
                TrimAdvancements.trialCompleted(player,self.isOminous());
        }
    }
}
