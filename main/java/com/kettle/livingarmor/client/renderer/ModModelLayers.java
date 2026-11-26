package com.kettle.livingarmor.client.renderer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModModelLayers {
	@SubscribeEvent
	public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
		event.registerLayerDefinition(CursedKingHelmetModel.LAYER_LOCATION, CursedKingHelmetModel::createBodyLayer);
		event.registerLayerDefinition(CursedKingLeggingsModel.LAYER_LOCATION, CursedKingLeggingsModel::createBodyLayer);
	}
}
