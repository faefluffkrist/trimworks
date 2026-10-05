package com.faefluffkrist.trimworks.gameplay;

import com.faefluffkrist.trimworks.mixin.MobGoalSelectorAccessor;
import com.faefluffkrist.trimworks.mixin.EntityGlowingAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.*;

/** Only original, solitary pillager witnesses can carry a bounded theft report. */
public final class SentryTheftReports {
    private static final Map<Mob, Report> REPORTERS = new WeakHashMap<>();
    private static final Map<Mob, Investigation> INVESTIGATORS = new WeakHashMap<>();
    private static final Map<Mob, Long> THEFT_BUFFED = new WeakHashMap<>();
    private static final Identifier RUSH_SPEED = Identifier.parse("trimworks:theft_rush_speed");
    private static final Set<Mob> RUSHING = Collections.newSetFromMap(new WeakHashMap<>());
    private static final Set<Mob> INSTALLED = Collections.newSetFromMap(new WeakHashMap<>());
    private static final class Report {
        final UUID thief;
        final Vec3 lastSeen;
        final Set<Mob> informed = new HashSet<>();
        int ticks;
        int firstInformedTick = -1;
        int motionTick;
        double motionX, motionY, motionZ;
        boolean originalGlow;
        boolean glowActive;
        Mob destination;
        Report(ServerPlayer player, Mob witness) {
            thief = player.getUUID(); lastSeen = player.position();
            motionX = witness.getX(); motionY = witness.getY(); motionZ = witness.getZ();
        }
    }
    private static final class Investigation {
        final UUID thief;
        final Vec3 lastSeen;
        int ticks;
        boolean arrived;
        Investigation(Report report) { thief = report.thief; lastSeen = report.lastSeen; }
    }
    private SentryTheftReports() {}
    public static void clear() {
        for (var entry : REPORTERS.entrySet()) clearGlow(entry.getKey(), entry.getValue());
        for (Mob mob : new ArrayList<>(RUSHING)) endRush(mob);
        REPORTERS.clear(); INVESTIGATORS.clear(); INSTALLED.clear(); RUSHING.clear(); THEFT_BUFFED.clear();
    }

