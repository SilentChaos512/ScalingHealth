package net.silentchaos512.scalinghealth.event;

import java.util.List;
import java.util.Random;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.init.ModItems;
import net.silentchaos512.scalinghealth.network.DataSyncManager;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.scalinghealth.utils.ScalingHealthSaveStorage;

public class ScalingHealthCommonEvents {

  @SubscribeEvent
  public void onLivingDrops(LivingDropsEvent event) {

    // Handle heart drops.
    EntityLivingBase entityLiving = event.getEntityLiving();
    if (!entityLiving.worldObj.isRemote) {
      Random rand = ScalingHealth.random;
      int stackSize = 0;
      if (event.isRecentlyHit() && rand.nextFloat() <= ConfigScalingHealth.HEART_DROP_CHANCE) {
        stackSize += 1;
      }

      if (!entityLiving.isNonBoss()) {
        int min = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MIN;
        int max = ConfigScalingHealth.HEARTS_DROPPED_BY_BOSS_MAX;
        stackSize += min + rand.nextInt(max - min + 1);
      }

      if (stackSize > 0)
        entityLiving.dropItem(ModItems.heart, stackSize);
    }
  }

  @SubscribeEvent
  public void onLivingSpawnEvent(EntityJoinWorldEvent event) {

    // Increase mob health and make blights?
    if (!(event.getEntity() instanceof EntityLivingBase))
      return;

    EntityLivingBase entityLiving = (EntityLivingBase) event.getEntity();
    if (!entityLiving.worldObj.isRemote && !(entityLiving instanceof EntityPlayer)) {
      if (!entityBlacklistedFromHealthIncrease(entityLiving)) {
        if (canIncreaseEntityHealth(entityLiving)) {
          increaseEntityHealth(entityLiving);
        }
      }
    }
  }

  @SubscribeEvent
  public void onLivingUpdateEvent(LivingUpdateEvent event) {

    EntityLivingBase entityLiving = event.getEntityLiving();

    // See PlayerBonusRegenHandler for new player health regen.

    // Seems to be used to send update packets?
    if (!entityLiving.worldObj.isRemote && entityLiving instanceof EntityPlayer
        && entityLiving.ticksExisted % 20 == 0) {
      EntityPlayer p = (EntityPlayer) entityLiving;
      // Gets all players in a one chunk radius?
      List<EntityPlayer> players = p.worldObj.getEntitiesWithinAABB(EntityPlayer.class,
          new AxisAlignedBB(p.posX - 0.5D, p.posY - 0.5D, p.posZ - 0.5D, p.posX + 0.5D,
              p.posY + 0.5D, p.posZ + 0.5D).expand(16, 8, 16));
      for (EntityPlayer pl : players) {
        NBTTagCompound tag = ScalingHealthSaveStorage.playerData.get(p.getName());
        // What is this? Why is it here?
        // if (tag.hasKey("username")) {
        // // ...
        // } else {
        // tag.setString("username", p.getName());
        // }
        DataSyncManager.requestServerToClientMessage("playerData", (EntityPlayerMP) pl, tag, true);
      }
    }
  }

  @SubscribeEvent
  public void onWorldTick(TickEvent.WorldTickEvent event) {

    // Handle difficulty ticks. Unlike Difficult Life, this is actually done each tick.
    World world = event.world;
    if (event.side == Side.SERVER && world != null && world.provider != null
        && world.provider.getDimension() == 0 && event.phase == Phase.START) {
      ScalingHealthSaveStorage.incrementDifficulty(world, ConfigScalingHealth.DIFFICULTY_PER_TICK);
    }
  }

  @SubscribeEvent
  public void onPlayerRespawn(
      net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerRespawnEvent event) {

    // Set player health correctly after respawn.
    if (event.player instanceof EntityPlayerMP) {
      EntityPlayerMP player = (EntityPlayerMP) event.player;

      // Lose health on death?
      if (ConfigScalingHealth.LOSE_HEALTH_ON_DEATH) {
        ScalingHealthSaveStorage.resetPlayerHealth(player);
      }

      // Apply health modifier
      int maxHealth = ScalingHealthSaveStorage.getPlayerHealth(player);
      ModifierHandler.setMaxHealth(player, maxHealth);
      if (player.getHealth() != maxHealth)
        player.setHealth(maxHealth);
    }
  }

  @SubscribeEvent
  public void onPlayerJoinedServerEvent(PlayerLoggedInEvent event) {

    // Sync player data and set health.
    if (event.player instanceof EntityPlayerMP) {
      DataSyncManager.requestServerToClientMessage("worldData", (EntityPlayerMP) event.player,
          ScalingHealthSaveStorage.commonTag, true);
      DataSyncManager.requestServerToClientMessage("playerData", (EntityPlayerMP) event.player,
          ScalingHealthSaveStorage.playerData.get(event.player.getName()), true);

      EntityPlayerMP player = (EntityPlayerMP) event.player;

      // Apply health modifier
      float maxHealth = ScalingHealthSaveStorage.getPlayerHealth(player);
      ModifierHandler.setMaxHealth(player, maxHealth);
      if (player.getHealth() > maxHealth)
        player.setHealth(maxHealth);
    }
  }

