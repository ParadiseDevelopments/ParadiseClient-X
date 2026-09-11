package net.paradise_client.inject.mixin.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.paradise_client.themes.ThemeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to apply themed rendering to all buttons in the game
 */
@Mixin(AbstractButton.class) public class PressableWidgetMixin {
  @Inject(method = "extractWidgetRenderState", at = @At("HEAD"), cancellable = true)
  public void renderThemedButton(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    AbstractButton self = (AbstractButton) (Object) this;

    if (!self.visible || self.getWidth() <= 0 || self.getHeight() <= 0) {
      return;
    }

    boolean hovered = self.isHovered();
    boolean pressed = self.isFocused() || self.isHoveredOrFocused();

    ThemeManager.renderButton(context,
      self.getX(),
      self.getY(),
      self.getWidth(),
      self.getHeight(),
      hovered,
      pressed,
      self.getMessage().getString(),
      Minecraft.getInstance().font);

    ci.cancel();
  }
}