package net.paradise_client.config;

import dev.dejvokep.boostedyaml.YamlDocument;
import net.minecraft.client.Minecraft;
import net.paradise_client.Constants;
import net.paradise_client.themes.Theme;

import java.io.*;

public class Config extends ConfigProvider {
  private Theme theme = Theme.LEGACY;

  public Config() {
    super("config.yml", "config-version", new File(Minecraft.getInstance().gameDirectory, "paradiseclient"));
    try {
      load();
    } catch (IOException e) {
      Constants.LOGGER.error("Error loading config file", e);
    }
  }

  @Override public void reload() throws IOException {
    loadTheme();
  }

  private void loadTheme() {
    YamlDocument fileConfig = getFileConfig();
    String themeValue = fileConfig.getString("theme", "LEGACY");

    // Migrate legacy string values to enum
    switch (themeValue) {
      case "ParadiseHack" -> theme = Theme.MATRIX;
      case "ParadiseParticle" -> theme = Theme.PARTICLE;
      case "ParadiseLegacy" -> theme = Theme.LEGACY;
      default -> {
        try {
          theme = Theme.valueOf(themeValue.toUpperCase());
        } catch (IllegalArgumentException e) {
          Constants.LOGGER.error("Invalid theme in config, defaulting to LEGACY", e);
          theme = Theme.LEGACY;
        }
      }
    }

    setTheme(theme);
  }

  public Theme getTheme() {
    return theme;
  }

  public void setTheme(Theme type) {
    this.theme = type;
    getFileConfig().set("theme", type.name());
    try {
      save();
    } catch (IOException e) {
      Constants.LOGGER.error("Error saving config file", e);
    }
  }
}
