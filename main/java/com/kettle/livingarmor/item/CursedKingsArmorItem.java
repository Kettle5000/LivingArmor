
package com.kettle.livingarmor.item;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundEvents;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.kettle.livingarmor.client.renderer.ArmorClientExtensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;

public abstract class CursedKingsArmorItem extends ArmorItem {
 
	public CursedKingsArmorItem(ArmorItem.Type type, Item.Properties properties) {
		super(new ArmorMaterial() {
			@Override
			public int getDurabilityForType(ArmorItem.Type type) {
				return new int[]{16, 18, 19, 14}[type.getSlot().getIndex()] * 20;
			}

			@Override
			public int getDefenseForType(ArmorItem.Type type) {
				return new int[]{1, 4, 5, 2}[type.getSlot().getIndex()];
			}

			@Override
			public int getEnchantmentValue() {
				return 1;
			}

			@Override
			public SoundEvent getEquipSound() {
				return SoundEvents.EMPTY;
			}

			@Override
			public Ingredient getRepairIngredient() {
				return Ingredient.of(Items.NETHERITE_INGOT);
			}

			@Override
			public String getName() {
				return "cursed_kings_armor";
			}

			@Override
			public float getToughness() {
				return 1f;
			}

			@Override
			public float getKnockbackResistance() {
				return 0f;
			}
		}, type, properties);
	}

	public static class Helmet extends CursedKingsArmorItem {
		public Helmet() {
			super(ArmorItem.Type.HELMET, new Item.Properties());
		}
		
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(ArmorClientExtensions.extensions);
		}


		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "living_armor:textures/entities/cursed_king_helmet.png";
		}
		
		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		    super.appendHoverText(stack, world, tooltip, flag);
		    tooltip.add(Component.translatable("tooltip.living_armor.armorset.tooltip")
		                     .withStyle(ChatFormatting.DARK_PURPLE));
		}
	}

	public static class Chestplate extends CursedKingsArmorItem {
		public Chestplate() {
			super(ArmorItem.Type.CHESTPLATE, new Item.Properties());
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "living_armor:textures/models/armor/cursedkingsarmor_layer_1.png";
		}
		
		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		    super.appendHoverText(stack, world, tooltip, flag);
		    tooltip.add(Component.translatable("tooltip.living_armor.armorset.tooltip")
		                     .withStyle(ChatFormatting.DARK_PURPLE));
		}
	}

	public static class Leggings extends CursedKingsArmorItem {
		public Leggings() {
			super(ArmorItem.Type.LEGGINGS, new Item.Properties());
		}
		
		@Override
		public void initializeClient(Consumer<IClientItemExtensions> consumer) {
			consumer.accept(ArmorClientExtensions.extensions);
		}
		
		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		    super.appendHoverText(stack, world, tooltip, flag);
		    tooltip.add(Component.translatable("tooltip.living_armor.armorset.tooltip")
		                     .withStyle(ChatFormatting.DARK_PURPLE));
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "living_armor:textures/entities/cursed_king_leggings.png";
		}
	}

	public static class Boots extends CursedKingsArmorItem {
		public Boots() {
			super(ArmorItem.Type.BOOTS, new Item.Properties());
		}
		
		@Override
		public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag flag) {
		    super.appendHoverText(stack, world, tooltip, flag);
		    tooltip.add(Component.translatable("tooltip.living_armor.armorset.tooltip")
		                     .withStyle(ChatFormatting.DARK_PURPLE));
		}

		@Override
		public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
			return "living_armor:textures/models/armor/cursedkingsarmor_layer_1.png";
		}
	}
}
