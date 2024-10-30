package cn.leolezury.eternalstarlight.common.entity.living.boss.gatekeeper;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.config.ESConfig;
import cn.leolezury.eternalstarlight.common.data.ESCrests;
import cn.leolezury.eternalstarlight.common.data.ESLootTables;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESBoss;
import cn.leolezury.eternalstarlight.common.entity.living.boss.ESServerBossEvent;
import cn.leolezury.eternalstarlight.common.entity.living.goal.GatekeeperTargetGoal;
import cn.leolezury.eternalstarlight.common.entity.living.goal.LookAtTargetGoal;
import cn.leolezury.eternalstarlight.common.entity.living.phase.BehaviorManager;
import cn.leolezury.eternalstarlight.common.handler.CommonHandlers;
import cn.leolezury.eternalstarlight.common.item.component.ResourceKeyComponent;
import cn.leolezury.eternalstarlight.common.network.OpenGatekeeperGuiPacket;
import cn.leolezury.eternalstarlight.common.network.ParticlePacket;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.*;
import cn.leolezury.eternalstarlight.common.util.ESBookUtil;
import cn.leolezury.eternalstarlight.common.util.ESCrestUtil;
import cn.leolezury.eternalstarlight.common.util.ESMathUtil;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class TheGatekeeper extends ESBoss implements Npc, Merchant {
	private static final String TAG_GATEKEEPER_NAME = "gatekeeper_name";
	private static final String TAG_FIGHT_TARGET = "flight_target";
	private static final String TAG_FIGHT_PLAYER_ONLY = "fight_player_only";
	private static final String TAG_RESTOCK_COOLDOWN = "restock_cooldown";

	public TheGatekeeper(EntityType<? extends ESBoss> entityType, Level level) {
		super(entityType, level);
	}

	private final ESServerBossEvent bossEvent = new ESServerBossEvent(this, getUUID(), BossEvent.BossBarColor.PURPLE, false);

	protected static final EntityDataAccessor<Float> FIXED_Y_ROT = SynchedEntityData.defineId(TheGatekeeper.class, EntityDataSerializers.FLOAT);

	public float getFixedYRot() {
		return this.getEntityData().get(FIXED_Y_ROT);
	}

	public void setFixedYRot(float attackYRot) {
		this.getEntityData().set(FIXED_Y_ROT, attackYRot);
	}

	private final BehaviorManager<TheGatekeeper> behaviorManager = new BehaviorManager<>(this, List.of(
		new GatekeeperMeleePhase(),
		new GatekeeperDodgePhase(),
		new GatekeeperDashPhase(),
		new GatekeeperCastFireballPhase(),
		new GatekeeperDanceFightPhase(),
		new GatekeeperSwingSwordPhase(),
		new GatekeeperComboPhase()
	));

	public AnimationState idleAnimationState = new AnimationState();
	public AnimationState meleeAnimationStateA = new AnimationState();
	public AnimationState meleeAnimationStateB = new AnimationState();
	public AnimationState meleeAnimationStateC = new AnimationState();
	public AnimationState dodgeAnimationState = new AnimationState();
	public AnimationState dashAnimationState = new AnimationState();
	public AnimationState castFireballAnimationState = new AnimationState();
	public AnimationState danceFightAnimationState = new AnimationState();
	public AnimationState swingSwordAnimationState = new AnimationState();
	public AnimationState comboAnimationState = new AnimationState();
	private String gatekeeperName = "TheGatekeeper";
	@Nullable
	private Player customer;
	@Nullable
	protected MerchantOffers offers;
	private int restockCooldown;
	@Nullable
	private ServerPlayer conversationTarget;
	@Nullable
	private String fightTarget;
	private boolean fightPlayerOnly = true;

	public void setFightPlayerOnly(boolean fightPlayerOnly) {
		this.fightPlayerOnly = fightPlayerOnly;
	}

	public void setFightTargetName(String fightTarget) {
		this.fightTarget = fightTarget;
	}

	public Optional<? extends Player> getFightTarget() {
		return level().players().stream().filter(p -> p.getName().getString().equals(fightTarget)).findFirst();
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(FIXED_Y_ROT, 0f);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compoundTag) {
		super.readAdditionalSaveData(compoundTag);
		gatekeeperName = compoundTag.getString(TAG_GATEKEEPER_NAME);
		fightTarget = compoundTag.getString(TAG_FIGHT_TARGET);
		fightPlayerOnly = compoundTag.getBoolean(TAG_FIGHT_PLAYER_ONLY);
		restockCooldown = compoundTag.getInt(TAG_RESTOCK_COOLDOWN);
		bossEvent.setId(getUUID());
		if (this.offers == null) {
			this.offers = new MerchantOffers();
			this.addTrades();
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compoundTag) {
		super.addAdditionalSaveData(compoundTag);
		compoundTag.putString(TAG_GATEKEEPER_NAME, gatekeeperName);
		if (fightTarget != null) {
			compoundTag.putString(TAG_FIGHT_TARGET, fightTarget);
		}
		compoundTag.putBoolean(TAG_FIGHT_PLAYER_ONLY, fightPlayerOnly);
		compoundTag.putInt(TAG_RESTOCK_COOLDOWN, restockCooldown);
	}

	@Override
	public void startSeenByPlayer(ServerPlayer serverPlayer) {
		super.startSeenByPlayer(serverPlayer);
		bossEvent.addPlayer(serverPlayer);
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer serverPlayer) {
		super.stopSeenByPlayer(serverPlayer);
		bossEvent.removePlayer(serverPlayer);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		goalSelector.addGoal(0, new FloatGoal(this));
		goalSelector.addGoal(1, new MeleeAttackGoal(this, 0.5D, false) {
			@Override
			protected void checkAndPerformAttack(LivingEntity livingEntity) {

			}
		});
		goalSelector.addGoal(2, new LookAtTargetGoal(this));
		goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
		goalSelector.addGoal(4, new LookAtPlayerGoal(this, Mob.class, 8.0F));

		targetSelector.addGoal(0, new GatekeeperTargetGoal(this) {
			@Override
			public boolean canUse() {
				return super.canUse() && fightPlayerOnly && isActivated();
			}
		});
		targetSelector.addGoal(1, new HurtByTargetGoal(this, TheGatekeeper.class) {
			@Override
			public boolean canUse() {
				return super.canUse() && !fightPlayerOnly && isActivated();
			}
		}.setAlertOthers());
		targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true) {
			@Override
			public boolean canUse() {
				return super.canUse() && !fightPlayerOnly && isActivated();
			}
		});
	}

	@Override
	protected BodyRotationControl createBodyControl() {
		return new BodyRotationControl(this) {
			@Override
			public void clientTick() {
				if (TheGatekeeper.this.getBehaviorState() == GatekeeperDashPhase.ID) {
					TheGatekeeper.this.setYBodyRot(TheGatekeeper.this.getFixedYRot());
					TheGatekeeper.this.setYHeadRot(TheGatekeeper.this.getFixedYRot());
				} else {
					super.clientTick();
				}
			}
		};
	}

	public static AttributeSupplier.Builder createAttributes() {
		return Monster.createMonsterAttributes()
			.add(Attributes.MAX_HEALTH, ESConfig.INSTANCE.mobsConfig.theGatekeeper.maxHealth())
			.add(Attributes.ARMOR, ESConfig.INSTANCE.mobsConfig.theGatekeeper.armor())
			.add(Attributes.ATTACK_DAMAGE, ESConfig.INSTANCE.mobsConfig.theGatekeeper.attackDamage())
			.add(Attributes.FOLLOW_RANGE, ESConfig.INSTANCE.mobsConfig.theGatekeeper.followRange())
			.add(Attributes.MOVEMENT_SPEED, 0.5F)
			.add(Attributes.ARMOR_TOUGHNESS, 5.0D)
			.add(Attributes.KNOCKBACK_RESISTANCE, 0.8D);
	}

	@Nullable
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor serverLevelAccessor, DifficultyInstance difficultyInstance, MobSpawnType mobSpawnType, @Nullable SpawnGroupData spawnGroupData) {
		spawnGroupData = super.finalizeSpawn(serverLevelAccessor, difficultyInstance, mobSpawnType, spawnGroupData);
		RandomSource randomSource = serverLevelAccessor.getRandom();
		this.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		return spawnGroupData;
	}

	@Override
	protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficultyInstance) {
		super.populateDefaultEquipmentSlots(randomSource, difficultyInstance);
		this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ESItems.SHATTERED_SWORD.get()));
	}

	public void stopAllAnimStates() {
		meleeAnimationStateA.stop();
		meleeAnimationStateB.stop();
		meleeAnimationStateC.stop();
		dodgeAnimationState.stop();
		dashAnimationState.stop();
		castFireballAnimationState.stop();
		danceFightAnimationState.stop();
		swingSwordAnimationState.stop();
		comboAnimationState.stop();
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
		if (accessor.equals(BEHAVIOR_STATE) && getBehaviorState() != 0) {
			stopAllAnimStates();
			switch (getBehaviorState()) {
				case GatekeeperMeleePhase.ID -> {
					switch (getRandom().nextInt(3)) {
						case 0 -> meleeAnimationStateA.start(tickCount);
						case 1 -> meleeAnimationStateB.start(tickCount);
						case 2 -> meleeAnimationStateC.start(tickCount);
					}
				}
				case GatekeeperDodgePhase.ID -> dodgeAnimationState.start(tickCount);
				case GatekeeperDashPhase.ID -> dashAnimationState.start(tickCount);
				case GatekeeperCastFireballPhase.ID -> castFireballAnimationState.start(tickCount);
				case GatekeeperDanceFightPhase.ID -> danceFightAnimationState.start(tickCount);
				case GatekeeperSwingSwordPhase.ID -> swingSwordAnimationState.start(tickCount);
				case GatekeeperComboPhase.ID -> comboAnimationState.start(tickCount);
			}
		}
		super.onSyncedDataUpdated(accessor);
	}

	@Override
	public boolean isActivated() {
		return super.isActivated() || !fightPlayerOnly;
	}

	@Override
	public boolean canAttack(LivingEntity livingEntity) {
		return super.canAttack(livingEntity) && isActivated();
	}

	@Override
	public void setDeltaMovement(Vec3 vec3) {
		boolean cantMove = getBehaviorState() == GatekeeperDanceFightPhase.ID || getBehaviorState() == GatekeeperSwingSwordPhase.ID;
		super.setDeltaMovement(cantMove ? new Vec3(0, vec3.y, 0) : vec3);
	}

	@Override
	public void initializeBoss() {
		super.initializeBoss();
		setActivated(false);
		gatekeeperName = CommonHandlers.getGatekeeperName();
	}

	@Override
	public boolean shouldPlayBossMusic() {
		return super.shouldPlayBossMusic() && isActivated();
	}

	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand interactionHand) {
		if (!level().isClientSide && fightPlayerOnly && player instanceof ServerPlayer serverPlayer && serverPlayer.getServer() != null && conversationTarget == null && getTradingPlayer() == null && getTarget() == null && getFightTarget().isEmpty()) {
			AdvancementHolder killDragon = serverPlayer.getServer().getAdvancements().get(ResourceLocation.withDefaultNamespace("end/kill_dragon"));
			boolean killed = killDragon != null && serverPlayer.getAdvancements().getOrStartProgress(killDragon).isDone();
			boolean challenged = isPlayerPermitted(serverPlayer);
			boolean hasOrb = false;
			for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
				if (serverPlayer.getInventory().getItem(i).is(ESItems.ORB_OF_PROPHECY.get())) hasOrb = true;
			}
			if (!challenged || !hasOrb) {
				conversationTarget = serverPlayer;
				ESPlatform.INSTANCE.sendToClient(serverPlayer, new OpenGatekeeperGuiPacket(getId(), killed, challenged));
			} else if (!getOffers().isEmpty()) {
				this.setTradingPlayer(serverPlayer);
				this.openTradingScreen(serverPlayer, this.getDisplayName(), 1);
			}
			return InteractionResult.sidedSuccess(this.level().isClientSide);
		}
		return InteractionResult.CONSUME;
	}

	public synchronized void handleDialogueClose(int operation) {
		if (conversationTarget != null) {
			if (operation == 1) {
				fightPlayerOnly = true;
				setFightTargetName(conversationTarget.getName().getString());
				setActivated(true);
				setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ESItems.SHATTERED_SWORD.get()));
			}
			if (operation == 2) {
				fightPlayerOnly = true;
				setFightTargetName("");
				setActivated(false);
				ItemStack stack = ESItems.ORB_OF_PROPHECY.get().getDefaultInstance();
				permitPlayer(conversationTarget);
				ESCrestUtil.upgradeCrest(conversationTarget, ESCrests.GUIDANCE_OF_STARS);
				if (!conversationTarget.getInventory().add(stack)) {
					ItemEntity entity = new ItemEntity(level(), conversationTarget.getX(), conversationTarget.getY(), conversationTarget.getZ(), stack);
					level().addFreshEntity(entity);
				}
				boolean hasBook = false;
				for (int i = 0; i < conversationTarget.getInventory().getContainerSize(); i++) {
					ItemStack inventoryItem = conversationTarget.getInventory().getItem(i);
					if (inventoryItem.is(ESItems.BOOK.get())) hasBook = true;
					if (inventoryItem.is(ESItems.LOOT_BAG.get())) {
						ResourceKeyComponent<LootTable> component = inventoryItem.get(ESDataComponents.LOOT_TABLE.get());
						if (component != null && component.resourceKey().location().equals(ESLootTables.BOSS_THE_GATEKEEPER.location())) {
							hasBook = true;
						}
					}
					;
				}
				if (!hasBook) {
					ItemStack book = ESItems.BOOK.get().getDefaultInstance();
					if (!conversationTarget.getInventory().add(book)) {
						ItemEntity entity = new ItemEntity(level(), conversationTarget.getX(), conversationTarget.getY(), conversationTarget.getZ(), book);
						level().addFreshEntity(entity);
					}
				}
			}
			conversationTarget = null;
		}
	}

	private void permitPlayer(ServerPlayer player) {
		ESCriteriaTriggers.CHALLENGED_GATEKEEPER.get().trigger(player);
		ESBookUtil.unlockFor(player, EternalStarlight.id("permitted_by_gatekeeper"));
	}

	private boolean isPlayerPermitted(ServerPlayer player) {
		if (player.getServer() != null) {
			AdvancementHolder challenge = player.getServer().getAdvancements().get(EternalStarlight.id("challenge_gatekeeper"));
			boolean challenged = challenge != null && player.getAdvancements().getOrStartProgress(challenge).isDone();
			if (challenged) {
				return true;
			}
		}
		return ESBookUtil.getUnlockedPartsFor(player).contains(EternalStarlight.id("permitted_by_gatekeeper"));
	}

	public void spawnMeleeAttackParticles() {
		if (level() instanceof ServerLevel serverLevel) {
			float lookYaw = getYHeadRot() + 90.0f;
			float lookPitch = -getXRot();
			Vec3 initialEndPos = ESMathUtil.rotationToPosition(getEyePosition(), 1f, lookPitch, lookYaw);
			for (int i = 0; i < 15; i++) {
				Vec3 endPos = initialEndPos.offsetRandom(getRandom(), 0.8f);
				ESPlatform.INSTANCE.sendToAllClients(serverLevel, new ParticlePacket(ESParticles.BLADE_SHOCKWAVE.get(), getEyePosition().x, getEyePosition().y, getEyePosition().z, endPos.x - getEyePosition().x, endPos.y - getEyePosition().y, endPos.z - getEyePosition().z));
			}
		}
	}

	@Override
	public void die(DamageSource source) {
		if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
			super.die(source);
		} else {
			getFightTarget().ifPresent(p -> {
				if (p instanceof ServerPlayer serverPlayer) {
					permitPlayer(serverPlayer);
				}
			});
			if (!level().isClientSide) {
				for (Player player : level().players()) {
					if (player instanceof ServerPlayer serverPlayer) {
						if (fightParticipants.stream().anyMatch(s -> s.equals(player.getName().getString())) && player.isAlive()) {
							permitPlayer(serverPlayer);
						}
					}
				}
			}
			setHealth(getMaxHealth());
			fightPlayerOnly = true;
			setFightTargetName("");
			setActivated(false);
			tryTeleportBack();
			if (level() instanceof ServerLevel serverLevel) {
				dropCustomDeathLoot(serverLevel, source, true);
			}
		}
	}

	@Override
	public boolean hurt(DamageSource source, float amount) {
		if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && (source.getEntity() == null || getTarget() == null || (source.getEntity().getUUID() != getTarget().getUUID() && !(source.getEntity() instanceof Player)))) {
			return false;
		}
		return super.hurt(source, amount);
	}

	private void tryTeleportBack() {
		Vec3 initialPos = getInitialPos();
		BlockPos blockPos = BlockPos.containing(initialPos);
		if (initialPos.distanceTo(position()) > 15) {
			Stream<VoxelShape> shapes = StreamSupport.stream(level().getBlockCollisions(this, getBoundingBox().move(initialPos.subtract(position()))).spliterator(), false);
			if (shapes.allMatch(VoxelShape::isEmpty) && level().getBlockState(blockPos.below()).isFaceSturdy(level(), blockPos.below(), Direction.UP)) {
				setPos(initialPos);
				return;
			}
			for (int i = 0; i < 16; i++) {
				if (teleportTowards(initialPos) && initialPos.distanceTo(position()) <= 15) {
					break;
				}
			}
		}
	}

	private boolean teleportTowards(Vec3 target) {
		Vec3 vec3 = new Vec3(this.getX() - target.x(), this.getY(0.5) - target.y(), this.getZ() - target.z()).normalize();
		double x = this.getX() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.x * 16.0;
		double y = this.getY() + (double) (this.random.nextInt(16) - 8) - vec3.y * 16.0;
		double z = this.getZ() + (this.random.nextDouble() - 0.5) * 8.0 - vec3.z * 16.0;
		return this.teleport(target, x, y, z);
	}

	private boolean teleport(Vec3 target, double x, double y, double z) {
		BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos(x, y, z);

		while (mutableBlockPos.getY() > this.level().getMinY() && !this.level().getBlockState(mutableBlockPos).blocksMotion()) {
			mutableBlockPos.move(Direction.DOWN);
		}

		BlockState blockState = this.level().getBlockState(mutableBlockPos);
		if (blockState.blocksMotion()) {
			return randomTeleportGatekeeper(target, x, y, z);
		}
		return false;
	}

	private boolean randomTeleportGatekeeper(Vec3 target, double x, double y, double z) {
		double oldX = this.getX();
		double oldY = this.getY();
		double oldZ = this.getZ();
		double currentY = y;
		boolean success = false;
		BlockPos blockPos = BlockPos.containing(x, currentY, z);
		Level level = this.level();

		if (level.hasChunkAt(blockPos)) {
			boolean blocksMotion = false;
			while (!blocksMotion && blockPos.getY() > level.getMinY()) {
				BlockPos blockPos2 = blockPos.below();
				BlockState blockState = level.getBlockState(blockPos2);
				if (blockState.blocksMotion()) {
					blocksMotion = true;
				} else {
					--currentY;
					blockPos = blockPos2;
				}
			}
			if (blocksMotion && new Vec3(x, currentY, z).distanceTo(target) <= 15) {
				this.teleportTo(x, currentY, z);
				success = level.noCollision(this) && !level.containsAnyLiquid(this.getBoundingBox());
			}
		}

		if (!success) {
			this.teleportTo(oldX, oldY, oldZ);
		}

		return success;
	}

	@Override
	public void aiStep() {
		super.aiStep();
		bossEvent.update();
		if (tickCount % 10 == 0 && !isActivated()) {
			bossEvent.allConvertToUnseen();
		}
		if (!level().isClientSide) {
			if (tickCount % 20 == 0 && (getTarget() == null || !getTarget().isAlive())) {
				setHealth(getMaxHealth());
				fightPlayerOnly = true;
				setFightTargetName("");
				setActivated(false);
				tryTeleportBack();
			}
			if (restockCooldown > 0) {
				restockCooldown--;
			} else {
				restockCooldown = 12000;
				restockAll();
			}
			if (conversationTarget != null && conversationTarget.distanceTo(this) > 20) {
				conversationTarget = null;
			}
			if (getTradingPlayer() != null && getTradingPlayer().distanceTo(this) > 16) {
				setTradingPlayer(null);
			}
			setCustomName(Component.literal(gatekeeperName));
			if (isLeftHanded()) {
				setLeftHanded(false);
			}
			if (getTarget() != null && !getTarget().isAlive()) {
				setTarget(null);
			}
			if (isActivated() && !isNoAi() && isAlive()) {
				behaviorManager.tick();
			}
		} else {
			idleAnimationState.startIfStopped(tickCount);
			level().addParticle(ESParticles.STARLIGHT.get(), getX() + (getRandom().nextDouble() - 0.5) * 2, getY() + 1 + (getRandom().nextDouble() - 0.5) * 2, getZ() + (getRandom().nextDouble() - 0.5) * 2, 0, 0, 0);
		}
	}

	@Override
	public SoundEvent getBossMusic() {
		return ESSoundEvents.MUSIC_BOSS_GATEKEEPER.get();
	}

	@Override
	protected SoundEvent getHurtSound(DamageSource damageSource) {
		return SoundEvents.PLAYER_HURT;
	}

	@Override
	protected SoundEvent getDeathSound() {
		return SoundEvents.PLAYER_DEATH;
	}

	@Override
	public void setTradingPlayer(@Nullable Player player) {
		this.customer = player;
	}

	@Nullable
	@Override
	public Player getTradingPlayer() {
		return this.customer;
	}

	@Override
	public MerchantOffers getOffers() {
		if (this.offers == null) {
			this.offers = new MerchantOffers();
			this.addTrades();
		}

		return this.offers;
	}

	protected void addTrades() {
		MerchantOffers merchantoffers = this.getOffers();
		this.addTrades(merchantoffers, GatekeeperTrades.TRADES, 50);
	}

	protected void addTrades(MerchantOffers original, VillagerTrades.ItemListing[] newTrades, int maxNumbers) {
		Set<Integer> set = new HashSet<>();
		if (newTrades.length > maxNumbers) {
			while (set.size() < maxNumbers) {
				set.add(this.random.nextInt(newTrades.length));
			}
		} else {
			for (int i = 0; i < newTrades.length; ++i) {
				set.add(i);
			}
		}

		for (Integer integer : set) {
			VillagerTrades.ItemListing trade = newTrades[integer];
			MerchantOffer merchantoffer = trade.getOffer(this, this.random);
			if (merchantoffer != null) {
				original.add(merchantoffer);
			}
		}
	}

	protected void restockAll() {
		for (MerchantOffer merchantoffer : this.getOffers()) {
			merchantoffer.resetUses();
		}
	}

	@Override
	public void overrideOffers(@Nullable MerchantOffers offers) {

	}

	@Override
	public void notifyTrade(MerchantOffer offer) {
		offer.increaseUses();
		this.ambientSoundTime = -this.getAmbientSoundInterval();
		if (offer.shouldRewardExp()) {
			int i = 3 + this.random.nextInt(4);
			this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5D, this.getZ(), i));
		}
	}

	@Override
	public void notifyTradeUpdated(ItemStack stack) {

	}

	@Override
	public int getVillagerXp() {
		return 0;
	}

	@Override
	public void overrideXp(int xp) {

	}

	@Override
	public boolean showProgressBar() {
		return false;
	}

	@Override
	public SoundEvent getNotifyTradeSound() {
		return null;
	}

	@Override
	public boolean isClientSide() {
		return this.level().isClientSide;
	}
}
