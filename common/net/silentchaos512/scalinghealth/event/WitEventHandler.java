package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;
import net.silentchaos512.wit.api.WitEntityInfoEvent;

public class WitEventHandler {

  @SubscribeEvent
  public void onWitEntityInfo(WitEntityInfoEvent event) {

    if (!ConfigScalingHealth.DEBUG_MODE)
      return;

    EntityLivingBase entity = event.entityLiving;
    if (entity != null && entity.getAttributeMap() != null) {
      TextFormatting tf = TextFormatting.GRAY;

      if (entity instanceof EntityPlayer) {
        PlayerData data = SHPlayerDataHandler.get((EntityPlayer) entity);
        if (data != null)
          event.lines.add(tf + String.format("Difficulty: %.4f", data.getDifficulty()));
      }

      for (IAttributeInstance attr : entity.getAttributeMap().getAllAttributes()) {
        if (attr != null)
          for (AttributeModifier mod : attr.getModifiers())
            if (mod != null)
              event.lines.add(tf + String.format("%s: %.3f (op %d)", mod.getName(),
                  (float) mod.getAmount(), mod.getOperation()));
      }
    }
  }
}
