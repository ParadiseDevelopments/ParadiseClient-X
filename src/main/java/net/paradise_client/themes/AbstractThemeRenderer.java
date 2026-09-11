package net.paradise_client.themes;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Abstract base class for UI themes Handles rendering of wallpapers, buttons, HUD elements, panels, etc.
 */
public abstract class AbstractThemeRenderer {

  /**
   * Renders the background/wallpaper
   */
  public abstract void renderBackground(GuiGraphicsExtractor context, int width, int height);

  /**
   * Renders a button with various states
   */
  public abstract void renderButton(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    boolean hovered,
    boolean pressed,
    String text,
    Font font);

  /**
   * Renders a HUD panel (semi-transparent overlay)
   */
  public abstract void renderHudPanel(GuiGraphicsExtractor context, int x, int y, int width, int height);

  /**
   * Renders a title bar
   */
  public abstract void renderTitleBar(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    String title,
    Font font,
    boolean active);

  /**
   * Renders a taskbar at the bottom of the screen
   */
  public abstract void renderTaskbar(GuiGraphicsExtractor context, int screenWidth, int screenHeight, int height);

  /**
   * Renders a panel/window background
   */
  public abstract void renderPanel(GuiGraphicsExtractor context, int x, int y, int width, int height);

  /**
   * Renders a text input field
   */
  public abstract void renderTextField(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean focused);

  /**
   * Renders a selection highlight
   */
  public abstract void renderSelection(GuiGraphicsExtractor context, int x, int y, int width, int height);

  /**
   * Renders a progress bar
   */
  public abstract void renderProgressBar(GuiGraphicsExtractor context, int x, int y, int width, int height, float progress);

  /**
   * Renders a slider
   */
  public abstract void renderSlider(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    float value,
    boolean hovered);

  /**
   * Renders a checkbox
   */
  public abstract void renderCheckbox(GuiGraphicsExtractor context, int x, int y, int size, boolean checked, boolean hovered);

  /**
   * Renders a separator line
   */
  public abstract void renderSeparator(GuiGraphicsExtractor context, int x, int y, int width);

  /**
   * Gets the theme name
   */
  public abstract String getThemeName();

  /**
   * Called when theme is initialized (for setup/animations)
   */
  public void initialize() {
  }

  /**
   * Called every frame for animations
   */
  public void update() {
  }

  /**
   * Utility: Blend two ARGB colors
   */
  protected int blendColors(int color1, int color2, float ratio) {
    int a1 = (color1 >> 24) & 0xFF;
    int r1 = (color1 >> 16) & 0xFF;
    int g1 = (color1 >> 8) & 0xFF;
    int b1 = color1 & 0xFF;

    int a2 = (color2 >> 24) & 0xFF;
    int r2 = (color2 >> 16) & 0xFF;
    int g2 = (color2 >> 8) & 0xFF;
    int b2 = color2 & 0xFF;

    int a = (int) (a1 + (a2 - a1) * ratio);
    int r = (int) (r1 + (r2 - r1) * ratio);
    int g = (int) (g1 + (g2 - g1) * ratio);
    int b = (int) (b1 + (b2 - b1) * ratio);

    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  /**
   * Utility: Draw vertical gradient
   */
  protected void drawVerticalGradient(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    int topColor,
    int bottomColor) {
    context.fillGradient(x, y, x + width, y + height, topColor, bottomColor);
  }

  /**
   * Utility: Draw horizontal gradient
   */
  protected void drawHorizontalGradient(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    int leftColor,
    int rightColor) {
    int bands = Math.max(1, Math.min(16, width));
    for (int i = 0; i < bands; i++) {
      float ratio = (bands == 1) ? 0.0f : (float) i / (bands - 1);
      int color = blendColors(leftColor, rightColor, ratio);
      int left = x + (int) ((float) (width * i) / bands);
      int right = x + (int) ((float) (width * (i + 1)) / bands);
      if (right <= left) {
        right = left + 1;
      }
      context.fill(left, y, right, y + height, color);
    }
  }
}