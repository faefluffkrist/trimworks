package com.faefluffkrist.trimworks.config;

import java.util.LinkedHashMap;
import java.util.Map;

public final class TrimEffectsConfig {
    public int configVersion = 11;
    public MobBonusesConfig mobBonuses = new MobBonusesConfig();
    /** Runtime server detection supplied in the synchronization payload, not a user setting. */
    public Boolean detectedNaturallyTrimmed;
    public BuiltInBonusesConfig builtInBonuses = new BuiltInBonusesConfig();
    public MaterialBonusesConfig materialBonuses = new MaterialBonusesConfig();
    public Map<String, TrimDefinition> trims = new LinkedHashMap<>();

    public static TrimEffectsConfig defaults() {
        TrimEffectsConfig c = new TrimEffectsConfig();
        c.add("minecraft:bolt", "minecraft:dolphins_grace", 0,0,0,0);
        c.add("minecraft:coast", "minecraft:water_breathing", 0,0,0,0);
        c.add("minecraft:dune", "minecraft:speed", 0,1,1,1);
        c.add("minecraft:eye", "minecraft:speed", 0,1,1,1);
        c.add("minecraft:flow", "minecraft:jump_boost", 0,1,1,2);
        c.add("minecraft:host", "minecraft:glowing", 0,1,1,1);
        c.add("minecraft:raiser", "minecraft:saturation", 0,1,1,1);
        c.add("minecraft:rib", "minecraft:haste", 0,1,1,2);
        c.add("minecraft:sentry", "minecraft:resistance", 0,1,1,2);
        c.add("minecraft:shaper", "minecraft:luck", 0,1,1,2);
        c.add("minecraft:silence", "minecraft:health_boost", 0,1,1,2);
        c.add("minecraft:snout", "minecraft:fire_resistance", 0,1,1,1);
        c.add("minecraft:spire", "minecraft:strength", 0,1,1,2);
        c.add("minecraft:tide", "minecraft:water_breathing", 0,1,1,1);
        c.add("minecraft:vex", "minecraft:invisibility", 0,1,1,1);
        c.add("minecraft:ward", "minecraft:absorption", 0,0,0,0);
        c.add("minecraft:wayfinder", "minecraft:slow_falling", 0,1,1,1);
        c.add("minecraft:wild", "minecraft:speed", 0,1,1,1);
        return c;
    }

    private void add(String trim, String effect, int one, int two, int three, int four) {
        trims.put(trim, new TrimDefinition(new ConfiguredEffect(effect, one, two, three, four)));
    }
}
