package com.kettle.livingarmor.init;

import com.kettle.livingarmor.entity.CursedKingEntity;
import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;

public class NearestFactionEnemyGoal<T extends Mob> extends NearestAttackableTargetGoal<T> {
    private final Mob mob;
    private final Class<? extends Mob> targetClass;

    public NearestFactionEnemyGoal(Mob mob, Class<T> targetClass) {
        super(mob, targetClass, true);
        this.mob = mob;
        this.targetClass = targetClass;
    }

    @Override
    public boolean canUse() {
        return super.canUse() && this.target != null && isEnemyFaction(this.target) && this.mob.isAlive();
    }

    private boolean isEnemyFaction(LivingEntity target) {
    	int faction1 = 0;
        if (this.mob instanceof CursedKingEntity) {
            faction1 = ((CursedKingEntity) this.mob).getFaction();
        } else if (this.mob instanceof LivingArmorEntity) {
        	faction1 = ((LivingArmorEntity) this.mob).getFaction();
        }
        int faction2 = 0;
        if (target instanceof CursedKingEntity) {
            faction2 = ((CursedKingEntity) target).getFaction();
        } else if (target instanceof LivingArmorEntity) {
        	faction2 = ((LivingArmorEntity) target).getFaction();
        }
        return faction1 != faction2;
    }
}