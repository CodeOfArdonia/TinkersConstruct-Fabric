package slimeknights.tconstruct.library.recipe.entitymelting;

import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builder for entity melting recipes
 */
@RequiredArgsConstructor(staticName = "melting")
public class EntityMeltingRecipeBuilder extends AbstractRecipeBuilder<EntityMeltingRecipeBuilder> {

  private final EntityIngredient ingredient;
  private final FluidStack output;
  private final int damage;

  /**
   * Creates a new builder doing 2 damage
   */
  public static EntityMeltingRecipeBuilder melting(EntityIngredient ingredient, FluidStack output) {
    return melting(ingredient, output, 2);
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, BuiltInRegistries.FLUID.getKey(this.output.getFluid()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "entity_melting");
    consumer.accept(new Finished(id, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {

    public Finished(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.add("entity", EntityMeltingRecipeBuilder.this.ingredient.serialize());
      json.add("result", RecipeHelper.serializeFluidStack(EntityMeltingRecipeBuilder.this.output));
      json.addProperty("damage", EntityMeltingRecipeBuilder.this.damage);
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerSmeltery.entityMeltingSerializer.get();
    }
  }
}
