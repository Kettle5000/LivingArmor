package com.kettle.livingarmor.client.renderer;


import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class LivingArmorModel<T extends LivingEntity> extends HumanoidModel<T> implements ArmedModel, HeadedModel {

	public LivingArmorModel(ModelPart p_170677_) {
		super(p_170677_);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	    if (entity.isBlocking()) {
	        // Rotate the arm to bring it closer to the center
	        this.leftArm.xRot = -1.5F; // More negative for a closer forward position
	        this.leftArm.yRot = 0.4F; // Slight inward rotation toward the body
	        this.leftArm.zRot = -0.2F;  // Adjust for natural positioning

	        // Optionally move the arm forward (offset its position)
	        this.leftArm.setPos(4.0F, 4.0F, -4.0F); // Adjust X, Y, Z to bring it forward
	    } 
	}

}
