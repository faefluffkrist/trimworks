package com.faefluffkrist.trimworks.mixin;

import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Entity.class)
public interface EntityGlowingAccessor {
    @Accessor("hasGlowingTag") boolean trimworks$originalGlowingTag();
}
