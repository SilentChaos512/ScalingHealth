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

package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

import javax.annotation.Nullable;
import java.util.List;

public class HealingItem extends Item {
    private static final int USE_TIME = 5 * 20;

    private final float healAmount;
    private final int healSpeed;
    private final int effectDuration;

    public HealingItem(float healAmount, int healSpeed) {
        super(new Item.Properties().stacksTo(16));
        this.healAmount = healAmount;
        this.healSpeed = healSpeed;
        this.effectDuration = (int) (this.healAmount * 100 * 20 * 2 / this.healSpeed);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TIME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.getHealth() < player.getMaxHealth() && !player.hasEffect(Registration.BANDAGED.get())) {
            player.startUsingItem(hand);
            return InteractionResultHolder.success( stack);
        }
        return InteractionResultHolder.fail(stack);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (!world.isClientSide) {
            entityLiving.addEffect(new MobEffectInstance(Registration.BANDAGED.get(),
                    this.effectDuration, this.healSpeed, false, false));
            stack.shrink(1);

            if (entityLiving instanceof Player) {
                Player player = (Player) entityLiving;
                player.awardStat(Stats.ITEM_USED.get(this));
            }
        }
        return stack;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int count) {
        if (count % 10 == 0) {
            player.playSound(SoundEvents.ARMOR_EQUIP_LEATHER,
                    1.25f, (float) (1.1f + 0.05f * ScalingHealth.RANDOM.nextGaussian()));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.scalinghealth.healing_item.value",
                (int) (this.healAmount * 100),
                this.effectDuration / 20));
        tooltip.add(Component.translatable("item.scalinghealth.healing_item.howToUse",
                USE_TIME / 20));
    }
}
