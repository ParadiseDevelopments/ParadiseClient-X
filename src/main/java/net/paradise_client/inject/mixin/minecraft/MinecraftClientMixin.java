package net.paradise_client.inject.mixin.minecraft;

import net.minecraft.client.Minecraft;
import net.paradise_client.Constants;
import net.paradise_client.event.bus.EventBus;
import net.paradise_client.event.impl.minecraft.ClientShutdownEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

/**
 * Mixin class to modify the behavior of the MinecraftClient class.
 * <p>
 * This class injects code into various methods of the MinecraftClient class to customize the window title, handle
 * client shutdown, start a Discord RPC thread, and keep track of the currently displayed screen.
 * </p>
 *
 * @author SpigotRCE
 * @since 1.0
 */
@Mixin(Minecraft.class) public class MinecraftClientMixin {

  /**
   * Injects code to modify the window title returned by the getWindowTitle method.
   * <p>
   * This method sets the window title to include the ParadiseClient edition, version, and the game version.
   * </p>
   *
   * @param callback Callback information for the return value.
   */
  @Inject(method = "updateTitle",
    at = @At(value = "HEAD"),
    cancellable = true) private void getClientTitle(CallbackInfo ci) {
    Minecraft.getInstance().getWindow().setTitle(Constants.windowTitle);
    ci.cancel();
  }

  /**
   * Injects code at the start of the close method to perform additional cleanup.
   * <p>
   * This method shuts down the chat room client when the Minecraft client is closed.
   * </p>
   *
   * @param ci Callback information.
   */
  @Inject(method = "close", at = @At(value = "HEAD")) private void closeHead(CallbackInfo ci) {
    EventBus.fire(EventBus.CLIENT_SHUTDOWN_EVENT_CHANNEL, new ClientShutdownEvent());
  }
}
