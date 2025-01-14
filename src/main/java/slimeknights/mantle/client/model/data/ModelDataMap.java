package slimeknights.mantle.client.model.data;

import com.google.common.base.Preconditions;
import slimeknights.mantle.client.model.ModelProperty;

import java.util.IdentityHashMap;
import java.util.Map;

public class ModelDataMap implements IModelData {

  private final Map<ModelProperty<?>, Object> backingMap;

  private ModelDataMap(Map<ModelProperty<?>, Object> map) {
    this.backingMap = new IdentityHashMap<>(map);
  }

  protected ModelDataMap() {
    this.backingMap = new IdentityHashMap<>();
  }

  @Override
  public boolean hasProperty(ModelProperty<?> prop) {
    return this.backingMap.containsKey(prop);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getData(ModelProperty<T> prop) {
    return (T) this.backingMap.get(prop);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T setData(ModelProperty<T> prop, T data) {
    Preconditions.checkArgument(prop.test(data), "Value is invalid for this property");
    return (T) this.backingMap.put(prop, data);
  }

  public static class Builder {

    private final Map<ModelProperty<?>, Object> defaults = new IdentityHashMap<>();

    public Builder withProperty(ModelProperty<?> prop) {
      return this.withInitial(prop, null);
    }

    public <T> Builder withInitial(ModelProperty<T> prop, T data) {
      this.defaults.put(prop, data);
      return this;
    }

    public ModelDataMap build() {
      return new ModelDataMap(this.defaults);
    }
  }
}
