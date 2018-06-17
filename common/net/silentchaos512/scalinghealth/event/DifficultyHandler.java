package net.silentchaos512.scalinghealth.event;

import java.util.List;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.silentchaos512.lib.util.StackHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.event.BlightSpawnEvent;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.network.NetworkHandler;
import net.silentchaos512.scalinghealth.network.message.MessageMarkBlight;
import net.silentchaos512.scalinghealth.utils.EntityDifficultyChangeList.DifficultyChanges;
import net.silentchaos512.scalinghealth.utils.EntityMatchList;
import net.silentchaos512.scalinghealth.utils.EquipmentTierMap;
import net.silentchaos512.scalinghealth.utils.MobPotionMap;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.scalinghealth.world.ScalingHealthSavedData;

public class DifficultyHandler {

  public static final String NBT_ENTITY_DIFFICULTY = ScalingHealth.RESOURCE_PREFIX + "difficulty";

  public static DifficultyHandler INSTANCE = new DifficultyHandler();

  public static int POTION_APPLY_TIME = 10 * 1200;
  static final String[] POTION_DEFAULTS = { //@formatter:off
      "minecraft:strength,30,1",
      "minecraft:speed,10,1",
      "minecraft:speed,50,2",
      "minecraft:fire_resistance,10,1",
      "minecraft:invisibility,25,1",
      "minecraft:resistance,30,1"
  }; //@formatter:on

  public MobPotionMap potionMap = new MobPotionMap();

  public void initPotionMap() {

    potionMap.clear();

    String[] lines = ConfigScalingHealth.INSTANCE.getConfiguration().getStringList("Mob Potions",
        ConfigScalingHealth.CAT_MOB_POTION, POTION_DEFAULTS,
        "The potion effects that mobs can spawn with. You can add effects from other mods if you"
            + " want to, or remove existing ones. Each line has 3 values separated by commas: the"
            + " potion ID, the minimum difficulty (higher = less common), and the level (1 = level I,"
            + " 2 = level II, etc).");

    for (String line : lines) {
      String[] params = line.split(",");
      if (params.length >= 3) {
        // Ignore extra parameters
        if (params.length > 3) {
          ScalingHealth.logHelper.warning("Mob potion effects: extra parameters in line: " + line
              + ". Ignoring extra parameters and processing the first 3.");
        }

        // Parse parameters.
        int index = -1;
        String id = "null";
        Potion potion = null;
        int minDiff = 0, level = 0;
        try {
          id = params[++index];
          potion = Potion.REGISTRY.getObject(new ResourceLocation(id));
          if (potion == null)
            throw new NullPointerException();
          minDiff = Integer.parseInt(params[++index]);
          level = Integer.parseInt(params[++index]);
        } catch (NumberFormatException ex) {
          ScalingHealth.logHelper.warning("Mob potion effects: could not parse parameter " + index
              + " as integer. Ignoring entire line: " + line);
          continue;
        } catch (NullPointerException ex) {
          ScalingHealth.logHelper
              .warning("Mob potion effects: potion \"" + id + "\" does not exist.");
          continue;
        }

        // Put it in the map if nothing goes wrong!
        potionMap.put(potion, minDiff, level - 1);
      } else {
        ScalingHealth.logHelper
            .warning("Mob potion effects: malformed line (need 3 comma-separated values): " + line
                + "Ignoring entire line.");
      }
    }
  }

  @SubscribeEvent
  public void onMobSpawn(LivingUpdateEvent event) {

    if (ConfigScalingHealth.DIFFICULTY_MAX <= 0)
      return;

    // Increase mob health and make blights?
    if (event.getEntity().world.isRemote || !(event.getEntity() instanceof EntityLiving))
      return;

    EntityLiving entityLiving = (EntityLiving) event.getEntity();

    //@formatter:off
    if (!canIncreaseEntityHealth(entityLiving)
        || entityBlacklistedFromHealthIncrease(entityLiving))
      return;
    //@formatter:on

    boolean makeBlight = increaseEntityHealth(entityLiving);
    if (makeBlight)
      makeEntityBlight(entityLiving, ScalingHealth.random);
  }

