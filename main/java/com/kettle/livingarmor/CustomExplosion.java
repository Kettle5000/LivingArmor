package com.kettle.livingarmor;

import java.util.List;

import com.google.common.base.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class CustomExplosion {
	
	public static void explodeCircle(ServerLevel level, Entity owner, BlockPos pos, double range, float damage,double blockHardness,Predicate<Entity> predicate) {
        explodeCircle(level,owner,pos,range,damage, ParticleTypes.EXPLOSION_EMITTER,true,blockHardness,predicate);
    }

    public static void explodeCircle(ServerLevel level, Entity owner, BlockPos pos, double range, float damage, ParticleOptions particleTypes, boolean dropItems,double blockHardness,Predicate<Entity> predicate){
        float hard = 0;
    	for(int i = 0; i <= 2*range; ++i) {
            for(int j = 0; j <= 2*range; ++j) {
                for(int k = 0; k <= 2*range; ++k) {
                    double distance = Mth.sqrt((float) ((i-range)*(i-range) + (j-range)*(j-range) + (k-range)*(k-range)));
                    if (Math.abs(i) != 2 || Math.abs(j) != 2 || Math.abs(k) != 2) {
                        if (distance<range+(0.5)){
                            BlockPos blockpos = pos.offset( i-(int)range,j-(int)range,k-(int)range);
                            RandomSource source = RandomSource.create();
                            BlockState state = level.getBlockState(blockpos);
                            if (state.isAir()) {
                            	continue;
                            }
                            hard = state.getDestroySpeed(level, blockpos);
                            if (hard <= blockHardness && hard >= 0 && source.nextIntBetweenInclusive(0, 100) >= hard){
                            	Block.dropResources(state, level, blockpos, level.getBlockEntity(blockpos), owner, ItemStack.EMPTY);
                                level.removeBlock(blockpos, dropItems);
                                if (Math.random() < 0.1){
                                    float value = source.nextFloat() * 0.05f;
                                    level.sendParticles(particleTypes,blockpos.getX(),blockpos.getY(),blockpos.getZ(),1,value,0,value,1);
                                }
                            }
                          }}}}}
        AABB searchbox = AABB.ofSize(new Vec3(pos.getX(), pos.getY(), pos.getZ()), range*2, range*2, range*2);
        List<Entity> entities = level.getEntities(owner,searchbox, predicate);
        DamageSource explosionDamage = new DamageSource(level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.EXPLOSION), owner, null);
        for (Entity entity : entities){
            entity.hurt(explosionDamage ,damage);
            // Apply knockback
            Vec3 knockbackDirection = Vec3.atCenterOf(pos).subtract(Vec3.atCenterOf(entity.blockPosition())).normalize();
            double knockbackStrength = range * 0.5; // Adjust strength proportional to explosion range
            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.knockback(knockbackStrength, knockbackDirection.x, knockbackDirection.z);
            }
        }
        level.playSound(null,pos, SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS);
    }

}
