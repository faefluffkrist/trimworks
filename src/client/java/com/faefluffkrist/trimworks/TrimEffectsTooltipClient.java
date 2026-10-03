package com.faefluffkrist.trimworks;

import com.faefluffkrist.trimworks.config.ConfiguredEffect;
import com.faefluffkrist.trimworks.config.TrimDefinition;
import com.faefluffkrist.trimworks.config.TrimEffectsConfigManager;
import com.faefluffkrist.trimworks.network.TrimConfigSyncPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.equipment.trim.ArmorTrim;

import java.util.List;

public class TrimEffectsTooltipClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(TrimConfigSyncPayload.TYPE, (payload, context) ->
                TrimEffectsConfigManager.acceptServerJson(payload.json()));
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> TrimEffectsConfigManager.clearServerSync());
        ItemTooltipCallback.EVENT.register((stack, context, type, tooltip) -> addTooltip(stack, tooltip));
    }

    private static void addTooltip(ItemStack stack, List<Component> tooltip) {
        int firstAdded = tooltip.size();
        addTrimTooltip(stack, tooltip);
        TooltipLayout.wrapAddedLines(tooltip, firstAdded);
    }

    private static void addTrimTooltip(ItemStack stack, List<Component> tooltip) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim != null) {
            addConfiguredTooltip(trim.pattern().value().assetId().toString(), tooltip, true);
            addMaterialBonusInfo(trim, tooltip);
            return;
        }
        if (stack.getItem() instanceof SmithingTemplateItem) {
            String patternId = getTrimPatternIdFromTemplate(stack);
            if (patternId != null) addConfiguredTooltip(patternId, tooltip, false);
        }
    }

    private static void addConfiguredTooltip(String patternId, List<Component> tooltip, boolean showActive) {
        TrimDefinition definition = TrimEffectsConfigManager.getDisplayConfig().trims.get(patternId);
        if (definition == null) return;

        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("tooltip.trimworks.trim_effect").withStyle(ChatFormatting.GOLD));
        if (!definition.enabled) {
            tooltip.add(Component.translatable("tooltip.trimworks.disabled").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        Player player = Minecraft.getInstance().player;
        int equippedPieces = showActive && player != null ? countEquippedMatchingPattern(player, patternId) : 0;

        if (definition.effects != null) for (ConfiguredEffect effect : definition.effects) {
            if (effect == null || effect.id == null || effect.levels == null) continue;
            int maxLevel = 0;
            int piecesForMax = 0;
            for (int pieces = 1; pieces <= 4; pieces++) {
                int level = effect.levels.getOrDefault(Integer.toString(pieces), 0);
                if (level > maxLevel) { maxLevel = level; piecesForMax = pieces; }
            }
            if (maxLevel <= 0) continue;

            Component name = effectName(effect.id);
            tooltip.add(Component.literal("+ ").withStyle(ChatFormatting.YELLOW)
                    .append(name.copy().withStyle(ChatFormatting.YELLOW)));
            tooltip.add(Component.translatable("tooltip.trimworks.maximum",
                    roman(maxLevel), matchingTrimText(piecesForMax)).withStyle(ChatFormatting.GRAY));

            if (equippedPieces > 0) {
                int active = effect.levels.getOrDefault(Integer.toString(equippedPieces), 0);
                if (active > 0) {
                    tooltip.add(Component.translatable("tooltip.trimworks.active",
                            roman(active), matchingTrimText(equippedPieces)).withStyle(ChatFormatting.YELLOW));
                }
            }
        }

        // Bolt has no ordinary status-effect progression now, so make its per-piece
        // built-in melee knockback visible even before the full set is complete.
        var builtIns = TrimEffectsConfigManager.getDisplayConfig().builtInBonuses;
        if ("minecraft:bolt".equals(patternId) && builtIns != null && builtIns.enabled && builtIns.boltMeleeKnockback) {
            tooltip.add(Component.literal("+ Melee Knockback").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("  2 pieces: " + signed(builtIns.boltKnockbackTwoPieces)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  4 pieces: " + signed(builtIns.boltKnockbackFourPieces)).withStyle(ChatFormatting.GRAY));
            if (equippedPieces >= 2) {
                tooltip.add(Component.literal("Active: " + signed(equippedPieces >= 4 ? builtIns.boltKnockbackFourPieces : builtIns.boltKnockbackTwoPieces) + " Knockback")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        // Ward's normal progression is a built-in sneaking-speed mechanic rather than a potion effect.
        if ("minecraft:ward".equals(patternId) && builtIns != null && builtIns.enabled && builtIns.wardSwiftSneak) {
            tooltip.add(Component.literal("+ Sneaking Speed").withStyle(ChatFormatting.YELLOW));
            tooltip.add(Component.literal("  2 pieces: " + signed(builtIns.wardSneakTwoPieces)).withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.literal("  4 pieces: " + signed(builtIns.wardSneakFourPieces)).withStyle(ChatFormatting.GRAY));
            if (equippedPieces >= 2) {
                tooltip.add(Component.literal("Active: " + signed(equippedPieces >= 4 ? builtIns.wardSneakFourPieces : builtIns.wardSneakTwoPieces) + " Sneaking Speed")
                        .withStyle(ChatFormatting.YELLOW));
            }
        }

        // Advertise configured built-in full-set bonuses on both templates and armor.
        // Armor additionally reports when the four-piece condition is currently met.
        addBuiltInBonusInfo(patternId, tooltip, showActive && equippedPieces >= 4);
    }

    private static void addBuiltInBonusInfo(String patternId, List<Component> tooltip, boolean active) {
        var cfg = TrimEffectsConfigManager.getDisplayConfig().builtInBonuses;
        if (cfg == null || !cfg.enabled) return;
        java.util.ArrayList<String> bonuses = new java.util.ArrayList<>();
        switch (patternId) {
            case "minecraft:ward" -> {
                if (cfg.wardDarknessImmunity) bonuses.add("Darkness Immunity");
                if (cfg.wardAncientCitySpeed) bonuses.add("Speed " + roman(cfg.wardSpeedLevel) + " on Deepslate/Ancient City Blocks");
            }
            case "minecraft:silence" -> {
                if (cfg.silenceWardenNeutrality) bonuses.add("Warden Neutrality unless provoked");
                if (cfg.silenceSculkSpeed) bonuses.add("Speed " + roman(cfg.silenceSpeedLevel) + " on Sculk Blocks");
                if (cfg.silenceSpectralMark) bonuses.add("Damaged Mobs Glow (" + number(cfg.silenceMarkTicks / 20.0) + "s)");
            }
            case "minecraft:tide" -> { if (cfg.tideDolphinsGrace) bonuses.add("Dolphin's Grace " + roman(cfg.tideGraceLevel)); }
            case "minecraft:bolt" -> { if (cfg.boltProjectileDeflection) bonuses.add(number(cfg.boltProjectileDeflectionChance * 100) + "% Arrow Deflection"); }
            case "minecraft:coast" -> { if (cfg.coastConduitPower) bonuses.add("Conduit Power " + roman(cfg.coastConduitLevel)); }
            case "minecraft:sentry" -> { if (cfg.sentryIllagerNeutrality) bonuses.add("Illager Neutrality (outside raids)"); }
            case "minecraft:vex" -> { if (cfg.vexNeutrality) bonuses.add("Vex Neutrality unless provoked"); }
            case "minecraft:dune" -> { if (cfg.duneTerrainSpeed) bonuses.add("Speed " + roman(cfg.duneSpeedLevel) + " on Dune Terrain"); }
            case "minecraft:wild" -> { if (cfg.wildTerrainSpeed) bonuses.add("Speed " + roman(cfg.wildSpeedLevel) + " on Wild Terrain"); }
            case "minecraft:eye" -> {
                if (cfg.eyeTerrainSpeed) bonuses.add("Speed " + roman(cfg.eyeSpeedLevel) + " on Stronghold/End Terrain");
                if (cfg.eyeEndermanGazeImmunity) bonuses.add("Enderman Gaze Immunity");
            }
            case "minecraft:snout" -> { if (cfg.snoutBruteHoglinNeutrality) bonuses.add("Piglin Brute & Hoglin Neutrality unless provoked"); }
            case "minecraft:rib" -> { if (cfg.ribWitherImmunity) bonuses.add("Wither Effect Immunity"); }
            default -> { }
        }
        if (cfg.extraBonuses != null && cfg.extraBonuses.get(patternId) != null)
            for (var rule : cfg.extraBonuses.get(patternId)) if (rule != null && rule.enabled) bonuses.add(ruleDescription(rule));
        if (bonuses.isEmpty()) return;
        tooltip.add(Component.literal(bonuses.size() == 1 ? "Full Set Bonus:" : "Full Set Bonuses:").withStyle(ChatFormatting.AQUA));
        for (String bonus : bonuses) tooltip.add(Component.literal(prefix(bonus)).withStyle(ChatFormatting.AQUA));
        if (active) {
            tooltip.add(Component.literal(bonuses.size() == 1 ? "Built-In Bonus: ACTIVE" : "Built-In Bonuses: ACTIVE")
                    .withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.literal("Requires: 4 matching trims").withStyle(ChatFormatting.GRAY));
        }
    }

    private static void addMaterialBonusInfo(ArmorTrim trim, List<Component> tooltip) {
        var cfg = TrimEffectsConfigManager.getDisplayConfig().materialBonuses;
        if (cfg == null || !cfg.enabled) return;
        String materialId = trim.material().unwrapKey().map(k -> k.identifier().toString()).orElse("");
        String bonus = null;
        int required = 4;
        if ("minecraft:gold".equals(materialId) && cfg.goldPiglinNeutrality) bonus = "Piglin Neutrality\nExcludes Piglin Brutes & Hoglins";
        else if (("minecraft:resin".equals(materialId) || "minecraft:amber".equals(materialId)) && cfg.amberSafeHoneyHarvest) bonus = "Safe honey harvesting from\nBeehives & Bee Nests\nWithout requiring smoke";
        else if ("minecraft:amethyst".equals(materialId) && cfg.amethystEnchantingBoost) bonus = "Primary enchantment: " + signed(cfg.amethystLevelIncrease) + " level" + (Math.abs(cfg.amethystLevelIncrease) == 1 ? "" : "s") + "\nTools, weapons & books";
        else if ("minecraft:quartz".equals(materialId) && cfg.quartzGhastNeutrality) bonus = "Ghast Neutrality unless provoked";
        else if ("minecraft:copper".equals(materialId) && cfg.copperLightningResistance) bonus = "Attracts Lightning\n" + signed(cfg.copperDamageResistancePercent) + "% Lightning Damage Resistance";
        else if ("minecraft:iron".equals(materialId) && cfg.ironKnockbackResistance) bonus = signed(cfg.ironResistance) + " Knockback Resistance";
        else if ("minecraft:redstone".equals(materialId) && cfg.redstoneMovementSpeed) bonus = number(cfg.redstoneSpeedPercent) + "% Movement Speed";
        else if ("minecraft:lapis".equals(materialId) && cfg.lapisExperienceBoost) bonus = number(cfg.lapisExperiencePercent) + "% Experience Gain";
        else if ("minecraft:emerald".equals(materialId) && cfg.emeraldVillagerDiscount) bonus = "Villager Discount (" + signed(cfg.emeraldReputation) + " reputation)";
        else if ("minecraft:diamond".equals(materialId) && cfg.diamondArmorToughness) bonus = signed(cfg.diamondToughness) + " Armor Toughness";
        else if ("minecraft:netherite".equals(materialId) && cfg.netheriteFireproofPiece) { bonus = "Lava-proof armor piece"; required = 1; }
        var extras = cfg.extraBonuses == null ? null : cfg.extraBonuses.get(materialId);
        if (bonus == null && (extras == null || extras.stream().noneMatch(r -> r != null && r.enabled))) return;

        tooltip.add(Component.literal("Material Bonus:").withStyle(ChatFormatting.LIGHT_PURPLE));
        if (bonus != null) tooltip.add(Component.literal(prefix(bonus)).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (extras != null) for (var rule : extras) if (rule != null && rule.enabled)
            tooltip.add(Component.literal(prefix(ruleDescription(rule))).withStyle(ChatFormatting.LIGHT_PURPLE));
        if (required == 1 && (extras == null || extras.stream().noneMatch(r -> r != null && r.enabled))) {
            tooltip.add(Component.literal("Material Bonus: ACTIVE").withStyle(ChatFormatting.GREEN));
            return;
        }
        Player player = Minecraft.getInstance().player;
        int count = player == null ? 0 : countEquippedMatchingMaterial(player, materialId);
        if (count >= 4) tooltip.add(Component.literal("Material Bonus: ACTIVE").withStyle(ChatFormatting.GREEN));
        else tooltip.add(Component.literal("Requires: 4 matching material trims").withStyle(ChatFormatting.GRAY));
    }

    private static int countEquippedMatchingMaterial(Player player, String materialId) {
        int count = 0;
        Identifier wanted = Identifier.tryParse(materialId);
        if (wanted == null) return 0;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ArmorTrim trim = player.getItemBySlot(slot).get(DataComponents.TRIM);
            if (trim != null && trim.material().is(wanted)) count++;
        }
        return count;
    }

    private static Component effectName(String rawId) {
        Identifier id = Identifier.tryParse(rawId);
        if (id != null) {
            var holder = BuiltInRegistries.MOB_EFFECT.get(id);
            if (holder.isPresent()) return Component.translatable(holder.get().value().getDescriptionId());
        }
        return Component.literal(rawId);
    }

    private static String getTrimPatternIdFromTemplate(ItemStack stack) {
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        String path = key.getPath();
        String suffix = "_armor_trim_smithing_template";
        if (!path.endsWith(suffix)) return null;
        String pattern = path.substring(0, path.length() - suffix.length());
        String fullId = key.getNamespace() + ":" + pattern;
        return TrimEffectsConfigManager.getDisplayConfig().trims.containsKey(fullId) ? fullId : null;
    }

    private static int countEquippedMatchingPattern(Player player, String patternId) {
        int count = 0;
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (EquipmentSlot slot : slots) {
            ArmorTrim trim = player.getItemBySlot(slot).get(DataComponents.TRIM);
            if (trim != null && trim.pattern().value().assetId().toString().equals(patternId)) count++;
        }
        return count;
    }

    private static String number(double value) {
        return java.math.BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
    private static String signed(double value) { return (value >= 0 ? "+" : "") + number(value); }
    private static String prefix(String text) { return text.startsWith("+") || text.startsWith("-") ? text : "+ " + text; }
    private static String ruleDescription(com.faefluffkrist.trimworks.config.BonusRule rule) {
        if ("effect".equals(rule.type)) return effectName(rule.id).getString() + " " + roman((int)rule.amount);
        String name = rule.id;
        var id = Identifier.tryParse(rule.id);
        if (id != null) {
            var holder = BuiltInRegistries.ATTRIBUTE.get(id);
            if (holder.isPresent()) name = Component.translatable(holder.get().value().getDescriptionId()).getString();
        }
        return signed(rule.amount) + (rule.percent ? "% " : " ") + name;
    }

    private static String roman(int level) {
        return switch (level) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            case 6 -> "VI"; case 7 -> "VII"; case 8 -> "VIII"; case 9 -> "IX"; case 10 -> "X";
            default -> Integer.toString(level);
        };
    }

    private static String matchingTrimText(int count) {
        return count == 1 ? "1 matching trim" : count + " matching trims";
    }
}
