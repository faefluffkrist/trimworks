package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Final;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
@Mixin(ResultSlot.class)
public abstract class TemplateCraftAdvancementMixin {
    @Shadow @Final private CraftingContainer craftSlots;
    @Shadow @Final private Player player;
    @Inject(method="onTake",at=@At("HEAD"))
    private void trimworks$duplicate(Player player,ItemStack output,CallbackInfo ci) {
        if(player instanceof ServerPlayer server)TrimAdvancements.duplicated(server,craftSlots,output);
    }
    @Inject(method="onQuickCraft(Lnet/minecraft/world/item/ItemStack;I)V",at=@At("HEAD"))
    private void trimworks$shiftDuplicate(ItemStack output,int count,CallbackInfo ci) {
        if(count>0&&player instanceof ServerPlayer server)TrimAdvancements.duplicated(server,craftSlots,output);
    }
}
