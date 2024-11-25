package slimeknights.tconstruct.library.recipe.melting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Builder for a recipe that melts an ingredient into a fuel
 */
@SuppressWarnings("removal")
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MeltingRecipeBuilder extends AbstractRecipeBuilder<MeltingRecipeBuilder> {

  private final Ingredient input;
  private final FluidStack output;
  private final int temperature;
  private final int time;
  @Nullable
  private OreRateType oreRate = null;
  @Nullable
  private OreRateType[] byproductRates = null;
  @Nullable
  private long[] unitSizes;
  private final List<FluidStack> byproducts = new ArrayList<>();

  /**
   * Creates a new builder instance using a specific temperature
   *
   * @param input       Recipe input
   * @param output      Recipe output
   * @param temperature Temperature required
   * @param time        Time this recipe takes
   * @return Builder instance
   */
  public static MeltingRecipeBuilder melting(Ingredient input, FluidStack output, int temperature, int time) {
    if (temperature < 0)
      throw new IllegalArgumentException("Invalid temperature " + temperature + ", must be greater than zero");
    if (time <= 0) throw new IllegalArgumentException("Invalid time " + time + ", must be greater than zero");
    return new MeltingRecipeBuilder(input, output, temperature, time);
  }

  /**
   * Creates a new builder instance using a specific temperature
   *
   * @param input      Recipe input
   * @param output     Recipe output
   * @param timeFactor Factor this recipe takes compared to the standard of ingots
   * @return Builder instance
   */
  public static MeltingRecipeBuilder melting(Ingredient input, FluidStack output, float timeFactor) {
    int temperature = FluidVariantAttributes.getTemperature(output.getType()) - 300;
    return melting(input, output, temperature, IMeltingRecipe.calcTime(temperature, timeFactor));
  }

  /**
   * Creates a new builder instance using a specific temperature
   *
   * @param input      Recipe input
   * @param fluid      Fluid result
   * @param amount     Fluid returned from recipe
   * @param timeFactor Factor this recipe takes compared to the standard of ingots
   * @return Builder instance
   */
  public static MeltingRecipeBuilder melting(Ingredient input, Fluid fluid, long amount, float timeFactor) {
    return melting(input, new FluidStack(fluid, amount), timeFactor);
  }

  /**
   * Creates a new builder instance using a specific temperature
   *
   * @param input  Recipe input
   * @param fluid  Fluid result
   * @param amount Fluid returned from recipe
   * @return Builder instance
   */
  public static MeltingRecipeBuilder melting(Ingredient input, Fluid fluid, long amount) {
    return melting(input, new FluidStack(fluid, amount), IMeltingRecipe.calcTimeFactor(amount));
  }

  /**
   * Sets this recipe as an ore recipe, output multiplied based on the melter
   *
   * @return Builder instance
   */
  public MeltingRecipeBuilder setOre(OreRateType rate, OreRateType... byproductRates) {
    this.oreRate = rate;
    this.byproductRates = byproductRates.length == 0 ? null : byproductRates;
    return this;
  }

  /**
   * Marks this item as damagable, the output should scale based on the input damage
   *
   * @return Builder instance
   */
  public MeltingRecipeBuilder setDamagable(long... unitSizes) {
    this.unitSizes = unitSizes;
    return this;
  }

  /**
   * Adds a byproduct to this recipe
   *
   * @param fluidStack Byproduct to add
   * @return Builder instance
   */
  public MeltingRecipeBuilder addByproduct(FluidStack fluidStack) {
    this.byproducts.add(fluidStack);
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, BuiltInRegistries.FLUID.getKey(this.output.getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    if (this.oreRate != null && this.unitSizes != null) {
      throw new IllegalStateException("Builder cannot be both ore and damagable");
    }
    // only build JSON if needed
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "melting");
    consumer.accept(new Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (MeltingRecipeBuilder.this.oreRate != null) {
        json.addProperty("rate", MeltingRecipeBuilder.this.oreRate.getName());
      }
      if (!MeltingRecipeBuilder.this.group.isEmpty()) {
        json.addProperty("group", MeltingRecipeBuilder.this.group);
      }
      json.add("ingredient", MeltingRecipeBuilder.this.input.toJson());
      JsonObject result = RecipeHelper.serializeFluidStack(MeltingRecipeBuilder.this.output);
      if (MeltingRecipeBuilder.this.unitSizes != null) {
        if (MeltingRecipeBuilder.this.unitSizes.length > 0) {
          result.addProperty("unit_size", MeltingRecipeBuilder.this.unitSizes[0]);
        } else {
          result.addProperty("unit_size", 1);
        }
      }
      json.add("result", result);
      json.addProperty("temperature", MeltingRecipeBuilder.this.temperature);
      json.addProperty("time", MeltingRecipeBuilder.this.time);
      if (!MeltingRecipeBuilder.this.byproducts.isEmpty()) {
        JsonArray array = new JsonArray();
        for (int i = 0; i < MeltingRecipeBuilder.this.byproducts.size(); i++) {
          FluidStack fluidStack = MeltingRecipeBuilder.this.byproducts.get(i);
          JsonObject byproduct = RecipeHelper.serializeFluidStack(fluidStack);
          if (MeltingRecipeBuilder.this.unitSizes != null && i <= MeltingRecipeBuilder.this.unitSizes.length) {
            byproduct.addProperty("unit_size", MeltingRecipeBuilder.this.unitSizes[i + 1]);
          } else if (MeltingRecipeBuilder.this.oreRate != null && MeltingRecipeBuilder.this.byproductRates != null && i < MeltingRecipeBuilder.this.byproductRates.length) {
            OreRateType rate = MeltingRecipeBuilder.this.byproductRates[i];
            if (rate != null) {
              byproduct.addProperty("rate", rate.getName());
            }
          }
          array.add(byproduct);
        }
        json.add("byproducts", array);
      }
    }

    @Override
    public RecipeSerializer<?> getType() {
      if (MeltingRecipeBuilder.this.oreRate != null) {
        return TinkerSmeltery.oreMeltingSerializer.get();
      }
      if (MeltingRecipeBuilder.this.unitSizes != null) {
        return TinkerSmeltery.damagableMeltingSerializer.get();
      }
      return TinkerSmeltery.meltingSerializer.get();
    }
  }
}
