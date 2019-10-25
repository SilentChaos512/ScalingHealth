/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.config;

import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.silentchaos512.lib.config.ConfigBaseNew;
import net.silentchaos512.lib.config.ConfigMultiValueLineParser;
import net.silentchaos512.lib.config.ConfigOption;
import net.silentchaos512.lib.event.Greetings;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.lib.util.I18nHelper;
import net.silentchaos512.lib.util.LogHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.client.HeartDisplayHandler;
import net.silentchaos512.scalinghealth.event.DamageScaling;
import net.silentchaos512.scalinghealth.lib.EnumAreaDifficultyMode;
import net.silentchaos512.scalinghealth.lib.EnumHealthModMode;
import net.silentchaos512.scalinghealth.lib.EnumResetTime;
import net.silentchaos512.scalinghealth.lib.SimpleExpression;
import net.silentchaos512.scalinghealth.lib.module.ModuleAprilTricks;
import net.silentchaos512.scalinghealth.utils.EntityDifficultyChangeList;
import net.silentchaos512.scalinghealth.utils.EntityMatchList;
import net.silentchaos512.scalinghealth.utils.PlayerMatchList;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Major cleanup needed!
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class Config extends ConfigBaseNew {
    public static final class Debug {
        @ConfigOption(name = "Debug Mode", category = CAT_DEBUG)
        @ConfigOption.BooleanDefault(false)
        @ConfigOption.Comment("Master switch for the other debug configs.")
        public static boolean debugMode;

        @ConfigOption(name = "Debug Overlay", category = CAT_DEBUG)
        @ConfigOption.BooleanDefault(true)
        @ConfigOption.Comment("Draws information related to the mod on-screen, including health modifiers," +
                " difficulty data, and more. This is intended for testing purposes only, not normal gameplay.")
        public static boolean debugOverlay;

        @ConfigOption(name = "Log Spawns", category = CAT_DEBUG)
        @ConfigOption.BooleanDefault(false)
        @ConfigOption.Comment("If debug mode is on, this will log details on mob spawns. This may slow down your game.")
        public static boolean logSpawns;

        @ConfigOption(name = "Log Player Damage", category = CAT_DEBUG)
        @ConfigOption.BooleanDefault(true)
        @ConfigOption.Comment("If debug mode is on, this will log details of damage done to players.")
        public static boolean logPlayerDamage;
    }

    // TODO: 1.13 - Split client category into client.hearts and client.difficulty
    public static final class Client {
        @ConfigOption(name = "Enable WIT Support", category = CAT_CLIENT_WIT)
        @ConfigOption.Comment("If true, additional information on entities will be added to WIT. Disable this if" +
                " another mod is using the \"wit\" mod ID and causing the game to crash as a result.")
        @ConfigOption.BooleanDefault(true)
        public static boolean enableWitSupport;

        public static final class Hearts {
            @ConfigOption(name = "Custom Heart Rendering", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Replaces vanilla heart rendering (regular and absorption)")
            public static boolean customHeartRendering;

            @ConfigOption(name = "Replace Vanilla Heart Row With Custom", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If true, replaces the vanilla hearts with Scaling Health's hearts. Otherwise," +
                    " regular vanilla hearts are rendered first, then custom hearts are used for extra health.")
            public static boolean replaceVanillaRow;

            @ConfigOption(name = "Text Offset X", category = CAT_CLIENT)
            @ConfigOption.RangeInt(0)
            @ConfigOption.Comment("Offset the position of health text.")
            public static int textOffsetX;
            @ConfigOption(name = "Text Offset Y", category = CAT_CLIENT)
            @ConfigOption.RangeInt(0)
            @ConfigOption.Comment("Offset the position of health text.")
            public static int textOffsetY;

            @ConfigOption(name = "Text Offset Absorption X", category = CAT_CLIENT)
            @ConfigOption.RangeInt(0)
            @ConfigOption.Comment("Offset the position of the absorption text")
            public static int absorbTextOffsetX;
            @ConfigOption(name = "Text Offset Absorption Y", category = CAT_CLIENT)
            @ConfigOption.RangeInt(0)
            @ConfigOption.Comment("Offset the position of the absorption text")
            public static int absorbTextOffsetY;

            public static HeartDisplayHandler.TextStyle textStyle;
            public static HeartDisplayHandler.TextColor textColor;
            public static int textSolidColor;
            public static HeartDisplayHandler.AbsorptionHeartStyle absorptionStyle;
            public static HeartDisplayHandler.TextStyle absorbTextStyle;
            public static HeartDisplayHandler.TextColor absorbTextColor;

            @ConfigOption(name = "Last Heart Outline Enabled", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Outline your highest (max health) heart in a different color. This makes seeing your" +
                    " max health a little bit easier.")
            public static boolean lastHeartOutline;

            @ConfigOption(name = "Last Heart Outline Color", category = CAT_CLIENT)
            @ConfigOption.RangeInt(value = 0xFFFFFF, min = 0, max = 0xFFFFFF)
            @ConfigOption.Comment("The color of the last heart outline (default value). Due to an oversight, this ended" +
                    " up as a decimal number. Oops.")
            public static int lastHeartOutlineColor;

            @ConfigOption(name = "Color Looping", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If true, heart colors will 'loop around' to the first color after going through the" +
                    " entire list. Set false to have every row after the last have the same color.")
            public static boolean heartColorLooping;

            @SuppressWarnings("MagicNumber")
            public static int[] defaultHeartColors = {
                    0xBF0000, // 0 red
                    0xE66000, // 25 orange-red
                    0xE69900, // 40 orange
                    0xE6D300, // 55 yellow
                    0x99E600, // 80 lime
                    0x4CE600, // 100 green
                    0x00E699, // 160 teal
                    0x00E6E6, // 180 aqua
                    0x0099E6, // 200 sky blue
                    0x0000E6, // 240 blue
                    0x9900E6, // 280 dark purple
                    0xD580FF, // 280 light purple
                    0x8C8C8C, // 0 gray
                    0xE6E6E6  // 0 white
            };
            public static int[] heartColors = new int[0];
            public static int[] absorptionHeartColors = new int[0];
        }

        public static final class Difficulty {
            @ConfigOption(name = "Render Difficulty Meter", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Show the difficulty meter. Usually, it is display for a few seconds occasionally." +
                    " If false, it is never shown.")
            public static boolean renderMeter;

            @ConfigOption(name = "Render Difficulty Meter Always", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("Render the difficulty meter at all times. If false, it displays occasionally.")
            public static boolean renderMeterAlways;

            @ConfigOption(name = "Difficulty Meter Display Time", category = CAT_CLIENT)
            @ConfigOption.RangeInt(value = 160, min = 0)
            @ConfigOption.Comment("The time (in ticks) to show the difficulty meter whenever it pops up.")
            public static int meterDisplayTime;

            @ConfigOption(name = "Position X", category = CAT_CLIENT)
            @ConfigOption.RangeInt(5)
            @ConfigOption.Comment("Sets position of the difficulty meter. Negative numbers anchor it to the right side of the screen.")
            public static int meterPosX;
            @ConfigOption(name = "Position Y", category = CAT_CLIENT)
            @ConfigOption.RangeInt(-30)
            @ConfigOption.Comment("Sets position of the difficulty meter. Negative numbers anchor it to the bottom of the screen.")
            public static int meterPosY;

            @ConfigOption(name = "Warn When Sleeping", category = CAT_CLIENT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If difficulty is set to change when the player sleeps, they will be warned when they get in bed.")
            public static boolean warnWhenSleeping;

            public static String sleepMessageOverride;
        }
    }

    public static final class Player {
        public static final class Health {
            @ConfigOption(name = "Allow Modified Health", category = CAT_PLAYER_HEALTH)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Allow Scaling Health to modify the player's health. Scaling Health's changes are" +
                    " often compatible with other mods, assuming they use Minecraft's attribute system. If set to" +
                    " false, heart containers and the '/scalinghealth health' command will not work.")
            public static boolean allowModify;

            @ConfigOption(name = "Starting Health", category = CAT_PLAYER_HEALTH)
            @ConfigOption.RangeInt(value = 20, min = 2)
            @ConfigOption.Comment("The amount of health (in half hearts) players will start with when first joining" +
                    " the world. Vanilla is 20.")
            public static int startingHealth;

            // TODO: 1.13 - should change to "max heart containers?" Current name is somewhat misleading
            @ConfigOption(name = "Max Health", category = CAT_PLAYER_HEALTH)
            @ConfigOption.RangeInt(value = 0, min = 0)
            @ConfigOption.Comment("The maximum amount of health (in half hearts) a player can achieve with heart" +
                    " containers alone. Zero means unlimited. NOTE: Players must still obey Minecraft's max health" +
                    " cap. You can change that value with the \"Max Health Cap\" setting under the main category.")
            public static int maxHealth;

            @ConfigOption(name = "Min Health", category = CAT_PLAYER_HEALTH)
            @ConfigOption.RangeInt(value = 2, min = 2)
            @ConfigOption.Comment("The minimum amount of health (in half hearts) a player can have. This is different from starting health.")
            public static int minHealth;

            // TODO: 1.13 - should be "change on death" for consistency
            @ConfigOption(name = "Health Lost On Death", category = CAT_PLAYER_HEALTH)
            @ConfigOption.RangeInt(0)
            @ConfigOption.Comment("The amount of health (in half hearts) a player will lose each time they die. Set" +
                    " to a negative number to cause players to gain health instead.")
            public static int lostOnDeath;

            public static EnumResetTime resetTime = EnumResetTime.NONE;
            public static Map<Integer, Integer> byXP = new HashMap<>();
        }

        public static final class BonusRegen {
            @ConfigOption(name = "Enable Bonus Regen", category = CAT_PLAYER_REGEN)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Enable bonus health regen for players. Vanilla regen is not changed in any way," +
                    " this just adds extra healing! Vanilla regen can be disabled with the naturalRegeneration gamerule.")
            public static boolean enabled;

            @ConfigOption(name = "Scale With Max Health", category = CAT_PLAYER_REGEN)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If enabled, regen will be proportional to max health")
            public static boolean scaleWithMaxHealth;

            @ConfigOption(name = "Food Min", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = 10, min = 0)
            @ConfigOption.Comment("The minimum food level at which bonus regen will be active (vanilla max food is 20).")
            public static int minFood;

            @ConfigOption(name = "Food Max", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = Integer.MAX_VALUE, min = 0)
            @ConfigOption.Comment("The maximum food level at which bonus regen will be active (vanilla max food is 20).")
            public static int maxFood;

            @ConfigOption(name = "Health Min", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = 0, min = 0)
            @ConfigOption.Comment("Bonus regen will stop when players have this much health or less.")
            public static int minHealth;

            @ConfigOption(name = "Health Max", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = Integer.MAX_VALUE, min = 0)
            @ConfigOption.Comment("Bonus regen will stop when players have this much health or more.")
            public static int maxHealth;

            @ConfigOption(name = "Delay (Initial)", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = 400, min = 0)
            @ConfigOption.Comment("The amount of time (in ticks) after being hurt before bonus regen activates.")
            public static int initialDelay;

            @ConfigOption(name = "Delay", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeInt(value = 100, min = 0)
            @ConfigOption.Comment("The amount of time (in ticks) between each bonus regen tick (a half heart being healed).")
            public static int delay;

            @ConfigOption(name = "Exhaustion Added", category = CAT_PLAYER_REGEN)
            @ConfigOption.RangeFloat(value = 0.1f, min = 0f, max = 1f)
            @ConfigOption.Comment("The food consumption for each bonus regen tick.")
            public static float exhaustion;
        }
    }

    public static final class FakePlayer {
        // Fake Players
        @ConfigOption(name = "Can Generate Hearts", category = CAT_FAKE_PLAYER)
        @ConfigOption.BooleanDefault(true)
        @ConfigOption.Comment("If enabled, fake players will be able to get heart container drops when killing mobs." +
                " Disabling should prevent at least some mob grinders from getting heart drops.")
        public static boolean generateHearts = true;
        public static boolean haveDifficulty = false; // Probably does not work?
    }

    public static final class Mob {
        // Mobs
        @ConfigOption(name = "Damage Modifier", category = CAT_MOB)
        @ConfigOption.RangeFloat(value = 0.1f, min = 0)
        @ConfigOption.Comment("A multiplier for extra attack strength all mobs will receive. Set to 0 to disable extra" +
                " attack strength.")
        public static float damageMultiplier;
        @ConfigOption(name = "Max Damage Bonus", category = CAT_MOB)
        @ConfigOption.RangeFloat(value = 10, min = 0, max = 1000)
        @ConfigOption.Comment("The maximum extra attack damage a mob can receive. Zero means unlimited.")
        public static float maxDamageBoost;
        @ConfigOption(name = "Potion Chance (Hostiles)", category = CAT_MOB)
        @ConfigOption.RangeFloat(value = 0.375f, min = 0, max = 1)
        @ConfigOption.Comment("The chance that an extra potion effect will be applied to any hostile mob. Note that"
                + " this effect requires the mob to have a certain amount of \"difficulty\" left after"
                + " it has been given extra health and damage. So entering 1 won't guarantee potion"
                + " effects.")
        public static float hostilePotionChance;
        @ConfigOption(name = "Potion Chance (Passives)", category = CAT_MOB)
        @ConfigOption.RangeFloat(value = 0.025f, min = 0, max = 1)
        @ConfigOption.Comment("The chance that an extra potion effect will be applied to any passive mob. Note that"
                + " this effect requires the mob to have a certain amount of \"difficulty\" left after"
                + " it has been given extra health and damage. So entering 1 won't guarantee potion"
                + " effects.")
        public static float passivePotionChance;
        @ConfigOption(name = "XP Boost", category = CAT_MOB)
        @ConfigOption.RangeFloat(value = 0.01f, min = 0, max = 1)
        @ConfigOption.Comment("Additional XP (as percentage) per point of difficulty. For example, if this is 0.01, a"
                + " mob will drop 2x (+1.0x) XP at 100 difficulty and 3x (+2.0x) at 200")
        public static float xpBoost;
        public static EntityMatchList damageBonusBlacklist = new EntityMatchList();

        public static final class Health {
            @ConfigOption(name = "Allow Boss Extra Health", category = CAT_MOB_HEALTH)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Allow boss mobs (dragon, wither, etc.) to spawn with extra health based on difficulty.")
            public static boolean allowBoss;
            @ConfigOption(name = "Allow Peaceful Extra Health", category = CAT_MOB_HEALTH)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Allow peaceful/passive mobs (such as animals) to spawn with extra health based on difficulty.")
            public static boolean allowPeaceful;
            @ConfigOption(name = "Allow Hostile Extra Health", category = CAT_MOB_HEALTH)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Allow hostile mobs (monsters) to spawn with extra health based on difficulty.")
            public static boolean allowHostile;
            @ConfigOption(name = "Base Health Modifier", category = CAT_MOB_HEALTH)
            @ConfigOption.RangeFloat(value = 0.5f, min = 0)
            @ConfigOption.Comment("The minimum extra health a hostile mob will have per point of difficulty, before the" +
                    " scaling mode is accounted for.")
            public static float hostileHealthMultiplier;
            @ConfigOption(name = "Base Health Modifier Peaceful", category = CAT_MOB_HEALTH)
            @ConfigOption.RangeFloat(value = 0.25f, min = 0)
            @ConfigOption.Comment("The minimum extra health a peaceful mob will have per point of difficulty, before the" +
                    " scaling mode is accounted for. Same as \"Base Health Modifier\", but for peaceful/passive mobs!")
            public static float peacefulHealthMultiplier;

            public static EnumHealthModMode healthScalingMode = EnumHealthModMode.MULTI_HALF;
            public static List<Integer> dimensionBlacklist = new ArrayList<>();
            public static EntityMatchList mobBlacklist = new EntityMatchList();
            private static final String[] mobBlacklistDefaults = new String[]{};
        }

        public static final class Blight {
            @ConfigOption(name = "Blacklist All Bosses", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If enabled, no bosses can become blights. If you need more control, use the Blacklist instead.")
            public static boolean blacklistBosses;
            @ConfigOption(name = "Blacklist All Hostiles", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If enabled, no hostile mobs can become blights.")
            public static boolean blacklistHostiles;
            @ConfigOption(name = "Blacklist All Passives", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If enabled, no passive (peaceful) mobs can become blights.")
            public static boolean blacklistPassives;
            @ConfigOption(name = "All Mobs Are Blights", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If true, every mob that can be a blight will be one 100% of the time.")
            public static boolean blightAlways;
            @ConfigOption(name = "Blight Chance Multiplier", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeFloat(value = 0.0625f, min = 0)
            @ConfigOption.Comment("Determines the chance of a mob spawning as a blight. Formula is "
                    + "\"blightChanceMulti * currentDifficulty / maxDifficulty\". Setting to 0 will disable blights." +
                    " Setting to 1 will guarantee blights at max difficulty.")
            public static float chanceMultiplier;
            @ConfigOption(name = "Fixed Chance", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If true, blights will have a fixed chance of spawning, regardless of difficulty." +
                    " Set the chance in \"Blight Chance Multiplier\".")
            public static boolean fixedBlightChance;
            @ConfigOption(name = "Blight Difficulty Multiplier", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeFloat(value = 3, min = 1)
            @ConfigOption.Comment("When an entity spawns as a blight, their calculated difficulty is multiplied by" +
                    " this. Higher numbers mean more health and damage!")
            public static float difficultyMultiplier;
            @ConfigOption(name = "Fire Rides Blights", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("Set blight fires to \"ride\" on the blight they belong to. In some cases, this might" +
                    " cause the fire to follow the blight more smoothly, but doesn't seem to help in most cases." +
                    " Leaving off is recommended.")
            public static boolean fireRidesBlight;
            @ConfigOption(name = "Fire Resist", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Should blights have the fire resistance potion effect?")
            public static boolean fireResist;
            @ConfigOption(name = "Immune To Suffocation", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If true, blights will not take suffocation (inside a block) damage")
            public static boolean immuneToSuffocation;
            @ConfigOption(name = "Invisibility", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("Should blights have the invisibility potion effect?")
            public static boolean invisibility;
            @ConfigOption(name = "Notify Players on Death", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Let all players know when a blight dies in chat.")
            public static boolean notifyOnDeath;
            @ConfigOption(name = "Potion Duration", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeInt(value = 5 * 20 * 60, min = -1)
            @ConfigOption.Comment("The duration (in ticks) of the potion effects applied to blights. The effects are" +
                    " refreshed frequently, so this value doesn't matter in most cases... except for the lingering" +
                    " potion effects left by blight creepers. Set to -1 for infinite time. Default is 5 minutes.")
            public static int potionDuration;
            @ConfigOption(name = "Amplifier Speed", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeInt(value = 4, min = -1, max = 99)
            @ConfigOption.Comment("The amplifier level on the speed potion effect applied to blights. Set -1 to" +
                    " disable, 0 is level 1.")
            public static int speedAmp;
            @ConfigOption(name = "Amplifier Strength", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeInt(value = 1, min = -1, max = 99)
            @ConfigOption.Comment("The amplifier level on the strength potion effect applied to blights. Set -1 to" +
                    " disable, 0 is level 1.")
            public static int strengthAmp;
            @ConfigOption(name = "Supercharge Creepers", category = CAT_MOB_BLIGHT)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("Blight creepers will be supercharged, like when they are struck by lightning.")
            public static boolean superchargeCreepers;
            @ConfigOption(name = "XP Multiplier", category = CAT_MOB_BLIGHT)
            @ConfigOption.RangeFloat(value = 10, min = 0, max = 1000)
            @ConfigOption.Comment("The multiplier applied to the amount of XP dropped when a blight is killed.")
            public static float xpMultiplier;

            public static EntityMatchList blightAllList = new EntityMatchList();
            public static EntityMatchList blacklist = new EntityMatchList();
            private static final String[] BLIGHT_BLACKLIST_DEFAULTS = new String[]{"minecraft:wither",
                    "minecraft:villager", "minecolonies:citizen"};
        }
    }

    // Blight equipment
    public static int BLIGHT_EQUIPMENT_HIGHEST_COMMON_TIER = 1;
    public static float BLIGHT_EQUIPMENT_TIER_UP_CHANCE = 0.095f;
    public static float BLIGHT_EQUIPMENT_ARMOR_PIECE_CHANCE = 0.5f;
    public static float BLIGHT_EQUIPMENT_HAND_PIECE_CHANCE = 0.5f;

    // Pets
    public static int PET_REGEN_DELAY = 600;

    public static final class Items {
        public static final class Heart {
            @ConfigOption(name = "Hearts Dropped by Blight Min", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 0, min = 0, max = 64)
            @ConfigOption.Comment("The minimum number of heart containers that a blight will drop when killed.")
            public static int blightMin;
            @ConfigOption(name = "Hearts Dropped by Blight Max", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 2, min = 0, max = 64)
            @ConfigOption.Comment("The maximum number of heart containers that a blight will drop when killed.")
            public static int blightMax;
            @ConfigOption(name = "Hearts Dropped by Boss Min", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 3, min = 0, max = 64)
            @ConfigOption.Comment("The minimum number of heart containers that a boss will drop when killed.")
            public static int bossMin;
            @ConfigOption(name = "Hearts Dropped by Boss Max", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 6, min = 0, max = 64)
            @ConfigOption.Comment("The maximum number of heart containers that a boss will drop when killed.")
            public static int bossMax;
            @ConfigOption(name = "Heart Drop Chance", category = CAT_ITEMS)
            @ConfigOption.RangeFloat(value = 0.01f, min = 0, max = 1)
            @ConfigOption.Comment("The chance of a hostile mob dropping a heart container when killed.")
            public static float chanceHostile;
            @ConfigOption(name = "Heart Drop Chance (Passive)", category = CAT_ITEMS)
            @ConfigOption.RangeFloat(value = 0.001f, min = 0, max = 1)
            @ConfigOption.Comment("The chance of a passive mob (animals) dropping a heart container when killed.")
            public static float chancePassive;
            @ConfigOption(name = "Drop Shards Instead of Containers", category = CAT_ITEMS)
            @ConfigOption.BooleanDefault(false)
            @ConfigOption.Comment("If enabled, heart crystals drop shards instead of full containers.")
            public static boolean dropShardsInstead;
            @ConfigOption(name = "Heart Healing Event", category = CAT_ITEMS)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If true, the healing from heart containers will fire a standard healing event," +
                    " allowing other mods to execute additional code or cancel the healing altogether. Disable if needed.")
            public static boolean healingEvent;
            @ConfigOption(name = "Hearts Health Restored", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 4, min = 0)
            @ConfigOption.Comment("The amount of extra health restored when using a heart container. This applies whether" +
                    " or not hearts increase max health.")
            public static int healthRestored;
            @ConfigOption(name = "Hearts Increase Max Health", category = CAT_ITEMS)
            @ConfigOption.BooleanDefault(true)
            @ConfigOption.Comment("If set to false, hearts will no longer increase the player's maximum health, but can" +
                    " still be used for healing.")
            public static boolean increaseHealth;
            @ConfigOption(name = "Heart XP Level Cost", category = CAT_ITEMS)
            @ConfigOption.RangeInt(value = 3, min = 0)
            @ConfigOption.Comment("The number of experience levels required to use a heart container.")
            public static int xpCost;
        }

        @ConfigOption(name = "Difficulty Change", category = CAT_ITEMS + ".cursed_heart")
        @ConfigOption.RangeFloat(10)
        @ConfigOption.Comment("The amount of difficulty added/removed when using a cursed heart.")
        public static float cursedHeartChange;
        @ConfigOption(name = "Difficulty Change", category = CAT_ITEMS + ".enchanted_heart")
        @ConfigOption.RangeFloat(-10)
        @ConfigOption.Comment("The amount of difficulty added/removed when using an enchanted heart.")
        public static float enchantedHeartChange;
        @ConfigOption(name = "Healing Items Fire Healing Event", category = CAT_ITEMS)
        @ConfigOption.BooleanDefault(true)
        @ConfigOption.Comment("If true, the healing from bandages and medkits will fire a standard healing event," +
                " allowing other mods to execute additional code or cancel the healing altogether. Disable if needed.")
        public static boolean healingItemFireEvent;
    }

    public static final class Difficulty {
        @ConfigOption(name = "Difficulty Added When Sleeping", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("Change in difficulty when a player sleeps through the night. Negative numbers cause difficulty to decrease.")
        public static float forSleeping;
        @ConfigOption(name = "Group Area Bonus", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0.05f, min = -10, max = 10)
        @ConfigOption.Comment("Adds extra difficulty per additional nearby player. So area difficulty will be" +
                " multiplied by: 1 + group_bonus * (players_in_area - 1)")
        public static float groupAreaBonus;
        @ConfigOption(name = "Idle Multiplier", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0.7f, min = -100, max = 100)
        @ConfigOption.Comment("Difficulty added per second is multiplied by this if the player is not moving.")
        public static float idleMulti;
        @ConfigOption(name = "Lost On Death", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("The difficulty a player loses on death. Negative numbers cause the player to gain difficulty.")
        public static float lostOnDeath;
        @ConfigOption(name = "Max Value", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 250, min = 0)
        @ConfigOption.Comment("The maximum difficulty level that can be reached.")
        public static float maxValue;
        @ConfigOption(name = "Min Value", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = 0)
        @ConfigOption.Comment("The minimum difficulty value. This can be different from the starting value.")
        public static float minValue;
        @ConfigOption(name = "Difficulty Per Blight Kill", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("Difficulty change per blight kill. Negative numbers cause difficulty to decrease.")
        public static float perBlightKill;
        @ConfigOption(name = "Difficulty Per Boss Kill", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("Difficulty change per boss kill. Negative numbers cause difficulty to decrease.")
        public static float perBossKill;
        @ConfigOption(name = "Difficulty Per Block", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(0.0025f)
        @ConfigOption.Comment("The amount of difficulty added per unit distance from the origin/spawn, assuming" +
                " \"Area Mode\" is set to a distance-based option. Negative numbers will decrease difficulty over distance.")
        public static float perBlock;
        @ConfigOption(name = "Difficulty Per Kill", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("Difficulty change per hostile mob killed. Negative numbers cause difficulty to decrease.")
        public static float perHostileKill;
        @ConfigOption(name = "Difficulty Per Passive Kill", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = -10000, max = 10000)
        @ConfigOption.Comment("Difficulty change per passive mob kill. Negative numbers cause difficulty to decrease.")
        public static float perPassiveKill;
        @ConfigOption(name = "Increase Per Second", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0.0011574f, min = -10000, max = 10000) // About 60 hrs to max (MAX / (60 * TICKS_PER_HOUR))
        @ConfigOption.Comment("The amount difficulty changes each second. In Difficult Life, the option was named per" +
                " tick, but was actually applied each second. Negative numbers will decrease difficulty over time.")
        public static float perSecond;
        @ConfigOption(name = "Search Radius", category = CAT_DIFFICULTY)
        @ConfigOption.RangeInt(value = 256, min = 0, max = Short.MAX_VALUE)
        @ConfigOption.Comment("The distance from a newly spawned mob to search for players to determine its difficulty" +
                " level. Set to 0 for unlimited range.")
        public static int searchRadius;
        @ConfigOption(name = "Starting Value", category = CAT_DIFFICULTY)
        @ConfigOption.RangeFloat(value = 0, min = 0)
        @ConfigOption.Comment("The starting difficulty level for new worlds or players joining for the first time.")
        public static float startValue;
        @ConfigOption(name = "Stats Consume Difficulty", category = CAT_CLIENT)
        @ConfigOption.BooleanDefault(false)
        @ConfigOption.Comment("If true, the difficulty a mob is spawned with will be \"consumed\" when given health/damage bonuses and potion effects (as in older versions).")
        public static boolean statsConsumeDifficulty;

        public static final Map<String, Integer> DIFFICULTY_BY_GAME_STAGES = new HashMap<>();
        private static final String[] DEFAULT_DIFFICULTY_LUNAR_MULTIPLIERS = new String[]{
                "1.5", "1.3", "1.2", "1.0", "0.8", "1.0", "1.2", "1.3"};
        public static PlayerMatchList DIFFICULTY_EXEMPT_PLAYERS = new PlayerMatchList();
        public static EntityDifficultyChangeList DIFFICULTY_PER_KILL_BY_MOB = new EntityDifficultyChangeList();
        public static Map<Integer, Float> DIMENSION_INCREASE_MULTIPLIER = new HashMap<>();
        public static Map<Integer, SimpleExpression> DIMENSION_VALUE_FACTOR = new HashMap<>();
        public static EnumAreaDifficultyMode AREA_DIFFICULTY_MODE = EnumAreaDifficultyMode.WEIGHTED_AVERAGE;
        public static EnumResetTime DIFFFICULTY_RESET_TIME = EnumResetTime.NONE;
        public static boolean DIFFICULTY_LUNAR_MULTIPLIERS_ENABLED = false;
        public static float[] DIFFICULTY_LUNAR_MULTIPLIERS = new float[8];
    }

    // Network
    public static int PACKET_DELAY = 20;

    // World
    public static float HEART_CRYSTAL_ORE_VEIN_COUNT = 3f / 7f;
    public static int HEART_CRYSTAL_ORE_VEIN_SIZE = 6;
    public static int HEART_CRYSTAL_ORE_MIN_HEIGHT = 10;
    public static int HEART_CRYSTAL_ORE_MAX_HEIGHT = 35;
    public static float HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP = 3f;
    public static float HEART_CRYSTAL_ORE_EXTRA_VEIN_RATE = HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP / 3125;
    public static int HEART_CRYSTAL_ORE_QUANTITY_DROPPED = 1;

    // Compatibility
    public static boolean MORPHEUS_OVERRIDE;

    static final String split = Configuration.CATEGORY_SPLITTER;
    public static final String CAT_MAIN = "main";
    public static final String CAT_DEBUG = CAT_MAIN + split + "debug";
    public static final String CAT_CLIENT = CAT_MAIN + split + "client";
    public static final String CAT_CLIENT_WIT = CAT_CLIENT + split + "wit";
    public static final String CAT_PLAYER = CAT_MAIN + split + "player";
    public static final String CAT_FAKE_PLAYER = CAT_MAIN + split + "fake_players";
    public static final String CAT_PLAYER_DAMAGE = CAT_PLAYER + split + "damage";
    public static final String CAT_PLAYER_HEALTH = CAT_PLAYER + split + "health";
    public static final String CAT_PLAYER_REGEN = CAT_PLAYER + split + "regen";
    public static final String CAT_MOB = CAT_MAIN + split + "mob";
    public static final String CAT_MOB_HEALTH = CAT_MOB + split + "health";
    public static final String CAT_MOB_BLIGHT = CAT_MOB + split + "blights";
    public static final String CAT_MOB_POTION = CAT_MOB + split + "potion";
    public static final String CAT_MOB_BLIGHT_EQUIP = CAT_MOB_BLIGHT + split + "equipment";
    public static final String CAT_PETS = CAT_MAIN + split + "pets";
    public static final String CAT_ITEMS = CAT_MAIN + split + "items";
    public static final String CAT_DIFFICULTY = CAT_MAIN + split + "difficulty";
    public static final String CAT_DIFFICULTY_LUNAR_PHASES = CAT_DIFFICULTY + split + "lunar_phases";
    public static final String CAT_NETWORK = CAT_MAIN + split + "network";
    public static final String CAT_WORLD = CAT_MAIN + split + "world";
    public static final String CAT_COMPAT = CAT_MAIN + split + "compatibility";
    public static final String CAT_HOLIDAYS = CAT_MAIN + split + "holidays";

    public static final Config INSTANCE = new Config();

    public Config() {
        super(ScalingHealth.MOD_ID_LOWER);
    }

    @Override
    public void init(File file) {
        // Re-route to different location.
        String path = file.getPath().replaceFirst("\\.cfg$", "/main.cfg");
        super.init(new File(path));
    }

    @Override
    public I18nHelper i18n() {
        return ScalingHealth.i18n;
    }

    @Override
    public LogHelper log() {
        return ScalingHealth.logHelper;
    }

    @Override
    public void load() {
        try {
            super.load();

            config.setCategoryRequiresMcRestart(CAT_CLIENT_WIT, true);

            ConfigMultiValueLineParser parser;

            // Change entity max health cap with reflection
            final int maxHealthCap = loadInt("Max Health Cap", CAT_MAIN, 2048, 2, Integer.MAX_VALUE,
                    "Max health cap for all entities, players and mobs (vanilla is 1024)");
            try {
                ScalingHealth.logHelper.info("Trying to change max health cap to {}", maxHealthCap);
                Field field = ObfuscationReflectionHelper.findField(RangedAttribute.class, "field_111118_b");
                field.setDouble(SharedMonsterAttributes.MAX_HEALTH, maxHealthCap);
            } catch (Exception ex) {
                ScalingHealth.logHelper.warn(ex, "Failed to change max health cap");
            }

            // Client
            Client.Hearts.textStyle = HeartDisplayHandler.TextStyle.loadFromConfig(this, "Health Text Style", HeartDisplayHandler.TextStyle.ROWS);
            Client.Hearts.textColor = HeartDisplayHandler.TextColor.loadFromConfig(this, "Health Text Color", HeartDisplayHandler.TextColor.GREEN_TO_RED);
            Client.Hearts.textSolidColor = Color.parse(config.getString("Health Text Solid Color", CAT_CLIENT, "FFFFFF", "Text color is color style is SOLID")).getColor();
            Client.Hearts.absorptionStyle = HeartDisplayHandler.AbsorptionHeartStyle.loadDromConfig(this);
            Client.Hearts.absorbTextStyle = HeartDisplayHandler.TextStyle.loadFromConfig(this, "Absorption Text Style", HeartDisplayHandler.TextStyle.DISABLED);
            Client.Hearts.absorbTextColor = HeartDisplayHandler.TextColor.loadFromConfig(this, "Absorption Text Color", HeartDisplayHandler.TextColor.WHITE);

            loadHeartColors(config);

            // Players
            // Health
            Player.Health.resetTime = EnumResetTime.loadFromConfig(config, Player.Health.resetTime, CAT_PLAYER_HEALTH);

            Player.Health.byXP.clear();
            parser = new ConfigMultiValueLineParser("Set Health By XP", ScalingHealth.logHelper, "\\s+", Integer.class, Integer.class);
            for (String str : config.getStringList("Set Health By XP", CAT_PLAYER_HEALTH, new String[0],
                    "Allows the player's health to be set according to XP level. Each line will have the level, then" +
                            " the max health after a space. For example, \"10 30\" would give the player 15 hearts (30" +
                            " health) at level 10. Note this is the \"target health\" for a player of this XP level." +
                            " The actual bonus heart will be the value you enter minus starting health. The highest" +
                            " level the player has passed will be selected. The health bonus will stack with heart" +
                            " containers (just remember heart containers consume XP by default).")) {
                Object[] array = parser.parse(str);
                if (array != null) {
                    Player.Health.byXP.put((int) array[0], (int) array[1]);
                }
            }

            // Damage Scaling
            DamageScaling.INSTANCE.loadConfig(config);

            // Mobs
            Mob.damageBonusBlacklist.clear();
            for (String str : config.getStringList("Damage Bonus Blacklist", CAT_MOB,
                    new String[0],
                    "Mobs listed here will not receive extra attack damage, but can still get extra health or become blights")) {
                Mob.damageBonusBlacklist.add(str);
            }
            // Health
            Mob.Health.healthScalingMode = EnumHealthModMode.loadFromConfig(config, Mob.Health.healthScalingMode);
            String[] dimList = config.getStringList("Dimension Blacklist", CAT_MOB_HEALTH, new String[0],
                    "Mobs will not gain extra health or become blights in these dimensions. Integers only,"
                            + " any other entries will be silently ignored.");
            Mob.Health.dimensionBlacklist.clear();
            for (String str : dimList) {
                if (canParseInt(str)) {
                    Mob.Health.dimensionBlacklist.add(Integer.parseInt(str));
                }
            }
            Mob.Health.mobBlacklist.clear();
            for (String str : config.getStringList("Blacklist", CAT_MOB_HEALTH,
                    Mob.Health.mobBlacklistDefaults,
                    "Mobs listed here will never receive extra health, and will not become blights. There is"
                            + " also a separate blacklist for blights, if you still want the mob in question to have"
                            + " extra health.")) {
                Mob.Health.mobBlacklist.add(str);
            }
            // Blights
            Mob.Blight.blightAllList.loadConfig(config, "Always Blight", CAT_MOB_BLIGHT, new String[0], false,
                    "If \"All Mobs Are Blights\" is enabled, this list can be used to filter mobs.");
            Mob.Blight.blacklist.clear();
            for (String str : config.getStringList("Blacklist", CAT_MOB_BLIGHT,
                    Mob.Blight.BLIGHT_BLACKLIST_DEFAULTS,
                    "Mobs listed here will never become blights, but can still receive extra health. There is"
                            + " also a blacklist for extra health.")) {
                Mob.Blight.blacklist.add(str);
            }
            // Blight equipment
            BLIGHT_EQUIPMENT_HIGHEST_COMMON_TIER = loadInt("Highest Common Tier", CAT_MOB_BLIGHT_EQUIP,
                    BLIGHT_EQUIPMENT_HIGHEST_COMMON_TIER, 0, 4,
                    "The highest commonly-occuring equipment tier for blights. This goes from 0 to 4"
                            + " inclusive. For armor, the defaults (tiers 0 to 4) are leather, gold, chainmail,"
                            + " iron, and diamond.");
            BLIGHT_EQUIPMENT_TIER_UP_CHANCE = config.getFloat("Tier Up Chance", CAT_MOB_BLIGHT_EQUIP,
                    BLIGHT_EQUIPMENT_TIER_UP_CHANCE, 0f, 1f,
                    "The chance that a higher tier will be selected for a blight. A common tier is chosen"
                            + " first, then it has a few chances to increase.");
            BLIGHT_EQUIPMENT_ARMOR_PIECE_CHANCE = config.getFloat("Armor Piece Chance", CAT_MOB_BLIGHT_EQUIP,
                    BLIGHT_EQUIPMENT_ARMOR_PIECE_CHANCE, 0f, 1f,
                    "The chance of an additional armor piece being given. Every blight receives a helmet,"
                            + " then has this probability of receiving a chestplate. If it gets a chestplate, it has"
                            + " this probability of receiving leggings, etc.");
            BLIGHT_EQUIPMENT_HAND_PIECE_CHANCE = config.getFloat("Hand Piece Chance", CAT_MOB_BLIGHT_EQUIP,
                    BLIGHT_EQUIPMENT_HAND_PIECE_CHANCE, 0f, 1f,
                    "The chance that a blight will receive equipment in their hands (swords, etc.) They only"
                            + " get a chance at an offhand item if a main hand item is selected. Depending on the"
                            + " mods you have installed, there may not be any hand equipment to chose from.");
            config.setCategoryComment(CAT_MOB_POTION, "Potion effects applied to non-blights.");
            config.setCategoryRequiresMcRestart(CAT_MOB_POTION, true);

            // Pets
            PET_REGEN_DELAY = loadInt("Regen Delay", CAT_PETS,
                    PET_REGEN_DELAY, 0, 72000,
                    "The number of ticks between regen ticks on pets. Set to 0 to disable pet regen.");

            // Items
            if (Items.Heart.bossMax < Items.Heart.bossMin)
                Items.Heart.bossMax = Items.Heart.bossMin;
            if (Items.Heart.blightMax < Items.Heart.blightMin)
                Items.Heart.blightMax = Items.Heart.blightMin;

            // Difficulty
            // Sleep message
            Client.Difficulty.sleepMessageOverride = config.getString("Warn When Sleeping - Message", "main.client", "",
                    "If not empty, this replaces the default 'warn when sleeping' message. Leaving this empty will pull the usual message from the lang file.");
            // Player exemptions
            Difficulty.DIFFICULTY_EXEMPT_PLAYERS.clear();
            for (String name : config.getStringList("Exempt Players", CAT_DIFFICULTY, new String[0],
                    "Players listed here will be \"exempt\" from the difficulty system. Exempt players are"
                            + " still part of difficulty calculations, but are treated as having zero difficulty.")) {
                Difficulty.DIFFICULTY_EXEMPT_PLAYERS.add(name);
            }
            // Difficulty Per Kill By Mob
            parser = new ConfigMultiValueLineParser("Difficulty Per Kill By Mob",
                    ScalingHealth.logHelper, "\\s", String.class, Float.class, Float.class);
            Difficulty.DIFFICULTY_PER_KILL_BY_MOB.clear();
            for (String str : config.getStringList("Difficulty Per Kill By Mob", CAT_DIFFICULTY,
                    new String[]{},
                    "Lets you set difficulty changes for individual mobs. Each line has 3 values separated by"
                            + " spaces: entity ID, standard kill change, blight kill change. For example, entering"
                            + " \"minecraft:zombie 0.1 -20\" will cause zombie kills to add 0.1 difficulty, but"
                            + " killing a blight zombie will remove 20 difficulty instead.")) {
                Object[] values = parser.parse(str);
                if (values != null) {
                    // Shouldn't need any safety checks, the parser returns null if any error occurs.
                    Difficulty.DIFFICULTY_PER_KILL_BY_MOB.put((String) values[0], (float) values[1], (float) values[2]);
                }
            }
            // Difficulty Dimension Multiplier
            parser = new ConfigMultiValueLineParser("Difficulty Dimension Multiplier",
                    ScalingHealth.logHelper, "\\s", Integer.class, Float.class);
            for (String str : config.getStringList("Difficulty Dimension Multiplier", CAT_DIFFICULTY,
                    new String[]{},
                    "Allows difficulty multipliers to be set for specific dimensions. Use the dimension ID"
                            + " and the multiplier you want, separated by a space. For example, \"-1 1.5\" would"
                            + " make difficulty increase 1.5x faster in the Nether.")) {
                Object[] values = parser.parse(str);
                if (values != null) {
                    Difficulty.DIMENSION_INCREASE_MULTIPLIER.put((int) values[0], (float) values[1]);
                }
            }
            // Dimension value factor
            parser = new ConfigMultiValueLineParser("Dimension Value Factor", ScalingHealth.logHelper, "\\s", Integer.class, String.class);
            for (String str : config.getStringList("Dimension Value Factor", CAT_DIFFICULTY, new String[]{},
                    "Apply a simple change to the area difficulty in a given dimension. Use the dimension ID, then a" +
                            " space, then an operator (+-*/) followed by a number. For example, \"-1 *2.0\" would" +
                            " make difficulty 2x higher in the Nether. \"1 +20\" would increase difficulty by 20 in The End")) {
                Object[] values = parser.parse(str);
                if (values != null) {
                    SimpleExpression.from((String) values[1]).ifPresent(exp ->
                            Difficulty.DIMENSION_VALUE_FACTOR.put((int) values[0], exp));
                }
            }
            // Lunar cycles
            Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS_ENABLED = loadBoolean("Lunar Phases Enabled", CAT_DIFFICULTY_LUNAR_PHASES, false,
                    "Enable lunar phase difficulty multipliers. Difficulty will receive a multiplier based"
                            + " on the phase of the moon.");
            parser = new ConfigMultiValueLineParser("Lunar Phase Multipliers", ScalingHealth.logHelper, "\\s", Float.class);
            int lunarPhaseIndex = 0;
            for (String str : config.getStringList("Lunar Phase Multipliers", CAT_DIFFICULTY_LUNAR_PHASES,
                    Difficulty.DEFAULT_DIFFICULTY_LUNAR_MULTIPLIERS,
                    "Difficulty multipliers for each lunar phase. There must be exactly 8 values. The first"
                            + " is full moon, the fifth is new moon.")) {
                Object[] values = parser.parse(str);
                if (values != null && lunarPhaseIndex < 8) {
                    Difficulty.DIFFICULTY_LUNAR_MULTIPLIERS[lunarPhaseIndex] = (float) values[0];
                }
                ++lunarPhaseIndex;
            }
            if (lunarPhaseIndex != 8) {
                ScalingHealth.logHelper.warn("Config \"Lunar Phase Multipliers\" has the wrong number"
                        + " of values. Needs 8, has " + lunarPhaseIndex);
            }
            Difficulty.DIFFICULTY_BY_GAME_STAGES.clear();
            parser = new ConfigMultiValueLineParser("Game Stages", ScalingHealth.logHelper, "\\s+", String.class, Integer.class);
            for (String str : config.getStringList("Game Stages", CAT_DIFFICULTY, new String[0],
                    "Allows difficulty to be set via Game Stages. Each line should consist of the stage key, followed" +
                            " by a space and the difficulty value (integers only). Example: \"my_stage_key 100\"")) {
                Object[] values = parser.parse(str);
                if (values != null) {
                    Difficulty.DIFFICULTY_BY_GAME_STAGES.put((String) values[0], (Integer) values[1]);
                }
            }
            Difficulty.AREA_DIFFICULTY_MODE = EnumAreaDifficultyMode.loadFromConfig(config, Difficulty.AREA_DIFFICULTY_MODE);
            Difficulty.DIFFFICULTY_RESET_TIME = EnumResetTime.loadFromConfig(config, Difficulty.DIFFFICULTY_RESET_TIME, CAT_DIFFICULTY);

            // Network
            PACKET_DELAY = loadInt("Packet Delay", CAT_NETWORK,
                    PACKET_DELAY, 1, 1200,
                    "The number of ticks between update packets. Smaller numbers mean more packets (and more "
                            + "bandwidth and processing power used), but also less client-server desynconfig.");

            // World
            String cat = CAT_WORLD + split + "heart_crystal_ore";
            HEART_CRYSTAL_ORE_VEIN_COUNT = config.getFloat("Veins Per Chunk", cat,
                    HEART_CRYSTAL_ORE_VEIN_COUNT, 0, 10000,
                    "The number of veins per chunk. The fractional part is a probability of an extra vein in each chunk.");
            HEART_CRYSTAL_ORE_VEIN_SIZE = loadInt("Vein Size", cat,
                    HEART_CRYSTAL_ORE_VEIN_SIZE, 0, 10000,
                    "The size of each vein.");
            HEART_CRYSTAL_ORE_MIN_HEIGHT = loadInt("Min Height", cat,
                    HEART_CRYSTAL_ORE_MIN_HEIGHT, 0, 255,
                    "The lowest y-level the ore can be found at. Must be less than Max Height");
            HEART_CRYSTAL_ORE_MAX_HEIGHT = loadInt("Max Height", cat,
                    HEART_CRYSTAL_ORE_MAX_HEIGHT, 0, 255,
                    "The highest y-level the ore can be found at. Must be greater than Min Height");
            if (HEART_CRYSTAL_ORE_MAX_HEIGHT <= HEART_CRYSTAL_ORE_MIN_HEIGHT) {
                HEART_CRYSTAL_ORE_MAX_HEIGHT = 35;
                HEART_CRYSTAL_ORE_MIN_HEIGHT = 10;
            }
            HEART_CRYSTAL_ORE_EXTRA_VEIN_RATE = config.getFloat("Extra Vein Rate", cat,
                    HEART_CRYSTAL_ORE_EXTRA_VEIN_RATE, 0f, 1f,
                    "The number of extra possible veins per chunk away from spawn. The default value will reach the cap at 50,000 blocks from spawn.");
            HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP = config.getFloat("Extra Vein Cap", cat,
                    HEART_CRYSTAL_ORE_EXTRA_VEIN_CAP, 0f, 1000f,
                    "The maximum number of extra veins created by distance from spawn.");
            HEART_CRYSTAL_ORE_QUANTITY_DROPPED = loadInt("Quantity Dropped", cat,
                    HEART_CRYSTAL_ORE_QUANTITY_DROPPED, 1, 64,
                    "The base number of heart crystal shards dropped by the ore. Fortune III can double this value at most.");

            // Compatibility
            config.setCategoryRequiresMcRestart(CAT_COMPAT, true);
            MORPHEUS_OVERRIDE = config.getBoolean("Morpheus Support", CAT_COMPAT, true,
                    "Override the Morpheus new day handler to fire sleep events. Without this, difficulty will not increase when sleeping.");

            ModuleAprilTricks.instance.loadConfig(config);
            ScalingHealth.logHelper.info("Config successfully loaded!");
        } catch (Exception ex) {
            ScalingHealth.logHelper.fatal("Could not load configuration file!");
            Greetings.addMessage(() -> new TextComponentString(TextFormatting.RED + "[Scaling Health] Could not load configuration file! The mod will not work correctly. See log for details."));
            ex.printStackTrace();
        }
    }

    private static void loadHeartColors(Configuration c) {
        // Get hex strings for default colors.
        String[] defaults = new String[Client.Hearts.defaultHeartColors.length];
        for (int i = 0; i < defaults.length; ++i)
            defaults[i] = String.format("%06x", Client.Hearts.defaultHeartColors[i]);

        // Load the string list from config.
        String[] list = c.getStringList("Heart Colors", Config.CAT_CLIENT, defaults,
                "The colors for each additional row of hearts. The colors will loop back around to the beginning if necessary. Use hexadecimal to specify colors (like HTML color codes).");
        String[] listAbsorb = c.getStringList("Absorption Heart Colors", Config.CAT_CLIENT, defaults,
                "The colors for each row of absorption hearts. Works the same way as \"Heart Colors\"");

        // Convert hex strings to ints.
        try {
            Client.Hearts.heartColors = new int[list.length];
            for (int i = 0; i < Client.Hearts.heartColors.length; ++i)
                Client.Hearts.heartColors[i] = Integer.decode("0x" + list[i]);

            Client.Hearts.absorptionHeartColors = new int[listAbsorb.length];
            for (int i = 0; i < Client.Hearts.absorptionHeartColors.length; ++i)
                Client.Hearts.absorptionHeartColors[i] = Integer.decode("0x" + listAbsorb[i]);
        } catch (NumberFormatException ex) {
            ScalingHealth.logHelper.warn("Failed to load heart colors because a value could not be parsed. Make sure all values are valid hexadecimal integers. Try using an online HTML color picker if you are having problems.");
            ex.printStackTrace();
        }
    }

    public boolean canParseInt(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public boolean canParseFloat(String str) {
        try {
            Float.parseFloat(str);
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    public float tryParseFloat(String str) {
        try {
            return Float.parseFloat(str);
        } catch (NumberFormatException ex) {
            return 0f;
        }
    }
}
