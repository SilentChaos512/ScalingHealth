# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.3.35] - 2019-05-19
### Added
- Config to add fixed values or multipliers to specific dimensions (Dimension Value Factor under difficulty category) [#154]
### Changed
- Copied es_VE.lang to other Spanish regions
### Fixed
- Difficulty meter not showing area difficulty values beyond the max

## [1.3.34] - 2019-04-08
Set minimum Forge version to 2779 to stop the crashes
### Added
- Config to set blights immune to suffocation damage (enabled by default)
- Config to override the sleep warning message with a custom one [#164]
- Health text style SOLID, which can be set to any color
### Fixed
- (Maybe) ConcurrentModificationException in [#162]

## [1.3.33] - 2019-02-17
### Added
- Config to disable heart color looping [#155]
- Config to scale player bonus regen with max health [#147]
### Fixed
- Crash when connecting to servers in some cases [#157]
- Heart container drops not respecting doMobLoot game rule [#156]
- Should fix damage scaling log spam with certain sources [#152] 

## [1.3.32] - 2019-01-07
### Added
- New config, Debug Overlay. Debug text will display if both this and Debug Mode are set to 'true'.
- Absorption heart color config

## [1.3.31] - 2018-11-04
### Added
- Wildcard support for all entity lists (like the blight blacklist). Wildcard character is asterisk (*). Wildcards are only allowed at the end of the entry. Examples: `modid:*` or `modid:something_*`.
- A separate pair of configs for adjusting absorption text position
### Changed
- Damage scaling events now have highest priority, should improve compatibility with some mods
### Fixed
- Damage scaling works with Draconic Evolution armor now, and should be compatible with most mods [Draconic Evolution #1207] (thanks Z-Tunic for testing this)

## [1.3.30] - 2018-10-13
### Added
- Absorption text, similar to the health text displayed to the left of hearts. Cannot display a max amount, as vanilla does not have any concept of max absorption. The GREEN_TO_RED color style will not work correctly because of this.
- Configs to enable/disable healing events for items. These are now enabled by default (they were disabled for some reason, disable if you have issues)
- Track number of heart containers used in statistics (unable to do other items at this time)
### Fixed
- A possible (rare?) crash when joining a LAN game or server [#141]

2018-10-13: Changing style, renamed from changelog.txt to CHANGELOG.md. Remainder of file is left unchanged.

1.3.29
Added: Configs to tweak health text position (#133)
Added: Sound events for all heart items (for resource pack makers, the default sounds are the same)
Fixed: Blight fires being too dark
Fixed: Mobs continuously redoing difficulty logic when health increases are disabled and difficulty is not
Fixed: Commands not working in LAN games
Fixed: Error when loading world data

1.3.28
Partial config code rewrite. Shouldn't change anything, but let me know if anything seems broken.
Added: Configs to restrict bonus regen by player health. For example, you could set players to only regen up to a certain number of hearts. Or not regen below a certain number if you're feeling evil.
Fixed: Blight death messages being broken (does not completely translate however, working on it)
Fixed: Blight fires spawning infinitely in some cases

1.3.27
Update difficulty meter textures (Z-Tunic)
Added: Config to change absorption heart style. Options are SHIELD, GOLD_OUTLINE, and VANILLA (#115)
Fixed: Hearts and absorption hearts rendering incorrectly in hardcore mode (#132)
Fixed: Sleep warning never showing

1.3.26
Updated heart textures (Z-Tunic)

1.3.25
Changed: The way hearts render when poisoned/withered
Fixed: Some items missing models (#128)
Fixed: Last heart outline missing or in wrong position (#129)
Fixed: Some issues with localizations

1.3.24
Updated es_VE.lang (Allinxter_910)
Added: pt_BR.lang (Diego Ramos)
Added: Config to disable vanilla hearts row and use only custom hearts (#123)
Added: Entity properties for loot tables, scalinghealth:is_blight and scalinghealth:difficulty. See wiki for details (#106)
Added: Config to control blight difficulty multiplier (finally)
Changed: Cap on regen food values removed (#125)

1.3.23
Changed: XP health bonuses can now be used with heart containers (#119)
Changed: Tweaked absorption display textures (#115)
Fixed: Armor bar being too high up when you have more than one row of absorption (#115)

1.3.22
More work on absorption overlay (#115)
Added: Config to set player max health based on their XP level (#119)

1.3.21
Added: Config to set difficulty level with Game Stages. You can set a specific difficulty level for any number of stages, and the largest is chosen. Overrides any other changes to difficulty.
Added: Absorption override, WIP (#115)
Fixed: Medkit recipe missing (changed it slightly because JSON)

1.3.20
Refactoring stuff to prepare for 1.13.
Changed: Max health cap is now changed in main.cfg.
Changed: No longer a coremod, found a better solution.
Changed: Switched to JSON recipes

1.3.19
Fixed (maybe): More unkillable mobs (#117)

1.3.18
Fixed: Mobs being unkillable if max difficulty is zero (#116)

1.3.17
Added: Config to blacklist/whitelist specific mobs with the "Always Blight" mode (#102)
Changed: Revert health text being disabled when custom heart rendering is disabled (#113)
Changed: Idle multiplier now accepts negative values (#112)
Fixed: The "get" commands not checking the right player (#111)
Fixed: Damage scaling sometimes applying infinite damage to the player, resulting in the player being unkillable (#110)

1.3.16
Added: Configs to apply the new damage scaling system to mobs (#99)
Changed: Commands have been reworked! You can now specify whether you want to get/set/add/sub the value. Fully supports the "@" notation now, you can even affect multiple players with one command (in theory). And tab completion works better. (#107, #72)
Fixed: Kill command having some strange effects if outOfWorld damage is scaled (#110)

1.3.15
Added: Configs to scale damage taken with either max health or player/area difficulty. How steeply damage amounts scale can be set for each damage source. You can add mod sources to the list too, you just need to know their damage type string (trying enabling debug mode, get hurt by it, then check the log). Check your spelling, there's no way for me to detect a source that doesn't exist. (#99)
Added: Config to make every mob spawn as a blight (be careful with that) (#102)
Changed: Disabling custom heart rendering will now override and disable health text settings (#104)
Changed: Blight speed/strength amplifiers can now be set to -1 to disable them entirely.
Changed: Increase default difficulty search radius to 256.
Fixed: Mobs not receiving extra attack damage or potion effects if their health modifier is very high. This removes the subtractions of difficulty as stats are selected. This may result in harder hitting mobs, even with default settings. (#100)
Fixed (maybe): Desync of health on server when Scaling Health is not allowed to modify health (#104)

1.3.14
Added: Localization key for difficulty meter text.
Fixed: Distance calculations overflowing, resulting in unkillable entities with NaN max health. Bugged entities should fix themselves when you get close to them. (#89)
Fixed: Returning from the End through the portal counting as "dying", potentially resulting in changes to health or difficulty. (#91)
Fixed: Server crash when lunar phases are enabled (#97)

1.3.13
Fixed: Server crash

1.3.12
Added: Config to add difficulty multipliers for different moon phases. Category is difficulty/lunar_phases in the config file. This feature is disabled by default.
Added: Config for XP cost to use heart containers (default is 3 levels)
Added: An outline on your last (highest) heart, if you have more than 10. Can be disabled or changed to any color (default is white)
Added: Config for food consumption (exhaustion) of bonus player regen. Value also increased. Default is 0.1 (still many times less than vanilla regen), old value was 0.025.
Added: Particle effects for heart container and cursed/enchanted hearts (#57)
Added: Sound effects for cursed/enchanted hearts (#57)
Added: More tooltips!
Added: ???
Fixed: All rows of hearts drawing, instead of just the top two (FPS boost for players with lots of hearts)
Fixed: Some other minor heart rendering issues

1.3.11
Added: New area difficulty mode, SERVER_WIDE. A difficulty value is now tracked for the world and will be used if this new area mode is selected. Basically, this makes the difficulty system work more like Difficulty Life did. (#83)
Added: Max damage bonus config, which let's you cap the extra attack damage mobs get (#78)
Changed: Default blight speed and strength amplifiers reduced to 4 and 1 respectively. Your config will NOT update on its own. Change it if you want to.
Fixed: Disabling difficulty with the new game rule making mobs unkillable (#86)

1.3.10
Added: Config to toggle all bosses receiving extra health (#79)
Added: Configs to blacklist all hostile or all passive mobs from becoming blights. Passives are blacklisted by default. (#79)
Added: A game rule (ScalingHealthDiffficulty) to enable/disable the difficulty system. When disabled, mobs will no longer gain extra health and damage, regardless of any other settings. Players will also stop accumulating difficulty, but their current value will remain unchanged. (#80)

1.3.9
Added: A config to exempt specific players from gaining difficulty. Add the name of each player (not case sensitive) who does not want to have the difficulty system to the list. Exempt players are still part of difficulty calculations, but they count as having zero difficulty. (#71)
Added: A minimum difficulty config, defaults to 0.
Fixed: Negative values not working with difficulty per block config (#75)

1.3.8
Fixed: Difficulty not increasing when sleeping using Morpheus (#60)
Fixed: Blight equipment receiving an unlimited number of enchantments (#70)

1.3.7
Added: Debug mode will now log all spawns.
Fixed (maybe): Some entities (notably Lycanites) not receiving extra health/damage (#64)
Fixed: New difficulty changer items appearing in all creative tabs (#67)

1.3.6
Added: Items that change difficulty (Cursed Heart and Enchanted Heart). They still need some polishing, but are functional. No recipes are included, you will need to make your own. (#57)
Added: Scoreboard criteria for difficulty (scalinghealth:difficulty) (#62)
Changed: Slimes (including magma cubes and probably most modded slimes) now drop far fewer hearts.
Fixed: Entity lists not matching mod entities (#61)

1.3.5
Added: Difficulty per blight kill config (how did I miss that?)
Added: Difficulty per peaceful kill config
Added: Config to set difficulty changes by mob (difficulty/Difficulty Per Kill By Mob) (#57)
Added: Config to set difficulty multipliers in certain dimensions (difficulty/Difficulty Dimension Multiplier) (#57)
Fixed (probably): Entity blacklist causing crashes in some cases (#58)

1.3.4
Fixed: The heart crystal shards -> heart container recipe not working in Forge 2552+ (#55)

1.3.3
Added: Config to add additional XP to all mobs. Default gives an extra 1% per difficulty. Only works on mobs spawned since this update. (#51)
Added: Config to tweak potions applied to non-blights. You can add/remove effects and specify the minimum difficulty requirement and potion level. This is under the new mob/potion category. If changed in-game, you must restart Minecraft. (#52)
Added: Config to add difficulty to players when sleeping through the night (#53)

1.3.2
Added: Config to replace heart container drops with heart crystal shards.
Added: Config to set blight potion duration. This is just to customize the duration of the lingering effects from blight creeper explosions. Blights will still refresh their potions infinitely. Set to -1 if you want infinite duration from lingering effects.

1.3.1
Added: Config to allow/prevent fake players from getting heart container drops (main/fake_players/Can Generate Hearts)
Added: Config to allow/prevent fake players from accumulating difficulty (main/fake_players/Have Difficulty) (WIP on #48, doesn't really do anything of note right now)
Added: Config to blacklist mob extra health by dimension (main/mob/health/Dimension Blacklist) (#37)
Added: Config for difficulty change on boss kills (main/difficulty/Difficulty Per Boss Kill) (#42)
Added: Config to blacklist all bosses from becoming blights (main/mob/blights/Blacklist All Bosses) (#46)
Added: Color setting for the new health text. GREEN_TO_RED mode (default) displays green at full health and moves to red as you take damage. WHITE is just white. PSYCHEDELIC lets you taste the rainbow.
Changed: Entity blacklists now check for registry names, but the old entity ID's will still match too (#46)

1.3.0
Requires Silent Lib 2.2.9. Cuts support for Minecraft 1.10.2 and 1.11.2.
Added: Bandages and medkits. These items will restore a percentage of your health over a set amount of time. Bandages restore 30% over 60 seconds, and medkits restore 70% over 35 seconds. Movement speed is reduced by 25% while in effect.
Added: Heart dust. Crafted from a heart container and used to craft bandages and medkits. You get 24 dust from each heart, but you can't craft them back.
Added: A small bit of text to the left of hearts. By default (ROWS mode), this will show how many rows of hearts (full and partial) you currently have, but does nothing to indicate your max health. Change to the HEALTH_AND_MAX mode to make the text display your current and max health values (the actual numbers Minecraft stores, so it's in half-hearts). Set to DISABLED to remove the text.
Added: Debug mode now shows all modifiers being applied to your health. If your health isn't coming out to the number you think it should, this might help you figure out which mod(s) are changing it.

1.2.2
Update version range to include 1.12.2.
Fixed: A crash with Metamorph (possibly other unreported crashes?)

1.2.0
Added: Difficulty meter now shows an actual number for the area difficulty.
Added: Config settings for blight equipment (new category: mob/blights/equipment)
Added: Any mob can now receive a single, beneficial potion effect. Chance is configurable, lasts just 10 minutes. It's nothing too crazy.
Added: An API for modders to work with.
Added (API): Getters for area/player difficulty. A method to add difficulty to the player.
Added (API): Method to add equipment that blights can spawn with. Blights can now receive items for their main/offhands, in addition to armor.
Added (API): Blight spawn events (Pre and Post phases)
Changed: Made blight fires a little bit bigger.
Changed: Blight potion effects now last only a short time, and are refreshed at regular intervals (fixes an issue with blight creepers)
Fixed: [1.11.2, 1.12.x] Blights not spawning with armor *facepalms*
Fixed: Difficulty meter not rendering when InGame Info XML is installed (#24)

1.1.3
Marked compatible with 1.12.1

1.1.2
Added: Configs that can be used to reset players' health and/or difficulty at regular intervals. You can set the resets to occur daily, weekly (specifying the weekday of your choice), or monthly.
Added: es_VE.lang (couresy of Dorzar)

1.1.1
Added: Heart containers now heal (configurable, defaults to 4 (2 hearts), set to 0 to disable)
Added: Config to disable max health increases from heart containers, allowing them to be used exclusively as healing items.
Added: Pet regen. Tamed animals will slowly recover health over time. Set the config to 0 to disable.
Fixed: The config for notifying players when blights die not being loaded (it wasn't even in the file).

1.1.0
Updated for Silent Lib 2.2.5 (adds MC 1.10.2 and 1.11.2 compatibility)

1.0.11
Silent Lib 2.2.4 (Forge 2387)

1.0.10
Updated for Silent Lib 2.2.3 and Forge 2373
Fixed: Blights not dropping hearts when killed by pets.

1.0.9
Changed: Heart container texture, courtesy of spawnus_

1.0.8
Forge 2340
Removed: A debug line I accidentally left in.
Fixed: Heart crystal ore not being in any creative tab.

1.0.7
Added: A config to control how many heart crystal shards drop from each ore. Note that fortune can double the base amount.
Changed: Many difficulty configs can now be set to negative values, allowing far greater control over the difficulty system!
Behind the scenes: Updated the config class to the Silent Lib "AdaptiveConfig". Currently this is not being used for anything, but your config file will now track the last build you used.

1.0.6
Added: Config to lose difficulty on death. Set to a negative number if you want to GAIN difficulty on death. Defaults to 0.
Added: Separate config for hearts dropped by passive mobs (defaults to 0.1%, hostile rate is 1%).
Added: Difficulty per kill config. Defaults to 0, set it to something else to increase difficulty for every hostile mob killed.

1.0.5
Added: Config option for damage scaling on mobs (#20)
Added: Config options for position of difficulty meter. Negative numbers will place it some distance from the right/bottom edge of the screen. (#19)
Added: Config options to add extra heart crystal ore veins based on distance from spawn (#16)
Added: Keybindings to show/hide difficulty meter (#15)

1.0.4-44
Fixed: Heart containers not being consumed when used on 1.10.2.

1.0.4-42
Fixed: Heart containers not functioning on 1.10.2.

1.0.4-41
First XCompat build released
Added: Some special blight death messages for comedic effect.

1.0.3
Added: A config to specify the amount of health lost when a player dies (defaults to 0). The old "Lose Health on Death" config is no longer used.

1.0.2
Added: "Distance and Time" difficulty mode: adds together the "Weighted Average" and "Distance from Spawn" modes. You may need to adjust some other values in the config to make the increases manageable.
Fixed (1.11 only): Textures not loading.

1.0.1
Added: Configs for difficulty meter (under the client category)
Fixed: Difficulty meter displaying at all times... did that for debugging and forgot to remove it.

1.0.0
Added: A config option to disable blight death messages... What, you don't like seeing "Blight Squid drowned" for the 50th time?
Changed: Improved heart rendering a bit
Fixed: A bug where the config file would fail to load if the length of the heart colors list was changed.

0.2.7
Added: Config option to replace blights being on fire with a particle effect (enabled by default). This is more reliable, as it works on fire-proof mobs. Current effect likely needs work...
Added: Additional configs for blights (fire resistance, invisibility, supercharged creepers).
Changed: Default hearts dropped by blights to 0-2 (up from 0-1, update your configs if desired)

0.2.6
Still attempting to solve a weird crash (see issue #9). Additional info should be dumped to the log.
Added: Blights drop extra XP now (configurable, default 10x).
Changed: Default hearts dropped by blights to 0-1 (down from 1-2). Update your config files!

0.2.5
Added: A config to disable modification of player health. Set to false if you want another mod to handle player health.
Fixed: A server crash (java.lang.NoClassDefFoundError: net/silentchaos512/scalinghealth/client/HeartDisplayHandler)
Fixed: Heart color configs not being padded with 0's. This had no effect on the behavior of the configs, but could be confusing to users.

0.2.4
Added: A blacklist for extra health and a blacklist for blights. Note that if a mob is blacklisted for extra health, it cannot become a blight.
Added: Config to specify extra heart colors.
Added: Death message when blights are killed by something other than a player.
Fixed: 'Allow Hostile Extra Health' and 'Allow Peaceful Extra Health' configs not working.

0.2.3
Added: All players are now alerted when a blight is killed.
Added: Blights now drop heart canisters when killed (1-2 by default)
Changed: Blights while now reignite after a few seconds if their fire goes out.
Changed: Difficulty bar now displays both area and player difficulty (the lower quarter of the bar is player difficulty)
Fixed: A probable crash with the difficulty bar, if max difficulty is zero.
Fixed: Config option to render the difficulty bar being ignored.

0.2.2
Added: A difficulty bar. Displays for a short time occasionally. Feedback appreciated.
Fixed: Health modifiers being wrong when other modifiers are already applied.
Fixed: Heart rendering when player has regen.

0.2.1
Added: Heart Crystal Ore. Drops shards that can be used to craft heart containers...
Added: Group difficulty bonus. The area difficulty increases a bit more for each additional player in the area (configurable)
Added: Idle difficulty increase multiplier. By default, difficulty increase per second is slower when the player is not moving. Set it to a number greater than one is you want faster increase when idle.
Fixed: Player's health displaying incorrectly after traveling between dimensions.
Fixed: Hearts not shaking on low health.
Fixed: Mobs with low health getting a larger boost on "MULTI" health scaling modes.

0.2.0
WARNING: A new save system has been implemented! It should be much more reliable, but your current health and difficulty will reset to default values. Use the "/scalinghealth" command to correct that. Deleting your current main.cfg config file is not required, but recommended.
Added: A player-based difficulty system. Each player will track a difficulty level. Should be good for servers.
Added: Area difficulty modes. When a mob spawns, an "area difficulty" value is calculated. This has several configurable modes, most of which will consider all nearby players to determine the difficulty. There are also options to use distance from the world spawn or origin. By default, a weighted average is used, with the weights being based on the distance of each player from the mob. In other words, the difficulty of closer players has a bigger impact.
Changed: Difficulty per tick changed to per second. Should make the values easier for some people to understand (no e-notation in the default value), and help with rounding errors. To get the value you want, try 'max_difficulty / (hours_to_max_difficulty * 3600)'.
Fixed: Heart containers being consumed at max health.
Possibly Fixed: Some problems with health getting reset. Not tested, but some or all of these issues may have been fixed by the new save system.

0.1.3
Added: Multiple health scaling modes. Defaults to MULTI_HALF.
        - ADD: The old method, adds a flat value based on difficulty.
        - MULTI: Applies a multiplier to health instead, so health increases are proportional to base max health.
        - MULTI_HALF: Same as MULTI, but the multiplier for higher health mobs is lower.
        - MULTI_QUARTER: Same as MULTI_HALF, but the multiplier reduction is even greater (max health is lower than MULTI_HALF and MULTI)
Added: A way to change the hard-coded max health limit (uses ASM, I don't think there's any other way)
Added: An ASM config file that has an option to change the max health cap (config/scalinghealth/asm.cfg). Defaults to 2048, twice the vanilla cap.
Added: A ding sound plays when using a heart container.
Fixed: Some mobs not being healed to the correct health. For real this time!
Fixed: The health modifier for mobs being calculated wrong if other modifiers are present.

0.1.2
Added: Custom health rendering now implemented.
Added: Some attribute modifier debug info. Displayed through WIT (not a dependency)
Changed: Main config file moved to its own folder (config/scalinghealth/main.cfg).
Fixed: Some mobs not having current health set correctly on spawn.
Fixed: Mob attack damage modifiers not being right...
Fixed: Heart Container model not loading.

0.1.1
Changed the way attribute modifiers are applied.

0.1.0
Initial alpha release.
