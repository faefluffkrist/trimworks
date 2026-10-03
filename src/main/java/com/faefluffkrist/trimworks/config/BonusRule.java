package com.faefluffkrist.trimworks.config;

/** Extra status effects or attribute modifiers; negative attribute amounts are penalties. */
public final class BonusRule {
    public boolean enabled = true;
    public String type = "effect";
    public String id = "minecraft:strength";
    public double amount = 1;
    public boolean percent = false;
}
