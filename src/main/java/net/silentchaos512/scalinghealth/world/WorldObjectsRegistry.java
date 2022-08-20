package net.silentchaos512.scalinghealth.world;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.RegisterEvent;
import net.silentchaos512.scalinghealth.ScalingHealth;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class WorldObjectsRegistry {
    public static final ResourceLocation HEART_CRYSTAL_PLACEMENT = ScalingHealth.getId("heart_crystal_placement");
    public static final ResourceLocation POWER_CRYSTAL_PLACEMENT = ScalingHealth.getId("power_crystal_placement");

    @SubscribeEvent
    public static void register(RegisterEvent event) {
        if (event.getRegistryKey() == Registry.PLACEMENT_MODIFIER_REGISTRY) {
            event.register(Registry.PLACEMENT_MODIFIER_REGISTRY, rh -> rh.register(HEART_CRYSTAL_PLACEMENT, create(HeartCrystalPlacement.CODEC)));
            event.register(Registry.PLACEMENT_MODIFIER_REGISTRY, rh -> rh.register(POWER_CRYSTAL_PLACEMENT, create(PowerCrystalPlacement.CODEC)));
        }
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> create(Codec<P> codec) {
        return () -> codec;
    }
}
