# Trimworks

### *Make your armor trims matter!*

Trimworks expands Minecraft's armor trim system by giving both **trim patterns and trim materials meaningful gameplay effects and abilities**.

Trim patterns progress based on the number of matching pieces equipped. Most provide a benefit at 2 matching pieces, with additional effects or abilities available at 4 pieces/full set. Individual trims may use different progression requirements.

Some trims do not provide bonuses at every progression level.

Trim materials provide a separate set of smaller bonuses, allowing the pattern and material systems to work independently.

## Requirements

**Required:** [Fabric API](https://modrinth.com/mod/fabric-api)  
**Optional:** [Mod Menu](https://modrinth.com/mod/modmenu)

### Recommended

- [Legendary Tooltips](https://modrinth.com/mod/legendary-tooltips)
- [Item Borders](https://modrinth.com/mod/item-borders)

These mods are not required, but pair well with Trimworks' expanded armor trim tooltips.

## How Trimworks Works

Trimworks has two independent progression systems:

### Trim Patterns

The **pattern** determines the primary gameplay effect.

- **2 matching pieces** activate the trim's base effect.
- **4 matching pieces** provide the full effect.
- Some 4-piece sets also unlock a unique mechanic or ability.

For example, you could wear four pieces using the Ward pattern to receive Ward's complete pattern bonus.

### Trim Materials

The **material** used to create the trim provides a separate secondary bonus.

Most material bonuses require **4 matching materials**, but the trim patterns do not need to match.

For example, armor using four different trim patterns could still activate the Amethyst material bonus as long as all four trims were made with Amethyst.

This means **pattern progression and material progression can be mixed independently.**

## Trim Pattern Effects

#### Bolt
**2 Pieces:** [+1 Melee Knockback]
**4 Pieces:**   [+1 Melee Knockback (total of 2)]
**Full Set Bonus:** [+ 25% Arrow Deflection]

#### Coast
**2 Pieces:** [None]
**4 Pieces:**   [None]
**Full Set Bonus:** [+ Conduit Power]

#### Dune
**2 Pieces:** [+1 Speed]
**4 Pieces:**   [None - See Full Set Bonus]
**Full Set Bonus:** [+1 Speed (total of 2) on Dune Terrain (i.e. Sand, Sandstone, etc.)]

#### Eye
**2 Pieces:** [+1 Speed]
**4 Pieces:**   [None - See Full Set Bonus]
**Full Set Bonus:** [+1 Speed (total of 2) on Stronghold & End Terrain | Immunity to Endermen's Gaze]

#### Flow
**2 Pieces:** [+1 Jump Boost]
**4 Pieces:**   [+1 Jump Boost (total of 2)]
**Full Set Bonus:** [None]

#### Host
**2 Pieces:** [+1 Glowing]
**4 Pieces:**   [None]
**Full Set Bonus:** [None]

#### Raiser
**2 Pieces:** [+1 Saturation]
**4 Pieces:**   [+1 Saturation (total of 2)]
**Full Set Bonus:** [None]

#### Rib
**2 Pieces:** [+1 Haste]
**4 Pieces:**   [+1 Haste (total of 2)]
**Full Set Bonus:** [Immunity to the Wither effect]

#### Sentry
**2 Pieces:** [+1 Resistance]
**4 Pieces:**   [+1 Resistance (total of 2)]
**Full Set Bonus:** [Illagers are neutral (unless provoked) - OUTSIDE of raids]

#### Shaper
**2 Pieces:** [+1 Luck]
**4 Pieces:**   [+1 Luck (total of 2)]
**Full Set Bonus:** [None]

#### Silence
**2 Pieces:** [+1 Health Boost]
**4 Pieces:**   [+1 Health Boost (total of 2)]
**Full Set Bonus:** [Wardens are neutral, unless provoked | 1+ Speed on Sculk | Damaged mobs glow (10s)]

#### Snout
**2 Pieces:** [+1 Fire Resistance]
**4 Pieces:**   [None]
**Full Set Bonus:** [Piglin Brute & Hoglins are neutral, unless provoked]

#### Spire
**2 Pieces:** [+1 Strength]
**4 Pieces:**   [+1 Strength (total of 2)]
**Full Set Bonus:** [None]

#### Tide
**2 Pieces:** [+1 Water Breathing]
**4 Pieces:**   [+1 Water Breathing (total of 2)]
**Full Set Bonus:** [Gain Dolphin's Grace]

#### Vex
**2 Pieces:** [Invisibility]
**4 Pieces:**   [None]
**Full Set Bonus:** [Vexes are neutral, unless provoked]

#### Wayfinder
**2 Pieces:** [+1 Slow Falling]
**4 Pieces:**   [+1 Slow Falling (total of 2)]
**Full Set Bonus:** [None]

#### Wild
**2 Pieces:** [+1 Speed]
**4 Pieces:**   [None - See Full Set Bonus]
**Full Set Bonus:** [+1 Speed (total of 2) on Wild Terrain (i.e. Grass, Gravel, etc.)]

#### Ward
**2 Pieces:** [+1 Swift Sneak]
**4 Pieces:**   [+1 Swift Sneak (total of 2)]
**Full Set Bonus:** [Immunity to the Darkness Effect (Warden) | +1 Speed on Deepslate/Ancient City Blocks]


## Trim Material Bonuses

Trim materials provide secondary bonuses independently from your chosen trim patterns.

Most bonuses require all four equipped armor pieces to use the same material.

#### Amethyst
**4 Pieces:** [Enchanting Quality Increases +1 Step]

#### Copper
**4 Pieces:** [Attracts Lightning (near the user) during Thunderstorms + 50% Lightning Damage Resistance]

#### Diamond
**4 Pieces:** [+1 Armor Toughness]

#### Emerald
**4 Pieces:** [Villager Discounts on trading]

#### Gold
**4 Pieces:** [Piglins are neutral, unless provoked (excludes Brutes & Hoglins - use the Snout armor trim!)]

#### Lapis
**4 Pieces:** [+5% Increase in Experience]

#### Netherite
**1 Piece:** [The Armor is Now Resistant to Lava]

#### Quartz
**4 Pieces:** [Ghasts are neutral unless provoked]

#### Resin
**4 Pieces:** [You can safely harvest honey without requiring a campfire]

## In-Game Tooltips

Trimworks is designed so that an external guide is not required during normal gameplay.

Hovering over trimmed armor displays information about the equipped trim, including:

- Pattern effects
- Current set progression
- Required number of matching pieces
- Active and inactive effects
- Special abilities
- Trim material bonuses
- Requirements for activating those bonuses

Tooltips automatically reflect the current Trimworks configuration.

## Multiplayer

On multiplayer servers, the **server's Trimworks configuration is authoritative**.

The server automatically synchronizes its configuration with connected players. This allows client-side tooltips to display the effects and requirements actually being used by the server rather than the player's local configuration.

## Configuration

Trimworks allows individual systems, effects, and bonuses to be customized.

Configuration options include:

- Trim effects and effect levels
- Individual pattern bonuses
- Individual material bonuses
- Multiple effects per trim
- Individual mechanics and abilities
- Enabling or disabling specific systems

## Issues and Suggestions

Found a bug, compatibility issue, or have an idea for Trimworks?

Please use the GitHub Issues page and select the appropriate report form.

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

### Compiled JARs will be generated in:
```shell
build/libs/
```

## Modpacks & License

Trimworks may be included in modpacks.
Trimworks is licensed under the MIT License. See [LICENSE](LICENSE) for details.
Copyright © 2026 faefluffkrist

## Credits

Trimworks began as an extension inspired by [TrimeEffects by ParroteX2](https://www.curseforge.com/minecraft/mc-mods/trimeffects), before growing into an independent implementation with its own gameplay systems, configuration, synchronization, tooltips, pattern abilities, and material bonuses.