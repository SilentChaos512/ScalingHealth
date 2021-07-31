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

package net.silentchaos512.scalinghealth.objects.item;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.silentchaos512.lib.util.EntityHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.EnabledFeatures;
import net.silentchaos512.scalinghealth.utils.config.SHItems;
import net.silentchaos512.scalinghealth.utils.config.SHPlayers;
import net.silentchaos512.utils.MathUtils;

public class HeartCrystal extends StatBoosterItem {
    public HeartCrystal(Properties properties) {
        super(properties);
    }

    @Override
    protected int getLevelCost(Player player) {
        return SHItems.levelCostToUseHeartCrystal(player);
    }

    @Override
    protected boolean isStatIncreaseAllowed(Player player) {
        return EnabledFeatures.healthCrystalEnabled() &&
                SHPlayers.getPlayerData(player).getBonusHearts(player) < SHPlayers.maxHealth();
    }

    @Override
    protected boolean shouldConsume(Player player) {
        return EnabledFeatures.healthCrystalRegenEnabled() &&
                player.getHealth() < player.getMaxHealth();
    }

    @Override
    protected void extraConsumeEffect(Player player) {
        int current = (int) player.getHealth();
        double healAmount = SHItems.heartCrystalHpBonusRegen();
        EntityHelper.heal(player, (float) healAmount, true);
        int newHealth = (int) player.getHealth();
        if (!MathUtils.doublesEqual(current + healAmount, newHealth)) {
            ScalingHealth.LOGGER.warn("Another mod seems to have canceled healing from a heart container (player {})", player.getName());
        }
    }

    @Override
    protected void increaseStat(Player player) {
        SHPlayers.getPlayerData(player).addHeartCrystals(player, SHItems.heartCrystalIncreaseAmount());
    }

    @Override
    protected ParticleOptions getParticleType() {
        return Registration.HEART_CRYSTAL_PARTICLE.get();
    }

    @Override
    protected SoundEvent getSoundEffect() {
        return Registration.HEART_CRYSTAL_USE.get();
    }
}
