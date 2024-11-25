package slimeknights.tconstruct.library.recipe.material;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Builder for a recipe to determine the material from an input
 */
@RequiredArgsConstructor(staticName = "materialRecipe")
@Accessors(chain = true)
public class MaterialRecipeBuilder extends AbstractRecipeBuilder<MaterialRecipeBuilder> {

  private final MaterialVariantId material;
  private Ingredient ingredient = Ingredient.EMPTY;
  @Setter
  private int value = 1;
  @Setter
  private int needed = 1;
  @Setter
  private ItemOutput leftover = null;

  /**
   * Sets the input ingredient for this material recipe
   *
   * @param tag Tag input
   * @return Builder instance
   */
  public MaterialRecipeBuilder setIngredient(TagKey<Item> tag) {
    return this.setIngredient(Ingredient.of(tag));
  }

  /**
   * Sets the input ingredient for this material recipe
   *
   * @param item Item input
   * @return Builder instance
   */
  public MaterialRecipeBuilder setIngredient(ItemLike item) {
    return this.setIngredient(Ingredient.of(item));
  }

  /**
   * Sets the input ingredient for this material recipe
   *
   * @param ingredient Ingredient input
   * @return Builder instance
   */
  public MaterialRecipeBuilder setIngredient(Ingredient ingredient) {
    this.ingredient = ingredient;
    return this;
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumerIn) {
    this.save(consumerIn, this.material.getId());
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumerIn, ResourceLocation id) {
    if (this.material == null) {
      throw new IllegalStateException("recipe " + id + " has no material associated with it");
    }
    if (this.ingredient == Ingredient.EMPTY) {
      throw new IllegalStateException("recipe " + id + " must have ingredient set");
    }
    if (this.value <= 0) {
      throw new IllegalStateException("recipe " + id + " has no value associated with it");
    }
    if (this.needed <= 0) {
      throw new IllegalStateException("recipe " + id + " has no needed associated with it");
    }
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "materials");
    consumerIn.accept(new Result(id, advancementId));
  }

  private class Result extends AbstractFinishedRecipe {

    public Result(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      if (!MaterialRecipeBuilder.this.group.isEmpty()) {
        json.addProperty("group", MaterialRecipeBuilder.this.group);
      }
      json.add("ingredient", MaterialRecipeBuilder.this.ingredient.toJson());
      json.addProperty("value", MaterialRecipeBuilder.this.value);
      json.addProperty("needed", MaterialRecipeBuilder.this.needed);
      json.addProperty("material", MaterialRecipeBuilder.this.material.toString());
      if (MaterialRecipeBuilder.this.value > 1 && MaterialRecipeBuilder.this.leftover != null) {
        json.add("leftover", MaterialRecipeBuilder.this.leftover.serialize());
      }
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerTables.materialRecipeSerializer.get();
    }
  }
}
