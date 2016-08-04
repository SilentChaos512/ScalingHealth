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

    if (event.getModID().equals(ScalingHealth.MOD_ID_LOWER)) {
      ConfigScalingHealth.load();
      ConfigScalingHealth.save();
    }
  }
}
