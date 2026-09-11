package net.paradise_client.inject.mixin.gui.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.paradise_client.ParadiseClient;
import net.paradise_client.mod.BungeeSpoofMod;
import net.paradise_client.screen.UUIDSpoofScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(JoinMultiplayerScreen.class) public abstract class MultiplayerScreenMixin extends Screen {

  @Shadow @Final private HeaderAndFooterLayout layout;
  /**
   * Reference to the BungeeSpoofMod instance for accessing mod data.
   */
  @Unique final BungeeSpoofMod bungeeSpoofMod = ParadiseClient.BUNGEE_SPOOF_MOD;
  /**
   * Button for opening UUID spoofing screen.
   */
  @Unique Button uuidSpoofButton;
  /**
   * Button for toggling BungeeCord spoofing.
   */
  @Unique Button bungeeToggleButton;
  /**
   * Text field for inputting BungeeCord IP.
   */
  @Unique EditBox bungeeClientIPField;
  /**
   * Button for toggling BungeeCord target hostname spoofing.
   */
  @Unique Button bungeeHostnameToggle;
  /**
   * Text field for inputting BungeeCord target hostname.
   */
  @Unique EditBox bungeeHostnameField;
  /**
   * Total footer height consumed while the custom row is present.
   */
  @Unique private static final int CUSTOM_FOOTER_HEIGHT = 68;

  /**
   * Constructor for MultiplayerScreenMixin.
   *
   * @param title The title of the screen.
   */
  protected MultiplayerScreenMixin(Component title) {
    super(title);
  }

  @Inject(method = "init()V", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
  private void paradiseClient$init(CallbackInfo ci, LinearLayout footer) {
    this.uuidSpoofButton = Button.builder(Component.literal("UUIDSpoof"),
        onPress -> Minecraft.getInstance().gui.setScreen(new UUIDSpoofScreen(this))).width(100).build();

    this.bungeeToggleButton = Button.builder(getBungeeButtonText(), onPress -> {
      this.bungeeSpoofMod.isIPForwarding = !bungeeSpoofMod.isIPForwarding;
      this.bungeeToggleButton.setMessage(getBungeeButtonText());
    }).width(100).build();

    this.bungeeHostnameToggle = Button.builder(getBungeeTargetButtonText(), onPress -> {
      this.bungeeSpoofMod.isHostnameForwarding = !bungeeSpoofMod.isHostnameForwarding;
      this.bungeeHostnameToggle.setMessage(getBungeeTargetButtonText());
    }).width(100).build();

    this.bungeeClientIPField = new EditBox(this.font, 74, 20, Component.literal("Bungee IP"));
    this.bungeeClientIPField.setMaxLength(128);
    this.bungeeClientIPField.setValue(bungeeSpoofMod.ip);
    this.bungeeClientIPField.setResponder((text) -> bungeeSpoofMod.ip = this.bungeeClientIPField.getValue());

    this.bungeeHostnameField = new EditBox(this.font, 74, 20, Component.literal("Hostname"));
    this.bungeeHostnameField.setMaxLength(128);
    this.bungeeHostnameField.setValue(bungeeSpoofMod.hostname);
    this.bungeeHostnameField.setResponder((text) -> bungeeSpoofMod.hostname = this.bungeeHostnameField.getValue());

    LinearLayout customRow = (LinearLayout)footer.addChild(LinearLayout.horizontal().spacing(4));
    customRow.addChild(this.uuidSpoofButton);
    customRow.addChild(this.bungeeToggleButton);
    customRow.addChild(this.bungeeHostnameToggle);
    customRow.addChild(this.bungeeClientIPField);
    customRow.addChild(this.bungeeHostnameField);

    this.addRenderableWidget(this.uuidSpoofButton);
    this.addRenderableWidget(this.bungeeToggleButton);
    this.addRenderableWidget(this.bungeeHostnameToggle);
    this.addRenderableWidget(this.bungeeClientIPField);
    this.addRenderableWidget(this.bungeeHostnameField);

    this.layout.setFooterHeight(CUSTOM_FOOTER_HEIGHT);
    this.repositionElements();
  }

  /**
   * Gets the text for the BungeeCord toggle button based on its current state.
   *
   * @return The text to display on the BungeeCord button.
   */
  @Unique private Component getBungeeButtonText() {
    return bungeeSpoofMod.isIPForwarding ? Component.literal("Bungee Enabled") : Component.literal("Bungee Disabled");
  }

  /**
   * Gets the text for the BungeeCord target hostname toggle button based on its current state.
   *
   * @return The text to display on the BungeeCord target hostname button.
   */
  @Unique private Component getBungeeTargetButtonText() {
    return bungeeSpoofMod.isHostnameForwarding ? Component.literal("Hostname Enabled") : Component.literal("Hostname Disabled");
  }
}