package net.paradise_client.themes.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.paradise_client.themes.AbstractThemeRenderer;

/**
 * Modern theme
 */
public class ModernThemeRenderer extends AbstractThemeRenderer {

  private static final int BACKGROUND_TOP = 0xFF1A1A2E;
  private static final int BACKGROUND_BOTTOM = 0xFF16213E;
  private static final int PANEL_COLOR = 0xCC2A2E48;
  private static final int BUTTON_BASE = 0xFF3A3F5C;
  private static final int BUTTON_HOVER = 0xFF4A5F78;
  private static final int BUTTON_PRESSED = 0xFF2E3A4C;
  private static final int TEXT_COLOR = 0xFFFFFFFF;
  private static final int ACCENT_COLOR = 0xFF00D4FF;
  private int animationFrame = 0;

  @Override public void renderBackground(GuiGraphicsExtractor context, int width, int height) {
    drawVerticalGradient(context, 0, 0, width, height, BACKGROUND_TOP, BACKGROUND_BOTTOM);

    float wave = (float) Math.sin(animationFrame * 0.02) * 0.05f + 0.05f;
    int overlayColor = (int) (wave * 255) << 24 | 0x00D4FF;
    drawVerticalGradient(context, 0, 0, width, height, overlayColor, 0x00000000);
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

    int cornerRadius = 6;

    if (!pressed) {
      drawRoundedRect(context, x + 1, y + 1, width, height, cornerRadius, 0x44000000);
    }

    int baseColor = pressed ? BUTTON_PRESSED : hovered ? BUTTON_HOVER : BUTTON_BASE;
    drawRoundedRect(context, x, y, width, height, cornerRadius, baseColor);

    int gradientTop = blendColors(baseColor, 0xFFFFFFFF, 0.08f);
    drawRoundedGradient(context, x, y, width, height / 2, gradientTop, baseColor);

    if (hovered) {
      drawRoundedBorder(context, x, y, width, height, cornerRadius, ACCENT_COLOR, 1);
      int glowColor = (int) (0x20) << 24 | (ACCENT_COLOR & 0xFFFFFF);
      drawRoundedRect(context, x - 1, y - 1, width + 2, height + 2, cornerRadius + 1, glowColor);
    } else {
      drawRoundedBorder(context, x, y, width, height, cornerRadius, 0x33FFFFFF, 1);
    }

    int textX = x + (width - font.width(text)) / 2;
    int textY = y + (height - font.lineHeight) / 2;
    if (!pressed) {
      context.text(font, text, textX + 1, textY + 1, 0x66000000, false);
    }
    context.text(font, text, textX, textY, TEXT_COLOR, false);
  }

  @Override public void renderHudPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int cornerRadius = 8;

    float pulse = (float) Math.sin(animationFrame * 0.05) * 0.15f + 0.25f;
    int glowColor = (int) (pulse * 255) << 24 | (ACCENT_COLOR & 0xFFFFFF);
    drawRoundedRect(context, x - 2, y - 2, width + 4, height + 4, cornerRadius + 2, glowColor);

    float alpha = (float) Math.sin(animationFrame * 0.08) * 0.05f + 0.75f;
    int animatedColor = (int) (alpha * 255) << 24 | (PANEL_COLOR & 0xFFFFFF);
    drawRoundedRect(context, x, y, width, height, cornerRadius, animatedColor);

    drawRoundedGradient(context, x + 2, y + 2, width - 4, height - 4, 0x22FFFFFF, 0x11000000);

    int accentHeight = 2;
    drawRoundedRect(context, x, y, width, accentHeight, cornerRadius, ACCENT_COLOR);

    int accentSize = 10;
    float accentAlpha = (float) Math.sin(animationFrame * 0.1) * 0.3f + 0.5f;
    int cornerAccent = (int) (accentAlpha * 255) << 24 | (ACCENT_COLOR & 0xFFFFFF);
    drawRoundedRect(context, x, y, accentSize, accentSize, cornerRadius, cornerAccent);
    drawRoundedRect(context, x + width - accentSize, y, accentSize, accentSize, cornerRadius, cornerAccent);
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

    int cornerRadius = 6;

    float alpha = (float) Math.cos(animationFrame * 0.05) * 0.05f + 0.95f;
    int color = active ? blendColors(BUTTON_BASE, ACCENT_COLOR, 0.4f) : BUTTON_BASE;
    int animatedColor = (int) (alpha * 255) << 24 | (color & 0xFFFFFF);

    drawRoundedRect(context, x, y, width, height, cornerRadius, animatedColor);

