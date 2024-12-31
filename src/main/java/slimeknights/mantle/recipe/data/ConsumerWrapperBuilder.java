package slimeknights.mantle.recipe.data;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.ResourceConditions;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builds a recipe consumer wrapper, which adds some extra properties to wrap the result of another recipe
 */
@SuppressWarnings("UnusedReturnValue")
public class ConsumerWrapperBuilder {

  private final List<ConditionJsonProvider> conditions = new ArrayList<>();
  @Nullable
  private final RecipeSerializer<?> override;
  @Nullable
  private final ResourceLocation overrideName;

  private ConsumerWrapperBuilder(@Nullable RecipeSerializer<?> override, @Nullable ResourceLocation overrideName) {
    this.override = override;
    this.overrideName = overrideName;
  }

  /**
   * Creates a wrapper builder with the default serializer
   *
   * @return Default serializer builder
   */
  public static ConsumerWrapperBuilder wrap() {
    return new ConsumerWrapperBuilder(null, null);
  }

  /**
   * Creates a wrapper builder with a serializer override
   *
   * @param override Serializer override
   * @return Default serializer builder
   */
  public static ConsumerWrapperBuilder wrap(RecipeSerializer<?> override) {
    return new ConsumerWrapperBuilder(override, null);
  }

  /**
   * Creates a wrapper builder with a serializer name override
   *
   * @param override Serializer override
   * @return Default serializer builder
   */
  public static ConsumerWrapperBuilder wrap(ResourceLocation override) {
    return new ConsumerWrapperBuilder(null, override);
  }

  /**
   * Adds a conditional to the consumer
   *
   * @param condition Condition to add
   * @return Added condition
   */
  public ConsumerWrapperBuilder addCondition(ConditionJsonProvider condition) {
    this.conditions.add(condition);
    return this;
  }

  /**
   * Builds the consumer for the wrapper builder
   *
   * @param consumer Base consumer
   * @return Built wrapper consumer
   */
  public Consumer<FinishedRecipe> build(Consumer<FinishedRecipe> consumer) {
    return (recipe) -> consumer.accept(new Wrapped(recipe, this.conditions, this.override, this.overrideName));
  }

  private static class Wrapped implements FinishedRecipe {

    private final FinishedRecipe original;
    private final List<ConditionJsonProvider> conditions;
    @Nullable
    private final RecipeSerializer<?> override;
    @Nullable
    private final ResourceLocation overrideName;

    private Wrapped(FinishedRecipe original, List<ConditionJsonProvider> conditions, @Nullable RecipeSerializer<?> override, @Nullable ResourceLocation overrideName) {
      // if wrapping another wrapper result, merge the two together
      if (original instanceof Wrapped toMerge) {
        this.original = toMerge.original;
        this.conditions = ImmutableList.<ConditionJsonProvider>builder().addAll(toMerge.conditions).addAll(conditions).build();
        // consumer wrappers are processed inside out, so the innermost wrapped recipe is the one with the most recent serializer override
        if (toMerge.override != null || toMerge.overrideName != null) {
          this.override = toMerge.override;
          this.overrideName = toMerge.overrideName;
        } else {
          this.override = override;
          this.overrideName = overrideName;
        }
      } else {
        this.original = original;
        this.conditions = conditions;
        this.override = override;
        this.overrideName = overrideName;
      }
    }

    @Override
    public JsonObject serializeRecipe() {
      JsonObject json = new JsonObject();
      if (this.overrideName != null) {
        json.addProperty("type", this.overrideName.toString());
      } else {
        json.addProperty("type", Objects.requireNonNull(BuiltInRegistries.RECIPE_SERIALIZER.getKey(this.getType())).toString());
      }
      this.serializeRecipeData(json);
      return json;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      // add conditions on top
      if (!this.conditions.isEmpty()) {
        JsonArray conditionsArray = new JsonArray();
        for (ConditionJsonProvider condition : this.conditions) {
          conditionsArray.add(condition.toJson());
        }
        json.add(ResourceConditions.CONDITIONS_KEY, conditionsArray);
      }
      // serialize the normal recipe
      this.original.serializeRecipeData(json);
    }

    @Override
    public ResourceLocation getId() {
      return this.original.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
      if (this.override != null) {
        return this.override;
      }
      return this.original.getType();
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
      return this.original.serializeAdvancement();
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
      return this.original.getAdvancementId();
    }
  }
}