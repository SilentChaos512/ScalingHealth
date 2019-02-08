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
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.difficulty.Difficulty;

import java.util.List;

public class ItemDifficultyChanger extends Item {
    public enum Type {
        ENCHANTED, CURSED
    }

    private final Type type;

    public ItemDifficultyChanger(Type type) {
        super(new Item.Builder());
        this.type = type;
    }

    public float getEffectAmount(ItemStack stack) {
//        if (type == Type.CURSED)
//            return Config.Items.cursedHeartChange;
//        else
//            return Config.Items.enchantedHeartChange;
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        float amount = getEffectAmount(stack);
        String amountStr = (amount > 0 ? "+" : "") + String.format("%.1f", amount);
        list.add(new TextComponentTranslation("item.scalinghealth.difficulty_changer.effectDesc", amountStr));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IDifficultySource source = Difficulty.source(player);

        double particleX = player.posX;
        double particleY = player.posY + 0.65f * player.height;
        double particleZ = player.posZ;

        if (!world.isRemote) {
            float change = getEffectAmount(stack);
            source.addDifficulty(change);
            stack.shrink(1);
        }

        switch (this.type) {
            // Enchanted Heart
            case ENCHANTED:
                enchantedHeartEffects(world, player, particleX, particleY, particleZ);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            // Cursed Heart
            case CURSED:
                cursedHeartEffects(world, player, particleX, particleY, particleZ);
                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            default:
                ScalingHealth.LOGGER.error("ItemDifficultyChanger invalid type: {}", this.type);
                return new ActionResult<>(EnumActionResult.PASS, stack);
        }
    }

    private void cursedHeartEffects(World world, EntityPlayer player, double particleX, double particleY, double particleZ) {
//        for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
//            double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
//            double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
//            double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
//            ScalingHealth.proxy.spawnParticles(EnumModParticles.CURSED_HEART,
//                    new Color(0.4f, 0f, 0.6f), world, particleX, particleY, particleZ, xSpeed, ySpeed, zSpeed);
//        }
//        world.playSound(null, player.getPosition(), ModSounds.CURSED_HEART_USE,
//                SoundCategory.PLAYERS, 0.3f,
//                (float) (0.7f + 0.05f * ScalingHealth.random.nextGaussian()));
    }

    private void enchantedHeartEffects(World world, EntityPlayer player, double particleX, double particleY, double particleZ) {
//        for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
//            double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
//            double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
//            double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
//            ScalingHealth.proxy.spawnParticles(EnumModParticles.ENCHANTED_HEART,
//                    new Color(1f, 1f, 0.5f), world, particleX, particleY, particleZ, xSpeed, ySpeed, zSpeed);
//        }
//        world.playSound(null, player.getPosition(), ModSounds.ENCHANTED_HEART_USE,
//                SoundCategory.PLAYERS, 0.4f, 1.7f);
    }
}
