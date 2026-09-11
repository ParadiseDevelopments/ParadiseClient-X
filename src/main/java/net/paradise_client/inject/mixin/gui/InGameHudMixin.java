package net.paradise_client.inject.mixin.gui;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import net.paradise_client.*;
import net.paradise_client.event.bus.EventBus;
import net.paradise_client.event.impl.minecraft.HudStartRenderEvent;
import net.paradise_client.mod.HudMod;
import net.paradise_client.protocol.ProtocolVersion;
import net.paradise_client.themes.ThemeManager;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

import static net.paradise_client.Helper.*;

/**
 * Mixin for the Hud class to inject custom HUD rendering behavior. This mixin is used to display additional
 * information on the HUD.
 *
 * @author SpigotRCE
 * @since 1.0
 */
@Mixin(Hud.class) public abstract class InGameHudMixin {

  /**
   * The Minecraft client instance.
   */
  @Final @Shadow private Minecraft minecraft;
  @Shadow @Final private PlayerTabOverlay tabList;

  /**
   * Injects behavior at the end of the extractRenderState method to add custom HUD information.
   *
   * @param context     The GuiGraphicsExtractor used for rendering.
   * @param tickCounter The DeltaTracker for frame timing.
   * @param ci          Callback information for the method.
   */
  @Inject(method = "extractRenderState", at = @At("TAIL")) public void renderMainHud(GuiGraphicsExtractor context,
    DeltaTracker tickCounter,
    CallbackInfo ci) {
    if (this.minecraft == null) {
      return;
    }

    ArrayList<String> text = new ArrayList<>();

    text.add("§l" + Constants.windowTitle);
    text.add("§7Server §f" +
      ((!Objects.isNull(this.minecraft.getCurrentServer()) && ParadiseClient.HUD_MOD.showServerIP) ?
        this.minecraft.getCurrentServer().ip :
        "Hidden"));

    net.minecraft.network.chat.Component version = Objects.isNull(this.minecraft.getCurrentServer()) ? null :
      this.minecraft.getCurrentServer().version;
    String engine = version == null ? null : version.getString();
    text.add("§7Engine §f" + (Objects.isNull(engine) ? "§8N/A" : engine.replaceAll("§[0-9a-fk-or]", "")));
    int fps = this.minecraft.getFps();
    long lastPacket = System.currentTimeMillis() - ParadiseClient.NETWORK_CONFIGURATION.lastPacket;
    String fpsColor = fps >= 60 ? "§a" : fps >= 30 ? "§e" : "§c";
    String packetColor = lastPacket <= 100 ? "§a" : lastPacket <= 1000 ? "§e" : "§c";
    text.add("§7FPS " + fpsColor + fps);
    text.add("§7Last Packet " + packetColor + lastPacket);
    text.add("§7Protocol §f" +
      ProtocolVersion.getProtocolVersion(ParadiseClient.NETWORK_CONFIGURATION.protocolVersion)
        .getVersionIntroducedIn());
    int playerCount = this.minecraft.getConnection().getOnlinePlayers().size();
    text.add("§7Players §f" + playerCount);

    ParadiseClient.HUD_MOD.hudElements.clear();
    ParadiseClient.HUD_MOD.hudElements.addAll(text);
    EventBus.HUD_START_RENDER_EVENT_CHANNEL.fire(HudStartRenderEvent.INSTANCE);

    int maxWidth = 0;
    for (String s : ParadiseClient.HUD_MOD.hudElements) {
      String cleanText = s.replaceAll("§[0-9a-fk-or]", "");
      maxWidth = Math.max(maxWidth, this.minecraft.font.width(cleanText));
    }
    int lineHeight = this.minecraft.font.lineHeight + 2; // Add line spacing
    int panelHeight = ParadiseClient.HUD_MOD.hudElements.size() * lineHeight;
    int padding = 8;
    ThemeManager.renderHudPanel(context, 3, 3, maxWidth + padding * 2, panelHeight + padding * 2);

    int i = 0;
    for (String s : ParadiseClient.HUD_MOD.hudElements) {
      int textY = 3 + padding + lineHeight * i;

      if (i == 0) {
        String cleanTitle = s.replaceAll("§[0-9a-fk-or]", "");
        int titleWidth = this.minecraft.font.width(cleanTitle);
        int centeredX = -12 + padding + ((maxWidth + padding) - titleWidth) / 2;
        renderTextWithGlow(context, s, centeredX, textY);
      } else {
        renderText(context, s, 3 + padding, textY);
      }
      i++;
    }

    ParadiseClient.NOTIFICATION_MANAGER.drawNotifications(context, this.minecraft.font);
  }

  /**
   * Renders text with a glowing effect
   */
  @Unique private void renderTextWithGlow(GuiGraphicsExtractor ct, String s, int x, int y) {
    String cleanText = s.replaceAll("&([0-9a-fk-or])", "§$1");

    // Simple shadow
    ct.text(this.minecraft.font, cleanText, x + 1, y + 1, 0xFF000000, false);

    // Main text
    ct.text(this.minecraft.font, cleanText, x, y, 0xFFFFFFFF, false);
  }

  /**
   * Renders text with an enhanced shadow effect
   */
  @Unique private void renderText(GuiGraphicsExtractor ct, String s, int x, int y) {
    String cleanText = s.replaceAll("&([0-9a-fk-or])", "§$1");
    ct.text(this.minecraft.font, cleanText, x, y, -1, false);
  }

  @Inject(method = "extractTabList", at = @At("HEAD"), cancellable = true)
  private void renderPlayerList(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
    assert this.minecraft.level != null;
    Scoreboard scoreboard = this.minecraft.level.getScoreboard();
    Objective scoreboardObjective = scoreboard.getDisplayObjective(DisplaySlot.LIST);
    if (!this.minecraft.options.keyPlayerList.isDown() ||
      this.minecraft.isLocalServer() &&
        Objects.requireNonNull(this.minecraft.player).connection.getListedOnlinePlayers().size() <= 1 &&
        scoreboardObjective == null) {
      this.tabList.setVisible(false);
      if (ParadiseClient.HUD_MOD.showPlayerList) {
        this.renderTAB(context, context.guiWidth(), scoreboard, scoreboardObjective);
      }
    } else {
      this.renderTAB(context, context.guiWidth(), scoreboard, scoreboardObjective);
    }
    ci.cancel();
  }

  @Unique
  private void renderTAB(GuiGraphicsExtractor context,
    int scaledWindowWidth,
    Scoreboard scoreboard,
    @Nullable Objective scoreboardObjective) {
    this.tabList.setVisible(true);
    this.tabList.extractRenderState(context, scaledWindowWidth, scoreboard, scoreboardObjective);
  }
}