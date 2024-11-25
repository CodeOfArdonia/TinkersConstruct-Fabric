package slimeknights.tconstruct.library.recipe.partbuilder;

import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import slimeknights.mantle.recipe.data.AbstractRecipeBuilder;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.tables.TinkerTables;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@RequiredArgsConstructor(staticName = "item")
public class ItemPartRecipeBuilder extends AbstractRecipeBuilder<ItemPartRecipeBuilder> {

  private final MaterialId materialId;
  private final ResourceLocation pattern;
  private final int cost;
  private final ItemOutput result;
  @Setter
  @Accessors(chain = true)
  private Ingredient patternItem;

  @Override
  public void save(Consumer<FinishedRecipe> consumer) {
    this.save(consumer, BuiltInRegistries.ITEM.getKey(this.result.get().getItem()));
  }

  @Override
  public void save(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
    ResourceLocation advancementId = this.buildOptionalAdvancement(id, "parts");
    consumer.accept(new Finished(id, advancementId));
  }

  private class Finished extends AbstractFinishedRecipe {

    public Finished(ResourceLocation ID, @Nullable ResourceLocation advancementID) {
      super(ID, advancementID);
    }

    @Override
    public void serializeRecipeData(JsonObject json) {
      json.addProperty("material", ItemPartRecipeBuilder.this.materialId.toString());
      json.addProperty("pattern", ItemPartRecipeBuilder.this.pattern.toString());
      if (ItemPartRecipeBuilder.this.patternItem != null) {
        json.add("pattern_item", ItemPartRecipeBuilder.this.patternItem.toJson());
      }
      json.addProperty("cost", ItemPartRecipeBuilder.this.cost);
      json.add("result", ItemPartRecipeBuilder.this.result.serialize());
    }

    @Override
    public RecipeSerializer<?> getType() {
      return TinkerTables.itemPartBuilderSerializer.get();
    }
  }
}
