package cn.leolezury.eternalstarlight.fabric.mixin.client;

import cn.leolezury.eternalstarlight.common.client.handler.ClientHandlers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(Gui.class)
public abstract class GuiMixin {
	@Inject(method = "renderCameraOverlays", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getTicksFrozen()I", shift = At.Shift.AFTER))
	private void renderCameraOverlays(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
		ClientHandlers.renderSpellCrosshair(guiGraphics, guiGraphics.guiWidth(), guiGraphics.guiHeight());
		ClientHandlers.renderEtherErosion(guiGraphics);
		ClientHandlers.renderOrbOfProphecyUse(guiGraphics);
		ClientHandlers.renderDreamCatcher(guiGraphics);
		ClientHandlers.renderCurrentCrest(guiGraphics);
		ClientHandlers.renderCarvedLunarisCactusFruitBlur(guiGraphics);
	}
}