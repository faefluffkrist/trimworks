package com.faefluffkrist.trimworks.configscreen;

final class BonusOptionInfo {
    record Info(String label, String explanation) {}
    static Info forField(String name) {
        return switch(name) {
            case "wardSwiftSneak" -> new Info("Sneaking speed", "Faster movement while sneaking; starts at two Ward pieces.");
            case "wardDarknessImmunity" -> new Info("Darkness immunity", "Clears Darkness while wearing four Ward patterns.");
            case "wardAncientCitySpeed" -> new Info("Ancient city speed", "Grants Speed on the configured deepslate/city block tag.");
            case "wardSneakTwoPieces" -> new Info("Sneaking speed: 2 pieces", "Flat addition to the sneaking-speed attribute. Default: 0.15.");
            case "wardSneakFourPieces" -> new Info("Sneaking speed: 4 pieces", "Flat addition to the sneaking-speed attribute. Default: 0.30.");
            case "wardSpeedLevel" -> new Info("Ancient city Speed level", "Status-effect level on matching terrain. Vanilla guide: II.");
            case "silenceWardenNeutrality" -> new Info("Warden neutrality", "Wardens ignore you until provoked; requires four pieces.");
            case "silenceSculkSpeed" -> new Info("Sculk movement bonus", "Grants Speed while standing on the configured sculk block tag.");
            case "silenceSpectralMark" -> new Info("Spectral mark", "Mobs you damage glow while four Silence patterns are equipped.");
            case "silenceSpeedLevel" -> new Info("Sculk Speed level", "Status-effect level on sculk terrain. Vanilla guide: II.");
            case "silenceMarkTicks" -> new Info("Glow duration", "Duration in game ticks; 20 ticks = 1 second.");
            case "boltMeleeKnockback" -> new Info("Melee knockback", "Adds a knockback impulse to melee attacks at two/four pieces.");
            case "boltProjectileDeflection" -> new Info("Arrow deflection", "Full set can reverse incoming arrows and take ownership.");
            case "boltProjectileDeflectionChance" -> new Info("Arrow deflection chance", "Percent of incoming arrow hits deflected. Range: 0–100%.");
            case "boltKnockbackTwoPieces" -> new Info("Melee knockback: 2 pieces", "Added knockback impulse with two or three Bolt patterns.");
            case "boltKnockbackFourPieces" -> new Info("Melee knockback: 4 pieces", "Added knockback impulse with four Bolt patterns.");
            case "coastConduitPower" -> new Info("Conduit Power", "Grants Conduit Power with four Coast patterns.");
            case "coastConduitLevel" -> new Info("Conduit Power level", "Usually level I; higher levels are allowed but may not scale.");
            case "tideDolphinsGrace" -> new Info("Dolphin’s Grace", "Grants Dolphin’s Grace with four Tide patterns.");
            case "tideGraceLevel" -> new Info("Dolphin’s Grace level", "Usually level I; higher levels are allowed but may not scale.");
            case "sentryIllagerNeutrality" -> new Info("Illager neutrality", "Illagers and ravagers ignore a full set unless provoked, stealing nearby, or in a raid. Evokers and vexes see through it.");
            case "sentryTheftProvocation" -> new Info("Nearby theft provokes illagers", "Opening a chest or barrel within 8 blocks horizontally and 4 vertically angers watching illagers.");
            case "sentryTheftWarning" -> new Info("Theft warning", "White action-bar warning for full Sentry wearers caught by one or two illagers. Five-minute cooldown.");
            case "sentryTheftSnitching" -> new Info("Lone witness reports theft", "A lone pillager searches 60 blocks for two illagers, with time and stuck limits. Runner and recruits gain a small rush bonus and Resistance II.");
            case "sentryGroupRetaliation" -> new Info("Illager group retaliation", "Attacks alert illagers within 15 horizontal and 6 vertical blocks of you. Theft witnesses do not spread anger.");
            case "sentryEvokerWarning" -> new Info("Evoker warning", "Warn when an evoker targets a full Sentry wearer. Five-minute cooldown per player.");
            case "sentryFriendsAndFoesNeutrality" -> new Info("Friends & Foes support", "Sentry neutrality also covers iceologers and illusioners. Requires Illager neutrality.");
            case "sentryTakesAPillageNeutrality" -> new Info("It Takes a Pillage support", "Sentry neutrality also covers archers, legioners and skirmishers. Requires Illager neutrality.");
            case "vexNeutrality" -> new Info("Vex neutrality", "Vexes ignore the full set unless you provoke them.");
            case "duneTerrainSpeed" -> new Info("Dune terrain bonus", "Grants Speed on the configured dune terrain block tag.");
            case "duneSpeedLevel" -> new Info("Dune Speed level", "Status-effect level on dune terrain. Vanilla guide: II.");
            case "wildTerrainSpeed" -> new Info("Wild terrain bonus", "Grants Speed on the configured wild terrain block tag.");
            case "wildSpeedLevel" -> new Info("Wild Speed level", "Status-effect level on wild terrain. Vanilla guide: II.");
            case "eyeTerrainSpeed" -> new Info("Stronghold/End terrain bonus", "Grants Speed on the configured stronghold/End block tag.");
            case "eyeSpeedLevel" -> new Info("Terrain Speed level", "Status-effect level on matching terrain. Vanilla guide: II.");
            case "eyeEndermanGazeImmunity" -> new Info("Enderman gaze immunity", "Looking at Endermen does not provoke them with four Eye trims.");
            case "snoutBruteHoglinNeutrality" -> new Info("Brute/Hoglin neutrality", "Piglin Brutes and Hoglins ignore the full set unless provoked.");
            case "ribWitherImmunity" -> new Info("Wither immunity", "Clears the Wither status effect with four Rib patterns.");
            case "goldPiglinNeutrality" -> new Info("Piglin neutrality", "Four Gold materials protect against Piglins; excludes Brutes/Hoglins.");
            case "amberSafeHoneyHarvest" -> new Info("Safe honey harvesting", "Safely harvest honey from Beehives & Bee Nests without requiring smoke.");
            case "amethystEnchantingBoost" -> new Info("Primary enchantment boost", "Four Amethyst materials change only the advertised table enchantment.");
            case "amethystLevelIncrease" -> new Info("Enchantment level change", "Levels added to the primary enchantment, including books. Default: +1.");
            case "quartzGhastNeutrality" -> new Info("Ghast neutrality", "Ghasts ignore four Quartz materials unless you provoke them.");
            case "netheriteFireproofPiece" -> new Info("Protect trimmed item", "Each Netherite-trimmed armor item is protected from burning in lava.");
            case "copperLightningResistance" -> new Info("Lightning resistance/attraction", "Four Copper materials reduce lightning damage and attract thunder strikes.");
            case "copperDamageResistancePercent" -> new Info("Lightning damage reduction", "Percent of lightning damage prevented; negative values increase damage.");
            case "copperLightningInterval" -> new Info("Lightning attraction interval", "Average interval in ticks during thunder. 6000 ticks = 5 minutes.");
            case "ironKnockbackResistance" -> new Info("Knockback resistance", "Enables the resistance attribute bonus with four Iron materials.");
            case "ironResistance" -> new Info("Resistance amount", "Flat attribute addition. 0.1 = 10%; 1 reaches full resistance.");
            case "redstoneMovementSpeed" -> new Info("Movement speed bonus", "Enables the speed attribute bonus with four Redstone materials.");
            case "redstoneSpeedPercent" -> new Info("Movement speed change", "Percent of base movement speed; a negative value slows the player.");
            case "lapisExperienceBoost" -> new Info("Experience modifier", "Modifies XP received from orbs with four Lapis materials.");
            case "lapisExperiencePercent" -> new Info("Experience gain change", "Bonus percent after Mending. Default: 10%; -100% removes all XP.");
            case "emeraldVillagerDiscount" -> new Info("Villager discount", "Four Emerald materials add reputation used to calculate trade prices.");
            case "emeraldReputation" -> new Info("Added trade reputation", "Reputation added for trade pricing. Negative values worsen prices.");
            case "diamondArmorToughness" -> new Info("Armor toughness", "Enables the toughness attribute bonus with four Diamond materials.");
            case "diamondToughness" -> new Info("Added armor toughness", "Flat armor-toughness addition. Default: +1; negative values reduce it.");
            default -> new Info(name, "Configurable bonus setting.");
        };
    }
}
