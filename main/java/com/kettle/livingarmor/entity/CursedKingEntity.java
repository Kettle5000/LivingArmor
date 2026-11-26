package com.kettle.livingarmor.entity;

import java.util.ArrayList;
import java.util.List;

import com.kettle.livingarmor.Config;
import com.kettle.livingarmor.CursedKingMoveControl;
import com.kettle.livingarmor.CursedMeleeAttackGoal;
import com.kettle.livingarmor.CustomFireBall;
import com.kettle.livingarmor.init.LivingArmorModEnchantments;
import com.kettle.livingarmor.init.LivingArmorModEntities;
import com.kettle.livingarmor.init.LivingArmorModItems;
import com.kettle.livingarmor.init.NearestFactionEnemyGoal;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.registries.ForgeRegistries;

public class CursedKingEntity extends Monster {
	private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IS_BLOCKING = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SHIELD_DISABLED = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Integer> DATA_SHIELD_ACTION_TICKS = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_SHIELD_COOLDOWN_TICKS = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.INT);
	private static final EntityDataAccessor<Integer> DATA_TEAM = SynchedEntityData.defineId(CursedKingEntity.class, EntityDataSerializers.INT);
	private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true);
	 
	public CursedKingEntity(PlayMessages.SpawnEntity packet, Level world) {
		this(LivingArmorModEntities.CURSED_KING.get(), world);
	}

	public CursedKingEntity(EntityType<CursedKingEntity> type, Level world) {
	    super(type, world);
	    setMaxUpStep(0.6f);
	    xpReward = 50;
	    setNoAi(false);
	    moveControl = new CursedKingMoveControl(this);
	    if (!world.isClientSide()) {
	    	setFaction(random.nextInt(0, 10));
	    	this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(LivingArmorModItems.CURSED_KINGS_HELMET));
		    this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(LivingArmorModItems.CURSED_KINGS_CHESTPLATE));
		    this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(LivingArmorModItems.CURSED_KINGS_LEGGINGS));
		    this.setItemSlot(EquipmentSlot.FEET, new ItemStack(LivingArmorModItems.CURSED_KINGS_BOOTS));
		    this.setItemSlot(EquipmentSlot.MAINHAND, Config.CursedMainHand);
		    this.setItemSlot(EquipmentSlot.OFFHAND, Config.CursedOffHand);
		    this.setDropChance(EquipmentSlot.HEAD, 0);
		    this.setDropChance(EquipmentSlot.CHEST, 0);
		    this.setDropChance(EquipmentSlot.LEGS, 0);
		    this.setDropChance(EquipmentSlot.FEET, 0);
		    this.setDropChance(EquipmentSlot.MAINHAND, 0);
		    this.setDropChance(EquipmentSlot.OFFHAND, 0);
        }
	}
	
	@Override
	public boolean removeWhenFarAway(double p_21542_) {
		return false;
	}

	@Override
	public Packet<ClientGamePacketListener> getAddEntityPacket() {
		return NetworkHooks.getEntitySpawningPacket(this);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new CursedMeleeAttackGoal(this, 1.2, false));
		this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
			@Override
			public boolean canContinueToUse() {
			      LivingEntity livingentity = this.mob.getTarget();
			      if (livingentity == null) {
			         livingentity = this.targetMob;
			      }

			      if (livingentity == null) {
			         return false;
			      } else if (!this.mob.canAttack(livingentity)) {
			         return false;
			      } else {
			         Team team = this.mob.getTeam();
			         Team team1 = livingentity.getTeam();
			         if (team != null && team1 == team) {
			            return false;
			         } else {
			            double d0 = this.getFollowDistance();
			            double distance = this.mob.distanceToSqr(livingentity);
			            boolean cansee = this.mob.getSensing().hasLineOfSight(livingentity);
			            if (distance <= d0 * d0 && cansee) {
			            	this.mob.setTarget(livingentity);
			               return true;
			            } else if (distance <= 4096) {
			            	this.mob.setTarget(livingentity);
			            	return true;
			            } else if (distance > 4096) {
			            	return false;
			            }
			         }
			         return true;
			      }
			   }
		});
		this.targetSelector.addGoal(3, new NearestFactionEnemyGoal<>(this, LivingArmorEntity.class));
		this.targetSelector.addGoal(4, new NearestFactionEnemyGoal<>(this, CursedKingEntity.class));
		this.targetSelector.addGoal(5, new HurtByTargetGoal(this).setAlertOthers());
		this.goalSelector.addGoal(6, new RandomLookAroundGoal(this));
		this.goalSelector.addGoal(7, new CursedKingEntity.ShootFireballGoal(this));
		this.goalSelector.addGoal(8, new CursedKingEntity.SummonLivingArmorGoal(this));
		this.goalSelector.addGoal(9, new FloatGoal(this));
	}
	
	public boolean isCharging() {
		  return this.entityData.get(DATA_IS_CHARGING);
	}

	public void setCharging(boolean p_32759_) {
		  this.entityData.set(DATA_IS_CHARGING, p_32759_);
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.entityData.define(DATA_IS_CHARGING, false);
		this.entityData.define(DATA_IS_BLOCKING, false);
		this.entityData.define(DATA_SHIELD_DISABLED, false);
		this.entityData.define(DATA_SHIELD_ACTION_TICKS, 0);
	    this.entityData.define(DATA_SHIELD_COOLDOWN_TICKS, 100);
	    this.entityData.define(DATA_TEAM, 0);
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
		return ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("living_armor:cursed_king_death"));
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.IN_FIRE))
			return false;
		if (damagesource.is(DamageTypes.DROWN))
			return false;
		if (damagesource.is(DamageTypes.WITHER))
			return false;
		if (damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		if (damagesource.is(DamageTypes.IN_WALL))
			return false;
		if (damagesource.is(DamageTypes.FELL_OUT_OF_WORLD))
			amount = this.getMaxHealth();
		if (this.isDamageSourceBlocked(damagesource) && !this.entityData.get(DATA_SHIELD_DISABLED)) {
			if (damagesource.getEntity() instanceof Player player && player.fallDistance > 0.0f && player.getMainHandItem().canDisableShield(player.getMainHandItem(), this, player)) {
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
	protected void actuallyHurt(DamageSource p_21240_, float p_21241_) {
		if (!this.isInvulnerableTo(p_21240_)) {
	         p_21241_ = net.minecraftforge.common.ForgeHooks.onLivingHurt(this, p_21240_, p_21241_);
	         if (p_21241_ <= 0) return;
	         p_21241_ = this.getDamageAfterArmorAbsorb(p_21240_, p_21241_);
	         p_21241_ = this.getDamageAfterMagicAbsorb(p_21240_, p_21241_);
	         float f1 = Math.max(p_21241_ - this.getAbsorptionAmount(), 0.0F);
	         this.setAbsorptionAmount(this.getAbsorptionAmount() - (p_21241_ - f1));
	         f1 = net.minecraftforge.common.ForgeHooks.onLivingDamage(this, p_21240_, f1);
	         f1 = Math.min(this.getMaxHealth() * 0.1f, f1);
	         if (p_21240_.getDirectEntity() instanceof ThrownPotion || p_21240_.getDirectEntity() instanceof AreaEffectCloud || p_21240_.is(DamageTypes.MAGIC) ) {
	        	 f1 = f1 * 1.25f;
	         }
	         if (f1 != 0.0F) {
	            this.getCombatTracker().recordDamage(p_21240_, f1);
	            this.setHealth(this.getHealth() - f1);
	            this.setAbsorptionAmount(this.getAbsorptionAmount() - f1);
	            this.gameEvent(GameEvent.ENTITY_DAMAGE);
	         }
	      }
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean doHurtTarget(Entity p_32892_) {
		boolean flag = super.doHurtTarget(p_32892_);
		RandomSource rand = this.getRandom();
		if (flag && p_32892_ instanceof LivingEntity) {
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
	
	@Override
	protected void tickDeath() {
	   ++this.deathTime;
	      if (this.deathTime >= 80 && !this.level().isClientSide() && !this.isRemoved()) {
	    	// Randomly choose one of the armor pieces to drop
	    	// Define all armor slots
	          ArrayList<EquipmentSlot> slots = new ArrayList<>(List.of(
	              EquipmentSlot.HEAD, 
	              EquipmentSlot.CHEST, 
	              EquipmentSlot.LEGS, 
	              EquipmentSlot.FEET
	          ));

	          // Randomize the number of drops (2 or 3)
	          int maxDrops = this.getRandom().nextIntBetweenInclusive(2, 3);

	          // Drop a random subset of the armor pieces
	          for (int i = 0; i < maxDrops; i++) {
	        	  int temp = this.random.nextInt(slots.size());
	        	  EquipmentSlot chosenSlot = slots.get(temp);
	        	  slots.remove(temp);
	              ItemStack droppedItem = this.getItemBySlot(chosenSlot);
	              
	              if (!droppedItem.isEmpty()) {
	                  // Spawn the selected armor piece as an ItemEntity
	                  ItemEntity itemEntity = new ItemEntity(
	                      this.level(),
	                      this.getX(),
	                      this.getY() + 1.0D,
	                      this.getZ(),
	                      droppedItem
	                  );

	                  itemEntity.setDefaultPickUpDelay();
	                  this.level().addFreshEntity(itemEntity);
	              }
	          }

	         super.tickDeath();
	      }
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 0.35);
		builder = builder.add(Attributes.MAX_HEALTH, 500);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 5);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 10);
		builder = builder.add(ForgeMod.ENTITY_REACH.get(), 4);
		return builder;
	}
	
	
	@Override
    public void tick() {
        super.tick();
        if (this.getOffhandItem().getItem() instanceof ShieldItem && this.getTarget() != null && !this.isDeadOrDying()) {
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
        		        } else {
        		        	this.entityData.set(DATA_SHIELD_ACTION_TICKS, 0);
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
            } else if (this.getTarget() == null) {
            	this.stopUsingItem();
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
    
    @Override
    public void startSeenByPlayer(ServerPlayer p_31483_) {
        super.startSeenByPlayer(p_31483_);
        this.bossEvent.addPlayer(p_31483_);
     }

    @Override
     public void stopSeenByPlayer(ServerPlayer p_31488_) {
        super.stopSeenByPlayer(p_31488_);
        this.bossEvent.removePlayer(p_31488_);
     }
    
    @Override
    protected void customServerAiStep() {
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
    }
    
    public void setFaction(int team) {
    	this.entityData.set(DATA_TEAM, team);
    }
    
    public int getFaction() {
    	return this.entityData.get(DATA_TEAM);
    }
    static class ShootFireballGoal extends Goal {
		private final CursedKingEntity king;
		public int chargeTime;

		public ShootFireballGoal(CursedKingEntity p_32776_) {
			this.king = p_32776_;
		}

		public boolean canUse() {
	        LivingEntity target = this.king.getTarget();
	        if (target == null || !target.isAlive()) {
	            return false;
	        }
	        return !this.king.getSensing().hasLineOfSight(target) || this.king.getHealth()/this.king.getMaxHealth() <= 0.25f;
	    }
		
	    @Override
	    public boolean canContinueToUse() {
	        // Continue if the target is still out of sight and alive
	        LivingEntity target = this.king.getTarget();
	        return target != null && target.isAlive() && (!this.king.getSensing().hasLineOfSight(target) || this.king.getHealth()/this.king.getMaxHealth() <= 0.25f);
	    }

		public void start() {
			this.chargeTime = 0;
		}

		public void stop() {
			this.king.setCharging(false);
		}

		public boolean requiresUpdateEveryTick() {
			return true;
		}

		public void tick() {
			LivingEntity livingentity = this.king.getTarget();
			if (livingentity != null) {
				if (livingentity.distanceToSqr(this.king) < 4096.0D) {
					Level level = this.king.level();
					++this.chargeTime;
					if (this.chargeTime == 1 && !this.king.isSilent()) {
						king.playSound(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("living_armor:charged_bomb")));
					}

					if (this.chargeTime == 50) {
						Vec3 vec3 = this.king.getViewVector(1.0F);
						double d2 = livingentity.getX() - (this.king.getX() + vec3.x * 1.0D);
						double d3 = livingentity.getY(0.5D) - (0.5D + this.king.getY(0.5D));
						double d4 = livingentity.getZ() - (this.king.getZ() + vec3.z * 1.0D);
		                CustomFireBall largefireball = new CustomFireBall(level, this.king, d2, d3, d4, 4);
		                largefireball.setPos(this.king.getX() + vec3.x * 2.0D, this.king.getY(0.5D) + 0.5D, largefireball.getZ() + vec3.z * 2.0D);
		                level.addFreshEntity(largefireball);
		                this.chargeTime = -40;
					}
				} else if (this.chargeTime > 0) {
					--this.chargeTime;
				}

				this.king.setCharging(this.chargeTime > 0);
			}
		}
	}
    
    static class SummonLivingArmorGoal extends Goal {
        private final CursedKingEntity cursedKing;
        private final Level level;
        private boolean[] summonedTiers;

        public SummonLivingArmorGoal(CursedKingEntity entity) {
            this.cursedKing = entity;
            this.level = entity.level();
            this.summonedTiers = new boolean[4]; // 4 tiers: 75%, 50%, 25%, 0%
        }

        @Override
        public boolean canUse() {
            if (cursedKing.isDeadOrDying()) return false;
            float healthPercentage = cursedKing.getHealth() / cursedKing.getMaxHealth();
            if (!cursedKing.level().isClientSide()) {
            	if (healthPercentage == 1f && !summonedTiers[0]) {
                    summonedTiers[0] = true;
                    summonLivingArmor(2);
                    return true;
                } else if (healthPercentage < 0.75f && healthPercentage >= 0.5f && !summonedTiers[1]) {
                    summonedTiers[1] = true;
                    summonLivingArmor(3);
                    return true;
                } else if (healthPercentage < 0.5f && healthPercentage >= 0.25f && !summonedTiers[2]) {
                    summonedTiers[2] = true;
                    summonLivingArmor(4);
                    return true;
                } else if (healthPercentage < 0.25f && !summonedTiers[3]) { // Below 25%
                    summonedTiers[3] = true;
                    summonLivingArmor(5);
                    return true;
                }
            }
            return false;
        }

        private void summonLivingArmor(int count) {
            for (int i = 0; i < count; i++) {
                LivingArmorEntity armor = new LivingArmorEntity(LivingArmorModEntities.LIVING_ARMOR.get(), level);
                armor.setFaction(cursedKing.getFaction());
                double x = cursedKing.getX() + (level.random.nextDouble() - 0.5) * 4.0;
                double y = cursedKing.getY();
                double z = cursedKing.getZ() + (level.random.nextDouble() - 0.5) * 4.0;
                armor.moveTo(x, y, z, level.random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(armor);
            }
        }
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