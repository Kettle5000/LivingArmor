package com.kettle.livingarmor.init;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;

import com.kettle.livingarmor.client.renderer.CursedKingRenderer;
import com.kettle.livingarmor.client.renderer.LivingArmorRenderer;

import net.minecraftforge.api.distmarker.Dist;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class LivingArmorModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(LivingArmorModEntities.LIVING_ARMOR.get(), LivingArmorRenderer::new);
		event.registerEntityRenderer(LivingArmorModEntities.CURSED_KING.get(), CursedKingRenderer::new);
	}
}
