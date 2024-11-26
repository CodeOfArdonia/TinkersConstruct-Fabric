package slimeknights.mantle.recipe.data;

import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Utilities to help in the creation of recipes
 * @deprecated use {@link IRecipeHelper}
 */
@Deprecated
@SuppressWarnings("unused")
public interface IRecipeBuilderUtils {
  /**
   * Gets the mod ID for this recipe builder
   * @return  Mod ID
   */
  String getModId();

  /**
   * Gets the base recipe consumer
   * @return Base recipe consumer
   */
  Consumer<FinishedRecipe> getConsumer();

  /**
   * Gets the base condition for the condition utility
   */
  @Nullable
  default ConditionJsonProvider baseCondition() {
    return null;
  }

  /**
   * Gets a resource location under the Inspirations mod ID
   * @param name Resource path
   * @return Resource location for Inspirations
   */
  default ResourceLocation resource(String name) {
    return new ResourceLocation(getModId(), name);
  }

  /**
   * Gets a resource location string for the given path
   * @param name Resource path
   * @return Resource location string Inspirations
   */
  default String resourceName(String name) {
    return String.format("%s:%s", getModId(), name);
  }

  /**
   * Prefixes an items resource location with the given folder
   * @param item   Item to fetch resource location from
   * @param prefix Name to prefix location with
   * @return Prefixed resource location
   */
  default ResourceLocation prefix(ItemLike item, String prefix) {
    return resource(prefix + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath());
  }

  /**
   * Wraps an items resource location with the given folder and suffix
   * @param item   Item to fetch resource location from
   * @param prefix Name to prefix location with
   * @param suffix Suffix for location
   * @return Prefixed resource location
   */
  default ResourceLocation wrap(ItemLike item, String prefix, String suffix) {
    return resource(prefix + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem())).getPath() + suffix);
  }

  /**
   * Gets a consumer with the given condition, plus the module condition
   * @param conditions Conditions to add
   * @return Consumer with condition
   */
  default Consumer<FinishedRecipe> withCondition(ConditionJsonProvider... conditions) {
    ConsumerWrapperBuilder builder = ConsumerWrapperBuilder.wrap();
    ConditionJsonProvider base = baseCondition();
    if (base != null) {
      builder.addCondition(base);
    }
    for (ConditionJsonProvider condition : conditions) {
      builder.addCondition(condition);
    }
    return builder.build(Objects.requireNonNull(getConsumer()));
  }
}
