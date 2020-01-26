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
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultySource;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.init.ModSounds;
import net.silentchaos512.scalinghealth.utils.SHDifficulty;
import net.silentchaos512.scalinghealth.utils.SHItems;

import javax.annotation.Nullable;
import java.util.List;

public class DifficultyMutatorItem extends Item {
    public enum Type {
        ENCHANTED, CURSED, CHANCE
    }

    private final Type type;

    public DifficultyMutatorItem(Type type) {
        super(new Item.Properties().group(ScalingHealth.SH));
        this.type = type;
    }

    private int getEffectAmount(@Nullable World world) {
        if (world == null)
            return 0;
        switch (this.type) {
            // Enchanted Heart
            case CURSED:
                return SHItems.cursedHeartAffectAmount(world);
            // Cursed Heart
            case ENCHANTED:
                return  SHItems.enchantedHeartAffectAmount(world);
            // Chance Heart
            case CHANCE:
                int max = SHItems.chanceHeartAffectAmount(world);
                // random equation:
                // (Math.random()*((max-min)+1))+min
                // max is max, min is -max
                int v = (int)(Math.random()*((max*2)+1))-max;
                if(v == 0)
                    return -max;
                else if(v > 0 && v != max)
                    return -v;
                else
                    return v;

                //1 in 21 odds of getting +10 difficulty, 2 in 21 odds of getting any # from -1 to -10 difficulty
            default:
                ScalingHealth.LOGGER.error("DifficultyMutatorItem invalid type: {}", this.type);
                return 0;
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> list, ITooltipFlag flag) {
        double amount = getEffectAmount(world);
        if(this.type != Type.CHANCE){
            String amountStr = (amount > 0 ? "+" : "") + String.format("%.1f", amount);
            list.add(new TranslationTextComponent("item.scalinghealth.difficulty_changer.effectDesc", amountStr));
        }
        else{
            list.add(new TranslationTextComponent("item.scalinghealth.difficulty_changer.effectDesc", "?"));
        }
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.EPIC;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);
        IDifficultySource source = SHDifficulty.source(player);

        float change = getEffectAmount(world);
        if (!world.isRemote) {
            source.addDifficulty(change);
            stack.shrink(1);
            player.addStat(Stats.ITEM_USED.get(this));
        }

        switch (this.type) {
            // Enchanted Heart
            case ENCHANTED:
                enchantedHeartEffects(world, player);
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            // Cursed Heart
            case CURSED:
                cursedHeartEffects(world, player);
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            // Chance Heart
            case CHANCE:
                chanceHeartEffects(world, player, change);
                return new ActionResult<>(ActionResultType.SUCCESS, stack);
            default:
                ScalingHealth.LOGGER.error("DifficultyMutatorItem invalid type: {}", this.type);
                return new ActionResult<>(ActionResultType.PASS, stack);
        }
    }

    private void cursedHeartEffects(World world, PlayerEntity player) {
        if(world.isRemote){
            ScalingHealth.LOGGER.debug("Spawned Particles on client");
            ModParticles.CURSED_HEART.spawn(40, player);
            ModSounds.CURSED_HEART_USE.play(player);
        }
    }

    private void enchantedHeartEffects(World world, PlayerEntity player) {
        if(world.isRemote){
            ScalingHealth.LOGGER.debug("Spawned Particles on client");
            ModParticles.ENCHANTED_HEART.spawn(40, player);
            ModSounds.ENCHANTED_HEART_USE.play(player);
        }
    }

    private void chanceHeartEffects(World world, PlayerEntity player, float diffChange){
        int max = SHItems.chanceHeartAffectAmount(world);
        if(diffChange == max){
            cursedHeartEffects(world, player);
            ModSounds.CURSED_HEART_USE.play(player);
        }
        else if(diffChange == -max){
            enchantedHeartEffects(world, player);
            ModSounds.ENCHANTED_HEART_USE.play(player);
        }
        else{
            ModSounds.ENCHANTED_HEART_USE.play(player);
            ModParticles.ENCHANTED_HEART.spawn(40 * ((int) -diffChange)/max, player);
        }
    }
}
