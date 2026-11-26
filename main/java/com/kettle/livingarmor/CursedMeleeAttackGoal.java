package com.kettle.livingarmor;

import com.kettle.livingarmor.entity.CursedKingEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraftforge.common.ForgeMod;

public class CursedMeleeAttackGoal extends MeleeAttackGoal {
	private final CursedKingEntity livingArmor; // Ensure the mob class extends PathfinderMob and implements shield mechanics.

    public CursedMeleeAttackGoal(Monster mob, double speedModifier, boolean followingTargetEvenIfNotSeen) {
        super(mob, speedModifier, followingTargetEvenIfNotSeen);
        this.livingArmor = (CursedKingEntity) mob; // Cast to your specific entity class.
    }

    @Override
	protected double getAttackReachSqr(LivingEntity entity) {
		return this.mob.getBbWidth() * this.livingArmor.getAttributeValue(ForgeMod.ENTITY_REACH.get()) * 2 * this.mob.getBbWidth() + entity.getBbWidth();
	}
}
