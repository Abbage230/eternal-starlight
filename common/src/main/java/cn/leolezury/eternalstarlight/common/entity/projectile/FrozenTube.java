package cn.leolezury.eternalstarlight.common.entity.projectile;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.ESRenderType;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.data.ESDamageTypes;
import cn.leolezury.eternalstarlight.common.entity.attack.EnergizedFlame;
import cn.leolezury.eternalstarlight.common.entity.interfaces.TrailOwner;
import cn.leolezury.eternalstarlight.common.entity.living.monster.Freeze;
import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import cn.leolezury.eternalstarlight.common.util.TrailEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

public class FrozenTube extends AbstractArrow implements TrailOwner {
	private static final String TAG_DEALT_DAMAGE = "dealt_damage";

	private static final ResourceLocation TRAIL_TEXTURE = EternalStarlight.id("textures/entity/concentrated_trail.png");
	private boolean dealtDamage;

	public FrozenTube(EntityType<? extends FrozenTube> entityType, Level level) {
		super(entityType, level);
	}

	public FrozenTube(Level level, LivingEntity livingEntity, @Nullable ItemStack itemStack2) {
		super(ESEntities.FROZEN_TUBE.get(), livingEntity, level, new ItemStack(ESItems.FROZEN_TUBE.get()), itemStack2);
	}

	public void tick() {
		if (this.inGroundTime > 4) {
			this.dealtDamage = true;
		}
		super.tick();
	}

	@Nullable
	protected EntityHitResult findHitEntity(Vec3 vec3, Vec3 vec32) {
		return this.dealtDamage ? null : super.findHitEntity(vec3, vec32);
	}

	@Override
	protected void onHit(HitResult hitResult) {
		super.onHit(hitResult);
		if (hitResult.getType() != HitResult.Type.MISS) {
			playSound(SoundEvents.GLASS_BREAK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
			if (level() instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, ESItems.FROZEN_TUBE.get().getDefaultInstance()), this.getX() + (this.random.nextFloat() - 0.5) * getBbWidth(), this.getY() + random.nextFloat() * getBbHeight(), this.getZ() + (this.random.nextFloat() - 0.5) * getBbWidth(), 5, 0.2, 0.2, 0.2, 0.0);
			}
			for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3))) {
				if (!level().isClientSide && (getOwner() == null || !getOwner().getUUID().equals(entity.getUUID()))) {
					if (entity.canFreeze()) {
						entity.hurt(getOwner() instanceof LivingEntity owner ? ESDamageTypes.getIndirectEntityDamageSource(level(), DamageTypes.FREEZE, this, owner) : level().damageSources().freeze(), getOwner() instanceof Freeze ? (float) ESConfig.INSTANCE.mobsConfig.freeze.attackDamage() : 5);
						entity.setTicksFrozen(entity.getTicksFrozen() + 100);
					} else if (getOwner() instanceof LivingEntity owner) {
						entity.hurt(damageSources().mobProjectile(this, owner), 2.5f);
					}
				}
			}
			for (int x = -4; x <= 4; x++) {
				for (int y = -4; y <= 4; y++) {
					for (int z = -4; z <= 4; z++) {
						BlockPos pos = blockPosition().offset(x, y, z);
						if (level().getBlockState(pos).is(Blocks.FIRE) && blockPosition().distSqr(pos) <= 4 * 4) {
							level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
						}
						if (level().getBlockState(pos).is(Blocks.WATER) && blockPosition().distSqr(pos) <= 4 * 4) {
							level().setBlockAndUpdate(pos, Blocks.ICE.defaultBlockState());
						}
						if (level().getBlockState(pos).is(Blocks.LAVA) && blockPosition().distSqr(pos) <= 2 * 2) {
							level().setBlockAndUpdate(pos, Blocks.MAGMA_BLOCK.defaultBlockState());
						}
					}
				}
			}
			level().getEntitiesOfClass(EnergizedFlame.class, getBoundingBox().inflate(5)).forEach(Entity::discard);
			discard();
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult entityHitResult) {

	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		this.dealtDamage = compoundTag.getBoolean(TAG_DEALT_DAMAGE);
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return ESItems.FROZEN_TUBE.get().getDefaultInstance();
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putBoolean(TAG_DEALT_DAMAGE, this.dealtDamage);
	}

	@Override
	protected float getWaterInertia() {
		return 0.99F;
	}

	@Override
	public boolean shouldRender(double d, double e, double f) {
		return true;
	}

	@Override
	public TrailEffect newTrail() {
		return new TrailEffect(0.3f, 15);
	}

	@Override
	public void updateTrail(TrailEffect effect) {
		Vec3 oldPos = new Vec3(xOld, yOld, zOld);
		effect.update(oldPos.add(0, getBbHeight() / 2, 0), position().subtract(oldPos));
		if (isRemoved()) {
			effect.setLength(Math.max(effect.getLength() - 0.9f, 0));
		}
	}

	@Override
	public Vector4f getTrailColor() {
		return new Vector4f(160 / 255f, 164 / 255f, 195 / 255f, 1f);
	}

	@Environment(EnvType.CLIENT)
	@Override
	public RenderType getTrailRenderType() {
		return ESRenderType.entityTranslucentNoDepth(TRAIL_TEXTURE);
	}
}