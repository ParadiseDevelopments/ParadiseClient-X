package net.paradise_client.protocol.packet.impl;

import io.netty.buffer.ByteBuf;
import net.paradise_client.protocol.*;
import net.paradise_client.protocol.packet.*;

import java.util.*;

public class ContainerClickC2SPacket extends AbstractPacket {
  private int windowID;
  /**
   * Added with 1.17.1
   */
  private Integer stateID;
  private int slot;
  private int button;
  /**
   * Removed with 1.17
   */
  private Integer actionNumber;
  private ClickType clickType;
  /**
   * Added with 1.17
   */
  private Map<Integer, ItemStack> slots;
  /**
   * Removed with 1.21.5
   */
  private ItemStack carriedItemStack;

  public ContainerClickC2SPacket(int windowID,
    Integer stateID,
    int slot,
    int button,
    Integer actionNumber,
    ClickType clickType,
    Map<Integer, ItemStack> slots,
    ItemStack carriedItemStack) {
    this.windowID = windowID;
    this.stateID = stateID;
    this.slot = slot;
    this.button = button;
    this.actionNumber = actionNumber;
    this.clickType = clickType;
    this.slots = slots;
    this.carriedItemStack = carriedItemStack;
  }

  public ContainerClickC2SPacket() {
  }

  @Override public void handle(AbstractPacketHandler var1) throws Exception {
  }

  @Override public int hashCode() {
    return 0;
  }

  @Override public boolean equals(Object var1) {
    return false;
  }

  @Override public String toString() {
    return "";
  }

  @Override public void write(ByteBuf buf, ProtocolVersion.Direction direction, int protocolVersion) {
    boolean v1_17 = protocolVersion >= ProtocolVersion.V_1_17.getProtocol();
    writeContainerId(windowID, buf, protocolVersion);
    if (protocolVersion >= ProtocolVersion.V_1_17_1.getProtocol()) {
      writeVarInt(stateID, buf);
    }
    buf.writeShort(slot);
    buf.writeByte(button);
    if (!v1_17) {
      buf.writeShort(this.actionNumber != null ? this.actionNumber : -1);
    }
    writeVarInt(this.clickType.ordinal(), buf);
    if (v1_17) {
      AbstractPacket.writeMap(
        this.slots != null ? this.slots : Collections.emptyMap(),
        ByteBuf::writeShort,
        (byteBuf, stack) -> AbstractPacket.writeItemStack(stack, protocolVersion, byteBuf),
        buf
      );
    }
    if (protocolVersion < ProtocolVersion.V_1_21_5.getProtocol()) {
      writeItemStack(this.carriedItemStack, protocolVersion, buf);
    }
  }

  public enum ClickType {
    PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL, UNKNOWN;

    public static final ClickType[] VALUES = values();

    public static ClickType getById(int id) {
      if (id < 0 || id >= (VALUES.length - 1)) {
        return UNKNOWN;
      }

      return VALUES[id];
    }
  }
}
