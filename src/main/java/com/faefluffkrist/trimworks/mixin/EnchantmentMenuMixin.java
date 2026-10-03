package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Unique private Player trimeffects$player;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void trimeffects$capturePlayer(int syncId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.trimeffects$player = inventory.player;
    }

    @ModifyArg(method = "lambda$slotsChanged$0", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getEnchantmentCost(Lnet/minecraft/util/RandomSource;IILnet/minecraft/world/item/ItemStack;)I"), index = 2)
    private int trimeffects$amethystEnchantingStep(int bookshelfPower) {
        if (trimeffects$player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            var cfg = MaterialTrimBonuses.config();
            if (cfg.enabled && cfg.amethystEnchantingBoost && MaterialTrimBonuses.hasAtLeast(serverPlayer, "minecraft:amethyst", 4)) {
                return bookshelfPower + 1;
            }
        }
        return bookshelfPower;
    }
}
