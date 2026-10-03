package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.config.BuiltInBonusesConfig;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BuiltInTrimBonuses {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final Set<Mob> TRACKED_MOBS = java.util.Collections.newSetFromMap(new java.util.WeakHashMap<>());
    private static final Set<String> PROVOKED = new HashSet<>();
    private static final TagKey<Block> DUNE_SPEED_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse("trimworks:dune_speed_blocks"));
    private static final TagKey<Block> WILD_SPEED_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse("trimworks:wild_speed_blocks"));
    private static final TagKey<Block> EYE_SPEED_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse("trimworks:eye_speed_blocks"));
    private static final TagKey<Block> SILENCE_SPEED_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse("trimworks:silence_speed_blocks"));
    private static final TagKey<Block> WARD_SPEED_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse("trimworks:ward_speed_blocks"));
    private static final Identifier WARD_SWIFT_SNEAK_ID = Identifier.parse("trimworks:ward_swift_sneak");

    private BuiltInTrimBonuses() {}

    public static BuiltInBonusesConfig config() {
        return TrimEffectsConfigManager.getServerConfig().builtInBonuses;
    }

    public static boolean enabled() {
        return config() != null && config().enabled;
    }

    public static int pieces(LivingEntity player, String trimId) {
        if (!MobTrimCompatibility.allows(player, trimId, 1)) return 0;
        int count = 0;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = player.getItemBySlot(slot);
            ArmorTrim trim = stack.get(DataComponents.TRIM);
            if (trim != null && trimId.equals(trim.pattern().value().assetId().toString())) count++;
        }
        return count;
    }

    public static boolean fullSet(LivingEntity player, String trimId) {
        return pieces(player, trimId) == 4;
    }

    public static void tick(MinecraftServer server) {
        for (LivingEntity player : server.getPlayerList().getPlayers()) {
            MaterialTrimBonuses.tick(player);
            ExtraTrimBonuses.tick(player);
            if (!enabled()) {
                var sneak = player.getAttribute(Attributes.SNEAKING_SPEED);
                if (sneak != null) sneak.removeModifier(WARD_SWIFT_SNEAK_ID);
            }
        }
        BuiltInBonusesConfig cfg = config();

        if (enabled()) for (ServerPlayer player : server.getPlayerList().getPlayers()) applyWearer(player);

        for (var level : server.getAllLevels()) {
            for (Entity entity : level.getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                // Every loaded mob (including modded mobs) can wear vanilla trim components.
                // Also clear our modifiers when compatibility or a category is turned off.
                var mobConfig = TrimEffectsConfigManager.getServerConfig().mobBonuses;
                boolean activeWearer = mob.isAlive() && MobTrimCompatibility.installed() && mobConfig != null && mobConfig.enabled
                        && java.util.Arrays.stream(ARMOR_SLOTS).anyMatch(slot -> mob.getItemBySlot(slot).get(DataComponents.TRIM) != null);
                if (activeWearer || TRACKED_MOBS.remove(mob)) {
                    TrimGameplay.apply(mob);
                    MaterialTrimBonuses.tick(mob);
                    ExtraTrimBonuses.tick(mob);
                    applyWearer(mob);
                    if (activeWearer) TRACKED_MOBS.add(mob);
                }
                LivingEntity player = mob.getTarget();
                if (player == null) continue;
                if (isProvoked(entity, player)) continue;

                var materialCfg = MaterialTrimBonuses.config();
                if (materialCfg.enabled && materialCfg.goldPiglinNeutrality && entity instanceof Piglin
                        && MaterialTrimBonuses.hasAtLeast(player, "minecraft:gold", 4)) {
                    mob.setTarget(null);
                } else if (materialCfg.enabled && materialCfg.quartzGhastNeutrality && entity instanceof Ghast
                        && MaterialTrimBonuses.hasAtLeast(player, "minecraft:quartz", 4)) {
                    mob.setTarget(null);
                } else if (cfg.enabled && cfg.silenceWardenNeutrality && entity instanceof Warden warden && fullSet(player, "minecraft:silence")) {
                    warden.clearAnger(player);
                    warden.setTarget(null);
                } else if (cfg.enabled && cfg.vexNeutrality && entity instanceof Vex && fullSet(player, "minecraft:vex")) {
                    mob.setTarget(null);
                } else if (cfg.enabled && cfg.sentryIllagerNeutrality && entity instanceof AbstractIllager && fullSet(player, "minecraft:sentry")) {
                    if (!(entity instanceof Raider raider) || !raider.hasActiveRaid()) mob.setTarget(null);
                } else if (cfg.enabled && cfg.snoutBruteHoglinNeutrality && fullSet(player, "minecraft:snout")
                        && (entity instanceof PiglinBrute || entity instanceof Hoglin)) {
                    mob.setTarget(null);
                }
            }
        }
    }

    public static void applyWearer(LivingEntity player) {
        BuiltInBonusesConfig cfg = config();
        if (!enabled()) {
            var sneak = player.getAttribute(Attributes.SNEAKING_SPEED);
            if (sneak != null) sneak.removeModifier(WARD_SWIFT_SNEAK_ID);
            return;
        }
            applyWardSwiftSneak(player, cfg);

            if (fullSet(player, "minecraft:ward")) {
                if (cfg.wardDarknessImmunity) {
                    BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:darkness")).ifPresent(holder -> player.removeEffect(holder));
                }
                if (cfg.wardAncientCitySpeed && player.getBlockStateOn().is(WARD_SPEED_BLOCKS)) applySpeed(player, cfg.wardSpeedLevel);
            }
            if (fullSet(player, "minecraft:silence") && cfg.silenceSculkSpeed && player.getBlockStateOn().is(SILENCE_SPEED_BLOCKS)) {
                applySpeed(player, cfg.silenceSpeedLevel);
            }
            if (cfg.ribWitherImmunity && fullSet(player, "minecraft:rib")) {
                BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:wither")).ifPresent(holder -> player.removeEffect(holder));
            }
            if (cfg.tideDolphinsGrace && fullSet(player, "minecraft:tide")) {
                BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:dolphins_grace")).ifPresent(holder ->
                        com.faefluffkrist.trimworks.advancement.TrimAdvancements.applyEffect(player, new MobEffectInstance(holder, 3, Math.max(0, cfg.tideGraceLevel - 1), false, false, true)));
            }
            if (cfg.coastConduitPower && fullSet(player, "minecraft:coast")) {
                BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:conduit_power")).ifPresent(holder ->
                        com.faefluffkrist.trimworks.advancement.TrimAdvancements.applyEffect(player, new MobEffectInstance(holder, 3, Math.max(0, cfg.coastConduitLevel - 1), false, false, true)));
            }
            if (cfg.duneTerrainSpeed && fullSet(player, "minecraft:dune") && player.getBlockStateOn().is(DUNE_SPEED_BLOCKS)) {
                applySpeed(player, cfg.duneSpeedLevel);
            }
            if (cfg.wildTerrainSpeed && fullSet(player, "minecraft:wild") && player.getBlockStateOn().is(WILD_SPEED_BLOCKS)) {
                applySpeed(player, cfg.wildSpeedLevel);
            }
            if (cfg.eyeTerrainSpeed && fullSet(player, "minecraft:eye") && player.getBlockStateOn().is(EYE_SPEED_BLOCKS)) {
                applySpeed(player, cfg.eyeSpeedLevel);
            }
    }

    private static void applyWardSwiftSneak(LivingEntity player, BuiltInBonusesConfig cfg) {
        var attribute = player.getAttribute(Attributes.SNEAKING_SPEED);
        if (attribute == null) return;
        attribute.removeModifier(WARD_SWIFT_SNEAK_ID);
        if (!cfg.wardSwiftSneak) return;

        int wardPieces = pieces(player, "minecraft:ward");
        double amount = wardPieces >= 4 ? cfg.wardSneakFourPieces : wardPieces >= 2 ? cfg.wardSneakTwoPieces : 0.0D;
        if (amount != 0.0D) {
            attribute.addTransientModifier(new AttributeModifier(WARD_SWIFT_SNEAK_ID, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void applySpeed(LivingEntity player, int level) {
        if (level <= 0) return;
        BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:speed")).ifPresent(holder ->
                com.faefluffkrist.trimworks.advancement.TrimAdvancements.applyEffect(player, new MobEffectInstance(holder, 3, Math.min(254, level - 1), false, false, true)));
    }

    public static void applyWardSpectralMark(LivingEntity player, Entity target) {
        BuiltInBonusesConfig cfg = config();
        if (!enabled() || !cfg.silenceSpectralMark || !fullSet(player, "minecraft:silence") || !(target instanceof LivingEntity living)) return;
        BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:glowing")).ifPresent(holder -> {
            boolean applied = living.addEffect(new MobEffectInstance(holder, Math.max(1, cfg.silenceMarkTicks), 0, false, false, true));
            if (applied && player instanceof ServerPlayer serverPlayer)
                com.faefluffkrist.trimworks.advancement.TrimAdvancements.spectralMark(serverPlayer,living,cfg.silenceMarkTicks);
        });
    }

    public static void markProvoked(Entity mob, LivingEntity player) {
        PROVOKED.add(key(mob.getUUID(), player.getUUID()));
    }

    public static boolean isProvoked(Entity mob, LivingEntity player) {
        return PROVOKED.contains(key(mob.getUUID(), player.getUUID()));
    }

    private static String key(UUID mob, UUID player) {
        return mob + ":" + player;
    }
}
