package net.paradise_client.inject.mixin.gui;

import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.Panorama;
import net.paradise_client.themes.ThemeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Panorama.class) public class RotatingCubeMapRendererMixin {
  @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
  public void render(GuiGraphicsExtractor context, int width, int height, CallbackInfo ci) {
    // Calls dynamic rendering based on the defined theme
    ThemeManager.renderBackground(context, width, height);
    ci.cancel();
  }
}