package slimeknights.mantle.client.model;

import com.google.common.base.Predicates;

import java.util.function.Predicate;

// Reimplementing 1.18 forge code because I am lazy
public class ModelProperty<T> implements Predicate<T> {

  private final Predicate<T> pred;

  public ModelProperty() {
    this(Predicates.alwaysTrue());
  }

  public ModelProperty(Predicate<T> pred) {
    this.pred = pred;
  }

  @Override
  public boolean test(T t) {
    return this.pred.test(t);
  }
}