  @SubscribeEvent
  public void onMobDeath(LivingDeathEvent event) {

    EntityLivingBase killed = event.getEntityLiving();
    DamageSource source = event.getSource();

    // Killed by player?
    if (source.getTrueSource() instanceof EntityPlayer) {
      DifficultyChanges changes = ConfigScalingHealth.DIFFICULTY_PER_KILL_BY_MOB.get(killed);
      EntityPlayer player = (EntityPlayer) source.getTrueSource();
      PlayerData data = SHPlayerDataHandler.get(player);
      if (data != null) {
        boolean isBlight = BlightHandler.isBlight(killed);
        float amount = isBlight ? changes.onBlightKill : changes.onStandardKill;
        if (ConfigScalingHealth.DEBUG_MODE) {
          ScalingHealth.logHelper.info("Killed " + (isBlight ? "Blight " : "") + killed.getName()
              + ": difficulty " + (amount > 0 ? "+" : "") + amount);
        }
        data.incrementDifficulty(amount, true);
      }
    }
  }

  private boolean increaseEntityHealth(EntityLivingBase entityLiving) {

    if (ConfigScalingHealth.DIFFICULTY_MAX <= 0)
      return false;

    // If true, enables old behavior where health/damage subtract from difficulty as applied.
    // TODO: Should this be a config, or should old behavior just be removed?
    final boolean statsTakeDifficulty = false;

    World world = entityLiving.world;
    float difficulty = (float) ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(world,
        entityLiving.getPosition());
    float originalDifficulty = difficulty;
    Random rand = ScalingHealth.random;
    boolean makeBlight = false;
    boolean isHostile = entityLiving instanceof IMob;

    // Lunar phase multipliers?
    if (ConfigScalingHealth.DIFFICULTY_LUNAR_MULTIPLIERS_ENABLED
        && world.getWorldTime() % 24000 > 12000) {
      int moonPhase = world.provider.getMoonPhase(world.getWorldTime()) % 8;
      float multi = ConfigScalingHealth.DIFFICULTY_LUNAR_MULTIPLIERS[moonPhase];
      difficulty *= multi;
    }

    // Make blight?
    if (!entityBlacklistedFromBecomingBlight(entityLiving)) {
      float chance = (float) (difficulty / ConfigScalingHealth.DIFFICULTY_MAX
          * ConfigScalingHealth.BLIGHT_CHANCE_MULTIPLIER);
      if ((ConfigScalingHealth.BLIGHT_ALWAYS && ConfigScalingHealth.BLIGHT_ALL_MATCH_LIST.matches(entityLiving))
          || rand.nextFloat() < chance) {
        makeBlight = true;
        difficulty *= 3;
      }
    }
    
    entityLiving.getEntityData().setShort(NBT_ENTITY_DIFFICULTY, (short) difficulty);

    float totalDifficulty = difficulty;

    float genAddedHealth = difficulty;
    float genAddedDamage = 0;
    float baseMaxHealth = (float) entityLiving
        .getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
    float healthMultiplier = isHostile ? ConfigScalingHealth.DIFFICULTY_GENERIC_HEALTH_MULTIPLIER
        : ConfigScalingHealth.DIFFICULTY_PEACEFUL_HEALTH_MULTIPLIER;

    genAddedHealth *= healthMultiplier;

    if (statsTakeDifficulty)
      difficulty -= genAddedHealth;

    if (difficulty > 0) {
      float diffIncrease = 2 * healthMultiplier * difficulty * rand.nextFloat();
      if (statsTakeDifficulty)
        difficulty -= diffIncrease;
      genAddedHealth += diffIncrease;
    }

    // Increase attack damage.
    if (difficulty > 0) {
      float diffIncrease = difficulty * rand.nextFloat();
      genAddedDamage = diffIncrease * ConfigScalingHealth.DIFFICULTY_DAMAGE_MULTIPLIER;
      // Clamp the value so it doesn't go over the maximum config.
      if (ConfigScalingHealth.DIFFICULTY_DAMAGE_MAX_BOOST > 0f) {
        genAddedDamage = MathHelper.clamp(genAddedDamage, 0f,
            ConfigScalingHealth.DIFFICULTY_DAMAGE_MAX_BOOST);
      }
      // Decrease difficulty based on the damage actually added, instead of diffIncrease.
      if (statsTakeDifficulty)
        difficulty -= genAddedDamage / ConfigScalingHealth.DIFFICULTY_DAMAGE_MULTIPLIER;
    }

    // Random potion effect
    float potionChance = isHostile ? ConfigScalingHealth.POTION_CHANCE_HOSTILE
        : ConfigScalingHealth.POTION_CHANCE_PASSIVE;
    if (difficulty > 0 && rand.nextFloat() < potionChance) {
      MobPotionMap.PotionEntry pot = potionMap.getRandom(rand, (int) difficulty);
      if (pot != null) {
        if (statsTakeDifficulty)
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
        ScalingHealth.logHelper.severe("Unknown mob health scaling mode: "
            + ConfigScalingHealth.MOB_HEALTH_SCALING_MODE.name());
        break;
    }
    ModifierHandler.addAttackDamage(entityLiving, genAddedDamage, 0);

    // Heal.
    entityLiving.setHealth(entityLiving.getMaxHealth());

    if (ConfigScalingHealth.DEBUG_MODE && ConfigScalingHealth.DEBUG_LOG_SPAWNS && originalDifficulty > 0f) {
      BlockPos pos = entityLiving.getPosition();
      String line = "Spawn debug: %s (%d, %d, %d): Difficulty=%.2f, Health +%.2f, Damage +%.2f";
      line = String.format(line, entityLiving.getName(), pos.getX(), pos.getY(), pos.getZ(),
          totalDifficulty, genAddedHealth, genAddedDamage);
      ScalingHealth.logHelper.info(line);
    }

    return makeBlight;
  }

  private void makeEntityBlight(EntityLiving entityLiving, Random rand) {

    BlightSpawnEvent event = new BlightSpawnEvent.Pre((EntityLiving) entityLiving,
        entityLiving.world, (float) entityLiving.posX, (float) entityLiving.posY,
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
      if (StackHelper.isValid(stack) && !stack.isItemEnchanted())
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

    if (entityLiving == null)
      return true;

    boolean isBoss = !entityLiving.isNonBoss();
    boolean isHostile = entityLiving instanceof IMob;
    boolean isPassive = !isHostile;

    if ((!ConfigScalingHealth.ALLOW_HOSTILE_EXTRA_HEALTH && isHostile)
        || (!ConfigScalingHealth.ALLOW_PEACEFUL_EXTRA_HEALTH && isPassive)
        || (!ConfigScalingHealth.ALLOW_BOSS_EXTRA_HEALTH && isBoss))
      return true;

    EntityMatchList blacklist = ConfigScalingHealth.MOB_HEALTH_BLACKLIST;
    List<Integer> dimBlacklist = ConfigScalingHealth.MOB_HEALTH_DIMENSION_BLACKLIST;

    if (blacklist == null || dimBlacklist == null)
      return false;

    return blacklist.contains(entityLiving) || dimBlacklist.contains(entityLiving.dimension);
  }

  private boolean canIncreaseEntityHealth(EntityLivingBase entityLiving) {

    if (entityLiving == null
        || !entityLiving.world.getGameRules().getBoolean(ScalingHealth.GAME_RULE_DIFFICULTY))
      return false;

    AttributeModifier modifier = entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
        .getModifier(ModifierHandler.MODIFIER_ID_HEALTH);
    // The tickExisted > 1 kinda helps with Lycanites, but checking for a modifier amount of 0 should catch issues with
    // some mobs not receiving health increases.
    // ScalingHealth.logHelper.debug(modifier != null ? modifier.getAmount() : null);
    return entityLiving.ticksExisted > 1
        && (modifier == null || modifier.getAmount() == 0.0 || Double.isNaN(modifier.getAmount()));
  }

  private boolean entityBlacklistedFromBecomingBlight(EntityLivingBase entityLiving) {

    if (entityLiving == null)
      return true;

    EntityMatchList blacklist = ConfigScalingHealth.BLIGHT_BLACKLIST;
    boolean isBoss = !entityLiving.isNonBoss();
    boolean isHostile = entityLiving instanceof IMob;
    boolean isPassive = !isHostile;

    if (blacklist == null)
      return false;

    return blacklist.contains(entityLiving)
        || (isHostile && ConfigScalingHealth.BLIGHT_BLACKLIST_ALL_HOSTILES)
        || (isPassive && ConfigScalingHealth.BLIGHT_BLACKLIST_ALL_PASSIVES)
        || (isBoss && ConfigScalingHealth.BLIGHT_BLACKLIST_ALL_BOSSES);
  }

  @SubscribeEvent
  public void onWorldTick(WorldTickEvent event) {

    if (event.world.getTotalWorldTime() % 20 == 0) {
      ScalingHealthSavedData data = ScalingHealthSavedData.get(event.world);
      data.difficulty += ConfigScalingHealth.DIFFICULTY_PER_SECOND;
      data.markDirty();
    }
  }

  // **************************************************************************
  // Equipment
  // **************************************************************************

  private EntityEquipmentSlot[] ORDERED_SLOTS = { EntityEquipmentSlot.HEAD,
      EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };

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
