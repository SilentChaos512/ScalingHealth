package net.silentchaos512.scalinghealth.objects;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.silentchaos512.scalinghealth.ScalingHealth;
import net.silentchaos512.scalinghealth.loot.TableGlobalModifier;
import net.silentchaos512.scalinghealth.loot.conditions.EntityGroupCondition;
import net.silentchaos512.scalinghealth.loot.conditions.SHMobProperties;
import net.silentchaos512.scalinghealth.objects.item.DifficultyMutatorItem;
import net.silentchaos512.scalinghealth.objects.item.HealingItem;
import net.silentchaos512.scalinghealth.objects.item.HeartCrystal;
import net.silentchaos512.scalinghealth.objects.item.PowerCrystal;
import net.silentchaos512.scalinghealth.objects.potion.BandagedEffect;
import net.silentchaos512.scalinghealth.world.HeartCrystalPlacement;
import net.silentchaos512.scalinghealth.world.PowerCrystalPlacement;

public class Registration {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ScalingHealth.MOD_ID);
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ScalingHealth.MOD_ID);
    private static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ScalingHealth.MOD_ID);
    private static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ScalingHealth.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ScalingHealth.MOD_ID);
    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLMS = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, ScalingHealth.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ScalingHealth.MOD_ID);
    private static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS = DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, ScalingHealth.MOD_ID);
    private static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS = DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, ScalingHealth.MOD_ID);

    public static final RegistryObject<Block> HEART_CRYSTAL_ORE = BLOCKS.register("heart_crystal_ore", () ->
            new DropExperienceBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3, 15)
                            .requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)
            )
    );

    public static final RegistryObject<Block> DEEPLSATE_HEART_CRYSTAL_ORE = BLOCKS.register("deepslate_heart_crystal_ore", () ->
            new DropExperienceBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3, 15)
                            .requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)
            )
    );

    public static final RegistryObject<Block> POWER_CRYSTAL_ORE = BLOCKS.register("power_crystal_ore", () ->
            new DropExperienceBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3, 15)
                            .requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)
            )
    );

    public static final RegistryObject<Block> DEEPSLATE_POWER_CRYSTAL_ORE = BLOCKS.register("deepslate_power_crystal_ore", () ->
            new DropExperienceBlock(
                    Block.Properties.of()
                            .mapColor(MapColor.STONE)
                            .instrument(NoteBlockInstrument.BASEDRUM)
                            .strength(3, 15)
                            .requiresCorrectToolForDrops(),
                    UniformInt.of(1, 5)
            )
    );

    public static final RegistryObject<Item> HEART_CRYSTAL_ORE_ITEM = ITEMS.register("heart_crystal_ore", () ->
            new BlockItem(HEART_CRYSTAL_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> POWER_CRYSTAL_ORE_ITEM = ITEMS.register("power_crystal_ore", () ->
            new BlockItem(POWER_CRYSTAL_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_HEART_CRYSTAL_ORE_ITEM = ITEMS.register("deepslate_heart_crystal_ore", () ->
            new BlockItem(DEEPLSATE_HEART_CRYSTAL_ORE.get(), new Item.Properties()));
    public static final RegistryObject<Item> DEEPSLATE_POWER_CRYSTAL_ORE_ITEM = ITEMS.register("deepslate_power_crystal_ore", () ->
            new BlockItem(DEEPSLATE_POWER_CRYSTAL_ORE.get(), new Item.Properties()));


    //Crystals
    public static final RegistryObject<Item> HEART_CRYSTAL = ITEMS.register("heart_crystal", () ->
            new HeartCrystal(new Item.Properties()));
    public static final RegistryObject<Item> HEART_CRYSTAL_SHARD = ITEMS.register("heart_crystal_shard", () ->
            new Item(new Item.Properties()));
    public static final RegistryObject<Item> HEART_DUST = ITEMS.register("heart_dust", () ->
            new Item(new Item.Properties()));
    public static final RegistryObject<Item> POWER_CRYSTAL = ITEMS.register("power_crystal", () ->
            new PowerCrystal(new Item.Properties()));
    public static final RegistryObject<Item> POWER_CRYSTAL_SHARD = ITEMS.register("power_crystal_shard", () ->
            new Item(new Item.Properties()));

    //healing
    public static final RegistryObject<Item> BANDAGES = ITEMS.register("bandages", () ->
            new HealingItem(0.3f, 1));
    public static final RegistryObject<Item> MEDKIT = ITEMS.register("medkit", () ->
            new HealingItem(0.7f, 4));

    //difficulty hearts
    public static final RegistryObject<Item> CURSED_HEART = ITEMS.register("cursed_heart", () ->
            new DifficultyMutatorItem(DifficultyMutatorItem.Type.CURSED, new Item.Properties()));
    public static final RegistryObject<Item> ENCHANTED_HEART = ITEMS.register("enchanted_heart", () ->
            new DifficultyMutatorItem(DifficultyMutatorItem.Type.ENCHANTED, new Item.Properties()));
    public static final RegistryObject<Item> CHANCE_HEART = ITEMS.register("chance_heart", () ->
            new DifficultyMutatorItem(DifficultyMutatorItem.Type.CHANCE, new Item.Properties()));

    public static final RegistryObject<MobEffect> BANDAGED = EFFECTS.register("bandaged", () ->
            new BandagedEffect(MobEffectCategory.NEUTRAL, 0xf7dcad)
                    .addAttributeModifier(
                            Attributes.MOVEMENT_SPEED,
                            BandagedEffect.MOD_UUID,
                            BandagedEffect.SPEED_MODIFIER,
                            AttributeModifier.Operation.MULTIPLY_TOTAL
                    ));

    public static final RegistryObject<SimpleParticleType> HEART_CRYSTAL_PARTICLE = PARTICLES.register("heart_crystal", () ->
            new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> POWER_CRYSTAL_PARTICLE = PARTICLES.register("power_crystal", () ->
            new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> CURSED_HEART_PARTICLE = PARTICLES.register("cursed_heart", () ->
            new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> ENCHANTED_HEART_PARTICLE = PARTICLES.register("enchanted_heart", () ->
            new SimpleParticleType(false));

    public static final RegistryObject<SoundEvent> CURSED_HEART_USE = makeSound("cursed_heart_use");
    public static final RegistryObject<SoundEvent> ENCHANTED_HEART_USE = makeSound("enchanted_heart_use");
    public static final RegistryObject<SoundEvent> HEART_CRYSTAL_USE = makeSound("heart_crystal_use");
    public static final RegistryObject<SoundEvent> PLAYER_DIED = makeSound("player_died");

    public static final RegistryObject<Codec<TableGlobalModifier>> TABLE_INJECTOR =
            GLMS.register("table_loot_mod", TableGlobalModifier.CODEC);

    public static final RegistryObject<CreativeModeTab> SH_TAB =
            TABS.register("scaling_health", () ->
                    CreativeModeTab.builder()
                            .title(Component.translatable("scalinghealth.tab"))
                            .icon(() -> new ItemStack(HEART_CRYSTAL.get()))
                            .build());

    public static final RegistryObject<LootItemConditionType> MOB_PROPERTIES =
            LOOT_CONDITIONS.register(SHMobProperties.NAME.getPath(), () -> new LootItemConditionType(new SHMobProperties.ThisSerializer()));
    public static final RegistryObject<LootItemConditionType> ENTITY_GROUP =
            LOOT_CONDITIONS.register(EntityGroupCondition.NAME.getPath(), () -> new LootItemConditionType(new EntityGroupCondition.ThisSerializer()));


    public static final RegistryObject<PlacementModifierType<?>> HEART_CRYSTAL_PLACEMENT =
            PLACEMENT_MODIFIERS.register("heart_crystal_placement", () -> placement(HeartCrystalPlacement.CODEC));
    public static final RegistryObject<PlacementModifierType<?>> POWER_CRYSTAL_PLACEMENT =
            PLACEMENT_MODIFIERS.register("power_crystal_placement", () -> placement(PowerCrystalPlacement.CODEC));

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
        ITEMS.register(bus);
        PARTICLES.register(bus);
        EFFECTS.register(bus);
        SOUNDS.register(bus);
        GLMS.register(bus);
        TABS.register(bus);
        LOOT_CONDITIONS.register(bus);
        PLACEMENT_MODIFIERS.register(bus);
        bus.addListener(Registration::buildCreativeTab);
    }

    public static void buildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == SH_TAB.getKey()) {
            event.accept(HEART_CRYSTAL_ORE_ITEM);
            event.accept(POWER_CRYSTAL_ORE_ITEM);
            event.accept(DEEPSLATE_HEART_CRYSTAL_ORE_ITEM);
            event.accept(DEEPSLATE_POWER_CRYSTAL_ORE_ITEM);
            event.accept(HEART_CRYSTAL);
            event.accept(HEART_CRYSTAL_SHARD);
            event.accept(HEART_DUST);
            event.accept(POWER_CRYSTAL);
            event.accept(POWER_CRYSTAL_SHARD);
            event.accept(BANDAGES);
            event.accept(MEDKIT);
            event.accept(CURSED_HEART);
            event.accept(ENCHANTED_HEART);
            event.accept(CHANCE_HEART);
        }
    }

    private static RegistryObject<SoundEvent> makeSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(ScalingHealth.getId(name)));
    }

    public static <P extends PlacementModifier> PlacementModifierType<P> placement(Codec<P> codec) {
        return () -> codec;
    }
}
