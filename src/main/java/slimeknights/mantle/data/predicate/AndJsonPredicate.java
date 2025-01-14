package slimeknights.mantle.data.predicate;

import lombok.RequiredArgsConstructor;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;

import java.util.List;

/**
 * Predicate that requires all children to match
 */
@RequiredArgsConstructor
public class AndJsonPredicate<I> implements IJsonPredicate<I> {

  private final NestedJsonPredicateLoader<I, AndJsonPredicate<I>> loader;
  private final List<IJsonPredicate<I>> children;

  @Override
  public boolean matches(I input) {
    for (IJsonPredicate<I> child : this.children) {
      if (!child.matches(input)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public IJsonPredicate<I> inverted() {
    return this.loader.invert(this);
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<I>> getLoader() {
    return this.loader;
  }

  /**
   * Creates a new loader for the given loader registry
   */
  public static <I> NestedJsonPredicateLoader<I, AndJsonPredicate<I>> createLoader(GenericLoaderRegistry<IJsonPredicate<I>> loader, InvertedJsonPredicate.Loader<I> inverted) {
    return new NestedJsonPredicateLoader<>(loader, inverted, AndJsonPredicate::new, t -> t.children);
  }
}
