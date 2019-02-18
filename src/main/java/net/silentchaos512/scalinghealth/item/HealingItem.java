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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModPotions;

import javax.annotation.Nullable;
import java.util.List;

public class HealingItem extends Item {
    private static final int USE_TIME = 5 * 20;

    private final float healAmount;
    private final int healSpeed;
    private final int effectDuration;

    public HealingItem(float healAmount, int healSpeed) {
        super(new Item.Builder().maxStackSize(16).group(ItemGroup.MISC));
        this.healAmount = healAmount;
        this.healSpeed = healSpeed;
        this.effectDuration = (int) (this.healAmount * 100 * 20 * 2 / this.healSpeed);
    }

    @Override
    public EnumAction getUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TIME;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.getHealth() < player.getMaxHealth() && !player.isPotionActive(ModPotions.BANDAGED.get())) {
            player.setActiveHand(hand);
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        }
        return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (!world.isRemote) {
            entityLiving.addPotionEffect(new PotionEffect(ModPotions.BANDAGED.get(),
                    this.effectDuration, this.healSpeed, false, false));
            stack.shrink(1);

            if (entityLiving instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entityLiving;
                player.addStat(StatList.ITEM_USED.get(this));
            }
        }
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        if (count % 10 == 0) {
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                    1.25f, (float) (1.1f + 0.05f * ScalingHealth.random.nextGaussian()));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
        tooltip.add(new TextComponentTranslation("item.scalinghealth.healing_item.value",
                (int) (this.healAmount * 100),
                this.effectDuration / 20));
        tooltip.add(new TextComponentTranslation("item.scalinghealth.healing_item.howToUse",
                USE_TIME / 20));
    }
}
