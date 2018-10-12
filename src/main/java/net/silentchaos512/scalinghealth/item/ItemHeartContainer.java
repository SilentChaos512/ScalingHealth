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

package net.silentchaos512.scalinghealth.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.silentchaos512.lib.util.ChatHelper;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.init.ModSounds;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageDataSync;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemHeartContainer extends Item {

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<String> list, ITooltipFlag flag) {
        list.add(ScalingHealth.i18n.subText(this, "desc"));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote) {
            PlayerData data = SHPlayerDataHandler.get(player);
            if (data == null) return ActionResult.newResult(EnumActionResult.PASS, stack);

            final boolean healthIncreaseAllowed = isHealthIncreaseAllowed(data);
            final int levelRequirement = getLevelsRequiredToUse(player, stack, healthIncreaseAllowed);

            // Does player have enough XP?
            if (player.experienceLevel < levelRequirement) {
                ChatHelper.translateStatus(player, ScalingHealth.i18n.getKey(this, "notEnoughXP"), true, levelRequirement);
                return ActionResult.newResult(EnumActionResult.PASS, stack);
            }

            // Heal the player (this is separate from the "healing" of the newly added heart, if that's allowed).
            final boolean consumed = Config.Items.Heart.healthRestored > 0 && player.getHealth() < player.getMaxHealth();
            if (consumed) {
                doExtraHealing(player);
            }

            // End here if health increases are not allowed.
            if (!healthIncreaseAllowed) {
                return useAsHealingItem(world, player, stack, levelRequirement, consumed);
            }

            // Increase health, consume heart.
            useForHealthIncrease(world, player, stack, data, levelRequirement);
        }
        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    private static void doExtraHealing(EntityPlayer player) {
        int current = (int) player.getHealth();
        EntityHelper.heal(player, Config.Items.Heart.healthRestored, Config.Items.Heart.healingEvent);
        int newHealth = (int) player.getHealth();
        if (current + Config.Items.Heart.healthRestored != newHealth) {
            ScalingHealth.logHelper.warn("Another mod seems to have canceled healing from a heart container (player {})", player.getName());
        }
    }

    private void incrementUseStat(EntityPlayer player) {
        StatBase useStat = StatList.getObjectUseStats(this);
        if (useStat != null) player.addStat(useStat);
    }

    @Nonnull
    private ActionResult<ItemStack> useAsHealingItem(World world, EntityPlayer player, ItemStack stack, int levelRequirement, boolean consumed) {
        if (consumed) {
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                    0.5f, 1.0f + 0.1f * (float) ScalingHealth.random.nextGaussian());
            stack.shrink(1);
            consumeLevels(player, levelRequirement);
            incrementUseStat(player);
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        } else {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }
    }

    private void useForHealthIncrease(World world, EntityPlayer player, ItemStack stack, PlayerData data, int levelRequirement) {
        data.incrementMaxHealth(2);
        stack.shrink(1);
        spawnParticlesAndPlaySound(world, player);
        consumeLevels(player, levelRequirement);
        incrementUseStat(player);
        NetworkHandler.INSTANCE.sendTo(new MessageDataSync(data, player), (EntityPlayerMP) player);
    }

    private static int getLevelsRequiredToUse(EntityPlayer player, ItemStack stack, boolean healthIncreaseAllowed) {
        return player.capabilities.isCreativeMode ? 0 : Config.Items.Heart.xpCost;
    }

    private static void consumeLevels(EntityPlayer player, int amount) {
        player.experienceLevel -= amount;
    }

    private static boolean isHealthIncreaseAllowed(PlayerData data) {
        return Config.Items.Heart.increaseHealth
                && (Config.Player.Health.maxHealth == 0
                || data.getMaxHealth() < Config.Player.Health.maxHealth);
    }

    private static void spawnParticlesAndPlaySound(World world, EntityPlayer player) {
        double particleX = player.posX;
        double particleY = player.posY + 0.65f * player.height;
        double particleZ = player.posZ;
        for (int i = 0; i < 40 - 10 * ScalingHealth.proxy.getParticleSettings(); ++i) {
            double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
            double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
            double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
            ScalingHealth.proxy.spawnParticles(EnumModParticles.HEART_CONTAINER,
                    new Color(1f, 0f, 0f), world, particleX, particleY, particleZ, xSpeed, ySpeed, zSpeed);
        }
        ScalingHealth.proxy.playSoundOnClient(player, ModSounds.HEART_CONTAINER_USE,
                0.5f, 1.0f + 0.1f * (float) ScalingHealth.random.nextGaussian());
    }
}
