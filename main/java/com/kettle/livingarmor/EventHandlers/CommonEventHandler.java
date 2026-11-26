package com.kettle.livingarmor.EventHandlers;

import com.kettle.livingarmor.entity.CursedKingEntity;
import com.kettle.livingarmor.entity.LivingArmorEntity;
import com.kettle.livingarmor.init.LivingArmorModEnchantments;
import com.kettle.livingarmor.init.LivingArmorModEntities;
import com.kettle.livingarmor.init.LivingArmorModItems;
import com.kettle.livingarmor.item.CursedKingsArmorItem;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;

import com.kettle.livingarmor.Config;

@Mod.EventBusSubscriber
public class CommonEventHandler {
	
	@SubscribeEvent
	public static void DebuffInmunity(MobEffectEvent.Applicable event) {
		if (event.getEntity().getItemBySlot(EquipmentSlot.HEAD).getItem() == LivingArmorModItems.CURSED_KINGS_HELMET) {
			LivingEntity entity = event.getEntity();
			if (entity.getItemBySlot(EquipmentSlot.CHEST).getItem() == LivingArmorModItems.CURSED_KINGS_CHESTPLATE
					&& entity.getItemBySlot(EquipmentSlot.LEGS).getItem() == LivingArmorModItems.CURSED_KINGS_LEGGINGS
					&& entity.getItemBySlot(EquipmentSlot.FEET).getItem() == LivingArmorModItems.CURSED_KINGS_BOOTS) {
				MobEffect effect = event.getEffectInstance().getEffect();
				if ((!effect.isBeneficial() || Config.deny_effects.contains(effect))  && !Config.allowed_effects.contains(effect)) {
					event.setResult(Result.DENY);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void OnWearing(LivingEquipmentChangeEvent event) {
		if (event.getEntity() instanceof Player player) {
			int temp = 0;
			for (ItemStack stack: player.getArmorSlots()) {
				if (stack.getItem() instanceof CursedKingsArmorItem) {
					if (stack.getEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get()) == 0) {
						stack.enchant(LivingArmorModEnchantments.HAUNTED.get(), 1);
					}
					temp ++;
				}
			}
			if (temp == 4) {
				ArrayList<MobEffect> toremove = new ArrayList<>();
				for (MobEffectInstance effect : player.getActiveEffects()) {
					if ((!effect.getEffect().isBeneficial() || Config.deny_effects.contains(effect.getEffect())) && !Config.allowed_effects.contains(effect.getEffect())) {
						toremove.add(effect.getEffect());
					}
				}
				if (!toremove.isEmpty()) {
					for (MobEffect effect1 : toremove) {
						player.removeEffect(effect1);
					}
				}
			}
		}
	}
	
	
	@SuppressWarnings("deprecation")
	@SubscribeEvent
	public static void onEntityDeath(LivingDeathEvent event) {
		if (!event.isCanceled() && event.getEntity() != null && !(event.getEntity() instanceof LivingArmorEntity || event.getEntity() instanceof CursedKingEntity)) {
			LivingEntity entity = event.getEntity();
			LevelAccessor world = entity.level();
			ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
			if (EnchantmentHelper.getItemEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get(), helmet) != 0) {
				ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
				ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
				ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);
				if (EnchantmentHelper.getItemEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get(), chestplate) != 0
						&& EnchantmentHelper.getItemEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get(), leggings) != 0
						&& EnchantmentHelper.getItemEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get(), boots) != 0) {
					boolean flag = helmet.getItem() == LivingArmorModItems.CURSED_KINGS_HELMET
							&& chestplate.getItem() == LivingArmorModItems.CURSED_KINGS_CHESTPLATE
							&& leggings.getItem() == LivingArmorModItems.CURSED_KINGS_LEGGINGS
							&& boots.getItem() == LivingArmorModItems.CURSED_KINGS_BOOTS;
					if (world instanceof ServerLevel _level) {
						LivingEntity entityToSpawn;
						if (flag) {
							entityToSpawn = LivingArmorModEntities.CURSED_KING.get().spawn(_level, entity.blockPosition(), MobSpawnType.MOB_SUMMONED);
							entityToSpawn.invulnerableTime = 10;
							entityToSpawn.hurtTime = 10;
						} else {
							entityToSpawn = LivingArmorModEntities.LIVING_ARMOR.get().spawn(_level, entity.blockPosition(), MobSpawnType.MOB_SUMMONED);
							entityToSpawn.invulnerableTime = 10;
							entityToSpawn.hurtTime = 10;
						}
						if (entityToSpawn != null) {
							entityToSpawn.setYRot(world.getRandom().nextFloat() * 360F);
							entityToSpawn.setItemSlot(EquipmentSlot.HEAD, helmet);
							entityToSpawn.setItemSlot(EquipmentSlot.CHEST, chestplate);
							entityToSpawn.setItemSlot(EquipmentSlot.LEGS, leggings);
							entityToSpawn.setItemSlot(EquipmentSlot.FEET, boots);
							if (entity.getItemBySlot(EquipmentSlot.MAINHAND) != ItemStack.EMPTY && (entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof SwordItem
									|| entity.getItemBySlot(EquipmentSlot.MAINHAND).getItem() instanceof AxeItem)) {
								entityToSpawn.setItemSlot(EquipmentSlot.MAINHAND,
										entity.getItemBySlot(EquipmentSlot.MAINHAND));
								entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
							}
							entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
							entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
							entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
							entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
						}
					}
				}
			}
		}
	}

}
