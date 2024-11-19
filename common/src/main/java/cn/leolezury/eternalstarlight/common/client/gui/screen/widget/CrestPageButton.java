package cn.leolezury.eternalstarlight.common.client.gui.screen.widget;

import cn.leolezury.eternalstarlight.common.EternalStarlight;
import cn.leolezury.eternalstarlight.common.util.ESGuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class CrestPageButton extends Button {
	private static final ResourceLocation NEXT_PAGE_ENABLED = EternalStarlight.id("textures/gui/screen/crest_selection/next_page_enabled.png");
	private static final ResourceLocation PREVIOUS_PAGE_ENABLED = EternalStarlight.id("textures/gui/screen/crest_selection/previous_page_enabled.png");
	private static final ResourceLocation NEXT_PAGE = EternalStarlight.id("textures/gui/screen/crest_selection/next_page.png");
	private static final ResourceLocation PREVIOUS_PAGE = EternalStarlight.id("textures/gui/screen/crest_selection/previous_page.png");
	private final boolean nextPage;

	private int prevHoverProgress;
	private int hoverProgress;

	public CrestPageButton(int x, int y, int width, int height, boolean nextPage, Component component, OnPress onPress) {
		super(x, y, width, height, component, onPress, DEFAULT_NARRATION);
		this.nextPage = nextPage;
	}

	public void tick() {
		prevHoverProgress = hoverProgress;
		if (isHovered()) {
			if (hoverProgress < 5) {
				hoverProgress++;
			}
		} else {
			if (hoverProgress > 0) {
				hoverProgress--;
			}
		}
	}

	@Override
	protected void renderWidget(GuiGraphics guiGraphics, int i, int j, float f) {
		float partialTicks = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(Minecraft.getInstance().level != null && Minecraft.getInstance().level.tickRateManager().runsNormally());
		float progress = (Mth.lerp(partialTicks, prevHoverProgress, hoverProgress) / 40f) + 1f;
		float width = getWidth() * progress;
		float height = getHeight() * progress;
		ESGuiUtil.blit(guiGraphics, RenderType::guiTextured, this.active ? (this.nextPage ? NEXT_PAGE_ENABLED : PREVIOUS_PAGE_ENABLED) : (this.nextPage ? NEXT_PAGE : PREVIOUS_PAGE), (getX() - (width - getWidth()) / 2f), (getY() - (height - getHeight()) / 2f), width, height, width, height, ARGB.colorFromFloat(alpha, 1, 1, 1));
	}
}
