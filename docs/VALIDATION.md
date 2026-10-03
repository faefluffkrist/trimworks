# Validation status

This archive contains updated source, not a newly compiled release JAR. Old build outputs are excluded to avoid accidentally installing an outdated mod.

A Gradle build was attempted here but could not download the Gradle distribution because the connection is blocked. The available Java compiler/runtime is version 17; the project requires a Java 25 JDK. The updated UI has not been launched in Minecraft. Minecraft API compatibility and rendered layout must be confirmed by a local build and game test.

Java syntax was parsed successfully for all 41 sources. A standalone test of the actual tooltip formatter passed content-preservation, six-word wrapping, indentation, numeric-row, and vanilla-isolation checks; its small Component stub does not validate Minecraft API compatibility. Static checks also cover unchanged main Java sources/config progression, complete option-description coverage, source encoding, icon identity, and representative screen layout bounds.

## Build

From the extracted project folder in Windows PowerShell:

```powershell
.\gradlew.bat clean build
```

The ordinary mod JAR should appear in `build\libs` as `trimworks-1.1.0.jar`. Do not install the sources JAR.

## UI checks

1. Check all three tabs at multiple GUI scales, including the smallest usual 320×240 scaled screen. Scroll each list with the wheel and drag its scrollbar.
2. Toggle a main trim, open its editor, and check each equipped-piece field has a label/explanation. Enter a blank or invalid value and verify Save reports an error.
3. Open the effect picker. Search `strength`, `minecraft:`, and a mod namespace. Select an effect, verify its registry ID is stored, and verify usual-level guidance appears. Higher levels should still save.
4. Add/remove effects, leave via Back, and check unsaved edits are discarded. Reset then Back must also discard the reset. Reopen after Save and check levels survive.
5. Open Bolt built-in settings and confirm every toggle and numerical field has an explanation. Set Arrow deflection chance to 40, save, and verify the config stores `0.4` and the tooltip shows 40%.
6. Open Raiser built-in settings and check the explicit empty message. Open Extra effects & attributes and check its empty message, then add Strength II and verify the bonus is configurable.
7. Check built-in/material category-wide toggles, per-option toggles, and per-extra-bonus toggles independently. Ensure disabled categories still allow configuration of their options.
8. Check Bolt, Ward, Gold, Resin, Amethyst, and Copper tooltips: descriptions wrap and indent while numerical rows stay readable. Check unrelated vanilla/other-mod tooltips remain unchanged.
9. Check the supplied icon appears in Mod Menu. Connect to a dedicated server and confirm synchronized settings are read-only and tooltips use server values.
10. Repeat the previous material/gameplay checks to confirm this client/UI update preserves behavior.

## Label fix verification

All manually drawn screen text now has full opacity. A regression test of the actual ConfigUi text helper confirmed it emits opaque colors for both RGB and ARGB inputs, using small Minecraft API stubs. All 41 Java sources passed syntax parsing. Gameplay/config source bytes and main resources match the newly supplied baseline. The screen has not been launched here, and a full Java 25/Gradle build still needs local verification.

Build version remains 1.1.0 and produces `trimworks-1.1.0.jar`; old build outputs are excluded from this source archive.


## Optional mob compatibility

See NATURALLY_TRIMMED.md for schema 11, API/policy checks, build instructions, and runtime test cases. The new integration has not been run in Minecraft in this environment.


## Advancements

See ADVANCEMENTS.md for the 73-entry catalog, persistent-stat counters, full-set eligibility, API/data/progression checks, and the manual runtime checklist. Java syntax parsing passed for all 56 sources; API checks and 33 progression assertions passed. Live Minecraft testing and a full Java 25 Gradle build remain required.
