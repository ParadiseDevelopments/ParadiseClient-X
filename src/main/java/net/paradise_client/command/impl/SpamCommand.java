package net.paradise_client.command.impl;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.paradise_client.*;
import net.paradise_client.command.Command;
import net.paradise_client.command.CommandManager;

/**
 * This class represents a command for spamming a specified command in Minecraft.
 *
 * @author SpigotRCE
 * @since 1.5
 */
public class SpamCommand extends Command {
  /**
   * A static boolean flag indicating whether the spamming is currently running.
   */
  public static boolean isRunning = false;

  /**
   * A thread for executing the spamming process.
   */
  private Thread thread;

  /**
   * Constructs a new SpamCommand instance.
   */
  public SpamCommand() {
    super("spam", "Spams the specified command", CommandManager.CommandCategory.MISC);
  }

  /**
   * Builds the command structure using Brigadier's LiteralArgumentBuilder.
   */
  @Override public void build(LiteralArgumentBuilder<CommandSource> root) {
    root.then(literal("stop").executes((context) -> {
      if (!isRunning) {
        Helper.printChatMessage("Spam is not running");
        return SINGLE_SUCCESS;
      }
      isRunning = false;
      return SINGLE_SUCCESS;
    })).then(argument("repeation", IntegerArgumentType.integer()).executes((context) -> {
      Helper.printChatMessage("§4§l" + context.getInput() + " <delay> <command>");
      return SINGLE_SUCCESS;
    }).then(argument("delay", IntegerArgumentType.integer()).executes((context) -> {
      Helper.printChatMessage("§4§l" + context.getInput() + " <command>");
      return SINGLE_SUCCESS;
    }).then(argument("command", StringArgumentType.greedyString()).executes((context) -> {
      int repetition = context.getArgument("repeation", Integer.class);
      int delay = context.getArgument("delay", Integer.class);
      String command = context.getArgument("command", String.class);
      SpamCommand.isRunning = true;
      thread = new Thread(() -> {
        for (int i = 0; i < repetition; i++) {
          if (!SpamCommand.isRunning) {
            this.thread = null;
            return;
          }
          try {
            Thread.sleep(delay);
          } catch (InterruptedException e) {
            Constants.LOGGER.error("Unable to sleep for 1000ms", e);
          }
          getMinecraftClient().getNetworkHandler().sendChatCommand(command);
        }
      });
      thread.start();
      return SINGLE_SUCCESS;
    })))).executes(this::incompleteCommand);
  }
}
