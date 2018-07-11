package net.silentchaos512.scalinghealth.init;

import net.minecraft.potion.Potion;
import net.silentchaos512.lib.registry.IRegistrationHandler;
import net.silentchaos512.lib.registry.SRegistry;
import net.silentchaos512.scalinghealth.potion.PotionBandaged;

public class ModPotions implements IRegistrationHandler<Potion> {

    public static PotionBandaged bandaged = new PotionBandaged();

    @Override
    public void registerAll(SRegistry reg) {
        reg.registerPotion(bandaged);
    }
}
