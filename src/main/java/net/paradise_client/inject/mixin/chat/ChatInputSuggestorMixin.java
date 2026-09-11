package net.paradise_client.inject.mixin.chat;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.*;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.CommandSuggestions;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.commands.SharedSuggestionProvider;
import net.paradise_client.ParadiseClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;


@Mixin(CommandSuggestions.class) public abstract class ChatInputSuggestorMixin {
  @Shadow @Final EditBox input;
  @Shadow boolean keepSuggestions;
  @Shadow private ParseResults<SharedSuggestionProvider> currentParse;
  @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
  @Shadow private CommandSuggestions.SuggestionsList suggestions;

  /**
   * To suggest tab completion for paradise registered commands.
   *
   * @param ci
   * @param reader
   */
  @Inject(method = "updateCommandInfo",
    at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false),
    cancellable = true) public void onRefresh(CallbackInfo ci, @Local StringReader reader) {
    String prefix = ParadiseClient.COMMAND_MANAGER.prefix;
    int length = prefix.length();

    if (reader.canRead(length) && reader.getString().startsWith(prefix, reader.getCursor())) {
      reader.setCursor(reader.getCursor() + length);

      if (this.currentParse == null) {
        this.currentParse = ParadiseClient.COMMAND_MANAGER.DISPATCHER.parse(reader,
          Minecraft.getInstance().getConnection().getSuggestionsProvider());
      }

      int cursor = input.getCursorPosition();
      if (cursor >= length && (this.suggestions == null || !this.keepSuggestions)) {
        this.pendingSuggestions =
          ParadiseClient.COMMAND_MANAGER.DISPATCHER.getCompletionSuggestions(this.currentParse, cursor);
        this.pendingSuggestions.thenRun(() -> {
          if (this.pendingSuggestions.isDone()) {
            this.showSuggestions(false);
          }
        });
      }

      ci.cancel();
    }
  }

  @Shadow public abstract void showSuggestions(boolean show);
}
