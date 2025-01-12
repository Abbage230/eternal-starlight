package cn.leolezury.eternalstarlight.common.client.renderer.entity;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.client.model.entity.LuminoFishModel;
import cn.leolezury.eternalstarlight.common.client.renderer.layer.LuminoFishGlowLayer;
import cn.leolezury.eternalstarlight.common.entity.living.animal.LuminoFish;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

@Environment(EnvType.CLIENT)
public class LuminoFishRenderer<T extends LuminoFish> extends MobRenderer<T, LuminoFishModel<T>> {
	private static final ResourceLocation ENTITY_TEXTURE = EternalStarlight.id("textures/entity/luminofish.png");

	public LuminoFishRenderer(EntityRendererProvider.Context context) {
		super(context, new LuminoFishModel<>(context.bakeLayer(LuminoFishModel.LAYER_LOCATION)), 0.3f);
		this.addLayer(new LuminoFishGlowLayer<>(this));
	}

	@Override
	protected void setupRotations(T livingEntity, PoseStack poseStack, float f, float g, float h, float i) {
		super.setupRotations(livingEntity, poseStack, f, g, h, i);
		if (!livingEntity.isInWater()) {
			poseStack.translate(0.1F, 0.1F, -0.1F);
			poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
		}
	}

	@Override
	public ResourceLocation getTextureLocation(T entity) {
		return ENTITY_TEXTURE;
	}
}
