# Advancement guide

One Trimworks tab unlocks after obtaining any vanilla armor trim template. Templates, Dressed for Adventure (full sets), Materials Matter, and Naturally Trimmed form separate branches within that page. All challenges are visible after unlock without being granted. Naturally Trimmed’s branch is included only when its mod is installed. The page uses the deepslate texture. Challenge parents control layout, not prerequisite completion. Existing earned challenges and counters remain saved. Legacy completion of the old root by itself does not bypass the template requirement.

Counters are saved as player statistics. Lapis counts newly gained levels from positive XP awards, rather than the level already held. Trial completion means finishing one trial spawner encounter; Bolt accepts regular and ominous, Flow accepts ominous only. A participant must still be alive, in that dimension, and within 64 blocks when the trial finishes.

Ward requires the same Warden within 32 blocks for 1,200 consecutive server ticks. Start the encounter with line of sight. Death, set removal, leaving range, an attack target, angry mode, or anger above 100 resets the timer. Relogging restarts it.

Shaper counts diamonds when a player first generates a vanilla structure chest’s loot. Reopening the chest and player-supplied diamonds do not count. Wayfinder counts downward movement outside flight, gliding, swimming and riding; teleports above 64 blocks per movement are excluded.

Existing completed advancements remain completed. To retest the revised Ward challenge:

```mcfunction
/advancement revoke @s only trimworks:pattern/ward_warden
```

## Full-set challenges

| Trim | Advancement | Requirement |
| --- | --- | --- |
| Wayfinder | The Long Way Down | Fall 200 cumulative blocks. Flight, gliding, swimming and riding do not count. Requires a full Wayfinder set. |
| Silence | A Quiet Offering | Kill an enemy whose XP feeds a sculk catalyst. Requires a full Silence set. |
| Spire | Shell We Dance? | Slay ten Shulkers in total. Requires a full Spire set. |
| Wild | Into the Wild | Enter a jungle temple. Requires a full Wild set. |
| Bolt | Trial and Trial Again | Complete five regular or ominous trial spawners in total. Requires a full Bolt set. |
| Ward | The City Falls Silent | Slay a Warden in an ancient city while wearing a full Ward set. Are you sure you've done the right thing? |
| Host | Late-Night Host | Visit trail ruins at night. Requires a full Host set. |
| Dune | Just Desserts | Take a Husk hit that leaves you with Hunger. Requires a full Dune set. |
| Coast | Ship Happens | Enter any shipwreck, including a beached one. Requires a full Coast set. |
| Dune | Sands of Time | Enter a desert temple. Requires a full Dune set. |
| Rib | Family Resemblance | Meet the Wither. Requires a full Rib set. |
| Ward | A Minute of Peace | Stay within 32 blocks of the same calm or suspicious Warden for 60 continuous seconds, without dying or letting it become angry. Its anger must never exceed 100. Requires a full Ward set. |
| Bolt | Gone with the Wind | Slay a Breeze. Requires a full Bolt set. |
| Flow | Against the Current | Complete five ominous trial spawners. Requires a full Flow set. |
| Spire | Tall Order | Slay the Ender Dragon, Wither, Elder Guardian or Warden. Requires a full Spire set. |
| Tide | Making Waves | Slay 50 Guardians in total. Requires a full Tide set. |
| Sentry | Who Goes There? | Meet a pillager or ravager. Requires a full Sentry set. |
| Rib | Bones of the Fortress | Enter a Nether Fortress. Requires a full Rib set. |
| Tide | Respect Your Elders? | Slay an Elder Guardian. Requires a full Tide set. |
| Eye | Eye to Eye | Meet the Ender Dragon, or carry its egg. Requires a full Eye set. |
| Snout | A Boaring End | Be killed by a piglin brute or hoglin inside a bastion remnant. Requires a full Snout set. |
| Silence | Sonic Boom | Kill a mob at least 20 blocks away after marking it with your still-active spectral glow. Requires a full Silence set. |
| Coast | X Marks the Spot | Find buried treasure. Requires a full Coast set. |
| Sentry | Not on My Watch | Be inside an active village raid. Requires a full Sentry set. |
| Vex | Spell Cancelled | Slay an Evoker. Requires a full Vex set. |
| Vex | Uninvited Guest | Enter a woodland mansion. Requires a full Vex set. |
| Eye | Eye on the Horizon | Enter an End city or its ship. Requires a full Eye set. |
| Silence | Quiet Company | Meet the Warden. Requires a full Silence set. |
| Wild | String Theory | Trigger an unpowered tripwire inside a jungle temple. Requires a full Wild set. |
| Shaper | Rough Around the Edges | Find five diamonds in newly opened generated structure loot chests. Requires a full Shaper set. |
| Snout | Among the Gold | Enter a bastion remnant. Requires a full Snout set. |

## Material challenges

| Advancement | Requirement |
| --- | --- |
| Fireproof Finish | Apply a Netherite trim to armor made from another material. |
| A Little Wiser | Gain 20 cumulative levels from earned XP while wearing an active four-piece Lapis set. Progress starts at zero and survives death and XP spending. |
| A Golden Deal | Complete 50 piglin barters while wearing an active four-piece Gold set. |
| A Thousand Steps | Travel 1,000 cumulative blocks while wearing an active four-piece Redstone set. |
| Polished Perfection | Apply a Diamond trim to Netherite armor. |
| A Shocking Choice | Be struck by lightning while wearing an active four-piece Copper set. |
| A Good Reputation | Complete 50 merchant trades while wearing an active four-piece Emerald set. |
| Home Is Where the Ghast Is | Leash or harness a happy ghast while wearing an active four-piece Quartz set. From stray to sky-high companion. |
| A Diamond in the Rough | Apply a Diamond trim to leather armor. |
| Crystal Clear Improvement | Upgrade the primary enchantment 50 times with the active four-piece Amethyst bonus. |
| Honey Without Smoke | Collect five bottles of honey from hives or nests while wearing an active four-piece Resin set. |
| Can We Keep It? | Find a generated Nether fossil within four blocks while wearing an active four-piece Quartz set. Perhaps a little lost ghast needs a home? |

## Verification

Advancement JSON, parent links, translations and all goal destinations passed validation. Counter boundary calculations passed executable checks. Changed Java sources passed an isolated compile against the bundled Minecraft class signatures with dependency stubs. This checks source types, but does not validate Mixin application or replace a full Java 25 Gradle build and in-game test.

Build with ` .\gradlew.bat build ` and test in a disposable world before release. For cumulative tasks, check just below and at the threshold; repeat an action without the required set and confirm it does not increment. Test Ward once continuously and once with an interruption.
