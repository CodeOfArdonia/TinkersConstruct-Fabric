package slimeknights.mantle.data.predicate.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.TagPredicateLoader;
import slimeknights.mantle.util.RegistryHelper;

/** Predicate matching an item tag */
public record ItemTagPredicate(TagKey<Item> tag) implements ItemPredicate {
  public static final TagPredicateLoader<Item,ItemTagPredicate> LOADER = new TagPredicateLoader<>(Registries.ITEM, ItemTagPredicate::new, c -> c.tag);

  @Override
  public boolean matches(Item item) {
    return RegistryHelper.contains(tag, item);
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<Item>> getLoader() {
    return LOADER;
  }
}