    private static boolean pillager(Mob mob) {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).toString();
        return mob instanceof Pillager || id.equals("takesapillage:archer")
                || id.equals("takesapillage:legioner") || id.equals("takesapillage:skirmisher");
    }
    private static boolean allowed() {
        var cfg = BuiltInTrimBonuses.config();
        return cfg != null && cfg.enabled && cfg.sentryIllagerNeutrality
                && cfg.sentryTheftProvocation && cfg.sentryTheftSnitching;
    }
    private static boolean anotherAngry(ServerLevel level, Mob reporter, ServerPlayer thief, Set<Mob> informed) {
        for (var entity : level.getAllEntities()) {
            if (entity instanceof Mob other && other != reporter && other.isAlive() && !informed.contains(other)
                    && (BuiltInTrimBonuses.isSentryIllager(other) || other instanceof net.minecraft.world.entity.monster.illager.Evoker
                        || other instanceof net.minecraft.world.entity.monster.Vex)
                    && (BuiltInTrimBonuses.isProvoked(other, thief) || other.getTarget() == thief)) return true;
        }
        return false;
    }
    public static boolean start(ServerLevel level, Mob witness, ServerPlayer thief) {
        if (!allowed() || !pillager(witness) || !(witness instanceof PathfinderMob)
                || INVESTIGATORS.containsKey(witness)) return false;
        Report existing = REPORTERS.get(witness);
        if (existing != null) return existing.thief.equals(thief.getUUID());
        if (anotherAngry(level, witness, thief, Set.of())) return false;
        if (recipients(level, witness, thief, Set.of()).isEmpty()) return false;
        Report report = new Report(thief, witness);
        report.originalGlow = ((EntityGlowingAccessor)witness).trimworks$originalGlowingTag();
        report.glowActive = true;
        witness.setGlowingTag(true);
        REPORTERS.put(witness, report);
        witness.setTarget(null);
        install(witness);
        return true;
    }
    private static java.util.List<Mob> recipients(ServerLevel level, Mob witness, ServerPlayer thief, Set<Mob> informed) {
        double x = witness.getX(), y = witness.getY(), z = witness.getZ();
        var candidates = level.getEntitiesOfClass(Mob.class, new AABB(x-60,y-60,z-60,x+60,y+60,z+60));
        candidates.removeIf(candidate -> candidate == witness || !candidate.isAlive()
                || !(candidate instanceof AbstractIllager) || !(candidate instanceof PathfinderMob)
                || (!BuiltInTrimBonuses.isSentryIllager(candidate) && !(candidate instanceof net.minecraft.world.entity.monster.illager.Evoker))
                || informed.contains(candidate) || BuiltInTrimBonuses.isProvoked(candidate, thief)
                || candidate.getTarget() == thief || REPORTERS.containsKey(candidate) || INVESTIGATORS.containsKey(candidate)
                || Math.abs(candidate.getX()-x) > 60 || Math.abs(candidate.getY()-y) > 60 || Math.abs(candidate.getZ()-z) > 60);
        candidates.sort(Comparator.comparingDouble(witness::distanceToSqr));
        return candidates;
    }
    public static boolean isTheftBuffed(LivingEntity entity) {
        return entity instanceof Mob mob && THEFT_BUFFED.getOrDefault(mob, Long.MIN_VALUE) > mob.level().getGameTime();
    }
    private static void clearGlow(Mob mob, Report report) {
        if (report != null && report.glowActive) {
            mob.setGlowingTag(report.originalGlow);
            report.glowActive = false;
        }
    }
    private static void resistance(Mob mob) {
        THEFT_BUFFED.put(mob, mob.level().getGameTime() + 600);
        BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:resistance")).ifPresent(holder ->
                mob.addEffect(new MobEffectInstance(holder, 600, 1, false, false, true)));
    }
    private static void rush(Mob mob) {
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null && RUSHING.add(mob)) speed.addTransientModifier(new AttributeModifier(
                RUSH_SPEED, 0.2D / 3.0D, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        resistance(mob);
    }
    private static void endRush(Mob mob) {
        var speed = mob.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) speed.removeModifier(RUSH_SPEED);
        if (RUSHING.remove(mob)) resistance(mob);
    }
    private static void install(Mob mob) {
        if (INSTALLED.add(mob)) ((MobGoalSelectorAccessor) mob).trimworks$goalSelector().addGoal(-1, new TheftGoal(mob));
    }
    public static void attacked(Mob mob, ServerPlayer player) {
        Investigation investigation = INVESTIGATORS.get(mob);
        if (investigation != null && investigation.thief.equals(player.getUUID())) {
            INVESTIGATORS.remove(mob); endRush(mob);
        }
    }
    public static boolean blocksTarget(Mob mob, LivingEntity target) {
        Report report = REPORTERS.get(mob);
        if (report != null && target.getUUID().equals(report.thief)) return true;
        Investigation investigation = INVESTIGATORS.get(mob);
        return investigation != null && target.getUUID().equals(investigation.thief)
                && (!mob.hasLineOfSight(target) || mob.distanceToSqr(target) > 225);
    }
    private static final class TheftGoal extends Goal {
        private final Mob mob;
        TheftGoal(Mob mob) { this.mob = mob; setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK)); }
        @Override public boolean canUse() { return mob.isAlive() && (REPORTERS.containsKey(mob) || INVESTIGATORS.containsKey(mob)); }
        @Override public boolean requiresUpdateEveryTick() { return true; }
        @Override public void stop() { mob.getNavigation().stop(); endRush(mob); clearGlow(mob, REPORTERS.get(mob)); }
        @Override public void tick() {
            if (!(mob.level() instanceof ServerLevel level)) return;
            Report report = REPORTERS.get(mob);
            if (report != null) report(level, report);
            else investigate(level, INVESTIGATORS.get(mob));
        }
        private void report(ServerLevel level, Report report) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(report.thief);
            if (!allowed() || player == null || player.level() != level || !player.isAlive()
                    || player.isCreative() || player.isSpectator()
                    || (++report.ticks % 10 == 1 && anotherAngry(level, mob, player, report.informed))) {
                finishReport(player); return;
            }
            if ((report.firstInformedTick < 0 && report.ticks >= 300) || (report.firstInformedTick >= 0 && report.ticks - report.firstInformedTick >= 100)) {
                finishReport(player); return;
            }
            if (report.ticks - report.motionTick >= 60) {
                boolean stuck = Math.abs(mob.getX()-report.motionX) < 0.1
                        && Math.abs(mob.getZ()-report.motionZ) < 0.1 && Math.abs(mob.getY()-report.motionY) <= 3;
                if (stuck) { finishReport(player); return; }
                report.motionTick = report.ticks;
                report.motionX = mob.getX(); report.motionY = mob.getY(); report.motionZ = mob.getZ();
            }
            if (report.ticks % 20 == 1) rush(mob);
            mob.setTarget(null);
            if (report.ticks % 10 != 1) return;
            Mob destination = report.destination;
            if (destination != null && (!destination.isAlive() || report.informed.contains(destination)
                    || BuiltInTrimBonuses.isProvoked(destination, player))) destination = null;
            if (destination == null) {
                var candidates = recipients(level, mob, player, report.informed);
                if (candidates.isEmpty() && report.firstInformedTick < 0) { finishReport(player); return; }
                for (Mob candidate : candidates) {
                    var path = mob.getNavigation().createPath(candidate, 0);
                    if (path != null && path.canReach()) { destination = candidate; break; }
                }
                report.destination = destination;
            }
            if (destination == null) {
                Vec3 away = DefaultRandomPos.getPosAway((PathfinderMob)mob, 16, 7, player.position());
                if (away != null) mob.getNavigation().moveTo(away.x, away.y, away.z, 1.0);
                return;
            }
            if (mob.distanceToSqr(destination) <= 16 && mob.hasLineOfSight(destination)) {
                SentryEncounters.informedOfTheft(destination, player);
                clearGlow(mob, report);
                report.informed.add(destination);
                if (report.firstInformedTick < 0) report.firstInformedTick = report.ticks;
                rush(destination);
                level.playSound(null, destination.getX(), destination.getY(), destination.getZ(),
                        SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(0), SoundSource.HOSTILE, 16.0F, 1.0F);
                INVESTIGATORS.put(destination, new Investigation(report));
                destination.setTarget(null);
                install(destination);
                report.destination = null;
                if (report.informed.size() == 2) finishReport(player);
            } else if (!mob.getNavigation().moveTo(destination, 1.0)) report.destination = null;
        }
        private void finishReport(ServerPlayer player) {
            Report finished = REPORTERS.remove(mob);
            clearGlow(mob, finished);
            mob.getNavigation().stop();
            if (finished != null && allowed() && player != null && player.isAlive() && player.level() == mob.level()) {
                INVESTIGATORS.put(mob, new Investigation(finished));
                mob.setTarget(null);
                mob.getNavigation().moveTo(finished.lastSeen.x, finished.lastSeen.y, finished.lastSeen.z, 1.0);
            } else {
                endRush(mob);
                if (player != null && player.isAlive() && player.level() == mob.level()) mob.setTarget(player);
            }
        }
        private void investigate(ServerLevel level, Investigation investigation) {
            if (investigation == null) return;
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(investigation.thief);
            if (!allowed() || player == null || !player.isAlive() || player.level() != level
                    || player.isCreative() || player.isSpectator() || ++investigation.ticks > 600) {
                INVESTIGATORS.remove(mob); mob.getNavigation().stop(); endRush(mob); return;
            }
            double dx = mob.getX()-investigation.lastSeen.x, dy = mob.getY()-investigation.lastSeen.y,
                    dz = mob.getZ()-investigation.lastSeen.z;
            if (!investigation.arrived && dx*dx+dy*dy+dz*dz <= 4) {
                investigation.arrived = true;
                endRush(mob);
                resistance(mob);
                mob.getNavigation().stop();
            }
            if (!investigation.arrived && investigation.ticks % 20 == 1) rush(mob);
            if (mob.hasLineOfSight(player) && mob.distanceToSqr(player) <= 225 && Math.abs(mob.getY()-player.getY()) <= 6) {
                INVESTIGATORS.remove(mob);
                endRush(mob);
                mob.setTarget(player);
                return;
            }
            mob.setTarget(null);
            if (!investigation.arrived && investigation.ticks % 20 == 1) mob.getNavigation().moveTo(
                    investigation.lastSeen.x, investigation.lastSeen.y, investigation.lastSeen.z, 1.0);
        }
    }
}
