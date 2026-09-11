package net.paradise_client.themes.impl;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.paradise_client.themes.AbstractThemeRenderer;

import java.util.Random;

/**
 * Particle theme
 */
public class ParticleThemeRenderer extends AbstractThemeRenderer {
  private static final Random random = new Random();
  private static final int PARTICLE_COUNT = 80;
  private static final Particle[] particles = new Particle[PARTICLE_COUNT];
  private static int lastWidth = -1;
  private static int lastHeight = -1;
  private static final int COLOR_BG_TOP = 0xFF0F0B1F;
  private static final int COLOR_BG_BOTTOM = 0xFF1A0F3D;
  private static final int COLOR_PARTICLE_BRIGHT = 0xFFFFFFFF;
  private static final int COLOR_PARTICLE_MEDIUM = 0xFFB8A3FF;
  private static final int COLOR_PARTICLE_DIM = 0xFF7B5FD3;
  private static final int COLOR_ACCENT_BRIGHT = 0xFFD4A8FF;
  private static final int COLOR_ACCENT_MEDIUM = 0xFFA17FE8;
  private static final int COLOR_ACCENT_DARK = 0xFF6B4FBF;
  private static final int COLOR_GLASS = 0xAA2A1F5E;
  private int animationFrame = 0;
  private static final float CONNECTION_DISTANCE = 100f;
  private static final float CONNECTION_DISTANCE_SQ = CONNECTION_DISTANCE * CONNECTION_DISTANCE;
  private static final int MAX_CONNECTIONS_PER_FRAME = 60;
  private static final int MAX_FILL_CALLS_PER_FRAME = 450;
  private int frameFillBudgetUsed = 0;

  static {
    for (int i = 0; i < particles.length; i++) {
      particles[i] = new Particle();
    }
  }

  @Override public String getThemeName() {
    return "Particle";
  }

  @Override public void renderBackground(GuiGraphicsExtractor context, int width, int height) {
    frameFillBudgetUsed = 0;
    if (width != lastWidth || height != lastHeight) {
      for (Particle p : particles) {
        p.reset(width, height);
      }
      lastWidth = width;
      lastHeight = height;
    }

    drawVerticalGradient(context, 0, 0, width, height, COLOR_BG_TOP, COLOR_BG_BOTTOM);

    float nebulaOffset = (float) Math.sin(animationFrame * 0.01) * 50;
    for (int y = 0; y < height; y += 40) {
      float wave = (float) Math.sin((y + nebulaOffset) * 0.02) * 0.1f + 0.1f;
      int nebulaAlpha = (int) (wave * 255);
      int nebulaColor = (nebulaAlpha << 24) | (COLOR_ACCENT_DARK & 0xFFFFFF);
      boundedFill(context, 0, y, width, y + 20, nebulaColor);
    }

    for (Particle p : particles) {
      p.update(width, height);

      int glowSize = p.size + 4;
      int glowAlpha = (int) (p.pulsePhase * 60);
      int glowColor = (glowAlpha << 24) | (COLOR_PARTICLE_BRIGHT & 0xFFFFFF);
      boundedFill(context,
        (int) p.x - glowSize / 2,
        (int) p.y - glowSize / 2,
        (int) p.x + glowSize / 2,
        (int) p.y + glowSize / 2,
        glowColor);

      int coreAlpha = (int) (255 * p.pulsePhase);
      int coreColor = (coreAlpha << 24) | (COLOR_PARTICLE_BRIGHT & 0xFFFFFF);
      boundedFill(context,
        (int) p.x - p.size / 2,
        (int) p.y - p.size / 2,
        (int) p.x + p.size / 2,
        (int) p.y + p.size / 2,
        coreColor);
    }

    int connectionsDrawn = 0;
    outer:
    for (int i = 0; i < particles.length; i++) {
      Particle p1 = particles[i];

      for (int j = i + 1; j < particles.length; j++) {
        if (connectionsDrawn >= MAX_CONNECTIONS_PER_FRAME || frameFillBudgetUsed >= MAX_FILL_CALLS_PER_FRAME) {
          break outer;
        }
        Particle p2 = particles[j];

        float dx = p2.x - p1.x;
        float dy = p2.y - p1.y;
        float distanceSq = dx * dx + dy * dy;
        if (distanceSq >= CONNECTION_DISTANCE_SQ) {
          continue;
        }
        float distance = (float) Math.sqrt(distanceSq);
        float opacity = 1.0f - (distance / CONNECTION_DISTANCE);
        int lineAlpha = (int) (opacity * 80);
        int lineColor = (lineAlpha << 24) | (COLOR_PARTICLE_MEDIUM & 0xFFFFFF);
        drawLine(context, (int) p1.x, (int) p1.y, (int) p2.x, (int) p2.y, lineColor);
        connectionsDrawn++;
      }
    }

    for (Particle p : particles) {
      for (int i = 0; i < p.trailLength; i++) {
        if (frameFillBudgetUsed >= MAX_FILL_CALLS_PER_FRAME) {
          break;
        }
        int trailX = (int) (p.x - p.speedX * i * 2);
        int trailY = (int) (p.y - p.speedY * i * 2);

        float trailOpacity = (1.0f - (float) i / p.trailLength) * 0.5f;
        int trailAlpha = (int) (trailOpacity * 255);
        int trailColor = (trailAlpha << 24) | (COLOR_PARTICLE_DIM & 0xFFFFFF);

        boundedFill(context, trailX, trailY, trailX + 1, trailY + 1, trailColor);
      }
    }
  }

