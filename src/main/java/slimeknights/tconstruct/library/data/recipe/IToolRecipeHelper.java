package slimeknights.tconstruct.library.data.recipe;

import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.recipe.casting.material.CompositeCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.partbuilder.PartRecipeBuilder;
import slimeknights.tconstruct.library.recipe.tinkerstation.building.ToolBuildingRecipeBuilder;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Interface for tool and part crafting recipes
 */
public interface IToolRecipeHelper extends ICastCreationHelper {

  /**
   * Registers recipe for tool building
   *
   * @param consumer Recipe consumer
   * @param tool     Tool
   * @param folder   Folder for recipe
   */
  default void toolBuilding(Consumer<FinishedRecipe> consumer, IModifiable tool, String folder) {
    ToolBuildingRecipeBuilder.toolBuildingRecipe(tool)
      .save(consumer, this.modResource(folder + Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(tool.asItem())).getPath()));
  }

  /**
   * Registers recipe for tool building
   *
   * @param consumer Recipe consumer
   * @param tool     Tool supplier
   * @param folder   Folder for recipe
   */
  default void toolBuilding(Consumer<FinishedRecipe> consumer, Supplier<? extends IModifiable> tool, String folder) {
    this.toolBuilding(consumer, tool.get(), folder);
  }

  /**
   * Adds a recipe to craft a material item
   *
   * @param consumer   Recipe consumer
   * @param part       Part to be crafted
   * @param cast       Part cast
   * @param cost       Part cost
   * @param partFolder Folder for recipes
   */
  default void partRecipes(Consumer<FinishedRecipe> consumer, IMaterialItem part, CastItemObject cast, int cost, String partFolder, String castFolder) {
    String name = Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(part.asItem())).getPath();

    // Part Builder
    PartRecipeBuilder.partRecipe(part)
      .setPattern(this.modResource(name))
      .setPatternItem(DefaultCustomIngredients.any(Ingredient.of(TinkerTags.Items.DEFAULT_PATTERNS), Ingredient.of(cast.get())))
      .setCost(cost)
      .save(consumer, this.modResource(partFolder + "builder/" + name));

    // Material Casting
    String castingFolder = partFolder + "casting/";
    MaterialCastingRecipeBuilder.tableRecipe(part)
      .setItemCost(cost)
      .setCast(cast.getMultiUseTag(), false)
      .save(consumer, this.modResource(castingFolder + name + "_gold_cast"));
    MaterialCastingRecipeBuilder.tableRecipe(part)
      .setItemCost(cost)
      .setCast(cast.getSingleUseTag(), true)
      .save(consumer, this.modResource(castingFolder + name + "_sand_cast"));
    CompositeCastingRecipeBuilder.table(part, cost)
      .save(consumer, this.modResource(castingFolder + name + "_composite"));

    // Cast Casting
    MaterialIngredient ingredient = MaterialIngredient.fromItem(part);
    this.castCreation(consumer, ingredient, cast, castFolder, Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(part.asItem())).getPath());
  }

  /**
   * Adds a recipe to craft a material item
   *
   * @param consumer   Recipe consumer
   * @param part       Part to be crafted
   * @param cast       Part cast
   * @param cost       Part cost
   * @param partFolder Folder for recipes
   */
  default void partRecipes(Consumer<FinishedRecipe> consumer, Supplier<? extends IMaterialItem> part, CastItemObject cast, int cost, String partFolder, String castFolder) {
    this.partRecipes(consumer, part.get(), cast, cost, partFolder, castFolder);
  }
}
