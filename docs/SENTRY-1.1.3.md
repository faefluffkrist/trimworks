# Trimworks 1.1.3 — Sentry neutrality

- Added ravagers to full-set Sentry neutrality.
- Evokers and vexes see through Sentry and remain hostile.
- Added independent, default-ON Sentry compatibility toggles for Friends & Foes (iceologer, illusioner) and It Takes a Pillage Continuation (archer, legioner, skirmisher).
- These toggles appear in Built-In Pattern Bonuses > Sentry > Full Set Bonus when the corresponding mod is installed. They require the main Illager neutrality setting to be ON.
- Prevent protected target acquisition immediately, with a tick fallback for existing targets.
- Neutrality ends for provoked mobs, participating raiders, or wearers inside an active raid.
- Successful player-attributed projectile damage now counts as provocation.
- Added an evoker action-bar warning for full Sentry wearers: "Seems like Evoker can't be fooled.." It appears when an evoker acquires the wearer as a target, lasts roughly five seconds with native fade-out, and has a five-minute game-time cooldown per player. Continuous targeting does not repeat it. The cooldown survives respawn within the same server session.
- Opening a chest, trapped chest, or barrel provokes eligible vanilla/modded illagers and ravagers within an 8-block horizontal radius and 4 blocks vertically, measured from the container center, if they can see the player. It does not require Sentry armor. Creative and spectator players are excluded. Provocation uses the existing per-mob/per-wearer system, lasting until the server restarts.
- Added default-ON Sentry settings for nearby theft provocation and the evoker warning.
- Removed gold-material piglin target clearing. Four gold trims still count as safe armor; native piglin anger for container opening, gold theft and attacks is preserved. Gold's tooltip is unchanged.
- Updated Sentry tooltip and configuration descriptions. Emerald's positive reputation tooltip explains that it offsets villager anger while a full matching-material set is equipped. This does not erase negative gossip or guarantee neutral prices for arbitrarily large reputation penalties.

No new required dependencies. Existing config files retain their settings; the two new compatibility fields default to true. Server config remains authoritative.

## Validation

Inspected the actual Friends & Foes 4.0.27 and It Takes a Pillage Continuation 1.0.13 entity classes for Minecraft 26.2. All five supported entities inherit AbstractIllager (directly or through SpellcasterIllager), so previous versions already attempted tick-based neutrality for them.

Thirty isolated Java checks passed using the actual Sentry rule method and stand-in game objects. Cases included vanilla/modded targets, unloaded mods, disabled compatibility, evoker/vex exclusions, full versus partial sets, master toggles, provocation, active raids and stopped raids. Twenty-one additional isolated checks passed for the actual encounter handler, covering warning refresh duration, repeated targets, cooldown expiry, player replacement on respawn, independent player cooldowns, theft range boundaries, visibility, excluded entities and toggles. A total of 51 isolated checks passed. These checks do not validate Mixin application or live Minecraft AI.

Full Gradle compilation was attempted but blocked by unavailable Fabric Loom 1.17-SNAPSHOT dependency resolution in the execution environment. No built JAR or in-game validation is claimed.

## Build and game test

Build from the extracted project root on Windows:

```powershell
.\gradlew.bat build
```

Install `build/libs/trimworks-1.1.3.jar` on both client and server, replacing the old Trimworks JAR. Use Java 25 and Minecraft 26.2.

1. In Survival, wear four Sentry-trimmed armor pieces. Outside a raid, confirm pillagers, vindicators, illusioners, ravagers and the five supported modded illagers ignore you.
2. Confirm evokers and vexes still attack. Other trim neutrality abilities retain their own rules.
3. Hit a protected mob with melee and, separately, an arrow. Each damaged mob should retaliate.
4. Remove one Sentry piece and confirm mobs can acquire you again.
5. Test during an active village raid: neutrality must not protect you.
6. Disable each compatibility toggle independently, save, reopen the world, and confirm only the corresponding mod's mobs regain normal targeting.
7. Test with neither optional mod installed. Confirm the extra toggles are hidden and vanilla Sentry behavior still works.
8. Repeat the essential checks on a dedicated server, including a client with differing local config to confirm server settings win.

