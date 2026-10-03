package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
@Mixin(TripWireBlock.class)
public abstract class TripwireAdvancementMixin {
    @Inject(method="entityInside",at=@At("RETURN"))
    private void trimworks$trigger(BlockState state,Level level,BlockPos pos,Entity entity,InsideBlockEffectApplier effects,boolean flag,CallbackInfo ci) {
        if(entity instanceof ServerPlayer player&&!state.getValue(TripWireBlock.POWERED)
                &&level.getBlockState(pos).is((TripWireBlock)(Object)this)
                &&level.getBlockState(pos).getValue(TripWireBlock.POWERED))TrimAdvancements.tripwire(player,pos);
    }
}
