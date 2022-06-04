package slimeknights.tconstruct.library.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.network.packet.ISimplePacket;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

/** Helpers for a few JSON related tasks */
public class JsonUtils {
  private JsonUtils() {}

  /**
   * Reads an integer with a minimum value
   * @param json  Json
   * @param key   Key to read
   * @param min   Minimum and default value
   * @return  Read int
   * @throws JsonSyntaxException if the key is not an int or below the min
   */
  public static int getIntMin(JsonObject json, String key, int min) {
    int value = GsonHelper.getAsInt(json, key, min);
    if (value < min) {
      throw new JsonSyntaxException(key + " must be at least " + min);
    }
    return value;
  }

  /**
   * Reads an integer with a minimum value
   * @param json  Json element to parse as an integer
   * @param key   Key to read
   * @param min   Minimum
   * @return  Read int
   * @throws JsonSyntaxException if the key is not an int or below the min
   */
  public static int getIntMin(JsonElement json, String key, int min) {
    int value = GsonHelper.convertToInt(json, key);
    if (value < min) {
      throw new JsonSyntaxException(key + " must be at least " + min);
    }
    return value;
  }

  /**
   * Parses a registry entry from JSON
   * @param registry  Registry
   * @param element   Element to deserialize
   * @param key       Json key
   * @param <T>  Object type
   * @return  Registry value
   * @throws JsonSyntaxException  If something failed to parse
   */
  public static <T> T convertToEntry(Registry<T> registry, JsonElement element, String key) {
    ResourceLocation name = JsonHelper.convertToResourceLocation(element, key);
    if (registry.containsKey(name)) {
      T value = registry.get(name);
      if (value != null) {
        return value;
      }
    }
    throw new JsonSyntaxException("Unknown " + registry + " " + name);
  }

  /**
   * Parses a registry entry from JSON
   * @param registry  Registry
   * @param parent    Parent JSON object
   * @param key       Json key
   * @param <T>  Object type
   * @return  Registry value
   * @throws JsonSyntaxException  If something failed to parse
   */
  public static <T> T getAsEntry(Registry<T> registry, JsonObject parent, String key) {
    return convertToEntry(registry, JsonHelper.getElement(parent, key), key);
  }

  /**
   * Converts the resource into a JSON file
   * @param resource  Resource to read. Closed when done
   * @return  JSON object, or null if failed to parse
   */
  @Nullable
  public static JsonObject getJson(Resource resource) {
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
      return GsonHelper.parse(reader);
    } catch (JsonParseException | IOException e) {
      TConstruct.LOG.error("Failed to load JSON from resource " + resource.getLocation(), e);
      return null;
    }
  }

  /** Parses an enum from its name */
  private static <T extends Enum<T>> T enumByName(String name, Class<T> enumClass) {
    for (T value : enumClass.getEnumConstants()) {
      if (value.name().toLowerCase(Locale.ROOT).equals(name)) {
        return value;
      }
    }
    throw new JsonSyntaxException("Invalid " + enumClass.getSimpleName() + " " + name);
  }

  /** Gets an enum value from its string name */
  public static <T extends Enum<T>> T convertToEnum(JsonElement element, String key, Class<T> enumClass) {
    String name = GsonHelper.convertToString(element, key);
    return enumByName(name, enumClass);
  }

  /** Gets an enum value from its string name */
  public static <T extends Enum<T>> T getAsEnum(JsonObject json, String key, Class<T> enumClass) {
    String name = GsonHelper.getAsString(json, key);
    return enumByName(name, enumClass);
  }

  /** Gets a list of JSON objects for a single path in all domains and packs, for a language file like loader */
  public static List<JsonObject> getFileInAllDomainsAndPacks(ResourceManager manager, String path) {
    return manager
      .getNamespaces().stream()
      .filter(ResourceLocation::isValidNamespace)
      .flatMap(namespace -> {
        ResourceLocation location = new ResourceLocation(namespace, path);
        try {
          return manager.getResources(location).stream();
        } catch (FileNotFoundException e) {
          // suppress, the above method throws instead of returning empty
        } catch (IOException e) {
          TConstruct.LOG.error("Failed to load JSON files from {}", location, e);
        }
        return Stream.empty();
      })
      .map(JsonUtils::getJson)
      .filter(Objects::nonNull).toList();
  }

  /** Sends the packet to the given player */
  private static void sendPackets(ServerPlayer player, ISimplePacket[] packets) {
    // on an integrated server, the modifier registries have a single instance on both the client and the server thread
    // this means syncing is unneeded, and has the side-effect of recreating all the modifier instances (which can lead to unexpected behavior)
    // as a result, integrated servers just mark fullyLoaded as true without syncing anything, side-effect is listeners may run twice on single player

    // on a dedicated server, the client is running a separate game instance, this is where we send packets, plus fully loaded should already be true
    // this event is not fired when connecting to a server
    if (!player.connection.getConnection().isMemoryConnection()) {
      TinkerNetwork network = TinkerNetwork.getInstance();
      for (ISimplePacket packet : packets) {
        network.sendTo(packet, player);
      }
    }
  }

  /** Called when the player logs in to send packets */
  public static void syncPackets(PlayerList playerList, @Nullable ServerPlayer targetedPlayer, ISimplePacket... packets) {
    // send to single player
    if (targetedPlayer != null) {
      sendPackets(targetedPlayer, packets);
    } else {
      // send to all players
      for (ServerPlayer player :playerList.getPlayers()) {
        sendPackets(player, packets);
      }
    }
  }

  /** Creates a JSON object with the given key set to a resource location */
  public static JsonObject withLocation(String key, ResourceLocation value) {
    JsonObject json = new JsonObject();
    json.addProperty(key, value.toString());
    return json;
  }

  /** Creates a JSON object with the given type set, makes using {@link slimeknights.mantle.data.GenericRegisteredSerializer} eaiser */
  public static JsonObject withType(ResourceLocation type) {
    return withLocation("type", type);
  }
}
