package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.JsonHelper;

import java.util.Locale;
import java.util.function.Function;

/**
 * Loader for an object with a single enum key
 *
 * @param <O> Object type
 * @param <T> Loader type
 */
public record EnumLoader<O extends IHaveLoader<?>, T extends Enum<T>>(
  String key,
  Class<T> enumClass,
  Function<T, O> constructor,
  Function<O, T> getter
) implements IGenericLoader<O> {

  @Override
  public O deserialize(JsonObject json) {
    return this.constructor.apply(JsonHelper.getAsEnum(json, this.key, this.enumClass));
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return this.constructor.apply(buffer.readEnum(this.enumClass));
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(this.key, this.getter.apply(object).name().toLowerCase(Locale.ROOT));
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeEnum(this.getter.apply(object));
  }
}
