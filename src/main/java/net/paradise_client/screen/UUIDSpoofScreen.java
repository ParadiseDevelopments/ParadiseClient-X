package net.paradise_client.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.paradise_client.*;
import net.paradise_client.mod.BungeeSpoofMod;
import net.paradise_client.themes.ThemeManager;

import java.util.UUID;

import static net.paradise_client.Constants.*;

/**
 * Screen for spoofing UUIDs.
 * <p>
 * This screen allows users to spoof their UUID by setting a Bungee username, a fake username, and choosing between
 * premium or cracked UUIDs.
 */
public class UUIDSpoofScreen extends Screen {

  private final BungeeSpoofMod bungeeSpoofMod = ParadiseClient.BUNGEE_SPOOF_MOD;
  private final Screen parentScreen;
  private final MinecraftClient minecraftClient = MinecraftClient.getInstance();

  private String status = "Stand by";
  private TextFieldWidget bungeeUsernameField;
  private TextFieldWidget bungeeFakeUsernameField;
  private TextFieldWidget bungeeTokenField;
  private ButtonWidget premiumButton;
  private int currentHeight;

  public UUIDSpoofScreen(Screen parentScreen) {
    super(Text.literal("UUID Spoof"));
    this.parentScreen = parentScreen;
  }

  @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
    ThemeManager.renderBackground(context, width, height);
    
    int panelWidth = 240;
    int panelHeight = 280;
    int panelX = (width - panelWidth) / 2;
    int panelY = (height - panelHeight) / 2;
    
    ThemeManager.renderPanel(context, panelX, panelY, panelWidth, panelHeight);
    ThemeManager.renderTitleBar(context, panelX, panelY, panelWidth, 25, "UUID Spoofing Tool", textRenderer, true);

    super.render(context, mouseX, mouseY, delta);
    
    int statusColor = status.contains("Error") ? 0xFFFF5555 : 0xFF00D4FF;
    context.drawCenteredTextWithShadow(this.textRenderer, this.status, this.width / 2, panelY + 35, statusColor);
  }

  @Override public void close() {
    minecraftClient.setScreen(parentScreen);
  }

  @Override protected void init() {
    int widgetWidth = 180;
    int panelY = (height - 280) / 2;
    currentHeight = panelY + 50;

    this.bungeeUsernameField =
      addInputField("Username", this.bungeeSpoofMod.usernameReal, value -> this.bungeeSpoofMod.usernameReal = value);
    this.bungeeFakeUsernameField = addInputField("Fake Username",
      this.bungeeSpoofMod.usernameFake,
      value -> this.bungeeSpoofMod.usernameFake = value);
    this.bungeeTokenField =
      addInputField("BungeeGuard Token", this.bungeeSpoofMod.token, value -> this.bungeeSpoofMod.token = value);

    premiumButton = addButton(bungeeSpoofMod.isUUIDOnline ? "Mode: Premium" : "Mode: Cracked",
      widgetWidth,
      button -> togglePremium());
    addButton("Apply Spoof", widgetWidth, button -> spoof());
    addButton("Back", widgetWidth, button -> close());
  }

  @Override public void resize(MinecraftClient client, int width, int height) {
    String username = this.bungeeUsernameField.getText();
    String fakeUsername = this.bungeeFakeUsernameField.getText();
    String token = this.bungeeTokenField.getText();
    super.resize(client, width, height);
    this.bungeeUsernameField.setText(username);
    this.bungeeFakeUsernameField.setText(fakeUsername);
    this.bungeeTokenField.setText(token);
  }

  private TextFieldWidget addInputField(String label,
    String initialValue,
    java.util.function.Consumer<String> onTextChanged) {
    int widgetWidth = 180;
    int tHeight = getNewHeight();

    TextFieldWidget textField = new TextFieldWidget(this.textRenderer,
      this.width / 2 - widgetWidth / 2,
      tHeight,
      widgetWidth,
      20,
      Text.literal(label)) {
        @Override
        public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            ThemeManager.renderTextField(context, getX(), getY(), getWidth(), getHeight(), isFocused());
            int color = isFocused() ? 0xFFFFFFFF : 0xFFAAAAAA;
            context.drawText(MinecraftClient.getInstance().textRenderer, getText(), getX() + 4, getY() + (getHeight() - 8) / 2, color, false);
        }
    };
    textField.setMaxLength(256);
    textField.setText(initialValue);
    textField.setChangedListener(onTextChanged);
    this.addSelectableChild(textField);
    this.addDrawable(textField);

    this.addDrawable(new TextWidget(this.width / 2 - widgetWidth / 2,
      tHeight - 12,
      widgetWidth,
      10,
      Text.literal(label),
      this.textRenderer));

    return textField;
  }

  private ButtonWidget addButton(String label, int width, ButtonWidget.PressAction action) {
    return this.addDrawableChild(new ThemeButton(this.width / 2 - width / 2, getNewHeight() - 5, width, 20, Text.literal(label), action));
  }

  private static class ThemeButton extends ButtonWidget {
    public ThemeButton(int x, int y, int width, int height, Text message, PressAction onPress) {
      super(x, y, width, height, message, onPress, ButtonWidget.DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
      ThemeManager.renderButton(context, getX(), getY(), getWidth(), getHeight(), isHovered(), false, getMessage().getString(), MinecraftClient.getInstance().textRenderer);
    }
  }

  private void togglePremium() {
    bungeeSpoofMod.isUUIDOnline = !bungeeSpoofMod.isUUIDOnline;
    premiumButton.setMessage(Text.literal(bungeeSpoofMod.isUUIDOnline ? "Premium" : "Cracked"));
  }

  private void spoof() {
    if (this.bungeeSpoofMod.isUUIDOnline) {
      try {
        this.bungeeSpoofMod.uuid = Helper.fetchUUID(this.bungeeSpoofMod.usernameFake);
        this.status = "Successfully spoofed premium UUID for \"" + this.bungeeSpoofMod.usernameFake + "\".";
      } catch (Exception e) {
        this.status = "Error fetching UUID. \"" + this.bungeeSpoofMod.usernameFake + "\" may not be premium.";
        LOGGER.error("Error fetching UUID", e);
      }
    } else {
      this.status = "Generating cracked UUID";
      this.bungeeSpoofMod.uuid =
        UUID.nameUUIDFromBytes(("OfflinePlayer:" + bungeeSpoofMod.usernameFake).getBytes());
      this.status = "Successfully spoofed cracked UUID for \"" + this.bungeeSpoofMod.usernameFake + "\".";
    }
    this.bungeeSpoofMod.sessionAccessor.paradiseClient$setUsername(this.bungeeSpoofMod.usernameReal);
    this.bungeeSpoofMod.sessionAccessor.paradiseClient$setUUID(this.bungeeSpoofMod.uuid);
  }

  private int getNewHeight() {
    currentHeight += 35;
    return currentHeight;
  }
}