9. With full Sentry armor, approach an evoker in Survival: confirm the exact warning above the hotbar, approximately five-second display and final fade. Approach without a full set: no warning. Trigger another evoker encounter within five minutes: no second warning. After five minutes, disengage and trigger a new encounter: a new warning should appear.
10. Place chests/barrels near vanilla pillagers and supported modded illagers. Confirm opening within the radius/height and line of sight provokes them, including with full Sentry armor. Repeat beyond 8 horizontal blocks, beyond 4 vertical blocks, and behind an opaque wall: no theft-triggered hostility. Test a blocked or locked chest: unsuccessful opening should not provoke them. Containers do not have an ownership system; player-placed containers also count.
11. Wear four gold material trims without gold armor. Piglins should ignore you initially, then become angry when you open a visible nearby chest or barrel, mine gold, or attack them. Confirm piglin brutes remain hostile as usual.
12. With negative villager reputation, compare prices with and without four emerald material trims. The configured bonus should reduce the reputation penalty while equipped, and the tooltip should describe that temporary effect.

## Combat retaliation follow-up

- Successful player-attributed hits now explicitly set illager retaliation targets instead of only recording provocation. This includes melee, arrows, tridents and player-attributed potion damage, and does not depend on Naturally Trimmed being installed.
- Attacking a vanilla or supported modded illager alerts combat illagers within 15 blocks horizontally and 6 vertically of the attacking player. Horizontal range is a radius; both limits must be satisfied. The directly hit mob retaliates even if farther away. Evokers, vexes and ravagers participate. Allies do not need direct line of sight for the combat alert, and there is no recursive alert chain added by Trimworks.
- The group alert applies regardless of whether the player wears Sentry, while the built-in bonuses master, Illager neutrality and new default-ON Illager group retaliation setting are enabled.
- Chest/barrel theft is still local. An illager that witnessed that player's theft is recorded separately; attacking or killing that witness does not initiate Trimworks' group combat alert. Other unrelated illagers remain neutral to a full-Sentry wearer unless independently provoked.
- Theft witness records are per mob and player and clear when the server/world session stops, matching the existing session-based provocation model.

Thirty Sentry rule checks and 33 encounter/combat checks passed (63 isolated checks total). New checks cover ranged victims outside the alert radius, allied targeting, horizontal/vertical boundaries, casters/vexes/ravagers, non-illager exclusion, ordinary lethal hits, non-spreading theft witness attacks/kills, the group toggle and per-player theft attribution. Actual Minecraft/modded AI and Mixin application still require in-game testing. Local compilation against the available game classes was attempted but blocked by missing game/Fabric dependency classes; no rebuilt JAR is included.

### Focused retest

1. Equip full Sentry. Shoot a fresh Friends & Foes iceologer with an arrow in Survival: it should target and attack you immediately.
2. Put a pillager, iceologer, modded archer and ravager near you. Attack any fresh illager: all eligible mobs within 15 horizontal/6 vertical blocks should target you.
3. Put additional guards farther than 15 horizontal blocks or 6 vertical blocks from you. They should remain neutral with full Sentry. The directly damaged distant mob should still retaliate.
4. Kill a fresh neutral illager with a single hit: nearby guards should still be alerted.
5. Open a chest with only one nearby observer and other guards outside the theft range or behind a wall. Attack/kill that observer: the uninvolved guards should remain neutral with full Sentry. Attack one of the uninvolved guards directly afterward: normal group retaliation should occur.
6. Disable Illager group retaliation, save and reopen the world. A directly shot iceologer should retaliate, but Trimworks should not issue the group alert.
7. Use fresh mobs for each case; previous provocation lasts for the world/server session. Confirm the earlier evoker warning, gold piglin anger and tooltip behavior still work.

## Lone theft witness follow-up

