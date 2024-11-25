package slimeknights.tconstruct.library.modifiers.util;

import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.ModifierManager;

/**
 * Supplier that will return a modifier once they are fully registered, typically used with {@link ModifierDeferredRegister}
 */
public class StaticModifier<T extends Modifier> extends LazyModifier {

  public StaticModifier(ModifierId id) {
    super(id);
  }

  @Override
  protected Modifier getUnchecked() {
    if (this.result == null) {
      this.result = ModifierManager.INSTANCE.getStatic(this.id);
    }
    return this.result;
  }

  /**
   * Returns true if this static modifier has a value. A return of true here means its safe to call {@link #get()}
   */
  @Override
  public boolean isBound() {
    if (!ModifierManager.INSTANCE.isModifiersRegistered()) {
      return false;
    }
    return this.getUnchecked() != ModifierManager.INSTANCE.getDefaultValue();
  }

  /**
   * Fetches the modifier from the modifier manager. Should not be called until after the modifier registration event fires
   *
   * @return Modifier instance
   * @throws IllegalStateException If the modifier manager has not registered modifiers, or if the modifier ID was never registered
   */
  @SuppressWarnings("unchecked")
  @Override
  public T get() {
    if (!ModifierManager.INSTANCE.isModifiersRegistered()) {
      throw new IllegalStateException("Cannot fetch a static modifiers before modifiers are registered");
    }
    Modifier result = this.getUnchecked();
    if (result == ModifierManager.INSTANCE.getDefaultValue()) {
      throw new IllegalStateException("Static modifier for " + this.id + " returned " + ModifierManager.EMPTY + ", this typically indicates the modifier is improperly registered");
    }
    return (T) result;
  }

  @Override
  public String toString() {
    return "StaticModifier{" + this.id + '}';
  }
}
