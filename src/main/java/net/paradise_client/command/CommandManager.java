package net.paradise_client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.paradise_client.*;
import net.paradise_client.command.impl.*;

import java.util.*;

/**
 * Manages and registers commands for the ParadiseClient Fabric mod.
 */
public class CommandManager {

  public enum CommandCategory {
    EXPLOIT("Exploits", "⚔"),
    COMBAT("Combat", "🗡"),
    MOVEMENT("Movement", "✈"),
    VISUAL("Visual", "👁"),
    PLAYER("Player", "👤"),
    UTILITY("Utility", "🛠"),
    MISC("Miscellaneous", "📦"),
    CRASH("Crashers", "💥");

    private final String displayName;
    private final String icon;

    CommandCategory(String displayName, String icon) {
      this.displayName = displayName;
      this.icon = icon;
    }

    public String getDisplayName() {
      return displayName;
    }

    public String getIcon() {
      return icon;
    }
  }

  public final CommandDispatcher<CommandSource> DISPATCHER = new CommandDispatcher<>();
  public final String prefix = ",";
  private final ArrayList<Command> commands = new ArrayList<>();
  private final MinecraftClient minecraftClient;

  public CommandManager(MinecraftClient minecraftClient) {
    this.minecraftClient = minecraftClient;
  }

  public void init() {
    register(new CopyCommand());
    register(new ExploitCommand());
    register(new ForceOPCommand());
    register(new GriefCommand());
    register(new ScreenShareCommand());
    register(new SpamCommand());
    register(new PlayersCommand());
    register(new ToggleTABCommand());
    register(new PurpurExploitCommand());
    register(new AuthMeVelocityBypassCommand());
    register(new SayCommand());
    register(new ChatSentryCommand());
    register(new ECBCommand());
    register(new SignedVelocityCommand());
    register(new DumpCommand());
    register(new HelpCommand());
    register(new RPCCommand());
  }

  public void register(Command command) {
    this.commands.add(command);
    LiteralArgumentBuilder<CommandSource> builder = Command.literal(command.getName());
    command.build(builder);
    com.mojang.brigadier.tree.LiteralCommandNode<CommandSource> node = DISPATCHER.register(builder);
    command.setNode(node);
    Constants.LOGGER.info("Registered command: {}", command.getName());
  }

  public ArrayList<Command> getCommands() {
    return this.commands;
  }

  public List<Command> getCommandsByCategory(CommandCategory category) {
    List<Command> list = new ArrayList<>();
    for (Command cmd : commands) {
      if (cmd.getCategory() == category) list.add(cmd);
    }
    return list;
  }

  public void dispatch(String message) {
    if (getCommand(message) != null && getCommand(message).isAsync()) {
      Helper.runAsync(() -> dispatchCommand(message));
      return;
    }
    Helper.runAsync(() -> dispatchCommand(message));
  }

  public Command getCommand(String alias) {
    for (Command command : commands) {
      if (command.getName().equals(alias)) return command;
    }
    return null;
  }

  private void dispatchCommand(String message) {
    try {
      DISPATCHER.execute(message, minecraftClient.getNetworkHandler().getCommandSource());
    } catch (CommandSyntaxException e) {
      Helper.printChatMessage("§c" + e.getMessage());
    }
  }
}
