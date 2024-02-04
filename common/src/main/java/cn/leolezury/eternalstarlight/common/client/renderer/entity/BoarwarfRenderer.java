package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.boarwarf.BoarwarfModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.boarwarf.BoarwarfBiomeLayer;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.boarwarf.profession.*;
import cn.leolezury.eternalstarlight.common.entity.npc.boarwarf.Boarwarf;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class BoarwarfRenderer<T extends Boarwarf> extends MobRenderer<T, BoarwarfModel<T>> {
    private static final ResourceLocation ENTITY_TEXTURE = new ResourceLocation(EternalStarlight.MOD_ID, "textures/entity/boarwarf/boarwarf.png");

    public BoarwarfRenderer(EntityRendererProvider.Context context) {
        super(context, new BoarwarfModel<>(context.bakeLayer(BoarwarfModel.LAYER_LOCATION)), 0.5f);
        this.addLayer(new BoarwarfBiomeLayer<>(this, context.getModelSet()));
        this.addLayer(new BoarwarfBlacksmithLayer<>(this, context.getModelSet()));
        this.addLayer(new BoarwarfChefLayer<>(this, context.getModelSet()));
        this.addLayer(new BoarwarfDruidLayer<>(this, context.getModelSet()));
        this.addLayer(new BoarwarfDyerLayer<>(this, context.getModelSet()));
        this.addLayer(new BoarwarfSilversmithLayer<>(this, context.getModelSet()));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        return ENTITY_TEXTURE;
    }
}
