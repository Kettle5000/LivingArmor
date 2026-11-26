
package com.kettle.livingarmor.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;

import com.kettle.livingarmor.entity.LivingArmorEntity;



public class LivingArmorRenderer extends HumanoidMobRenderer<LivingArmorEntity, LivingArmorModel<LivingArmorEntity>> {
	public LivingArmorRenderer(EntityRendererProvider.Context context) {
		super(context, new LivingArmorModel(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, new LivingArmorModel(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new LivingArmorModel(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(LivingArmorEntity entity) {
		return new ResourceLocation("living_armor:textures/entities/temp_texture.png");
	}
}
