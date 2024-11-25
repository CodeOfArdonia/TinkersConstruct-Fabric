package slimeknights.tconstruct.library.recipe.alloying;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Builder for alloy recipes
 */
@SuppressWarnings("unused")
@RequiredArgsConstructor(staticName = "alloy")
public class AlloyRecipeBuilder extends AbstractRecipeBuilder<AlloyRecipeBuilder> {

  private final FluidStack output;
  private final int temperature;
  private final List<FluidIngredient> inputs = new ArrayList<>();

  /**
   * Creates a new recipe producing the given fluid
   *
   * @param fluid Fluid output
   * @return Builder instance
   */
  public static AlloyRecipeBuilder alloy(FluidStack fluid) {
    return alloy(fluid, FluidVariantAttributes.getTemperature(fluid.getType()) - 300);
  }

  /**
   * Creates a new recipe producing the given fluid
   *
   * @param fluid  Fluid output
   * @param amount Output amount
   * @return Builder instance
   */
  public static AlloyRecipeBuilder alloy(Fluid fluid, long amount) {
    return alloy(new FluidStack(fluid, amount));
  }


  /* Inputs */

  /**
   * Adds an input
   *
   * @param input Input ingredient
   * @return Builder instance
   */
  public AlloyRecipeBuilder addInput(FluidIngredient input) {
    this.inputs.add(input);
    return this;
  }

  /**
   * Adds an input
   *
   * @param input Input fluid
   * @return Builder instance
   */
  public AlloyRecipeBuilder addInput(FluidStack input) {
    return this.addInput(FluidIngredient.of(input));
  }

  /**
   * Adds an input
   *
   * @param fluid  Input fluid
   * @param amount Input amount
   * @return Builder instance
   */
  public AlloyRecipeBuilder addInput(Fluid fluid, long amount) {
    return this.addInput(FluidIngredient.of(new FluidStack(fluid, amount)));
  }

  /**
   * Adds an input
   *
   * @param tag    Input tag
   * @param amount Input amount
   * @return Builder instance
   */
  public AlloyRecipeBuilder addInput(TagKey<Fluid> tag, long amount) {
    return this.addInput(FluidIngredient.of(tag, amount));
  }


  /* Building */

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, Objects.requireNonNull(BuiltInRegistries.FLUID.getKey(this.output.getFluid())));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (this.inputs.size() < 2) {
      throw new IllegalStateException("Invalid alloying recipe " + id + ", must have at least two inputs");
    }
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "alloys");
    consumer.accept(new Result(id, advancementId));
  }

  /**
   * Result class for the builder
   */
  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      JsonArray inputArray = new JsonArray();
      for (FluidIngredient input : AlloyRecipeBuilder.this.inputs) {
        inputArray.add(input.serialize());
      }
      json.add("inputs", inputArray);
      json.add("result", RecipeHelper.serializeFluidStack(AlloyRecipeBuilder.this.output));
      json.addProperty("temperature", AlloyRecipeBuilder.this.temperature);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerSmeltery.alloyingSerializer.get();
    }
  }
}
