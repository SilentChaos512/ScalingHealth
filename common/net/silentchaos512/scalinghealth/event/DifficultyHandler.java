package net.silentchaos512.scalinghealth.event;

import java.util.List;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageMarkBlight;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class DifficultyHandler {

  public static DifficultyHandler INSTANCE = new DifficultyHandler();

  @SubscribeEvent
  public void onMobSpawn(LivingUpdateEvent event) {

    // Increase mob health and make blights?
    if (!(event.getEntity() instanceof EntityLivingBase))
      return;

    EntityLivingBase entityLiving = (EntityLivingBase) event.getEntity();

    //@formatter:off
    if (entityLiving.world.isRemote
        || entityLiving instanceof EntityPlayer
        || entityBlacklistedFromHealthIncrease(entityLiving)
        || !canIncreaseEntityHealth(entityLiving))
      return;
    //@formatter:on

    boolean makeBlight = increaseEntityHealth(entityLiving);
    if (makeBlight)
      makeEntityBlight(entityLiving, ScalingHealth.random);
  }

  @SubscribeEvent
  public void onMobDeath(LivingDeathEvent event) {

    EntityLivingBase entity = event.getEntityLiving();
    DamageSource source = event.getSource();

    if (source.getTrueSource() instanceof EntityPlayer) {
      if (entity instanceof IMob) {
        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        PlayerData data = SHPlayerDataHandler.get(player);
        data.incrementDifficulty(ConfigScalingHealth.DIFFICULTY_PER_KILL);
      }
    }
  }

  private boolean increaseEntityHealth(EntityLivingBase entityLiving) {

    float difficulty = (float) ConfigScalingHealth.AREA_DIFFICULTY_MODE
        .getAreaDifficulty(entityLiving.world, entityLiving.getPosition());
    Random rand = ScalingHealth.random;
    boolean makeBlight = false;

    // Make blight?
    if (!entityBlacklistedFromBecomingBlight(entityLiving)) {
      float chance = (float) (difficulty / ConfigScalingHealth.DIFFICULTY_MAX
          * ConfigScalingHealth.BLIGHT_CHANCE_MULTIPLIER);
      if (rand.nextFloat() < chance) {
        makeBlight = true;
        difficulty *= 3;
      }
    }

    float genAddedHealth = difficulty;
    float genAddedDamage = 0;
    float baseMaxHealth = (float) entityLiving
        .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();

    if (entityLiving instanceof IMob)
      genAddedHealth *= ConfigScalingHealth.DIFFICULTY_GENERIC_HEALTH_MULTIPLIER;
    else
      genAddedHealth *= ConfigScalingHealth.DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER;

    difficulty -= genAddedHealth;

    if (difficulty > 0) {
      float diffIncrease = difficulty * rand.nextFloat();
      difficulty -= diffIncrease;
      genAddedHealth += diffIncrease;
    }

    // Increase attack damage.
    if (difficulty > 0) {
      float diffIncrease = difficulty * rand.nextFloat();
      difficulty -= diffIncrease;
      genAddedDamage = diffIncrease * ConfigScalingHealth.DIFFICULTY_DAMAGE_MULTIPLIER;
    }

    if (difficulty > 0) {
      // TODO: Random potion effects? (DLEventHandler:217)
    }

    // Apply extra health and damage.
    float healthMulti = 1f;
    float healthScaleDiff = Math.max(0, baseMaxHealth - 20f);
    switch (ConfigScalingHealth.MOB_HEALTH_SCALING_MODE) {
      case ADD:
        ModifierHandler.setMaxHealth(entityLiving, genAddedHealth + baseMaxHealth, 0);
        break;
      case MULTI:
        healthMulti = genAddedHealth / 20f;
        ModifierHandler.setMaxHealth(entityLiving, healthMulti + baseMaxHealth, 1);
        break;
      case MULTI_HALF:
        healthMulti = genAddedHealth / (20f + healthScaleDiff * 0.5f);
        ModifierHandler.setMaxHealth(entityLiving, healthMulti + baseMaxHealth, 1);
        break;
      case MULTI_QUARTER:
        healthMulti = genAddedHealth / (20f + healthScaleDiff * 0.75f);
        ModifierHandler.setMaxHealth(entityLiving, healthMulti + baseMaxHealth, 1);
        break;
      default:
        ScalingHealth.logHelper.severe("Unknown mob health scaling mode: "
            + ConfigScalingHealth.MOB_HEALTH_SCALING_MODE.name());
        break;
    }
    ModifierHandler.addAttackDamage(entityLiving, genAddedDamage, 0);

    // Heal.
    entityLiving.setHealth(entityLiving.getMaxHealth());

    // ScalingHealth.logHelper.info(
    // entityLiving.getName() + ": Health +" + genAddedHealth + ", Damage +" + genAddedDamage);

    return makeBlight;
  }

  private void makeEntityBlight(EntityLivingBase entityLiving, Random rand) {

    //@formatter:off
    BlightHandler.markBlight(entityLiving);
    BlightHandler.spawnBlightFire(entityLiving);

    // ===============
    // Potions Effects
    // ===============

    // Invisibility
    if (ConfigScalingHealth.BLIGHT_INVISIBLE)
      entityLiving.addPotionEffect(new PotionEffect(
          MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    // Fire Resistance
    if (ConfigScalingHealth.BLIGHT_FIRE_RESIST)
      entityLiving.addPotionEffect(new PotionEffect(
          MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    // Speed
    if (ConfigScalingHealth.BLIGHT_AMP_SPEED > 0)
      entityLiving.addPotionEffect(new PotionEffect(
          MobEffects.SPEED, Integer.MAX_VALUE, ConfigScalingHealth.BLIGHT_AMP_SPEED, true, false));
    // Strength
    if (ConfigScalingHealth.BLIGHT_AMP_STRENGTH > 0)
      entityLiving.addPotionEffect(new PotionEffect(
          MobEffects.STRENGTH, Integer.MAX_VALUE, ConfigScalingHealth.BLIGHT_AMP_STRENGTH, true, false));

    // ================
    // Random Equipment
    // ================

    if (entityLiving instanceof EntityLiving) {
      EntityLiving entity = (EntityLiving) entityLiving;
      int i = rand.nextInt(2);
      float f = 0.5f;

      for (int j = 0; j < 3; ++j)
        if (rand.nextFloat() < 0.095f)
          ++i;

      for (int j = 3; j >= 0; --j) {
        EntityEquipmentSlot slot = EntityEquipmentSlot.values()[j + 2];
        ItemStack stack = entity.getItemStackFromSlot(slot); // FIXME?

        if (j < 3 && rand.nextFloat() < f) break;

        if (stack == null) {
          ItemStack stack1 = selectArmorForSlot(j + 1, i);
          if (stack1 != null)
            entity.setItemStackToSlot(slot, stack1);
        }
      }
    }

    for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
      ItemStack stack = entityLiving.getItemStackFromSlot(slot);
      if (stack != null)
        EnchantmentHelper.addRandomEnchantment(rand, stack, 30, false);
    }

    // ===============
    // Special Effects
    // ===============

    if (ConfigScalingHealth.BLIGHT_SUPERCHARGE_CREEPERS && entityLiving instanceof EntityCreeper) {
      ((EntityCreeper) entityLiving)
          .onStruckByLightning(new EntityLightningBolt(entityLiving.world,
              entityLiving.posX, entityLiving.posY, entityLiving.posZ, true));
    }

    // Notify clients
    MessageMarkBlight message = new MessageMarkBlight(entityLiving);
    NetworkHandler.INSTANCE.sendToAllAround(message, new TargetPoint(entityLiving.dimension,
        entityLiving.posX, entityLiving.posY, entityLiving.posZ, 128));

    // @formatter:on
  }

  private boolean entityBlacklistedFromHealthIncrease(EntityLivingBase entityLiving) {

    //@formatter:off
    if (entityLiving == null) return true;
    if (!ConfigScalingHealth.ALLOW_HOSTILE_EXTRA_HEALTH && entityLiving instanceof EntityMob)
      return true;
    if (!ConfigScalingHealth.ALLOW_PEACEFUL_EXTRA_HEALTH && entityLiving instanceof EntityAnimal)
      return true;

    String entityId = EntityList.getEntityString(entityLiving);
    List<String> blacklist = ConfigScalingHealth.getMobHealthBlacklist();

    if (entityId == null || blacklist == null) return false;

    return blacklist.contains(entityId);
    //@formatter:on
  }

  private boolean canIncreaseEntityHealth(EntityLivingBase entityLiving) {

    return entityLiving.getAttributeMap() != null && entityLiving.ticksExisted > 1
        && entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
            .getModifier(ModifierHandler.MODIFIER_ID_HEALTH) == null;
  }

  private boolean entityBlacklistedFromBecomingBlight(EntityLivingBase entityLiving) {

    // @formatter:off
    if (entityLiving == null) return true;

    String entityId = EntityList.getEntityString(entityLiving);
    List<String> blacklist = ConfigScalingHealth.getMobBlightBlacklist();

    if (entityId == null || blacklist == null) return false;

    return blacklist.contains(entityId);
    //@formatter:on
  }

  private ItemStack selectArmorForSlot(int slot, int tier) {

    Item item = null;
    switch (slot) {
      case 4:
        if (tier == 0)
          item = Items.LEATHER_HELMET;
        else if (tier == 1)
          item = Items.GOLDEN_HELMET;
        else if (tier == 2)
          item = Items.CHAINMAIL_HELMET;
        else if (tier == 3)
          item = Items.IRON_HELMET;
        else if (tier == 4)
          item = Items.DIAMOND_HELMET;
        break;
      case 3:
        if (tier == 0)
          item = Items.LEATHER_CHESTPLATE;
        else if (tier == 1)
          item = Items.GOLDEN_CHESTPLATE;
        else if (tier == 2)
          item = Items.CHAINMAIL_CHESTPLATE;
        else if (tier == 3)
          item = Items.IRON_CHESTPLATE;
        else if (tier == 4)
          item = Items.DIAMOND_CHESTPLATE;
        break;
      case 2:
        if (tier == 0)
          item = Items.LEATHER_LEGGINGS;
        else if (tier == 1)
          item = Items.GOLDEN_LEGGINGS;
        else if (tier == 2)
          item = Items.CHAINMAIL_LEGGINGS;
        else if (tier == 3)
          item = Items.IRON_LEGGINGS;
        else if (tier == 4)
          item = Items.DIAMOND_LEGGINGS;
        break;
      case 1:
        if (tier == 0)
          item = Items.LEATHER_BOOTS;
        else if (tier == 1)
          item = Items.GOLDEN_BOOTS;
        else if (tier == 2)
          item = Items.CHAINMAIL_BOOTS;
        else if (tier == 3)
          item = Items.IRON_BOOTS;
        else if (tier == 4)
          item = Items.DIAMOND_BOOTS;
        break;
    }

    return item == null ? StackHelper.empty() : new ItemStack(item);
  }
}