- A full-Sentry wearer caught by one or two illagers receives a white action-bar warning. A lone fleeing pillager uses "They caught me, I need to assassinate them before they tell the others.."; other one/two-observer cases use "Damn it, they saw me stealing.." Three or more observers produce no theft note.
- The theft warning lasts roughly five seconds with native fade and has its own five-minute game-time cooldown, independent of the evoker warning.
- Only an original pillager theft witness can report, and only when no other loaded illager in the same dimension is independently angry with that player. Vanilla pillagers and It Takes a Pillage archers, legioners and skirmishers can carry reports. A lone iceologer or illusioner remains a local combatant and uses the shorter note.
- The witness flees and searches for reachable, uninvolved pillagers within 32 horizontal/8 vertical blocks of its current location. It physically approaches each recipient and informs them within four blocks with line of sight. At most two recipients are informed. If no recipient can be reached, it continues trying to flee; no remote alert is sent.
- An independently angered second illager cancels the reporting mission. Recipients informed by that same mission do not cancel its remaining second delivery.
- Informed mobs remember the theft location, investigate it for up to 30 seconds, and acquire the player only when actually visible nearby (15 horizontal/6 vertical awareness bounds, with a 15-block distance limit). They do not report to additional mobs. A direct player hit interrupts investigation and makes the informed mob retaliate.
- Killing the original witness before contact prevents delivery. If it already informed one recipient, that recipient remains informed; killing it prevents delivery to the second. Attacking theft witnesses or informed guards still does not trigger the general combat group alert.
- Added independent default-ON Theft warning and Lone witness reports theft toggles under Sentry's full-set settings. The reporting system operates regardless of armor; the warnings require full Sentry.

Current validation: 30 Sentry rule checks, 39 encounter/warning/combat checks and 20 theft-report state checks passed (89 isolated checks total). Navigation was represented by stand-in objects; these checks do not prove live pathfinding, Mixin loading or full compilation. No rebuilt JAR is included.

### Additional game tests

1. With full Sentry, open a chest while exactly one pillager can see you. Put two additional pillagers beyond the theft radius but within the runner's search area. Expect the long white warning and a fleeing witness.
2. Kill the witness before it reaches a guard. Confirm the guards remain uninformed. Repeat and let it reach one guard before killing it: only that guard should investigate.
3. Let the witness reach two guards. Confirm exactly those two investigate the original theft location, and a third uninvolved guard is not recruited.
4. Move out of sight after stealing. Confirm informed guards investigate the stored location rather than magically tracking your current position through walls.
5. Steal where two pillagers can see you. Expect the short white warning, normal local aggression and no reporting runner. With three observers, expect no theft note and no reporting runner.
6. Repeat with less than a full Sentry set: the reporting mechanics may still occur, but neither theft warning should appear. Test the warning and snitching toggles independently.
7. During a lone runner's mission, independently anger another illager. Confirm the runner stops reporting and joins normal aggression.
8. Put unreachable guards behind sealed walls. Confirm no remote informing occurs. Live pathfinding and speed should be checked here.
9. Verify the five-minute theft warning cooldown and approximate five-second fade. Evoker warnings retain their separate cooldown.

## Reporting limits and rushing follow-up (current behavior)

These limits supersede the earlier 32/8 search bounds and unlimited fleeing:

- Search for available vanilla/supported modded illagers within 60 blocks along X, Y and Z of the runner. Only loaded mobs are considered; the feature does not force-load chunks. Eligible recruits now include supported iceologers/illusioners as well as pillagers and It Takes a Pillage illagers.
- If no available recruit is detected at the outset, the original witness keeps attacking instead of starting a fleeing goal. The shorter theft note is used, because there is no fleeing runner.
- A runner has up to 15 seconds (300 game ticks) before informing the first recruit. Once one is informed, it has five seconds (100 ticks) to inform the second; this replaces the initial deadline. It still informs at most two, by physical contact and line of sight.
- If no uninformed recruit remains after the first delivery, the five-second deadline still applies. The three-second stuck rule can end this sooner if the runner is blocked.
- Stuck detection compares X/Z displacement over three seconds, using a 0.1-block tolerance. If both axes stay within that tolerance and Y changes no more than three blocks, the reporting attempt ends. Larger vertical progress avoids the stuck trigger.
- On timeout, getting stuck or completion, the witness returns to investigate the recorded theft location and acquires the thief when visible nearby. It does not know a hidden player's new position.
- Runner and informed guards receive Speed I for 30 seconds, refreshed while reporting/investigating. Stronger existing Speed effects are not downgraded. The remaining effect expires naturally after reporting/investigation ends.

Current isolated validation: 22 reporting-limit checks, 39 encounter/warning/combat checks and 30 Sentry-rule checks passed (91 checks total). Cases include no allies, 60-block horizontal and vertical detection, no remote informing, both deadlines, stuck thresholds, maximum recruits, last-position investigation, independent anger cancellation and assassination. Full compilation and live navigation remain unverified.

