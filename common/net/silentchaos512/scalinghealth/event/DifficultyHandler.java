package net.silentchaos512.scalinghealth.event;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.event.BlightSpawnEvent;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageMarkBlight;
import net.silentchaos512.scalinghealth.utils.EquipmentTierMap;
import net.silentchaos512.scalinghealth.utils.MobPotionMap;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

public class DifficultyHandler {

  public static DifficultyHandler INSTANCE = new DifficultyHandler();

  public static int POTION_APPLY_TIME = 10 * 1200;

  public MobPotionMap potionMap = new MobPotionMap();

  public void initPotionMap() {

    potionMap.put(MobEffects.STRENGTH, 30, 0);
    potionMap.put(MobEffects.SPEED, 10, 0);
    potionMap.put(MobEffects.SPEED, 30, 1);
    potionMap.put(MobEffects.FIRE_RESISTANCE, 10, 0);
    potionMap.put(MobEffects.INVISIBILITY, 20, 0);
    potionMap.put(MobEffects.RESISTANCE, 30, 0);
  }

  @SubscribeEvent
  public void onMobSpawn(LivingUpdateEvent event) {

    // Increase mob health and make blights?
    if (!(event.getEntity() instanceof EntityLiving))
      return;

    EntityLiving entityLiving = (EntityLiving) event.getEntity();

    //@formatter:off
    if (entityLiving.world.isRemote
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

    EntityLivingBase killedEntity = event.getEntityLiving();
    DamageSource source = event.getSource();

    // Killed by player?
    if (source.getTrueSource() instanceof EntityPlayer) {
      // Is hostile mob?
      if (killedEntity instanceof IMob) {
        EntityPlayer player = (EntityPlayer) source.getTrueSource();
        PlayerData data = SHPlayerDataHandler.get(player);
        // Boss or not? Change difficulty accordingly.
        if (killedEntity.isNonBoss())
          data.incrementDifficulty(ConfigScalingHealth.DIFFICULTY_PER_KILL);
        else
          data.incrementDifficulty(ConfigScalingHealth.DIFFICULTY_PER_BOSS_KILL);
      }
    }
  }

  private boolean increaseEntityHealth(EntityLivingBase entityLiving) {

    float difficulty = (float) ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(entityLiving.world, entityLiving.getPosition());
    Random rand = ScalingHealth.random;
    boolean makeBlight = false;
    boolean isHostile = entityLiving instanceof IMob;

    // Make blight?
    if (!entityBlacklistedFromBecomingBlight(entityLiving)) {
      float chance = (float) (difficulty / ConfigScalingHealth.DIFFICULTY_MAX * ConfigScalingHealth.BLIGHT_CHANCE_MULTIPLIER);
      if (rand.nextFloat() < chance) {
        makeBlight = true;
        difficulty *= 3;
      }
    }

    float genAddedHealth = difficulty;
    float genAddedDamage = 0;
    float baseMaxHealth = (float) entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
    float healthMultiplier = isHostile ? ConfigScalingHealth.DIFFICULTY_GENERIC_HEALTH_MULTIPLIER
        : ConfigScalingHealth.DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER;

    genAddedHealth *= healthMultiplier;

    difficulty -= genAddedHealth;

    if (difficulty > 0) {
      float diffIncrease = 2 * healthMultiplier * difficulty * rand.nextFloat();
      difficulty -= diffIncrease;
      genAddedHealth += diffIncrease;
    }

    // Increase attack damage.
    if (difficulty > 0) {
      float diffIncrease = difficulty * rand.nextFloat();
      difficulty -= diffIncrease;
      genAddedDamage = diffIncrease * ConfigScalingHealth.DIFFICULTY_DAMAGE_MULTIPLIER;
    }

    // Random potion effect
    float potionChance = isHostile ? ConfigScalingHealth.POTION_CHANCE_HOSTILE
        : ConfigScalingHealth.POTION_CHANCE_PASSIVE;
    if (difficulty > 0 && rand.nextFloat() < potionChance) {
      MobPotionMap.PotionEntry pot = potionMap.getRandom(rand, (int) difficulty);
      if (pot != null) {
        difficulty -= pot.cost;
        entityLiving.addPotionEffect(new PotionEffect(pot.potion, POTION_APPLY_TIME));
      }
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
        ScalingHealth.logHelper.severe("Unknown mob health scaling mode: " + ConfigScalingHealth.MOB_HEALTH_SCALING_MODE.name());
        break;
    }
    ModifierHandler.addAttackDamage(entityLiving, genAddedDamage, 0);

    // Heal.
    entityLiving.setHealth(entityLiving.getMaxHealth());

    // ScalingHealth.logHelper.info(
    // entityLiving.getName() + ": Health +" + genAddedHealth + ", Damage +" + genAddedDamage);

    return makeBlight;
  }

  private void makeEntityBlight(EntityLiving entityLiving, Random rand) {

    BlightSpawnEvent event = new BlightSpawnEvent.Pre((EntityLiving) entityLiving, entityLiving.world, (float) entityLiving.posX, (float) entityLiving.posY,
        (float) entityLiving.posZ);
    if (MinecraftForge.EVENT_BUS.post(event)) {
      // Someone canceled the "blightification"
      return;
    }

    //@formatter:off
    BlightHandler.markBlight(entityLiving);
    BlightHandler.spawnBlightFire(entityLiving);

    // ==============
    // Potion Effects
    // ==============

    BlightHandler.applyBlightPotionEffects(entityLiving);

    // ================
    // Random Equipment
    // ================

    if (entityLiving instanceof EntityLiving) {
      EntityLiving entity = (EntityLiving) entityLiving;

      // Select a tier (0 to 4)
      final int highestTier = 4;
      final int commonTier = ConfigScalingHealth.BLIGHT_EQUIPMENT_HIGHEST_COMMON_TIER;
      int tier = rand.nextInt(1 + commonTier);
      for (int j = 0; j < highestTier - commonTier; ++j) {
        if (rand.nextFloat() < ConfigScalingHealth.BLIGHT_EQUIPMENT_TIER_UP_CHANCE) {
          ++tier;
        }
      }
      tier = MathHelper.clamp(tier, 0, highestTier);

      float pieceChance = ConfigScalingHealth.BLIGHT_EQUIPMENT_ARMOR_PIECE_CHANCE;

      // Armor slots
      for (EntityEquipmentSlot slot : ORDERED_SLOTS) {
        ItemStack oldEquipment = entity.getItemStackFromSlot(slot);

        if (slot != EntityEquipmentSlot.HEAD && rand.nextFloat() > pieceChance)
          break;

        if (StackHelper.isEmpty(oldEquipment)) {
          ItemStack newEquipment = selectEquipmentForSlot(slot, tier);
          if (StackHelper.isValid(newEquipment)) {
            entity.setItemStackToSlot(slot, newEquipment);
          }
        }
      }

      // Hand slots
      pieceChance = ConfigScalingHealth.BLIGHT_EQUIPMENT_HAND_PIECE_CHANCE;
      if (rand.nextFloat() > pieceChance) {
        // Main hand
        ItemStack oldEquipment = entity.getHeldItemMainhand();
        if (StackHelper.isEmpty(oldEquipment)) {
          ItemStack newEquipment = selectEquipmentForSlot(EntityEquipmentSlot.MAINHAND, tier);
          if (StackHelper.isValid(newEquipment)) {
            entity.setItemStackToSlot(EntityEquipmentSlot.MAINHAND, newEquipment);
          }
        }

        // Off hand (only if we tried to do main hand)
        if (rand.nextFloat() > pieceChance) {
          oldEquipment = entity.getHeldItemOffhand();
          if (StackHelper.isEmpty(oldEquipment)) {
            ItemStack newEquipment = selectEquipmentForSlot(EntityEquipmentSlot.OFFHAND, tier);
            if (StackHelper.isValid(newEquipment)) {
              entity.setItemStackToSlot(EntityEquipmentSlot.OFFHAND, newEquipment);
            }
          }
        }
      }
    }

    // Add random enchantments
    for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
      ItemStack stack = entityLiving.getItemStackFromSlot(slot);
      if (StackHelper.isValid(stack))
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

    MinecraftForge.EVENT_BUS.post(new BlightSpawnEvent.Post((EntityLiving) entityLiving, entityLiving.world, (float) entityLiving.posX, (float) entityLiving.posY,
        (float) entityLiving.posZ));

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
    List<Integer> dimBlacklist = ConfigScalingHealth.MOB_HEALTH_DIMENSION_BLACKLIST;

    if (entityId == null || blacklist == null || dimBlacklist == null) return false;

    return blacklist.contains(entityId) || dimBlacklist.contains(entityLiving.dimension);
    //@formatter:on
  }

  private boolean canIncreaseEntityHealth(EntityLivingBase entityLiving) {

    return entityLiving.getAttributeMap() != null && entityLiving.ticksExisted > 1
        && entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifier(ModifierHandler.MODIFIER_ID_HEALTH) == null;
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

  // **************************************************************************
  // Equipment
  // **************************************************************************

  private EntityEquipmentSlot[] ORDERED_SLOTS = { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

  public EquipmentTierMap mapHelmets = new EquipmentTierMap(5, EntityEquipmentSlot.HEAD);
  public EquipmentTierMap mapChestplates = new EquipmentTierMap(5, EntityEquipmentSlot.CHEST);
  public EquipmentTierMap mapLeggings = new EquipmentTierMap(5, EntityEquipmentSlot.LEGS);
  public EquipmentTierMap mapBoots = new EquipmentTierMap(5, EntityEquipmentSlot.FEET);
  public EquipmentTierMap mapMainhands = new EquipmentTierMap(5, EntityEquipmentSlot.MAINHAND);
  public EquipmentTierMap mapOffhands = new EquipmentTierMap(5, EntityEquipmentSlot.OFFHAND);

  public void initDefaultEquipment() {

    mapHelmets.put(new ItemStack(Items.LEATHER_HELMET), 0);
    mapHelmets.put(new ItemStack(Items.GOLDEN_HELMET), 1);
    mapHelmets.put(new ItemStack(Items.CHAINMAIL_HELMET), 2);
    mapHelmets.put(new ItemStack(Items.IRON_HELMET), 3);
    mapHelmets.put(new ItemStack(Items.DIAMOND_HELMET), 4);

    mapChestplates.put(new ItemStack(Items.LEATHER_CHESTPLATE), 0);
    mapChestplates.put(new ItemStack(Items.GOLDEN_CHESTPLATE), 1);
    mapChestplates.put(new ItemStack(Items.CHAINMAIL_CHESTPLATE), 2);
    mapChestplates.put(new ItemStack(Items.IRON_CHESTPLATE), 3);
    mapChestplates.put(new ItemStack(Items.DIAMOND_CHESTPLATE), 4);

    mapLeggings.put(new ItemStack(Items.LEATHER_LEGGINGS), 0);
    mapLeggings.put(new ItemStack(Items.GOLDEN_LEGGINGS), 1);
    mapLeggings.put(new ItemStack(Items.CHAINMAIL_LEGGINGS), 2);
    mapLeggings.put(new ItemStack(Items.IRON_LEGGINGS), 3);
    mapLeggings.put(new ItemStack(Items.DIAMOND_LEGGINGS), 4);

    mapBoots.put(new ItemStack(Items.LEATHER_BOOTS), 0);
    mapBoots.put(new ItemStack(Items.GOLDEN_BOOTS), 1);
    mapBoots.put(new ItemStack(Items.CHAINMAIL_BOOTS), 2);
    mapBoots.put(new ItemStack(Items.IRON_BOOTS), 3);
    mapBoots.put(new ItemStack(Items.DIAMOND_BOOTS), 4);
  }

  private ItemStack selectEquipmentForSlot(EntityEquipmentSlot slot, int tier) {

    tier = MathHelper.clamp(tier, 0, 4);
    switch (slot) {
      case CHEST:
        return mapChestplates.getRandom(tier);
      case FEET:
        return mapBoots.getRandom(tier);
      case HEAD:
        return mapHelmets.getRandom(tier);
      case LEGS:
        return mapLeggings.getRandom(tier);
      case MAINHAND:
        return mapMainhands.getRandom(tier);
      case OFFHAND:
        return mapOffhands.getRandom(tier);
      default:
        return StackHelper.empty();
    }
  }
}