    int gradientColor = blendColors(color, 0xFFFFFFFF, 0.15f);
    drawRoundedGradient(context, x, y, width, height / 2, gradientColor, color);

    if (active) {
      drawRoundedRect(context, x, y + height - 2, width, 2, 0, ACCENT_COLOR);
    }

    int textX = x + (width - font.width(title)) / 2;
    int textY = y + (height - font.lineHeight) / 2;
    context.text(font, title, textX + 1, textY + 1, 0x88000000, false);
    context.text(font, title, textX, textY, TEXT_COLOR, false);
  }

  @Override public void renderTaskbar(GuiGraphicsExtractor context, int screenWidth, int screenHeight, int height) {
    int y = screenHeight - height;

    drawHorizontalGradient(context, 0, y, screenWidth, height, BUTTON_BASE, blendColors(BUTTON_BASE, 0xFF000000, 0.2f));

    float pulse = (float) Math.sin(animationFrame * 0.06) * 0.15f + 0.2f;
    int accentColor = blendColors(0x00000000, ACCENT_COLOR, pulse);
    drawHorizontalGradient(context, 0, y, screenWidth, 2, ACCENT_COLOR, accentColor);

    context.fill(0, y, screenWidth, y + 1, 0x66000000);
  }

  @Override public void renderPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int cornerRadius = 6;
    drawRoundedRect(context, x + 2, y + 2, width, height, cornerRadius, 0x66000000);
    drawRoundedRect(context, x, y, width, height, cornerRadius, PANEL_COLOR);
    drawRoundedGradient(context, x + 2, y + 2, width - 4, height - 4, 0x33FFFFFF, 0x11000000);
    drawRoundedBorder(context, x, y, width, height, cornerRadius, 0x44FFFFFF, 1);
  }

  @Override public void renderTextField(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean focused) {
    int cornerRadius = 4;
    int borderColor = focused ? ACCENT_COLOR : 0xFF555555;

    if (focused) {
      int shadowColor = (int) (0x40) << 24 | (ACCENT_COLOR & 0xFFFFFF);
      drawRoundedRect(context, x - 1, y - 1, width + 2, height + 2, cornerRadius + 1, shadowColor);
    }

    drawRoundedRect(context, x, y, width, height, cornerRadius, 0xFF2A2E48);
    drawRoundedRect(context, x + 2, y + 2, width - 4, height - 4, cornerRadius - 1, 0xFF3A3F5C);
    drawRoundedBorder(context, x, y, width, height, cornerRadius, borderColor, focused ? 2 : 1);
  }

  @Override public void renderSelection(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int cornerRadius = 3;
    drawRoundedRect(context, x, y, width, height, cornerRadius, blendColors(0x00000000, ACCENT_COLOR, 0.25f));
    drawRoundedBorder(context, x, y, width, height, cornerRadius, ACCENT_COLOR, 1);
  }

  @Override public void renderProgressBar(GuiGraphicsExtractor context, int x, int y, int width, int height, float progress) {
    int cornerRadius = height / 2;
    float clampedProgress = Math.max(0, Math.min(1, progress));
    int fillWidth = (int) (width * clampedProgress);

    drawRoundedRect(context, x, y, width, height, cornerRadius, 0xFF2A2E48);

    if (fillWidth > 0) {
      int progressColor1 = ACCENT_COLOR;
      int progressColor2 = blendColors(ACCENT_COLOR, 0xFFFFFFFF, 0.3f);
      drawRoundedRect(context, x, y, fillWidth, height, cornerRadius, progressColor1);
      drawRoundedGradient(context, x, y, fillWidth, height / 2, progressColor2, progressColor1);

      float shimmer = (float) Math.sin(animationFrame * 0.15 + progress * 10) * 0.2f + 0.2f;
      int shimmerColor = (int) (shimmer * 255) << 24 | 0xFFFFFF;
      drawRoundedRect(context, x, y, fillWidth, height / 3, cornerRadius, shimmerColor);
    }
    drawRoundedBorder(context, x, y, width, height, cornerRadius, 0x44FFFFFF, 1);
  }

  @Override
  public void renderSlider(GuiGraphicsExtractor context, int x, int y, int width, int height, float value, boolean hovered) {
    int cornerRadius = height / 2;
    float clampedValue = Math.max(0, Math.min(1, value));
    int fillWidth = (int) (width * clampedValue);

    drawRoundedRect(context, x, y, width, height, cornerRadius, 0xFF2A2E48);

    int fillColor = hovered ? blendColors(ACCENT_COLOR, 0xFFFFFFFF, 0.2f) : ACCENT_COLOR;
    if (fillWidth > 0) {
      drawRoundedRect(context, x, y, fillWidth, height, cornerRadius, fillColor);
    }

    int knobSize = height + 4;
    int knobX = x + fillWidth - knobSize / 2;
    int knobY = y - 2;

    drawRoundedRect(context, knobX + 2, knobY + 2, knobSize, knobSize, knobSize / 2, 0x88000000);
    drawRoundedRect(context, knobX, knobY, knobSize, knobSize, knobSize / 2, 0xFF3A3F5C);
    drawRoundedGradient(context, knobX, knobY, knobSize, knobSize / 2, 0x66FFFFFF, 0x00FFFFFF);

    if (hovered) {
      drawRoundedBorder(context, knobX, knobY, knobSize, knobSize, knobSize / 2, ACCENT_COLOR, 2);
    }
  }

  @Override public void renderCheckbox(GuiGraphicsExtractor context, int x, int y, int size, boolean checked, boolean hovered) {
    int cornerRadius = 3;

    drawRoundedRect(context, x + 1, y + 1, size, size, cornerRadius, 0x66000000);
    drawRoundedRect(context, x, y, size, size, cornerRadius, BUTTON_BASE);

    if (checked) {
      drawRoundedRect(context, x + 3, y + 3, size - 6, size - 6, cornerRadius - 1, ACCENT_COLOR);
      if (hovered) {
        int glowColor = (int) (0x40) << 24 | (ACCENT_COLOR & 0xFFFFFF);
        drawRoundedRect(context, x + 2, y + 2, size - 4, size - 4, cornerRadius, glowColor);
      }
    }

    if (hovered) {
      drawRoundedRect(context, x, y, size, size, cornerRadius, blendColors(0x00000000, 0xFFFFFFFF, 0.15f));
      drawRoundedBorder(context, x, y, size, size, cornerRadius, ACCENT_COLOR, 1);
    } else {
      drawRoundedBorder(context, x, y, size, size, cornerRadius, 0x44FFFFFF, 1);
    }
  }

  @Override public void renderSeparator(GuiGraphicsExtractor context, int x, int y, int width) {
    drawHorizontalGradient(context, x, y, width / 3, 1, 0x00555555, 0xFF555555);
    context.fill(x + width / 3, y, x + 2 * width / 3, y + 1, 0xFF555555);
    drawHorizontalGradient(context, x + 2 * width / 3, y, width / 3, 1, 0xFF555555, 0x00555555);
  }

  @Override public String getThemeName() {
    return "Modern";
  }

  @Override public void update() {
    animationFrame = (animationFrame + 1) % 360;
  }

  private void drawRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
    if (radius <= 0) {
      context.fill(x, y, x + width, y + height, color);
      return;
    }

    context.fill(x + radius, y, x + width - radius, y + height, color);
    context.fill(x, y + radius, x + radius, y + height - radius, color);
    context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

    fillCircleQuarter(context, x + radius, y + radius, radius, color, 0);
    fillCircleQuarter(context, x + width - radius, y + radius, radius, color, 1);
    fillCircleQuarter(context, x + radius, y + height - radius, radius, color, 2);
    fillCircleQuarter(context, x + width - radius, y + height - radius, radius, color, 3);
  }

  private void drawRoundedGradient(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    int colorTop,
    int colorBottom) {
    drawVerticalGradient(context, x, y, width, height, colorTop, colorBottom);
  }

  private void drawRoundedBorder(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    int radius,
    int color,
    int thickness) {
    for (int i = 0; i < thickness; i++) {
      drawRoundedRect(context, x + i, y + i, width - 2 * i, thickness, radius, color);
      drawRoundedRect(context, x + i, y + height - thickness - i, width - 2 * i, thickness, radius, color);
      drawRoundedRect(context, x + i, y + i, thickness, height - 2 * i, radius, color);
      drawRoundedRect(context, x + width - thickness - i, y + i, thickness, height - 2 * i, radius, color);
    }
  }

  private void fillCircleQuarter(GuiGraphicsExtractor context, int centerX, int centerY, int radius, int color, int quarter) {
    for (int dy = 0; dy <= radius; dy++) {
      for (int dx = 0; dx <= radius; dx++) {
        if (dx * dx + dy * dy <= radius * radius) {
          int px = 0, py = 0;
          switch (quarter) {
            case 0:
              px = centerX - dx;
              py = centerY - dy;
              break;
            case 1:
              px = centerX + dx;
              py = centerY - dy;
              break;
            case 2:
              px = centerX - dx;
              py = centerY + dy;
              break;
            case 3:
              px = centerX + dx;
              py = centerY + dy;
              break;
          }
          context.fill(px, py, px + 1, py + 1, color);
        }
      }
    }
  }
}