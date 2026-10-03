package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BeehiveBlock.class)
public abstract class BeehiveBlockMixin {
    // Use vanilla smoke handling: skip both nearby-bee anger and emergency release on harvest.
    // Bee nests share this block implementation; resin is the registry material, not resin_brick.
    @Redirect(method = "useItemOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/CampfireBlock;isSmokeyPos(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)Z"))
    private boolean trimeffects$amberActsLikeSmoke(Level level, BlockPos pos, ItemStack stack, BlockState state, Level methodLevel,
                                                   BlockPos methodPos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player instanceof ServerPlayer serverPlayer) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.amberSafeHoneyHarvest && MaterialTrimBonuses.hasAmberAtLeast(serverPlayer, 4)) return true;
        }
        return CampfireBlock.isSmokeyPos(level, pos);
    }

    @Redirect(method="useItemOn",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/stats/Stat;)V"))
    private void trimworks$collectedHoney(Player player,net.minecraft.stats.Stat<?> stat) {
        player.awardStat(stat);
        if(stat.getValue()==net.minecraft.world.item.Items.GLASS_BOTTLE&&player instanceof ServerPlayer server
                &&com.faefluffkrist.trimworks.advancement.TrimAdvancements.activeMaterial(server,"resin"))
            com.faefluffkrist.trimworks.advancement.TrimAdvancements.addProgress(server,"resin_honey",1);
    }
}
