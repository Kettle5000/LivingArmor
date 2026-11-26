package com.kettle.livingarmor;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.common.MinecraftForge;


import com.kettle.livingarmor.init.LivingArmorModTabs;
import com.kettle.livingarmor.init.LivingArmorModItems;
import com.kettle.livingarmor.init.LivingArmorModSounds;
import com.kettle.livingarmor.init.LivingArmorModEntities;
import com.kettle.livingarmor.init.LivingArmorModEnchantments;


@Mod("living_armor")
public class LivingArmorMod {
	public static final Logger LOGGER = LogManager.getLogger(LivingArmorMod.class);
	public static final String MODID = "living_armor";

	public LivingArmorMod() {
		// Start of user code block mod constructor
		// End of user code block mod constructor
		MinecraftForge.EVENT_BUS.register(this);
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register our mod's ForgeConfigSpec so that Forge can create and load the config file for us
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC, "LivingArmor.toml");
		LivingArmorModItems.REGISTRY.register(bus);
		LivingArmorModEntities.REGISTRY.register(bus);
		LivingArmorModEnchantments.REGISTRY.register(bus);
		LivingArmorModTabs.REGISTRY.register(bus);
		LivingArmorModSounds.REGISTRY.register(bus);

		// Start of user code block mod init
		// End of user code block mod init
	}
}

