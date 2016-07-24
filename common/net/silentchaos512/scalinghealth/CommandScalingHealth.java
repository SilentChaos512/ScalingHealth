package net.silentchaos512.scalinghealth;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.silentchaos512.scalinghealth.utils.ScalingHealthSaveStorage;

public class CommandScalingHealth implements ICommand {

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

    return "Usage: /" + getCommandName() + " difficulty|health <value> [player]";
  }

  @Override
  public List<String> getCommandAliases() {

    return Lists.newArrayList();
  }

  @Override
  public void execute(MinecraftServer server, ICommandSender sender, String[] args)
      throws CommandException {

    ScalingHealth.logHelper.debug(args);

    if (args.length < 2) {
      sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
    }

    String command = args[0];
    if (command.equals("difficulty")) {
      try {
        double value = Double.parseDouble(args[1]);
        double current = ScalingHealthSaveStorage.getDifficulty(sender.getEntityWorld());
        ScalingHealthSaveStorage.incrementDifficulty(sender.getEntityWorld(), value - current);
      } catch (NumberFormatException ex) {
        sender.addChatMessage(new TextComponentString(getCommandUsage(sender)));
      }
    } else if (command.equals("health")) {
      // TODO
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

    // TODO Auto-generated method stub
    ScalingHealth.logHelper.debug(args);
    return null;
  }

  @Override
  public boolean isUsernameIndex(String[] args, int index) {

    // TODO Auto-generated method stub
    return false;
  }

}
