# Naturally Trimmed compatibility

Trimworks remains a Fabric Minecraft 26.2 mod. Install Naturally Trimmed separately, with a release compatible with your Minecraft version. It is optional: no dependency, copied source, bundled jar, or direct linkage to its classes was added. Trimworks retains its existing MIT license and attribution. Naturally Trimmed distributes under LGPL-3.0-only and remains a separate project.

## Configuration

Open Trimworks in Mod Menu and select **Naturally Trimmed** at the bottom of the configuration hub. The button is grayed out when the mod is not loaded. On a multiplayer server, the server's detection is authoritative and the settings are read-only on clients. Edit the server's `config/trimworks.json` to change its policy. In singleplayer, use Save; Back discards pending edits. Reopen the world after saving, as with the existing configuration.

The master switch enables/disables Trimworks bonuses for non-player mobs. It does not disable Naturally Trimmed's equipment generation and never changes player bonuses.

- **Main Trims:** blocking a pattern prevents its main status effects and its pattern/set bonuses on mobs. Its material remains independent.
- **Full Set Bonus:** blocking a pattern here prevents its built-in pattern abilities and added full-set effects/attributes, while keeping its main status effects. If the pattern is already blocked under Main Trims, this row displays `Trim off`. This category also controls the existing two-piece Ward/Bolt built-in abilities.
- **Materials:** blocking a material prevents that material's wearer bonuses and added material effects/attributes on mobs, independently of the pattern.

Allowed means eligible, not forced on: the normal category switches, individual ability switches, configured levels/amounts, and equipped-piece requirements still apply. Players continue to use the existing rules. Defaults allow all three categories on mobs when Naturally Trimmed is present; existing player configuration is preserved.

The three lists include known modded IDs. Materials supplied by mods appear in the regular Materials editor while a world is open, where added effects/attributes can be configured. Entries for absent mods remain in the blacklists. Outside a world, previously configured IDs and vanilla entries remain available; open a world to discover additional registry entries.

### JSON example

```json
"mobBonuses": {
  "enabled": true,
  "mainTrimBlacklist": ["minecraft:spire", "example:custom_trim"],
  "fullSetBlacklist": ["minecraft:bolt"],
  "materialBlacklist": ["minecraft:copper", "example:custom_material"]
}
```

This blocks Spire's main and pattern bonuses on mobs, leaves Bolt's main effects eligible but blocks Bolt abilities, and blocks copper wearer bonuses. Config schema 11 adds this block without resetting existing settings. `detectedNaturallyTrimmed` is runtime synchronization metadata, not a setting to edit.

## Supported behavior

All loaded `Mob` subclasses, including modded mobs, use the four normal armor slots. Held armor, body armor, and tool trims do not contribute. Naturally Trimmed supplies ordinary Minecraft trim components; this integration consumes those components without altering mob spawning, loot, trades, or AI goals. It also works for manually equipped mobs while the optional mod is loaded; it does not track the origin of an armor item.

Main status effects use the configured one/two/three/four-piece levels. Full-set effect/attribute rules and existing terrain speed, effect immunities, Ward sneak attribute, Silence spectral marking, Bolt melee knockback/arrow deflection, and neutrality checks are shared with mob wearers where applicable. Material attributes and copper lightning attraction/resistance are shared too. Neutrality does not suppress provoked enemies; custom mod AI may re-acquire targets or use combat methods that bypass the standard hooks.

Some bonuses inherently require player actions and have no mob equivalent: enchanting-table boosts, XP pickup bonuses, villager trading discounts, honey harvesting, and Enderman player-gaze immunity. Minecraft itself determines whether a particular status effect has useful behavior on a mob. Netherite's dropped-item fire protection remains an item property controlled by the existing global material setting; mob blacklists govern wearers, not dropped items.

When equipped trims or policy change, this integration removes its transient attribute modifiers; its short-duration status effects stop refreshing and expire naturally. It skips mobs without trimmed armor except for a final modifier cleanup after a tracked wearer loses eligibility.

## Validation and local test

Java syntax parsing passed for all 45 sources. The edited gameplay and UI classes type-check against the Minecraft 26.2 API classes supplied in the stable ZIP. Because the available runtime is Java 17, the API-only check used class headers adapted for compiler inspection and small stubs for unavailable Fabric/Mixin/dependency classes; this does not substitute for a Java 25 Gradle build or validate mixin injection at runtime. A separate executable policy test passed 28 assertions covering absent/present mod detection, server/client detection precedence, player isolation, independent exclusions, pattern-to-set blocking, custom IDs, and null-list handling.

Build locally with Java 25:

```powershell
.\gradlew.bat clean build
```

Use `build/libs/trimworks-1.1.0.jar` with matching Fabric API and Naturally Trimmed, plus optional Mod Menu. Runtime verification is still required:

1. Without Naturally Trimmed, check the grayed-out compatibility button and unchanged player bonuses.
2. With it, equip a persistent mob with two/four matching trimmed armor pieces and inspect its configured effects/attributes. Check a modded mob/trim/material if available.
3. Block a pattern: its main and pattern bonuses should stop on the mob while the material remains eligible. Block only the full set: its main effects should remain. Block only the material: pattern bonuses should remain.
4. Disable the master: all mob bonuses should stop; player bonuses should remain. Remove armor and check transient attributes return to their base values.
5. Verify Bolt arrows/melee, Silence marking, copper lightning, and applicable neutrality with the standard combat hooks.
6. Join a dedicated server with Naturally Trimmed installed only there; its detection should enable the read-only compatibility view. Check the inverse case with the mod only on the client.
