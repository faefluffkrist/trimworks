# Trimworks

### *Make your armor trims matter!*

Trimworks expands Minecraft's armor trim system by giving both **trim patterns and trim materials meaningful gameplay effects, abilities, progression, and customization**.

Most trim patterns provide a benefit at **2 matching pieces**, with additional effects or abilities available at **4 pieces/full set**. Trim materials provide a separate set of secondary bonuses, allowing pattern and material progression to work independently.

## Requirements

- **Required:** [Fabric API](https://modrinth.com/mod/fabric-api)
- **Optional:** [Mod Menu](https://modrinth.com/mod/modmenu)
- **Optional:** [Naturally Trimmed](https://modrinth.com/mod/naturally-trimmed)

## Features

### Trim Patterns

Each of Minecraft's 18 armor trim patterns has its own effects and progression.

Depending on the pattern, matching armor pieces can provide status effects, attributes, terrain-based bonuses, mob neutrality, environmental abilities, or unique full-set mechanics.

Examples include:

- Arrow deflection
- Terrain-based movement bonuses
- Conduit Power and Dolphin's Grace
- Darkness and Wither immunity
- Illager, Warden, Vex, Piglin Brute, Hoglin, and other mob interactions
- Additional combat and utility effects

### Trim Materials

The material used for an armor trim provides a separate secondary bonus.

Material bonuses include mechanics based around:

- Enchanting
- Lightning
- Armor attributes
- Villager trading
- Piglin and Ghast neutrality
- Experience gain
- Lava resistance
- Honey harvesting
- Movement speed

Most material bonuses require four matching materials, although some have different requirements.

Pattern and material bonuses are independent, allowing different trim patterns to share the same material bonus.

## Advancements & Challenges

Trimworks includes a dedicated advancement page focused on discovering, collecting, wearing, and using armor trims.

Progression includes:

- Discovering all 18 vanilla trim templates
- Completing the template collection
- Duplicating templates
- Equipping full sets of each trim pattern
- Pattern-specific challenges based on their abilities and origins
- Material-themed challenges
- Persistent cumulative objectives

Cumulative advancement progress is preserved through death, relogging, and server restarts.

## Naturally Trimmed Compatibility

Trimworks includes optional integration with [Naturally Trimmed](https://modrinth.com/mod/naturally-trimmed).

When installed, eligible mobs wearing trimmed armor can receive applicable Trimworks:

- Main Trim effects
- Full Set Bonuses and abilities
- Material Bonuses

Mob bonuses have their own configuration and blacklists, allowing them to be controlled independently from player bonuses.

Naturally Trimmed also unlocks an additional Trimworks advancement branch.

Naturally Trimmed is **not required or bundled** with Trimworks.

## In-Game Tooltips

Trimworks provides expanded armor tooltips showing:

- Pattern effects
- Set progression
- Required matching pieces
- Active and inactive effects
- Full Set Bonuses and abilities
- Material bonuses
- Activation requirements
- Configured bonus values

Tooltips automatically reflect the active Trimworks configuration.

## Configuration

Trimworks provides extensive configuration for its gameplay systems.

Configuration includes:

- Trim effects and effect levels
- Built-in pattern bonuses and values
- Material bonuses and values
- Individual mechanics and abilities
- Additional Full Set status effects
- Additional Full Set attribute modifiers
- Multiple additional effects or attributes per trim
- Flat, percentage, and negative attribute values
- Naturally Trimmed mob bonuses and blacklists

Compatible effects and attributes added by other mods can be selected by name or registry ID.

[Mod Menu](https://modrinth.com/mod/modmenu) is recommended for easier in-game configuration.

## Multiplayer

On multiplayer servers, the **server's Trimworks configuration is authoritative**.

Server settings automatically synchronize to connected players so client-side tooltips reflect the effects, values, requirements, and abilities actually being used by the server.

## Full Documentation & Download

For the complete pattern and material reference, screenshots, advancement previews, and official releases:

[Trimworks on Modrinth](https://modrinth.com/mod/trimworks-faefluffkrist)

## Issues and Suggestions

Found a bug, compatibility issue, or have a feature suggestion?

[GitHub Issues](https://github.com/faefluffkrist/trimworks/issues)

## Building from Source

Trimworks uses the included Gradle Wrapper.

### Windows

```shell
gradlew.bat build
```

### Linux / macOS
```shell
./gradlew build
```

# Compiled JARS are generated in:
```shell
build/libs/
```
## Modpacks & License

Trimworks may be included in modpacks.
Trimworks is licensed under the MIT License

Copyright © 2026 faefluffkrist

## Credit

Trimworks began as an extension inspired by TrimeEffects by ParroteX2, before growing into an independent implementation with its own gameplay systems, configuration, synchronization, tooltips, pattern abilities, material bonuses, and advancement system.