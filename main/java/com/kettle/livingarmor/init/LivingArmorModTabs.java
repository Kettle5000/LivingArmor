package com.kettle.livingarmor.init;

import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.core.registries.Registries;

import com.kettle.livingarmor.LivingArmorMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LivingArmorModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, LivingArmorMod.MODID);

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.SPAWN_EGGS) {
			tabData.accept(LivingArmorModItems.LIVING_ARMOR_SPAWN_EGG.get());
		} else if (tabData.getTabKey() == CreativeModeTabs.COMBAT) {
			tabData.accept(LivingArmorModItems.CURSED_KINGS_ARMOR_HELMET.get());
			tabData.accept(LivingArmorModItems.CURSED_KINGS_ARMOR_CHESTPLATE.get());
			tabData.accept(LivingArmorModItems.CURSED_KINGS_ARMOR_LEGGINGS.get());
			tabData.accept(LivingArmorModItems.CURSED_KINGS_ARMOR_BOOTS.get());
		}
	}
}
