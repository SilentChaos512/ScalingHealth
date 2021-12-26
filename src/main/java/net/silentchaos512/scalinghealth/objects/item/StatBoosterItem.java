package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.Util;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.capability.PetHealthCapability;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.utils.ParticleUtils;
import net.silentchaos512.scalinghealth.utils.SoundUtils;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;

import javax.annotation.Nullable;
import java.util.List;

public abstract class StatBoosterItem extends Item {
    public StatBoosterItem(Properties properties) {
        super(properties);
    }

    private boolean usedForPet = false;

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(new TranslatableComponent(this.getDescriptionId() + ".desc"));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand handIn) {
        ItemStack stack = player.getItemInHand(handIn);
        if(usedForPet) {
            if(world.isClientSide) usedForPet = false;
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }


        final boolean statIncreaseAllowed = isStatIncreaseAllowed(player);
        final int levelRequirement = getLevelCost(player);

        // Does player have enough XP?
        if (player.experienceLevel < levelRequirement) {
            if (world.isClientSide) {
                String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
                player.sendMessage(new TranslatableComponent(translationKey, levelRequirement), Util.NIL_UUID);
            }
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (!world.isClientSide) {
            // May be used as a healing item even if there is no stat increase
            final boolean consumed = shouldConsume(player);
            if (consumed) {
                extraConsumeEffect(player);
            }

            // End here if stat increases are not allowed
            if (!statIncreaseAllowed) {
                return useAsConsumable(world, player, stack, levelRequirement, consumed);
            }

            // Increase stat, consume item
            return useAsStatIncreaseItem(player, stack, levelRequirement);
        }
        else if(shouldConsume(player) || isStatIncreaseAllowed(player))
            spawnParticlesAndPlaySound(player);

        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    public void increasePetHp(Player player, TamableAnimal pet, ItemStack stack){
        //check config

        int levelRequirement = getLevelCost(player);
        if (player.experienceLevel < levelRequirement) {
            String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
            player.sendMessage(new TranslatableComponent(translationKey, levelRequirement), Util.NIL_UUID);
            return;
        }

        usedForPet = true;
        pet.getCapability(PetHealthCapability.INSTANCE).ifPresent(data -> data.addHealth(SHMechanicListener.getMobMechanics().pets.petsHealthCrystalGain, pet));
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.awardStat(Stats.ITEM_USED.get(this));
    }

    protected abstract int getLevelCost(Player player);

    protected abstract boolean isStatIncreaseAllowed(Player player);

    protected abstract boolean shouldConsume(Player player);

    protected abstract void extraConsumeEffect(Player player);

    protected abstract void increaseStat(Player player);

    protected abstract ParticleOptions getParticleType();

    protected abstract SoundEvent getSoundEffect();

    private InteractionResultHolder<ItemStack> useAsConsumable(Level world, Player player, ItemStack stack, int levelRequirement, boolean consumed) {
        if (consumed) {
            world.playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS,
                    0.5f, 1 + 0.1f * (float) ScalingHealth.RANDOM.nextGaussian());
            stack.shrink(1);
            consumeLevels(player, levelRequirement);
            player.awardStat(Stats.ITEM_USED.get(this));
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }
        return new InteractionResultHolder<>(InteractionResult.PASS, stack);
    }

    private InteractionResultHolder<ItemStack> useAsStatIncreaseItem(Player player, ItemStack stack, int levelRequirement) {
        increaseStat(player);
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.awardStat(Stats.ITEM_USED.get(this));
        IPlayerData.sendUpdatePacketTo(player);
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
    }

    private void spawnParticlesAndPlaySound(Player player) {
        ScalingHealth.LOGGER.debug("StatBoosterItem effect!");
        ParticleUtils.spawn(getParticleType(), 40, player);
        SoundUtils.play(player, getSoundEffect());
    }

    private static void consumeLevels(Player player, int amount) {
        player.giveExperienceLevels(-amount);
        SHPlayers.getPlayerData(player).updateStats(player);
    }
}
