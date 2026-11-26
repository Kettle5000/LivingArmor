
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package com.kettle.livingarmor.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.enchantment.Enchantment;

import com.kettle.livingarmor.enchantment.HauntedEnchantment;
import com.kettle.livingarmor.LivingArmorMod;

public class LivingArmorModEnchantments {
	public static final DeferredRegister<Enchantment> REGISTRY = DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, LivingArmorMod.MODID);
	public static final RegistryObject<Enchantment> HAUNTED = REGISTRY.register("haunted", () -> new HauntedEnchantment());
}
