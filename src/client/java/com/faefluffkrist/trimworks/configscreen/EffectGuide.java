package com.faefluffkrist.trimworks.configscreen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Survival-level guidance, not a cap on command/mod-configured amplifier levels. */
final class EffectGuide {
    private EffectGuide() {}
    static String name(String raw,boolean attribute){
        if(raw==null)return "Choose a registered entry";
        var id=Identifier.tryParse(raw);if(id==null)return raw;
        if(attribute){var h=BuiltInRegistries.ATTRIBUTE.get(id);if(h.isPresent())return Component.translatable(h.get().value().getDescriptionId()).getString();}
        else{var h=BuiltInRegistries.MOB_EFFECT.get(id);if(h.isPresent())return Component.translatable(h.get().value().getDescriptionId()).getString();}
        return raw;
    }
    static String guide(String raw){
        if(raw==null||!raw.startsWith("minecraft:"))return "Modded effect: consult its mod. Levels 1–255 allowed.";
        return switch(raw.substring(10)){
            case "speed","haste","strength","jump_boost","regeneration","poison","instant_health","instant_damage" -> "Usual potion/beacon level: up to II. Higher allowed.";
            case "resistance","absorption","slowness" -> "Vanilla survival level: up to IV. Higher allowed.";
            case "hunger" -> "Vanilla survival level: up to III. Higher allowed.";
            case "wither" -> "Vanilla survival level: up to II. Higher allowed.";
            case "hero_of_the_village" -> "Vanilla raid reward: up to V. Higher allowed.";
            case "dolphins_grace","conduit_power","water_breathing","fire_resistance","night_vision","invisibility","slow_falling","weakness","glowing","blindness","darkness","levitation","nausea","infested","oozing","weaving","wind_charged" -> "Usually level I. Higher allowed; some effects do not scale.";
            default -> "No general survival-level guide. Levels 1–255 allowed.";
        };
    }
}
