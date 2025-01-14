package slimeknights.mantle.data.predicate.damage;

import net.minecraft.world.damagesource.DamageSource;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.loader.StringLoader;
import slimeknights.mantle.data.predicate.IJsonPredicate;

/**
 * Predicate that matches a named source
 */
public record SourceMessagePredicate(String message) implements DamageSourcePredicate {

  public static final IGenericLoader<SourceMessagePredicate> LOADER = new StringLoader<>("message", SourceMessagePredicate::new, SourceMessagePredicate::message);

  public SourceMessagePredicate(DamageSource source) {
    this(source.getMsgId());
  }

  @Override
  public boolean matches(DamageSource source) {
    return this.message.equals(source.getMsgId());
  }

  @Override
  public IGenericLoader<? extends IJsonPredicate<DamageSource>> getLoader() {
    return LOADER;
  }
}
