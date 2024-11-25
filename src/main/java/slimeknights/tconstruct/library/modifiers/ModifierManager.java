package slimeknights.tconstruct.library.modifiers;

import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import io.github.fabricators_of_create.porting_lib.event.common.ModsLoadedCallback;
import io.github.fabricators_of_create.porting_lib.util.CraftingHelper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.enchantment.Enchantment;
import slimeknights.mantle.data.GenericLoaderRegistry;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.RegistryHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.JsonRedirect;
import slimeknights.tconstruct.library.utils.GenericTagUtil;
import slimeknights.tconstruct.library.utils.JsonUtils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Modifier registry and JSON loader
 */
@Log4j2
public class ModifierManager extends SimpleJsonResourceReloadListener implements IdentifiableResourceReloadListener {

  /**
   * Location of dynamic modifiers
   */
  public static final String FOLDER = "tinkering/modifiers";
  /**
   * Location of modifier tags
   */
  public static final String TAG_FOLDER = "tinkering/tags/modifiers";

  public static final ResourceLocation ENCHANTMENT_MAP = TConstruct.getResource("tinkering/enchantments_to_modifiers.json");
  /**
   * Registry key to make tag keys
   */
  public static final ResourceKey<? extends Registry<Modifier>> REGISTRY_KEY = ResourceKey.createRegistryKey(TConstruct.getResource("modifiers"));

  /**
   * GSON instance for loading dynamic modifiers
   */
  public static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();

  /**
   * ID of the default modifier
   */
  public static final ModifierId EMPTY = new ModifierId(TConstruct.MOD_ID, "empty");

  /**
   * Singleton instance of the modifier manager
   */
  public static final ModifierManager INSTANCE = new ModifierManager();

  /**
   * Default modifier to use when a modifier is not found
   */
  @Getter
  private final Modifier defaultValue;

  /**
   * If true, static modifiers have been registered, so static modifiers can safely be fetched
   */
  @Getter
  private boolean modifiersRegistered = false;
  /**
   * All modifiers registered directly with the manager
   */
  @VisibleForTesting
  final Map<ModifierId, Modifier> staticModifiers = new HashMap<>();
  /**
   * Map of all modifier types that are expected to load in datapacks
   */
  private final Map<ModifierId, Class<?>> expectedDynamicModifiers = new HashMap<>();
  /**
   * Map of all modifier types that are expected to load in datapacks
   */
  public static final GenericLoaderRegistry<Modifier> MODIFIER_LOADERS = new GenericLoaderRegistry<>();

  /**
   * Modifiers loaded from JSON
   */
  private Map<ModifierId, Modifier> dynamicModifiers = Collections.emptyMap();
  /**
   * Modifier tags loaded from JSON
   */
  private Map<ResourceLocation, Collection<Modifier>> tags = Collections.emptyMap();
  /**
   * Map from modifier to tags on the modifier
   */
  private Map<ModifierId, Set<TagKey<Modifier>>> reverseTags = Collections.emptyMap();

  /**
   * List of tag to modifier mappings to try
   */
  private Map<TagKey<Enchantment>, Modifier> enchantmentTagMap = Collections.emptyMap();
  /**
   * Mapping from enchantment to modifiers, for conversions
   */
  private Map<Enchantment, Modifier> enchantmentMap = Collections.emptyMap();

  /**
   * If true, dynamic modifiers have been loaded from datapacks, so its safe to fetch dynamic modifiers
   */
  @Getter
  boolean dynamicModifiersLoaded = false;
//  private IContext conditionContext = IContext.EMPTY;

  private ModifierManager() {
    super(GSON, FOLDER);
    // create the empty modifier
    this.defaultValue = new EmptyModifier();
    this.defaultValue.setId(EMPTY);
    this.staticModifiers.put(EMPTY, this.defaultValue);
  }

  /**
   * For internal use only
   */
  @Deprecated
  public void init() {
    this.fireRegistryEvent();
    this.addDataPackListeners();
    ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register((player, joined) -> JsonUtils.syncPackets(player, joined, new UpdateModifiersPacket(this.dynamicModifiers, this.tags, this.enchantmentMap, this.enchantmentTagMap)));
  }

