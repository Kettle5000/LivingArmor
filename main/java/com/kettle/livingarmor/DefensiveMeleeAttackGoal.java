package com.kettle.livingarmor;

import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.common.ForgeMod;

public class DefensiveMeleeAttackGoal extends MeleeAttackGoal {
	private final LivingArmorEntity livingArmor; // Ensure the mob class extends PathfinderMob and implements shield mechanics.

    public DefensiveMeleeAttackGoal(Monster mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.livingArmor = (LivingArmorEntity) mob; // Cast to your specific entity class.
    }

    @Override
	protected double getAttackReachSqr(LivingEntity entity) {
		return this.mob.getBbWidth() * this.livingArmor.getAttributeValue(ForgeMod.ENTITY_REACH.get()) * 2 * this.mob.getBbWidth() + entity.getBbWidth();
	}
    
    @Override
    protected void checkAndPerformAttack(LivingEntity p_25557_, double p_25558_) {
        if (!this.livingArmor.isUsingShield()) {
        	super.checkAndPerformAttack(p_25557_, p_25558_);
        }

    }
}
