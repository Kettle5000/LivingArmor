package com.kettle.livingarmor;

import com.kettle.livingarmor.entity.CursedKingEntity;
import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class CursedKingMoveControl extends MoveControl {
	private final CursedKingEntity livingArmor;
	public CursedKingMoveControl(Mob mob) {
		super(mob);
		this.livingArmor = (CursedKingEntity) mob;
	}
	
	@Override
	public void tick() {
		if (!this.livingArmor.isUsingShield()) {
			super.tick();
		} else {
			if (this.livingArmor.onGround()) {
				this.livingArmor.setSpeed((float) 0.2);
			}
		}
	}

}
