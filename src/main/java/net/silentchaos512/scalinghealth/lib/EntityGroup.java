package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.SHMobs;

import javax.annotation.Nullable;
import java.util.Optional;

public enum EntityGroup {
    PEACEFUL("peaceful"),
    HOSTILE("hostile"),
    BOSS("boss"),
    BLIGHT(null),
    PLAYER(null);

    @Nullable private final ResourceLocation bonusDropsLootTable;

    EntityGroup(@Nullable String bonusDropsTable) {
        bonusDropsLootTable = bonusDropsTable != null
                ? new ResourceLocation(ScalingHealth.MOD_ID, "bonus_drops/" + bonusDropsTable)
                : null;
    }

    public static EntityGroup from(LivingEntity entity) {
        return from(entity, false);
    }

    public static EntityGroup from(LivingEntity entity, boolean ignoreBlightStatus) {
        if (entity instanceof PlayerEntity)
            return PLAYER;
        if (!entity.isNonBoss())
            return BOSS;
        if (entity instanceof IMob) {
        if (!ignoreBlightStatus && SHMobs.isBlight((MobEntity) entity))
            return BLIGHT;
        return HOSTILE;
    }
        return PEACEFUL;
}

    public double getPotionChance(LivingEntity entity) {
        if (this == PEACEFUL)
            return SHMobs.passivePotionChance(entity.world);
        return SHMobs.hostilePotionChance(entity.world);
    }

    public MobHealthMode getHealthMode(LivingEntity entity) {
        return SHMobs.healthMode(entity.world);
    }

    public Optional<ResourceLocation> getBonusDropsLootTable() {
        return Optional.ofNullable(bonusDropsLootTable);
    }

    public boolean isAffectedByDamageScaling(LivingEntity entity) {
        switch (this) {
            case PLAYER:
                return Config.get(entity).damageScaling.affectPlayers.get();
            case PEACEFUL:
                return Config.get(entity).damageScaling.affectPeacefuls.get();
            default:
                return Config.get(entity).damageScaling.affectHostiles.get();
        }
    }
}
