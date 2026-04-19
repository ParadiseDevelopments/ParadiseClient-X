package net.paradise_client.themes;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.paradise_client.ParadiseClient;

/**
 * Main theme rendering manager - delegates to active theme
 */
public class ThemeManager {
  private static Theme currentTheme = Theme.LEGACY;

  public static void setTheme(Theme theme) {
    currentTheme = theme;
    currentTheme.getRenderer().initialize();
    ParadiseClient.CONFIG.setTheme(theme);
  }

  public static Theme getTheme() {
    return currentTheme;
  }

  public static void update() {
    currentTheme.getRenderer().update();
  }

  public static void renderBackground(DrawContext context, int width, int height) {
    currentTheme.getRenderer().renderBackground(context, width, height);
  }

  public static void renderButton(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    boolean hovered,
    boolean pressed,
    String text,
    TextRenderer font) {
    currentTheme.getRenderer().renderButton(context, x, y, width, height, hovered, pressed, text, font);
  }

  public static void renderHudPanel(DrawContext context, int x, int y, int width, int height) {
    currentTheme.getRenderer().renderHudPanel(context, x, y, width, height);
  }

  public static void renderTitleBar(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    String title,
    TextRenderer font,
    boolean active) {
    currentTheme.getRenderer().renderTitleBar(context, x, y, width, height, title, font, active);
  }

  public static void renderTaskbar(DrawContext context, int screenWidth, int screenHeight, int height) {
    currentTheme.getRenderer().renderTaskbar(context, screenWidth, screenHeight, height);
  }

  public static void renderPanel(DrawContext context, int x, int y, int width, int height) {
    currentTheme.getRenderer().renderPanel(context, x, y, width, height);
  }

  public static void renderTextField(DrawContext context, int x, int y, int width, int height, boolean focused) {
    currentTheme.getRenderer().renderTextField(context, x, y, width, height, focused);
  }

  public static void renderSelection(DrawContext context, int x, int y, int width, int height) {
    currentTheme.getRenderer().renderSelection(context, x, y, width, height);
  }

  public static void renderProgressBar(DrawContext context, int x, int y, int width, int height, float progress) {
    currentTheme.getRenderer().renderProgressBar(context, x, y, width, height, progress);
  }

  public static void renderSlider(DrawContext context,
    int x,
    int y,
    int width,
    int height,
    float value,
    boolean hovered) {
    currentTheme.getRenderer().renderSlider(context, x, y, width, height, value, hovered);
  }

  public static void renderCheckbox(DrawContext context, int x, int y, int size, boolean checked, boolean hovered) {
    currentTheme.getRenderer().renderCheckbox(context, x, y, size, checked, hovered);
  }

  public static void renderSeparator(DrawContext context, int x, int y, int width) {
    currentTheme.getRenderer().renderSeparator(context, x, y, width);
  }
}