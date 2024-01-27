# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [7.0.1] - 2024-01-27
###Fixed
- Fixed crash on servers

## [7.0.0] - 2022-08-20
- Updated to 1.19.2
### Added
- Ore generation now uses the mojang datapack system.

## [6.3.0] - 2022-08-15
### Fixed
- Rearranged part of SH's networking, should be a bit more stable and fix some odd bugs. Testing needed.

## [6.2.1] - 2022-05-12
### Fixed
- Difficulty exempt now works properly instead of being inverted (fixes mob hp not scaling)
- Fix power crystal increasing difficulty based on wrong config (default 4)
- "Fixed" default max on power crystal damage increase from 10 to 50. By default it requires 100 crystals to get the max
- Fixed seemingly random crash on load.
### Added
- SH's additional natural regen will now no longer work if the vanilla natural regen gamerule is disabled.

## [6.2.0] - 2022-03-30
- Updated to Forge version 40.0.18+

## [6.1.0] - 2022-03-09
- Updated to 1.18.2
### Fixed
- Oregen on/off configs work again

## [6.0.0] - 2021-12-26
- Updated to 1.18
### Added
- Deepslate variations for heart and power crystal ore
- Deepslate versions spawn between -64 and 0, and should be as frequent as the stone ores of previous SH versions
- Stone versions of ore spawn between 0 and ~36 and are rarer than previous versions
### Fixed
- Heart/Power crystal animation and particles would play on the client even when the level requirement was not met.

## [5.0.4] - 2021-08-14
### Fixed
- Ores now mine faster with a pickaxe and require at least an iron one to drop

## [5.0.3] - 2021-08-10
### Fixed
- Scaling Health hearts now properly render

## [5.0.2] - 2021-08-09
### Fixed
- Health and difficulty resetting after death and return from end
- Health per levels can't be deactivated in the datapack (it was and still is de-activable from server-config)
- Fix potential crash on death with capabilities

## [5.0.1] - 2021-08-08
### Fixed
- Updated forge to 37.0.22 to fix a breaking change

## [5.0.0] - 2021-07-30
Updated to 1.17.1

## [4.1.5] - 2021-06-11
### Fixed
- Crash on dedicated server

## [4.1.4] - 2021-06-10
### Fixed
- Hearts from experience was updating incorrectly

## [4.1.3] - 2021-05-30
### Fixed
- Some mobs had NaN amount for modifiers.
 
## [4.1.2] - 2021-05-22
### Fixed
- Potion effects due to difficulty are no longer always active but properly based on the potion chance.
- Fixed huge random health bonus (still has a randomness on difficulty for now, between 95% and 105%)

## [4.1.1] - 2021-05-16
### Fixed
- Commands now work after /reload

## [4.1.0] - 2021-05-11
### Notes
- Previous configs will NOT work anymore and will have to be transferred over to being a datapack. Information about datapacks can be find on the minecraft wiki. For other SH specific inquiries with datapacks leave a comment on CF or join the discord
- This update is NOT network compatible, and will require both the client and the server to be up to date.
### Added
- Moved config options to the datapack system
- Changed a few default values, lowering difficulty overall by default
- Changed default value for xp level threshold before health gain (3 -> 10 levels)
- Added ukrainian translation, courtesy of github user Yuraplonka
### Fixed
- Fixed power crystal ore gen config option not working
- Fixed crash occurring when a creeper would explode with certain mods installed
- Fixed dimension/biome difficulty multiplier not working properly when the other list was empty
- Fixed some crashes/bugs occurring from modifying the config files

## [4.0.5] - 2021-03-15
### Fixed
- Chests loot tables are now exposed to be modified by datapacks. Requires Forge 36.0.14 and up.

## [4.0.4] - 2021-03-09
### Fixed
- "Tag used before it was bound" crashes (SilentChaos512)

## [4.0.3] - 2021-01-16
### Added
- Italian translations, credit of github user Giovxyz.

## [4.0.2] - 2021-01-04
### Fixed
- Reduced extra debug noise behind config master switch
- Register Configured Features, avoids potential problems with world gen with other mods.

##[4.0.1] - 2021-01-01
### Added
- Added a debug option in COMMON to turn on mob potion logging (now turned off by default). 
- Added a config option to turn off the purple blight flames. Updated to 1.16.4

## [4.0.0] - 2020-09-16
Updated to 1.16.3