  /**
   * Fires the modifier registry event
   */
  private void fireRegistryEvent() {
    ModsLoadedCallback.EVENT.register(envType -> new ModifierRegistrationEvent().sendEvent());
    this.modifiersRegistered = true;
  }

  /**
   * Adds the managers as datapack listeners
   */
  private void addDataPackListeners() {
    ResourceManagerHelper.get(PackType.SERVER_DATA).registerReloadListener(this);
//    conditionContext = event.getConditionContext();
  }

  @Override
  protected void apply(Map<ResourceLocation, JsonElement> splashList, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
    long time = System.nanoTime();

    // load modifiers from JSON
    Map<ModifierId, ModifierId> redirects = new HashMap<>();
    this.dynamicModifiers = splashList.entrySet().stream()
      .map(entry -> this.loadModifier(entry.getKey(), entry.getValue().getAsJsonObject(), redirects))
      .filter(Objects::nonNull)
      .collect(Collectors.toMap(Modifier::getId, mod -> mod));

    // process redirects
    Map<ModifierId, Modifier> resolvedRedirects = new HashMap<>(); // handled as a separate map to prevent redirects depending on order (no double redirects)
    for (Entry<ModifierId, ModifierId> redirect : redirects.entrySet()) {
      ModifierId from = redirect.getKey();
      ModifierId to = redirect.getValue();
      if (!this.contains(to)) {
        log.error("Invalid modifier redirect {} as modifier {} does not exist", from, to);
      } else {
        resolvedRedirects.put(from, this.get(to));
      }
    }
    int modifierSize = this.dynamicModifiers.size();
    this.dynamicModifiers.putAll(resolvedRedirects);

    // validate required modifiers
    for (Entry<ModifierId, Class<?>> entry : this.expectedDynamicModifiers.entrySet()) {
      Modifier modifier = this.dynamicModifiers.get(entry.getKey());
      if (modifier == null) {
        log.error("Missing expected modifier '" + entry.getKey() + "'");
      } else if (!entry.getValue().isInstance(modifier)) {
        log.error("Modifier '" + entry.getKey() + "' was loaded with the wrong class type. Expected " + entry.getValue().getName() + ", got " + modifier.getClass().getName());
      }
    }

    // TODO: this should be set back to false at some point
    this.dynamicModifiersLoaded = true;
    long timeStep = System.nanoTime();
    log.info("Loaded {} dynamic modifiers and {} modifier redirects in {} ms", modifierSize, redirects.size(), (timeStep - time) / 1000000f);
    time = timeStep;

    // load modifier tags
    TagLoader<Modifier> tagLoader = new TagLoader<>(id -> {
      Modifier modifier = ModifierManager.getValue(new ModifierId(id));
      if (modifier == this.defaultValue) {
        return Optional.empty();
      }
      return Optional.of(modifier);
    }, TAG_FOLDER);
    this.tags = tagLoader.loadAndBuild(pResourceManager);
    this.reverseTags = GenericTagUtil.reverseTags(REGISTRY_KEY, Modifier::getId, this.tags);
    timeStep = System.nanoTime();
    log.info("Loaded {} modifier tags for {} modifiers in {} ms", this.tags.size(), this.reverseTags.size(), (timeStep - time) / 1000000f);

    // load modifier to enchantment mapping
    this.enchantmentMap = new HashMap<>();
    this.enchantmentTagMap = new LinkedHashMap<>();
    for (Resource resource : pResourceManager.getResourceStack(ENCHANTMENT_MAP)) {
      JsonObject enchantmentJson = JsonHelper.getJson(resource);
      if (enchantmentJson != null) {
        for (Entry<String, JsonElement> entry : enchantmentJson.entrySet()) {
          try {
            // parse the modifier first, its the same in both cases
            String key = entry.getKey();
            ModifierId modifierId = new ModifierId(JsonHelper.convertToResourceLocation(entry.getValue(), "modifier"));
            Modifier modifier = this.get(modifierId);
            if (modifier == this.defaultValue) {
              throw new JsonSyntaxException("Unknown modifier " + modifierId + " for enchantment " + key);
            }

            // if it starts with #, it's a tag
            if (key.startsWith("#")) {
              ResourceLocation tagId = ResourceLocation.tryParse(key.substring(1));
              if (tagId == null) {
                throw new JsonSyntaxException("Invalid enchantment tag ID " + key.substring(1));
              }
              this.enchantmentTagMap.put(TagKey.create(Registries.ENCHANTMENT, tagId), modifier);
            } else {
              // assume its an ID
              ResourceLocation enchantId = ResourceLocation.tryParse(key);
              if (enchantId == null || !BuiltInRegistries.ENCHANTMENT.containsKey(enchantId)) {
                throw new JsonSyntaxException("Invalid enchantment ID " + key);
              }
              this.enchantmentMap.put(BuiltInRegistries.ENCHANTMENT.get(enchantId), modifier);
            }
          } catch (JsonSyntaxException e) {
            log.info("Invalid enchantment to modifier mapping", e);
          }
        }
      }
    }
    log.info("Loaded {} enchantment to modifier mappings in {} ms", this.enchantmentMap.size() + this.enchantmentTagMap.size(), (System.nanoTime() - timeStep) / 1000000f);

    new ModifiersLoadedEvent().sendEvent();
  }

