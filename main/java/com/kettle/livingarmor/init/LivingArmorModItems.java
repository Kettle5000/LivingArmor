package com.kettle.livingarmor.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

import com.kettle.livingarmor.LivingArmorMod;
import com.kettle.livingarmor.item.*;

import net.minecraft.world.item.Item;

@Mod.EventBusSubscriber(modid = LivingArmorMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LivingArmorModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, LivingArmorMod.MODID);
	public static final RegistryObject<Item> LIVING_ARMOR_SPAWN_EGG = REGISTRY.register("living_armor_spawn_egg", () -> new ForgeSpawnEggItem(LivingArmorModEntities.LIVING_ARMOR, -1, -1, new Item.Properties()));
	public static final RegistryObject<Item> CURSED_KINGS_ARMOR_HELMET = REGISTRY.register("cursed_kings_armor_helmet", () -> new CursedKingsArmorItem.Helmet());
	public static final RegistryObject<Item> CURSED_KINGS_ARMOR_CHESTPLATE = REGISTRY.register("cursed_kings_armor_chestplate", () -> new CursedKingsArmorItem.Chestplate());
	public static final RegistryObject<Item> CURSED_KINGS_ARMOR_LEGGINGS = REGISTRY.register("cursed_kings_armor_leggings", () -> new CursedKingsArmorItem.Leggings());
	public static final RegistryObject<Item> CURSED_KINGS_ARMOR_BOOTS = REGISTRY.register("cursed_kings_armor_boots", () -> new CursedKingsArmorItem.Boots());
	public static Item CURSED_KINGS_HELMET;
	public static Item CURSED_KINGS_CHESTPLATE;
	public static Item CURSED_KINGS_LEGGINGS;
	public static Item CURSED_KINGS_BOOTS;
	
	@SubscribeEvent
	public static void AfterRegistry(final ModConfigEvent event) {
		CURSED_KINGS_HELMET = CURSED_KINGS_ARMOR_HELMET.get();
		CURSED_KINGS_CHESTPLATE = CURSED_KINGS_ARMOR_CHESTPLATE.get();
		CURSED_KINGS_LEGGINGS = CURSED_KINGS_ARMOR_LEGGINGS.get();
		CURSED_KINGS_BOOTS = CURSED_KINGS_ARMOR_BOOTS.get();
	}
}
