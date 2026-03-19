package net.paradise_client.protocol.packet;

import com.google.common.base.*;
import com.mojang.authlib.properties.Property;
import com.viaversion.nbt.io.NBTIO;
import com.viaversion.nbt.tag.CompoundTag;
import io.netty.buffer.*;
import net.paradise_client.protocol.*;
import net.paradise_client.protocol.ItemStack;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;

/**
 * Minimal implementation of BungeeCord protocol.
 *
 * @author SpigotRCE
 */
public abstract class AbstractPacket {
  private boolean encoded = false;
  private ByteBuf encodedBuf;

  public static <T> T readStringMapKey(ByteBuf buf, Map<String, T> map) {
    String string = readString(buf);
    T result = (T) map.get(string);
    Preconditions.checkArgument(result != null, "Unknown string key %s", string);
    return result;
  }

  public static String readString(ByteBuf buf) {
    return readString(buf, 32767);
  }

  public static String readString(ByteBuf buf, int maxLen) {
    int len = readVarInt(buf);
    if (len > maxLen * 3) {
      throw new BadPacketException("Cannot receive string longer than " + maxLen * 3 + " (got " + len + " bytes)");
    } else {
      String s = buf.toString(buf.readerIndex(), len, StandardCharsets.UTF_8);
      buf.readerIndex(buf.readerIndex() + len);
      if (s.length() > maxLen) {
        throw new BadPacketException("Cannot receive string longer than " +
          maxLen +
          " (got " +
          s.length() +
          " characters)");
      } else {
        return s;
      }
    }
  }

  public static int readVarInt(ByteBuf input) {
    return readVarInt(input, 5);
  }

  public static int readVarInt(ByteBuf input, int maxBytes) {
    int out = 0;
    int bytes = 0;

    byte in;
    do {
      in = input.readByte();
      out |= (in & 127) << bytes++ * 7;
      if (bytes > maxBytes) {
        throw new BadPacketException("VarInt too big (max " + maxBytes + ")");
      }
    } while ((in & 128) == 128);

    return out;
  }

  public static void writeArray(byte[] b, ByteBuf buf) {
    if (b.length > 32767) {
      throw new BadPacketException("Cannot send byte array longer than Short.MAX_VALUE (got " + b.length + " bytes)");
    } else {
      writeVarInt(b.length, buf);
      buf.writeBytes(b);
    }
  }

  public static void writeVarInt(int value, ByteBuf output) {
    do {
      int part = value & 127;
      value >>>= 7;
      if (value != 0) {
        part |= 128;
      }

      output.writeByte(part);
    } while (value != 0);

  }

  public static byte[] toArray(ByteBuf buf) {
    byte[] ret = new byte[buf.readableBytes()];
    buf.readBytes(ret);
    return ret;
  }

  public static byte[] readArray(ByteBuf buf) {
    return readArray(buf, buf.readableBytes());
  }

  public static byte[] readArray(ByteBuf buf, int limit) {
    int len = readVarInt(buf);
    if (len > limit) {
      throw new BadPacketException("Cannot receive byte array longer than " + limit + " (got " + len + " bytes)");
    } else {
      byte[] ret = new byte[len];
      buf.readBytes(ret);
      return ret;
    }
  }

  public static int[] readVarIntArray(ByteBuf buf) {
    int len = readVarInt(buf);
    int[] ret = new int[len];

    for (int i = 0; i < len; ++i) {
      ret[i] = readVarInt(buf);
    }

    return ret;
  }

  public static void writeStringArray(List<String> s, ByteBuf buf) {
    writeVarInt(s.size(), buf);

    for (String str : s) {
      writeString(str, buf);
    }

  }

  public static void writeString(String s, ByteBuf buf) {
    writeString(s, buf, 32767);
  }

  public static void writeString(String s, ByteBuf buf, int maxLength) {
    if (s.length() > maxLength) {
      throw new BadPacketException("Cannot send string longer than " +
        maxLength +
        " (got " +
        s.length() +
        " characters)");
    } else {
      byte[] b = s.getBytes(StandardCharsets.UTF_8);
      if (b.length > maxLength * 3) {
        throw new BadPacketException("Cannot send string longer than " +
          maxLength * 3 +
          " (got " +
          b.length +
          " bytes)");
      } else {
        writeVarInt(b.length, buf);
        buf.writeBytes(b);
      }
    }
  }

  public static List<String> readStringArray(ByteBuf buf) {
    int len = readVarInt(buf);
    List<String> ret = new ArrayList<>(len);

    for (int i = 0; i < len; ++i) {
      ret.add(readString(buf));
    }

    return ret;
  }

