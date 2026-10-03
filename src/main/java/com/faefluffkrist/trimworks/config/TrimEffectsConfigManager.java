package com.faefluffkrist.trimworks.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public final class TrimEffectsConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger("Trimworks");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("trimworks.json");
    private static final Path LEGACY_CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("trimeffects-extension.json");
    private static TrimEffectsConfig config = TrimEffectsConfig.defaults();
    private static volatile TrimEffectsConfig syncedServerConfig;

    private TrimEffectsConfigManager() {}

    /** Authoritative local configuration. Gameplay must always use this accessor. */
    public static TrimEffectsConfig getServerConfig() {
        return config;
    }

    /** Configuration shown by client UI. A connected server's synchronized copy wins. */
    public static TrimEffectsConfig getDisplayConfig() {
        TrimEffectsConfig synced = syncedServerConfig;
        return synced != null ? synced : config;
    }

    /** Kept for compatibility with older extension code; equivalent to server/local config. */
    public static TrimEffectsConfig get() {
        return config;
    }

    public static String toServerJson() {
        return GSON.toJson(config);
    }

    public static boolean acceptServerJson(String json) {
        try {
            TrimEffectsConfig received = GSON.fromJson(json, TrimEffectsConfig.class);
            if (received == null || received.trims == null) {
                LOGGER.warn("Received an empty Trimworks configuration from the server; ignoring it.");
                return false;
            }
            syncedServerConfig = received;
            LOGGER.info("Accepted synchronized Trimworks configuration with {} trim definitions.", received.trims.size());
            return true;
        } catch (JsonParseException e) {
            LOGGER.error("Could not parse synchronized Trimworks configuration from server.", e);
            return false;
        }
    }

    public static void clearServerSync() {
        syncedServerConfig = null;
    }

    public static boolean hasServerSync() {
        return syncedServerConfig != null;
    }

    public static void load() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            if (Files.notExists(CONFIG_PATH) && Files.exists(LEGACY_CONFIG_PATH)) {
                Files.copy(LEGACY_CONFIG_PATH, CONFIG_PATH);
                LOGGER.info("Migrated legacy TrimEffects Extension config to {}", CONFIG_PATH);
            }
            if (Files.notExists(CONFIG_PATH)) {
                config = TrimEffectsConfig.defaults();
                save();
                LOGGER.info("Created default Trimworks config at {}", CONFIG_PATH);
            } else {
                try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
                    TrimEffectsConfig loaded = GSON.fromJson(reader, TrimEffectsConfig.class);
                    config = loaded != null ? loaded : TrimEffectsConfig.defaults();
                }
            }
            migrateLegacyTrimIds();
            migrateVanillaDefaultsV2();
            migrateVanillaDefaultsV3();
            migrateVanillaDefaultsV4();
            migrateBuiltInBonuses();
            migrateBuiltInBonusesV5();
            migrateWardSilenceV6();
            migrateMaterialBonusesV7();
            migrateMaterialBonusesV8();
            migrateTwoPieceMinimumV9();
            validate();
            LOGGER.info("Loaded Trimworks config with {} trim definitions.", config.trims.size());
        } catch (IOException | JsonParseException e) {
            LOGGER.error("Could not load {}. Using built-in defaults for this session.", CONFIG_PATH, e);
            config = TrimEffectsConfig.defaults();
        }
    }

    public static void save() throws IOException {
        try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
            GSON.toJson(config, writer);
        }
    }

    /** Migrates Milestone 1-5 configs from bare vanilla ids ("spire") to full ids ("minecraft:spire"). */
    private static void migrateLegacyTrimIds() {
        if (config.trims == null || config.trims.isEmpty()) return;
        Map<String, TrimDefinition> migrated = new LinkedHashMap<>();
        boolean changed = false;
        for (Map.Entry<String, TrimDefinition> entry : config.trims.entrySet()) {
            String oldId = entry.getKey();
            String newId = oldId != null && oldId.indexOf(':') < 0 ? "minecraft:" + oldId : oldId;
            if (!java.util.Objects.equals(oldId, newId)) changed = true;
            migrated.put(newId, entry.getValue());
        }
        if (changed) {
            config.trims = migrated;
            try { save(); } catch (IOException e) { LOGGER.warn("Migrated trim ids in memory but could not save the migrated config yet.", e); }
            LOGGER.info("Migrated legacy trim ids to namespaced ids.");
        }
    }

    /** Updates only untouched v1 Coast/Tide defaults. Customized definitions are preserved. */
    private static void migrateVanillaDefaultsV2() {
        if (config.configVersion >= 2 || config.trims == null) return;
        boolean changed = false;
        TrimDefinition coast = config.trims.get("minecraft:coast");
        if (matchesSingleEffect(coast, "minecraft:water_breathing", 1, 1, 1, 1)) {
            coast.effects.clear();
            coast.effects.add(new ConfiguredEffect("minecraft:water_breathing", 0, 0, 0, 0));
            changed = true;
        }
        TrimDefinition tide = config.trims.get("minecraft:tide");
        if (matchesSingleEffect(tide, "minecraft:conduit_power", 1, 1, 1, 1)) {
            tide.effects.clear();
            tide.effects.add(new ConfiguredEffect("minecraft:water_breathing", 1, 1, 1, 1));
            changed = true;
        }
        if (changed) LOGGER.info("Migrated untouched Coast/Tide defaults for built-in full-set bonuses.");
    }


    /** Updates only untouched legacy Dune/Wild defaults. Customized definitions are preserved. */
    private static void migrateVanillaDefaultsV3() {
        if (config.configVersion >= 3 || config.trims == null) return;
        boolean changed = false;
        TrimDefinition dune = config.trims.get("minecraft:dune");
        if (matchesSingleEffect(dune, "minecraft:speed", 1, 2, 2, 2)) {
            dune.effects.clear();
            dune.effects.add(new ConfiguredEffect("minecraft:speed", 0, 1, 1, 1));
            changed = true;
        }
        TrimDefinition wild = config.trims.get("minecraft:wild");
        if (matchesSingleEffect(wild, "minecraft:hero_of_the_village", 5, 5, 5, 5)) {
            wild.effects.clear();
            wild.effects.add(new ConfiguredEffect("minecraft:speed", 0, 1, 1, 1));
            changed = true;
        }
        if (changed) LOGGER.info("Migrated untouched Dune/Wild defaults for terrain full-set bonuses.");
    }

    /** Updates only untouched Bolt/Eye defaults for the revised built-in bonus identities. */
    private static void migrateVanillaDefaultsV4() {
        if (config.configVersion >= 4 || config.trims == null) return;
        boolean changed = false;
        TrimDefinition bolt = config.trims.get("minecraft:bolt");
        if (matchesSingleEffect(bolt, "minecraft:dolphins_grace", 1, 1, 1, 1)) {
            bolt.effects.clear();
            bolt.effects.add(new ConfiguredEffect("minecraft:dolphins_grace", 0, 0, 0, 0));
            changed = true;
        }
        TrimDefinition eye = config.trims.get("minecraft:eye");
        if (matchesSingleEffect(eye, "minecraft:regeneration", 1, 2, 2, 2)) {
            eye.effects.clear();
            eye.effects.add(new ConfiguredEffect("minecraft:speed", 0, 1, 1, 1));
            changed = true;
        }
        if (changed) LOGGER.info("Migrated untouched Bolt/Eye defaults for revised built-in bonuses.");
    }


    /** v6: Ward becomes Swift Sneak traversal; Silence owns Sculk speed + Spectral Mark. */
    private static void migrateWardSilenceV6() {
        if (config.configVersion >= 6) return;
        if (config.builtInBonuses == null) config.builtInBonuses = new BuiltInBonusesConfig();

        TrimDefinition ward = config.trims == null ? null : config.trims.get("minecraft:ward");
        if (matchesSingleEffect(ward, "minecraft:absorption", 1, 2, 2, 2)) {
            ward.effects.clear();
            ward.effects.add(new ConfiguredEffect("minecraft:absorption", 0, 0, 0, 0));
        }

        // These are the corrected v6 identities. Existing v5 toggles were newly introduced
        // defaults, so migrate them to the trim they were intended for.
        config.builtInBonuses.wardSwiftSneak = true;
        config.builtInBonuses.wardAncientCitySpeed = true;
        config.builtInBonuses.silenceSculkSpeed = true;
        config.builtInBonuses.silenceSpectralMark = true;
        config.configVersion = 6;
        try { save(); } catch (IOException e) { LOGGER.warn("Migrated Ward/Silence v6 settings in memory but could not save them yet.", e); }
    }

    /** v7: adds material-based utility bonuses without changing pattern configuration. */
    private static void migrateMaterialBonusesV7() {
        if (config.configVersion >= 7) return;
        if (config.materialBonuses == null) config.materialBonuses = new MaterialBonusesConfig();
        config.configVersion = 7;
        try { save(); } catch (IOException e) { LOGGER.warn("Migrated material bonus v7 settings in memory but could not save them yet.", e); }
    }


    /** v8: all set-based material bonuses require four matching material trims and adds the remaining vanilla materials. */
    private static void migrateMaterialBonusesV8() {
        if (config.configVersion >= 8) return;
        if (config.materialBonuses == null) config.materialBonuses = new MaterialBonusesConfig();
        config.materialBonuses.copperLightningResistance = true;
        config.materialBonuses.ironKnockbackResistance = true;
        config.materialBonuses.redstoneMovementSpeed = true;
        config.materialBonuses.lapisExperienceBoost = true;
        config.materialBonuses.emeraldVillagerDiscount = true;
        config.materialBonuses.diamondArmorToughness = true;
        config.configVersion = 8;
        try { save(); } catch (IOException e) { LOGGER.warn("Migrated material bonus v8 settings in memory but could not save them yet.", e); }
    }

    /** v9: default pattern effects begin at two matching pieces. User-customized definitions are preserved. */
    private static void migrateTwoPieceMinimumV9() {
        if (config.configVersion >= 9) return;
        if (config.trims == null) {
            config.configVersion = 9;
            return;
        }

        migrateDefaultEffect("minecraft:flow", "minecraft:jump_boost", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:host", "minecraft:glowing", 1, 1, 1, 1, 0, 1, 1, 1);
        migrateDefaultEffect("minecraft:raiser", "minecraft:saturation", 1, 1, 1, 1, 0, 1, 1, 1);
        migrateDefaultEffect("minecraft:rib", "minecraft:haste", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:sentry", "minecraft:resistance", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:shaper", "minecraft:luck", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:silence", "minecraft:health_boost", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:snout", "minecraft:fire_resistance", 1, 1, 1, 1, 0, 1, 1, 1);
        migrateDefaultEffect("minecraft:spire", "minecraft:strength", 1, 2, 2, 2, 0, 1, 1, 2);
        migrateDefaultEffect("minecraft:tide", "minecraft:water_breathing", 1, 1, 1, 1, 0, 1, 1, 1);
        migrateDefaultEffect("minecraft:vex", "minecraft:invisibility", 1, 1, 1, 1, 0, 1, 1, 1);
        migrateDefaultEffect("minecraft:wayfinder", "minecraft:slow_falling", 1, 1, 1, 1, 0, 1, 1, 1);

        config.configVersion = 9;
        try { save(); } catch (IOException e) { LOGGER.warn("Migrated two-piece minimum v9 settings in memory but could not save them yet.", e); }
    }

    private static void migrateDefaultEffect(String trimId, String effectId,
                                             int old1, int old2, int old3, int old4,
                                             int new1, int new2, int new3, int new4) {
        TrimDefinition def = config.trims.get(trimId);
        if (matchesSingleEffect(def, effectId, old1, old2, old3, old4)) {
            def.effects.clear();
            def.effects.add(new ConfiguredEffect(effectId, new1, new2, new3, new4));
        }
    }

    private static boolean matchesSingleEffect(TrimDefinition def, String id, int a, int b, int c, int d) {
        if (def == null || def.effects == null || def.effects.size() != 1) return false;
        ConfiguredEffect e = def.effects.getFirst();
        return e != null && id.equals(e.id) && e.levels != null
                && java.util.Objects.equals(e.levels.get("1"), a)
                && java.util.Objects.equals(e.levels.get("2"), b)
                && java.util.Objects.equals(e.levels.get("3"), c)
                && java.util.Objects.equals(e.levels.get("4"), d);
    }

    /** Adds the Milestone 6 built-in bonus block without disturbing an existing armor-effect config. */
    private static void migrateBuiltInBonuses() {
        boolean changed = false;
        if (config.builtInBonuses == null) {
            config.builtInBonuses = new BuiltInBonusesConfig();
            changed = true;
        }
        if (config.configVersion < 4) {
            config.configVersion = 4;
            changed = true;
        }
        if (changed) {
            try { save(); } catch (IOException e) { LOGGER.warn("Migrated built-in bonus settings in memory but could not save them yet.", e); }
        }
    }


    /** Enables newly introduced v5 built-in bonuses for existing configs. */
    private static void migrateBuiltInBonusesV5() {
        if (config.configVersion >= 5) return;
        if (config.builtInBonuses == null) config.builtInBonuses = new BuiltInBonusesConfig();
        config.builtInBonuses.wardAncientCitySpeed = true;
        config.builtInBonuses.silenceSculkSpeed = true;
        config.builtInBonuses.silenceSpectralMark = true;
        config.builtInBonuses.snoutBruteHoglinNeutrality = true;
        config.builtInBonuses.ribWitherImmunity = true;
        config.configVersion = 5;
        try { save(); } catch (IOException e) { LOGGER.warn("Migrated v5 built-in bonus settings in memory but could not save them yet.", e); }
    }

    /** Adds trim patterns supplied by Minecraft/mods without inventing gameplay bonuses for them. */
    public static boolean ensureDiscoveredTrims(RegistryAccess access) {
        if (access == null || config.trims == null) return false;
        var registry = access.lookup(Registries.TRIM_PATTERN);
        if (registry.isEmpty()) return false;
        boolean changed = false;
        for (var pattern : registry.get()) {
            String id = pattern.assetId().toString();
            if (!config.trims.containsKey(id)) {
                TrimDefinition definition = new TrimDefinition();
                definition.enabled = false;
                config.trims.put(id, definition);
                changed = true;
                LOGGER.info("Discovered trim pattern '{}' and added it disabled by default.", id);
            }
        }
        if (changed) {
            try { save(); } catch (IOException e) { LOGGER.warn("Discovered new trim patterns but could not save them to config yet.", e); }
        }
        return changed;
    }

    private static void validate() {
        if (config.trims == null) {
            LOGGER.warn("Config has no 'trims' object; restoring default trim definitions in memory.");
            config.trims = TrimEffectsConfig.defaults().trims;
            return;
        }

        for (Map.Entry<String, TrimDefinition> trimEntry : config.trims.entrySet()) {
            String trimId = trimEntry.getKey();
            TrimDefinition trim = trimEntry.getValue();
            if (trim == null || trim.effects == null) {
                LOGGER.warn("Trim '{}' has no valid effect list and will be ignored by future gameplay integration.", trimId);
                continue;
            }

            for (ConfiguredEffect effect : trim.effects) {
                if (effect == null || effect.id == null) {
                    LOGGER.warn("Trim '{}' contains an effect with no id.", trimId);
                    continue;
                }

                Identifier id = Identifier.tryParse(effect.id);
                if (id == null) {
                    LOGGER.warn("Trim '{}' contains malformed status effect id '{}'.", trimId, effect.id);
                    continue;
                }

                if (!BuiltInRegistries.MOB_EFFECT.containsKey(id)) {
                    LOGGER.warn("Trim '{}' references unknown status effect '{}'. If this is a modded effect, ensure its providing mod is installed.", trimId, effect.id);
                }

                if (effect.levels == null) {
                    LOGGER.warn("Trim '{}' effect '{}' has no levels map.", trimId, effect.id);
                    continue;
                }

                for (int pieces = 1; pieces <= 4; pieces++) {
                    Integer level = effect.levels.get(Integer.toString(pieces));
                    if (level == null) {
                        LOGGER.warn("Trim '{}' effect '{}' is missing a level for {} matching trim(s).", trimId, effect.id, pieces);
                    } else if (level < 0 || level > 255) {
                        LOGGER.warn("Trim '{}' effect '{}' has level {} for {} pieces; expected 0-255.", trimId, effect.id, level, pieces);
                    }
                }
            }
        }
    }
}
