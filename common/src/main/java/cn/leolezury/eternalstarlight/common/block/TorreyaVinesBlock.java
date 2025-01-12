package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.GrowingPlantHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class TorreyaVinesBlock extends GrowingPlantHeadBlock {
	public static final MapCodec<TorreyaVinesBlock> CODEC = simpleCodec(TorreyaVinesBlock::new);
	public static final VoxelShape SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 16.0D, 10.0D);

	public TorreyaVinesBlock(Properties properties) {
		super(properties, Direction.DOWN, SHAPE, false, 0.1D);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected MapCodec<TorreyaVinesBlock> codec() {
		return CODEC;
	}

	@Override
	protected int getBlocksToGrowWhenBonemealed(RandomSource randomSource) {
		return 1;
	}

	@Override
	protected boolean canGrowInto(BlockState state) {
		return state.isAir();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos attachPos = pos.relative(this.growthDirection.getOpposite());
		BlockState attachState = level.getBlockState(attachPos);
		if (!this.canAttachTo(attachState)) {
			return false;
		} else {
			return attachState.is(this.getHeadBlock()) || attachState.is(this.getBodyBlock()) || attachState.is(BlockTags.LEAVES) || attachState.isFaceSturdy(level, attachPos, this.growthDirection);
		}
	}

	@Override
	public BlockState updateShape(BlockState blockState, Direction direction, BlockState blockState2, LevelAccessor levelAccessor, BlockPos blockPos, BlockPos blockPos2) {
		BlockState state = levelAccessor.getBlockState(blockPos.above());
		if (state.is(ESBlocks.TORREYA_VINES_PLANT.get()) && !levelAccessor.getBlockState(blockPos.above(2)).is(ESBlocks.TORREYA_VINES_PLANT.get()) && !state.getValue(TorreyaVinesPlantBlock.TOP)) {
			state = state.setValue(TorreyaVinesPlantBlock.TOP, true);
			levelAccessor.setBlock(blockPos.above(), state, 2);
		}
		return super.updateShape(blockState, direction, blockState2, levelAccessor, blockPos, blockPos2);
	}

	@Override
	protected Block getBodyBlock() {
		return ESBlocks.TORREYA_VINES_PLANT.get();
	}

	@Override
	public ItemStack getCloneItemStack(LevelReader levelReader, BlockPos pos, BlockState state) {
		return new ItemStack(ESItems.TORREYA_VINES.get());
	}
}
