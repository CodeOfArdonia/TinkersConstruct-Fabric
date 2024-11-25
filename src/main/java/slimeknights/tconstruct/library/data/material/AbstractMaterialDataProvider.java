package slimeknights.tconstruct.library.data.material;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import slimeknights.mantle.client.book.data.JsonCondition;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.library.json.JsonRedirect;
import slimeknights.tconstruct.library.materials.definition.IMaterial;
import slimeknights.tconstruct.library.materials.definition.Material;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialManager;
import slimeknights.tconstruct.library.materials.json.MaterialJson;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Extendable material provider, useful for addons
 */
@SuppressWarnings({"SameParameterValue", "unused"})
public abstract class AbstractMaterialDataProvider extends GenericDataProvider {

  /**
   * General purpose materials
   */
  public static final int ORDER_GENERAL = 0;
  /**
   * Materials primarily used for harvest
   */
  public static final int ORDER_HARVEST = 1;
  /**
   * Materials primarily used for weapons
   */
  public static final int ORDER_WEAPON = 2;
  /**
   * General purpose materials
   */
  public static final int ORDER_SPECIAL = 3;
  /**
   * Ranged exclusive materials
   */
  public static final int ORDER_RANGED = 4;
  /**
   * Order for mod integration materials
   */
  public static final int ORDER_COMPAT = 5;
  /**
   * Order for nether materials in tiers 1-3
   */
  public static final int ORDER_NETHER = 10;
  /**
   * Order for end materials in tiers 1-4
   */
  public static final int ORDER_END = 15;
  /**
   * Order for materials that are just a binding
   */
  public static final int ORDER_BINDING = 20;
  /**
   * Order for materials that are just used for repair or textures
   */
  public static final int ORDER_REPAIR = 25;

  /**
   * List of all added materials
   */
  private final Map<MaterialId, DataMaterial> allMaterials = new HashMap<>();

  /**
   * Boolean just in case material stats run first
   */
  private boolean addMaterialsRun = false;

  public AbstractMaterialDataProvider(FabricDataOutput output) {
    super(output, MaterialManager.FOLDER, MaterialManager.GSON);
  }

  /**
   * Function to add all relevant materials
   */
  protected abstract void addMaterials();

  private void ensureAddMaterialsRun() {
    if (this.addMaterialsRun) {
      return;
    }
    this.addMaterialsRun = true;
    this.addMaterials();
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    this.ensureAddMaterialsRun();
    List<CompletableFuture<?>> futures = new ArrayList<>();
    this.allMaterials.forEach((id, data) -> futures.add(this.saveThing(cache, id, this.convert(data))));
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }

  /**
   * Gets a list of all material IDs that are generated. Note this will run {@link #addMaterials()}, so generally its better to run your material data provider first
   *
   * @return Material ID list
   */
  public Set<MaterialId> getAllMaterials() {
    this.ensureAddMaterialsRun();
    // ignore any materials with no IMaterial defintion, means its purely a redirect and will never exist in game
    return this.allMaterials.values().stream()
      .map(DataMaterial::material)
      .filter(Objects::nonNull)
      .map(IMaterial::getIdentifier)
      .collect(Collectors.toSet());
  }


  /* Base methods */

  /**
   * Adds a material to be generated with a condition and redirect data
   */
  protected void addMaterial(IMaterial material, @Nullable ConditionJsonProvider condition, JsonRedirect... redirect) {
    this.allMaterials.put(material.getIdentifier(), new DataMaterial(material, condition, redirect));
  }

  /**
   * Adds JSON to redirect an ID to another ID
   */
  protected void addRedirect(MaterialId id, @Nullable ConditionJsonProvider condition, JsonRedirect... redirect) {
    this.allMaterials.put(id, new DataMaterial(null, condition, redirect));
  }

  /**
   * Adds JSON to redirect an ID to another ID
   */
  protected void addRedirect(MaterialId id, JsonRedirect... redirect) {
    this.addRedirect(id, null, redirect);
  }

  /* Material helpers */

  /**
   * Conditions on a forge tag existing
   */
  protected static ConditionJsonProvider tagExistsCondition(String name) {
    return DefaultResourceConditions.itemTagsPopulated(TagKey.create(Registries.ITEM, new ResourceLocation("c", name)));
  }

  /**
   * Creates a normal material with a condition and a redirect
   */
  protected void addMaterial(MaterialId location, int tier, int order, boolean craftable, boolean hidden, @Nullable ConditionJsonProvider condition, JsonRedirect... redirect) {
    this.addMaterial(new Material(location, tier, order, craftable, hidden), condition, redirect);
  }

  /**
   * Creates a normal material
   */
  protected void addMaterial(MaterialId location, int tier, int order, boolean craftable) {
    this.addMaterial(location, tier, order, craftable, false, null);
  }

  /**
   * Creates a new compat material
   */
  protected void addCompatMaterial(MaterialId location, int tier, int order, String tagName, boolean craftable) {
    ConditionJsonProvider condition = DefaultResourceConditions.or(ConfigEnabledCondition.FORCE_INTEGRATION_MATERIALS, tagExistsCondition(tagName));
    this.addMaterial(location, tier, order, craftable, false, condition);
  }

  /**
   * Creates a new compat material
   */
  protected void addCompatMetalMaterial(MaterialId location, int tier, int order, String ingotName) {
    this.addCompatMaterial(location, tier, order, ingotName + "_ingots", false);
  }

  /**
   * Creates a new compat material
   */
  protected void addCompatMetalMaterial(MaterialId location, int tier, int order) {
    this.addCompatMetalMaterial(location, tier, order, location.getPath());
  }


  /* Redirect helpers */

  /**
   * Makes a conditional redirect to the given ID
   */
  protected JsonRedirect conditionalRedirect(MaterialId id, @Nullable ConditionJsonProvider condition) {
    return new JsonRedirect(id, condition, null);
  }

  /**
   * Makes an unconditional redirect to the given ID
   */
  protected JsonRedirect redirect(MaterialId id) {
    return this.conditionalRedirect(id, null);
  }


  /* Helpers */

  /**
   * Converts a material to JSON
   *
   * @param data Data to save
   * @return Material JSON
   */
  private MaterialJson convert(DataMaterial data) {
    IMaterial material = data.material;
    JsonRedirect[] redirect = data.redirect;
    if (redirect != null && redirect.length == 0) {
      redirect = null;
    }
    if (material == null) {
      return new MaterialJson(new JsonCondition(data.condition), null, null, null, null, redirect);
    }
    return new MaterialJson(new JsonCondition(data.condition), material.isCraftable(), material.getTier(), material.getSortOrder(), material.isHidden(), redirect);
  }

  private record DataMaterial(@Nullable IMaterial material, @Nullable ConditionJsonProvider condition,
                              JsonRedirect[] redirect) {}
}
