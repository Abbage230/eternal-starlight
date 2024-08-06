package cn.leolezury.eternalstarlight.common.weather;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.ClientWeatherState;
import cn.leolezury.eternalstarlight.common.client.renderer.world.ESWeatherRenderer;
import cn.leolezury.eternalstarlight.common.client.shader.ESShaders;
import cn.leolezury.eternalstarlight.common.entity.projectile.AethersentMeteor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

public class MeteorRainWeather extends AbstractWeather {
	private static final ResourceLocation RAIN_LOCATION = EternalStarlight.id("textures/environment/meteor_rain.png");

	public MeteorRainWeather(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canStart(ServerLevel level) {
		return true;
	}

	@Override
	public boolean canContinue(ServerLevel level, int ticks) {
		return true;
	}

	@Override
	public void serverWeatherTick(ServerLevel level, int ticks) {

	}

	@Override
	public void tickBlock(ServerLevel level, int ticks, BlockPos pos) {
		if (level.getRandom().nextInt(3000) == 0) {
			int targetX = pos.getX();
			int targetY = pos.getY();
			int targetZ = pos.getZ();
			RandomSource random = level.getRandom();
			AethersentMeteor meteor = new AethersentMeteor(level, null, targetX + (random.nextFloat() - 0.5) * 3, targetY + 200 + (random.nextFloat() - 0.5) * 5, targetZ + (random.nextFloat() - 0.5) * 3);
			meteor.setSize(10);
			meteor.setTargetPos(new Vec3(targetX, targetY, targetZ));
			meteor.setOnlyHurtEnemy(false);
			level.addFreshEntity(meteor);
			level.sendParticles(ParticleTypes.EXPLOSION, meteor.getX(), meteor.getY(), meteor.getZ(), 2, 0.2D, 0.2D, 0.2D, 0.0D);
		}
	}

	@Override
	public void onStart(ServerLevel level) {

	}

	@Override
	public void onStop(ServerLevel level, int ticks) {

	}

	@Environment(EnvType.CLIENT)
	@Override
	public boolean renderWeather(ClientLevel level, int ticks, float partialTick, LightTexture lightTexture, double camX, double camY, double camZ) {
		float rainTicks = Math.min(ClientWeatherState.ticks + partialTick, ClientWeatherState.duration);
		float rainLevel = (ClientWeatherState.duration / 2f - Math.abs(ClientWeatherState.duration / 2f - rainTicks)) / (ClientWeatherState.duration / 2f);
		ESWeatherRenderer.renderWeather(ESShaders.getMeteorRain(), lightTexture, Biome.Precipitation.RAIN, RAIN_LOCATION, RAIN_LOCATION, rainLevel, ticks, true, partialTick, camX, camY, camZ);
		return true;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public float modifyRainLevel(float original) {
		float partialTick = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(Minecraft.getInstance().level != null && Minecraft.getInstance().level.tickRateManager().runsNormally());
		float rainTicks = Math.min(ClientWeatherState.ticks + partialTick, ClientWeatherState.duration);
		ClientWeatherState.levelTarget = (ClientWeatherState.duration / 2f - Math.abs(ClientWeatherState.duration / 2f - rainTicks)) / (ClientWeatherState.duration / 2f);
		return ClientWeatherState.getRainLevel(partialTick);
	}
}
