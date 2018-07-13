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

package net.silentchaos512.scalinghealth.item;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.silentchaos512.lib.registry.ICustomMesh;
import net.silentchaos512.lib.registry.ICustomModel;
import net.silentchaos512.lib.util.Color;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.config.Config;
import net.silentchaos512.scalinghealth.lib.EnumModParticles;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler;
import net.silentchaos512.scalinghealth.utils.SHPlayerDataHandler.PlayerData;

import java.util.List;

public class ItemDifficultyChanger extends Item implements ICustomMesh, ICustomModel {

    enum Type {
        ENCHANTED, CURSED, UNKNOWN; // down (0), up (1)

        static Type getByMeta(int meta) {
            if (meta < 0 || meta >= values().length)
                return UNKNOWN;
            return values()[meta];
        }
    }

    public ItemDifficultyChanger() {
        setHasSubtypes(true);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        if (stack.getItemDamage() > 1) {
            return;
        }

        String amountStr = String.format("%d",
                stack.getItemDamage() == Type.ENCHANTED.ordinal()
                        ? (int) Config.ENCHANTED_HEART_DIFFICULTY_CHANGE
                        : (int) Config.CURSED_HEART_DIFFICULTY_CHANGE);
        if (amountStr.matches("^\\d+")) {
            amountStr = "+" + amountStr;
        }

        String line = ScalingHealth.localizationHelper.getSubText(this, "effectDesc", amountStr);
        list.add(TextFormatting.WHITE + line);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return EnumRarity.EPIC;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tab)) return;

        list.add(new ItemStack(this, 1, 0));
        list.add(new ItemStack(this, 1, 1));
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        PlayerData data = SHPlayerDataHandler.get(player);

        if (data == null) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        double particleX = player.posX;
        double particleY = player.posY + 0.65f * player.height;
        double particleZ = player.posZ;

        switch (Type.getByMeta(stack.getItemDamage())) {
            // Enchanted Heart
            case ENCHANTED:
                // Lower difficulty, consume 1 from stack.
                data.incrementDifficulty(Config.ENCHANTED_HEART_DIFFICULTY_CHANGE);
                stack.shrink(1);

                // Particles and sound effect!
                for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
                    double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
                    double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
                    double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
                    ScalingHealth.proxy.spawnParticles(EnumModParticles.ENCHANTED_HEART,
                            new Color(1f, 1f, 0.5f), world, particleX, particleY, particleZ, xSpeed, ySpeed, zSpeed);
                }
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                        SoundCategory.PLAYERS, 0.4f, 1.7f);

                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            // Cursed Heart
            case CURSED:
                // Raise difficulty, consume 1 from stack.
                data.incrementDifficulty(Config.CURSED_HEART_DIFFICULTY_CHANGE);
                stack.shrink(1);

                // Particles and sound effect!
                for (int i = 0; i < 20 - 5 * ScalingHealth.proxy.getParticleSettings(); ++i) {
                    double xSpeed = 0.08 * ScalingHealth.random.nextGaussian();
                    double ySpeed = 0.05 * ScalingHealth.random.nextGaussian();
                    double zSpeed = 0.08 * ScalingHealth.random.nextGaussian();
                    ScalingHealth.proxy.spawnParticles(EnumModParticles.CURSED_HEART,
                            new Color(0.4f, 0f, 0.6f), world, particleX, particleY, particleZ, xSpeed, ySpeed, zSpeed);
                }
                world.playSound(null, player.getPosition(), SoundEvents.BLOCK_FIRE_EXTINGUISH,
                        SoundCategory.PLAYERS, 0.3f,
                        (float) (0.7f + 0.05f * ScalingHealth.random.nextGaussian()));

                return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            default:
                ScalingHealth.logHelper.warn("DifficultyChanger invalid meta: {}", stack.getItemDamage());
                return new ActionResult<>(EnumActionResult.PASS, stack);
        }
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + stack.getItemDamage();
    }

    private static final ModelResourceLocation MODEL_0 = new ModelResourceLocation("scalinghealth:difficultychanger0", "inventory");
    private static final ModelResourceLocation MODEL_1 = new ModelResourceLocation("scalinghealth:difficultychanger1", "inventory");

    @Override
    public ItemMeshDefinition getCustomMesh() {
        return stack -> stack.getItemDamage() == 0 ? MODEL_0 : MODEL_1;
    }

    @Override
    public void registerModels() {
        ModelLoader.setCustomModelResourceLocation(this, 0, MODEL_0);
        ModelLoader.setCustomModelResourceLocation(this, 1, MODEL_1);
    }
}