  /**
   * Loads a modifier from JSON
   */
  @Nullable
  private Modifier loadModifier(ResourceLocation key, JsonElement element, Map<ModifierId, ModifierId> redirects) {
    try {
      JsonObject json = GsonHelper.convertToJsonObject(element, "modifier");

      // processed first so a modifier can both conditionally redirect and fallback to a conditional modifier
      if (json.has("redirects")) {
        for (JsonRedirect redirect : JsonHelper.parseList(json, "redirects", JsonRedirect::fromJson)) {
          Predicate<JsonObject> redirectCondition = redirect.getConditionPredicate();
          if (redirectCondition == null || redirectCondition.test(json)) {
            ModifierId redirectTarget = new ModifierId(redirect.getId());
            log.debug("Redirecting modifier {} to {}", key, redirectTarget);
            redirects.put(new ModifierId(key), redirectTarget);
            return null;
          }
        }
      }

      // conditions
      if (json.has(ResourceConditions.CONDITIONS_KEY) && !CraftingHelper.getConditionPredicate(GsonHelper.getAsJsonObject(json, ResourceConditions.CONDITIONS_KEY)).test(json)) {
        return null;
      }

      // fallback to actual modifier
      Modifier modifier = MODIFIER_LOADERS.deserialize(json);
      modifier.setId(new ModifierId(key));
      return modifier;
    } catch (JsonSyntaxException e) {
      log.error("Failed to load modifier {}", key, e);
      return null;
    }
  }

  /**
   * Updates the modifiers from the server
   */
  void updateModifiersFromServer(Map<ModifierId, Modifier> modifiers, Map<ResourceLocation, Collection<Modifier>> tags, Map<Enchantment, Modifier> enchantmentMap, Map<TagKey<Enchantment>, Modifier> enchantmentTagMappings) {
    this.dynamicModifiers = modifiers;
    this.dynamicModifiersLoaded = true;
    this.tags = tags;
    this.reverseTags = GenericTagUtil.reverseTags(REGISTRY_KEY, Modifier::getId, tags);
    this.enchantmentMap = enchantmentMap;
    this.enchantmentTagMap = enchantmentTagMappings;
    new ModifiersLoadedEvent().sendEvent();
  }


  /* Query the registry */

  /**
   * Fetches a static modifier by ID, only use if you need access to modifiers before the world loads
   */
  public Modifier getStatic(ModifierId id) {
    return this.staticModifiers.getOrDefault(id, this.defaultValue);
  }

  /**
   * Checks if the given static modifier exists
   */
  public boolean containsStatic(ModifierId id) {
    return this.staticModifiers.containsKey(id) || this.expectedDynamicModifiers.containsKey(id);
  }

  /**
   * Checks if the registry contains the given modifier
   */
  public boolean contains(ModifierId id) {
    return this.staticModifiers.containsKey(id) || this.dynamicModifiers.containsKey(id);
  }

