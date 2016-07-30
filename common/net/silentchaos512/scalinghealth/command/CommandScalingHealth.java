package net.silentchaos512.scalinghealth.command;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.ScalingHealthSaveStorage;

public class CommandScalingHealth implements ICommand {

  public static final String NUMFORMAT = "%.2f";

  @Override
  public int compareTo(ICommand arg0) {

    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public String getCommandName() {

    return ScalingHealth.MOD_ID.toLowerCase();
  }

  @Override
  public String getCommandUsage(ICommandSender sender) {

    return "Usage: /" + getCommandName() + " <difficulty|health> <value> [player]";
  }

  @Override
  public List<String> getCommandAliases() {

    return Lists.newArrayList();
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args)
      throws CommandException {

    if (args.length < 1) {
      tell(sender, getCommandUsage(sender), false);
      return;
    }

    String command = args[0];
    if (command.equals("difficulty")) {
      double current = ScalingHealthSaveStorage.getDifficulty(sender.getEntityWorld());
      if (args.length == 1) {
        // Display difficulty.
        String strCurrent = String.format(NUMFORMAT, current);
        String strMax = String.format(NUMFORMAT, ConfigScalingHealth.DIFFICULTY_MAX);
        tell(sender, "showDifficulty", true, strCurrent, strMax);
        return;
      }

      try {
        // Try set difficulty.
        double value = Double.parseDouble(args[1]);
        // Bounds check.
        if (value < 0 || value > ConfigScalingHealth.DIFFICULTY_MAX) {
          tell(sender, "outOfBounds", true, String.format(NUMFORMAT, 0.0f),
              String.format("%.2f", ConfigScalingHealth.DIFFICULTY_MAX));
          return;
        }

        // Change it!
        ScalingHealthSaveStorage.incrementDifficulty(sender.getEntityWorld(), value - current);
        tell(sender, "setDifficulty", true, String.format(NUMFORMAT, value));
      } catch (NumberFormatException ex) {
        tell(sender, getCommandUsage(sender), false);
      }
    } else if (command.equals("health")) {
      if (args.length < 2) {
        tell(sender, getCommandUsage(sender), false);
        return;
      }
      try {
        // Try set player health.
        int value = Integer.parseInt(args[1]);
        EntityPlayerMP player = (EntityPlayerMP) sender;

        // Bounds check.
        int max = ConfigScalingHealth.PLAYER_HEALTH_MAX;
        max = max <= 0 ? Integer.MAX_VALUE : max;
        if (value < 2 || (value > max)) {
          tell(sender, "outOfBounds", true, 2, max);
          return;
        }

        // Specific player, or the user?
        if (args.length > 2) {
          String name = args[2];
          player = server.getPlayerList().getPlayerByUsername(name);
          if (player == null) {
            tell(sender, "playerNotFound", true, name);
            return;
          }
        }

        // Change it!
        float currentHealth = player.getHealth();
        float toHeal = value - currentHealth;

        ScalingHealthSaveStorage.setPlayerHealth(player, value);
        if (toHeal > 0)
          player.heal(toHeal);

        tell(sender, "setHealth", true, player.getName(), value);
      } catch (NumberFormatException ex) {
        tell(sender, getCommandUsage(sender), false);
      }
    }
  }

  @Override
  public boolean checkPermission(MinecraftServer server, ICommandSender sender) {

    return (server.isSinglePlayer() && server.worldServers[0].getWorldInfo().areCommandsAllowed())
        || server.getPlayerList().getOppedPlayers()
            .getGameProfileFromName(sender.getName()) != null;
  }

  @Override
  public List<String> getTabCompletionOptions(MinecraftServer server, ICommandSender sender,
      String[] args, BlockPos pos) {

    return Lists.newArrayList("difficulty", "health");
  }

  @Override
  public boolean isUsernameIndex(String[] args, int index) {

    // TODO Auto-generated method stub
    return false;
  }

  private void tell(ICommandSender sender, String key, boolean fromLocalizationFile,
      Object... args) {

    String value = fromLocalizationFile
        ? ScalingHealth.localizationHelper.getLocalizedString("command." + key, args) : key;
    sender.addChatMessage(new TextComponentString(value));
  }
}
