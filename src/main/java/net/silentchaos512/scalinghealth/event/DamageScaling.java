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

package net.silentchaos512.scalinghealth.event;

import gnu.trove.map.hash.THashMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.silentchaos512.lib.config.ConfigMultiValueLineParser;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.api.ScalingHealthAPI;
import net.silentchaos512.scalinghealth.config.Config;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DamageScaling {
    private static final Marker MARKER = MarkerManager.getMarker("DamageScaling");
    private static final String[] SOURCES_DEFAULT = {"inFire", "lightningBolt", "onFire", "lava", "hotFloor", "inWall", "cramming", "drown", "starve", "cactus", "fall", "flyIntoWall", "outOfWorld", "generic",
            "magic", "wither", "anvil", "fallingBlock", "dragonBreath", "fireworks"};
    private static final String SOURCES_COMMENT = "Set damage scaling by damage source. All vanilla sources should be included, but set to no scaling. Mod sources can be added too, you'll just need the damage"
            + " type string. The number represents how steeply the damage scales. 0 means no scaling (vanilla), 1 means it will be proportional to difficulty/max health (whichever you set). The scaling"
            + " number can be anything, although I recommend a non-negative number.";

    public static final DamageScaling INSTANCE = new DamageScaling();

    private float genericScale;
    private float difficultyWeight;
    private boolean affectHostileMobs;
    private boolean affectPassiveMobs;
    private Mode scaleMode;
    private final Map<String, Float> scalingMap = new THashMap<>();
    private final Set<UUID> entityAttackedThisTick = new HashSet<>();

    private DamageScaling() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerHurt(LivingAttackEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity.world.isRemote) return;
        // Entity invulnerable?
        if (entity.isEntityInvulnerable(event.getSource()) || entity.hurtResistantTime > entity.maxHurtResistantTime / 2)
            return;

        // Check entity has already been processed from original event, or is not allowed to be affected
        if (entityAttackedThisTick.contains(entity.getPersistentID())
                || (entity instanceof IMob && !affectHostileMobs)
                || (!(entity instanceof EntityPlayer) && !affectPassiveMobs))
            return;

        DamageSource source = event.getSource();
        if (source == null) return;

        // Get scaling factor from map, if it exists. Otherwise, use the generic scale.
        float scale = scalingMap.getOrDefault(source.getDamageType(), genericScale);

        // Get the amount of the damage to affect. Can be many times the base value.
        final float affectedAmount = getEffectScale(entity);

        // Calculate damage to add to the original.
        final float original = event.getAmount();
        final float change = scale * affectedAmount * original;
        if (change != 0) {
            final float newAmount = makeSane(event.getAmount() + change);

            event.setCanceled(true);
            entityAttackedThisTick.add(entity.getPersistentID());
            entity.attackEntityFrom(event.getSource(), newAmount);

            if (Config.Debug.debugMode && Config.Debug.logPlayerDamage) {
                ScalingHealth.LOGGER.info(MARKER, "{} on {}: {} -> {} (scale={}, affected={}, change={})",
                        source.damageType, entity.getName(), original, newAmount, scale, affectedAmount, change);
            }
        }
    }

    private float getEffectScale(EntityLivingBase entity) {
        switch (scaleMode) {
            case AREA_DIFFICULTY:
                return (float) ScalingHealthAPI.getAreaDifficulty(entity.world, entity.getPosition()) * difficultyWeight;
            case MAX_HEALTH:
                float baseHealth = entity instanceof EntityPlayer
                        ? Config.Player.Health.startingHealth
                        : (float) entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue();
                return (entity.getMaxHealth() - baseHealth) / baseHealth;
            case PLAYER_DIFFICULTY:
                return (float) ScalingHealthAPI.getEntityDifficulty(entity) * difficultyWeight;
            default:
                throw new IllegalStateException("Unknown damage scaling mode: " + scaleMode);
        }
    }

    private static float makeSane(float scaledAmount) {
        // Clamp scaled damage to sane values (non-negative and finite)
        if (scaledAmount < 0)
            return 0;
        if (!Float.isFinite(scaledAmount))
            return Float.MAX_VALUE;
        return scaledAmount;
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        entityAttackedThisTick.clear();
    }

    public void loadConfig(Configuration config) {
        final String category = Config.CAT_PLAYER_DAMAGE;

        genericScale = config.getFloat("Generic Scale", category, 0f, -Float.MAX_VALUE, Float.MAX_VALUE,
                "If the damage source is not in the \"Scale By Source\" list, this value is used instead.");
        difficultyWeight = config.getFloat("Difficulty Weight", category, 0.04f, 0f, 1000f,
                "How much each point of difficulty affects damage scaling. With the default value of 0.04 (1/25th) and max difficulty of 250, that's up to a 10x multiplier on added damage. So player's would"
                        + " take 11x damage at max difficulty, if the source scale is set to 1.0.");
        scaleMode = Config.INSTANCE.loadEnum("Scaling Mode", Config.CAT_PLAYER_DAMAGE, Mode.class, Mode.MAX_HEALTH, "Set what value we scale against. MAX_HEALTH scales to player's max health MINUS starting health. Defaults to MAX_HEALTH if an invalid string is entered.");

        affectHostileMobs = config.getBoolean("Affect Hostile Mobs", category, false, "Also apply damage scaling to hostile mobs when they take damage.");
        affectPassiveMobs = config.getBoolean("Affect Passive Mobs", category, false, "Also apply damage scaling to passive mobs when they take damage.");

        // The parser is used to extract multiple values of different types from a single string. Parsing returns an Object
        // array if successful, or null if anything goes wrong. The parser also handles error logging.
        ConfigMultiValueLineParser parser = new ConfigMultiValueLineParser("Scale By Source", ScalingHealth.logHelper, "\\s", String.class, Float.class);
        scalingMap.clear();

        // Construct a default values array. Just SOURCES_DEFAULT with 0.0 appended to each element.
        String[] defaults = new String[SOURCES_DEFAULT.length];
        for (int i = 0; i < defaults.length; ++i) {
            defaults[i] = SOURCES_DEFAULT[i] + " 0.0";
        }

        for (String str : config.getStringList("Scale By Source", category, defaults, SOURCES_COMMENT)) {
            Object[] values = parser.parse(str);
            // If not null, the values are guaranteed to be the correct types.
            if (values != null)
                scalingMap.put((String) values[0], (Float) values[1]);
        }
    }

    public enum Mode {
        MAX_HEALTH, PLAYER_DIFFICULTY, AREA_DIFFICULTY;
    }
}
