package net.paradise_client.protocol;

import com.viaversion.nbt.tag.CompoundTag;

public class ItemStack {
  public static final ItemStack EMPTY = new ItemStack(0, 1);

  // minimal implementation
  private int count;
  private int id;
  private final CompoundTag tag = new CompoundTag();

  public ItemStack(int count, int id) {
    this.count = count;
    this.id = id;
  }

  public boolean isEmpty() {
    return count < 1;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public CompoundTag getTag() {
    return tag;
  }
}
