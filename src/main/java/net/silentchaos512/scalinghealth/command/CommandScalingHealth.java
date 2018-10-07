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

package net.silentchaos512.scalinghealth.command;

import com.google.common.collect.ImmutableList;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.silentchaos512.lib.command.CommandBaseSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class CommandScalingHealth extends CommandBaseSL {
    private static final String NUMFORMAT = "%.2f";

    @Override
    public String getName() {
        return ScalingHealth.MOD_ID_LOWER;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TextFormatting.RED + "Usage: /" + getName() + " <difficulty|health|world_difficulty> <get|set|add|sub> [value] [player]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length < 2) {
            tell(sender, getUsage(sender), false);
            return;
        }

        // Get arguments.
        String command = args[0];
        SubCommand subCommand = SubCommand.fromArg(args[1]);
        boolean isGet = subCommand == SubCommand.GET;
        if (subCommand == null || (!isGet && args.length < 3)) {
            tell(sender, getUsage(sender), false);
            return;
        }
        double value = isGet ? -1D : parseDouble(args[2]);
        List<EntityPlayerMP> targets = getTargetPlayers(server, sender, isGet, args);

        if (command.equals("difficulty"))
            executeDifficulty(server, sender, subCommand, value, targets);
        else if (command.equals("health"))
            executeHealth(server, sender, subCommand, value, targets);
        else if (command.equals("world_difficulty"))
            executeWorldDifficulty(server, sender, subCommand, value, sender.getEntityWorld());
        else
            tell(sender, getUsage(sender), false);
    }

    private List<EntityPlayerMP> getTargetPlayers(MinecraftServer server, ICommandSender sender, boolean isGet, String[] args) throws PlayerNotFoundException, CommandException {
        int index = isGet ? 2 : 3;
        return args.length < index + 1
                ? ImmutableList.of(getCommandSenderAsPlayer(sender))
                : getPlayers(server, sender, args[index]);
    }

    private void executeDifficulty(MinecraftServer server, ICommandSender sender, @Nonnull SubCommand subCommand, double value, List<EntityPlayerMP> targets) {
        if (targets.isEmpty())
            return;

        for (EntityPlayerMP player : targets) {
            PlayerData data = SHPlayerDataHandler.get(player);

            if (data == null) {
                tell(sender, "Player data is null for " + player.getName(), false);
                continue;
            }

            if (subCommand == SubCommand.GET) {
                // Report difficulty
                double current = data.getDifficulty();
                String strCurrent = String.format(NUMFORMAT, current);
                String strMax = String.format(NUMFORMAT, Config.Difficulty.maxValue);
                tell(sender, "showDifficulty", true, player.getName(), strCurrent, strMax);
            } else {
                // Try set difficulty
                double current = data.getDifficulty();
                double toSet = getValueToSet(subCommand, value, current);
                double min = getMinValue(subCommand, current, Config.Difficulty.minValue, Config.Difficulty.maxValue);
                double max = getMaxValue(subCommand, current, Config.Difficulty.minValue, Config.Difficulty.maxValue);

                // Bounds check
                if (!checkBounds(subCommand, value, toSet, current, min, max)) {
                    tell(sender, TextFormatting.RED, "outOfBounds", true, String.format(NUMFORMAT, min), String.format(NUMFORMAT, max));
                    return;
                }

                // Change it!
                data.setDifficulty(toSet);
                tell(sender, "setDifficulty", true, player.getName(), String.format(NUMFORMAT, toSet));
            }
        }
    }

    private void executeWorldDifficulty(MinecraftServer server, ICommandSender sender, @Nonnull SubCommand subCommand, double value, World world) {
        ScalingHealthSavedData data = ScalingHealthSavedData.get(world);
        if (data == null) {
            tell(sender, "World data is null!", false);
            return;
        }

        if (subCommand == SubCommand.GET) {
            // Report difficulty
            double current = data.difficulty;
            String strCurrent = String.format(NUMFORMAT, current);
            String strMax = String.format(NUMFORMAT, Config.Difficulty.maxValue);
            tell(sender, "showWorldDifficulty", true, strCurrent, strMax);
        } else {
            // Try set difficulty
            double current = data.difficulty;
            double toSet = getValueToSet(subCommand, value, current);
            double min = getMinValue(subCommand, current, Config.Difficulty.minValue, Config.Difficulty.maxValue);
            double max = getMaxValue(subCommand, current, Config.Difficulty.minValue, Config.Difficulty.maxValue);

            // Bounds check
            if (!checkBounds(subCommand, value, toSet, current, min, max)) {
                tell(sender, TextFormatting.RED, "outOfBounds", true, String.format(NUMFORMAT, min), String.format(NUMFORMAT, max));
                return;
            }

            // Change it!
            data.difficulty = toSet;
            data.markDirty();
            tell(sender, "setWorldDifficulty", true, String.format(NUMFORMAT, toSet));
        }
    }

    private void executeHealth(MinecraftServer server, ICommandSender sender, @Nonnull SubCommand subCommand, double value, List<EntityPlayerMP> targets) {
        if (targets.isEmpty())
            return;

        for (EntityPlayerMP player : targets) {
            PlayerData data = SHPlayerDataHandler.get(player);

            if (data == null) {
                tell(sender, "Player data is null for " + player.getName(), false);
                continue;
            }

            if (subCommand == SubCommand.GET) {
                // Report health.
                float current = player.getHealth();
                float max = player.getMaxHealth();
                float modValue = data.getMaxHealth() - Config.Player.Health.startingHealth;
                String strCurrent = String.format(NUMFORMAT, current);
                String strMax = String.format(NUMFORMAT, max);
                String strMod = (modValue >= 0f ? "+" : "") + modValue;
                tell(sender, "showHealth", true, player.getName(), strCurrent, strMax, strMod);
            } else {
                // Try set health.
                double current = data.getMaxHealth();
                double toSet = getValueToSet(subCommand, value, current);
                double hardMax = Config.Player.Health.maxHealth;
                hardMax = (int) (hardMax <= 0 ? SharedMonsterAttributes.MAX_HEALTH.clampValue(Integer.MAX_VALUE) : hardMax);
                double min = getMinValue(subCommand, current, 2, hardMax);
                double max = getMaxValue(subCommand, current, 2, hardMax);

                // Bounds check
                if (!checkBounds(subCommand, value, toSet, current, min, max)) {
                    tell(sender, TextFormatting.RED, "outOfBounds", true, min, max);
                    return;
                }

                // Change it!
                float toHeal = (float) (toSet - current);
                data.setMaxHealth((float) toSet);

                if (toHeal > 0)
                    player.heal(toHeal);

                tell(sender, "setHealth", true, player.getName(), toSet);
            }
        }
    }

    /**
     * Gets the actual value to set, based on subCommand. Does not check that the value is valid.
     *
     * @param subCommand The subcommand (most likely SET/ADD/SUB)
     * @param current    The current value ({@link PlayerData#getMaxHealth()}, {@link
     *                   PlayerData#getDifficulty()}, etc.)
     * @param current    The current value (data.getMaxHealth(), data.getDifficulty(), etc)
     * @return The value that will be set, assuming it is valid.
     */
    private double getValueToSet(SubCommand subCommand, double value, double current) {
        double toSet = value;
        if (subCommand == SubCommand.ADD)
            toSet = current + value;
        else if (subCommand == SubCommand.SUB)
            toSet = current - value;
        return toSet;
    }

    /**
     * Gets the minimum value the player could enter, based on subCommand.
     *
     * @param subCommand The subcommand (most likely SET/ADD/SUB)
     * @param current    The current value ({@link PlayerData#getMaxHealth()}, {@link
     *                   PlayerData#getDifficulty()}, etc.)
     * @param min        The minimum allowed absolute value.
     * @param max        The maximum allowed absolute value.
     * @return The minimum value that can be entered, adjusted for the subcommand.
     */
    private double getMinValue(SubCommand subCommand, double current, double min, double max) {
        if (subCommand == SubCommand.ADD)
            return min - current;
        else if (subCommand == SubCommand.SUB)
            return current - max;
        else
            return min;
    }

    /**
     * Gets the maximum value the player could enter, based on subCommand.
     *
     * @param subCommand The subcommand (most likely SET/ADD/SUB)
     * @param current    The current value ({@link PlayerData#getMaxHealth()}, {@link
     *                   PlayerData#getDifficulty()}, etc.)
     * @param min        The minimum allowed absolute value.
     * @param max        The maximum allowed absolute value.
     * @return The maximum value that can be entered, adjusted for the subcommand.
     */
    private double getMaxValue(SubCommand subCommand, double current, double min, double max) {
        if (subCommand == SubCommand.ADD)
            return max - current;
        else if (subCommand == SubCommand.SUB)
            return current - min;
        else
            return max;
    }

    private boolean checkBounds(SubCommand subCommand, double value, double toSet, double current, double min, double max) {
        return !(value < min || value > max);
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1)
            return getListOfStringsMatchingLastWord(args, "difficulty", "health", "world_difficulty");
        else if (args.length == 2)
            return getListOfStringsMatchingLastWord(args, "get", "set", "add", "sub");
        else if (isUsernameIndex(args, args.length))
            return getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
        else
            return ImmutableList.of();
    }

    @Override
    public boolean isUsernameIndex(String[] args, int index) {
        return args.length > 2 && args[1].equals("get") ? index == 3 : index == 4;
    }

    private void tell(ICommandSender sender, String key, boolean fromLocalizationFile, Object... args) {
        tell(sender, TextFormatting.RESET, key, fromLocalizationFile, args);
    }

    private void tell(ICommandSender sender, TextFormatting format, String key, boolean fromLocalizationFile, Object... args) {
        key = "command." + ScalingHealth.MOD_ID_LOWER + "." + key;
        if (fromLocalizationFile)
            sender.sendMessage(new TextComponentString("" + format).appendSibling(new TextComponentTranslation(key, args)));
        else
            sender.sendMessage(new TextComponentString(format + String.format(key, args)));
    }

    enum SubCommand {
        GET, SET, ADD, SUB;

        @Nullable
        static SubCommand fromArg(String arg) {

            for (SubCommand val : values())
                if (val.name().equalsIgnoreCase(arg))
                    return val;
            return null;
        }
    }
}
