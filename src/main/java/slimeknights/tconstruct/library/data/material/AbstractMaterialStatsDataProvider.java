package slimeknights.tconstruct.library.data.material;

import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.json.MaterialStatJson;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Base data generator for use in addons, depends on the regular material provider
 */
public abstract class AbstractMaterialStatsDataProvider extends GenericDataProvider {

  /**
   * All material stats generated so far
   */
  private final Map<MaterialId, List<IMaterialStats>> allMaterialStats = new HashMap<>();
  /* Materials data provider for validation */
  private final AbstractMaterialDataProvider materials;

  public AbstractMaterialStatsDataProvider(FabricDataOutput output, AbstractMaterialDataProvider materials) {
    super(output, MaterialStatsManager.FOLDER);
    this.materials = materials;
  }

  /**
   * Adds all relevant material stats
   */
  protected abstract void addMaterialStats();

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    this.addMaterialStats();

    // ensure we have stats for all materials
    Set<MaterialId> materialsGenerated = this.materials.getAllMaterials();
    for (MaterialId material : materialsGenerated) {
      if (!this.allMaterialStats.containsKey(material)) {
        throw new IllegalStateException(String.format("Missing material stats for '%s'", material));
      }
    }
    // does not ensure we have materials for all stats, we may be adding stats for another mod
    // generate finally
    List<CompletableFuture<?>> futures = new ArrayList<>();
    this.allMaterialStats.forEach((materialId, materialStats) -> futures.add(this.saveThing(cache, materialId, this.convert(materialStats))));
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }


  /* Helpers */

  /**
   * Adds a set of material stats for the given material ID
   *
   * @param location Material ID
   * @param stats    Stats to add
   */
  protected void addMaterialStats(MaterialId location, IMaterialStats... stats) {
    this.allMaterialStats.computeIfAbsent(location, materialId -> new ArrayList<>())
      .addAll(Arrays.asList(stats));
  }

  /* Internal */

  /**
   * Converts a material and stats list to a JSON
   */
  private JsonWrapper convert(List<IMaterialStats> stats) {
    Map<ResourceLocation, IMaterialStats> wrappedStats = stats.stream()
      .collect(Collectors.toMap(
        IMaterialStats::getIdentifier,
        stat -> stat));
    return new JsonWrapper(wrappedStats);
  }

  /**
   * Separate json wrapper for serialization, since we know the types here.
   * See {@link MaterialStatJson} for its deserialization counterpart.
   */
  @SuppressWarnings("unused")
  @AllArgsConstructor
  private static class JsonWrapper {

    private final Map<ResourceLocation, IMaterialStats> stats;
  }
}
