package net.silentchaos512.scalinghealth.command;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.silentchaos512.lib.command.CommandBaseSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class CommandScalingHealth extends CommandBaseSL {

  public static final String NUMFORMAT = "%.2f";

  @Override
  public String getName() {

    return ScalingHealth.MOD_ID_LOWER;
  }

  @Override
  public String getUsage(ICommandSender sender) {

    return "Usage: /" + getName() + " <difficulty|health> <value> [player]";
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args)
      throws CommandException {

    if (args.length < 1) {
      tell(sender, getUsage(sender), false);
      return;
    }

    // Get arguments.
    String command = args[0];
    double value = -1D;
    EntityPlayer targetPlayer = null;

    if (args.length > 1) {
      try {
        value = Double.parseDouble(args[1]);
      } catch (Exception ex) {
        tell(sender, getUsage(sender), false);
        return;
      }
    }
    if (args.length > 2) {
      String name = args[2];
      targetPlayer = server.getPlayerList().getPlayerByUsername(name);
      if (targetPlayer == null) {
        tell(sender, "playerNotFound", true, name);
        return;
      }
    }

    if (command.equals("difficulty")) {
      executeDifficulty(server, sender, value, targetPlayer);
    } else if (command.equals("health")) {
      executeHealth(server, sender, value, targetPlayer);
    }
  }

  private void executeDifficulty(MinecraftServer server, ICommandSender sender, double value,
      EntityPlayer targetPlayer) {

    if (targetPlayer == null)
      targetPlayer = (EntityPlayer) sender;
    PlayerData data = SHPlayerDataHandler.get(targetPlayer);

    if (data == null) {
      tell(sender, "Player data is null!", false);
      return;
    }

    if (value < 0) {
      // Report difficulty.
      double current = data.getDifficulty();
      String strCurrent = String.format(NUMFORMAT, current);
      String strMax = String.format(NUMFORMAT, ConfigScalingHealth.DIFFICULTY_MAX);
      tell(sender, "showDifficulty", true, targetPlayer.getName(), strCurrent, strMax);
    } else {
      // Try set difficulty.
      // Bounds check.
      if (value < 0 || value > ConfigScalingHealth.DIFFICULTY_MAX) {
        tell(sender, "outOfBounds", true, String.format(NUMFORMAT, 0.0f),
            String.format("%.2f", ConfigScalingHealth.DIFFICULTY_MAX));
        return;
      }

      // Change it!
      data.setDifficulty(value);
      tell(sender, "setDifficulty", true, targetPlayer.getName(), String.format(NUMFORMAT, value));
    }
  }

  private void executeHealth(MinecraftServer server, ICommandSender sender, double value,
      EntityPlayer targetPlayer) {

    if (targetPlayer == null)
      targetPlayer = (EntityPlayer) sender;
    PlayerData data = SHPlayerDataHandler.get(targetPlayer);

    if (data == null) {
      tell(sender, "Player data is null!", false);
      return;
    }

    if (value < 0) {
      // Report health.
      float current = targetPlayer.getHealth();
      float max = targetPlayer.getMaxHealth();
      String strCurrent = String.format(NUMFORMAT, current);
      String strMax = String.format(NUMFORMAT, max);
      tell(sender, "showHealth", true, targetPlayer.getName(), strCurrent, strMax);
    } else {
      // Bounds check.
      int max = ConfigScalingHealth.PLAYER_HEALTH_MAX;
      max = max <= 0 ? Integer.MAX_VALUE : max;
      if (value < 2 || value > max) {
        tell(sender, "outOfBounds", true, 2, max);
        return;
      }

      // Change it!
      float currentHealth = targetPlayer.getHealth();
      float toHeal = (float) (value - currentHealth);
      data.setMaxHealth((float) value);

      if (toHeal > 0)
        targetPlayer.heal(toHeal);

      tell(sender, "setHealth", true, targetPlayer.getName(), value);
    }
  }

  @Override
  public boolean checkPermission(MinecraftServer server, ICommandSender sender) {

    return ((server.isDedicatedServer() && !(sender instanceof EntityPlayer))
        || server.isSinglePlayer() && server.worlds[0].getWorldInfo().areCommandsAllowed())
        || server.getPlayerList().getOppedPlayers()
            .getGameProfileFromName(sender.getName()) != null;
  }

  @Override
  public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender,
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
    sender.sendMessage(new TextComponentString(value));
  }
}
