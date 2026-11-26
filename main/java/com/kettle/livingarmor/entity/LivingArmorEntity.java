
package com.kettle.livingarmor.entity;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.common.DungeonHooks;

import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.protocol.Packet;
import net.minecraft.core.BlockPos;

import java.util.Random;

import com.kettle.livingarmor.Config;
import com.kettle.livingarmor.DefensiveMeleeAttackGoal;
import com.kettle.livingarmor.LivingArmorMoveControl;
import com.kettle.livingarmor.init.LivingArmorModEnchantments;
import com.kettle.livingarmor.init.LivingArmorModEntities;
import com.kettle.livingarmor.init.NearestFactionEnemyGoal;
import com.mojang.datafixers.util.Pair;


import java.util.List;
import java.util.ArrayList;

import net.minecraftforge.common.ForgeMod;

public class LivingArmorEntity extends Monster {
	private static final EntityDataAccessor<Boolean> DATA_IS_BLOCKING = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SHIELD_DISABLED = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_SHIELD_ACTION_TICKS = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SHIELD_COOLDOWN_TICKS = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TEAM = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SUMMON_INVUL = SynchedEntityData.defineId(LivingArmorEntity.class, EntityDataSerializers.INT);
	public LivingArmorEntity(PlayMessages.SpawnEntity packet, Level world) {
		this(LivingArmorModEntities.LIVING_ARMOR.get(), world);
	}

