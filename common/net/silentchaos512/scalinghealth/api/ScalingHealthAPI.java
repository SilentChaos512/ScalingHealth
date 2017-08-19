package net.silentchaos512.scalinghealth.api;

import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.event.DifficultyHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.scalinghealth.utils.StackProducer;

public class ScalingHealthAPI {

  // **************************************************************************
  // Difficulty
  // **************************************************************************

  /**
   * Gets the area difficulty for the given position.
   * 
   * @return The area difficulty.
   */
  public static double getAreaDifficulty(World world, BlockPos pos) {

    return ConfigScalingHealth.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, pos);
  }

  /**
   * Gets the player difficulty for the given player.
   * 
   * @return The player's difficulty, or Double.NaN if the data can't be obtained for some reason.
   */
  public static double getPlayerDifficulty(@Nonnull EntityPlayer player) {

    if (player == null) {
      return Double.NaN;
    }

    PlayerData data = SHPlayerDataHandler.get(player);
    if (data == null) {
      return Double.NaN;
    }

    return data.getDifficulty();
  }

  /**
   * Adds difficulty to the player. The player's difficulty will be clamped to valid values.
   */
  public static void addPlayerDifficulty(@Nonnull EntityPlayer player, double amount) {

    if (player != null) {
      PlayerData data = SHPlayerDataHandler.get(player);
      if (data != null) {
        data.incrementDifficulty(amount);
      }
    }
  }

  // **************************************************************************
  // Blights
  // **************************************************************************

  /**
   * Adds equipment that blights can randomly spawn with. The tier must be between 0 and 4, inclusive. Default armor
   * items (from tier 0 to 4) are leather, gold, chainmail, iron, and diamond.
   * 
   * @param producer
   *          The weapon/armor/whatever producer.
   * @param slot
   *          The slot the item belongs in. Currently, no checks are made to ensure the slot is correct for the item,
   *          but mixing them up may have unintended consequences.
   * @param tier
   *          The tier of the item. Higher tiers are less likely to be selected. Tiers 0 and 1 are very common and equal
   *          likely to be chosen. Higher tiers become exponentially less common.
   */
  public static void addBlightEquipment(StackProducer producer, EntityEquipmentSlot slot, int tier) {

    switch (slot) {
      case CHEST:
        DifficultyHandler.INSTANCE.mapChestplates.put(producer, tier);
        break;
      case FEET:
        DifficultyHandler.INSTANCE.mapBoots.put(producer, tier);
        break;
      case HEAD:
        DifficultyHandler.INSTANCE.mapHelmets.put(producer, tier);
        break;
      case LEGS:
        DifficultyHandler.INSTANCE.mapLeggings.put(producer, tier);
        break;
      case MAINHAND:
        DifficultyHandler.INSTANCE.mapMainhands.put(producer, tier);
        break;
      case OFFHAND:
        DifficultyHandler.INSTANCE.mapOffhands.put(producer, tier);
        break;
      default:
        break;
    }
  }

  /**
   * Adds equipment that blights can randomly spawn with. The tier must be between 0 and 4, inclusive. Default armor
   * items (from tier 0 to 4) are leather, gold, chainmail, iron, and diamond.
   * 
   * @param stack
   *          The weapon/armor/whatever.
   * @param slot
   *          The slot the item belongs in. Currently, no checks are made to ensure the slot is correct for the item,
   *          but mixing them up may have unintended consequences.
   * @param tier
   *          The tier of the item. Higher tiers are less likely to be selected. Tiers 0 and 1 are very common and equal
   *          likely to be chosen. Higher tiers become exponentially less common.
   */
  public static void addBlightEquipment(ItemStack stack, EntityEquipmentSlot slot, int tier) {

    addBlightEquipment(new StackProducer(stack), slot, tier);
  }
}
