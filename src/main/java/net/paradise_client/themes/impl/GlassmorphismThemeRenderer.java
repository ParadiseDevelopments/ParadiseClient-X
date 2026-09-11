package net.paradise_client.themes.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.paradise_client.Constants;
import net.paradise_client.themes.AbstractThemeRenderer;

/**
 * Glassmorphism theme
 */
public class GlassmorphismThemeRenderer extends AbstractThemeRenderer {
  private static final int GLASS_BASE = 0x88FFFFFF;
  private static final int GLASS_SHADOW = 0x22000000;
  private static final int ACCENT_COLOR = 0xFF4A90E2;
  private static final int TEXT_COLOR = 0xFF333333;

  private int animationFrame = 0;
  private float buttonHoverScale = 1.0f;
  private boolean wasHovered = false;

  @Override public void renderBackground(GuiGraphicsExtractor context, int width, int height) {
    context.blit(RenderPipelines.GUI_TEXTURED,
      Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/wallpaper/wallpaper-glassmor.png"),
      0,
      0,
      0.0F,
      0.0F,
      width,
      height,
      width,
      height,
      width,
      height);
  }

  @Override
  public void renderButton(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    boolean hovered,
    boolean pressed,
    String text,
    Font font) {
    if (hovered != wasHovered) {
      wasHovered = hovered;
    }
    float targetScale = hovered ? 1.1f : 1.0f;
    float diff = targetScale - buttonHoverScale;
    buttonHoverScale += diff * (1.0f - (float) Math.cos(animationFrame * 0.05) * 0.5f);
    if (Math.abs(diff) < 0.01f) {
      buttonHoverScale = targetScale;
    }

    int animatedWidth = (int) (width * buttonHoverScale);
    int animatedHeight = (int) (height * buttonHoverScale);
    int offsetX = x + (width - animatedWidth) / 2;
    int offsetY = y + (height - animatedHeight) / 2;

    int baseAlpha = pressed ? 0x66 : hovered ? 0x88 : 0x77;
    int glassColor = (baseAlpha << 24) | (0xFFFFFF & GLASS_BASE);
    if (hovered) {
      glassColor = blendColors(glassColor, ACCENT_COLOR, 0.2f);
    }
    context.fill(offsetX, offsetY, offsetX + animatedWidth, offsetY + animatedHeight, glassColor);
    drawVerticalGradient(context,
      offsetX + 1,
      offsetY + 1,
      animatedWidth - 2,
      animatedHeight - 2,
      blendColors(0x22FFFFFF, GLASS_SHADOW, 0.3f),
      GLASS_SHADOW);

    int textX = offsetX + (animatedWidth - font.width(text)) / 2;
    int textY = offsetY + (animatedHeight - font.lineHeight) / 2;
    context.text(font, text, textX, textY, TEXT_COLOR, false);
    if (hovered) {
      context.fill(offsetX,
        offsetY,
        offsetX + animatedWidth,
        offsetY + animatedHeight,
        blendColors(0x00000000, ACCENT_COLOR, 0.1f));
    }
  }

