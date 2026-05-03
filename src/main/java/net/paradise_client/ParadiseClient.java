package net.paradise_client;

import net.fabricmc.api.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.paradise_client.addon.AddonLoader;
import net.paradise_client.command.CommandManager;
import net.paradise_client.config.Config;
import net.paradise_client.discord.DiscordRPCManager;
import net.paradise_client.exploit.ExploitManager;
import net.paradise_client.mod.*;
import net.paradise_client.packet.DummyPacket;
import net.paradise_client.ui.notification.NotificationManager;
import net.paradise_client.ui.screen.ClickGuiScreen;
import org.lwjgl.glfw.*;
import org.lwjgl.system.MemoryStack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * The main class for the ParadiseClient Fabric mod.
 * <p>
 * This class implements the {@link ModInitializer} interface and is responsible for initializing the various components
 * and modules of the mod when the mod is loaded.
 * </p>
 *
 * @author SpigotRCE
 */
public class ParadiseClient implements ModInitializer, ClientModInitializer {

  public static final MinecraftClient MINECRAFT_CLIENT = MinecraftClient.getInstance();
  public static final NetworkConfiguration NETWORK_CONFIGURATION = new NetworkConfiguration();
  public static ParadiseClient INSTANCE;
  public static BungeeSpoofMod BUNGEE_SPOOF_MOD;
  public static MiscMod MISC_MOD;
  public static HudMod HUD_MOD;
  public static ExploitMod EXPLOIT_MOD;
  public static CommandManager COMMAND_MANAGER;
  public static ExploitManager EXPLOIT_MANAGER;
  public static NetworkMod NETWORK_MOD;
  public static NotificationManager NOTIFICATION_MANAGER;
  public static DiscordRPCManager DISCORD_RPC_MANAGER;
  public static Config CONFIG;

  @Override public void onInitializeClient() {
    // check for os.
    // UnsupportedOSUtil.performCheck();
    // then load the rest.
    INSTANCE = this;
    updateIcon();
    DISCORD_RPC_MANAGER = new DiscordRPCManager(MINECRAFT_CLIENT);
    CONFIG = new Config();
    try {
      CONFIG.load();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    registerChannels();
    initializeMods();
    initializeManagers();
    setupKeyBindings();
    checkForUpdates();
    AddonLoader.loadAddons();
  }

  // todo: perhaps move this to a mixin?
  private void updateIcon() {
    MINECRAFT_CLIENT.execute(() -> {
      try (MemoryStack stack = MemoryStack.stackPush()) {
        long windowHandle = MINECRAFT_CLIENT.getWindow().getHandle();

        GLFWImage.Buffer icons = GLFWImage.malloc(2, stack);

        icons.put(0, loadIcon("/assets/paradiseclient/textures/icon/icon_16.png", stack));
        icons.put(1, loadIcon("/assets/paradiseclient/textures/icon/icon_32.png", stack));

        GLFW.glfwSetWindowIcon(windowHandle, icons);
      } catch (Exception e) {
        Constants.LOGGER.error("Failed to set window icon", e);
      }
    });
  }

  private void registerChannels() {
    registerChannel("velocityreport:main");
    registerChannel("purpur:beehive_c2s");
    registerChannel("authmevelocity:main");
    registerChannel("chatsentry:data_sync");
    registerChannel("ecb:channel");
    registerChannel("signedvelocity:main");
  }

  private void initializeMods() {
    BUNGEE_SPOOF_MOD = new BungeeSpoofMod();
    MISC_MOD = new MiscMod();
    HUD_MOD = new HudMod();
    EXPLOIT_MOD = new ExploitMod();
    NETWORK_MOD = new NetworkMod();
  }

  private void initializeManagers() {
    EXPLOIT_MANAGER = new ExploitManager(MINECRAFT_CLIENT);
    EXPLOIT_MANAGER.init();

    COMMAND_MANAGER = new CommandManager(MINECRAFT_CLIENT);
    COMMAND_MANAGER.init();

    NOTIFICATION_MANAGER = new NotificationManager();
  }

  private void setupKeyBindings() {
    KeyBinding keyBinding = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open paradise command",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_COMMA,
      Constants.MOD_NAME));

    KeyBinding clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("Open Paradise Click GUI",
      InputUtil.Type.KEYSYM,
      GLFW.GLFW_KEY_RIGHT_SHIFT,
      Constants.MOD_NAME));

    ClientTickEvents.END_CLIENT_TICK.register(client -> {
      while (keyBinding.wasPressed()) {
        client.setScreen(new ChatScreen(COMMAND_MANAGER.prefix));
      }
      while (clickGuiKey.wasPressed()) {
        client.setScreen(new ClickGuiScreen());
      }
    });
  }

  private void checkForUpdates() {
    new Thread(() -> {
      try {
        String latestVersion = Helper.getLatestReleaseTag();
        if (latestVersion == null) {
          return;
        }

        MISC_MOD.latestVersion = latestVersion;
        MISC_MOD.isClientOutdated = !Objects.equals(latestVersion, Constants.VERSION);
        if (MISC_MOD.isClientOutdated) {
          try {
            Helper.showTrayMessage("ParadiseClient is outdated! Latest version: " + latestVersion,
              TrayIcon.MessageType.WARNING);
          } catch (AWTException e) {
            Constants.LOGGER.error("Failed to show tray message for update", e);
          }
        }

      } catch (IOException e) {
        Constants.LOGGER.error("Error checking for latest release tag", e);
      }
    }).start();
  }

  private GLFWImage loadIcon(String path, MemoryStack stack) throws IOException {
    InputStream input = getClass().getResourceAsStream(path);
    if (input == null) {
      throw new IOException("Icon not found: " + path);
    }

    BufferedImage image = ImageIO.read(input);
    int width = image.getWidth();
    int height = image.getHeight();
    int[] pixelsRaw = new int[width * height];
    image.getRGB(0, 0, width, height, pixelsRaw, 0, width);

    ByteBuffer pixels = stack.malloc(width * height * 4);

    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        int pixel = pixelsRaw[y * width + x];
        pixels.put((byte) ((pixel >> 16) & 0xFF)); // Red
        pixels.put((byte) ((pixel >> 8) & 0xFF));  // Green
        pixels.put((byte) (pixel & 0xFF));         // Blue
        pixels.put((byte) ((pixel >> 24) & 0xFF)); // Alpha
      }
    }
    pixels.flip();

    GLFWImage icon = GLFWImage.malloc(stack);
    icon.set(width, height, pixels);
    return icon;
  }

  public void registerChannel(String channelName) {
    String nameSpace = channelName.split(":")[0];
    String id = channelName.split(":")[1];
    PayloadTypeRegistry.playC2S().register(new CustomPayload.Id<>(Identifier.of(nameSpace, id)), DummyPacket.CODEC);
  }

  @Override public void onInitialize() {
  }
}
