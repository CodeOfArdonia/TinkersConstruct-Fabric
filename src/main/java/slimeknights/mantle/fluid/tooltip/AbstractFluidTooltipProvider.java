package slimeknights.mantle.fluid.tooltip;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.Util;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.GenericDataProvider;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Provider for fluid tooltip information
 */
@SuppressWarnings("unused")
public abstract class AbstractFluidTooltipProvider extends GenericDataProvider {

  private final Map<ResourceLocation, ResourceLocation> redirects = new HashMap<>();
  private final Map<ResourceLocation, FluidUnitListBuilder> builders = new HashMap<>();
  private final String modId;

  public AbstractFluidTooltipProvider(FabricDataOutput generator, String modId) {
    super(generator, PackType.CLIENT_RESOURCES, FluidTooltipHandler.FOLDER, FluidTooltipHandler.GSON);
    this.modId = modId;
  }

  /**
   * Adds all relevant fluids to the maps
   */
  protected abstract void addFluids();

  @Override
  public final CompletableFuture<?> run(CachedOutput cache) {
    this.addFluids();
    final List<CompletableFuture<?>> futures = new ArrayList<>();
    this.builders.forEach((key, builder) -> futures.add(this.saveThing(cache, key, builder.build())));
    this.redirects.forEach((key, target) -> {
      JsonObject json = new JsonObject();
      json.addProperty("redirect", target.toString());
      futures.add(this.saveThing(cache, key, json));
    });
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }


  /* Helpers */

  /**
   * Creates a ResourceLocation for the local mod
   */
  protected ResourceLocation id(String name) {
    return new ResourceLocation(this.modId, name);
  }

  /**
   * Adds a fluid to the builder
   */
  protected FluidUnitListBuilder add(ResourceLocation id, @Nullable TagKey<Fluid> tag) {
    if (this.redirects.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a redirect");
    }
    FluidUnitListBuilder newBuilder = new FluidUnitListBuilder(tag);
    FluidUnitListBuilder original = this.builders.put(id, newBuilder);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already registered");
    }
    return newBuilder;
  }

  /**
   * Adds a fluid to the builder
   */
  protected FluidUnitListBuilder add(String id, TagKey<Fluid> tag) {
    return this.add(this.id(id), tag);
  }

  /**
   * Adds a fluid to the builder using the tag name as the ID
   */
  protected FluidUnitListBuilder add(TagKey<Fluid> tag) {
    return this.add(this.id(tag.location().getPath()), tag);
  }

  /**
   * Adds a fluid to the builder with no tag
   */
  protected FluidUnitListBuilder add(ResourceLocation id) {
    return this.add(id, null);
  }

  /**
   * Adds a fluid to the builder with no tag
   */
  protected FluidUnitListBuilder add(String id) {
    return this.add(this.id(id), null);
  }

  /**
   * Adds a redirect from a named builder to a target
   */
  protected void addRedirect(ResourceLocation id, ResourceLocation target) {
    if (this.builders.containsKey(id)) {
      throw new IllegalArgumentException(id + " is already registered as a unit list");
    }
    ResourceLocation original = this.redirects.put(id, target);
    if (original != null) {
      throw new IllegalArgumentException(id + " is already redirecting to " + original);
    }
  }

  /**
   * Builder for a unit list
   */
  @SuppressWarnings("unused")
  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  protected class FluidUnitListBuilder {

    @Nullable
    private final TagKey<Fluid> tag;
    private final ImmutableList.Builder<FluidUnit> units = ImmutableList.builder();

    /**
     * Adds a unit with a full translation key
     */
    public FluidUnitListBuilder addUnitRaw(String key, long amount) {
      this.units.add(new FluidUnit(key, amount));
      return this;
    }

    /**
     * Adds a unit local to the current mod
     */
    public FluidUnitListBuilder addUnit(String key, long amount) {
      return this.addUnitRaw(Util.makeDescriptionId("gui", AbstractFluidTooltipProvider.this.id("fluid." + key)), amount);
    }

    /**
     * Adds a unit local to the given mod
     */
    public FluidUnitListBuilder addUnit(String key, String domain, long amount) {
      return this.addUnitRaw(Util.makeDescriptionId("gui", new ResourceLocation(domain, "fluid." + key)), amount);
    }

    /**
     * Builds the final instance
     */
    private FluidUnitList build() {
      return new FluidUnitList(this.tag, this.units.build());
    }
  }
}
