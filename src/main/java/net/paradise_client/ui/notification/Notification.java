package net.paradise_client.ui.notification;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.paradise_client.themes.AbstractThemeRenderer;
import net.paradise_client.themes.Theme;
import net.paradise_client.themes.ThemeManager;

public class Notification {
  private static final int PADDING_X = 12;
  private static final int PADDING_Y = 8;
  private static final int MAX_WIDTH = 260;
  private static final int BAR_HEIGHT = 2;
  private static final int STACK_GAP = 8;

  private static final long SLIDE_MS = 600L;
  private static final long LIFETIME = 5_000L;

  private final String title;
  private final String message;
  private final long startTime;
  private final AbstractThemeRenderer theme = Theme.TRANSPARENT.getRenderer();

  private int x, y, width, height;

  public Notification(String title, String message) {
    this.title = title;
    this.message = message;
    this.startTime = System.currentTimeMillis();
  }

  public boolean draw(DrawContext ctx, TextRenderer tr, int slot) {
    long elapsed = System.currentTimeMillis() - startTime;

    float pLife = MathHelper.clamp(elapsed / (float) LIFETIME, 0f, 1f);
    float pSlideIn = easeOutCubic(MathHelper.clamp(elapsed / (float) SLIDE_MS, 0f, 1f));

    float alphaMul;
    if (elapsed < SLIDE_MS) {
      alphaMul = pSlideIn;               // fade‑in
    } else if (elapsed > LIFETIME - SLIDE_MS) {
      float pSlideOut = (elapsed - (LIFETIME - SLIDE_MS)) / (float) SLIDE_MS;
      alphaMul = 1f - easeOutCubic(pSlideOut); // fade‑out
    } else {
      alphaMul = 1f;
    }

    int titleW = tr.getWidth(title);
    int msgW = tr.getWidth(message);
    this.width = Math.min(Math.max(titleW, msgW) + PADDING_X * 2, MAX_WIDTH);
    this.height = tr.fontHeight * 2 + PADDING_Y * 2 + 6 + BAR_HEIGHT;

    int screenW = ctx.getScaledWindowWidth();
    int screenH = ctx.getScaledWindowHeight();

    int startX = screenW + 10;
    int endX = screenW - width - 12;
    if (elapsed < SLIDE_MS) {
      this.x = (int) (endX + (startX - endX) * (1f - pSlideIn));
    } else if (elapsed > LIFETIME - SLIDE_MS) {
      float pSlideOut = easeOutCubic((elapsed - (LIFETIME - SLIDE_MS)) / (float) SLIDE_MS);
      this.x = (int) (endX + (startX - endX) * pSlideOut);
    } else {
      this.x = endX;
    }

    float stackTargetY = screenH - height - 20 - slot * (height + STACK_GAP);
    float baseY;
    if (elapsed < SLIDE_MS) {
      float yOffset = 15 * (1f - easeOutCubic(pSlideIn));
      baseY = stackTargetY - yOffset;
    } else {
      baseY = stackTargetY;
    }
    this.y = (int) baseY;

    theme.renderHudPanel(ctx, x, y, width, height);

    int txtClr = argb(alphaMul, getMessageColor());
    int titleClr = argb(alphaMul, getTitleColor());

    ctx.drawText(tr, title, x + PADDING_X, y + PADDING_Y, titleClr, false);
    ctx.drawText(tr, message, x + PADDING_X, y + PADDING_Y + tr.fontHeight + 4, txtClr, false);

    float barFrac = 1f - pLife;
    theme.renderProgressBar(ctx, x, y + height - BAR_HEIGHT, width, BAR_HEIGHT, barFrac);

    return pLife >= 1f;
  }

  public boolean isHovered(double mouseX, double mouseY) {
    return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
  }

  public void onClick(int button) {
    // Default click behavior: dismiss
  }

  private static float easeOutCubic(float t) {
    return (float) (1f - Math.pow(1f - t, 3));
  }

  private static int argb(float alpha, int rgb) {
    return ((int) (alpha * 255) << 24) | (rgb & 0x00FFFFFF);
  }

  private int getTitleColor() {
    Theme currentTheme = ThemeManager.getTheme();
    return switch (currentTheme) {
      case MATRIX -> 0x00FF00;
      case PARTICLE -> 0xD4A8FF;
      case LEGACY -> 0xE0E0E0;
      case MODERN, TRANSPARENT -> 0x00D4FF;
    };
  }

  private int getMessageColor() {
    Theme currentTheme = ThemeManager.getTheme();
    return switch (currentTheme) {
      case MATRIX -> 0x00AA00;
      case PARTICLE, LEGACY, MODERN, TRANSPARENT -> 0xFFFFFF;
    };
  }
}
