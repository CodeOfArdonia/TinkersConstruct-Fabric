package slimeknights.tconstruct.library.recipe.casting.container;

import com.google.gson.JsonObject;
import lombok.AllArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builder for a container filling recipe. Takes an arbitrary fluid for a specific amount to fill a Forge {@link net.minecraftforge.fluids.capability.IFluidHandlerItem}
 */
@AllArgsConstructor(staticName = "castingRecipe")
@SuppressWarnings({"WeakerAccess", "unused"})
public class ContainerFillingRecipeBuilder extends AbstractRecipeBuilder<ContainerFillingRecipeBuilder> {

  private final ResourceLocation result;
  private final long fluidAmount;
  private final ContainerFillingRecipeSerializer<?> recipeSerializer;

  /**
   * Creates a new builder instance using the given result, amount, and serializer
   *
   * @param result           Recipe result
   * @param fluidAmount      Container size
   * @param recipeSerializer Serializer
   * @return Builder instance
   */
  public static ContainerFillingRecipeBuilder castingRecipe(ItemLike result, long fluidAmount, ContainerFillingRecipeSerializer<?> recipeSerializer) {
    return new ContainerFillingRecipeBuilder(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(result.asItem())), fluidAmount, recipeSerializer);
  }

  /**
   * Creates a new basin recipe builder using the given result, amount, and serializer
   *
   * @param result      Recipe result
   * @param fluidAmount Container size
   * @return Builder instance
   */
  public static ContainerFillingRecipeBuilder basinRecipe(ResourceLocation result, long fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.basinFillingRecipeSerializer.get());
  }

  /**
   * Creates a new basin recipe builder using the given result, amount, and serializer
   *
   * @param result      Recipe result
   * @param fluidAmount Container size
   * @return Builder instance
   */
  public static ContainerFillingRecipeBuilder basinRecipe(ItemLike result, long fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.basinFillingRecipeSerializer.get());
  }

  /**
   * Creates a new table recipe builder using the given result, amount, and serializer
   *
   * @param result      Recipe result
   * @param fluidAmount Container size
   * @return Builder instance
   */
  public static ContainerFillingRecipeBuilder tableRecipe(ResourceLocation result, long fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.tableFillingRecipeSerializer.get());
  }

  /**
   * Creates a new table recipe builder using the given result, amount, and serializer
   *
   * @param result      Recipe result
   * @param fluidAmount Container size
   * @return Builder instance
   */
  public static ContainerFillingRecipeBuilder tableRecipe(ItemLike result, long fluidAmount) {
    return castingRecipe(result, fluidAmount, TinkerSmeltery.tableFillingRecipeSerializer.get());
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, this.result);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "casting");
    consumerIn.accept(new ContainerFillingRecipeBuilder.Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return ContainerFillingRecipeBuilder.this.recipeSerializer;
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!ContainerFillingRecipeBuilder.this.group.isEmpty()) {
        json.addProperty("group", ContainerFillingRecipeBuilder.this.group);
      }
      json.addProperty("fluid_amount", ContainerFillingRecipeBuilder.this.fluidAmount);
      json.addProperty("container", ContainerFillingRecipeBuilder.this.result.toString());
    }
  }
}
