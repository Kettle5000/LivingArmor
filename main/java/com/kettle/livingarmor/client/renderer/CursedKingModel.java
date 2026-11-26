package com.kettle.livingarmor.client.renderer;

import com.kettle.livingarmor.entity.CursedKingEntity;

import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class CursedKingModel<T extends CursedKingEntity> extends HumanoidModel<T> implements ArmedModel, HeadedModel {

	public CursedKingModel(ModelPart root) {
		super(root);
	}

	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {

		if (entity.deathTime > 0) {
            // Timing variables
            float time = entity.deathTime;
            float progress;

            // Head (Phase 1)
            if (entity.deathTime <= 20) {
            	progress = time/20.0f;
                this.head.yRot = 0.8F * progress;
                this.head.zRot = -0.4F * progress;
            }
            // Right Arm Detaches (Phase 2: 20-33 ticks)
            else if (entity.deathTime <= 25) {
            	this.head.yRot = 0.8F;
                this.head.zRot = -0.4F;
                
                this.rightArm.setPos(-5.0f, 2.0f + 3.6f *(time - 20), -5.0f);
                this.rightArm.xRot = -1.5f * ((time - 20) * 0.2f);
            }
            // Look at Left Arm (Phase 3: 30-40 ticks)
            else if (entity.deathTime <= 45) {
            	this.rightArm.setPos(-5.0f, 20.f, -5.0f);
                this.rightArm.xRot = -1.5f;
                
                this.head.yRot = -0.8F * ((time - 25) * 0.05f);
                this.head.zRot = 0.4F * (time - 25) * 0.05f;
            }
            // Left Arm Detaches (Phase 4: 40-53 ticks)
            else if (entity.deathTime <= 50) {
            	this.rightArm.setPos(-5.0f, 20.f, -5.0f);
                this.rightArm.xRot = -1.5f;
                this.head.yRot = -0.8F;
                this.head.zRot = 0.4F;
                
                this.leftArm.setPos(5.0f, 2.0f + 3.6f * (time - 45), 0.0f);
                this.leftArm.xRot = -1.5f * ((time - 45) * 0.2f);
            }
            // Head Detaches (Phase 5: 50-65 ticks)
            else if (entity.deathTime <= 60) {
            	this.rightArm.setPos(-5.0f, 20.f, -5.0f);
                this.rightArm.xRot = -1.5f;
                this.head.yRot = -0.8F;
                this.head.zRot = 0.4F;
                this.leftArm.setPos(5.0f, 20.0f, 0.0f);
                this.leftArm.xRot = -1.5f;
                this.head.setPos(0.0f, 1.5f *(time - 50), -1.5f * ((time - 50)) * 0.75f);
                this.head.xRot = 1.5F * (time - 50) * 0.1f;
            }
            // Torso Detaches (Phase 6: 60-75 ticks)
            else if (entity.deathTime <= 70) {
            	this.rightArm.setPos(-5.0f, 20.f, -5.0f);
                this.rightArm.xRot = -1.5f;
                this.head.yRot = -0.8F;
                this.head.zRot = 0.4F;
                this.leftArm.setPos(5.0f, 20.0f, 0.0f);
                this.leftArm.xRot = -1.5f;
                this.head.setPos(0.0f, 15.0f, -11.25f);
                this.head.xRot = 1.5F;
                
                this.body.setPos(0.0f, 2.0f * (time - 60), 1.5f * (time - 60));
                this.body.xRot = -1.5f * ((time - 60) * 0.1f);
            }
            // Legs Detach (Phase 7: 70-80 ticks)
            else if (entity.deathTime <= 80) {
            	this.rightArm.setPos(-5.0f, 20.f, -5.0f);
                this.rightArm.xRot = -1.5f;
                this.head.yRot = -0.8F;
                this.head.zRot = 0.4F;
                this.leftArm.setPos(5.0f, 20.0f, 0.0f);
                this.leftArm.xRot = -1.5f;
                this.head.setPos(0.0f, 15.0f, -11.25f);
                this.head.xRot = 1.5F;
                this.body.setPos(0.0f, 20.0f, 15.0f);
                this.body.xRot = -1.5f;
            	
                this.leftLeg.setPos(1.9f + (time - 70f) * 0.7f, 12f + ((time - 70) * 0.8f), 0.0f);
                this.rightLeg.setPos(-1.9f - (time - 70f) * 0.7f, 12f + ((time - 70) * 0.8f), 0.0f);
                this.rightLeg.zRot = -1.5f * ((time - 70f) / 10f);
                this.leftLeg.zRot = 1.5f * ((time - 70f) / 10f);
            }
        } else {
			this.head.x = 0.0f;
			this.head.y = 0.0f;
			this.head.z = 0.0f;
			this.body.x = 0.0f;
			this.body.y = 0.0f;
			this.body.z = 0.0f;
			this.leftArm.x = 5.0f;
			this.leftArm.y = 2.0f;
			this.leftArm.z = 0.0f;
			this.rightArm.x = -5.0f;
			this.rightArm.y = 2.0f;
			this.rightArm.z = 0.0f;
			this.rightLeg.x = -1.9f;
			this.rightLeg.y = 12f;
			this.rightLeg.z = 0.0f;
			this.leftLeg.x = 1.9f;
			this.leftLeg.y = 12f;
			this.leftLeg.z = 0.0f;
			this.head.yRot = 0.0f;
			this.head.zRot = 0.0f;
			super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
			if (entity.isBlocking()) {
				// Rotate the arm to bring it closer to the center
				this.leftArm.xRot = -1.5F; // More negative for a closer forward position
				this.leftArm.yRot = 0.4F; // Slight inward rotation toward the body
				this.leftArm.zRot = -0.2F; // Adjust for natural positioning

				// Optionally move the arm forward (offset its position)
				this.leftArm.setPos(4.0F, 4.0F, -4.0F); // Adjust X, Y, Z to bring it forward
			}
		}
	}

}

