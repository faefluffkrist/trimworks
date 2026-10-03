package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.world.inventory.MerchantResultSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
@Mixin(MerchantResultSlot.class)
public abstract class TradeAdvancementMixin {
    @Shadow @Final private Merchant merchant;
    @Shadow @Final private net.minecraft.world.inventory.MerchantContainer slots;
    @Inject(method="onTake",at=@At(value="INVOKE",target="Lnet/minecraft/world/entity/player/Player;awardStat(Lnet/minecraft/resources/Identifier;)V",shift=At.Shift.AFTER))
    private void trimworks$completedTrade(Player player,ItemStack output,CallbackInfo ci) {
        if(player instanceof ServerPlayer server) {
            var offer=slots.getActiveOffer();
            TrimAdvancements.traded(server,offer!=null?offer.getResult():output,merchant);
        }
    }
}
