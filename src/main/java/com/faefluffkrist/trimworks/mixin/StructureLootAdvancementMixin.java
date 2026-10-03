package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.player.Player;
@Mixin(RandomizableContainer.class)
public interface StructureLootAdvancementMixin {
    @Inject(method="unpackLootTable",at=@At("HEAD"))
    default void trimworks$beforeLoot(Player player,CallbackInfo ci) {
        if(player instanceof ServerPlayer server)TrimAdvancements.lootOpening(server,(RandomizableContainer)(Object)this);
    }
    @Inject(method="unpackLootTable",at=@At("RETURN"))
    default void trimworks$afterLoot(Player player,CallbackInfo ci) {
        if(player instanceof ServerPlayer server)TrimAdvancements.lootOpened(server,(RandomizableContainer)(Object)this);
    }
}
