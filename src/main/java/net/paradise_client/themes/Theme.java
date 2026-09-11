package net.paradise_client.themes;

import net.paradise_client.themes.impl.*;

/**
 * Theme enumeration with renderer instances
 */
public enum Theme {
  MATRIX("Matrix", new MatrixThemeRenderer()),
  PARTICLE("Particle", new ParticleThemeRenderer()),
  LEGACY("Legacy", new LegacyThemeRenderer()),
  MODERN("Modern", new ModernThemeRenderer()),
  GLASSMORPHISM("Glassmorphism", new GlassmorphismThemeRenderer());

  private final String name;
  private final AbstractThemeRenderer renderer;

  Theme(String name, AbstractThemeRenderer renderer) {
    this.name = name;
    this.renderer = renderer;
  }

  public String getName() {
    return name;
  }

  public AbstractThemeRenderer getRenderer() {
    return renderer;
  }
}