package net.silentchaos512.scalinghealth.resources.mechanics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
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
public class SHMechanicListener extends SimpleJsonResourceReloadListener {
    private static SHMechanicListener currentInstance = null;
    private static SHMechanicListener reloadingInstance = null;

    public static final Logger LOGGER = LogManager.getLogger("SHMechanicsListener");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static final String FOLDER = "sh_mechanics";
    private SHMechanics shMechanics;

    public SHMechanicListener() {
        super(GSON, FOLDER);
        if (currentInstance == null)
            currentInstance = this;
        else
            reloadingInstance = this;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler) {
        Function<String, JsonElement> getter = file -> map.entrySet().stream()
                .filter(e -> e.getKey().getNamespace().equals(ScalingHealth.MOD_ID) && e.getKey().getPath().equals(file))
                .map(Map.Entry::getValue)
                .findAny().orElse(JsonNull.INSTANCE);

        var player = PlayerMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(PlayerMechanics.FILE))
                .getOrThrow(false, prefix("PlayerMechanics: "));
        var item = ItemMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(ItemMechanics.FILE))
                .getOrThrow(false, prefix("ItemMechanics: "));
        var mob = MobMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(MobMechanics.FILE))
                .getOrThrow(false, prefix("MobMechanics: "));
        var difficulty = DifficultyMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(DifficultyMechanics.FILE))
                .getOrThrow(false, prefix("DifficultyMechanics: "));
        var ds = DamageScalingMechanics.CODEC.parse(JsonOps.INSTANCE, getter.apply(DamageScalingMechanics.FILE))
                .getOrThrow(false, prefix("DamageScalingMechanics: "));
        this.shMechanics = new SHMechanics(player, item, mob, difficulty, ds);
        LOGGER.debug("Finished Parsing SH Config!");

        if (this == reloadingInstance) {
            currentInstance = this;
            reloadingInstance = null;
        }
    }

    static SHMechanics getInstance() {
        if (currentInstance == null)
            throw new RuntimeException("Tried to access SHMechanicsListener too early!");
        return currentInstance.shMechanics;
    }

    private static Consumer<String> prefix(String pre) {
        return s -> LOGGER.error(pre + s);
    }

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent event) {
        event.addListener(new SHMechanicListener());
    }
}
