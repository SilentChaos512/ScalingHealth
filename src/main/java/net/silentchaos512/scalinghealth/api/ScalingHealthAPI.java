/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.api;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.silentchaos512.scalinghealth.config.Config;
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
        return Config.Difficulty.AREA_DIFFICULTY_MODE.getAreaDifficulty(world, pos);
    }

    /**
     * Gets the player difficulty for the given player.
     *
     * @return The player's difficulty, or Double.NaN if the data can't be obtained for some reason.
     */
    public static double getPlayerDifficulty(EntityPlayer player) {
        PlayerData data = SHPlayerDataHandler.get(player);
        if (data == null) {
            return Double.NaN;
        }

        return data.getDifficulty();
    }

    /**
     * Adds difficulty to the player. The player's difficulty will be clamped to valid values.
     */
    public static void addPlayerDifficulty(EntityPlayer player, double amount) {
        PlayerData data = SHPlayerDataHandler.get(player);
        if (data != null) {
            data.incrementDifficulty(amount, false);
        }
    }

    /**
     * For players, gets the player's difficulty. For other entities, it gets the difficulty they
     * spawned with. Non-player difficulty is stored as a short, so it will always be a whole
     * number.
     */
    public static double getEntityDifficulty(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return getPlayerDifficulty((EntityPlayer) entity);

        return entity.getEntityData().getShort(DifficultyHandler.NBT_ENTITY_DIFFICULTY);
    }

    // **************************************************************************
    // Blights
    // **************************************************************************

    /**
     * Adds equipment that blights can randomly spawn with. The tier must be between 0 and 4,
     * inclusive. Default armor items (from tier 0 to 4) are leather, gold, chainmail, iron, and
     * diamond.
     *
     * @param producer The weapon/armor/whatever producer.
     * @param slot     The slot the item belongs in. Currently, no checks are made to ensure the
     *                 slot is correct for the item, but mixing them up may have unintended
     *                 consequences.
     * @param tier     The tier of the item. Higher tiers are less likely to be selected. Tiers 0
     *                 and 1 are very common and equal likely to be chosen. Higher tiers become
     *                 exponentially less common.
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
     * Adds equipment that blights can randomly spawn with. The tier must be between 0 and 4,
     * inclusive. Default armor items (from tier 0 to 4) are leather, gold, chainmail, iron, and
     * diamond.
     *
     * @param stack The weapon/armor/whatever.
     * @param slot  The slot the item belongs in. Currently, no checks are made to ensure the slot
     *              is correct for the item, but mixing them up may have unintended consequences.
     * @param tier  The tier of the item. Higher tiers are less likely to be selected. Tiers 0 and 1
     *              are very common and equal likely to be chosen. Higher tiers become exponentially
     *              less common.
     */
    public static void addBlightEquipment(ItemStack stack, EntityEquipmentSlot slot, int tier) {
        addBlightEquipment(new StackProducer(stack), slot, tier);
    }

    /**
     * Adds a potion effect that any mob can spawn with.
     *
     * @param potion    The potion.
     * @param cost      The amount of "difficulty" the potion requires. A mob must have this much
     *                  difficulty left after health and damage boosts to receive this effect. So
     *                  effects with higher costs are less likely to occur.
     * @param amplifier The amplifier on the potion effect. An amplifier of 0 means level 1.
     */
    public static void addMobSpawnPotion(Potion potion, int cost, int amplifier) {
        DifficultyHandler.INSTANCE.potionMap.put(potion, cost, amplifier);
    }
}
