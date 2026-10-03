package com.faefluffkrist.trimworks.mixin;
import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.Set;
import java.util.UUID;
import net.minecraft.world.level.block.entity.trialspawner.TrialSpawnerStateData;
import org.spongepowered.asm.mixin.gen.Accessor;
@Mixin(TrialSpawnerStateData.class)
public interface TrialParticipantsAccessor {
    @Accessor("detectedPlayers") Set<UUID> trimworks$participants();
}
