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
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.ParticleUtils;
import net.silentchaos512.scalinghealth.utils.SoundUtils;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.config.SHItems;

import javax.annotation.Nullable;
import java.util.List;

public class DifficultyMutatorItem extends Item {
    public enum Type {
        ENCHANTED,
        CURSED,
        CHANCE
    }

    private final Type type;

    public DifficultyMutatorItem(Type type, Properties properties) {
        super(properties);
        this.type = type;
    }

    private double getEffectAmount(@Nullable Level world) {
        if (world == null)
            return 0;
        switch (this.type) {
            // Enchanted Heart
            case CURSED:
                return SHItems.cursedHeartAffectAmount();
            // Cursed Heart
            case ENCHANTED:
                return  SHItems.enchantedHeartAffectAmount();
            // Chance Heart
            case CHANCE:
                int max = SHItems.chanceHeartAffectAmount();
                // random equation:
                // (Math.random()*((max-min)+1))+min
                // max is max, min is -max
                int v = (int) (Math.random() * (max * 2 + 1) - max);
                if(v == 0)
                    return -max;
                else if(v > 0 && v != max)
                    return -v;
                else
                    return v;
            default:
                ScalingHealth.LOGGER.error("DifficultyMutatorItem invalid type: {}", this.type);
                return 0;
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> list, TooltipFlag flag) {
        double amount = getEffectAmount(world);
        if(this.type != Type.CHANCE) {
            String amountStr = (amount > 0 ? "+" : "") + String.format("%.1f", amount);
            list.add(new TranslatableComponent("item.scalinghealth.difficulty_changer.effectDesc", amountStr));
        }
        else
            list.add(new TranslatableComponent("item.scalinghealth.difficulty_changer.effectDesc", "?"));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if(!EnabledFeatures.difficultyEnabled())
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);

        IDifficultySource source = SHDifficulty.source(player);

        float change = (float) getEffectAmount(world);
        if (!world.isClientSide) {
            source.addDifficulty(change);
            stack.shrink(1);
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        switch (this.type) {
            // Enchanted Heart
            case ENCHANTED:
                enchantedHeartEffects(world, player);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            // Cursed Heart
            case CURSED:
                cursedHeartEffects(world, player);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            // Chance Heart
            case CHANCE:
                chanceHeartEffects(world, player, (int) change);
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            default:
                ScalingHealth.LOGGER.error("DifficultyMutatorItem invalid type: {}", this.type);
                return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }
    }

    private void cursedHeartEffects(Level world, Player player) {
        if(world.isClientSide) {
            ParticleUtils.spawn(Registration.CURSED_HEART_PARTICLE.get(), 40, player);
            SoundUtils.play(player, Registration.CURSED_HEART_USE.get());
        }
    }

    private void enchantedHeartEffects(Level world, Player player) {
        if(world.isClientSide) {
            ParticleUtils.spawn(Registration.ENCHANTED_HEART_PARTICLE.get(), 40, player);
            SoundUtils.play(player, Registration.ENCHANTED_HEART_USE.get());
        }
    }

    private void chanceHeartEffects(Level world, Player player, int diffChange) {
        int max = SHItems.chanceHeartAffectAmount();
        if(diffChange == max)
            cursedHeartEffects(world, player);
        else if(diffChange == -max)
            enchantedHeartEffects(world, player);
        else {
            SoundUtils.play(player, Registration.ENCHANTED_HEART_USE.get());
            ParticleUtils.spawn(Registration.ENCHANTED_HEART_PARTICLE.get(), 40 * -diffChange/max, player);
        }
    }
}
