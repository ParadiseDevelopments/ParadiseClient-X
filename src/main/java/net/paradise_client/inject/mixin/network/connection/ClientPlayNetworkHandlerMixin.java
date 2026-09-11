package net.paradise_client.inject.mixin.network.connection;

import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.LastSeenMessagesTracker;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.chat.SignedMessageBody;
import net.minecraft.network.chat.SignedMessageChain;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ServerboundChatPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Crypt;
import net.minecraft.world.level.Level;
import net.paradise_client.*;
import net.paradise_client.event.bus.EventBus;
import net.paradise_client.event.impl.chat.*;
import net.paradise_client.inject.accessor.ClientPlayNetworkHandlerAccessor;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.time.Instant;

/**
 * Mixin class to modify the behavior of the ClientPlayNetworkHandler class.
 * <p>
 * This class handles the game join event and updates connection information.
 * </p>
 *
 * @author SpigotRCE
 * @since 2.17
 */
@Mixin(ClientPacketListener.class)
public abstract class ClientPlayNetworkHandlerMixin implements ClientPlayNetworkHandlerAccessor {

  @Shadow private LastSeenMessagesTracker lastSeenMessages;

  @Shadow private SignedMessageChain.Encoder signedMessageEncoder;

  /**
   * Injects code at the end of the onGameJoin method to update connection status and server IP.
   * <p>
   * This method sets the connection status to true and updates the server IP address when the game join packet is
   * received.
   * </p>
   *
   * @param packet The game join packet received from the server.
   * @param info   The callback information.
   */
  @Inject(method = "handleLogin", at = @At("TAIL")) private void onGameJoin(ClientboundLoginPacket packet,
    CallbackInfo info) {
    ParadiseClient.NETWORK_MOD.isConnected = true;

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);

    if (ParadiseClient.MISC_MOD.isClientOutdated) {
      Helper.printChatMessage("&c&lWarning: &4Your client is outdated!");
      Helper.showNotification("Client is outdated!", "Latest: " + ParadiseClient.MISC_MOD.latestVersion);
    }

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[World Info]");
    Helper.printChatMessage("&7 - Dimension: &f" + packet.commonPlayerSpawnInfo().dimension().identifier());
    Helper.printChatMessage("&7 - Hashed Seed: &f" + packet.commonPlayerSpawnInfo().seed());

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[Server Dimensions]");
    for (ResourceKey<Level> dimension : packet.levels()) {
      Helper.printChatMessage("&7 - &f" + dimension.identifier());
    }

    Helper.printChatMessage("");
    Helper.printChatMessage("&b&l[Server Flags]");
    Helper.printChatMessage("&7 - Secure Chat: " + (packet.enforcesSecureChat() ? "&aEnabled" : "&cDisabled"));
    Helper.printChatMessage("&7 - Hardcore Mode: " + (packet.hardcore() ? "&aEnabled" : "&cDisabled"));
    Helper.printChatMessage("&7 - Max Players: &f" + packet.maxPlayers());
    Helper.printChatMessage("&7 - Render Distance: &f" + packet.chunkRadius());
    Helper.printChatMessage("&7 - Simulation Distance: &f" + packet.simulationDistance());

    Helper.printChatMessage("&8&m-----------------------------------------------------", false);
  }

  /**
   * This method fires the {@link ChatPreEvent}.
   *
   * @param content The content entered by the player.
   * @param ci      The callback information.
   */
  @Inject(method = "sendChat", at = @At("HEAD"), cancellable = true)
  private void onSendChatMessageH(String content, CallbackInfo ci) {

    EventBus.ListenerContext<ChatPreEvent> ctx = EventBus.CHAT_PRE_EVENT_CHANNEL.fire(new ChatPreEvent(content));

    if (ctx.isCancelled()) {
      ci.cancel();
      return;
    }

    content = ctx.getEvent().getMessage();

    if (content.startsWith(ParadiseClient.COMMAND_MANAGER.prefix)) {
      ParadiseClient.COMMAND_MANAGER.dispatch(content.substring(1));
      ParadiseClient.MINECRAFT_CLIENT.gui.hud.getChat().addRecentChat(content);
      ci.cancel();
    }
  }

  /**
   * This method fires the {@link ChatPostEvent}.
   *
   * @param content The content entered by the player.
   * @param ci      The callback information.
   */
  @Inject(method = "sendChat", at = @At("TAIL")) private void onSendChatMessageT(String content,
    CallbackInfo ci) {
    EventBus.fire(EventBus.CHAT_POST_EVENT_CHANNEL, new ChatPostEvent(content));
  }

  /**
   * Accessor method to send chat message internally without firing the chat events.
   *
   * @param message The message to be sent.
   */
  @Override public void paradiseClient$sendChatMessage(String message) {
    Instant instant = Instant.now();
    long l = Crypt.SaltSupplier.getLong();
    LastSeenMessagesTracker.Update lastSeenMessages = this.lastSeenMessages.generateAndApplyUpdate();
    MessageSignature messageSignatureData =
      this.signedMessageEncoder.pack(new SignedMessageBody(message, instant, l, lastSeenMessages.lastSeen()));
    Helper.sendPacket(new ServerboundChatPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));
  }
}
