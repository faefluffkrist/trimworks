package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.config.BonusRule;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import java.util.List;
import java.util.Map;

/** Configurable full-set effects and attributes, alongside the original mechanics. */
public final class ExtraTrimBonuses {
    private ExtraTrimBonuses() {}
    public static void tick(net.minecraft.world.entity.LivingEntity player) {
        // Clear only our own modifiers. This also handles removed rules, changed attributes,
        // disabled categories, and unequipped armor without retaining player references.
        for (var attribute : BuiltInRegistries.ATTRIBUTE) {
            var holder = BuiltInRegistries.ATTRIBUTE.get(BuiltInRegistries.ATTRIBUTE.getKey(attribute));
            if (holder.isEmpty()) continue;
            var instance = player.getAttribute(holder.get());
            if (instance == null) continue;
            var stale = instance.getModifiers().stream()
                    .filter(m -> m.id().getNamespace().equals("trimworks") && m.id().getPath().startsWith("extra/"))
                    .map(AttributeModifier::id).toList();
            for (var id : stale) instance.removeModifier(id);
        }
        var cfg = TrimEffectsConfigManager.getServerConfig();
        if (cfg.builtInBonuses != null && cfg.builtInBonuses.enabled) apply(player, cfg.builtInBonuses.extraBonuses, false);
        if (cfg.materialBonuses != null && cfg.materialBonuses.enabled) apply(player, cfg.materialBonuses.extraBonuses, true);
    }
    private static void apply(net.minecraft.world.entity.LivingEntity player, Map<String, List<BonusRule>> groups, boolean material) {
        if (groups == null) return;
        for (var entry : groups.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) continue;
            boolean active = material ? MaterialTrimBonuses.pieces(player, entry.getKey()) >= 4
                    : BuiltInTrimBonuses.fullSet(player, entry.getKey());
            if (!active) continue;
            for (int i = 0; i < entry.getValue().size(); i++) {
                var rule = entry.getValue().get(i);
                if (rule == null || !rule.enabled || rule.id == null || !Double.isFinite(rule.amount)) continue;
                var id = Identifier.tryParse(rule.id);
                if (id == null) continue;
                if ("effect".equals(rule.type)) {
                    if (rule.amount < 1) continue;
                    BuiltInRegistries.MOB_EFFECT.get(id).ifPresent(h -> player.addEffect(
                            new MobEffectInstance(h, 3, Math.min(254, (int)rule.amount - 1), false, false, true)));
                } else if ("attribute".equals(rule.type)) {
                    var holder = BuiltInRegistries.ATTRIBUTE.get(id);
                    if (holder.isEmpty()) continue;
                    var instance = player.getAttribute(holder.get());
                    if (instance == null || rule.amount == 0) continue;
                    var trimId = Identifier.tryParse(entry.getKey());
                    if (trimId == null) continue;
                    var modifierId = Identifier.parse("trimworks:extra/" + (material ? "material/" : "pattern/")
                            + trimId.getNamespace() + "/" + trimId.getPath() + "/" + i);
                    instance.addTransientModifier(new AttributeModifier(modifierId,
                            rule.percent ? rule.amount / 100.0D : rule.amount,
                            rule.percent ? AttributeModifier.Operation.ADD_MULTIPLIED_BASE : AttributeModifier.Operation.ADD_VALUE));
                }
            }
        }
    }
}
