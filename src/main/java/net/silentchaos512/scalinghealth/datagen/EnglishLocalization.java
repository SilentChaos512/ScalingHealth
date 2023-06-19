package net.silentchaos512.scalinghealth.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.objects.Registration;

public class EnglishLocalization extends LanguageProvider {
    public EnglishLocalization(DataGenerator gen) {
        super(gen.getPackOutput(), ScalingHealth.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        noPost("blight", "Blight %s");
        add("blight", "killedByPlayer", "Blight %s was killed by %s!");

        add(Registration.HEART_CRYSTAL_ORE.get(), "Heart Crystal Ore");
        add(Registration.POWER_CRYSTAL_ORE.get(), "Power Crystal Ore");
        add(Registration.DEEPLSATE_HEART_CRYSTAL_ORE.get(), "Deepslate Heart Crystal Ore");
        add(Registration.DEEPSLATE_POWER_CRYSTAL_ORE.get(), "Deepslate Power Crystal Ore");

        addCommand("outOfBounds", "Value must be between %s and %s");
        addCommand("valueOverMax", "%s / %s");
        addCommand("playerName", "Player: %s");
        addCommand("playerName.inDimension", "(in dimension %s)");
        addCommand("difficulty.area", "Area Difficulty: %s");
        addCommand("difficulty.player", "Player Difficulty: %s");
        addCommand("difficulty.server", "Server Difficulty: %s");
        addCommand("health.actual", "Actual Health: %s");
        addCommand("health.heartCrystals", "Heart Crystals: %s");
        addCommand("health.heartCrystals.values", "%d (%s health)");
        addCommand("power.actual", "Actual Attack Damage: %s");
        addCommand("power.powerCrystals", "Power Crystals: %s");
        addCommand("power.powerCrystals.values", "%d (%s damage)");
        addCommand("recalculate.start", "Recalculating difficulty for all entities! This may take some time...");
        addCommand("recalculate.finish", "Recalculate finished! Processed %d entities.");

        add(Registration.BANDAGED.get(), "Bandaged");

        add(Registration.BANDAGES.get(), "Bandages");
        add(Registration.HEART_CRYSTAL_SHARD.get(), "Heart Crystal Shard");
        add(Registration.CURSED_HEART.get(), "Cursed Heart");
        add(Registration.ENCHANTED_HEART.get(), "Enchanted Heart");
        add(Registration.CHANCE_HEART.get(), "Chance Heart");
        addItem("difficulty_changer.effectDesc", "Difficulty %s");
        addItem("stat_booster.notEnoughXP", "You need %d XP levels to use this.");
        add(Registration.HEART_CRYSTAL.get(), "Heart Crystal");
        addItem("heart_crystal.desc", "Increases maximum health");
        add(Registration.HEART_DUST.get(), "Heart Dust");
        addItem("healing_item.value", "Restores %d%% of your health over %d seconds.");
        addItem("healing_item.howToUse", "Use for %d seconds to apply.");
        add(Registration.MEDKIT.get(), "Medkit");
        add(Registration.POWER_CRYSTAL.get(), "Power Crystal");
        addItem("power_crystal.desc", "Increases base attack damage");
        add(Registration.POWER_CRYSTAL_SHARD.get(), "Power Crystal Shard");

        noPost("itemGroup", "Scaling Health");
        add("key", "difficultyMeter", "Difficulty Meter Toggle");
        add("misc", "difficultyMeterText", "DIFFICULTY");
        add("misc", "sleepWarning", "[Scaling Health] Warning: Sleeping will change your difficulty.");
        add("misc", "afkmessage", "You are now afk, you will gain less difficulty with time.");

        addSubtitle("cursed_heart_use", "Cursed Heart used");
        addSubtitle("enchanted_heart_use", "Enchanted Heart used");
        addSubtitle("heart_crystal_use", "Heart Crystal used");
        addSubtitle("player_died", "Somebody died (LOL)");

        addDiffMode("average", "Average");
        addDiffMode("extrema", "Extrema");
        addDiffMode("distance", "Distance");
        addDiffMode("distance_and_time", "Distance and Time");
        addDiffMode("server_wide", "Server Wide");

        noPre("tab", "Scaling Health");
    }

    private void addDiffMode(String post, String translation) {
        noPre("modes.difficulty." + post, translation);
    }

    private void addSubtitle(String post, String translation) {
        noPre("subtitle." + post, translation);
    }

    private void addItem(String post, String translation) {
        add("item", post, translation);
    }

    private void addCommand(String post, String translation) {
        add("command", post, translation);
    }

    private void noPre(String post, String translation) {
        add(ScalingHealth.MOD_ID + "." + post, translation);
    }

    private void noPost(String pre, String translation) {
        add(pre + "." + ScalingHealth.MOD_ID, translation);
    }

    private void add(String pre, String post, String translation) {
        add(pre + "." + ScalingHealth.MOD_ID + "." + post, translation);
    }
}
