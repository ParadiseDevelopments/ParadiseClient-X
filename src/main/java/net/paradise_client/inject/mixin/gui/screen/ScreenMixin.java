package net.paradise_client.inject.mixin.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin for the Screen class to customize background rendering. This mixin replaces the default background for specific
 * screens with a custom texture.
 *
 * @author SpigotRCE
 * @since 1.9
 */
@Mixin(Screen.class) public abstract class ScreenMixin {
  @Shadow protected Minecraft minecraft;

  /**
   * Injects custom background rendering into the renderBackground method. This method draws a custom texture for
   * specific screens and cancels the original rendering.
   *
   * @param context The draw context used for rendering.
   * @param mouseX  The X coordinate of the mouse.
   * @param mouseY  The Y coordinate of the mouse.
   * @param delta   The time delta since the last frame.
   * @param ci      The callback information for the method.
   */
  @Inject(method = "extractBackground", at = @At(value = "HEAD"), cancellable = true) private void renderBackground(
    GuiGraphicsExtractor context,
    int mouseX,
    int mouseY,
    float delta,
    CallbackInfo ci) {
    if (this.minecraft.level == null) {
      this.extractPanorama(context, delta);
    }
    ci.cancel();
  }

  @Shadow protected abstract void extractPanorama(GuiGraphicsExtractor context, float deltaTicks);
}
