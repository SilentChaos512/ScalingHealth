package net.silentchaos512.scalinghealth.utils;

import net.minecraft.entity.MobEntity;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.config.equipment.*;

public final class SHMobEquipment {
    private SHMobEquipment() {throw new IllegalAccessError("Utility class");}

    public static void equipAll(MobEntity entity){
        equipHelmet(entity);
        equipChestplate(entity);
        equipLeggings(entity);
        equipBoots(entity);
        //equipWeapon(entity);
    }

    public static void equipHelmet(MobEntity entity) {
        HelmetConfig config = Config.get(entity).equipment.equipmentHelmet;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) ((config.getMaxTier()+ 1) * SHDifficulty.affected(entity).affectiveDifficulty(entity.world) / SHDifficulty.maxValue(entity.world));
        ScalingHealth.LOGGER.debug("HELMET: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), SHDifficulty.affected(entity).affectiveDifficulty(entity.world), SHDifficulty.maxValue(entity.world), tier);

        if(tier > config.getMaxTier())
            tier = config.getMaxTier();
        //if we are at at least 2% of max difficulty, set tier to 1
        if(tier == 0) {
            if(SHDifficulty.areaDifficulty(entity.world, entity.getPosition()) > SHDifficulty.maxValue(entity.world) * 0.02)
                tier = 1;
            else
                return;
        }
        config.processMob(entity, tier);
    }

    public static void equipChestplate(MobEntity entity){
        ChestConfig config = Config.get(entity).equipment.equipmentChest;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) ((config.getMaxTier()+ 1) * SHDifficulty.affected(entity).affectiveDifficulty(entity.world) / SHDifficulty.maxValue(entity.world));
        ScalingHealth.LOGGER.debug("Chestplate: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), SHDifficulty.affected(entity).affectiveDifficulty(entity.world), SHDifficulty.maxValue(entity.world), tier);

        if(tier > config.getMaxTier())
            tier = config.getMaxTier();
        //if we are at at least 2% of max difficulty, set tier to 1
        if(tier == 0) {
            if(SHDifficulty.areaDifficulty(entity.world, entity.getPosition()) > SHDifficulty.maxValue(entity.world) * 0.02)
                tier = 1;
            else
                return;
        }

        config.processMob(entity, tier);
    }

    public static void equipLeggings(MobEntity entity){
        LeggingsConfig config = Config.get(entity).equipment.equipmentLegging;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) ((config.getMaxTier()+ 1) * SHDifficulty.affected(entity).affectiveDifficulty(entity.world) / SHDifficulty.maxValue(entity.world));
        ScalingHealth.LOGGER.debug("Leggings: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), SHDifficulty.affected(entity).affectiveDifficulty(entity.world), SHDifficulty.maxValue(entity.world), tier);

        if(tier > config.getMaxTier())
            tier = config.getMaxTier();
        //if we are at at least 2% of max difficulty, set tier to 1
        if(tier == 0) {
            if(SHDifficulty.areaDifficulty(entity.world, entity.getPosition()) > SHDifficulty.maxValue(entity.world) * 0.02)
                tier = 1;
            else
                return;
        }

        config.processMob(entity, tier);
    }

    public static void equipBoots(MobEntity entity){
        BootsConfig config =  Config.get(entity).equipment.equipmentBoots;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) ((config.getMaxTier()+ 1) * SHDifficulty.affected(entity).affectiveDifficulty(entity.world) / SHDifficulty.maxValue(entity.world));
        ScalingHealth.LOGGER.debug("Boots: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), SHDifficulty.affected(entity).affectiveDifficulty(entity.world), SHDifficulty.maxValue(entity.world), tier);

        if(tier > config.getMaxTier())
            tier = config.getMaxTier();
        //if we are at at least 2% of max difficulty, set tier to 1
        if(tier == 0) {
            if(SHDifficulty.areaDifficulty(entity.world, entity.getPosition()) > SHDifficulty.maxValue(entity.world) * 0.02)
                tier = 1;
            else
                return;
        }

        config.processMob(entity, tier);
    }

    public static void equipWeapon(MobEntity entity) {/*
        WeaponConfig config = Config.get(entity).equipment.equipmentWeapon;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) ((config.getMaxTier()+ 1) * SHDifficulty.affected(entity).affectiveDifficulty(entity.world) / SHDifficulty.maxValue(entity.world));
        ScalingHealth.LOGGER.debug("WEAPON: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), SHDifficulty.affected(entity).affectiveDifficulty(entity.world), SHDifficulty.maxValue(entity.world), tier);

        if(tier > config.getMaxTier())
            tier = config.getMaxTier();
        //if we are at at least 2% of max difficulty, set tier to 1
        if(tier == 0) {
            if(SHDifficulty.areaDifficulty(entity.world, entity.getPosition()) > SHDifficulty.maxValue(entity.world) * 0.02)
                tier = 1;
            else
                return;
        }
        config.processMob(entity, tier);*/
    }
}
