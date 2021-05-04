package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.particles.IParticleData;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc"));
    }

    @Override
    public Rarity getRarity(ItemStack stack) {
        return Rarity.RARE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if(usedForPet){
            if(world.isRemote)  usedForPet = false;
            return new ActionResult<>(ActionResultType.FAIL, stack);
        }

        if (!world.isRemote) {
            final boolean statIncreaseAllowed = isStatIncreaseAllowed(player);
            final int levelRequirement = getLevelCost(player);

            // Does player have enough XP?
            if (player.experienceLevel < levelRequirement) {
                String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
                player.sendMessage(new TranslationTextComponent(translationKey, levelRequirement), Util.DUMMY_UUID);
                return new ActionResult<>(ActionResultType.PASS, stack);
            }

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

        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    public void increasePetHp(PlayerEntity player, TameableEntity pet, ItemStack stack){
        //check config

        int levelRequirement = getLevelCost(player);
        if (player.experienceLevel < levelRequirement) {
            String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
            player.sendMessage(new TranslationTextComponent(translationKey, levelRequirement), Util.DUMMY_UUID);
            return;
        }

        usedForPet = true;
        pet.getCapability(PetHealthCapability.INSTANCE).ifPresent(data -> data.addHealth(SHMechanicListener.getMobMechanics().pets.petsHealthCrystalGain, pet));
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.addStat(Stats.ITEM_USED.get(this));
    }

    protected abstract int getLevelCost(PlayerEntity player);

    protected abstract boolean isStatIncreaseAllowed(PlayerEntity player);

    protected abstract boolean shouldConsume(PlayerEntity player);

    protected abstract void extraConsumeEffect(PlayerEntity player);

    protected abstract void increaseStat(PlayerEntity player);

    protected abstract IParticleData getParticleType();

    protected abstract SoundEvent getSoundEffect();

    private ActionResult<ItemStack> useAsConsumable(World world, PlayerEntity player, ItemStack stack, int levelRequirement, boolean consumed) {
        if (consumed) {
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                    0.5f, 1 + 0.1f * (float) ScalingHealth.RANDOM.nextGaussian());
            stack.shrink(1);
            consumeLevels(player, levelRequirement);
            player.addStat(Stats.ITEM_USED.get(this));
            return new ActionResult<>(ActionResultType.SUCCESS, stack);
        }
        return new ActionResult<>(ActionResultType.PASS, stack);
    }

    private ActionResult<ItemStack> useAsStatIncreaseItem(PlayerEntity player, ItemStack stack, int levelRequirement) {
        increaseStat(player);
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.addStat(Stats.ITEM_USED.get(this));
        IPlayerData.sendUpdatePacketTo(player);
        return new ActionResult<>(ActionResultType.SUCCESS, stack);
    }

    private void spawnParticlesAndPlaySound(PlayerEntity player) {
        ScalingHealth.LOGGER.debug("StatBoosterItem effect!");
        ParticleUtils.spawn(getParticleType(), 40, player);
        SoundUtils.play(player, getSoundEffect());
    }

    private static void consumeLevels(PlayerEntity player, int amount) {
        player.addExperienceLevel(-amount);
        SHPlayers.getPlayerData(player).updateStats(player);
    }
}
