# Trimworks configuration

Open Trimworks through Mod Menu. The configuration screen has three tabs at the same level:

- **Main Trims:** regular status effects and their levels for exactly 1, 2, 3, or 4 matching equipped pieces.
- **Full Set Bonus:** the original pattern abilities and configurable extra full-set bonuses.
- **Materials:** the original trim-material bonuses and configurable extra full-set bonuses.

Each tab has a scrollable list and a draggable scrollbar. Trim/material names have their own labels. Main trim toggles, edit buttons, and category-wide toggles are separate controls. Category-wide toggles apply only to their own bonus category.

## Main trim effects

Select Edit next to a trim. Each level field explains the exact equipped-piece count it controls. Enter **0** to disable that step, **1** for level I, **2** for level II, and so on. These are effect levels, not zero-based amplifier values. Only equipped armor counts.

**Choose / search effect** opens a live suggestion list. Type a translated effect name such as `strength`, a registry ID such as `minecraft:strength`, or a namespace such as `minecraft:`. Installed mod effects are included. Selecting a suggestion stores its registry ID.

The effect editor shows usual vanilla survival/potion/beacon level guidance. This is a recommendation, not a hard cap. Values from 1–255 remain allowed; higher values may have no additional effect for some status effects. There is no universal maximum defined by the effect registry itself. Modded effects and effects without a general survival source are marked accordingly.

Use Add Effect or Remove to manage entries. An empty list stays empty and explicitly says how to add an effect. Save commits the draft; Back discards unsaved changes. Reset changes the draft to defaults and must be saved to keep.

## Built-in and material bonuses

Every built-in setting has a name and a short explanation. Numerical fields describe their units and conditions. Arrow deflection chance is displayed as a percent: enter **25** for 25%, rather than entering `0.25`. The existing JSON still stores that chance as `0.25` for compatibility. Glow duration and lightning attraction interval use ticks; 20 ticks = 1 second.

Trims with no original built-in bonus explicitly say so. **Extra effects & attributes** allows adding bonuses even to these trims.

Extra bonuses each have an ON/OFF control, type, searchable registry entry, amount, and unit explanation. Status effects use integer levels from 1–255. Attributes use either flat additions or percentages of the base stat; negative amounts apply penalties. All extra bonuses require four matching equipped patterns/materials.

Existing original mechanics have independent toggles. To replace an original mechanic, turn it off and add an extra effect/attribute. To combine them, leave the original enabled.

Server settings continue to override client settings in multiplayer. Connected clients can view but cannot edit a remote server's synchronized configuration. Reopen the world or restart the dedicated server after saving.

## Tooltips

Only Trimworks-added tooltip text is reformatted; vanilla and other mods' tooltip text is preserved. Descriptions use compact wording and wrap at up to six words, with indented continuation lines. Numeric values stay with their unit groups. Bolt and Ward progression have dedicated rows for two and four pieces.

## Compatibility

Regular trim progression, gameplay classes, config field names, and server synchronization are preserved from the supplied project. The previously fixed EnchantmentInstance accessor methods remain intact. The mod version now matches the supplied project folder: **1.1.0**.

GUI labels use opaque ARGB colors. The selected effect/attribute remains visible in the editor header, and main-effect fields explicitly identify each piece count. Resin tooltips state that smoke is not required.