  private boolean boundedFill(GuiGraphicsExtractor context, int x0, int y0, int x1, int y1, int color) {
    if (frameFillBudgetUsed >= MAX_FILL_CALLS_PER_FRAME) {
      return false;
    }
    context.fill(x0, y0, x1, y1, color);
    frameFillBudgetUsed++;
    return true;
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

    int radius = 8;

    if (hovered) {
      float pulse = (float) Math.sin(animationFrame * 0.1) * 0.15f + 0.3f;
      int glowAlpha = (int) (pulse * 255);
      int glowColor = (glowAlpha << 24) | (COLOR_ACCENT_BRIGHT & 0xFFFFFF);
      fillRoundedRect(context, x - 3, y - 3, width + 6, height + 6, radius + 2, glowColor);
    }

    int topColor = pressed ? 0xDD4A3A7E : hovered ? 0xDD6A5A9E : 0xDD3A2A6E;
    int bottomColor = pressed ? 0xDD2A1A4E : hovered ? 0xDD4A3A7E : 0xDD2A1A5E;
    fillRoundedRect(context, x, y, width, height, radius, topColor);
    drawVerticalGradient(context, x + 1, y + 1, width - 2, height - 2, topColor, bottomColor);

    fillRoundedRect(context, x + 2, y + 2, width - 4, 2, radius, 0x44FFFFFF);

    if (hovered) {
      int particleCount = 8;
      for (int i = 0; i < particleCount; i++) {
        float angle = (float) (i * Math.PI * 2 / particleCount + animationFrame * 0.05);
        int px = (int) (x + width / 2 + Math.cos(angle) * (width / 2 + 5));
        int py = (int) (y + height / 2 + Math.sin(angle) * (height / 2 + 5));
        context.fill(px - 1, py - 1, px + 1, py + 1, COLOR_ACCENT_BRIGHT);
      }
    }

    int borderColor = hovered ? COLOR_ACCENT_BRIGHT : COLOR_ACCENT_MEDIUM;
    drawRoundedRectBorder(context, x, y, width, height, radius, borderColor, 1);

    if (text != null && font != null) {
      int textX = x + (width - font.width(text)) / 2;
      int textY = y + (height - font.lineHeight) / 2;

      if (hovered) {
        context.text(font, text, textX, textY, 0x44FFFFFF, false);
      }
      context.text(font, text, textX + 1, textY + 1, 0x88000000, false);
      context.text(font, text, textX, textY, 0xFFFFFFFF, false);
    }
  }

  @Override public void renderHudPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int baseRadius = 10;
    float radiusMultiplier = (float) Math.sin(animationFrame * 0.05) * 0.2f + 1.0f; // Pulsing between 0.8 and 1.2
    int radius = (int) (baseRadius * radiusMultiplier);

