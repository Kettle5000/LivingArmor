package com.kettle.livingarmor.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.kettle.livingarmor.init.LivingArmorModEnchantments;
import com.kettle.livingarmor.init.LivingArmorModEntities;
import com.kettle.livingarmor.init.LivingArmorModItems;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorStand.class)
public class ArmorStandMixin {

	@Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        ArmorStand entity = (ArmorStand)(Object)this;
        Level world = entity.level();
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (helmet.getEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get()) != 0) {
            ItemStack chestplate = entity.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack leggings = entity.getItemBySlot(EquipmentSlot.LEGS);
            ItemStack boots = entity.getItemBySlot(EquipmentSlot.FEET);

            if (chestplate.getEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get()) != 0 &&
                leggings.getEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get()) != 0 &&
                boots.getEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get()) != 0) {

                boolean fullSet = helmet.getItem() == LivingArmorModItems.CURSED_KINGS_HELMET &&
                                  chestplate.getItem() == LivingArmorModItems.CURSED_KINGS_CHESTPLATE &&
                                  leggings.getItem() == LivingArmorModItems.CURSED_KINGS_LEGGINGS &&
                                  boots.getItem() == LivingArmorModItems.CURSED_KINGS_BOOTS;
                List<Player> area = world.getEntitiesOfClass(Player.class, AABB.ofSize(entity.position(), 8, 4, 8), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
                if (!area.isEmpty()) {
                    if (world instanceof ServerLevel serverLevel) {
                        LivingEntity entityToSpawn = fullSet
                            ? LivingArmorModEntities.CURSED_KING.get().spawn(serverLevel, entity.blockPosition(), MobSpawnType.MOB_SUMMONED)
                            : LivingArmorModEntities.LIVING_ARMOR.get().spawn(serverLevel, entity.blockPosition(), MobSpawnType.MOB_SUMMONED);

                        if (entityToSpawn != null) {
                            entityToSpawn.setYRot(world.getRandom().nextFloat() * 360F);
                            entityToSpawn.setItemSlot(EquipmentSlot.HEAD, helmet);
                            entityToSpawn.setItemSlot(EquipmentSlot.CHEST, chestplate);
                            entityToSpawn.setItemSlot(EquipmentSlot.LEGS, leggings);
                            entityToSpawn.setItemSlot(EquipmentSlot.FEET, boots);

                            ItemStack mainhand = entity.getItemBySlot(EquipmentSlot.MAINHAND);
                            if (!mainhand.isEmpty() && (mainhand.getItem() instanceof SwordItem || mainhand.getItem() instanceof AxeItem)) {
                                entityToSpawn.setItemSlot(EquipmentSlot.MAINHAND, mainhand);
                                entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                            }

                            entity.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
                            entity.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
                            entity.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
                            entity.setItemSlot(EquipmentSlot.FEET, ItemStack.EMPTY);
                            entity.discard();
                        }
                    }
                }
            }
        }
    }
	
	@Inject(method = "hurt", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/damagesource/DamageSource;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0,
			shift = At.Shift.BEFORE), cancellable = true)
	private void beforeBypassCheck(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		if (!source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.OUTSIDE_BORDER)) {
			ArmorStand self = (ArmorStand) (Object) this;

			// custom condition: check full armor set
			ItemStack helmet = self.getItemBySlot(EquipmentSlot.HEAD);
			if (helmet.getItem() == LivingArmorModItems.CURSED_KINGS_HELMET) {
				ItemStack chestplate = self.getItemBySlot(EquipmentSlot.CHEST);
				ItemStack leggings = self.getItemBySlot(EquipmentSlot.LEGS);
				ItemStack boots = self.getItemBySlot(EquipmentSlot.FEET);
				if (chestplate.getItem() == LivingArmorModItems.CURSED_KINGS_CHESTPLATE
						&& leggings.getItem() == LivingArmorModItems.CURSED_KINGS_LEGGINGS
						&& boots.getItem() == LivingArmorModItems.CURSED_KINGS_BOOTS) {
					cir.setReturnValue(false);
				}
			}
		}
	}

}
