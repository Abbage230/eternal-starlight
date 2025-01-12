package cn.leolezury.eternalstarlight.common.data;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.registry.ESItems;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;

import java.util.List;
import java.util.Map;

public class ESTrimMaterials {
	public static final ResourceKey<TrimMaterial> RED_STARLIGHT_CRYSTAL = create("red_starlight_crystal");
	public static final ResourceKey<TrimMaterial> BLUE_STARLIGHT_CRYSTAL = create("blue_starlight_crystal");
	public static final ResourceKey<TrimMaterial> ATALPHAITE = create("atalphaite");
	public static final ResourceKey<TrimMaterial> THIOQUARTZ = create("thioquartz");
	public static final ResourceKey<TrimMaterial> AETHERSENT = create("aethersent");
	public static final ResourceKey<TrimMaterial> THERMAL_SPRINGSTONE = create("thermal_springstone");
	public static final ResourceKey<TrimMaterial> GLACITE = create("glacite");
	public static final ResourceKey<TrimMaterial> SWAMP_SILVER = create("swamp_silver");
	public static final ResourceKey<TrimMaterial> AMARAMBER = create("amaramber");
	public static final ResourceKey<TrimMaterial> GOLEM_STEEL = create("golem_steel");
	public static final ResourceKey<TrimMaterial> MOONRING = create("moonring");
	public static final ResourceKey<TrimMaterial> DOOMEDEN_BONE = create("doomeden_bone");

	public static final List<ResourceKey<TrimMaterial>> TRIM_MATERIALS = List.of(
		RED_STARLIGHT_CRYSTAL,
		BLUE_STARLIGHT_CRYSTAL,
		ATALPHAITE,
		THIOQUARTZ,
		AETHERSENT,
		THERMAL_SPRINGSTONE,
		GLACITE,
		SWAMP_SILVER,
		AMARAMBER,
		GOLEM_STEEL,
		MOONRING,
		DOOMEDEN_BONE
	);

	public static void bootstrap(BootstrapContext<TrimMaterial> context) {
		register(context, RED_STARLIGHT_CRYSTAL, ESItems.RED_STARLIGHT_CRYSTAL_SHARD.asHolder(), 0xb63070, 1.0f);
		register(context, BLUE_STARLIGHT_CRYSTAL, ESItems.BLUE_STARLIGHT_CRYSTAL_SHARD.asHolder(), 0x308fb6, 0.8f);
		register(context, ATALPHAITE, ESItems.ATALPHAITE.asHolder(), 0xe0683e, 0.5f);
		register(context, THIOQUARTZ, ESItems.THIOQUARTZ_SHARD.asHolder(), 0xd7e3a8, 0.7f);
		register(context, AETHERSENT, ESItems.AETHERSENT_INGOT.asHolder(), 0x905ea8, 1.0f);
		register(context, THERMAL_SPRINGSTONE, ESItems.THERMAL_SPRINGSTONE_INGOT.asHolder(), 0xfdbd77, 0.5f);
		register(context, GLACITE, ESItems.GLACITE_SHARD.asHolder(), 0xcafeff, 0.8f);
		register(context, SWAMP_SILVER, ESItems.SWAMP_SILVER_INGOT.asHolder(), 0x8797b8, 0.2f);
		register(context, AMARAMBER, ESItems.AMARAMBER_INGOT.asHolder(), 0xc3647e, 1.0f);
		register(context, GOLEM_STEEL, ESItems.GOLEM_STEEL_INGOT.asHolder(), 0x397aaa, 0.2f);
		register(context, MOONRING, ESItems.TENACIOUS_PETAL.asHolder(), 0xa36d9e, 0.8f);
		register(context, DOOMEDEN_BONE, ESItems.BROKEN_DOOMEDEN_BONE.asHolder(), 0x978182, 0.1f);
	}

	private static void register(BootstrapContext<TrimMaterial> context, ResourceKey<TrimMaterial> key, Holder<Item> trimItem, int color, float itemModelIndex) {
		TrimMaterial material = new TrimMaterial(key.location().getPath(), trimItem, itemModelIndex, Map.of(), Component.translatable(Util.makeDescriptionId("trim_material", key.location())).withStyle(Style.EMPTY.withColor(color)));
		context.register(key, material);
	}

	private static ResourceKey<TrimMaterial> create(String name) {
		return ResourceKey.create(Registries.TRIM_MATERIAL, EternalStarlight.id(name));
	}
}
