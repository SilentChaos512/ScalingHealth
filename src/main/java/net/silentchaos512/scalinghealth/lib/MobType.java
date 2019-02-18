package net.silentchaos512.scalinghealth.lib;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.utils.Difficulty;

import javax.annotation.Nullable;
import java.util.Optional;

public enum MobType {
    PEACEFUL("peaceful"),
    HOSTILE("hostile"),
    BOSS("boss"),
    BLIGHT(null),
    PLAYER(null);

    @Nullable private final ResourceLocation bonusDropsLootTable;

    MobType(@Nullable String bonusDropsTable) {
        bonusDropsLootTable = bonusDropsTable != null
                ? new ResourceLocation(ScalingHealth.MOD_ID, "bonus_drops/" + bonusDropsTable)
                : null;
    }

    public static MobType from(EntityLivingBase entity) {
        return from(entity, false);
    }

    public static MobType from(EntityLivingBase entity, boolean ignoreBlightStatus) {
        if (entity instanceof EntityPlayer)
            return PLAYER;
        if (!entity.isNonBoss())
            return BOSS;
        if (!ignoreBlightStatus && Difficulty.isBlight(entity))
            return BLIGHT;
        if (entity instanceof IMob)
            return HOSTILE;
        return PEACEFUL;
    }

    public double getPotionChance(EntityLivingBase entity) {
        if (this == PEACEFUL)
            return Config.get(entity).mobs.peacefulPotionChance.get();
        return Config.get(entity).mobs.hostilePotionChance.get();
    }

    public MobHealthMode getHealthMode(EntityLivingBase entity) {
        return Config.get(entity).mobs.healthMode.get();
    }

    public Optional<ResourceLocation> getBonusDropsLootTable() {
        return Optional.ofNullable(bonusDropsLootTable);
    }
}