  private void increaseEntityHealth(EntityLivingBase entityLiving) {

    float difficulty = (float) ScalingHealthSaveStorage.getDifficulty(entityLiving.worldObj);
    Random rand = ScalingHealth.random;

    // Make blight?
    if (!entityBlacklistedFromBecomingBlight(entityLiving)) {
      float chance = (float) (difficulty / ConfigScalingHealth.DIFFICULTY_MAX
          * ConfigScalingHealth.BLIGHT_CHANCE_MULTIPLIER);
      if (rand.nextFloat() < chance) {
        makeEntityBlight(entityLiving, rand);
        difficulty *= 3;
      }
    }

    float genAddedHealth = difficulty;
    float genAddedDamage = 0;

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
      genAddedDamage = diffIncrease / 10f;
      ModifierHandler.setAttackDamage(entityLiving, genAddedDamage);
    }

    if (difficulty > 0) {
      // TODO: Random potion effects? (DLEventHandler:217)
    }

    // Apply extra health.
    ModifierHandler.setMaxHealth(entityLiving, genAddedHealth + entityLiving.getMaxHealth());
    entityLiving.setHealth(entityLiving.getMaxHealth());

    // TODO: Config!
//    LogHelper log = ScalingHealth.logHelper;
//    log.info(
//        entityLiving.getName() + ": Health +" + genAddedHealth + ", Damage +" + genAddedDamage);
  }

  private void makeEntityBlight(EntityLivingBase entityLiving, Random rand) {

    //@formatter:off
    entityLiving.addPotionEffect(new PotionEffect(
        MobEffects.INVISIBILITY, Integer.MAX_VALUE, 0, true, false));
    entityLiving.addPotionEffect(new PotionEffect(
        MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, true, false));
    entityLiving.addPotionEffect(new PotionEffect(
        MobEffects.SPEED, Integer.MAX_VALUE, ConfigScalingHealth.BLIGHT_AMP_SPEED, true, false));
    entityLiving.addPotionEffect(new PotionEffect(
        MobEffects.STRENGTH, Integer.MAX_VALUE, ConfigScalingHealth.BLIGHT_AMP_STRENGTH, true, false));

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

    entityLiving.setFire(Integer.MAX_VALUE / 20);
    if (entityLiving instanceof EntityCreeper) {
      ((EntityCreeper) entityLiving)
          .onStruckByLightning(new EntityLightningBolt(entityLiving.worldObj,
              entityLiving.posX, entityLiving.posY, entityLiving.posZ, true));
    }
    // @formatter:on
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

    return item == null ? null : new ItemStack(item);
  }

  private boolean entityBlacklistedFromHealthIncrease(EntityLivingBase entityLiving) {

    if (!ConfigScalingHealth.ALLOW_HOSTILE_EXTRA_HEALTH && entityLiving instanceof EntityMob)
      return false;
    if (!ConfigScalingHealth.ALLOW_PEACEFUL_EXTRA_HEALTH && entityLiving instanceof EntityAnimal)
      return false;

    // TODO
    return false;
  }

  private boolean canIncreaseEntityHealth(EntityLivingBase entityLiving) {

    return entityLiving.getAttributeMap() != null
        && entityLiving.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
            .getModifier(ModifierHandler.MODIFIER_ID_HEALTH) == null;
  }

  private boolean entityBlacklistedFromBecomingBlight(EntityLivingBase entityLiving) {

    // TODO
    return false;
  }

  @SubscribeEvent
  public void onPlayerLoadFromFile(PlayerEvent.LoadFromFile event) {

    ScalingHealthSaveStorage.loadPlayerFile(event);
  }

  @SubscribeEvent
  public void onPlayerSaveToFile(PlayerEvent.SaveToFile event) {

    ScalingHealthSaveStorage.savePlayerFile(event);
  }

  @SubscribeEvent
  public void onWorldLoadEvent(WorldEvent.Load event) {

    ScalingHealthSaveStorage.loadServerWorldFile(event);
  }

  @SubscribeEvent
  public void onWorldSaveEvent(WorldEvent.Save event) {

    ScalingHealthSaveStorage.saveServerWorldFile(event);
  }

  @SubscribeEvent
  public void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {

    if (event.getModID().equals(ScalingHealth.MOD_ID)) {
      ConfigScalingHealth.load();
      ConfigScalingHealth.save();
    }
  }
}