### Focused additional tests

1. With no other illagers loaded within 60 blocks, steal in front of one pillager: it should attack, without a fleeing phase.
2. Put a recruit 40–59 blocks away. Confirm the witness detects it, runs with Speed I and informs it only after reaching it.
3. Provide a distant/unreachable recruit while allowing the runner to move. Confirm it abandons the initial search by 15 seconds and returns toward the theft location.
4. Let it inform one guard, then keep any second guard out of reach. Confirm it gives up within five seconds of the first delivery (or earlier if stuck).
5. Block the runner in a doorway. If X/Z remain stationary and Y varies no more than three blocks, it should abandon fleeing after roughly three seconds. Confirm ordinary small jumps do not evade that check.
6. Confirm the runner and informed guards have Speed I while rushing. Allow any stronger existing Speed effect to remain stronger.

## Reinforcement horn, slower rush and resistance (current behavior)

This replaces the prior Speed I rushing effect and extra navigation-speed multipliers:

- Each reinforcement plays the Ponder goat-horn sound once from its own location when physically informed. This is SoundEvents.GOAT_HORN_SOUND_VARIANTS index 0, the Ponder instrument. The original runner does not play the reinforcement horn. Horn volume uses the instrument's 256-block audible range.
- Rushing uses a custom +6.67% base movement-speed modifier, one third of vanilla Speed I's +20%. Navigation speed multipliers are now 1.0, removing the extra 25%/10% previously layered onto the potion. External Speed effects are not removed or overwritten.
- Runner and reinforcements receive Resistance I while reporting/investigating. The effect is refreshed to 30 seconds while en route.
- On reaching within two blocks of the stored last-seen location, the custom rush modifier is removed and Resistance I remains for 30 seconds from arrival. It is not continually refreshed while waiting at the arrived location.
- The custom rush modifier also cleans up when the investigation ends, a direct hit interrupts investigation, or the guard engages a visible thief before arriving. Remaining resistance expires naturally. Stronger external Resistance effects are not downgraded.

Current isolated validation: 28 report/rush checks, 39 encounter/warning/combat checks and 30 Sentry checks passed (97 checks total). New cases cover positional horn emission, exact +6.67% modifier, Resistance I duration/amplifier, arrival speed removal and preventing reapplication after arrival. Full compilation, audible sound behavior and real movement remain unverified in this environment.

Focused retest: let one runner inform two guards. Listen for Ponder at each guard, compare the slower pace, and inspect their Resistance I effects. Hide and let them reach the theft location: verify their custom rush ends and resistance counts down for 30 seconds. All earlier theft, snitching, timer and assassination rules remain in place.

## Advancement exclusion, runner outline and Resistance II (current behavior)

- Killing a mob with an active theft-report resistance/rush marker no longer awards the Naturally Trimmed "Even Odds" advancement (`naturally_trimmed/slay_empowered`). This exclusion applies to both the runner and informed guards while their theft buffs remain active, even if other qualifying trim bonuses are also present. The ordinary trimmed-mob advancement remains unchanged.
- The original fleeing witness has a spectral-style glowing outline through walls until it informs the first reinforcement. The outline also ends if the runner gives up, gets stuck, stops reporting or the session ends. No spectral arrow damage is inflicted. The runner's previous glowing tag is restored, and independent potion-based glowing remains intact.
- Resistance is now Resistance II (amplifier 1), retaining the existing 30-second duration after arrival/ending the rush. The +6.67% rush bonus, Ponder horn and reporting time/range limits are unchanged.

Current validation: 35 theft-report/rush/glow checks, 39 encounter/warning/combat checks and 30 Sentry checks passed (104 isolated checks total). New checks include glow while reporting, cleanup on first recruit or stuck fallback, preservation of an existing glow tag, Resistance II, theft-buff classification and expiry. Full compilation and actual Minecraft rendering/advancement events remain unverified.

Retest: watch one runner glow through a wall; allow it to inform its first guard and confirm the outline ends. Inspect runner/guard Resistance II and its 30-second post-arrival duration. Revoke `trimworks:naturally_trimmed/slay_empowered` before killing a theft-buffed mob and confirm it is not re-awarded. Test an ordinary genuinely trim-empowered hostile mob separately to confirm its advancement still works.