	public LivingArmorEntity(EntityType<LivingArmorEntity> type, Level world) {
	    super(type, world);
	    setMaxUpStep(0.6f);
	    xpReward = 10;
	    setNoAi(false);
	    moveControl = new LivingArmorMoveControl(this);
	    if (!world.isClientSide()) {
	    	if (!Config.allowedArmorSets.isEmpty()) {
		        int totalWeight = Config.allowedArmorSets.stream()
		            .mapToInt(Pair::getSecond)
		            .sum();

		        int randomWeight = this.getRandom().nextInt(totalWeight);
		        int currentWeight = 0;

		        for (Pair<List<ItemStack>, Integer> entry : Config.allowedArmorSets) {
		            currentWeight += entry.getSecond();
		            if (randomWeight < currentWeight) {
		                List<ItemStack> chosenSet = entry.getFirst();

		                // Ensure the set has the required size (armor + main hand + offhand)
		                if (chosenSet.size() == 6) {
		                    this.setItemSlot(EquipmentSlot.HEAD, chosenSet.get(0).copy());
		                    this.setItemSlot(EquipmentSlot.CHEST, chosenSet.get(1).copy());
		                    this.setItemSlot(EquipmentSlot.LEGS, chosenSet.get(2).copy());
		                    this.setItemSlot(EquipmentSlot.FEET, chosenSet.get(3).copy());
		                    this.setItemSlot(EquipmentSlot.MAINHAND, chosenSet.get(4).copy());
		                    this.setItemSlot(EquipmentSlot.OFFHAND, chosenSet.get(5).copy());
		                }
		                break;
		            }
		        }
		    }
        }
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new DefensiveMeleeAttackGoal(this, 1.2, false));
		this.targetSelector.addGoal(2, new NearestFactionEnemyGoal<>(this, LivingArmorEntity.class));
		this.targetSelector.addGoal(3, new NearestFactionEnemyGoal<>(this, CursedKingEntity.class));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Player.class, true));
		this.targetSelector.addGoal(5, new HurtByTargetGoal(this).setAlertOthers());
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(7, new FloatGoal(this));
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_BLOCKING, false);
		this.entityData.define(DATA_SHIELD_DISABLED, false);
		this.entityData.define(DATA_SHIELD_ACTION_TICKS, 0);
	    this.entityData.define(DATA_SHIELD_COOLDOWN_TICKS, 100);
	    this.entityData.define(DATA_TEAM, 0);
	    this.entityData.define(DATA_SUMMON_INVUL, 10);
	}
	
	public void setFaction(int team) {
    	this.entityData.set(DATA_TEAM, team);
    }
    
    public int getFaction() {
    	return this.entityData.get(DATA_TEAM);
    }
    
    public void setInvul() {
    	this.entityData.set(DATA_SUMMON_INVUL, 10);
    }
	
	@Override
	public MobType getMobType() {
		return MobType.UNDEFINED;
	}

	@Override
	public double getMyRidingOffset() {
		return -0.35D;
	}

	@Override
	public SoundEvent getAmbientSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.armor.equip_iron"));
	}

	@Override
	public void playStepSound(BlockPos pos, BlockState blockIn) {
		this.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("item.armor.equip_chain")), 0.15f, 1);
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.armor_stand.break"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("entity.generic.death"));
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.getDirectEntity() instanceof ThrownPotion || damagesource.getDirectEntity() instanceof AreaEffectCloud)
			amount *= 1.25f;
		if (damagesource.is(DamageTypes.DROWN))
			return false;
		if (damagesource.is(DamageTypes.WITHER))
			return false;
		if (damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		if (damagesource.is(DamageTypes.IN_WALL))
			return false;
		if (this.entityData.get(DATA_SUMMON_INVUL) > 0) {
			return false;
		}
		if (this.isDamageSourceBlocked(damagesource) && !this.entityData.get(DATA_SHIELD_DISABLED)) {
			if (damagesource.getEntity() instanceof Player player && player.getMainHandItem().canDisableShield(player.getMainHandItem(), this, player)) {
				this.entityData.set(DATA_SHIELD_DISABLED, true);
				this.playSound(SoundEvents.SHIELD_BREAK, 1.0F, 1.0F);
				this.stopUsingItem();
			} else {
				this.playSound(SoundEvents.SHIELD_BLOCK, 1.0F, 1.0F);
			}
			return false;
		}
		return super.hurt(damagesource, amount);
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean doHurtTarget(Entity p_32892_) {
		boolean flag = super.doHurtTarget(p_32892_);
		Random rand = new Random();
		if (flag && rand.nextFloat(1.0F) <= 0.2 && p_32892_ instanceof LivingEntity) {
			Iterable<ItemStack> slots = p_32892_.getArmorSlots();
			List<ItemStack> itemList = new ArrayList<>();
			for (ItemStack armor : slots) {
				if (!(EnchantmentHelper.getItemEnchantmentLevel(LivingArmorModEnchantments.HAUNTED.get(), armor) > 0)) {
					itemList.add(armor);
				}
			}
			if (!itemList.isEmpty()) {
				itemList.get(rand.nextInt(itemList.size())).enchant(LivingArmorModEnchantments.HAUNTED.get(), 1);
			} 
		}
		return flag;
	}

	@SuppressWarnings("deprecation")
	public static void init() {
		SpawnPlacements.register(LivingArmorModEntities.LIVING_ARMOR.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> (world.getDifficulty() != Difficulty.PEACEFUL && Monster.isDarkEnoughToSpawn(world, pos, random) && Mob.checkMobSpawnRules(entityType, world, reason, pos, random)));
		DungeonHooks.addDungeonMob(LivingArmorModEntities.LIVING_ARMOR.get(), 180);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.25);
		builder = builder.add(Attributes.MAX_HEALTH, 20);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 1);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 5);
		builder = builder.add(ForgeMod.ENTITY_REACH.get(), 4);
		return builder;
	}
	
	
	@Override
    public void tick() {
        super.tick();
        if (this.entityData.get(DATA_SUMMON_INVUL) > 0) {
        	this.entityData.set(DATA_SUMMON_INVUL, this.entityData.get(DATA_SUMMON_INVUL) - 1);
        }
        if (this.getOffhandItem().getItem() instanceof ShieldItem && this.getTarget() != null) {
        	if (this.getTarget() instanceof Player player) {
                if (isPlayerLookingAtMob(player, this, 20.0)) {
                	// Decrease cooldown if applicable
                    if (this.entityData.get(DATA_SHIELD_COOLDOWN_TICKS) > 0 && this.entityData.get(DATA_SHIELD_ACTION_TICKS) <= 0) {
                    	this.entityData.set(DATA_SHIELD_COOLDOWN_TICKS, this.entityData.get(DATA_SHIELD_COOLDOWN_TICKS) - 1);
                    	if (this.entityData.get(DATA_SHIELD_COOLDOWN_TICKS) <= 0 && this.getTarget() != null) {
                            startUsingShield();
                        }
                    }
                    if (this.entityData.get(DATA_SHIELD_ACTION_TICKS) > 0) {
                        // If the shield is in use, decrement its action timer
                    	this.entityData.set(DATA_SHIELD_ACTION_TICKS, this.entityData.get(DATA_SHIELD_ACTION_TICKS) - 1);
                        //this.getNavigation().stop(); // Stop movement during shield usage
                    	LivingEntity target = this.getTarget();
                    	double dx = target.getX() - this.getX();
        		        double dz = target.getZ() - this.getZ();

        		        // Calculate the desired yaw
        		        float targetYaw = (float) (Mth.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;

        		        // Update the mob's yaw to face the target
        		        this.setYRot(targetYaw);
        		        this.yBodyRot = targetYaw; // Sync body rotation with the yaw
        		        this.yHeadRot = targetYaw; // Optional: sync head rotation
        		        if (!this.entityData.get(DATA_SHIELD_DISABLED)) {
        		        	this.startUsingItem(InteractionHand.OFF_HAND);
        		        }
                        // If action timer ends, stop using the shield
                        if (this.entityData.get(DATA_SHIELD_ACTION_TICKS) <= 0) {
                            stopUsingShield();
                        }
                    } else {
                        // If not using the shield, ensure the item use is stopped
                        this.stopUsingItem();  
                    }
                }
            }
        }
    }

    private void startUsingShield() {
    	this.entityData.set(DATA_IS_BLOCKING, true);
    	this.entityData.set(DATA_SHIELD_ACTION_TICKS, this.getRandom().nextInt(40, 80)); // Duration for using the shield (2-4 seconds)
    	this.entityData.set(DATA_SHIELD_COOLDOWN_TICKS, 0);
    }

    private void stopUsingShield() {
    	this.entityData.set(DATA_IS_BLOCKING, false);
    	this.entityData.set(DATA_SHIELD_DISABLED, false);
    	this.entityData.set(DATA_SHIELD_ACTION_TICKS, 0);
    	this.entityData.set(DATA_SHIELD_COOLDOWN_TICKS, this.getRandom().nextInt(100, 160)); // Cooldown before the shield can be used again (5-8 seconds)
    }

    public boolean isUsingShield() {
        return this.entityData.get(DATA_IS_BLOCKING);
    }

    public void setUsingShield(boolean usingShield) {
    	this.entityData.set(DATA_IS_BLOCKING, usingShield);
    }
    
    private static boolean isPlayerLookingAtMob(Player player, LivingEntity mob, double maxDistance) {
        // Get the player's eye position and direction they're looking
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 viewVector = player.getViewVector(1.0F);

        // Get the vector pointing from the player's eye position to the mob's position
        Vec3 mobPosition = mob.position().add(0, mob.getBbHeight(), 0); // Target the middle of the mob
        Vec3 toMob = mobPosition.subtract(eyePosition);

        // Check if the mob is within max distance
        if (toMob.lengthSqr() > maxDistance * maxDistance) {
            return false;
        }

        // Normalize the vector and calculate the dot product
        toMob = toMob.normalize();
        double dotProduct = viewVector.dot(toMob);

        // Check if the mob is within the player's field of view
        return dotProduct > 0.5; // Adjust this threshold for a narrower/wider field of view
    }
}
