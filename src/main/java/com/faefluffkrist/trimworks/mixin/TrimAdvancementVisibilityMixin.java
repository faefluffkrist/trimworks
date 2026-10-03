package com.faefluffkrist.trimworks.mixin;

import com.faefluffkrist.trimworks.advancement.TrimAdvancements;
import com.faefluffkrist.trimworks.gameplay.MobTrimCompatibility;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import java.util.function.Predicate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keep the complete Trimworks tree browsable after its first template discovery. */
@Mixin(AdvancementVisibilityEvaluator.class)
public abstract class TrimAdvancementVisibilityMixin {
    @Inject(method="evaluateVisibility(Lnet/minecraft/advancements/AdvancementNode;Ljava/util/function/Predicate;Lnet/minecraft/server/advancements/AdvancementVisibilityEvaluator$Output;)V",
            at=@At("HEAD"),cancellable=true)
    private static void trimworks$visibility(AdvancementNode root,Predicate<AdvancementNode> completed,
                                            AdvancementVisibilityEvaluator.Output output,CallbackInfo ci) {
        if(!root.holder().id().toString().equals("trimworks:root"))return;
        boolean unlocked=trimworks$hasTemplate(root,completed);
        trimworks$showTree(root,unlocked,output);
        ci.cancel();
    }
    private static boolean trimworks$hasTemplate(AdvancementNode node,Predicate<AdvancementNode> completed) {
        var id=node.holder().id();
        if(id.getNamespace().equals("trimworks")&&id.getPath().startsWith("templates/")
                &&TrimAdvancements.PATTERNS.contains(id.getPath().substring("templates/".length()))&&completed.test(node))return true;
        for(var child:node.children())if(trimworks$hasTemplate(child,completed))return true;
        return false;
    }
    private static void trimworks$showTree(AdvancementNode node,boolean unlocked,AdvancementVisibilityEvaluator.Output output) {
        var id=node.holder().id();
        boolean compatible=!id.getNamespace().equals("trimworks")||!id.getPath().startsWith("naturally_trimmed/")||MobTrimCompatibility.installed();
        boolean visible=unlocked&&compatible;
        output.accept(node,visible);
        for(var child:node.children())trimworks$showTree(child,visible,output);
    }
}
