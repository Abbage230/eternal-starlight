package cn.leolezury.eternalstarlight.common.client.particle.effect;

import cn.leolezury.eternalstarlight.common.client.handler.ClientHandlers;
import cn.leolezury.eternalstarlight.common.particle.ExplosionShockParticleOptions;
import cn.leolezury.eternalstarlight.common.util.Easing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

@Environment(EnvType.CLIENT)
public class ExplosionShockParticle extends TextureSheetParticle {
	private final Vec3 direction;
	private final float length;
	private final Vector3f fromColor, toColor;

	protected ExplosionShockParticle(ClientLevel clientLevel, double x, double y, double z, double dx, double dy, double dz, Vector3f fromColor, Vector3f toColor, SpriteSet spriteSet) {
		super(clientLevel, x, y, z, dx, dy, dz);
		this.friction = 0;
		this.lifetime = (int) (this.random.nextFloat() * 7 + 7);
		this.direction = new Vec3(dx, dy, dz).normalize();
		this.length = this.random.nextFloat() * 1.25f + 1.15f;
		this.fromColor = fromColor;
		this.toColor = toColor;
		this.pickSprite(spriteSet);
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public void render(VertexConsumer consumer, Camera camera, float partialTick) {
		Vec3 camPos = camera.getPosition();
		PoseStack stack = new PoseStack();
		stack.pushPose();
		stack.translate(-camPos.x, -camPos.y, -camPos.z);
		double currentX = Mth.lerp(partialTick, this.xo, this.x);
		double currentY = Mth.lerp(partialTick, this.yo, this.y);
		double currentZ = Mth.lerp(partialTick, this.zo, this.z);
		Vec3 sight = camPos.subtract(currentX, currentY, currentZ).scale(-1);
		Vec3 start = new Vec3(currentX, currentY, currentZ).add(direction.scale(Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, 0, length * 2f)));
		Vec3 end = start.add(direction.scale(Easing.IN_OUT_QUAD.interpolate(Mth.abs(Math.min(age + partialTick, lifetime) / lifetime - 0.5f) * 2, length / 2f, 0)));
		Vec3 offset = end.subtract(start);
		Vec3 sideOffset = offset.cross(sight).normalize().scale(0.03);
		PoseStack.Pose pose = stack.last();
		float u0 = this.getU0();
		float u1 = Easing.IN_OUT_QUAD.interpolate(Mth.abs(Math.min(age + partialTick, lifetime) / lifetime - 0.5f) * 2, this.getU1(), this.getU0());
		float v0 = this.getV0();
		float v1 = this.getV1();
		consumer.addVertex(pose, start.add(sideOffset).toVector3f()).setColor(Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.x(), toColor.x()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.y(), toColor.y()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.z(), toColor.z()) / 255, 1).setUv(u0, v0).setLight(ClientHandlers.FULL_BRIGHT);
		consumer.addVertex(pose, start.add(sideOffset.scale(-1)).toVector3f()).setColor(Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.x(), toColor.x()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.y(), toColor.y()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.z(), toColor.z()) / 255, 1).setUv(u0, v1).setLight(ClientHandlers.FULL_BRIGHT);
		consumer.addVertex(pose, end.add(sideOffset.scale(-1)).toVector3f()).setColor(Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.x(), toColor.x()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.y(), toColor.y()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.z(), toColor.z()) / 255, 1).setUv(u1, v1).setLight(ClientHandlers.FULL_BRIGHT);
		consumer.addVertex(pose, end.add(sideOffset).toVector3f()).setColor(Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.x(), toColor.x()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.y(), toColor.y()) / 255, Easing.IN_OUT_QUAD.interpolate(Math.min(age + partialTick, lifetime) / lifetime, fromColor.z(), toColor.z()) / 255, 1).setUv(u1, v0).setLight(ClientHandlers.FULL_BRIGHT);
		stack.popPose();
	}

	public static class Provider implements ParticleProvider<ExplosionShockParticleOptions> {
		private final SpriteSet sprites;

		public Provider(SpriteSet spriteSet) {
			this.sprites = spriteSet;
		}

		@Override
		public Particle createParticle(ExplosionShockParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
			return new ExplosionShockParticle(level, x, y, z, dx, dy, dz, options.fromColor(), options.toColor(), sprites);
		}
	}
}
