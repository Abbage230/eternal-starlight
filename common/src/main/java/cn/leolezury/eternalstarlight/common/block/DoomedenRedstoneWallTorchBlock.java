package cn.leolezury.eternalstarlight.common.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class DoomedenRedstoneWallTorchBlock extends DoomedenRedstoneTorchBlock {
	public static final MapCodec<DoomedenRedstoneWallTorchBlock> CODEC = simpleCodec(DoomedenRedstoneWallTorchBlock::new);
	public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = RedstoneTorchBlock.LIT;

	public DoomedenRedstoneWallTorchBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, true));
	}

	@Override
	protected MapCodec<? extends BaseTorchBlock> codec() {
		return CODEC;
	}

	@Override
	public VoxelShape getShape(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, CollisionContext collisionContext) {
		return WallTorchBlock.getShape(blockState);
	}

	@Override
	public boolean canSurvive(BlockState blockState, LevelReader levelReader, BlockPos blockPos) {
		return WallTorchBlock.canSurvive(levelReader, blockPos, blockState.getValue(FACING));
	}

	@Override
	public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
		return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext blockPlaceContext) {
		BlockState blockState = Blocks.WALL_TORCH.getStateForPlacement(blockPlaceContext);
		return blockState == null ? null : this.defaultBlockState().setValue(FACING, blockState.getValue(FACING));
	}

	@Override
	public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
		if (blockState.getValue(LIT)) {
			Direction direction = blockState.getValue(FACING).getOpposite();
			double e = (double) blockPos.getX() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.27 * (double) direction.getStepX();
			double f = (double) blockPos.getY() + 0.7 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.22;
			double g = (double) blockPos.getZ() + 0.5 + (randomSource.nextDouble() - 0.5) * 0.2 + 0.27 * (double) direction.getStepZ();
			level.addParticle(DustParticleOptions.REDSTONE, e, f, g, 0.0, 0.0, 0.0);
		}
	}

	@Override
	public int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
		return blockState.getValue(LIT) && blockState.getValue(FACING) != direction ? 15 : 0;
	}

	@Override
	public BlockState rotate(BlockState blockState, Rotation rotation) {
		return blockState.setValue(FACING, rotation.rotate(blockState.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState blockState, Mirror mirror) {
		return blockState.rotate(mirror.getRotation(blockState.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, LIT);
	}
}
