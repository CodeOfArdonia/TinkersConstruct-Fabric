package slimeknights.mantle.registration.adapter;

import lombok.RequiredArgsConstructor;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import java.util.Objects;

/**
 * A convenience wrapper for forge registries, to be used in combination with the {@link net.minecraftforge.event.RegistryEvent.Register} event.
 * Simply put it allows you to register things by passing (thing, name) instead of having to set the name inline.
 * There also is a convenience variant for items and itemblocks, see {@link ItemRegistryAdapter}.
 */
@SuppressWarnings("WeakerAccess")
@RequiredArgsConstructor
public class RegistryAdapter<T> {
  private final Registry<T> registry;
  private final String modId;

//  /**
//   * Automatically creates determines the modid from the currently loading mod.
//   * If this results in the wrong namespace, use the other constructor where you can provide the modid.
//   * The modid is used as the namespace for resource locations, so if your mods id is "foo" it will register an item "bar" as "foo:bar".
//   */
//  public RegistryAdapter(Registry<T> registry) {
//    this(registry, ModLoadingContext.get().getActiveContainer().getModId());
//  }

  /**
   * Construct a resource location that belongs to the given namespace. Usually your mod.
   * @param name  Name for location
   */
  public ResourceLocation getResource(String name) {
    return new ResourceLocation(modId, name);
  }

  /**
   * Construct a resource location string that belongs to the given namespace. Usually your mod.
   * @param name  Name for location
   */
  public String resourceName(String name) {
    return modId + ":" + name;
  }

  /**
   * General purpose registration method. Just pass the name you want your thing registered as.
   * @param entry  Entry to register
   * @param name   Registry name
   * @return Registry entry
   */
  public <I extends T> I register(I entry, String name) {
    return this.register(entry, this.getResource(name));
  }

  /**
   * Registers an entry using the name from another entry
   * @param entry  Entry to register
   * @param name   Entry name to copy
   * @param <I>    Value type
   * @return  Registered entry
   */
  public <I extends T> I register(I entry, Object name) {
    return this.register(entry, Objects.requireNonNull(getRegistryName(name)));
  }

  public static ResourceLocation getRegistryName(Object obj) {
    if(obj instanceof Block block)
      return BuiltInRegistries.BLOCK.getKey(block);
    if(obj instanceof Item item)
      return BuiltInRegistries.ITEM.getKey(item);
    if(obj instanceof Fluid fluid)
      return BuiltInRegistries.FLUID.getKey(fluid);
    if(obj instanceof EntityType<?> entityType)
      return BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
    return null;
  }

  /**
   * General purpose backup registration method. In case you want to set a very specific resource location.
   * You should probably use the special purpose methods instead of this.
   * <p>
   * Note: changes the things registry name. Do not call this with already registered objects!
   * @param entry     Entry to register
   * @param location  Registry name
   * @return Registry entry
   */
  public <I extends T> I register(I entry, ResourceLocation location) {
    return Registry.register(registry, location, entry);
  }
}