    float pulse = (float) Math.sin(animationFrame * 0.06) * 0.15f + 0.35f;
    int glowAlpha = (int) (pulse * 255);
    int glowColor = (glowAlpha << 24) | (COLOR_ACCENT_MEDIUM & 0xFFFFFF);
    fillRoundedRect(context, x - 2, y - 2, width + 4, height + 4, radius + 1, glowColor);

    fillRoundedRect(context, x, y, width, height, radius, COLOR_GLASS);

    drawVerticalGradient(context, x + 2, y + 2, width - 4, height - 4, 0x22FFFFFF, 0x11000000);

    for (int i = 0; i < 5; i++) {
      float particleX = x + ((i * 37 + animationFrame * 2) % width);
      float particleY = y + ((i * 53 + animationFrame) % height);
      context.fill((int) particleX, (int) particleY, (int) particleX + 2, (int) particleY + 2, 0x44FFFFFF);
    }

    drawRoundedRectBorder(context, x, y, width, height, radius, COLOR_ACCENT_BRIGHT, 1);
    drawCornerParticles(context, x, y, width, height);
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

    int radius = 6;

    if (active) {
      float pulse = (float) Math.sin(animationFrame * 0.08) * 0.2f + 0.4f;
      int glowAlpha = (int) (pulse * 255);
      int glowColor = (glowAlpha << 24) | (COLOR_ACCENT_BRIGHT & 0xFFFFFF);
      fillRoundedRect(context, x - 1, y - 1, width + 2, height + 2, radius + 1, glowColor);
    }

    int topColor = active ? 0xDD6A5A9E : 0xDD4A3A7E;
    int bottomColor = active ? 0xDD4A3A7E : 0xDD2A1A5E;
    fillRoundedRect(context, x, y, width, height, radius, topColor);
    drawHorizontalGradient(context, x + 1, y + 1, width - 2, height - 2, topColor, bottomColor);

    fillRoundedRect(context, x + 2, y + 2, width - 4, 2, radius, 0x33FFFFFF);

