package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.IHaveLoader;

import java.util.function.Function;

/**
 * Loader for a string value
 *
 * @param <O>
 */
public record StringLoader<O extends IHaveLoader<?>>(
  String key,
  Function<String, O> constructor,
  Function<O, String> getter
) implements IGenericLoader<O> {

  @Override
  public O deserialize(JsonObject json) {
    return this.constructor.apply(GsonHelper.getAsString(json, this.key));
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(this.key, this.getter.apply(object));
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return this.constructor.apply(buffer.readUtf(Short.MAX_VALUE));
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeUtf(this.getter.apply(object));
  }
}