  /**
   * Gets the modifier for the given ID
   */
  public Modifier get(ModifierId id) {
    // highest priority is static modifiers, cannot be replaced
    Modifier modifier = this.staticModifiers.get(id);
    if (modifier != null) {
      return modifier;
    }
    // second priority is dynamic modifiers, fallback to the default
    return this.dynamicModifiers.getOrDefault(id, this.defaultValue);
  }

  /**
   * Gets the modifier for a given enchantment. Not currently synced to client side
   *
   * @param enchantment Enchantment
   * @return Closest modifier to the enchantment, or null if no match
   */
  @Nullable
  public Modifier get(Enchantment enchantment) {
    // if we saw it before, return the last value
    if (this.enchantmentMap.containsKey(enchantment)) {
      return this.enchantmentMap.get(enchantment);
    }
    // did not find, check the tags
    for (Entry<TagKey<Enchantment>, Modifier> mapping : this.enchantmentTagMap.entrySet()) {
      if (RegistryHelper.contains(BuiltInRegistries.ENCHANTMENT, mapping.getKey(), enchantment)) {
        return mapping.getValue();
      }
    }
    return null;
  }

  /**
   * Gets a stream of all enchantments that match the given modifiers
   */
  public Stream<Enchantment> getEquivalentEnchantments(Predicate<ModifierId> modifiers) {
    Predicate<Entry<?, Modifier>> predicate = entry -> modifiers.test(entry.getValue().getId());
    return Stream.concat(
      this.enchantmentMap.entrySet().stream().filter(predicate).map(Entry::getKey),
      this.enchantmentTagMap.entrySet().stream().filter(predicate).flatMap(entry -> RegistryHelper.getTagValueStream(BuiltInRegistries.ENCHANTMENT, entry.getKey()))
    ).distinct().sorted(Comparator.comparing(enchantment -> Objects.requireNonNull(BuiltInRegistries.ENCHANTMENT.getKey(enchantment))));
  }

  /**
   * Gets a list of all modifier IDs
   */
  public Stream<ResourceLocation> getAllLocations() {
    // filter out redirects (redirects are any modifiers where the ID does not match the key
    return Stream.concat(this.staticModifiers.entrySet().stream(), this.dynamicModifiers.entrySet().stream())
      .filter(entry -> entry.getKey().equals(entry.getValue().getId()))
      .map(Entry::getKey);
  }

  /**
   * Gets a stream of all modifier values
   */
  public Stream<Modifier> getAllValues() {
    return Stream.concat(this.staticModifiers.values().stream(), this.dynamicModifiers.values().stream()).distinct();
  }


  /* Helpers */

  /**
   * Gets the modifier for the given ID
   */
  public static Modifier getValue(ModifierId name) {
    return INSTANCE.get(name);
  }

  /**
   * Parses a modifier from JSON
   *
   * @param element Element to deserialize
   * @param key     Json key
   * @return Registry value
   * @throws JsonSyntaxException If something failed to parse
   */
  public static Modifier convertToModifier(JsonElement element, String key) {
    ModifierId name = new ModifierId(JsonHelper.convertToResourceLocation(element, key));
    if (INSTANCE.contains(name)) {
      return INSTANCE.get(name);
    }
    throw new JsonSyntaxException("Unknown modifier " + name);
  }

  /**
   * Parses a modifier from JSON
   *
   * @param parent Parent JSON object
   * @param key    Json key
   * @return Registry value
   * @throws JsonSyntaxException If something failed to parse
   */
  public static Modifier deserializeModifier(JsonObject parent, String key) {
    return convertToModifier(JsonHelper.getElement(parent, key), key);
  }

  /**
   * Reads a modifier from the buffer
   *
   * @param buffer Buffer instance
   * @return Modifier instance
   */
  public static Modifier fromNetwork(FriendlyByteBuf buffer) {
    return INSTANCE.get(new ModifierId(buffer.readUtf(Short.MAX_VALUE)));
  }

