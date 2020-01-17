package net.silentchaos512.scalinghealth.utils;

public final class SHMobEquipment {
    private SHMobEquipment() {throw new IllegalAccessError("Utility class");}

    /*public static void equipAll(MobEntity entity){
        equipHelmet(entity);
        equipChestplate(entity);
        equipLeggings(entity);
        equipBoots(entity);
        equipWeapon(entity);
    }

    public static void equipHelmet(MobEntity entity){
        EquipmentConfig config = Config.get(entity).mobs.equipmentHelmet;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) (config.getMaxTier() * Difficulty.affected(entity).affectiveDifficulty(entity.world) / maxValue(entity.world));
        ScalingHealth.LOGGER.debug("HELMET: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), Difficulty.affected(entity).affectiveDifficulty(entity.world), maxValue(entity.world), tier);

        if(tier > config.getMaxTier()) tier = config.getMaxTier();
        //if were at at least 2% of max difficulty, set tier to 1
        if(tier == 0 && Difficulty.areaDifficulty(entity.world, entity.getPosition()) > maxValue(entity.world) * 0.02) tier = 1;

        config.processMob(entity, tier);
    }

    public static void equipChestplate(MobEntity entity){
        EquipmentConfig config = Config.get(entity).mobs.equipmentChest;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) (config.getMaxTier() * Difficulty.affected(entity).affectiveDifficulty(entity.world) / maxValue(entity.world));
        ScalingHealth.LOGGER.debug("CHESTPLATE: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), Difficulty.affected(entity).affectiveDifficulty(entity.world), maxValue(entity.world), tier);
        if(tier > config.getMaxTier()) tier = config.getMaxTier();
        //if were at at least 2% of max difficulty, set tier to 1
        if(tier == 0 && Difficulty.areaDifficulty(entity.world, entity.getPosition()) > maxValue(entity.world) * 0.02) tier = 1;

        config.processMob(entity, tier);
    }

    public static void equipLeggings(MobEntity entity){
        EquipmentConfig config = Config.get(entity).mobs.equipmentLegging;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) (config.getMaxTier() * Difficulty.affected(entity).affectiveDifficulty(entity.world) / maxValue(entity.world));
        ScalingHealth.LOGGER.debug("LEGGINGS: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), Difficulty.affected(entity).affectiveDifficulty(entity.world), maxValue(entity.world), tier);
        if(tier > config.getMaxTier()) tier = config.getMaxTier();
        //if were at at least 2% of max difficulty, set tier to 1
        if(tier == 0 && Difficulty.areaDifficulty(entity.world, entity.getPosition()) > maxValue(entity.world) * 0.02) tier = 1;

        config.processMob(entity, tier);
    }

    public static void equipBoots(MobEntity entity){
        EquipmentConfig config =  Config.get(entity).mobs.equipmentBoots;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) (config.getMaxTier() * Difficulty.affected(entity).affectiveDifficulty(entity.world) / maxValue(entity.world));
        ScalingHealth.LOGGER.debug("BOOTS: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), Difficulty.affected(entity).affectiveDifficulty(entity.world), maxValue(entity.world), tier);
        if(tier > config.getMaxTier()) tier = config.getMaxTier();
        //if were at at least 2% of max difficulty, set tier to 1
        if(tier == 0 && Difficulty.areaDifficulty(entity.world, entity.getPosition()) > maxValue(entity.world) * 0.02) tier = 1;

        config.processMob(entity, tier);
    }

    public static void equipWeapon(MobEntity entity){
        EquipmentConfig config = Config.get(entity).mobs.equipmentWeapon;
        //adding 1 to max tier or else the max tier will only happen on max diff
        int tier = (int) (config.getMaxTier() * Difficulty.affected(entity).affectiveDifficulty(entity.world) / maxValue(entity.world));
        ScalingHealth.LOGGER.debug("WEAPON: Maxtier = {}, mob diff = {}, max = {}, tier = {}", config.getMaxTier(), Difficulty.affected(entity).affectiveDifficulty(entity.world), maxValue(entity.world), tier);
        if(tier > config.getMaxTier()) tier = config.getMaxTier();
        //if were at at least 2% of max difficulty, set tier to 1
        if(tier == 0 && Difficulty.areaDifficulty(entity.world, entity.getPosition()) > maxValue(entity.world) * 0.02) tier = 1;

        config.processMob(entity, tier);
    }*/
}