  @Override public void renderHudPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    context.fill(x, y, x + width, y + height, GLASS_BASE);
    drawVerticalGradient(context, x + 1, y + 1, width - 2, height - 2, 0x33FFFFFF, GLASS_SHADOW);
    float shimmer = (float) Math.sin((animationFrame + x * 0.1) * 0.05) * 0.1f + 1.0f;
    int shimmerColor = (int) (shimmer * 0x22) << 24 | (ACCENT_COLOR & 0xFFFFFF);
    context.fill(x + 1, y + 1, x + width - 1, y + height - 1, shimmerColor);
    float pulse = (float) Math.sin(animationFrame * 0.1) * 0.05f + 0.95f;
    int borderColor = (int) (pulse * 255) << 24 | (ACCENT_COLOR & 0xFFFFFF);
    context.horizontalLine(x, x + width, y, borderColor);
    context.horizontalLine(x, x + width, y + height - 1, borderColor);
    context.verticalLine(x, y, y + height, borderColor);
    context.verticalLine(x + width - 1, y, y + height, borderColor);
  }

  @Override
  public void renderTitleBar(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    String title,
    Font font,
    boolean active) {
    int glassColor = active ? 0xAAFFFFFF : 0x88FFFFFF;
    context.fill(x, y, x + width, y + height, glassColor);
    drawVerticalGradient(context, x + 1, y + 1, width - 2, height - 2, 0x44FFFFFF, GLASS_SHADOW);
    float pulse = (float) Math.sin(animationFrame * 0.08) * 0.15f + 0.85f;
    int glowColor = (int) (pulse * 0x44) << 24 | (ACCENT_COLOR & 0xFFFFFF);
    context.fill(x - 1, y - 1, x + width + 1, y + height + 1, glowColor);
    int textX = x + (width - font.width(title)) / 2;
    int textY = y + (height - font.lineHeight) / 2;
    context.text(font, title, textX, textY, TEXT_COLOR, false);
  }

  @Override public void renderTaskbar(GuiGraphicsExtractor context, int screenWidth, int screenHeight, int height) {
    int y = screenHeight - height;
    context.fill(0, y, screenWidth, y + height, 0x77FFFFFF);
    float wave = (float) Math.sin((animationFrame + y * 0.05) * 0.1) * 0.1f + 0.9f; // Vertical wave effect
    int waveColor = (int) (wave * 0x33) << 24 | (GLASS_SHADOW & 0xFFFFFF);
    drawHorizontalGradient(context, 0, y, screenWidth, height, 0x88FFFFFF, blendColors(GLASS_SHADOW, waveColor, 0.5f));
  }

  @Override public void renderPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    context.fill(x, y, x + width, y + height, GLASS_BASE);
    drawVerticalGradient(context, x + 1, y + 1, width - 2, height - 2, 0x33FFFFFF, GLASS_SHADOW);
  }

  @Override public void renderTextField(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean focused) {
    context.fill(x, y, x + width, y + height, 0x77FFFFFF);
    drawVerticalGradient(context, x + 1, y + 1, width - 2, height - 2, 0x44FFFFFF, GLASS_SHADOW);
    int borderColor = focused ? ACCENT_COLOR : 0xFF999999;
    context.horizontalLine(x, x + width, y, borderColor);
    context.horizontalLine(x, x + width, y + height - 1, borderColor);
    context.verticalLine(x, y, y + height, borderColor);
    context.verticalLine(x + width - 1, y, y + height, borderColor);
  }

  @Override public void renderSelection(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    context.fill(x, y, x + width, y + height, blendColors(0x00000000, ACCENT_COLOR, 0.15f));
  }

  @Override public void renderProgressBar(GuiGraphicsExtractor context, int x, int y, int width, int height, float progress) {
    int fillWidth = (int) (width * Math.max(0, Math.min(1, progress)));
    context.fill(x, y, x + width, y + height, 0x77FFFFFF);
    context.fill(x, y, x + fillWidth, y + height, blendColors(GLASS_BASE, ACCENT_COLOR, 0.5f));
  }

  @Override
  public void renderSlider(GuiGraphicsExtractor context, int x, int y, int width, int height, float value, boolean hovered) {
    int fillWidth = (int) (width * Math.max(0, Math.min(1, value)));
    context.fill(x, y, x + width, y + height, 0x77FFFFFF);
    context.fill(x, y, x + fillWidth, y + height, hovered ? blendColors(ACCENT_COLOR, 0xFFFFFF, 0.2f) : ACCENT_COLOR);
    int knobX = x + fillWidth - height / 2;
    context.fill(knobX, y, knobX + height, y + height, 0x88FFFFFF);
  }

  @Override public void renderCheckbox(GuiGraphicsExtractor context, int x, int y, int size, boolean checked, boolean hovered) {
    context.fill(x, y, x + size, y + size, 0x77FFFFFF);
    drawVerticalGradient(context, x + 1, y + 1, size - 2, size - 2, 0x44FFFFFF, GLASS_SHADOW);
    if (checked) {
      context.fill(x + 2, y + 2, x + size - 2, y + size - 2, ACCENT_COLOR);
    }
    if (hovered) {
      context.fill(x, y, x + size, y + size, blendColors(0x00000000, 0xFFFFFF, 0.1f));
    }
  }

  @Override public void renderSeparator(GuiGraphicsExtractor context, int x, int y, int width) {
    context.fill(x, y, x + width, y + 1, 0xFFCCCCCC);
  }

  @Override public String getThemeName() {
    return "Glassmorphism";
  }

  @Override public void update() {
    animationFrame = (animationFrame + 1) % 360;
  }
}