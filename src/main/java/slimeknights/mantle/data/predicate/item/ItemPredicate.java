package slimeknights.mantle.data.predicate.item;

import net.minecraft.world.item.Item;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.SingletonLoader;
import slimeknights.mantle.data.predicate.AndJsonPredicate;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.InvertedJsonPredicate;
import slimeknights.mantle.data.predicate.NestedJsonPredicateLoader;
import slimeknights.mantle.data.predicate.OrJsonPredicate;

import java.util.function.Predicate;

/**
 * Simple serializable item predicate
 */
public interface ItemPredicate extends IJsonPredicate<Item> {

  /**
   * Predicate that matches all items
   */
  ItemPredicate ANY = simple(item -> true);
  /**
   * Loader for item predicates
   */
  GenericLoaderRegistry<IJsonPredicate<Item>> LOADER = new GenericLoaderRegistry<>(ANY, true);
  /**
   * Loader for inverted conditions
   */
  InvertedJsonPredicate.Loader<Item> INVERTED = new InvertedJsonPredicate.Loader<>(LOADER);
  /**
   * Loader for and conditions
   */
  NestedJsonPredicateLoader<Item, AndJsonPredicate<Item>> AND = AndJsonPredicate.createLoader(LOADER, INVERTED);
  /**
   * Loader for or conditions
   */
  NestedJsonPredicateLoader<Item, OrJsonPredicate<Item>> OR = OrJsonPredicate.createLoader(LOADER, INVERTED);


  @Override
  default IJsonPredicate<Item> inverted() {
    return INVERTED.create(this);
  }

  /**
   * Creates a new predicate singleton
   */
  static ItemPredicate simple(Predicate<Item> predicate) {
    return SingletonLoader.singleton(loader -> new ItemPredicate() {
      @Override
      public boolean matches(Item item) {
        return predicate.test(item);
      }

      @Override
      public IGenericLoader<? extends IJsonPredicate<Item>> getLoader() {
        return loader;
      }
    });
  }
}
