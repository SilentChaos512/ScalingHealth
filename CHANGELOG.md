# Changelog

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.3.5] - 2020-01-09
Mostly a debug update, trying to get things working
### Added
- Config options

## [2.3.4] - 2020-01-07
### Added
- Lucky Heart
- Blacklist mob config
- Explanation for the different difficulty types and mob hp configs
### Fixed
- Hearts lost on death
- Certain difficulty types now work properly

## [2.3.3] - 2019-10-06
### Added
- Config to disable heart tank icons

## [2.3.2] - 2019-09-10
### Fixed
- Peaceful mobs being identified as hostile (wrong effect chances)

## [Unreleased]
### Fixed
- Cursed heart and enchanted heart not appearing in creative menu or JEI [#191]

## [2.3.1] - 2019-08-06
- Updated for Forge 28.0.45
### Added
- New WIP HUD element, "heart tanks". This is a row of icons above your hearts which will display how many rows of hearts. No configs for this at the moment, just needed to get this update out.

## [2.3.0] - 2019-07-23
- Updated for 1.14.4

## [2.2.1] - 2019-07-15
### Fixed
- Player starting health not working [#180]
- Crash when dropping items
- Crash when joining a world
- Various other minor fixes

## [2.2.0] - 2019-07-03
Updated for 1.14.3
### Fixed
- Ore loot tables
- `scalinghealth:mob_properties` condition requiring both min and max difficulty (both are optional)

## [2.1.0]
Port to 1.14.2
Update zh_cn translation (XuyuEre)
### Added
- Sleep configs are back

## [2.0.4] - 2019-05-01
### Added
- Location multiplier configs. Allows difficulty to be increased/decreased in different dimensions and biomes.
- Lunar cycle configs are back

## [2.0.3] - 2019-04-12
### Added
- Blight potion configs (found in dimension config files)
- Player bonus regen configs
### Fixed
- Blight fires

## [2.0.2] - 2019-03-12
### Added
- `sh_summon` command - Summons entities with a given difficulty and blight status, along with everything the vanilla summon command offers.
### Fixed
- Maybe reduce the chance of capability-related crashes

## [2.0.1] - 2019-03-02
Disabled recipe and model generators... oops. You can delete the "output" folder in your ".minecraft" folder. Sorry about that.
### Added
- Russian translation (by Voknehzyr)
