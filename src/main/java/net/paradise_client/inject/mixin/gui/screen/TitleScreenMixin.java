package net.paradise_client.inject.mixin.gui.screen;

import net.paradise_client.themes.Theme;
import net.paradise_client.themes.ThemeManager;
import net.minecraft.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.AccessibilityOptionsScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.paradise_client.Constants;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.realmsclient.RealmsMainScreen;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {

  @Shadow private boolean fading;
  @Shadow private long fadeInStart;

  private final Minecraft client = Minecraft.getInstance();
  private final Identifier logoImage = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/icon/icon.png");
  private final Identifier optionsIcon = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/icon/options.png");
  private final Identifier accessibilityIcon = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/icon/accessibility.png");
  private final Identifier realmsIcon = Identifier.fromNamespaceAndPath(Constants.MOD_ID, "textures/icon/realms.png");

  private Button quitButton;
  private Button optionsButton;
  private Button accessibilityButton;
  private Button realmsButton;

  protected TitleScreenMixin(Component title) {
    super(title);
  }

  @Inject(method = "init()V", at = @At("TAIL"))
  private void initParadise(CallbackInfo ci) {
    List<GuiEventListener> toRemove = new ArrayList<>();
    for (GuiEventListener element : this.children()) {
      if (element instanceof Button) {
        toRemove.add(element);
      }
    }
    for (GuiEventListener element : toRemove) {
      this.removeWidget(element);
    }

    quitButton = null;

    int buttonWidth = 200;
    int buttonHeight = 20;
    int spacing = 6;

    int logoHeight = 100;
    int logoY = this.height / 2 - 120;
    int titleY = logoY + logoHeight + 10;
    int titleHeight = this.font.lineHeight;
    int centerY = titleY + titleHeight + 20;
    int centerX = this.width / 2 - buttonWidth / 2;

    this.addRenderableWidget(Button.builder(Component.literal("Singleplayer"),
                    b -> minecraft.gui.setScreen(new SelectWorldScreen(this)))
            .bounds(centerX, centerY, buttonWidth, buttonHeight).build());

    this.addRenderableWidget(Button.builder(Component.literal("Multiplayer"),
                    b -> minecraft.gui.setScreen(new JoinMultiplayerScreen(this)))
            .bounds(centerX, centerY + buttonHeight + spacing, buttonWidth, buttonHeight).build());

    this.addRenderableWidget(Button.builder(Component.literal("Website"),
                    b -> Util.getPlatform().openUri("https://paradise-client.net"))
            .bounds(centerX, centerY + 2 * (buttonHeight + spacing), buttonWidth, buttonHeight).build());

    int quitButtonWidth = 60;
    int quitButtonHeight = 20;
    int quitX = this.width - quitButtonWidth - 18;
    int quitY = 18;
    quitButton = Button.builder(Component.literal("Quit"),
                    b -> {
                      minecraft.stop();
                    })
            .bounds(quitX, quitY, quitButtonWidth, quitButtonHeight)
            .build();
    this.addRenderableWidget(quitButton);

    Font font = this.font;
    int iconSize = 20;
    int lastMainY = centerY + 2 * (buttonHeight + spacing);
    int bottomY = lastMainY + buttonHeight + 20;
    int toolbarSpacing = 4;
    int toolbarStartX = (this.width - (3 * iconSize + 2 * toolbarSpacing)) / 2;

    optionsButton = Button.builder(Component.literal("Options"),
                    b -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options, false)))
            .bounds(toolbarStartX, bottomY, iconSize, iconSize).build();
    this.addRenderableWidget(optionsButton);

    accessibilityButton = Button.builder(Component.literal("Accessibility Settings"),
                    b -> minecraft.gui.setScreen(new AccessibilityOptionsScreen(this, minecraft.options)))
            .bounds(toolbarStartX + iconSize + toolbarSpacing, bottomY, iconSize, iconSize).build();
    this.addRenderableWidget(accessibilityButton);

    realmsButton = Button.builder(Component.literal("Realms"),
                    b -> minecraft.gui.setScreen(new RealmsMainScreen(this)))
            .bounds(toolbarStartX + 2 * (iconSize + toolbarSpacing), bottomY, iconSize, iconSize).build();
    this.addRenderableWidget(realmsButton);

    int themeButtonWidth = 100;
    int themeButtonHeight = 20;
    int themeX = 18;
    int themeY = 18;
    this.addRenderableWidget(Button.builder(Component.literal("Change Theme"),
                    b -> {
                      Theme[] themes = Theme.values();
                      int currentIndex = Arrays.asList(themes).indexOf(ThemeManager.getTheme());
                      int nextIndex = (currentIndex + 1) % themes.length;
                      ThemeManager.setTheme(themes[nextIndex]);
                    })
            .bounds(themeX, themeY, themeButtonWidth, themeButtonHeight).build());
  }

  @Inject(method = "extractRenderState", at = @At("HEAD"), cancellable = true)
  private void renderParadise(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
    if (this.fadeInStart == 0L && this.fading) {
      this.fadeInStart = Util.getMillis();
    }

    if (this.fading) {
      float t = (float)(Util.getMillis() - this.fadeInStart) / 2000.0F;
      if (t > 1.0F) {
        this.fading = false;
      } else {
        t = Mth.clamp(t, 0.0F, 1.0F);
      }
    }

    ThemeManager.update();
    ThemeManager.renderBackground(context, this.width, this.height);

    int logoWidth = 100;
    int logoHeight = 100;
    int logoX = (this.width - logoWidth) / 2;
    int logoY = this.height / 2 - 120;
    context.blit(RenderPipelines.GUI_TEXTURED, logoImage, logoX, logoY, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight, logoWidth, logoHeight);

    Font font = this.font;

    for (GuiEventListener element : this.children()) {
      if (element instanceof Button button) {
        boolean hovered = button.isMouseOver(mouseX, mouseY);
        boolean pressed = hovered && (GLFW.glfwGetMouseButton(minecraft.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_1) == GLFW.GLFW_PRESS);
        String text = button.getMessage().getString();
        if (button == optionsButton || button == accessibilityButton || button == realmsButton) {
          text = "";
        }
        ThemeManager.renderButton(context,
                button.getX(),
                button.getY(),
                button.getWidth(),
                button.getHeight(),
                hovered,
                pressed,
                text,
                font);

        if (button == optionsButton) {
          int iconX = button.getX() + (button.getWidth() - 16) / 2;
          int iconY = button.getY() + (button.getHeight() - 16) / 2;
          context.blit(RenderPipelines.GUI_TEXTURED, optionsIcon, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16);
        } else if (button == accessibilityButton) {
          int iconX = button.getX() + (button.getWidth() - 16) / 2;
          int iconY = button.getY() + (button.getHeight() - 16) / 2;
          context.blit(RenderPipelines.GUI_TEXTURED, accessibilityIcon, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16);
        } else if (button == realmsButton) {
          int iconX = button.getX() + (button.getWidth() - 16) / 2;
          int iconY = button.getY() + (button.getHeight() - 16) / 2;
          context.blit(RenderPipelines.GUI_TEXTURED, realmsIcon, iconX, iconY, 0.0F, 0.0F, 16, 16, 16, 16, 16, 16);
        }
      }
    }

    String versionText = "ParadiseClient " + Constants.VERSION;
    context.text(font, versionText, 8, this.height - font.lineHeight - 8, 0x88FFFFFF, false);

    String disclaimer = "Not affiliated with Mojang or Microsoft. Do not distribute!";
    int disclaimerWidth = font.width(disclaimer);
    context.text(font, disclaimer, this.width - disclaimerWidth - 8,
            this.height - font.lineHeight - 8, 0x88FFFFFF, false);

    ci.cancel();
  }
}