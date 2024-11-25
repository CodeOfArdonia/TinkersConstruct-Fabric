package slimeknights.tconstruct.library.modifiers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

/**
 * Class implementing a modifier hook, used as a key for TODO method link
 */
@RequiredArgsConstructor
public class ModifierHook<T> {

  /**
   * Unique name of this hook, used for serialization
   */
  @Getter
  private final ResourceLocation name;
  /**
   * Filter to check if an object is valid for this hook
   */
  private final Class<T> filter;
  /**
   * Default instance for when a modifier does not implement this hook
   */
  @Getter
  private final T defaultInstance;
  /**
   * Logic to merge multiple instances into a single instance
   */
  @Nullable
  private final Function<Collection<T>, T> merger;

  public ModifierHook(ResourceLocation name, Class<T> filter, T defaultInstance) {
    this(name, filter, defaultInstance, null);
  }

  /**
   * checks if the given module can be used for this hook
   */
  public boolean isValid(Object module) {
    return this.filter.isInstance(module);
  }

  /**
   * Unchecked cast of the module to this hook type. Use only if certain the module type and hook type are the same
   */
  @SuppressWarnings("unchecked")
  public T cast(Object module) {
    return (T) module;
  }

  /**
   * Returns true if this hook supports merging
   */
  public boolean canMerge() {
    return this.merger != null;
  }

  /**
   * Merges the given modifiers into a single instance. Only supported if {@link #canMerge()} returns true
   */
  public T merge(Collection<T> modules) {
    if (modules.isEmpty()) {
      return this.defaultInstance;
    }
    if (modules.size() == 1) {
      return modules.iterator().next();
    }
    if (this.merger == null) {
      throw new IllegalStateException(this.name + " does not support merging");
    }
    return this.merger.apply(modules);
  }

  @Override
  public String toString() {
    return "ModifierHook{" + this.name + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    ModifierHook<?> that = (ModifierHook<?>) o;
    return this.name.equals(that.name);
  }

  @Override
  public int hashCode() {
    return this.name.hashCode();
  }
}
