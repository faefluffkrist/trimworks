package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.monster.warden.Warden;
import org.spongepowered.asm.mixin.gen.Invoker;
@Mixin(Warden.class)
public interface WardenAngerAccessor {
    @Invoker("getActiveAnger") int trimworks$activeAnger();
}
