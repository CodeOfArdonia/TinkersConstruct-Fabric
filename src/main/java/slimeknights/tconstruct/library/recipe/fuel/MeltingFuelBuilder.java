package slimeknights.tconstruct.library.recipe.fuel;

import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builds a new recipe for a melter or smeltery fuel
 */
@SuppressWarnings("removal")
@AllArgsConstructor(staticName = "fuel")
public class MeltingFuelBuilder extends AbstractRecipeBuilder<MeltingFuelBuilder> {

  private final FluidIngredient input;
  private final int duration;
  private final int temperature;

  /**
   * Creates a new builder instance with automatic temperature
   *
   * @param fluid    Fluid stack
   * @param duration Fluid duration
   * @return Builder instance
   */
  public static MeltingFuelBuilder fuel(FluidStack fluid, int duration) {
    return fuel(FluidIngredient.of(fluid), duration, FluidVariantAttributes.getTemperature(fluid.getType()) - 300);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    if (this.input.getFluids().isEmpty()) {
      throw new IllegalStateException("Must have at least one fluid for dynamic input");
    }
    this.save(consumer, BuiltInRegistries.FLUID.getKey(this.input.getFluids().get(0).getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "melting_fuel");
    consumer.accept(new Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!MeltingFuelBuilder.this.group.isEmpty()) {
        json.addProperty("group", MeltingFuelBuilder.this.group);
      }
      json.add("fluid", MeltingFuelBuilder.this.input.serialize());
      json.addProperty("duration", MeltingFuelBuilder.this.duration);
      json.addProperty("temperature", MeltingFuelBuilder.this.temperature);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerSmeltery.fuelSerializer.get();
    }
  }
}
