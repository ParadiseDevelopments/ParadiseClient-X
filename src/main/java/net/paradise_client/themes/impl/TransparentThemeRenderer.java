package net.paradise_client.themes.impl;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.paradise_client.themes.AbstractThemeRenderer;

/**
 * Transparent theme with a clean, semi-transparent look.
 */
public class TransparentThemeRenderer extends AbstractThemeRenderer {
  private static final int BG_COLOR = 0x66000000;
  private static final int ACCENT_COLOR = 0xFF00D4FF;
  private static final int BORDER_COLOR = 0x33FFFFFF;
  private static final int TEXT_COLOR = 0xFFFFFFFF;

  @Override
  public void renderBackground(DrawContext context, int width, int height) {
    // Subtle dark overlay with a hint of accent color
    context.fill(0, 0, width, height, 0x66000000);
    // Optional: Add a very subtle gradient or vignette if needed
  }

  @Override
  public void renderButton(DrawContext context, int x, int y, int width, int height, boolean hovered, boolean pressed, String text, TextRenderer font) {
    int color = pressed ? 0x99000000 : hovered ? 0x77000000 : 0x44000000;
    drawRoundedRect(context, x, y, width, height, 3, color);
    drawRoundedBorder(context, x, y, width, height, 3, hovered ? ACCENT_COLOR : 0x22FFFFFF, 1);
    if (text != null && font != null) {
      int textX = x + (width - font.getWidth(text)) / 2;
      int textY = y + (height - 8) / 2;
      context.drawText(font, text, textX, textY, TEXT_COLOR, false);
    }
  }

  @Override
  public void renderHudPanel(DrawContext context, int x, int y, int width, int height) {
    drawRoundedRect(context, x, y, width, height, 4, 0x44000000);
    drawRoundedBorder(context, x, y, width, height, 4, 0x22FFFFFF, 1);
  }

  @Override
  public void renderTitleBar(DrawContext context, int x, int y, int width, int height, String title, TextRenderer font, boolean active) {
    int color = active ? 0x88000000 : 0x66000000;
    drawRoundedRect(context, x, y, width, height, 3, color);
    drawRoundedBorder(context, x, y, width, height, 3, active ? ACCENT_COLOR : 0x44FFFFFF, 1);
    if (title != null && font != null) {
      context.drawText(font, title, x + 8, y + (height - 8) / 2, TEXT_COLOR, false);
    }
  }

  @Override
  public void renderTaskbar(DrawContext context, int screenWidth, int screenHeight, int height) {
    context.fill(0, screenHeight - height, screenWidth, screenHeight, 0xAA000000);
    context.fill(0, screenHeight - height, screenWidth, screenHeight - height + 1, 0x44FFFFFF);
  }

  @Override
  public void renderPanel(DrawContext context, int x, int y, int width, int height) {
    drawRoundedRect(context, x, y, width, height, 5, 0x66000000);
    drawRoundedBorder(context, x, y, width, height, 5, 0x22FFFFFF, 1);
  }

  @Override
  public void renderTextField(DrawContext context, int x, int y, int width, int height, boolean focused) {
    drawRoundedRect(context, x, y, width, height, 4, 0x33000000);
    drawRoundedBorder(context, x, y, width, height, 4, focused ? ACCENT_COLOR : 0x22FFFFFF, 1);
  }

  @Override
  public void renderSelection(DrawContext context, int x, int y, int width, int height) {
    drawRoundedRect(context, x, y, width, height, 3, 0x4400D4FF);
  }

  @Override
  public void renderProgressBar(DrawContext context, int x, int y, int width, int height, float progress) {
    drawRoundedRect(context, x, y, width, height, 4, 0x66000000);
    int fillWidth = (int)(width * progress);
    if (fillWidth > 0) {
      drawRoundedRect(context, x, y, fillWidth, height, 4, ACCENT_COLOR);
    }
  }

  @Override
  public void renderSlider(DrawContext context, int x, int y, int width, int height, float value, boolean hovered) {
    context.fill(x, y + height / 2 - 1, x + width, y + height / 2 + 1, 0x66FFFFFF);
    int thumbX = x + (int)((width - 8) * value);
    drawRoundedRect(context, thumbX, y, 8, height, 2, hovered ? ACCENT_COLOR : 0xFFFFFFFF);
  }

  @Override
  public void renderCheckbox(DrawContext context, int x, int y, int size, boolean checked, boolean hovered) {
    drawRoundedRect(context, x, y, size, size, 2, 0x66000000);
    drawRoundedBorder(context, x, y, size, size, 2, hovered ? ACCENT_COLOR : 0x66FFFFFF, 1);
    if (checked) {
      drawRoundedRect(context, x + 2, y + 2, size - 4, size - 4, 1, ACCENT_COLOR);
    }
  }

  @Override
  public void renderSeparator(DrawContext context, int x, int y, int width) {
    context.fill(x, y, x + width, y + 1, 0x44FFFFFF);
  }

  private void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
    context.fill(x + radius, y, x + width - radius, y + height, color);
    context.fill(x, y + radius, x + radius, y + height - radius, color);
    context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
    
    // Corners
    fillCircleQuarter(context, x + radius, y + radius, radius, color, 0); // Top-left
    fillCircleQuarter(context, x + width - radius, y + radius, radius, color, 1); // Top-right
    fillCircleQuarter(context, x + radius, y + height - radius, radius, color, 2); // Bottom-left
    fillCircleQuarter(context, x + width - radius, y + height - radius, radius, color, 3); // Bottom-right
  }

  private void drawRoundedBorder(DrawContext context, int x, int y, int width, int height, int radius, int color, int thickness) {
    // Top, bottom, left, right lines
    context.fill(x + radius, y, x + width - radius, y + thickness, color); // Top
    context.fill(x + radius, y + height - thickness, x + width - radius, y + height, color); // Bottom
    context.fill(x, y + radius, x + thickness, y + height - radius, color); // Left
    context.fill(x + width - thickness, y + radius, x + width, y + height - radius, color); // Right
    
    // Corner borders could be more complex, but simple version:
    // This is a simplified version, ideally use a more complex arc renderer
  }

  private void fillCircleQuarter(DrawContext context, int cx, int cy, int r, int color, int quarter) {
    for (int i = 0; i <= r; i++) {
      for (int j = 0; j <= r; j++) {
        if (i * i + j * j <= r * r) {
          int x = 0, y = 0;
          switch (quarter) {
            case 0 -> { x = cx - i; y = cy - j; }
            case 1 -> { x = cx + i - 1; y = cy - j; }
            case 2 -> { x = cx - i; y = cy + j - 1; }
            case 3 -> { x = cx + i - 1; y = cy + j - 1; }
          }
          context.fill(x, y, x + 1, y + 1, color);
        }
      }
    }
  }

  @Override public String getThemeName() {
    return "Transparent";
  }
}
