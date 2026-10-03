package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;
@Mixin(SmithingMenu.class)
public abstract class SmithingAdvancementMixin {
    @Inject(method="onTake",at=@At("HEAD"))
    private void trimworks$smith(Player player,ItemStack output,CallbackInfo ci) {
        var template=((SmithingMenu)(Object)this).getSlot(0).getItem();
        if(player instanceof ServerPlayer server&&BuiltInRegistries.ITEM.getKey(template.getItem()).getPath().endsWith("_armor_trim_smithing_template"))
            TrimAdvancements.smithed(server,output);
    }
}
