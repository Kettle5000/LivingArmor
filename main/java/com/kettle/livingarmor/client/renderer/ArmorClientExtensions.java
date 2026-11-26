package com.kettle.livingarmor.client.renderer;

import java.util.Collections;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

@OnlyIn(Dist.CLIENT)
public class ArmorClientExtensions implements IClientItemExtensions {
	
	public static ArmorClientExtensions extensions = new ArmorClientExtensions();

	@Override
	public HumanoidModel getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel defaultModel) {
		HumanoidModel armorModel = new HumanoidModel(new ModelPart(Collections.emptyList(),
				Map.of("head", new CursedKingHelmetModel(Minecraft.getInstance().getEntityModels().bakeLayer(CursedKingHelmetModel.LAYER_LOCATION)).Helmet, "hat", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "body",
						new ModelPart(Collections.emptyList(), Collections.emptyMap()), "right_arm", new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_arm",
						new ModelPart(Collections.emptyList(), Collections.emptyMap()), "left_leg", new CursedKingLeggingsModel(Minecraft.getInstance().getEntityModels().bakeLayer(CursedKingLeggingsModel.LAYER_LOCATION)).LeftLeg2, "right_leg",
						new CursedKingLeggingsModel(Minecraft.getInstance().getEntityModels().bakeLayer(CursedKingLeggingsModel.LAYER_LOCATION)).RightLeg1)));
		armorModel.crouching = living.isShiftKeyDown();
		armorModel.riding = defaultModel.riding;
		armorModel.young = living.isBaby();
		return armorModel;
	}
	
}
