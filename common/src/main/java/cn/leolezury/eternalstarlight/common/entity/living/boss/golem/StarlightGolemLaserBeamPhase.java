package cn.leolezury.eternalstarlight.common.entity.living.boss.golem;

import cn.leolezury.eternalstarlight.common.entity.attack.ray.GolemLaserBeam;
import cn.leolezury.eternalstarlight.common.entity.living.phase.BehaviorPhase;
import cn.leolezury.eternalstarlight.common.registry.ESEntities;
import cn.leolezury.eternalstarlight.common.registry.ESSoundEvents;
import cn.leolezury.eternalstarlight.common.vfx.ScreenShakeVfx;
import net.minecraft.server.level.ServerLevel;

public class StarlightGolemLaserBeamPhase extends BehaviorPhase<StarlightGolem> {
	public static final int ID = 1;

	public StarlightGolemLaserBeamPhase() {
		super(ID, 2, 200, 400);
	}

	@Override
	public boolean canStart(StarlightGolem entity, boolean cooldownOver) {
		return cooldownOver && entity.getTarget() != null;
	}

	@Override
	public void onStart(StarlightGolem entity) {

	}

	@Override
	public void tick(StarlightGolem entity) {
		if (entity.getBehaviorTicks() == 60) {
			entity.playSound(ESSoundEvents.STARLIGHT_GOLEM_PREPARE_BEAM.get());
			GolemLaserBeam beam = new GolemLaserBeam(ESEntities.GOLEM_LASER_BEAM.get(), entity.level(), entity, entity.getX(), entity.getY() + entity.getBbHeight() / 2.5f, entity.getZ(), entity.yHeadRot + 90, -entity.getXRot());
			entity.level().addFreshEntity(beam);
		}
		if (entity.getBehaviorTicks() >= 60 && entity.getBehaviorTicks() % 40 == 0) {
			if (entity.level() instanceof ServerLevel serverLevel) {
				ScreenShakeVfx.createInstance(entity.level().dimension(), entity.position(), 40, 60, 0.3f, 0.3f, 4.5f, 5).send(serverLevel);
			}
			entity.spawnEnergizedFlame(1, 15, false);
		}
	}

	@Override
	public boolean canContinue(StarlightGolem entity) {
		return true;
	}

	@Override
	public void onStop(StarlightGolem entity) {

	}
}
