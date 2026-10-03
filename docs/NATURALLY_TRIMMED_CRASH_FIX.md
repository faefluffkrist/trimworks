# Naturally Trimmed 3.6.1 spawning crash workaround

Trimworks supplies the missing class access permission for Minecraft 26.2's
EquipmentLayerRenderer$TrimSpriteKey. Naturally Trimmed 3.6.1 references this
class in TrimApplier.isValidTrim while assigning armor trims to spawned mobs.
Its active access-widener file omits the entry found in its bundled 26.1 file.

The workaround is registered in fabric.mod.json and Loom's build configuration.
It changes access to one vanilla class; it neither edits Naturally Trimmed nor
requires it to be installed. Version remains 1.1.1.

## Validation status

Static wiring and archive checks passed. A full build and Minecraft runtime
test were not available here. This addresses the reported IllegalAccessError,
not all possible Naturally Trimmed compatibility problems. Dedicated-server
compatibility is unverified: Naturally Trimmed references a client rendering
class from server-side spawning code, which this access change does not remove.

## Local verification

1. With Java 25, run `.\gradlew.bat clean build validateAccessWidener`.
2. Install the ordinary build/libs/trimworks-1.1.1.jar (not the sources JAR),
   replacing the previous Trimworks JAR. Leave Naturally Trimmed unchanged.
3. In a disposable singleplayer test world with both mods, repeat zombie
   summons and natural spawning. Also test skeleton spawning. Repeat enough
   times to exercise random trim assignment, and inspect trimmed armor.
4. Confirm Trimworks effects on trimmed mobs still follow its configuration.
5. Launch without Naturally Trimmed and check startup and normal gameplay.
6. Test dedicated-server startup and spawning separately. Report any new crash;
   do not assume a passing singleplayer test establishes server support.
