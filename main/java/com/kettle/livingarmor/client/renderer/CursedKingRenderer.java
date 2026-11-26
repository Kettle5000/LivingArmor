
package com.kettle.livingarmor.client.renderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.model.geom.ModelLayers;

import com.kettle.livingarmor.entity.CursedKingEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;


@OnlyIn(Dist.CLIENT)
public class CursedKingRenderer<T extends CursedKingEntity> extends HumanoidMobRenderer<CursedKingEntity, CursedKingModel<CursedKingEntity>> {
	public CursedKingRenderer(EntityRendererProvider.Context context) {
		super(context, new CursedKingModel<CursedKingEntity>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer<>(this, new CursedKingModel<CursedKingEntity>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new CursedKingModel<CursedKingEntity>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
	}

	@Override
	public ResourceLocation getTextureLocation(CursedKingEntity entity) {
		return new ResourceLocation("living_armor:textures/entities/temp_texture.png");
	}
	
	@Override
	protected void setupRotations(CursedKingEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
	    if (entity.deathTime <= 0) {
	        super.setupRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
	    } else if (!entity.hasPose(Pose.SLEEPING)) {
	        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - rotationYaw));
	      }
	}
	
}
