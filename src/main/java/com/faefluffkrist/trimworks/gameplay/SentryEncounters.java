package com.faefluffkrist.trimworks.gameplay;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.illager.Evoker;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.Set;
import java.util.HashSet;
import net.minecraft.world.phys.AABB;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.HashMap;

/** Server-side theft retaliation and rate-limited action-bar messages. */
public final class SentryEncounters {
    private static final Map<Mob, UUID> EVOKER_TARGETS = new WeakHashMap<>();
    private static final Map<Mob, Set<UUID>> THEFT_WITNESSES = new WeakHashMap<>();
    private static final Map<UUID, Warning> WARNINGS = new HashMap<>();
    private static final Component EVOKER_WARNING = Component.literal("Seems like Evoker can't be fooled..");
    private static final int COOLDOWN_TICKS = 5 * 60 * 20;
    private static final class Warning {
        int cooldown;
        int theftCooldown;
        Component message = EVOKER_WARNING;
        int refreshTicks;
    }
    private SentryEncounters() {}

    public static void clear() {
        EVOKER_TARGETS.clear();
        WARNINGS.clear();
        THEFT_WITNESSES.clear();
        SentryTheftReports.clear();
    }

    public static void tickMob(Mob mob) {
        if (!(mob instanceof Evoker)) return;
        var target = mob.getTarget();
        UUID current = target == null ? null : target.getUUID();
        UUID previous = current == null ? EVOKER_TARGETS.remove(mob) : EVOKER_TARGETS.put(mob, current);
        if (current == null || current.equals(previous)) return;
        var cfg = BuiltInTrimBonuses.config();
        if (cfg == null || !cfg.enabled || !cfg.sentryEvokerWarning
                || !(target instanceof ServerPlayer player) || !BuiltInTrimBonuses.fullSet(player, "minecraft:sentry")) return;
        Warning warning = WARNINGS.computeIfAbsent(player.getUUID(), p -> new Warning());
        if (warning.cooldown > 0) return;
        warning.cooldown = COOLDOWN_TICKS;
        // Vanilla's action bar lasts 60 ticks, fading during its final 20 ticks.
        // Refresh for 40 ticks, then let that native timer expire: roughly five seconds total.
        warning.message = EVOKER_WARNING;
        warning.refreshTicks = 40;
        player.sendOverlayMessage(EVOKER_WARNING);
    }

    public static void tickWarnings(MinecraftServer server) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Warning warning = WARNINGS.get(player.getUUID());
            if (warning == null) continue;
            if (warning.cooldown > 0) warning.cooldown--;
            if (warning.theftCooldown > 0) warning.theftCooldown--;
            if (warning.refreshTicks > 0) {
                warning.refreshTicks--;
                if (BuiltInTrimBonuses.fullSet(player, "minecraft:sentry")) player.sendOverlayMessage(warning.message);
                else warning.refreshTicks = 0;
            }
        }
    }

    public static void theftWarning(ServerPlayer player, boolean fleeing) {
        var cfg = BuiltInTrimBonuses.config();
        if (!cfg.sentryTheftWarning || !BuiltInTrimBonuses.fullSet(player, "minecraft:sentry")) return;
        Warning warning = WARNINGS.computeIfAbsent(player.getUUID(), p -> new Warning());
        if (warning.theftCooldown > 0) return;
        warning.theftCooldown = COOLDOWN_TICKS;
        warning.message = Component.literal(fleeing
                ? "They caught me, I need to assassinate them before they tell the others.."
                : "Damn it, they saw me stealing..").withStyle(net.minecraft.ChatFormatting.WHITE);
        warning.refreshTicks = 40;
        player.sendOverlayMessage(warning.message);
    }

    public static void informedOfTheft(Mob mob, ServerPlayer player) {
        THEFT_WITNESSES.computeIfAbsent(mob, m -> new HashSet<>()).add(player.getUUID());
        BuiltInTrimBonuses.markProvoked(mob, player);
    }

    /** Successful attributed damage, including lethal hits and projectiles. */
    public static void attacked(ServerLevel level, LivingEntity victim, ServerPlayer player) {
        BuiltInTrimBonuses.markProvoked(victim, player);
        if (!(victim instanceof Mob mob) || !isCombatIllager(mob)
                || player.isSpectator() || player.isCreative()) return;
        SentryTheftReports.attacked(mob, player);
        // Record and target before any subsequent AI pass can reapply Sentry neutrality.
        retaliate(mob, player);
        Set<UUID> witnesses = THEFT_WITNESSES.get(mob);
        if (witnesses != null && witnesses.contains(player.getUUID())) return;
        var cfg = BuiltInTrimBonuses.config();
        if (cfg == null || !cfg.enabled || !cfg.sentryIllagerNeutrality || !cfg.sentryGroupRetaliation) return;
        // The player must be within each ally's range; no recursive mob-to-mob alert chain.
        double x = player.getX(), y = player.getY(), z = player.getZ();
        AABB area = new AABB(x - 15, y - 6, z - 15, x + 15, y + 6, z + 15);
        for (Mob ally : level.getEntitiesOfClass(Mob.class, area)) {
            double dx = ally.getX() - x, dz = ally.getZ() - z;
            if (ally.isAlive() && dx * dx + dz * dz <= 225 && Math.abs(ally.getY() - y) <= 6
                    && isCombatIllager(ally)) retaliate(ally, player);
        }
    }

    private static boolean isCombatIllager(Mob mob) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        // Combat retaliation includes the casters excluded from Sentry disguise.
        return mob instanceof AbstractIllager || mob instanceof Vex || mob instanceof Ravager
                || id.equals("friendsandfoes:iceologer") || id.equals("friendsandfoes:illusioner")
                || id.equals("takesapillage:archer") || id.equals("takesapillage:legioner")
                || id.equals("takesapillage:skirmisher");
    }

    private static void retaliate(Mob mob, ServerPlayer player) {
        BuiltInTrimBonuses.markProvoked(mob, player);
        if (!mob.isAlive()) return;
        mob.setLastHurtByMob(player);
        mob.setTarget(player);
    }

    public static void openedContainer(ServerLevel level, ServerPlayer player, BlockPos pos) {
        var cfg = BuiltInTrimBonuses.config();
        if (cfg == null || !cfg.enabled || !cfg.sentryIllagerNeutrality || !cfg.sentryTheftProvocation
                || player.isSpectator() || player.isCreative()) return;
        double x = pos.getX() + 0.5, y = pos.getY() + 0.5, z = pos.getZ() + 0.5;
        AABB area = new AABB(x - 8, y - 4, z - 8, x + 8, y + 4, z + 8);
        java.util.List<Mob> caught = new java.util.ArrayList<>();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            double dx = mob.getX() - x, dz = mob.getZ() - z;
            if (!mob.isAlive() || dx * dx + dz * dz > 64 || Math.abs(mob.getY() - y) > 4
                    || !BuiltInTrimBonuses.isSentryIllager(mob) || !mob.hasLineOfSight(player)) continue;
            caught.add(mob);
            informedOfTheft(mob, player);
            retaliate(mob, player);
        }
        boolean fleeing = caught.size() == 1 && SentryTheftReports.start(level, caught.getFirst(), player);
        if (!caught.isEmpty() && caught.size() <= 2) theftWarning(player, fleeing);
    }
}