  public static void setVarInt(int value, ByteBuf output, int pos, int len) {
    switch (len) {
      case 1:
        output.setByte(pos, value);
        break;
      case 2:
        output.setShort(pos, (value & 127 | 128) << 8 | value >>> 7 & 127);
        break;
      case 3:
        output.setMedium(pos, (value & 127 | 128) << 16 | (value >>> 7 & 127 | 128) << 8 | value >>> 14 & 127);
        break;
      case 4:
        output.setInt(pos,
          (value & 127 | 128) << 24 |
            (value >>> 7 & 127 | 128) << 16 |
            (value >>> 14 & 127 | 128) << 8 |
            value >>> 21 & 127);
        break;
      case 5:
        output.setInt(pos,
          (value & 127 | 128) << 24 |
            (value >>> 7 & 127 | 128) << 16 |
            (value >>> 14 & 127 | 128) << 8 |
            value >>> 21 & 127 |
            128);
        output.setByte(pos + 4, value >>> 28);
        break;
      default:
        throw new IllegalArgumentException("Invalid varint len: " + len);
    }

  }

  public static int readVarShort(ByteBuf buf) {
    int low = buf.readUnsignedShort();
    int high = 0;
    if ((low & '耀') != 0) {
      low &= 32767;
      high = buf.readUnsignedByte();
    }

    return (high & 255) << 15 | low;
  }

  public static void writeVarShort(ByteBuf buf, int toWrite) {
    int low = toWrite & 32767;
    int high = (toWrite & 8355840) >> 15;
    if (high != 0) {
      low |= 32768;
    }

    buf.writeShort(low);
    if (high != 0) {
      buf.writeByte(high);
    }

  }

  public static void writeUUID(UUID value, ByteBuf output) {
    output.writeLong(value.getMostSignificantBits());
    output.writeLong(value.getLeastSignificantBits());
  }

  public static UUID readUUID(ByteBuf input) {
    return new UUID(input.readLong(), input.readLong());
  }

  public static void writeProperties(Property[] properties, ByteBuf buf) {
    if (properties == null) {
      writeVarInt(0, buf);
    } else {
      writeVarInt(properties.length, buf);

      for (Property prop : properties) {
        writeString(prop.name(), buf);
        writeString(prop.value(), buf);
        if (prop.signature() != null) {
          buf.writeBoolean(true);
          writeString(prop.signature(), buf);
        } else {
          buf.writeBoolean(false);
        }
      }

    }
  }

  public static Property[] readProperties(ByteBuf buf) {
    Property[] properties = new Property[readVarInt(buf)];

    for (int j = 0; j < properties.length; ++j) {
      String name = readString(buf);
      String value = readString(buf);
      if (buf.readBoolean()) {
        properties[j] = new Property(name, value, readString(buf));
      } else {
        properties[j] = new Property(name, value);
      }
    }

    return properties;
  }

  public static CompoundTag readTag(ByteBuf input) {
    try {
      int readerIndex = input.readerIndex();
      byte tagId = input.readByte();

      if (tagId == 0) {
        return null;
      }

      input.readerIndex(readerIndex);

      ByteBufInputStream byteBufInputStream = new ByteBufInputStream(input);

      return NBTIO.readTag(
        byteBufInputStream,
        com.viaversion.nbt.limiter.TagLimiter.noop(),
        false,
        CompoundTag.class
      );
    } catch (IOException e) {
      throw new BadPacketException("Failed to read NBT tag", e);
    }
  }

  public static void writeTag(CompoundTag tag, ByteBuf output) {
    try {
      if (tag == null) {
        output.writeByte(0);
        return;
      }
      ByteBufOutputStream byteBufOutputStream = new ByteBufOutputStream(output);
      NBTIO.writeTag(byteBufOutputStream, tag, false);
    } catch (IOException e) {
      throw new BadPacketException("Failed to write NBT tag", e);
    }
  }

  public static void writeContainerId(int containerId, ByteBuf buf, int protocolVersion) {
    if (protocolVersion >= ProtocolVersion.V_1_21_2.getProtocol()) {
      writeVarInt(containerId,buf);
    } else {
      buf.writeByte(containerId);
    }
  }

  public static int readContainerId(ByteBuf buf, int protocolVersion) {
    if (protocolVersion >= ProtocolVersion.V_1_21_2.getProtocol()) {
      return readVarInt(buf);
    }
    return buf.readUnsignedByte();
  }

