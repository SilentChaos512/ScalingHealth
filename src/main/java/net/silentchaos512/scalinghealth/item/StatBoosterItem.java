package net.silentchaos512.scalinghealth.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.init.ModSounds;

import javax.annotation.Nullable;
import java.util.List;

public abstract class StatBoosterItem extends Item {
    public StatBoosterItem() {
        super(new Properties().group(ScalingHealth.SH));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc"));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            IPlayerData data = playerIn.getCapability(PlayerDataCapability.INSTANCE).orElseThrow(() ->
                    new IllegalStateException("Player data is null!"));

            final boolean statIncreaseAllowed = isStatIncreaseAllowed(playerIn, data);
            final int levelRequirement = getLevelCost(playerIn);

            // Does player have enough XP?
            if (playerIn.experienceLevel < levelRequirement) {
                String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
                playerIn.sendMessage(new TranslationTextComponent(translationKey, levelRequirement));
                return new ActionResult<>(ActionResultType.PASS, stack);
            }

            // May be used as a healing item even if there is no stat increase
            final boolean consumed = shouldConsume(playerIn);
            if (consumed) {
                extraConsumeEffect(playerIn);
            }

            // End here is stat increases are not allowed
            if (!statIncreaseAllowed) {
                return useAsConsumable(worldIn, playerIn, stack, levelRequirement, consumed);
            }

            // Increase stat, consume item
            return useAsStatIncreaseItem(worldIn, playerIn, stack, data, levelRequirement);
        } else {
            spawnParticlesAndPlaySound(worldIn, playerIn);
        }

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    abstract int getLevelCost(PlayerEntity player);

    abstract boolean isStatIncreaseAllowed(PlayerEntity player, IPlayerData data);

    abstract boolean shouldConsume(PlayerEntity player);

    abstract void extraConsumeEffect(PlayerEntity player);

    abstract void increaseStat(PlayerEntity player, ItemStack stack, IPlayerData data);

    abstract ModParticles getParticleType();

    abstract ModSounds getSoundEffect();

    private ActionResult<ItemStack> useAsConsumable(World world, PlayerEntity player, ItemStack stack, int levelRequirement, boolean consumed) {
        if (consumed) {
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                    0.5f, 1 + 0.1f * (float) ScalingHealth.random.nextGaussian());
            stack.shrink(1);
            consumeLevels(player, levelRequirement);
            player.addStat(Stats.ITEM_USED.get(this));
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private ActionResult<ItemStack> useAsStatIncreaseItem(World world, PlayerEntity player, ItemStack stack, IPlayerData data, int levelRequirement) {
        increaseStat(player, stack, data);
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.addStat(Stats.ITEM_USED.get(this));
        IPlayerData.sendUpdatePacketTo(player);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private void spawnParticlesAndPlaySound(World world, PlayerEntity player) {
        ScalingHealth.LOGGER.debug("StatBoosterItem effect!");
        getParticleType().spawn(40, player);

        float pitch = 1 + 0.1f * (float) ScalingHealth.random.nextGaussian();
        getSoundEffect().play(player, 0.5f, pitch);
    }

    private static void consumeLevels(PlayerEntity player, int amount) {
        //player.experienceLevel -= amount;
        player.addExperienceLevel(-amount);
    }
}
