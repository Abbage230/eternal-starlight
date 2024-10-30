package cn.leolezury.eternalstarlight.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class PolishedToxiteBlock extends Block {
	public static final MapCodec<PolishedToxiteBlock> CODEC = simpleCodec(PolishedToxiteBlock::new);

	public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

	public PolishedToxiteBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(getStateDefinition().any().setValue(PART, Part.FULL));
	}

	@Override
	protected MapCodec<PolishedToxiteBlock> codec() {
		return CODEC;
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		boolean above = level.getBlockState(pos.above()).is(this);
		boolean below = level.getBlockState(pos.below()).is(this);
		if ((!above && !below)) {
			return state.setValue(PART, Part.FULL);
		} else if (above && below) {
			return state.setValue(PART, Part.MIDDLE);
		} else if (above) {
			return state.setValue(PART, Part.LOWER);
		} else {
			return state.setValue(PART, Part.UPPER);
		}
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(PART);
	}

	public enum Part implements StringRepresentable {
		FULL("full"),
		UPPER("upper"),
		MIDDLE("middle"),
		LOWER("lower");

		private final String name;

		Part(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
