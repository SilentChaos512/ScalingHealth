package net.silentchaos512.scalinghealth.loot.conditions;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.capability.IDifficultyAffected;
import net.silentchaos512.scalinghealth.objects.Registration;
import net.silentchaos512.scalinghealth.utils.config.SHDifficulty;

public class SHMobProperties implements LootItemCondition {
    public static final ResourceLocation NAME = new ResourceLocation(ScalingHealth.MOD_ID, "mob_properties");

    private final LootContext.EntityTarget target;
    private final boolean isBlight;
    private final float minDifficulty;
    private final float maxDifficulty;

    public SHMobProperties(LootContext.EntityTarget target, boolean isBlight, float minDifficulty, float maxDifficulty) {
        this.target = target;
        this.isBlight = isBlight;
        this.minDifficulty = minDifficulty;
        this.maxDifficulty = maxDifficulty;
    }

    public static LootItemCondition.Builder builder(LootContext.EntityTarget target, boolean isBlight, float minDifficulty, float maxDifficulty){
        return () -> new SHMobProperties(target, isBlight, minDifficulty, maxDifficulty);
    }

    @Override
    public LootItemConditionType getType() {
        return Registration.MOB_PROPERTIES.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(this.target.getParam());
        if (entity instanceof Mob) {
            IDifficultyAffected affected = SHDifficulty.affected(entity);
            //rare case where its prob better to get the non-blight difficulty
            float difficulty = affected.getDifficulty();
            return difficulty >= this.minDifficulty &&
                    difficulty <= this.maxDifficulty &&
                    (!this.isBlight || affected.isBlight());
        }
        return false;
    }

    public static class ThisSerializer implements Serializer<SHMobProperties> {
        @Override
        public void serialize(JsonObject json, SHMobProperties value, JsonSerializationContext context) {
            json.add("entity", context.serialize(value.target));
            json.addProperty("is_blight", value.isBlight);
            JsonObject difficultyObj = new JsonObject();
            difficultyObj.addProperty("min", value.minDifficulty);
            difficultyObj.addProperty("max", value.maxDifficulty);
            json.add("difficulty", difficultyObj);
        }

        @Override
        public SHMobProperties deserialize(JsonObject json, JsonDeserializationContext context) {
            LootContext.EntityTarget target = GsonHelper.getAsObject(json, "entity", context, LootContext.EntityTarget.class);
            boolean isBlight = GsonHelper.getAsBoolean(json, "is_blight", false);
            float minDifficulty = 0;
            float maxDifficulty = Float.MAX_VALUE;
            if (json.has("difficulty")) {
                JsonElement difficulty = json.get("difficulty");
                if (difficulty.isJsonObject()) {
                    JsonObject jsonObject = difficulty.getAsJsonObject();
                    minDifficulty = GsonHelper.getAsFloat(jsonObject, "min", minDifficulty);
                    maxDifficulty = GsonHelper.getAsFloat(jsonObject, "max", maxDifficulty);
                } else {
                    minDifficulty = maxDifficulty = difficulty.getAsFloat();
                }
            }
            return new SHMobProperties(target, isBlight, minDifficulty, maxDifficulty);
        }
    }
}
