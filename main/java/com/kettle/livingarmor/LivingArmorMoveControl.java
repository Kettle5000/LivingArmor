package com.kettle.livingarmor;

import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.MoveControl;

public class LivingArmorMoveControl extends MoveControl {
	private final LivingArmorEntity livingArmor;
	public LivingArmorMoveControl(Mob mob) {
		super(mob);
		this.livingArmor = (LivingArmorEntity) mob;
	}
	
	@Override
	public void tick() {
		if (!this.livingArmor.isUsingShield()) {
			super.tick();
		} else {
			if (this.livingArmor.onGround()) {
				this.livingArmor.setSpeed((float) 0.05);
			}
		}
	}

}
