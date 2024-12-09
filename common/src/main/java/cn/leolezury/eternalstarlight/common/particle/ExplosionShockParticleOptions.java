package cn.leolezury.eternalstarlight.common.particle;

import cn.leolezury.eternalstarlight.common.registry.ESParticles;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public record ExplosionShockParticleOptions(Vector3f fromColor, Vector3f toColor) implements ParticleOptions {
	public static final ExplosionShockParticleOptions AETHERSENT = new ExplosionShockParticleOptions(new Vector3f(255, 255, 255), new Vector3f(233, 173, 237));
	public static final ExplosionShockParticleOptions CRESCENT_SPEAR = new ExplosionShockParticleOptions(new Vector3f(161, 223, 255), new Vector3f(124, 164, 213));

	public static final MapCodec<ExplosionShockParticleOptions> CODEC = RecordCodecBuilder.mapCodec((instance) -> instance.group(
		ExtraCodecs.VECTOR3F.fieldOf("from_color").forGetter(ExplosionShockParticleOptions::fromColor),
		ExtraCodecs.VECTOR3F.fieldOf("to_color").forGetter(ExplosionShockParticleOptions::toColor)
	).apply(instance, ExplosionShockParticleOptions::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ExplosionShockParticleOptions> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

	@Override
	public ParticleType<ExplosionShockParticleOptions> getType() {
		return ESParticles.EXPLOSION_SHOCK.get();
	}
}