    if (title != null && font != null) {
      int textX = x + 8;
      int textY = y + (height - font.lineHeight) / 2;

      if (active) {
        context.text(font, title, textX, textY, 0x33FFFFFF, false);
      }
      context.text(font, title, textX + 1, textY + 1, 0x88000000, false);
      context.text(font, title, textX, textY, active ? COLOR_ACCENT_BRIGHT : 0xFFFFFFFF, true);
    }
    drawRoundedRectBorder(context, x, y, width, height, radius, active ? COLOR_ACCENT_BRIGHT : COLOR_ACCENT_MEDIUM, 1);
  }

  @Override public void renderTaskbar(GuiGraphicsExtractor context, int screenWidth, int screenHeight, int height) {
    int y = screenHeight - height;

    drawVerticalGradient(context, 0, y, screenWidth, height, 0xDD3A2A6E, 0xDD1A0A4E);

    float pulse = (float) Math.sin(animationFrame * 0.05) * 0.3f + 0.5f;
    int pulseAlpha = (int) (pulse * 255);
    int pulseColor = (pulseAlpha << 24) | (COLOR_ACCENT_BRIGHT & 0xFFFFFF);
    context.fill(0, y - 1, screenWidth, y, pulseColor);
    context.fill(0, y, screenWidth, y + 2, COLOR_ACCENT_BRIGHT);

    for (int i = 0; i < screenWidth; i += 30) {
      float particleY = y + height / 2 + (float) Math.sin((i + animationFrame * 2) * 0.05) * 5;
      context.fill(i, (int) particleY, i + 2, (int) particleY + 2, COLOR_PARTICLE_MEDIUM);
    }
  }

  @Override public void renderPanel(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int radius = 8;
    fillRoundedRect(context, x, y, width, height, radius, COLOR_GLASS);
    fillRoundedRect(context, x + 1, y + 1, width - 2, 2, radius, 0x33FFFFFF);
    drawRoundedRectBorder(context, x, y, width, height, radius, COLOR_ACCENT_DARK, 1);
  }

  @Override public void renderTextField(GuiGraphicsExtractor context, int x, int y, int width, int height, boolean focused) {
    int radius = 6;

    if (focused) {
      float pulse = (float) Math.sin(animationFrame * 0.1) * 0.15f + 0.35f;
      int glowAlpha = (int) (pulse * 255);
      int glowColor = (glowAlpha << 24) | (COLOR_ACCENT_BRIGHT & 0xFFFFFF);
      fillRoundedRect(context, x - 2, y - 2, width + 4, height + 4, radius + 1, glowColor);
    }

    fillRoundedRect(context, x, y, width, height, radius, 0xFF1A0A3E);

    fillRoundedRect(context, x + 1, y + 1, width - 2, 2, radius, 0x44000000);

    int borderColor = focused ? COLOR_ACCENT_BRIGHT : COLOR_ACCENT_MEDIUM;
    drawRoundedRectBorder(context, x, y, width, height, radius, borderColor, focused ? 2 : 1);

    if (focused && (animationFrame / 15) % 2 == 0) {
      context.fill(x + 5, y + 4, x + 6, y + height - 4, COLOR_ACCENT_BRIGHT);
    }
  }

  @Override public void renderSelection(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int radius = 4;
    fillRoundedRect(context, x, y, width, height, radius, 0x66FFFFFF & COLOR_ACCENT_MEDIUM);
    drawRoundedRectBorder(context, x, y, width, height, radius, COLOR_ACCENT_BRIGHT, 1);
  }

  @Override public void renderProgressBar(GuiGraphicsExtractor context, int x, int y, int width, int height, float progress) {
    int radius = height / 2;
    float clampedProgress = Math.max(0, Math.min(1, progress));
    int fillWidth = (int) (width * clampedProgress);

    fillRoundedRect(context, x, y, width, height, radius, 0xFF1A0A3E);

    if (fillWidth > 0) {
      drawVerticalGradient(context, x, y, fillWidth, height, COLOR_ACCENT_BRIGHT, COLOR_ACCENT_MEDIUM);
      fillRoundedRect(context, x, y, fillWidth, height, radius, 0x00000000);

      float shimmer = (float) Math.sin(animationFrame * 0.15 + progress * 10) * 0.3f + 0.3f;
      int shimmerAlpha = (int) (shimmer * 255);
      int shimmerColor = (shimmerAlpha << 24) | 0xFFFFFF;
      fillRoundedRect(context, x, y, fillWidth, height / 2, radius, shimmerColor);

      for (int i = 0; i < 5; i++) {
        int particleX = x + fillWidth - i * 4;
        int particleY = y + height / 2 + (int) (Math.sin((i + animationFrame) * 0.3) * 3);
        context.fill(particleX, particleY, particleX + 2, particleY + 2, COLOR_PARTICLE_BRIGHT);
      }
    }

    drawRoundedRectBorder(context, x, y, width, height, radius, COLOR_ACCENT_MEDIUM, 1);
  }

  @Override
  public void renderSlider(GuiGraphicsExtractor context, int x, int y, int width, int height, float value, boolean hovered) {
    int radius = 2;
    float clampedValue = Math.max(0, Math.min(1, value));
    int fillWidth = (int) (width * clampedValue);

    fillRoundedRect(context, x, y + height / 2 - 2, width, 4, radius, 0xFF3A2A6E);

    if (fillWidth > 0) {
      fillRoundedRect(context, x, y + height / 2 - 2, fillWidth, 4, radius, COLOR_ACCENT_MEDIUM);
    }

    int thumbSize = height + 2;
    int thumbX = x + fillWidth - thumbSize / 2;
    int thumbY = y - 1;

    if (hovered) {
      float pulse = (float) Math.sin(animationFrame * 0.12) * 0.2f + 0.4f;
      int glowAlpha = (int) (pulse * 255);
      int glowColor = (glowAlpha << 24) | (COLOR_ACCENT_BRIGHT & 0xFFFFFF);
      fillRoundedRect(context, thumbX - 3, thumbY - 3, thumbSize + 6, thumbSize + 6, thumbSize / 2, glowColor);
    }

    fillRoundedRect(context, thumbX, thumbY, thumbSize, thumbSize, thumbSize / 2, COLOR_ACCENT_MEDIUM);
    fillRoundedRect(context, thumbX + 1, thumbY + 1, thumbSize - 2, 2, thumbSize / 2, 0x66FFFFFF);
    drawRoundedRectBorder(context, thumbX, thumbY, thumbSize, thumbSize, thumbSize / 2, COLOR_ACCENT_BRIGHT, 1);
  }

  @Override public void renderCheckbox(GuiGraphicsExtractor context, int x, int y, int size, boolean checked, boolean hovered) {
    int radius = 4;

    if (hovered) {
      fillRoundedRect(context, x - 2, y - 2, size + 4, size + 4, radius + 1, 0x44FFFFFF & COLOR_ACCENT_BRIGHT);
    }

    fillRoundedRect(context, x, y, size, size, radius, 0xFF1A0A3E);

    if (checked) {
      int checkSize = size - 6;
      fillRoundedRect(context, x + 3, y + 3, checkSize, checkSize, radius - 1, COLOR_ACCENT_BRIGHT);

      float pulse = (float) Math.sin(animationFrame * 0.1) * 0.2f + 0.3f;
      int pulseAlpha = (int) (pulse * 255);
      int pulseColor = (pulseAlpha << 24) | 0xFFFFFF;
      fillRoundedRect(context, x + 3, y + 3, checkSize, checkSize, radius - 1, pulseColor);
    }
    int borderColor = hovered ? COLOR_ACCENT_BRIGHT : COLOR_ACCENT_MEDIUM;
    drawRoundedRectBorder(context, x, y, size, size, radius, borderColor, hovered ? 2 : 1);
  }

  @Override public void renderSeparator(GuiGraphicsExtractor context, int x, int y, int width) {
    drawHorizontalGradient(context, x, y, width / 3, 1, 0x00FFFFFF, COLOR_ACCENT_MEDIUM);
    context.fill(x + width / 3, y, x + 2 * width / 3, y + 1, COLOR_ACCENT_MEDIUM);
    drawHorizontalGradient(context, x + 2 * width / 3, y, width / 3, 1, COLOR_ACCENT_MEDIUM, 0x00FFFFFF);

    for (int i = 0; i < width; i += 20) {
      float offset = (float) Math.sin((i + animationFrame * 2) * 0.1) * 3;
      context.fill(x + i, (int) (y + offset), x + i + 2, (int) (y + offset + 2), COLOR_PARTICLE_MEDIUM);
    }
  }

  @Override public void update() {
    animationFrame++;
  }

  private void drawLine(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int color) {
    int dx = Math.abs(x2 - x1);
    int dy = Math.abs(y2 - y1);
    int sx = x1 < x2 ? 1 : -1;
    int sy = y1 < y2 ? 1 : -1;
    int err = dx - dy;
    boolean batchByRow = dx >= dy;
    int runKey = batchByRow ? y1 : x1;
    int runStart = batchByRow ? x1 : y1;
    int runEnd = runStart;

    while (true) {
      int currentKey = batchByRow ? y1 : x1;
      int currentVal = batchByRow ? x1 : y1;
      if (currentKey == runKey) {
        runStart = Math.min(runStart, currentVal);
        runEnd = Math.max(runEnd, currentVal);
      } else {
        if (!flushLineRun(context, batchByRow, runKey, runStart, runEnd, color)) {
          return;
        }
        runKey = currentKey;
        runStart = currentVal;
        runEnd = currentVal;
      }

      if (x1 == x2 && y1 == y2) {
        break;
      }

      int e2 = 2 * err;
      if (e2 > -dy) {
        err -= dy;
        x1 += sx;
      }
      if (e2 < dx) {
        err += dx;
        y1 += sy;
      }
    }
    flushLineRun(context, batchByRow, runKey, runStart, runEnd, color);
  }

  private boolean flushLineRun(GuiGraphicsExtractor context, boolean batchByRow, int key, int start, int end, int color) {
    if (batchByRow) {
      return boundedFill(context, start, key, end + 1, key + 1, color);
    }
    return boundedFill(context, key, start, key + 1, end + 1, color);
  }

  private void drawCornerParticles(GuiGraphicsExtractor context, int x, int y, int width, int height) {
    int[][] corners = {{x, y}, {x + width, y}, {x, y + height}, {x + width, y + height}};

    for (int[] corner : corners) {
      for (int i = 0; i < 3; i++) {
        float angle = (float) (animationFrame * 0.05 + i * Math.PI * 2 / 3);
        int px = (int) (corner[0] + Math.cos(angle) * 8);
        int py = (int) (corner[1] + Math.sin(angle) * 8);
        context.fill(px - 1, py - 1, px + 1, py + 1, COLOR_PARTICLE_MEDIUM);
      }
    }
  }

  private void fillRoundedRect(GuiGraphicsExtractor context, int x, int y, int width, int height, int radius, int color) {
    if (radius <= 0 || width <= 0 || height <= 0) {
      return;
    }

    radius = Math.min(radius, Math.min(width / 2, height / 2));

    context.fill(x + radius, y, x + width - radius, y + height, color);
    context.fill(x, y + radius, x + radius, y + height - radius, color);
    context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);

    fillCircleQuadrant(context, x + radius, y + radius, radius, color, 0);
    fillCircleQuadrant(context, x + width - radius - 1, y + radius, radius, color, 1);
    fillCircleQuadrant(context, x + radius, y + height - radius - 1, radius, color, 2);
    fillCircleQuadrant(context, x + width - radius - 1, y + height - radius - 1, radius, color, 3);
  }

  private void drawRoundedRectBorder(GuiGraphicsExtractor context,
    int x,
    int y,
    int width,
    int height,
    int radius,
    int color,
    int thickness) {
    if (radius <= 0 || width <= 0 || height <= 0) {
      return;
    }

    radius = Math.min(radius, Math.min(width / 2, height / 2));

    for (int i = 0; i < thickness; i++) {
      context.fill(x + radius, y + i, x + width - radius, y + i + 1, color);
      context.fill(x + radius, y + height - i - 1, x + width - radius, y + height - i, color);
      context.fill(x + i, y + radius, x + i + 1, y + height - radius, color);
      context.fill(x + width - i - 1, y + radius, x + width - i, y + height - radius, color);
    }
  }

  private void fillCircleQuadrant(GuiGraphicsExtractor context, int cx, int cy, int radius, int color, int quadrant) {
    for (int dy = -radius; dy <= radius; dy++) {
      for (int dx = -radius; dx <= radius; dx++) {
        if (dx * dx + dy * dy <= radius * radius) {
          boolean inQuadrant = false;
          switch (quadrant) {
            case 0:
              inQuadrant = (dx <= 0 && dy <= 0);
              break;
            case 1:
              inQuadrant = (dx >= 0 && dy <= 0);
              break;
            case 2:
              inQuadrant = (dx <= 0 && dy >= 0);
              break;
            case 3:
              inQuadrant = (dx >= 0 && dy >= 0);
              break;
          }
          if (inQuadrant) {
            context.fill(cx + dx, cy + dy, cx + dx + 1, cy + dy + 1, color);
          }
        }
      }
    }
  }

  private static class Particle {
    float x, y;
    float speedX, speedY;
    int size;
    int trailLength;
    float pulsePhase;
    float pulseSpeed;

    public Particle() {
      pulseSpeed = 0.05f + random.nextFloat() * 0.05f;
    }

    public void reset(int width, int height) {
      x = random.nextInt(Math.max(width, 1));
      y = random.nextInt(Math.max(height, 1));

      float angle = random.nextFloat() * (float) Math.PI * 2;
      float speed = 0.3f + random.nextFloat() * 0.7f;
      speedX = (float) Math.cos(angle) * speed;
      speedY = (float) Math.sin(angle) * speed;

      size = 2 + random.nextInt(3);
      trailLength = 3 + random.nextInt(5);
      pulsePhase = random.nextFloat();
    }

    public void update(int width, int height) {
      x += speedX;
      y += speedY;

      if (x < 0 || x > width) {
        speedX = -speedX;
        x = Math.max(0, Math.min(width, x));
      }
      if (y < 0 || y > height) {
        speedY = -speedY;
        y = Math.max(0, Math.min(height, y));
      }

      pulsePhase = (float) Math.sin(System.currentTimeMillis() * pulseSpeed * 0.001) * 0.3f + 0.7f;
    }
  }
}