package net.silentchaos512.scalinghealth.resources.mechanics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.resources.JsonReloadListener;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID)
public class SHMechanicListener extends JsonReloadListener {
    private static SHMechanicListener currentInstance = null;
    private static SHMechanicListener reloadingInstance = null;

    public static final Logger LOGGER = LogManager.getLogger("SHMechanicsListener");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String FOLDER = "sh_mechanics";
    private PlayerMechanics playerMechanics;
    private ItemMechanics itemMechanics;
    private MobMechanics mobMechanics;
    private DifficultyMechanics difficultyMechanics;
    private DamageScalingMechanics damageScalingMechanics;

    public SHMechanicListener(boolean server) {
        super(GSON, FOLDER);
        if (!server || currentInstance == null)
            currentInstance = this;
        else
            reloadingInstance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, IResourceManager resourceManager, IProfiler profiler) {
        Function<String, JsonElement> getter = file -> map.entrySet().stream()
                .filter(e -> e.getKey().getNamespace().equals(ScalingHealth.MOD_ID) && e.getKey().getPath().equals(file))
                .map(Map.Entry::getValue)
                .findAny().orElse(JsonNull.INSTANCE);

        this.playerMechanics = PlayerMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(PlayerMechanics.FILE))
                .getOrThrow(false, prefix("PlayerMechanics: "));
        this.itemMechanics = ItemMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(ItemMechanics.FILE))
                .getOrThrow(false, prefix("ItemMechanics: "));
        this.mobMechanics = MobMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(MobMechanics.FILE))
                .getOrThrow(false, prefix("MobMechanics: "));
        this.difficultyMechanics = DifficultyMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(DifficultyMechanics.FILE))
                .getOrThrow(false, prefix("DifficultyMechanics: "));
        this.damageScalingMechanics = DamageScalingMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(DamageScalingMechanics.FILE))
                .getOrThrow(false, prefix("DamageScalingMechanics: "));
        LOGGER.debug("Finished Parsing SH Config!");

        if (this == reloadingInstance) {
            currentInstance = this;
            reloadingInstance = null;
        }
    }

    public static SHMechanicListener getInstance() {
        if (currentInstance == null)
            throw new RuntimeException("Tried to access SHMechanicsListener too early!");
        return currentInstance;
    }

    private static Consumer<String> prefix(String pre) {
        return s -> LOGGER.error(pre + s);
    }

    public static PlayerMechanics getPlayerMechanics() {
        return getInstance().playerMechanics;
    }

    public static ItemMechanics getItemMechanics() {
        return getInstance().itemMechanics;
    }

    public static MobMechanics getMobMechanics() {
        return getInstance().mobMechanics;
    }

    public static DifficultyMechanics getDifficultyMechanics() {
        return getInstance().difficultyMechanics;
    }

    public static DamageScalingMechanics getDamageScalingMechanics() {
        return getInstance().damageScalingMechanics;
    }

    public static void setClientInstance(PlayerMechanics playerMechanics, ItemMechanics itemMechanics, MobMechanics mobMechanics, DifficultyMechanics difficultyMechanics, DamageScalingMechanics damageScalingMechanics) {
        new SHMechanicListener(false);
        currentInstance.playerMechanics = playerMechanics;
        currentInstance.itemMechanics = itemMechanics;
        currentInstance.mobMechanics = mobMechanics;
        currentInstance.difficultyMechanics = difficultyMechanics;
        currentInstance.damageScalingMechanics = damageScalingMechanics;
        ScalingHealth.LOGGER.debug("Loaded SHMechanics on the client.");
    }

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent event) {
        event.addListener(new SHMechanicListener(true));
    }
}
