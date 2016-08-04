package net.silentchaos512.scalinghealth.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.silentchaos512.scalinghealth.config.ConfigScalingHealth;
import net.silentchaos512.scalinghealth.utils.ModifierHandler;
import net.silentchaos512.wit.api.WitEntityInfoEvent;

public class WitEventHandler {

  @SubscribeEvent
  public void onWitEntityInfo(WitEntityInfoEvent event) {

    if (!ConfigScalingHealth.DEBUG_MODE)
      return;

    EntityLivingBase entity = event.entityLiving;
    if (entity != null && entity.getAttributeMap() != null) {
      TextFormatting tf = TextFormatting.GRAY;

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
