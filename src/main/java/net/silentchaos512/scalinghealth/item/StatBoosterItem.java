package net.silentchaos512.scalinghealth.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.CapabilityPlayerData;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.client.particles.ModParticles;
import net.silentchaos512.scalinghealth.init.ModSounds;

import javax.annotation.Nullable;
import java.util.List;

public abstract class StatBoosterItem extends Item {
    public StatBoosterItem() {
        super(new Properties().group(ItemGroup.MISC));
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        tooltip.add(new TextComponentTranslation(this.getTranslationKey() + ".desc"));
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.RARE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ItemStack stack = playerIn.getHeldItem(handIn);

        if (!worldIn.isRemote) {
            IPlayerData data = playerIn.getCapability(CapabilityPlayerData.INSTANCE).orElseThrow(() ->
                    new IllegalStateException("Player data is null!"));

            final boolean statIncreaseAllowed = isStatIncreaseAllowed(playerIn, data);
            final int levelRequirement = getLevelCost(playerIn);

            // Does player have enough XP?
            if (playerIn.experienceLevel < levelRequirement) {
                String translationKey = "item.scalinghealth.stat_booster.notEnoughXP";
                playerIn.sendMessage(new TextComponentTranslation(translationKey, levelRequirement));
                return new ActionResult<>(EnumActionResult.PASS, stack);
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

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    abstract int getLevelCost(EntityPlayer player);

    abstract boolean isStatIncreaseAllowed(EntityPlayer player, IPlayerData data);

    abstract boolean shouldConsume(EntityPlayer player);

    abstract void extraConsumeEffect(EntityPlayer player);

    abstract void increaseStat(EntityPlayer player, ItemStack stack, IPlayerData data);

    abstract ModParticles getParticleType();

    abstract ModSounds getSoundEffect();

    private ActionResult<ItemStack> useAsConsumable(World world, EntityPlayer player, ItemStack stack, int levelRequirement, boolean consumed) {
        if (consumed) {
            world.playSound(null, player.getPosition(), SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS,
                    0.5f, 1 + 0.1f * (float) ScalingHealth.random.nextGaussian());
            stack.shrink(1);
            consumeLevels(player, levelRequirement);
            player.addStat(StatList.ITEM_USED.get(this));
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    private ActionResult<ItemStack> useAsStatIncreaseItem(World world, EntityPlayer player, ItemStack stack, IPlayerData data, int levelRequirement) {
        increaseStat(player, stack, data);
        stack.shrink(1);
        consumeLevels(player, levelRequirement);
        player.addStat(StatList.ITEM_USED.get(this));
        // TODO: Send a packet to client?
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void spawnParticlesAndPlaySound(World world, EntityPlayer player) {
        ScalingHealth.LOGGER.debug("StatBoosterItem effect!");
        getParticleType().spawn(40, player);

        float pitch = 1 + 0.1f * (float) ScalingHealth.random.nextGaussian();
        getSoundEffect().play(player, 0.5f, pitch);
    }

    private static void consumeLevels(EntityPlayer player, int amount) {
        player.experienceLevel -= amount;
    }
}
