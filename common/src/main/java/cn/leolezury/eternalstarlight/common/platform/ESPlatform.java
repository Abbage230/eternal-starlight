package cn.leolezury.eternalstarlight.common.platform;

import cn.leolezury.eternalstarlight.common.block.fluid.EtherFluid;
import cn.leolezury.eternalstarlight.common.client.ESDimensionSpecialEffects;
import cn.leolezury.eternalstarlight.common.client.model.item.GlowingBakedModel;
import cn.leolezury.eternalstarlight.common.item.armor.AlchemistArmorItem;
import cn.leolezury.eternalstarlight.common.item.armor.ThermalSpringstoneArmorItem;
import cn.leolezury.eternalstarlight.common.item.weapon.CrescentSpearItem;
import cn.leolezury.eternalstarlight.common.item.weapon.HammerItem;
import cn.leolezury.eternalstarlight.common.item.weapon.ScytheItem;
import cn.leolezury.eternalstarlight.common.platform.registry.RegistrationProvider;
import cn.leolezury.eternalstarlight.common.resource.gatekeeper.TheGatekeeperNameManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import java.nio.file.Path;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public interface ESPlatform {
	ESPlatform INSTANCE = Util.make(() -> {
		final ServiceLoader<ESPlatform> loader = ServiceLoader.load(ESPlatform.class);
		final Iterator<ESPlatform> iterator = loader.iterator();
		if (!iterator.hasNext()) {
			throw new RuntimeException("Platform instance not found!");
		} else {
			final ESPlatform platform = iterator.next();
			if (iterator.hasNext()) {
				throw new RuntimeException("More than one platform instance was found!");
			}
			return platform;
		}
	});

	enum Loader {
		NEOFORGE,
		FABRIC
	}

	// some loader-related stuff
	Loader getLoader();

	boolean isPhysicalClient();

	Path getConfigDir();

	// registry utils
	<T> RegistrationProvider<T> createRegistrationProvider(ResourceKey<? extends Registry<T>> key, String namespace);

	<T> RegistrationProvider<T> createNewRegistryProvider(ResourceKey<? extends Registry<T>> key, String namespace);

	<T> void registerDatapackRegistry(ResourceKey<Registry<T>> key, Codec<T> codec, Codec<T> networkCodec);

	// for initialization
	// items
	default SpawnEggItem createSpawnEgg(Supplier<EntityType<? extends Mob>> defaultType, int backgroundColor, int highlightColor, Item.Properties properties) {
		return new SpawnEggItem(defaultType.get(), backgroundColor, highlightColor, properties);
	}

	default ScytheItem createScythe(Tier tier, boolean canTill, Item.Properties properties) {
		return new ScytheItem(tier, canTill, properties);
	}

	default HammerItem createHammer(Tier tier, Item.Properties properties) {
		return new HammerItem(tier, properties);
	}

	default CrescentSpearItem createCrescentSpear(Item.Properties properties) {
		return new CrescentSpearItem(properties);
	}

	ThermalSpringstoneArmorItem createThermalSpringStoneArmor(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties);

	AlchemistArmorItem createAlchemistArmor(Holder<ArmorMaterial> material, ArmorItem.Type type, Item.Properties properties);

	CreativeModeTab getESTab();

	// blocks
	default FlowerPotBlock createFlowerPot(Supplier<FlowerPotBlock> pot, Supplier<? extends Block> flower, BlockBehaviour.Properties properties) {
		return new FlowerPotBlock(flower.get(), properties);
	}

	default EtherFluid.Still createEtherFluid() {
		return new EtherFluid.Still();
	}

	default EtherFluid.Flowing createFlowingEtherFluid() {
		return new EtherFluid.Flowing();
	}

	// reload listeners
	default TheGatekeeperNameManager createGatekeeperNameManager() {
		return new TheGatekeeperNameManager();
	}

	// event-related
	default boolean postMobGriefingEvent(Level level, Entity entity) {
		return level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
	}

	default boolean postTravelToDimensionEvent(Entity entity, ResourceKey<Level> dimension) {
		return true;
	}

	// item stuff
	boolean isShears(ItemStack stack);

	boolean isShield(ItemStack stack);

	default boolean canScrape(ItemStack stack) {
		return stack.is(ItemTags.AXES);
	}

	Pair<Predicate<UseOnContext>, Consumer<UseOnContext>> getToolTillAction(UseOnContext context);

	// dispenser
	default boolean canBoatInFluid(Boat boat, FluidState state) {
		return state.is(FluidTags.WATER);
	}

	// client-side
	@Environment(EnvType.CLIENT)
	default DimensionSpecialEffects getDimEffect() {
		return new ESDimensionSpecialEffects(160.0F, false, DimensionSpecialEffects.SkyType.NONE, false, false);
	}

	@Environment(EnvType.CLIENT)
	default BakedModel getGlowingBakedModel(BakedModel origin) {
		return new GlowingBakedModel(origin);
	}

	@Environment(EnvType.CLIENT)
	default void renderBlock(BlockRenderDispatcher dispatcher, PoseStack stack, MultiBufferSource multiBufferSource, BlockAndTintGetter level, BlockState state, BlockPos pos, long seed) {
		dispatcher.getModelRenderer().tesselateBlock(level, dispatcher.getBlockModel(state), state, pos, stack, multiBufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(state)), false, RandomSource.create(), seed, OverlayTexture.NO_OVERLAY);
	}

	// networking
	void sendToClient(ServerPlayer player, CustomPacketPayload packet);

	default void sendToAllClients(ServerLevel level, CustomPacketPayload packet) {
		for (ServerPlayer player : level.players()) {
			sendToClient(player, packet);
		}
	}

	void sendToServer(CustomPacketPayload packet);
}
