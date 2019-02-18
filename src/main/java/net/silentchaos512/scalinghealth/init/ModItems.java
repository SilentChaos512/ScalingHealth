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
import net.minecraft.item.ItemGroup;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.item.DifficultyMutatorItem;
import net.silentchaos512.scalinghealth.item.HealingItem;
import net.silentchaos512.scalinghealth.item.HeartCrystal;
import net.silentchaos512.scalinghealth.item.PowerCrystal;
import net.silentchaos512.utils.Lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Supplier;

public enum ModItems implements IItemProvider {
    HEART_CRYSTAL(HeartCrystal::new),
    HEART_CRYSTAL_SHARD(() -> new Item(new Item.Builder().group(ItemGroup.MISC))),
    HEART_DUST(() -> new Item(new Item.Builder().group(ItemGroup.MISC))),
    POWER_CRYSTAL(PowerCrystal::new),
    POWER_CRYSTAL_SHARD(() -> new Item(new Item.Builder().group(ItemGroup.MISC))),
    BANDAGES(() -> new HealingItem(0.3f, 1)),
    MEDKIT(() -> new HealingItem(0.7f, 4)),
    CURSED_HEART(() -> new DifficultyMutatorItem(DifficultyMutatorItem.Type.CURSED)),
    ENCHANTED_HEART(() -> new DifficultyMutatorItem(DifficultyMutatorItem.Type.ENCHANTED));

    private final Lazy<Item> item;

    ModItems(Supplier<Item> factory) {
        item = Lazy.of(factory);
    }

    @Override
    public Item asItem() {
        return item.get();
    }

    public String getName() {
        return name().toLowerCase(Locale.ROOT);
    }

    static final Collection<ItemBlock> blocksToRegister = new ArrayList<>();

    public static void registerAll(RegistryEvent.Register<Item> event) {
        // Workaround for Forge event bus bug
        if (!event.getName().equals(ForgeRegistries.ITEMS.getRegistryName())) return;

        blocksToRegister.forEach(ForgeRegistries.ITEMS::register);

        for (ModItems item : values()) {
            register(item.getName(), item.asItem());
        }
    }

    private static void register(String name, Item item) {
        ResourceLocation registryName = new ResourceLocation(ScalingHealth.MOD_ID, name);
        item.setRegistryName(registryName);
        ForgeRegistries.ITEMS.register(item);
    }
}
