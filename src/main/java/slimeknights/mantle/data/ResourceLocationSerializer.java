package slimeknights.mantle.data;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.lang.reflect.Type;
import java.util.function.Function;

/**
 * Extension to Resource Location serializer to change the default mod ID.
 * TODO 1.19: move to {@code slimeknights.mantle.data.gson}
 */
@RequiredArgsConstructor
public class ResourceLocationSerializer<T extends ResourceLocation> implements JsonDeserializer<T>, JsonSerializer<T> {

  private final Function<String, T> constructor;
  private final String modId;

  /**
   * Creates an instance for resource locations
   */
  public static ResourceLocationSerializer<ResourceLocation> resourceLocation(String modId) {
    return new ResourceLocationSerializer<>(ResourceLocation::new, modId);
  }

  @Override
  public JsonElement serialize(ResourceLocation loc, Type type, JsonSerializationContext context) {
    return new JsonPrimitive(loc.toString());
  }

  @Override
  public T deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
    String loc = GsonHelper.convertToString(element, "location");
    if (!loc.contains(":")) {
      loc = this.modId + ":" + loc;
    }
    return this.constructor.apply(loc);
  }
}
