package cn.leolezury.eternalstarlight.common.world.gen.system.transformer.biome;

import cn.leolezury.eternalstarlight.common.registry.ESDataTransformerTypes;
import cn.leolezury.eternalstarlight.common.world.gen.system.WorldGenProvider;
import cn.leolezury.eternalstarlight.common.world.gen.system.transformer.DataTransformerType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;

import java.util.Random;

public class AssimilateBiomesTransformer extends NeighborsRelatedTransformer {
	public static final MapCodec<AssimilateBiomesTransformer> CODEC = MapCodec.assumeMapUnsafe(Codec.BOOL.fieldOf("only_lonely").xmap(AssimilateBiomesTransformer::new, transformer -> transformer.onlyLonely).codec());

	private final boolean onlyLonely;

	public AssimilateBiomesTransformer(boolean onlyLonely) {
		this.onlyLonely = onlyLonely;
	}

	@Override
	public int transform(WorldGenProvider provider, Random random, int original, int up, int down, int left, int right) {
		if (!onlyLonely || (original != up && original != down && original != left && original != right)) {
			if (up == left && up == right) {
				return up;
			}
			if (down == left && down == right) {
				return down;
			}
			if (left == up && left == down) {
				return left;
			}
			if (right == up && right == down) {
				return right;
			}
			if (up == left || up == right) {
				return up;
			}
			if (down == left || down == right) {
				return down;
			}
			if (up == down) {
				return up;
			}
			if (left == right) {
				return left;
			}
		}
		return onlyLonely ? original : chooseRandomly(random, up, down, left, right);
	}

	@Override
	public DataTransformerType<?> type() {
		return ESDataTransformerTypes.ASSIMILATE.get();
	}
}
