package slimeknights.tconstruct.library.tools.nbt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiFunction;

/**
 * NBT wrapper enforcing namespaces on compound keys
 */
@EqualsAndHashCode
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NamespacedNBT implements INamespacedNBTView {

  /**
   * Compound representing modifier data
   */
  @Getter(AccessLevel.PROTECTED)
  private CompoundTag data;

  /**
   * Creates a new mod data containing empty data
   */
  public NamespacedNBT() {
    this(new CompoundTag());
  }

  @Override
  public <T> T get(ResourceLocation name, BiFunction<CompoundTag, String, T> function) {
    return function.apply(this.data, name.toString());
  }

  @Override
  public boolean contains(ResourceLocation name, int type) {
    return this.data.contains(name.toString(), type);
  }

  /**
   * Sets the given NBT into the data
   *
   * @param name Key name
   * @param nbt  NBT value
   */
  public void put(ResourceLocation name, Tag nbt) {
    this.data.put(name.toString(), nbt);
  }

  /**
   * Sets an integer from the mod data
   *
   * @param name  Name
   * @param value Integer value
   */
  public void putInt(ResourceLocation name, int value) {
    this.data.putInt(name.toString(), value);
  }

  /**
   * Sets an integer from the mod data
   *
   * @param name  Name
   * @param value Long value
   */
  public void putLong(ResourceLocation name, long value) {
    this.data.putLong(name.toString(), value);
  }

  /**
   * Sets an boolean from the mod data
   *
   * @param name  Name
   * @param value Boolean value
   */
  public void putBoolean(ResourceLocation name, boolean value) {
    this.data.putBoolean(name.toString(), value);
  }

  /**
   * Sets an float from the mod data
   *
   * @param name  Name
   * @param value Float value
   */
  public void putFloat(ResourceLocation name, float value) {
    this.data.putFloat(name.toString(), value);
  }

  /**
   * Reads a string from the mod data
   *
   * @param name  Name
   * @param value String value
   */
  public void putString(ResourceLocation name, String value) {
    this.data.putString(name.toString(), value);
  }

  /**
   * Removes the given key from the NBT
   *
   * @param name Key to remove
   */
  public void remove(ResourceLocation name) {
    this.data.remove(name.toString());
  }


  /* Networking */

  /**
   * Gets a copy of the internal data, generally should only be used for syncing, no reason to call directly
   */
  public CompoundTag getCopy() {
    return this.data.copy();
  }

  /**
   * Called to merge this NBT data from another
   *
   * @param data data
   */
  public void copyFrom(CompoundTag data) {
    this.data.getAllKeys().clear();
    this.data.merge(data);
  }

  /**
   * Parses the data from NBT
   *
   * @param data data
   * @return Parsed mod data
   */
  public static NamespacedNBT readFromNBT(CompoundTag data) {
    return new NamespacedNBT(data);
  }

  @Override
  public void readFromNbt(CompoundTag compoundTag) {
    compoundTag.put("data", this.data);
//    capability.invalidate();
//    capability = LazyOptional.of(() -> NamespacedNBT.readFromNBT(nbt.get()));
  }

  @Override
  public void writeToNbt(CompoundTag compoundTag) {
    this.data = compoundTag.getCompound("data");
  }
}
