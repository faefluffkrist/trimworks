package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.SentryEncounters;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.world.InteractionResult;

/** Hook the existing successful container-opening branch, including trapped chests. */
@Mixin({ChestBlock.class, BarrelBlock.class})
public abstract class SentryContainerMixin {
    @Inject(method = "useWithoutItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/piglin/PiglinAi;angerNearbyPiglins(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/player/Player;Z)V"))
    private void trimworks$nearbyTheft(BlockState state, Level level, BlockPos pos, Player player,
                                      BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer
                && player.containerMenu != player.inventoryMenu) {
            SentryEncounters.openedContainer(serverLevel, serverPlayer, pos);
        }
    }
}
