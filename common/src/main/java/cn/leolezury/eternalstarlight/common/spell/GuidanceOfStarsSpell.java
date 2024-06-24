package cn.leolezury.eternalstarlight.common.spell;

import cn.leolezury.eternalstarlight.common.data.ESDimensions;
import net.minecraft.world.entity.LivingEntity;

public class GuidanceOfStarsSpell extends AbstractSpell {
	public GuidanceOfStarsSpell(Properties properties) {
		super(properties);
	}

	@Override
	public boolean checkExtraConditions(LivingEntity entity) {
		return entity.level().dimension() == ESDimensions.STARLIGHT_KEY;
	}

	@Override
	public boolean checkExtraConditionsToContinue(LivingEntity entity, int ticks) {
		return entity.level().dimension() == ESDimensions.STARLIGHT_KEY;
	}

	@Override
	public void onPreparationTick(LivingEntity entity, int ticks) {

	}

	@Override
	public void onSpellTick(LivingEntity entity, int ticks) {
	}

	@Override
	public void onStart(LivingEntity entity) {
	}

	@Override
	public void onStop(LivingEntity entity, int ticks) {
	}
}