  public static void writeItemStack(ItemStack stack, int protocolVersion, ByteBuf buf) {
    ItemStack finalStack = stack == null ? ItemStack.EMPTY : stack;
    if (protocolVersion < ProtocolVersion.V_1_13_2.getProtocol()) {
      int id = finalStack.isEmpty() ? -1 : finalStack.getId();
      buf.writeShort(id);
      if (id != -1) {
        buf.writeByte(finalStack.getCount());
        if (protocolVersion < ProtocolVersion.V_1_13.getProtocol()) {
          // legacy data - probably damage value, but I'm not sure because there was no doc in packet events
          buf.writeByte(0);
        }
        writeTag(finalStack.getTag(), buf);
      }
    } else if (finalStack.isEmpty()) {
      buf.writeBoolean(false);
    } else {
      buf.writeBoolean(true);
      writeVarInt(finalStack.getId(), buf);
      buf.writeByte(finalStack.getCount());
      writeTag(finalStack.getTag(), buf);
    }
  }

  public static <K, V> void writeMap(Map<K, V> map, Writer<K> keyConsumer, Writer<V> valueConsumer, ByteBuf buf) {
    writeVarInt(map.size(), buf);
    for (Map.Entry<K, V> entry : map.entrySet()) {
      K key = entry.getKey();
      V value = entry.getValue();
      keyConsumer.accept(buf, key);
      valueConsumer.accept(buf, value);
    }
  }

  public static <E extends Enum<E>> void writeEnumSet(EnumSet<E> enumset, Class<E> oclass, ByteBuf buf) {
    E[] enums = (E[]) (oclass.getEnumConstants());
    BitSet bits = new BitSet(enums.length);

    for (int i = 0; i < enums.length; ++i) {
      bits.set(i, enumset.contains(enums[i]));
    }

    writeFixedBitSet(bits, enums.length, buf);
  }

  public static void writeFixedBitSet(BitSet bits, int size, ByteBuf buf) {
    if (bits.length() > size) {
      throw new BadPacketException("BitSet too large (expected " + size + " got " + bits.size() + ")");
    } else {
      buf.writeBytes(Arrays.copyOf(bits.toByteArray(), size + 7 >> 3));
    }
  }

  public static <E extends Enum<E>> EnumSet<E> readEnumSet(Class<E> oclass, ByteBuf buf) {
    E[] enums = (E[]) (oclass.getEnumConstants());
    BitSet bits = readFixedBitSet(enums.length, buf);
    EnumSet<E> set = EnumSet.noneOf(oclass);

    for (int i = 0; i < enums.length; ++i) {
      if (bits.get(i)) {
        set.add(enums[i]);
      }
    }

    return set;
  }

  public static BitSet readFixedBitSet(int i, ByteBuf buf) {
    byte[] bits = new byte[i + 7 >> 3];
    buf.readBytes(bits);
    return BitSet.valueOf(bits);
  }

  public <T> T readNullable(Function<ByteBuf, T> reader, ByteBuf buf) {
    return (T) (buf.readBoolean() ? reader.apply(buf) : null);
  }

  public <T> void writeNullable(T t0, BiConsumer<T, ByteBuf> writer, ByteBuf buf) {
    if (t0 != null) {
      buf.writeBoolean(true);
      writer.accept(t0, buf);
    } else {
      buf.writeBoolean(false);
    }

  }

  public void read(ByteBuf buf, Protocol protocol, ProtocolVersion.Direction direction, int protocolVersion) {
    this.read(buf, direction, protocolVersion);
  }

  public void read(ByteBuf buf, ProtocolVersion.Direction direction, int protocolVersion) {
    this.read(buf);
  }

  public void read(ByteBuf buf) {
    throw new UnsupportedOperationException("Packet must implement read method");
  }

  public void write(ByteBuf buf, Protocol protocol, ProtocolVersion.Direction direction, int protocolVersion) {
    this.write(buf, direction, protocolVersion);
  }

  public void write(ByteBuf buf, ProtocolVersion.Direction direction, int protocolVersion) {
    this.write(buf);
  }

  public void write(ByteBuf buf) {
    throw new UnsupportedOperationException("Packet must implement write method");
  }

  public Protocol nextProtocol() {
    return null;
  }

  public boolean isEncoded() {
    return encoded;
  }

  public void setEncoded(boolean encoded, ByteBuf buf) {
    this.encoded = encoded;
    this.encodedBuf = buf;
  }

  public ByteBuf getEncodedBuf() {
    return encodedBuf;
  }

  public void setEncodedBuf(ByteBuf encodedBuf) {
    this.encodedBuf = encodedBuf;
  }

  public abstract void handle(AbstractPacketHandler var1) throws Exception;

  public abstract int hashCode();

  public abstract boolean equals(Object var1);

  public abstract String toString();

  @FunctionalInterface
  public interface Writer<T> extends BiConsumer<ByteBuf, T> {
  }
}
