package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
/** Shift-click empties the original result before onTake; snapshot without altering vanilla arguments. */
@Mixin(ItemCombinerMenu.class)
public abstract class QuickSmithingAdvancementMixin {
    @Unique private ItemStack trimworks$smithOutput=ItemStack.EMPTY;
    @Inject(method="quickMoveStack",at=@At("HEAD"))
    private void trimworks$beforeQuickSmith(Player player,int slot,CallbackInfoReturnable<ItemStack> cir) {
        trimworks$smithOutput=ItemStack.EMPTY;
        if((Object)this instanceof SmithingMenu menu&&slot==menu.getResultSlot()
                &&BuiltInRegistries.ITEM.getKey(menu.getSlot(0).getItem().getItem()).getPath().endsWith("_armor_trim_smithing_template"))
            trimworks$smithOutput=menu.getSlot(slot).getItem().copy();
    }
    @Inject(method="quickMoveStack",at=@At("RETURN"))
    private void trimworks$afterQuickSmith(Player player,int slot,CallbackInfoReturnable<ItemStack> cir) {
        if(player instanceof ServerPlayer server&&!cir.getReturnValue().isEmpty()&&!trimworks$smithOutput.isEmpty())
            TrimAdvancements.smithed(server,trimworks$smithOutput);
        trimworks$smithOutput=ItemStack.EMPTY;
    }
}
