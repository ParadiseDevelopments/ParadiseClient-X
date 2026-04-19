package net.paradise_client.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.command.CommandSource;
import net.paradise_client.Helper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class Command {
  protected static final int SINGLE_SUCCESS = com.mojang.brigadier.Command.SINGLE_SUCCESS;

  private final String name;
  private final String description;
  private final boolean async;
  private final CommandManager.CommandCategory category;
  private com.mojang.brigadier.tree.LiteralCommandNode<net.minecraft.command.CommandSource> node;

  public Command(String name, String description, CommandManager.CommandCategory category) {
    this(name, description, category, false);
  }

  public Command(String name, String description, CommandManager.CommandCategory category, boolean async) {
    this.name = name;
    this.description = description;
    this.category = category;
    this.async = async;
  }

  public com.mojang.brigadier.tree.LiteralCommandNode<net.minecraft.command.CommandSource> getNode() {
    return node;
  }

  public void setNode(com.mojang.brigadier.tree.LiteralCommandNode<net.minecraft.command.CommandSource> node) {
    this.node = node;
  }

  public String getName() {
    return name;
  }

  protected static LiteralArgumentBuilder<net.minecraft.command.CommandSource> literal(final String name) {
    return LiteralArgumentBuilder.literal(name);
  }

  protected static <T> RequiredArgumentBuilder<net.minecraft.command.CommandSource, T> argument(final String name,
                                                                                              final ArgumentType<T> type) {
    return RequiredArgumentBuilder.argument(name, type);
  }

  public abstract void build(LiteralArgumentBuilder<net.minecraft.command.CommandSource> root);

  public CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<?> ctx, SuggestionsBuilder builder) {
    String partialName;
    try {
      partialName = ctx.getArgument("user", String.class).toLowerCase();
    } catch (IllegalArgumentException ignored) {
      partialName = "";
    }

    if (partialName.isEmpty()) {
      MinecraftClient.getInstance().getNetworkHandler()
              .getPlayerList()
              .forEach(playerListEntry -> builder.suggest(playerListEntry.getProfile().getName()));
      return builder.buildFuture();
    }

    String finalPartialName = partialName;
    MinecraftClient.getInstance().getNetworkHandler()
            .getPlayerList()
            .stream()
            .map(PlayerListEntry::getProfile)
            .filter(player -> player.getName().toLowerCase().startsWith(finalPartialName.toLowerCase()))
            .forEach(profile -> builder.suggest(profile.getName()));

    return builder.buildFuture();
  }

  public MinecraftClient getMinecraftClient() {
    return MinecraftClient.getInstance();
  }

  public int incompleteCommand(CommandContext<?> context) {
    Helper.printChatMessage("Incomplete command!");
    return 1;
  }

  public String getDescription() {
    return description;
  }

  public boolean isAsync() {
    return async;
  }

  public CommandManager.CommandCategory getCategory() {
    return category;
  }
}
