package net.silentchaos512.scalinghealth.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.silentchaos512.lib.command.CommandBaseSL;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.ScalingHealthAPI;
import net.silentchaos512.scalinghealth.event.BlightHandler;
import net.silentchaos512.scalinghealth.event.DifficultyHandler;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageMarkBlight;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;

public class CommandRecalculate extends CommandBaseSL {
    @Override
    public String getName() {
        return "sh_recalculate";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return TextFormatting.RED + "Usage: /" + getName();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) {
        // no arguments
        int processed = recalculateAllEntities(sender.getEntityWorld());
        ScalingHealth.LOGGER.info("Recalculated difficulty for {} entities, see details above", processed);
        ITextComponent message = new TextComponentTranslation("command.scalinghealth.recalculate.result", processed);
        sender.sendMessage(message);
    }

    private static int recalculateAllEntities(World world) {
        int processed = 0;

        for (EntityLivingBase entity : world.getEntities(EntityLivingBase.class, e -> !(e instanceof EntityPlayer))) {
            // Old entity properties, mostly for logging
            boolean oldBlightFlag = BlightHandler.isBlight(entity);
            double oldDifficulty = ScalingHealthAPI.getEntityDifficulty(entity);
            double oldMaxHealth = ModifierHandler.getHealthModifier(entity);
            double oldAttackDamage = ModifierHandler.getDamageModifier(entity);

            // Remove blight marker from NBT and all potion effects
            BlightHandler.markBlight(entity, false);
            entity.clearActivePotions();

            if (DifficultyHandler.INSTANCE.recalculate(entity)) {
                ++processed;

                boolean newBlightFlag = BlightHandler.isBlight(entity);
                double newDifficulty = ScalingHealthAPI.getEntityDifficulty(entity);
                double newMaxHealth = ModifierHandler.getHealthModifier(entity);
                double newAttackDamage = ModifierHandler.getDamageModifier(entity);

                // Update blight status on clients
                if (oldBlightFlag != newBlightFlag) {
                    IMessage message = new MessageMarkBlight(entity, newBlightFlag);
                    NetworkHandler.INSTANCE.sendToAllAround(message, new NetworkRegistry.TargetPoint(entity.dimension, entity.posX, entity.posY, entity.posZ, 128));
                }

                ScalingHealth.LOGGER.info("Recalculate {}: blight {} -> {}; difficulty {} -> {}; max health {} -> {}; attack damage {} -> {}",
                        entity.getName(), oldBlightFlag, newBlightFlag, oldDifficulty, newDifficulty, oldMaxHealth, newMaxHealth, oldAttackDamage, newAttackDamage);
            }
        }

        return processed;
    }
}
