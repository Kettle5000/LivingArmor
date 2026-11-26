package com.kettle.livingarmor.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;

import com.kettle.livingarmor.LivingArmorMod;

import net.minecraft.resources.ResourceLocation;


public class LivingArmorModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, LivingArmorMod.MODID);
	public static final RegistryObject<SoundEvent> CHARGED_BOMB = REGISTRY.register("charged_bomb", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("living_armor", "charged_bomb")));
	public static final RegistryObject<SoundEvent> CURSED_KINGS_DEATH = REGISTRY.register("cursed_king_death", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("living_armor", "cursed_king_death")));
}
