package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.gameplay.MaterialTrimBonuses;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.ArrayList;
import java.util.List;

@Mixin(EnchantmentMenu.class)
public abstract class EnchantmentMenuMixin {
    @Unique private Player trimeffects$player;
    @Unique private boolean trimeffects$purchasing;
    @Unique private int trimeffects$button;
    @Shadow @Final public int[] enchantClue;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("TAIL"))
    private void trimeffects$capturePlayer(int syncId, Inventory inventory, ContainerLevelAccess access, CallbackInfo ci) {
        trimeffects$player = inventory.player;
    }
    @Inject(method = "clickMenuButton", at = @At("HEAD"))
    private void trimeffects$beginPurchase(Player player, int button, CallbackInfoReturnable<Boolean> cir) {
        trimeffects$purchasing = true;
        trimeffects$button = button;
    }
    @Inject(method = "clickMenuButton", at = @At("RETURN"))
    private void trimeffects$endPurchase(Player player, int button, CallbackInfoReturnable<Boolean> cir) {
        trimeffects$purchasing = false;
    }
    @Inject(method = "getEnchantmentList", at = @At("RETURN"), cancellable = true)
    private void trimeffects$boostFirst(RegistryAccess registry, ItemStack stack, int slot, int cost,
                                      CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
        if (!trimeffects$purchasing || !(trimeffects$player instanceof net.minecraft.server.level.ServerPlayer player)) return;
        var cfg = MaterialTrimBonuses.config();
        if (!cfg.enabled || !cfg.amethystEnchantingBoost || !MaterialTrimBonuses.hasAtLeast(player, "minecraft:amethyst", 4)) return;
        var result = cir.getReturnValue();
        if (result == null || result.isEmpty()) return;
        trimeffects$purchasing = false; // Do not boost the menu refresh later in this purchase.
        var boosted = new ArrayList<>(result);
        int firstIndex = 0;
        // Vanilla chooses the displayed clue from the generated list, not always index 0.
        // Boost that primary enchantment so Sharpness I in the offer receives Sharpness II.
        if (trimeffects$button >= 0 && trimeffects$button < enchantClue.length) {
            var enchantments = registry.lookupOrThrow(Registries.ENCHANTMENT);
            for (int i = 0; i < boosted.size(); i++) {
                if (enchantments.getId(boosted.get(i).enchantment().value()) == enchantClue[trimeffects$button]) {
                    firstIndex = i;
                    break;
                }
            }
        }
        var first = boosted.get(firstIndex);
        // Component storage supports levels through 255, including levels above vanilla maxima.
        boosted.set(firstIndex, new EnchantmentInstance(first.enchantment(), Math.max(1, Math.min(255, first.level() + cfg.amethystLevelIncrease))));
        if(boosted.get(firstIndex).level()>first.level())
            com.faefluffkrist.trimworks.advancement.TrimAdvancements.addProgress(player,"amethyst_upgrades",1);
        cir.setReturnValue(boosted);
    }
}

