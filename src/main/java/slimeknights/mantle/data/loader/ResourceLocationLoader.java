package slimeknights.mantle.data.loader;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.JsonHelper;

import java.util.function.Function;

/**
 * Loader for an object with a resource location
 *
 * @param <O> Object type
 */
public record ResourceLocationLoader<O extends IHaveLoader<?>>(
  String key,
  Function<ResourceLocation, O> constructor,
  Function<O, ResourceLocation> getter
) implements IGenericLoader<O> {

  @Override
  public O deserialize(JsonObject json) {
    return this.constructor.apply(JsonHelper.getResourceLocation(json, this.key));
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return this.constructor.apply(buffer.readResourceLocation());
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(this.key, this.getter.apply(object).toString());
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.getter.apply(object));
  }
}
