package slimeknights.tconstruct.library.tools.definition;

import com.google.common.annotations.VisibleForTesting;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.IMaterialRegistry;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.stats.IRepairableMaterialStats;
import slimeknights.tconstruct.library.tools.nbt.MaterialNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;

import java.util.List;
import java.util.stream.IntStream;

/**
 * This class serves primarily as a container where the datapack tool data will be injected on datapack load
 */
public class ToolDefinition {

  /**
   * Empty tool definition instance to prevent the need for null for a fallback
   */
  public static final ToolDefinition EMPTY = new ToolDefinition(TConstruct.getResource("empty"), new IToolStatProvider() {
    @Override
    public StatsNBT buildStats(ToolDefinition definition, MaterialNBT materials) {
      return StatsNBT.EMPTY;
    }

    @Override
    public boolean isMultipart() {
      return false;
    }
  }, 0);

  @Getter
  private final ResourceLocation id;
  /**
   * Function to convert from tool definition and materials into tool stats
   */
  @Getter
  private final IToolStatProvider statProvider;

  /**
   * Max tier to pull materials from if uninitialized
   */
  @Getter
  private final int defaultMaxTier;

  /**
   * Base data loaded from JSON, contains stats, traits, and starting slots
   */
  @Getter
  protected ToolDefinitionData data;

  protected ToolDefinition(ResourceLocation id, IToolStatProvider statProvider, int defaultMaxTier) {
    this.id = id;
    this.statProvider = statProvider;
    this.defaultMaxTier = defaultMaxTier;
    this.data = statProvider.getDefaultData();
  }

  /**
   * Creates an tool definition builder
   *
   * @param id Tool definition ID
   * @return Definition builder
   */
  public static ToolDefinition.Builder builder(ResourceLocation id) {
    return new Builder(id);
  }

  /**
   * Creates an tool definition builder
   *
   * @param item Tool item
   * @return Definition builder
   */
  public static ToolDefinition.Builder builder(RegistryObject<? extends ItemLike> item) {
    return builder(item.getId());
  }

  /**
   * Creates an tool definition builder
   *
   * @param item Tool item
   * @return Definition builder
   */
  public static ToolDefinition.Builder builder(ItemObject<? extends ItemLike> item) {
    return builder(item.getRegistryName());
  }

  /**
   * Checks if the tool uses multipart stats, may not match {@link #getData()} if the JSON file was invalid
   */
  public boolean isMultipart() {
    return this.statProvider.isMultipart();
  }

  /**
   * Builds the stats for this tool definition
   *
   * @param materials Materials list
   * @return Stats NBT
   */
  public StatsNBT buildStats(MaterialNBT materials) {
    return this.statProvider.buildStats(this, materials);
  }


  /* Repairing */

  /**
   * Cached indices that can be used to repair this tool
   */
  private int[] repairIndices;

  /**
   * Largest weight of all repair parts
   */
  private Integer maxRepairWeight;

  /**
   * Returns a list of part material requirements for repair materials
   */
  public int[] getRepairParts() {
    if (this.repairIndices == null) {
      // get indices of all head parts
      List<PartRequirement> components = this.getData().getParts();
      if (components.isEmpty()) {
        this.repairIndices = new int[0];
      } else {
        IMaterialRegistry registry = MaterialRegistry.getInstance();
        this.repairIndices = IntStream.range(0, components.size())
          .filter(i -> registry.getDefaultStats(components.get(i).getStatType()) instanceof IRepairableMaterialStats)
          .toArray();
      }
    }
    return this.repairIndices;
  }

  /**
   * Gets the largest weight for all repair parts
   */
  public int getMaxRepairWeight() {
    if (this.maxRepairWeight == null) {
      int max = 1;
      List<PartRequirement> parts = this.getData().getParts();
      for (int i : this.getRepairParts()) {
        int cmp = parts.get(i).getWeight();
        if (cmp > max) {
          max = cmp;
        }
      }
      this.maxRepairWeight = max;
    }
    return this.maxRepairWeight;
  }


  /* Loader methods */

  /**
   * Validates the given tool data works with this tool definition. Throws if invalid
   */
  public void validate(ToolDefinitionData data) {
    this.statProvider.validate(data);
  }

  /**
   * Updates the data in this tool definition from the JSON loader, should not be called directly other than by the loader
   */
  @VisibleForTesting
  public void setData(ToolDefinitionData data) {
    this.data = data;
    // clear caches
    this.repairIndices = null;
    this.maxRepairWeight = null;
  }

  /**
   * Sets the tool data to the default, for the sake of erroring
   */
  protected void setDefaultData() {
    this.setData(this.statProvider.getDefaultData());
  }

  /**
   * If true, the definition data is loaded from the datapack, so we can expect it to be reliable. False typically means datapacks are not yet loaded (e.g. menu startup)
   */
  public boolean isDataLoaded() {
    return this.data != this.statProvider.getDefaultData();
  }

  /**
   * Builder to easily create a tool definition
   */
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    /**
     * ID for loading the tool definition data from datapacks
     */
    private final ResourceLocation id;
    /**
     * Stats provider for building the tool from tool parts
     */
    @Setter
    @Accessors(chain = true)
    private IToolStatProvider statsProvider;
    /**
     * If true, registers the material with the tool definition data loader
     */
    private boolean register = true;
    /**
     * Max tier to choose from for initializing tools with no materials, unused for non-multipart tools
     */
    @Setter
    @Accessors(chain = true)
    private int defaultMaxTier = 1;

    /**
     * Sets the tool to use a melee harvest tool stat provider, which requires at least 1 head part and uses any number of handle or bindings
     */
    public Builder meleeHarvest() {
      this.setStatsProvider(ToolStatProviders.MELEE_HARVEST);
      return this;
    }

    /**
     * Sets the tool to not use parts
     */
    public Builder noParts() {
      this.setStatsProvider(ToolStatProviders.NO_PARTS);
      return this;
    }

    /**
     * Sets the tool to use a ranged tool stat provider, which requires at least 1 limb part and uses any number of bowstrings
     */
    public Builder ranged() {
      this.setStatsProvider(ToolStatProviders.RANGED);
      return this;
    }

    /**
     * Tells the definition to not be registered with the loader, used internally for testing. In general mods wont need this
     */
    public Builder skipRegister() {
      this.register = false;
      return this;
    }

    /**
     * Builds the final tool definition
     *
     * @return Tool definition
     */
    public ToolDefinition build() {
      if (this.statsProvider == null) {
        throw new IllegalArgumentException("Stats provider is required for tools");
      }
      ToolDefinition definition = new ToolDefinition(this.id, this.statsProvider, this.defaultMaxTier);
      if (this.register) {
        ToolDefinitionLoader.getInstance().registerToolDefinition(definition);
      }
      return definition;
    }
  }
}
