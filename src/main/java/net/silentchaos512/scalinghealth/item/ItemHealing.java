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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.silentchaos512.lib.registry.ICustomMesh;
import net.silentchaos512.lib.registry.ICustomModel;
import net.silentchaos512.lib.util.LocalizationHelper;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.init.ModPotions;

import javax.annotation.Nullable;
import java.util.List;

public class ItemHealing extends Item implements ICustomMesh, ICustomModel {

    public enum Type {
        BANDAGE(0.3f, 1), MEDKIT(0.7f, 4);

        public final float healPercentage;
        public final int effectDuration;
        public final int amplifier;

        Type(float healPercentage, int speed) {
            this.healPercentage = healPercentage;
            this.effectDuration = (int) (healPercentage * 100 * 20 * 2 / speed);
            this.amplifier = speed - 1;
        }

        public static Type clampMeta(int unclampedMetadata) {
            return values()[MathHelper.clamp(unclampedMetadata, 0, values().length - 1)];
        }
    }
    public static final int USE_TIME = 5 * 20;

    public ItemHealing() {
        setMaxStackSize(4);
        setHasSubtypes(true);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return USE_TIME;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.getHealth() < player.getMaxHealth() && !player.isPotionActive(ModPotions.bandaged)) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World world, EntityLivingBase entityLiving) {
        if (!world.isRemote) {
            Type healingType = Type.clampMeta(stack.getItemDamage());
            entityLiving.addPotionEffect(new PotionEffect(ModPotions.bandaged, healingType.effectDuration,
                    healingType.amplifier, false, false));
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase player, int count) {
        if (count % 10 == 0) {
            player.playSound(SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                    1.25f, (float) (1.1f + 0.05f * ScalingHealth.random.nextGaussian()));
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!isInCreativeTab(tab)) return;
        for (Type type : Type.values()) {
            list.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> list, ITooltipFlag flagIn) {
        LocalizationHelper loc = ScalingHealth.localizationHelper;
        Type healingType = Type.clampMeta(stack.getItemDamage());

        list.add(loc.getSubText(this, "healingValue", (int) (healingType.healPercentage * 100), healingType.effectDuration / 20));
        list.add(loc.getSubText(this, "howToUse", USE_TIME / 20));
    }

    @Override
    public String getTranslationKey(ItemStack stack) {
        return super.getTranslationKey() + stack.getItemDamage();
    }

    private static final ModelResourceLocation MODEL_0 = new ModelResourceLocation("scalinghealth:healingitem0", "inventory");
    private static final ModelResourceLocation MODEL_1 = new ModelResourceLocation("scalinghealth:healingitem1", "inventory");

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
