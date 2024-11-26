package slimeknights.mantle.registration.object;

import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Registry object wrapper to implement {@link ItemLike}
 * @param <I>  Item class
 */
@SuppressWarnings({"unused", "WeakerAccess"})
@AllArgsConstructor
public class ItemObject<I extends ItemLike> implements Supplier<I>, ItemLike {
  /** Supplier to the registry entry */
  private final Supplier<? extends I> entry;
  /** Supplier to the registry name for this entry, allows fetching the name before the entry resolves if registry object is used */
  private final ResourceLocation name;

  /**
   * Creates a new item object from a supplier instance. Registry name will be fetched from the supplier entry, so the entry must be present during construction
   * @param entry  Existing registry entry, typically a vanilla block or a registered block
   */
  public ItemObject(I entry) {
    this.entry = () -> entry;
    this.name = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(entry.asItem()), () -> "Attempted to create an Item Object with an unregistered entry");
  }

  /**
   * Creates a new item object using the given registry object. This variant can resolve its name before the registry object entry resolves
   * @param object  Object base
   */
  public ItemObject(RegistryObject<? extends I> object) {
    this.entry = object;
    this.name = object.getId();
  }

  /**
   * Creates a new item object using another item object. Intended to be used in a subclass to avoid an extra wrapper
   * @param object  Object base
   */
  protected ItemObject(ItemObject<? extends I> object) {
    this.entry = object.entry;
    this.name = object.name;
  }

  /**
   * Gets the entry, throwing an exception if not present
   * @return  Entry
   * @throws NullPointerException  if not present
   */
  @Override
  public I get() {
    return Objects.requireNonNull(entry.get(), () -> "Item Object not present " + name);
  }

  /**
   * Gets the entry, or null if its not present
   * @return  entry, or null if missing
   */
  @Nullable
  public I getOrNull() {
    try {
      return entry.get();
    } catch (NullPointerException e) {
      // thrown by RegistryObject if missing value
      return null;
    }
  }

  @Override
  public Item asItem() {
    return get().asItem();
  }

  /**
   * Gets the resource location for the given item
   * @return  Resource location for the given item
   */
  public ResourceLocation getRegistryName() {
    return name;
  }
}
