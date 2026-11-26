
package com.kettle.livingarmor.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;

import com.kettle.livingarmor.LivingArmorMod;
import com.kettle.livingarmor.entity.CursedKingEntity;
import com.kettle.livingarmor.entity.LivingArmorEntity;

import net.minecraft.world.entity.Entity;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LivingArmorModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, LivingArmorMod.MODID);
	public static final RegistryObject<EntityType<LivingArmorEntity>> LIVING_ARMOR = register("living_armor", EntityType.Builder.<LivingArmorEntity>of(LivingArmorEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(LivingArmorEntity::new).fireImmune().sized(0.6f, 1.8f));
	public static final RegistryObject<EntityType<CursedKingEntity>> CURSED_KING = register("cursed_king", EntityType.Builder.<CursedKingEntity>of(CursedKingEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true)
			.setTrackingRange(64).setUpdateInterval(3).setCustomClientFactory(CursedKingEntity::new).fireImmune().sized(0.6f, 1.8f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> RegistryObject<EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(FMLCommonSetupEvent event) {
		event.enqueueWork(() -> {
			LivingArmorEntity.init();
		});
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(LIVING_ARMOR.get(), LivingArmorEntity.createAttributes().build());
		event.put(CURSED_KING.get(), CursedKingEntity.createAttributes().build());
	}
}
