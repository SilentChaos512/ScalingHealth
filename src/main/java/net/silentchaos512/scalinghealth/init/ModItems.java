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

package net.silentchaos512.scalinghealth.init;

import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.item.ItemDifficultyChanger;
import net.silentchaos512.scalinghealth.item.ItemHealing;
import net.silentchaos512.scalinghealth.item.ItemHeartContainer;

import java.util.ArrayList;
import java.util.Collection;

public final class ModItems {
    public static ItemHeartContainer heart;
    public static Item crystalShard;
    public static ItemDifficultyChanger cursedHeart;
    public static ItemDifficultyChanger enchantedHeart;

    static Collection<ItemBlock> blocksToRegister = new ArrayList<>();

    private ModItems() {}

    public static void registerAll(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> reg = event.getRegistry();

        blocksToRegister.forEach(reg::register);

        heart = register(reg, "heart_container", new ItemHeartContainer());
        crystalShard = register(reg, "heart_shard", new Item(new Item.Builder()));
        register(reg, "heart_dust", new Item(new Item.Builder()));
        register(reg, "bandages", new ItemHealing(0.3f, 1));
        register(reg, "medkit", new ItemHealing(0.7f, 4));
        cursedHeart = register(reg, "cursed_heart", new ItemDifficultyChanger(ItemDifficultyChanger.Type.CURSED));
        enchantedHeart = register(reg, "enchanted_heart", new ItemDifficultyChanger(ItemDifficultyChanger.Type.ENCHANTED));
    }

    private static <T extends Item> T register(IForgeRegistry<Item> reg, String name, T item) {
        ResourceLocation registryName = new ResourceLocation(ScalingHealth.MOD_ID, name);
        item.setRegistryName(registryName);
        reg.register(item);

        return item;
    }
}
