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

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.silentchaos512.scalinghealth.ScalingHealth;

@Mod.EventBusSubscriber(modid = ScalingHealth.MOD_ID, value = Dist.CLIENT)
public final class ScalingHealthClientEvents {
    private static final float DEBUG_TEXT_SCALE = 0.6f;

    private ScalingHealthClientEvents() {}

    @SubscribeEvent
    public static void renderTick(RenderGameOverlayEvent.Post event) {
        /*
        Minecraft mc = Minecraft.getInstance();
        if (Config.Debug.debugMode && Config.Debug.debugOverlay && mc.world != null && event.getType() == ElementType.ALL) {
            FontRenderer fontRender = mc.fontRenderer;

            GL11.glPushMatrix();
            GlStateManager.scaled(DEBUG_TEXT_SCALE, DEBUG_TEXT_SCALE, 1);

            String text = getDebugText();
            int y = 3;
            for (String line : text.split("\n")) {
                String[] array = line.split("=");
                if (array.length == 2) {
                    fontRender.drawString(array[0].trim(), 3, y, Color.VALUE_WHITE);
                    fontRender.drawString(array[1].trim(), 90, y, Color.VALUE_WHITE);
                } else {
                    fontRender.drawString(line, 3, y, Color.VALUE_WHITE);
                }
                y += 10;
            }

            GL11.glPopMatrix();
        }
        */
    }

    private static String getDebugText() {
        /*
        Minecraft mc = Minecraft.getInstance();
        World world = mc.world;
        EntityPlayer player = mc.player;
        PlayerData data = SHPlayerDataHandler.get(player);
        AreaDifficultyMode areaMode = Config.Difficulty.AREA_DIFFICULTY_MODE;
        if (data == null)
            return "Player data is null!";

        StringBuilder ret = new StringBuilder();

        ret.append(String.format("Area Difficulty = %.4f (%s)\n",
                areaMode.getAreaDifficulty(world, player.getPosition()), areaMode.name()));
        ret.append(String.format("Player Difficulty = %.4f\n", data.getDifficulty()));
        ret.append("Player Health = ").append(player.getHealth()).append(" / ").append(player.getMaxHealth()).append("\n");

        // Display all health attribute modifiers.
        IAttributeInstance attr = player.getAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (!attr.getModifiers().isEmpty()) {
            for (AttributeModifier mod : attr.getModifiers()) {
                ret.append("         ").append(mod).append("\n");
            }
        } else {
            ret.append("        No modifiers! That should not happen.\n");
        }

        int regenTimer = PlayerBonusRegenHandler.getTimerForPlayer(player);
        ret.append(String.format("Regen Timer = %d (%ds)", regenTimer, regenTimer / 20)).append("\n");
        ret.append(String.format("Food = %d (%.2f)", player.getFoodStats().getFoodLevel(),
                player.getFoodStats().getSaturationLevel())).append("\n");

        // Blight count
        int blightCount = world.getEntities(EntityLivingBase.class, BlightHandler::isBlight).size();
        int blightFires = world.getEntities(BlightFireEntity.class, e -> true).size();
        ret.append(String.format("Blights (Fires) = %d (%d)", blightCount, blightFires));

        return ret.toString();
        */
        return "";
    }
}
