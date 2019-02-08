/*
 * Scaling Health
 * Copyright (C) 2018 SilentChaos512
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 3
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.silentchaos512.scalinghealth.event;

public class WitEventHandler {
    /*
    @SubscribeEvent
    public void onWitEntityInfo(WitEntityInfoEvent event) {
        if (!Config.Debug.debugMode)
            return;

        EntityLivingBase entity = event.entityLiving;
        if (entity != null) {
            if (entity.getEntityData() == null)
                event.lines.add("Entity Data is null!?");
            else
                event.lines.add("Blight: " + BlightHandler.isBlight(entity));
        }

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
    */
}
