package cn.leolezury.eternalstarlight.common.block;

import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.registry.ESBlocks;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public interface WeatheringGolemSteel {
	Supplier<ImmutableMap<Block, Block>> TO_OXIDIZED = Suppliers.memoize(() -> ImmutableMap.<Block, Block>builder()
		.put(ESBlocks.GOLEM_STEEL_BLOCK.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_BLOCK.get())
		.put(ESBlocks.GOLEM_STEEL_SLAB.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_SLAB.get())
		.put(ESBlocks.GOLEM_STEEL_STAIRS.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_STAIRS.get())
		.put(ESBlocks.GOLEM_STEEL_TILES.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_TILES.get())
		.put(ESBlocks.GOLEM_STEEL_TILE_SLAB.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_TILE_SLAB.get())
		.put(ESBlocks.GOLEM_STEEL_TILE_STAIRS.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_TILE_STAIRS.get())
		.put(ESBlocks.GOLEM_STEEL_GRATE.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_GRATE.get())
		.put(ESBlocks.GOLEM_STEEL_PILLAR.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_PILLAR.get())
		.put(ESBlocks.GOLEM_STEEL_BARS.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_BARS.get())
		.put(ESBlocks.CHISELED_GOLEM_STEEL_BLOCK.get(), ESBlocks.OXIDIZED_CHISELED_GOLEM_STEEL_BLOCK.get())
		.put(ESBlocks.GOLEM_STEEL_JET.get(), ESBlocks.OXIDIZED_GOLEM_STEEL_JET.get())
		.build());

	default InteractionResult use(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player) {
		Optional<Block> scraped = TO_OXIDIZED.get().entrySet().stream().filter(e -> e.getValue() == state.getBlock()).findFirst().map(Map.Entry::getKey);
		if (ESPlatform.INSTANCE.canScrape(stack) && scraped.isPresent()) {
			level.setBlockAndUpdate(pos, scraped.get().withPropertiesOf(state));
			player.playSound(SoundEvents.AXE_SCRAPE);
			stack.hurtAndBreak(1, player, player.getEquipmentSlotForItem(stack));
			return InteractionResult.SUCCESS;
		}
		return InteractionResult.TRY_WITH_EMPTY_HAND;
	}

	default boolean isOxidized() {
		if (this instanceof Block block) {
			return TO_OXIDIZED.get().containsValue(block);
		} else {
			return false;
		}
	}

	default Optional<BlockState> getOxidizedState(BlockState blockState) {
		if (TO_OXIDIZED.get().containsKey(blockState.getBlock())) {
			return Optional.ofNullable(TO_OXIDIZED.get().get(blockState.getBlock())).map((block) -> block.withPropertiesOf(blockState));
		}
		return Optional.empty();
	}

	default void changeOverTime(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		if (randomSource.nextFloat() < 0.05688889F) {
			this.getNextState(blockState, serverLevel, blockPos, randomSource).ifPresent((state) -> {
				serverLevel.setBlockAndUpdate(blockPos, state);
			});
		}
	}

	default Optional<BlockState> getNextState(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
		boolean oxidized = this.isOxidized();
		int lessAffected = 0;
		int moreAffected = 0;

		for (BlockPos pos : BlockPos.withinManhattan(blockPos, 4, 4, 4)) {
			int distManhattan = pos.distManhattan(blockPos);
			if (distManhattan > 4) {
				break;
			}

			if (!pos.equals(blockPos)) {
				Block block = serverLevel.getBlockState(pos).getBlock();
				if (block instanceof WeatheringGolemSteel weatheringGolemSteel) {
					boolean otherBlockOxidized = weatheringGolemSteel.isOxidized();
					if (!otherBlockOxidized && oxidized) {
						return Optional.empty();
					}
					if (otherBlockOxidized && !oxidized) {
						++moreAffected;
					} else {
						++lessAffected;
					}
				}
			}
		}

		float oxidizeFactor = (float) (moreAffected + 1) / (float) (moreAffected + lessAffected + 1);
		return randomSource.nextFloat() < oxidizeFactor * oxidizeFactor * 0.75f ? this.getOxidizedState(blockState) : Optional.empty();
	}
}
