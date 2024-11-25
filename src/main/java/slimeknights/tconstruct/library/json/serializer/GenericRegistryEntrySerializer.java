package slimeknights.tconstruct.library.json.serializer;

import com.google.gson.JsonObject;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.mantle.data.GenericLoaderRegistry.IHaveLoader;
import slimeknights.mantle.util.JsonHelper;

import java.util.Objects;
import java.util.function.Function;

/**
 * Serializer for an object with a registry entry parameter
 *
 * @param <O> Object type
 * @param <V> Registry entry type
 */
public record GenericRegistryEntrySerializer<O extends IHaveLoader<?>, V>(
  String key,
  Registry<V> registry,
  Function<V, O> constructor,
  Function<O, V> getter
) implements IGenericLoader<O> {

  @Override
  public O deserialize(JsonObject json) {
    return this.constructor.apply(JsonHelper.getAsEntry(this.registry, json, this.key));
  }

  @Override
  public void serialize(O object, JsonObject json) {
    json.addProperty(this.key, Objects.requireNonNull(this.registry.getKey(this.getter.apply(object))).toString());
  }

  @Override
  public O fromNetwork(FriendlyByteBuf buffer) {
    return this.constructor.apply(this.registry.byId(buffer.readVarInt()));
  }

  @Override
  public void toNetwork(O object, FriendlyByteBuf buffer) {
    buffer.writeVarInt(this.registry.getId(this.getter.apply(object)));
  }
}
