package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.config.ConfiguredEffect;
import com.faefluffkrist.trimworks.config.TrimDefinition;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.HashMap;
import java.util.Map;

public final class TrimGameplay {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };

    private TrimGameplay() {}

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) apply(player);
            BuiltInTrimBonuses.tick(server);
        });
    }

    private static void apply(ServerPlayer player) {
        TrimEffectsConfigManager.ensureDiscoveredTrims(player.level().registryAccess());
        Map<String, Integer> equipped = countEquippedPatterns(player);
        for (Map.Entry<String, TrimDefinition> entry : TrimEffectsConfigManager.getServerConfig().trims.entrySet()) {
            TrimDefinition definition = entry.getValue();
            if (definition == null || !definition.enabled || definition.effects == null) continue;

            int pieces = equipped.getOrDefault(entry.getKey(), 0);
            if (pieces <= 0) continue;

            for (ConfiguredEffect configured : definition.effects) {
                if (configured == null || configured.id == null || configured.levels == null) continue;
                Integer level = configured.levels.get(Integer.toString(pieces));
                if (level == null || level <= 0) continue;

                Identifier id = Identifier.tryParse(configured.id);
                if (id == null) continue;
                var holder = BuiltInRegistries.MOB_EFFECT.get(id);
                if (holder.isEmpty()) continue;

                player.addEffect(new MobEffectInstance(holder.get(), 3, level - 1, false, false, true));
            }
        }
    }

    private static Map<String, Integer> countEquippedPatterns(ServerPlayer player) {
        Map<String, Integer> counts = new HashMap<>();
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            ArmorTrim trim = stack.get(DataComponents.TRIM);
            if (trim == null) continue;
            String pattern = trim.pattern().value().assetId().toString();
            counts.merge(pattern, 1, Integer::sum);
        }
        return counts;
    }
}
