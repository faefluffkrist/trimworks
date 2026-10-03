package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.piglin.PiglinAi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PiglinAi.class)
public abstract class PiglinAiMixin {
    @Inject(method = "isWearingSafeArmor", at = @At("HEAD"), cancellable = true)
    private static void trimeffects$goldTrimCountsAsSafeArmor(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (entity != null) {
            LivingEntity player = entity;
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.goldPiglinNeutrality && MaterialTrimBonuses.hasAtLeast(player, "minecraft:gold", 4)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Inject(method="mobInteract",at=@At("RETURN"))
    private static void trimworks$paidPiglin(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.monster.piglin.Piglin piglin,
        net.minecraft.world.entity.player.Player player,net.minecraft.world.InteractionHand hand,
        CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        if(player instanceof ServerPlayer server&&cir.getReturnValue().consumesAction()&&piglin.getOffhandItem().is(net.minecraft.world.item.Items.GOLD_INGOT))
            com.faefluffkrist.trimworks.advancement.TrimAdvancements.rememberBarter(piglin,server);
    }
    @Inject(method="pickUpItem",at=@At("HEAD"))
    private static void trimworks$thrownGold(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.monster.piglin.Piglin piglin,
        net.minecraft.world.entity.item.ItemEntity item,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if(item.getItem().is(net.minecraft.world.item.Items.GOLD_INGOT)) {
            com.faefluffkrist.trimworks.advancement.TrimAdvancements.clearBarter(piglin);
            if(item.getOwner() instanceof ServerPlayer server)
                com.faefluffkrist.trimworks.advancement.TrimAdvancements.rememberBarter(piglin,server);
        }
    }
    @Inject(method="stopHoldingOffHandItem",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/monster/piglin/PiglinAi;getBarterResponseItems(Lnet/minecraft/world/entity/monster/piglin/Piglin;)Ljava/util/List;"))
    private static void trimworks$completedBarter(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.monster.piglin.Piglin piglin,
        boolean barter,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        com.faefluffkrist.trimworks.advancement.TrimAdvancements.completedBarter(piglin);
    }
    @Inject(method="cancelAdmiring",at=@At("HEAD"))
    private static void trimworks$canceledPayment(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.monster.piglin.Piglin piglin,
        org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        com.faefluffkrist.trimworks.advancement.TrimAdvancements.clearBarter(piglin);
    }
    @Inject(method="stopHoldingOffHandItem",at=@At("HEAD"))
    private static void trimworks$interruptedPayment(net.minecraft.server.level.ServerLevel level,net.minecraft.world.entity.monster.piglin.Piglin piglin,
        boolean barter,org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if(!barter)com.faefluffkrist.trimworks.advancement.TrimAdvancements.clearBarter(piglin);
    }
}
