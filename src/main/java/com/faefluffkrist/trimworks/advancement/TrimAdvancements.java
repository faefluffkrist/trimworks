package com.faefluffkrist.trimworks.advancement;

import com.faefluffkrist.trimworks.gameplay.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatFormatter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.item.*;
import java.util.*;

/** Advancements are server-authoritative; cumulative progress uses Minecraft's saved player statistics. */
public final class TrimAdvancements {
    public static final List<String> PATTERNS = List.of("bolt","coast","dune","eye","flow","host","raiser","rib","sentry","shaper","silence","snout","spire","tide","vex","ward","wayfinder","wild");
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET};
    private static final Map<String,Integer> GOALS = Map.ofEntries(
        Map.entry("amethyst_upgrades",50),Map.entry("emerald_trades",50),Map.entry("gold_barters",50),Map.entry("lapis_levels",20),Map.entry("resin_honey",5),Map.entry("redstone_distance",100000),
        Map.entry("bolt_trials",5),Map.entry("flow_trials",5),Map.entry("tide_guardians",50),Map.entry("spire_shulkers",10),Map.entry("shaper_diamonds",5),Map.entry("wayfinder_fall",20000));
    // Registry values use identity: retain the statistics created with the registered IDs.
    // Parsing an equal Identifier later does not supply the registered value instance.
    private static final Map<String,Stat<Identifier>> PROGRESS_STATS = new HashMap<>();
    private static final Map<LivingEntity,Mark> SPECTRAL = new WeakHashMap<>();
    private static final Map<Mob,Map<Identifier,EffectMark>> EFFECTS = new WeakHashMap<>();
    private static final Map<Piglin,UUID> BARTER_OWNERS = new WeakHashMap<>();
    private static final Map<ServerPlayer,Double> DISTANCE_REMAINDERS = new WeakHashMap<>();
    private static final Set<String> PATTERN_GOALS = Set.of("bolt_trials","flow_trials","tide_guardians","spire_shulkers","shaper_diamonds","wayfinder_fall");
    private static final Map<ServerPlayer,Double> FALL_REMAINDERS = new WeakHashMap<>();
    private static final Map<ServerPlayer,WardWatch> WARD_WATCHES = new WeakHashMap<>();
    private record WardWatch(UUID warden,int ticks) {}
    private static String goalPath(String name) { return (PATTERN_GOALS.contains(name)?"pattern/":"material/")+name; }
    private record Mark(UUID player, long firstTick, long expiry) {}
    private record EffectMark(int amplifier, long expiry) {}
    private TrimAdvancements() {}

    public static void register() {
        for (String name : GOALS.keySet().stream().sorted().toList()) {
            Identifier id = Identifier.parse("trimworks:"+name);
            Registry.register(BuiltInRegistries.CUSTOM_STAT,id,id);
            PROGRESS_STATS.put(name,Stats.CUSTOM.get(id,(name.equals("redstone_distance")||name.equals("wayfinder_fall"))?StatFormatter.DISTANCE:StatFormatter.DEFAULT));
        }
    }
    public static void award(ServerPlayer player,String path) { award(player,path,"done"); }
    private static void award(ServerPlayer player,String path,String criterion) {
        var advancement=player.level().getServer().getAdvancements().get(Identifier.parse("trimworks:"+path));
        if(advancement!=null)player.getAdvancements().award(advancement,criterion);
    }
    private static boolean hasTemplateDiscovery(ServerPlayer player) {
        for(String pattern:PATTERNS) {
            var advancement=player.level().getServer().getAdvancements().get(Identifier.parse("trimworks:templates/"+pattern));
            if(advancement!=null&&player.getAdvancements().getOrStartProgress(advancement).isDone())return true;
        }
        return false;
    }
    public static void addProgress(ServerPlayer player,String name,int amount) {
        if(amount<=0)return;
        int goal=GOALS.get(name);
        var stat=PROGRESS_STATS.get(name);
        int current=player.getStats().getValue(stat);
        int increment=AdvancementProgressRules.increment(current,amount,goal);
        if(increment>0)player.awardStat(stat,increment);
        if(player.getStats().getValue(stat)>=goal)award(player,goalPath(name));
    }
    public static boolean fullPattern(ServerPlayer player,String pattern) {
        return BuiltInTrimBonuses.fullSet(player,"minecraft:"+pattern);
    }
    public static boolean activeMaterial(ServerPlayer player,String material) {
        var cfg=MaterialTrimBonuses.config();
        boolean mechanic=switch(material) {
            case "amethyst" -> cfg.amethystEnchantingBoost;
            case "copper" -> cfg.copperLightningResistance;
            case "diamond" -> cfg.diamondArmorToughness;
            case "emerald" -> cfg.emeraldVillagerDiscount && cfg.emeraldReputation != 0;
            case "gold" -> cfg.goldPiglinNeutrality;
            case "lapis" -> cfg.lapisExperienceBoost && cfg.lapisExperiencePercent != 0;
            case "netherite" -> cfg.netheriteFireproofPiece;
            case "quartz" -> cfg.quartzGhastNeutrality;
            case "resin" -> cfg.amberSafeHoneyHarvest;
            case "redstone" -> cfg.redstoneMovementSpeed && cfg.redstoneSpeedPercent != 0;
            default -> false;
        };
        return cfg.enabled&&mechanic&&MaterialTrimBonuses.hasAtLeast(player,"minecraft:"+material,4);
    }
    public static void tick(ServerPlayer player) {
        watchWarden(player);
        if(player.tickCount%20!=0 || !player.isAlive())return;
        var inventory=player.getInventory();
        boolean trimmed=false;
        for(int slot=0;slot<inventory.getContainerSize();slot++) {
            ItemStack item=inventory.getItem(slot);
            String id=BuiltInRegistries.ITEM.getKey(item.getItem()).toString();
            for(String pattern:PATTERNS)if(id.equals("minecraft:"+pattern+"_armor_trim_smithing_template")) {
                award(player,"root");award(player,"templates/"+pattern);
                award(player,"templates/all",pattern);
            }
            if(isTrimmedArmor(item))trimmed=true;
            if(id.equals("minecraft:dragon_egg")&&fullPattern(player,"eye"))award(player,"pattern/eye_dragon");
        }
        if(trimmed) {
            award(player,"materials");
            if(MobTrimCompatibility.installed()){award(player,"naturally_trimmed/root");award(player,"naturally_trimmed/find_armor");}
        }
        // Section headers unlock with the first earned template, then remain available.
        if(hasTemplateDiscovery(player)) {
            award(player,"templates/section");
            award(player,"full_sets");award(player,"materials");
            if(MobTrimCompatibility.installed())award(player,"naturally_trimmed/root");
        }
        for(String pattern:PATTERNS)if(fullPattern(player,pattern)){award(player,"full_sets");award(player,"sets/"+pattern);}
        visit(player,"dune","desert_pyramid","dune_temple");
        visit(player,"rib","fortress","rib_fortress");
        visit(player,"snout","bastion_remnant","snout_bastion");
        visit(player,"coast","shipwreck","coast_shipwreck");
        visit(player,"coast","shipwreck_beached","coast_shipwreck");
        visit(player,"coast","buried_treasure","coast_treasure");
        visit(player,"eye","end_city","eye_city");
        if((Math.floorMod(player.level().getOverworldClockTime(),24000)>=13000&&Math.floorMod(player.level().getOverworldClockTime(),24000)<23000))visit(player,"host","trail_ruins","host_ruins");
        visit(player,"vex","mansion","vex_mansion");
        visit(player,"wild","jungle_pyramid","wild_temple");
        if(activeMaterial(player,"quartz")&&nearFossil(player))award(player,"material/quartz_fossil");
        if(fullPattern(player,"sentry")) {
            var raid=player.level().getRaidAt(player.blockPosition());
            if(raid!=null&&raid.isActive()&&!raid.isStopped())award(player,"pattern/sentry_raid");
        }
        boolean encounters=fullPattern(player,"eye")||fullPattern(player,"rib")||fullPattern(player,"sentry")||fullPattern(player,"silence")||fullPattern(player,"ward");
        if(encounters)for(Entity entity:player.level().getEntities(player,player.getBoundingBox().inflate(128),e->e instanceof LivingEntity&&e.isAlive())) {
            String type=BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            double distance=player.distanceToSqr(entity);
            if(!player.hasLineOfSight(entity))continue;
            if(type.equals("minecraft:ender_dragon")&&fullPattern(player,"eye"))award(player,"pattern/eye_dragon");
            if(type.equals("minecraft:wither")&&distance<=4096&&fullPattern(player,"rib"))award(player,"pattern/rib_wither");
            if(distance>1024)continue;
            if((type.equals("minecraft:pillager")||type.equals("minecraft:ravager"))&&fullPattern(player,"sentry"))award(player,"pattern/sentry_encounter");
            if(type.equals("minecraft:warden")) {
                if(fullPattern(player,"silence"))award(player,"pattern/silence_warden");

            }
        }
        for(var goal:GOALS.entrySet())if(player.getStats().getValue(PROGRESS_STATS.get(goal.getKey()))>=goal.getValue())award(player,goalPath(goal.getKey()));
    }
    private static void watchWarden(ServerPlayer player) {
        if(!player.isAlive()||!fullPattern(player,"ward")){WARD_WATCHES.remove(player);return;}
        var previous=WARD_WATCHES.get(player);
        var candidates=player.level().getEntitiesOfClass(net.minecraft.world.entity.monster.warden.Warden.class,player.getBoundingBox().inflate(32),w->w.isAlive()&&player.distanceToSqr(w)<=1024);
        var warden=candidates.stream().filter(w->previous!=null&&w.getUUID().equals(previous.warden())).findFirst()
                .orElseGet(()->candidates.stream().filter(player::hasLineOfSight).findFirst().orElse(null));
        if(warden==null||warden.getTarget()!=null||warden.getAngerLevel()==net.minecraft.world.entity.monster.warden.AngerLevel.ANGRY
                ||((com.faefluffkrist.trimworks.mixin.WardenAngerAccessor)warden).trimworks$activeAnger()>100) {
            WARD_WATCHES.remove(player);return;
        }
        int ticks=previous!=null&&previous.warden().equals(warden.getUUID())?previous.ticks()+1:1;
        WARD_WATCHES.put(player,new WardWatch(warden.getUUID(),ticks));
        if(ticks>=1200)award(player,"pattern/ward_warden");
    }
    public static void trialCompleted(ServerPlayer player,boolean ominous) {
        if(!player.isAlive())return;
        if(fullPattern(player,"bolt"))addProgress(player,"bolt_trials",1);
        if(ominous&&fullPattern(player,"flow"))addProgress(player,"flow_trials",1);
    }
    public static void fell(ServerPlayer player,double dy,boolean grounded) {
        if(!fullPattern(player,"wayfinder")||player.isFallFlying()||player.isInWater()||player.isPassenger()||player.getAbilities().flying) {
            FALL_REMAINDERS.remove(player);return;
        }
        if(dy>=0)return;
        var distance=AdvancementProgressRules.distance(0,dy,0,FALL_REMAINDERS.getOrDefault(player,0.0));
        FALL_REMAINDERS.put(player,distance.remainder());
        addProgress(player,"wayfinder_fall",distance.centimeters());
    }
    public static void died(ServerPlayer player,net.minecraft.world.damagesource.DamageSource source) {
        WARD_WATCHES.remove(player);
        var killer=source.getEntity();
        if(killer!=null&&fullPattern(player,"snout")&&inside(player,"bastion_remnant")) {
            String type=BuiltInRegistries.ENTITY_TYPE.getKey(killer.getType()).toString();
            if(type.equals("minecraft:piglin_brute")||type.equals("minecraft:hoglin"))award(player,"pattern/snout_death");
        }
    }
    public static void huskHit(ServerPlayer player,net.minecraft.world.damagesource.DamageSource source) {
        if(source.getEntity() instanceof net.minecraft.world.entity.monster.zombie.Husk&&fullPattern(player,"dune")
                &&player.hasEffect(net.minecraft.world.effect.MobEffects.HUNGER))award(player,"pattern/dune_hunger");
    }
    public static void tripwire(ServerPlayer player,net.minecraft.core.BlockPos pos) {
        if(fullPattern(player,"wild")&&insideAt(player,"jungle_pyramid",pos))award(player,"pattern/wild_tripwire");
    }
    private static final Map<net.minecraft.world.RandomizableContainer,UUID> OPENING_LOOT = new WeakHashMap<>();
    public static void lootOpening(ServerPlayer player,net.minecraft.world.RandomizableContainer container) {
        var table=container.getLootTable();
        if(table!=null&&table.identifier().getNamespace().equals("minecraft")&&table.identifier().getPath().startsWith("chests/"))
            OPENING_LOOT.put(container,player.getUUID());
    }
    public static void lootOpened(ServerPlayer player,net.minecraft.world.RandomizableContainer container) {
        UUID opener=OPENING_LOOT.remove(container);
        if(opener!=null&&opener.equals(player.getUUID())&&container.getLootTable()==null)foundLoot(player,container);
    }
    public static void foundLoot(ServerPlayer player,net.minecraft.world.RandomizableContainer container) {
        if(fullPattern(player,"shaper")&&player.level().structureManager().getStructureWithPieceAt(container.getBlockPos(),structure->true).isValid()) {
            int diamonds=0;
            for(int i=0;i<container.getContainerSize();i++)if(container.getItem(i).is(Items.DIAMOND))diamonds+=container.getItem(i).getCount();
            addProgress(player,"shaper_diamonds",diamonds);
        }
    }
    private static void visit(ServerPlayer player,String pattern,String structure,String milestone) {
        if(fullPattern(player,pattern)&&inside(player,structure))award(player,"pattern/"+milestone);
    }
    private static boolean nearFossil(ServerPlayer player) {
        // Fossils can be buried or too small to stand inside. Match nearby fossil bones,
        // then verify their generated structure, rather than accepting arbitrary bone blocks.
        var pos=player.blockPosition();
        for(int x=-4;x<=4;x++)for(int y=-4;y<=4;y++)for(int z=-4;z<=4;z++) {
            var bone=pos.offset(x,y,z);
            if(player.level().getBlockState(bone).is(net.minecraft.world.level.block.Blocks.BONE_BLOCK)
                    &&insideAt(player,"nether_fossil",bone))return true;
        }
        return false;
    }
    private static boolean inside(ServerPlayer player,String structure) {
        return insideAt(player,structure,player.blockPosition());
    }
    private static boolean insideAt(ServerPlayer player,String structure,net.minecraft.core.BlockPos pos) {
        var registry=player.level().registryAccess().lookup(Registries.STRUCTURE);
        if(registry.isEmpty())return false;
        var holder=registry.get().get(Identifier.parse("minecraft:"+structure));
        return holder.isPresent()&&player.level().structureManager().getStructureWithPieceAt(pos,holder.get().value()).isValid();
    }
    public static boolean isTrimmedArmor(ItemStack stack) {
        var equipped=stack.get(DataComponents.EQUIPPABLE);
        return stack.get(DataComponents.TRIM)!=null&&equipped!=null&&Arrays.asList(ARMOR).contains(equipped.slot());
    }
    public static void duplicated(ServerPlayer player,net.minecraft.world.inventory.CraftingContainer input,ItemStack output) {
        Identifier id=BuiltInRegistries.ITEM.getKey(output.getItem());
        if(!id.getPath().endsWith("_armor_trim_smithing_template"))return;
        // Taking a recipe output that also consumed this template identifies duplication.
        for(int i=0;i<input.getContainerSize();i++)if(input.getItem(i).is(output.getItem())) {award(player,"templates/duplicate");return;}
    }
    public static void smithed(ServerPlayer player,ItemStack output) {
        var trim=output.get(DataComponents.TRIM);if(trim==null||!isTrimmedArmor(output))return;
        var cfg=MaterialTrimBonuses.config();if(!cfg.enabled)return;
        String id=BuiltInRegistries.ITEM.getKey(output.getItem()).toString();
        if(trim.material().is(Identifier.parse("minecraft:diamond"))&&cfg.diamondArmorToughness) {
            if(id.startsWith("minecraft:leather_"))award(player,"material/diamond_leather");
            if(id.startsWith("minecraft:netherite_"))award(player,"material/diamond_netherite");
        }
        if(trim.material().is(Identifier.parse("minecraft:netherite"))&&cfg.netheriteFireproofPiece&&!id.startsWith("minecraft:netherite_"))award(player,"material/netherite_trim");
    }
    public static void traded(ServerPlayer player,ItemStack output,net.minecraft.world.item.trading.Merchant merchant) {
        if(activeMaterial(player,"emerald"))addProgress(player,"emerald_trades",1);
        if(MobTrimCompatibility.installed()&&merchant instanceof net.minecraft.world.entity.npc.villager.Villager&&isTrimmedArmor(output)) {
            award(player,"naturally_trimmed/root");award(player,"naturally_trimmed/trade_armor");
        }
    }
    public static void rememberBarter(Piglin piglin,ServerPlayer player) {
        if(piglin.isAdult())BARTER_OWNERS.put(piglin,player.getUUID());
    }
    public static void clearBarter(Piglin piglin) { BARTER_OWNERS.remove(piglin); }
    public static void completedBarter(Piglin piglin) {
        UUID owner=BARTER_OWNERS.remove(piglin);
        if(owner==null||!(piglin.level() instanceof net.minecraft.server.level.ServerLevel level))return;
        var player=level.getServer().getPlayerList().getPlayer(owner);
        if(player!=null&&player.level()==level&&activeMaterial(player,"gold"))addProgress(player,"gold_barters",1);
    }
    public static void moved(ServerPlayer player,double dx,double dy,double dz) {
        if(!activeMaterial(player,"redstone")) {DISTANCE_REMAINDERS.remove(player);return;}
        var distance=AdvancementProgressRules.distance(dx,dy,dz,DISTANCE_REMAINDERS.getOrDefault(player,0.0));
        DISTANCE_REMAINDERS.put(player,distance.remainder());
        addProgress(player,"redstone_distance",distance.centimeters());
    }
    public static boolean applyEffect(LivingEntity wearer,MobEffectInstance effect) {
        boolean applied=wearer.addEffect(effect);
        if(applied&&wearer instanceof Mob mob&&MobTrimCompatibility.installed()) {
            Identifier id=BuiltInRegistries.MOB_EFFECT.getKey(effect.getEffect().value());
            EFFECTS.computeIfAbsent(mob,k->new HashMap<>()).put(id,new EffectMark(effect.getAmplifier(),wearer.level().getGameTime()+effect.getDuration()));
        }
        return applied;
    }
    public static void spectralMark(ServerPlayer player,LivingEntity victim,int ticks) {
        long now=player.level().getGameTime();var old=SPECTRAL.get(victim);
        long first=old!=null&&old.player.equals(player.getUUID())&&old.expiry>now?old.firstTick:now;
        SPECTRAL.put(victim,new Mark(player.getUUID(),first,now+Math.max(1,ticks)));
    }
    public static void killed(ServerPlayer player,LivingEntity victim) {
        String type=BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        if(type.equals("minecraft:evoker")&&fullPattern(player,"vex"))award(player,"pattern/vex_evoker");
        if(type.equals("minecraft:warden")&&fullPattern(player,"ward")&&insideAt(player,"ancient_city",victim.blockPosition()))award(player,"pattern/ward_slay");
        if(type.equals("minecraft:breeze")&&fullPattern(player,"bolt"))award(player,"pattern/bolt_breeze");
        if(type.equals("minecraft:guardian")&&fullPattern(player,"tide"))addProgress(player,"tide_guardians",1);
        if(type.equals("minecraft:elder_guardian")&&fullPattern(player,"tide"))award(player,"pattern/tide_elder");
        if(type.equals("minecraft:shulker")&&fullPattern(player,"spire"))addProgress(player,"spire_shulkers",1);
        if(Set.of("minecraft:ender_dragon","minecraft:wither","minecraft:elder_guardian","minecraft:warden").contains(type)&&fullPattern(player,"spire"))award(player,"pattern/spire_boss");
        var mark=SPECTRAL.remove(victim);long now=player.level().getGameTime();
        var glow=BuiltInRegistries.MOB_EFFECT.get(Identifier.parse("minecraft:glowing"));
        if(victim instanceof Mob&&fullPattern(player,"silence")&&mark!=null&&AdvancementProgressRules.distantMark(mark.player.equals(player.getUUID()),mark.firstTick,mark.expiry,now,
                glow.isPresent()&&victim.hasEffect(glow.get()),player.distanceToSqr(victim)))award(player,"pattern/silence_distant_kill");
        if(!MobTrimCompatibility.installed()||!(victim instanceof Enemy))return;
        boolean trimmed=Arrays.stream(ARMOR).anyMatch(slot->isTrimmedArmor(victim.getItemBySlot(slot)));
        if(trimmed){award(player,"naturally_trimmed/root");award(player,"naturally_trimmed/slay_trimmed");}
        boolean active=false;
        var effects=victim instanceof Mob mob?EFFECTS.remove(mob):null;
        if(effects!=null)for(var entry:effects.entrySet()) {
            var holder=BuiltInRegistries.MOB_EFFECT.get(entry.getKey());
            if(holder.isEmpty()||entry.getValue().expiry<=now)continue;
            var current=victim.getEffect(holder.get());
            if(current!=null&&current.getAmplifier()==entry.getValue().amplifier&&current.getDuration()>0)active=true;
        }
        for(var attribute:BuiltInRegistries.ATTRIBUTE) {
            var holder=BuiltInRegistries.ATTRIBUTE.get(BuiltInRegistries.ATTRIBUTE.getKey(attribute));if(holder.isEmpty())continue;
            var instance=victim.getAttribute(holder.get());
            if(instance!=null&&instance.getModifiers().stream().anyMatch(m->m.id().getNamespace().equals("trimworks")&&m.amount()!=0))active=true;
        }
        if(active && !com.faefluffkrist.trimworks.gameplay.SentryTheftReports.isTheftBuffed(victim)) {
            award(player,"naturally_trimmed/root");award(player,"naturally_trimmed/slay_empowered");
        }
    }
}
