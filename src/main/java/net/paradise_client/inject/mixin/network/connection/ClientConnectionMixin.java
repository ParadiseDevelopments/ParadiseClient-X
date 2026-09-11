package net.paradise_client.inject.mixin.network.connection;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.client.Minecraft;
import net.minecraft.network.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.paradise_client.*;
import net.paradise_client.event.bus.EventBus;
import net.paradise_client.event.impl.network.PhaseChangeEvent;
import net.paradise_client.event.impl.network.packet.incoming.*;
import net.paradise_client.event.impl.network.packet.outgoing.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Mixin class to modify the behavior of the ClientConnection class.
 * <p>
 * This class intercepts packet reading and sending operations to allow for custom packet handling and event triggering.
 * It also updates connection status on disconnection.
 * </p>
 *
 * @author SpigotRCE
 * @since 1.1
 */
@Mixin(Connection.class) public class ClientConnectionMixin {

  /**
   * Injects code at the start of the channelRead0 method to handle incoming packets.
   * <p>
   * This method cancels the processing of the packet if the PacketIncomingPreEvent event returns false.
   * </p>
   *
   * @param channelHandlerContext The Netty channel handler context.
   * @param packet                The incoming packet.
   * @param ci                    Callback information.
   */
  @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
    at = @At("HEAD"),
    cancellable = true) public void channelRead0Head(ChannelHandlerContext channelHandlerContext,
    Packet<?> packet,
    CallbackInfo ci) {
    EventBus.ListenerContext<PacketIncomingPreEvent> ctx =
      EventBus.fire(EventBus.PACKET_INCOMING_PRE_EVENT_CHANNEL, new PacketIncomingPreEvent(packet));
    if (ctx.isCancelled()) {
      ci.cancel();
    }

    ParadiseClient.NETWORK_CONFIGURATION.lastPacket = System.currentTimeMillis();

    if (packet instanceof ClientboundCommandSuggestionsPacket suggestionsS2CPacket) {
      if (suggestionsS2CPacket.id() != ParadiseClient.MISC_MOD.requestId) {
        return;
      }
      if (!ParadiseClient.MISC_MOD.isDumping) {
        return;
      }
      Helper.printChatMessage("Command suggestions received! Dumping");
      Helper.printChatMessage("Debug request id: " + suggestionsS2CPacket.id());
      List<ClientboundCommandSuggestionsPacket.Entry> suggestions = suggestionsS2CPacket.suggestions();

      new Thread(() -> {
        try {
          suggestions.forEach(suggestion -> {
            Minecraft.getInstance().getConnection().sendCommand("ip " + suggestion.text());
          });
        } catch (Exception ignored) {
        }
      }).start();
    }

    if (packet instanceof ClientboundResourcePackPushPacket resourcePackSendS2CPacket) {
      String url = resourcePackSendS2CPacket.url();
      Helper.printChatMessage(Component.nullToEmpty("Server resource pack url: " + url));
    }

  }

  /**
   * Injects code at the end of the channelRead0 method to handle post-processing of incoming packets.
   * <p>
   * This method triggers the PacketIncomingPostEvent event after the packet has been processed.
   * </p>
   *
   * @param channelHandlerContext The Netty channel handler context.
   * @param packet                The incoming packet.
   * @param ci                    Callback information.
   */
  @Inject(method = "channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V",
    at = @At("TAIL")) public void channelRead0Tail(ChannelHandlerContext channelHandlerContext,
    Packet<?> packet,
    CallbackInfo ci) {
    EventBus.fire(EventBus.PACKET_INCOMING_POST_EVENT_CHANNEL, new PacketIncomingPostEvent(packet));
  }

  /**
   * Injects code at the start of the sendImmediately method to handle outgoing packets.
   * <p>
   * This method cancels the sending of the packet if the PacketOutgoingPreEvent event returns false.
   * </p>
   *
   * @param packet    The outgoing packet.
   * @param callbacks The packet callbacks.
   * @param flush     Whether to flush the packet.
   * @param ci        Callback information.
   */
  @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
  public void sendImmediatelyHead(Packet<?> packet, ChannelFutureListener callbacks, boolean flush, CallbackInfo ci) {
    EventBus.ListenerContext<PacketOutgoingPreEvent> ctx =
      EventBus.fire(EventBus.PACKET_OUTGOING_PRE_EVENT_CHANNEL, new PacketOutgoingPreEvent(packet));
    if (ctx.isCancelled()) {
      ci.cancel();
    }
  }

  /**
   * Injects code at the end of the sendImmediately method to handle post-processing of outgoing packets.
   * <p>
   * This method triggers the PacketOutgoingPostEvent event after the packet has been sent.
   * </p>
   *
   * @param packet    The outgoing packet.
   * @param callbacks The packet callbacks.
   * @param flush     Whether to flush the packet.
   * @param ci        Callback information.
   */
  @Inject(method = "sendPacket", at = @At("TAIL")) public void sendImmediatelyTail(Packet<?> packet,
    ChannelFutureListener callbacks,
    boolean flush,
    CallbackInfo ci) throws InvocationTargetException, IllegalAccessException {
    EventBus.fire(EventBus.PACKET_OUTGOING_POST_EVENT_CHANNEL, new PacketOutgoingPostEvent(packet));
  }

  /**
   * Injects code at the start of the disconnect method to handle disconnection events.
   * <p>
   * This method updates the connection status in the network mod to indicate disconnection.
   * </p>
   *
   * @param disconnectionInfo The disconnection information.
   * @param ci                Callback information.
   */
  @Inject(method = "disconnect(Lnet/minecraft/network/DisconnectionDetails;)V", at = @At("HEAD"))
  public void disconnectHead(DisconnectionDetails disconnectionInfo, CallbackInfo ci) {
    ParadiseClient.NETWORK_MOD.isConnected = false;
  }

  @Inject(method = "initiateServerboundPlayConnection(Ljava/lang/String;ILnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/ProtocolInfo;Lnet/minecraft/network/ClientboundPacketListener;Z)V",
    at = @At("HEAD"))
  public <S extends ServerboundPacketListener, C extends ClientboundPacketListener> void connect(String address,
    int port,
    ProtocolInfo<S> outboundState,
    ProtocolInfo<C> inboundState,
    C prePlayStateListener,
    boolean transfer,
    CallbackInfo ci) {
    ParadiseClient.NETWORK_CONFIGURATION.phase = ConnectionProtocol.HANDSHAKING;
  }

  @Inject(method = "setupInboundProtocol", at = @At("HEAD"))
  public <T extends PacketListener> void onTransitionInbound(ProtocolInfo<T> state, T packetListener, CallbackInfo ci)
    throws InvocationTargetException, IllegalAccessException {
    ParadiseClient.NETWORK_CONFIGURATION.set(state.id(),
      state.flow(),
      ParadiseClient.NETWORK_CONFIGURATION.protocolVersion);
    EventBus.fire(EventBus.PHASE_CHANGE_EVENT_CHANNEL, new PhaseChangeEvent(state.id()));
  }
}
