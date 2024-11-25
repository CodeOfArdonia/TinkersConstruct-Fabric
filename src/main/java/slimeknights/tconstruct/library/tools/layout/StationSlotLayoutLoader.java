package slimeknights.tconstruct.library.tools.layout;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.fabricators_of_create.porting_lib.entity.events.OnDatapackSyncCallback;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.players.PlayerList;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Loader for tinker station slot layouts, loaded serverside as that makes it eaiser to modify with recipes and the filters are needed both sides
 */
@Log4j2
public class StationSlotLayoutLoader extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

  public static final String FOLDER = "tinkering/station_layouts";
  public static final Gson GSON = (new GsonBuilder())
    .registerTypeHierarchyAdapter(Ingredient.class, new IngredientSerializer())
    .registerTypeHierarchyAdapter(LayoutIcon.class, LayoutIcon.SERIALIZER)
    .registerTypeAdapter(Pattern.class, Pattern.SERIALIZER)
    .setPrettyPrinting()
    .disableHtmlEscaping()
    .create();
  private static final StationSlotLayoutLoader INSTANCE = new StationSlotLayoutLoader();

  /**
   * Map of name to slot layout
   */
  private Map<ResourceLocation, StationSlotLayout> layoutMap = Collections.emptyMap();
  /**
   * List of layouts that must be loaded for the game to work properly
   */
  private final List<ResourceLocation> requiredLayouts = new ArrayList<>();

  /**
   * List of all slots in order
   */
  @Getter
  private List<StationSlotLayout> sortedSlots = Collections.emptyList();

  private StationSlotLayoutLoader() {
    super(GSON, FOLDER);
  }

  /**
   * Sets the slots to the given collection from the packet
   */
  public void setSlots(Collection<StationSlotLayout> slots) {
    this.setSlots(slots.stream().collect(Collectors.toMap(StationSlotLayout::getName, Function.identity())));
  }

  /**
   * Updates the slot layouts
   */
  private void setSlots(Map<ResourceLocation, StationSlotLayout> map) {
    this.layoutMap = map;
    this.sortedSlots = map.values().stream()
      .filter(layout -> !layout.isMain())
      .sorted(Comparator.comparingInt(StationSlotLayout::getSortIndex))
      .collect(Collectors.toList());
  }

  @Override
  protected void apply(Map<ResourceLocation, JsonElement> splashList, ResourceManager resourceManager, ProfilerFiller profiler) {
    long time = System.nanoTime();
    ImmutableMap.Builder<ResourceLocation, StationSlotLayout> builder = ImmutableMap.builder();
    for (Entry<ResourceLocation, JsonElement> entry : splashList.entrySet()) {
      ResourceLocation key = entry.getKey();
      JsonElement value = entry.getValue();
      try {
        // skip empty objects, allows disabling a slot at a lower datapack
        JsonObject object = GsonHelper.convertToJsonObject(value, "station_layout");
        if (!object.entrySet().isEmpty()) {
          // just need a valid slot information
          StationSlotLayout layout = GSON.fromJson(object, StationSlotLayout.class);
          int size = layout.getInputSlots().size() + (layout.getToolSlot().isHidden() ? 0 : 1);
          if (size < 2) {
            throw new JsonParseException("Too few slots for layout " + key + ", must have at least 2");
          }
          layout.setName(key);
          builder.put(key, layout);
        }
      } catch (Exception e) {
        log.error("Failed to load station slot layout for name {}", key, e);
      }
    }
    this.setSlots(builder.build());
    log.info("Loaded {} station slot layouts in {} ms", this.layoutMap.size(), (System.nanoTime() - time) / 1000000f);
    List<String> missing = this.requiredLayouts.stream().filter(name -> !this.layoutMap.containsKey(name)).map(ResourceLocation::toString).collect(Collectors.toList());
    if (!missing.isEmpty()) {
      log.error("Failed to load the following required layouts: {}", String.join(", ", missing));
    }
  }

  /**
   * Gets a layout by name
   */
  public StationSlotLayout get(ResourceLocation name) {
    return this.layoutMap.getOrDefault(name, StationSlotLayout.EMPTY);
  }


  /**
   * Registers the name of a layout that should be loaded, if its missing that causes an error
   */
  public void registerRequiredLayout(ResourceLocation name) {
    this.requiredLayouts.add(name);
  }

  /* Events */

  /**
   * Called on datapack sync to send the tool data to all players
   */
  private void onDatapackSync(PlayerList playerList, @Nullable ServerPlayer player) {
    UpdateTinkerSlotLayoutsPacket packet = new UpdateTinkerSlotLayoutsPacket(this.layoutMap.values());
    TinkerNetwork.getInstance().sendToPlayerList(player, playerList, packet);
  }

  /**
   * Adds the managers as datapack listeners
   */
  private void addDataPackListeners() {
    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(this);
  }


  /* Static */

  /**
   * Gets the singleton instance of the loader
   */
  public static StationSlotLayoutLoader getInstance() {
    return INSTANCE;
  }

  /**
   * Initializes the tool definition loader
   */
  public static void init() {
    INSTANCE.addDataPackListeners();
    OnDatapackSyncCallback.EVENT.register(INSTANCE::onDatapackSync);
  }

  @Override
  public ResourceLocation getFabricId() {
    return TConstruct.getResource("station_slot_layout_loader");
  }

  /**
   * GSON serializer for ingredients
   */
  private static class IngredientSerializer implements JsonSerializer<Ingredient>, JsonDeserializer<Ingredient> {

    @Override
    public Ingredient deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
      return Ingredient.fromJson(json);
    }

    @Override
    public JsonElement serialize(Ingredient ingredient, Type typeOfSrc, JsonSerializationContext context) {
      return ingredient.toJson();
    }
  }
}
