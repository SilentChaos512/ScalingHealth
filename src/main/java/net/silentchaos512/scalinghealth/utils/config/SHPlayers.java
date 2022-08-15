package net.silentchaos512.scalinghealth.utils.config;

import net.minecraft.world.entity.player.Player;
import net.silentchaos512.scalinghealth.capability.IPlayerData;
import net.silentchaos512.scalinghealth.capability.PlayerDataCapability;
import net.silentchaos512.scalinghealth.config.EvalVars;
import net.silentchaos512.scalinghealth.resources.mechanics.PlayerMechanics;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanicListener;
import net.silentchaos512.scalinghealth.resources.mechanics.SHMechanics;
import net.silentchaos512.utils.MathUtils;

/**
 * Utility class for player-related settings. Same as with {@link SHDifficulty}, this should put a
 * stop to the tangled mess of config references.
 */
public final class SHPlayers {
   private SHPlayers() {throw new IllegalAccessError("Utility class");}

   private static PlayerMechanics getMechanics() {
      return SHMechanics.getMechanics().playerMechanics();
   }

   public static IPlayerData getPlayerData(Player entity) {
      return entity.getCapability(PlayerDataCapability.INSTANCE).orElseThrow(() -> new IllegalStateException("Could not access capability"));
   }

   public static int startingHealth() {
      return getMechanics().startingHp;
   }

   public static int minHealth() {
      return getMechanics().minHealth;
   }

   public static int maxHealth() {
      return getMechanics().maxHealth;
   }

   public static int minHeartCrystals() {
      return (minHealth() - startingHealth()) / (2 * SHItems.heartCrystalIncreaseAmount());
   }

   public static int maxHeartCrystals() {
      if (maxHealth() == Integer.MAX_VALUE)
         return Integer.MAX_VALUE;
      return (maxHealth() - startingHealth()) / (2 * SHItems.heartCrystalIncreaseAmount());
   }

   public static double maxAttackDamage() {
      return getMechanics().maxAttackDamage;
   }

   public static int maxPowerCrystals() {
      return (int) ((maxAttackDamage() - 1) / SHItems.powerCrystalIncreaseAmount());
   }

   public static int clampExtraHearts(int value) {
      return MathUtils.clamp(value,
              (minHealth() - startingHealth()) / 2,
              (maxHealth() - startingHealth()) / 2
      );
   }

   public static int clampedHpFromHeartCrystals(int crystals) { //clamping to not decrease hp to a non-integer multiple of heart crystals.
      int clampedCrystals = MathUtils.clamp(crystals, minHeartCrystals(), maxHeartCrystals());
      return clampedCrystals * SHItems.heartCrystalIncreaseAmount();
   }

   public static int clampPowerCrystals(int value) {
      return MathUtils.clamp(value,
              0,
              maxPowerCrystals()
      );
   }

   public static int getCrystalsAfterDeath(Player player) {
      float healthDifference =  player.getMaxHealth() - MathUtils.clamp((int) EvalVars.apply(player, getMechanics().healthOnDeath.get()), minHealth(), maxHealth());
      int crystalDifference = (int) healthDifference / (2 * SHItems.heartCrystalIncreaseAmount());
      return getPlayerData(player).getHeartCrystals() - crystalDifference;
   }

   //Amount of levels needed for an hp increase.
   public static int levelsPerHp() {
      return getMechanics().levelsPerHp;
   }

   //amount of hp obtained for an hp increase from levels.
   public static int hpPerLevel() {
      return getMechanics().hpPerLevel;
   }

   public static int fullHeartsFromXp(int levels) {
      if (hpPerLevel() == 0 || levelsPerHp() == 0)
         return 0;
      return EnabledFeatures.healthXpEnabled() ? levels / levelsPerHp() * hpPerLevel() : 0;
   }
}
