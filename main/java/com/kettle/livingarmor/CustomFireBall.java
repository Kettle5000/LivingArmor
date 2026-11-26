package com.kettle.livingarmor;


import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class CustomFireBall extends Fireball {
	private float power;

	public CustomFireBall(Level p_181151_, LivingEntity p_181152_, double x, double y, double z, float p_181156_) {
		super(EntityType.FIREBALL, p_181152_, x, y, z, p_181151_);
		this.power = p_181156_;
	}

	@Override
	public boolean hurt(DamageSource p_36839_, float p_36840_) {
		return false;
	}
	
	@Override
	protected boolean shouldBurn() {
		return false;
	}
	
	@Override
	public Vec3 getDeltaMovement() {
	    Vec3 baseMovement = super.getDeltaMovement();
	    if (baseMovement.lengthSqr() <= 2.5) {
		    double speedMultiplier = 1.05D; // Increase this for higher speed
		    return baseMovement.scale(speedMultiplier);
	    }
	    return baseMovement;
	}
	
	protected void onHit(HitResult p_37218_) {
	      super.onHit(p_37218_);
	      Level level = this.level();
	      if (!level.isClientSide) {
	         if (net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level(), this.getOwner())) {
	        	 if (this.level() instanceof ServerLevel serverLevel) {
	        		 CustomExplosion.explodeCircle(serverLevel,this.getOwner(),this.blockPosition(), this.power, this.power * 2f, 2.5,
	        				 entity -> {return entity instanceof LivingEntity livingEntity && !(entity instanceof LivingArmorEntity);});
	        	 }
	         }
	         this.discard();
	      }

	   }

	   protected void onHitEntity(EntityHitResult p_37216_) {
	      super.onHitEntity(p_37216_);
	      Level level = this.level();
	      if (!level.isClientSide) {
	         Entity entity = p_37216_.getEntity();
	         Entity entity1 = this.getOwner();
	         entity.hurt(this.damageSources().fireball(this, entity1), 20.0F);
	         if (entity1 instanceof LivingEntity) {
	            this.doEnchantDamageEffects((LivingEntity)entity1, entity);
	         }

	      }
	   }

}
