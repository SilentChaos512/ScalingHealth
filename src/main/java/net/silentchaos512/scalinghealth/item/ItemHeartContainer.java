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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.silentchaos512.lib.util.ChatHelper;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageDataSync;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

import java.util.List;

public class ItemHeartContainer extends Item {

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        List<String> lines = ScalingHealth.localizationHelper.getDescriptionLines(this);
        if (lines.size() > 0) {
            String line = lines.get(0);
            lines.set(0, TextFormatting.WHITE + line);
            list.addAll(lines);
        }
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

            boolean healthIncreaseAllowed = Config.HEARTS_INCREASE_HEALTH && data != null
                    && (Config.PLAYER_HEALTH_MAX == 0 || data.getMaxHealth() < Config.PLAYER_HEALTH_MAX);
            int levelRequirement = getLevelsRequiredToUse(player, stack, healthIncreaseAllowed);

            // Does player have enough XP?
            if (player.experienceLevel < levelRequirement) {
                String message = ScalingHealth.localizationHelper.getSubText(this, "notEnoughXP", levelRequirement);
                ChatHelper.sendStatusMessage(player, message, true);
                return new ActionResult<>(EnumActionResult.PASS, stack);
            }

            // Heal the player (this is separate from the "healing" of the newly added heart, if that's allowed).
            boolean consumed = false;
            if (Config.HEARTS_HEALTH_RESTORED > 0 && player.getHealth() < player.getMaxHealth()) {
                float currentHealth = player.getHealth();
                player.setHealth(currentHealth + Config.HEARTS_HEALTH_RESTORED);
                consumed = true;
            }

            // End here if health increases are not allowed.
            if (!healthIncreaseAllowed) {
                if (consumed) {
                    world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 0.5f,
                            1.0f + 0.1f * (float) ScalingHealth.random.nextGaussian());
                    stack.shrink(1);
                    consumeLevels(player, levelRequirement);
                    return new ActionResult<>(EnumActionResult.SUCCESS, stack);
                } else {
                    return new ActionResult<>(EnumActionResult.PASS, stack);
                }
            }

            // Increase health, consume heart.
            data.incrementMaxHealth(2);
            stack.shrink(1);

            // Particles and sound.
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
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.5f,
                    0.7f + 0.1f * (float) ScalingHealth.random.nextGaussian());

            consumeLevels(player, levelRequirement);

            NetworkHandler.INSTANCE.sendTo(new MessageDataSync(data, player), (EntityPlayerMP) player);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private int getLevelsRequiredToUse(EntityPlayer player, ItemStack stack, boolean healthIncreaseAllowed) {
        return player.capabilities.isCreativeMode ? 0 : Config.HEART_XP_LEVEL_COST;
    }

    private void consumeLevels(EntityPlayer player, int amount) {
        player.experienceLevel -= amount;
    }
}
