package cn.leolezury.eternalstarlight.common.data;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.platform.ESPlatform;
import cn.leolezury.eternalstarlight.common.world.gen.system.biome.BiomeData;
import cn.leolezury.eternalstarlight.common.world.gen.system.transformer.DataTransformer;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public class ESRegistries {
    public static final ResourceKey<Registry<BiomeData>> BIOME_DATA = ResourceKey.createRegistryKey(new ResourceLocation(EternalStarlight.MOD_ID, "biome_data"));
    public static final ResourceKey<Registry<DataTransformer>> DATA_TRANSFORMER = ResourceKey.createRegistryKey(new ResourceLocation(EternalStarlight.MOD_ID, "worldgen_data_transformer"));

    static {
        ESPlatform.INSTANCE.registerDatapackRegistry(BIOME_DATA, BiomeData.CODEC, BiomeData.CODEC);
        ESPlatform.INSTANCE.registerDatapackRegistry(DATA_TRANSFORMER, DataTransformer.CODEC, null);
    }

    public static void loadClass() {}
}