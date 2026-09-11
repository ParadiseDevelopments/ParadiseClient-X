package net.paradise_client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.paradise_client.*;
import net.paradise_client.mod.BungeeSpoofMod;

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
  private final Minecraft minecraftClient = Minecraft.getInstance();

  private String status;
  private EditBox bungeeUsernameField;
  private EditBox bungeeFakeUsernameField;
  private EditBox bungeeTokenField;
  private Button premiumButton;
  private int currentHeight;

  public UUIDSpoofScreen(Screen parentScreen) {
    super(Component.literal("UUID Spoof"));
    this.parentScreen = parentScreen;
  }

  @Override public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
    this.extractBackground(context, mouseX, mouseY, delta);
    super.extractRenderState(context, mouseX, mouseY, delta);
    context.centeredText(this.font, this.status, this.width / 2, 20, 0xFFFFFF);
  }

  @Override public void onClose() {
    minecraftClient.gui.setScreen(parentScreen);
  }

  @Override protected void init() {
    status = "Stand by";
    int widgetWidth = 200;
    int widgetXOffset = widgetWidth / 2;
    currentHeight = this.height / 2 - 90;

    this.bungeeUsernameField =
      addInputField("Username", this.bungeeSpoofMod.usernameReal, value -> this.bungeeSpoofMod.usernameReal = value);
    this.bungeeFakeUsernameField = addInputField("FakeUsername",
      this.bungeeSpoofMod.usernameFake,
      value -> this.bungeeSpoofMod.usernameFake = value);
    this.bungeeTokenField =
      addInputField("BungeeGuard Token", this.bungeeSpoofMod.token, value -> this.bungeeSpoofMod.token = value);

    premiumButton = addButton(bungeeSpoofMod.isUUIDOnline ? "Premium" : "Cracked",
      widgetWidth,
      widgetXOffset,
      button -> togglePremium());
    addButton("Spoof", widgetWidth, widgetXOffset, button -> spoof());
    addButton("Exit", widgetWidth, widgetXOffset, button -> onClose());
  }

  @Override public void resize(int width, int height) {
    String username = this.bungeeUsernameField.getValue();
    String fakeUsername = this.bungeeFakeUsernameField.getValue();
    String token = this.bungeeTokenField.getValue();
    this.init(width, height);
    this.bungeeUsernameField.setValue(username);
    this.bungeeFakeUsernameField.setValue(fakeUsername);
    this.bungeeTokenField.setValue(token);
  }

  private EditBox addInputField(String label,
    String initialValue,
    java.util.function.Consumer<String> onTextChanged) {
    int widgetWidth = 200;
    int widgetXOffset = widgetWidth / 2;
    int tHeight = getNewHeight();

    EditBox textField = new EditBox(this.font,
      this.width / 2 - widgetXOffset,
      tHeight,
      widgetWidth,
      20,
      Component.literal(label));
    textField.setMaxLength(256);
    textField.setValue(initialValue);
    textField.setResponder(onTextChanged);
    this.addWidget(textField);
    this.addRenderableOnly(textField);

    this.addRenderableOnly(new StringWidget(this.width / 2 - widgetXOffset,
      tHeight - 15,
      widgetWidth,
      20,
      Component.literal(label),
      this.font));

    return textField;
  }

  private Button addButton(String label, int width, int xOffset, Button.OnPress action) {
    return this.addRenderableWidget(Button.builder(Component.literal(label), action)
      .bounds(this.width / 2 - xOffset, getNewHeight() - 10, width, 20)
      .build());
  }

  private void togglePremium() {
    bungeeSpoofMod.isUUIDOnline = !bungeeSpoofMod.isUUIDOnline;
    premiumButton.setMessage(Component.literal(bungeeSpoofMod.isUUIDOnline ? "Premium" : "Cracked"));
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
