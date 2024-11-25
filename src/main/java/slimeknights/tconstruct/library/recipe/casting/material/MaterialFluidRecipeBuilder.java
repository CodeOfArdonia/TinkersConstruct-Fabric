package slimeknights.tconstruct.library.recipe.casting.material;

import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builder to make parts and composites castable
 */
@SuppressWarnings("removal")
@Accessors(chain = true)
@RequiredArgsConstructor(staticName = "material")
public class MaterialFluidRecipeBuilder extends AbstractRecipeBuilder<MaterialFluidRecipeBuilder> {

  /**
   * Output material ID
   */
  private final MaterialVariantId outputId;
  /**
   * Fluid used for casting
   */
  @Setter
  private FluidIngredient fluid = FluidIngredient.EMPTY;
  /**
   * Temperature for cooling time calculations
   */
  @Setter
  private int temperature = -1;
  /**
   * Material base for composite
   */
  @Setter
  @Nullable
  private MaterialVariantId inputId;

  /**
   * Sets the fluid for this recipe, and cooling time if unset.
   *
   * @param fluidStack Fluid input
   * @return Builder instance
   */
  public MaterialFluidRecipeBuilder setFluidAndTemp(FluidStack fluidStack) {
    this.fluid = FluidIngredient.of(fluidStack);
    if (this.temperature == -1) {
      this.temperature = FluidVariantAttributes.getTemperature(fluidStack.getType()) - 300;
    }
    return this;
  }

  /**
   * Sets the fluid for this recipe, and cooling time
   *
   * @param tagIn  Tag<Fluid> instance
   * @param amount Fluid amount
   */
  public MaterialFluidRecipeBuilder setFluid(TagKey<Fluid> tagIn, long amount) {
    this.setFluid(FluidIngredient.of(tagIn, amount));
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, this.outputId.getId());
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (this.fluid == FluidIngredient.EMPTY) {
      throw new IllegalStateException("Material fluid recipes require a fluid input");
    }
    if (this.temperature < 0) {
      throw new IllegalStateException("Temperature is too low, must be at least 0");
    }
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "materials");
    consumer.accept(new Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (MaterialFluidRecipeBuilder.this.inputId != null) {
        json.addProperty("input", MaterialFluidRecipeBuilder.this.inputId.toString());
      }
      json.add("fluid", MaterialFluidRecipeBuilder.this.fluid.serialize());
      json.addProperty("temperature", MaterialFluidRecipeBuilder.this.temperature);
      json.addProperty("output", MaterialFluidRecipeBuilder.this.outputId.toString());
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerSmeltery.materialFluidRecipe.get();
    }
  }
}