  /**
   * Reads a modifier from the buffer
   *
   * @param modifier Modifier instance
   * @param buffer   Buffer instance
   */
  public static void toNetwork(Modifier modifier, FriendlyByteBuf buffer) {
    buffer.writeUtf(modifier.getId().toString());
  }

  @Override
  public ResourceLocation getFabricId() {
    return TConstruct.getResource("modifier_manager");
  }


  /* Tags */

  /**
   * Creates a tag key for a modifier
   */
  public static TagKey<Modifier> getTag(ResourceLocation id) {
    return TagKey.create(REGISTRY_KEY, id);
  }

  /**
   * Checks if the given modifier is in the given tag
   *
   * @return True if the modifier is in the tag
   */
  public static boolean isInTag(ModifierId modifier, TagKey<Modifier> tag) {
    return INSTANCE.reverseTags.getOrDefault(modifier, Collections.emptySet()).contains(tag);
  }

  /**
   * Gets all values contained in the given tag
   *
   * @param tag Tag instance
   * @return Contained values
   */
  public static List<Modifier> getTagValues(TagKey<Modifier> tag) {
    return INSTANCE.tags.getOrDefault(tag.location(), Collections.emptyList()).stream().toList();
  }


  /* Events */

  /**
   * Event for registering modifiers
   */
  @RequiredArgsConstructor(access = AccessLevel.PROTECTED)
  public class ModifierRegistrationEvent extends BaseEvent {

    public static Event<ModifierRegistrationCallback> EVENT = EventFactory.createArrayBacked(ModifierRegistrationCallback.class, callbacks -> event -> {
      for (ModifierRegistrationCallback e : callbacks)
        e.onRegistration(event);
    });

    /**
     * Registers a static modifier with the manager. Static modifiers cannot be configured by datapacks, so its generally encouraged to use dynamic modifiers
     *
     * @param name     Modifier name
     * @param modifier Modifier instance
     */
    public void registerStatic(ModifierId name, Modifier modifier) {
      // should not include under both types
      if (ModifierManager.this.expectedDynamicModifiers.containsKey(name)) {
        throw new IllegalArgumentException(name + " is already expected as a dynamic modifier");
      }

      // set the name and register it
      modifier.setId(name);
      Modifier existing = ModifierManager.this.staticModifiers.putIfAbsent(name, modifier);
      if (existing != null) {
        throw new IllegalArgumentException("Attempting to register a duplicate static modifier, this is not supported. Original value " + existing);
      }
    }

    /**
     * Registers that the given modifier is expected to be loaded in datapacks
     *
     * @param name        Modifier name
     * @param classFilter Class type the modifier is expected to have. Can be an interface
     */
    public void registerExpected(ModifierId name, Class<?> classFilter) {
      // should not include under both types
      if (ModifierManager.this.staticModifiers.containsKey(name)) {
        throw new IllegalArgumentException(name + " is already registered as a static modifier");
      }

      // register it
      Class<?> existing = ModifierManager.this.expectedDynamicModifiers.putIfAbsent(name, classFilter);
      if (existing != null) {
        throw new IllegalArgumentException("Attempting to register a duplicate expected modifier, this is not supported. Original value " + existing);
      }
    }

    @Override
    public void sendEvent() {
      EVENT.invoker().onRegistration(this);
    }
  }

  public interface ModifierRegistrationCallback {

    void onRegistration(ModifierRegistrationEvent event);
  }

  /**
   * Event fired when modifiers reload
   */
  public static class ModifiersLoadedEvent extends BaseEvent {

    public static Event<ModifiersLoadedCallback> EVENT = EventFactory.createArrayBacked(ModifiersLoadedCallback.class, callbacks -> event -> {
      for (ModifiersLoadedCallback e : callbacks)
        e.onLoaded(event);
    });

    @Override
    public void sendEvent() {
      EVENT.invoker().onLoaded(this);
    }
  }

  public interface ModifiersLoadedCallback {

    void onLoaded(ModifiersLoadedEvent event);
  }

  /**
   * Class for the empty modifier instance, mods should not need to extend this class
   */
  private static class EmptyModifier extends Modifier {

    @Override
    public boolean shouldDisplay(boolean advanced) {
      return false;
    }
  }
}
