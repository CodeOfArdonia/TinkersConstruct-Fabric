package slimeknights.mantle.recipe.data;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.minecraft.advancements.critereon.InventoryChangeTrigger.TriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.mantle.registration.object.BuildingBlockObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.mantle.registration.object.WallBuildingBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject;

import java.util.Objects;
import java.util.function.Consumer;

public interface ICommonRecipeHelper extends IRecipeHelper {
  /* Metals */

  /**
   * Registers a recipe packing a small item into a large one
   *
   * @param consumer  Recipe consumer
   * @param large     Large item
   * @param small     Small item
   * @param largeName Large name
   * @param smallName Small name
   * @param folder    Recipe folder
   */
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike large, String smallName, ItemLike small, String folder) {
    // ingot to block
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, large)
      .define('#', small)
      .pattern("###")
      .pattern("###")
      .pattern("###")
      .unlockedBy("has_item", RecipeProvider.has(small))
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(large.asItem())).toString())
      .save(consumer, this.wrap(large.asItem(), folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, small, 9)
      .requires(large)
      .unlockedBy("has_item", RecipeProvider.has(large))
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(small.asItem())).toString())
      .save(consumer, this.wrap(small.asItem(), folder, String.format("_from_%s", largeName)));
  }

  /**
   * Registers a recipe packing a small item into a large one
   *
   * @param consumer  Recipe consumer
   * @param largeItem Large item
   * @param smallItem Small item
   * @param smallTag  Tag for small item
   * @param largeName Large name
   * @param smallName Small name
   * @param folder    Recipe folder
   */
  default void packingRecipe(Consumer<FinishedRecipe> consumer, String largeName, ItemLike largeItem, String smallName, ItemLike smallItem, TagKey<Item> smallTag, String folder) {
    // ingot to block
    // note our item is in the center, any mod allowed around the edges
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, largeItem)
      .define('#', smallTag)
      .define('*', smallItem)
      .pattern("###")
      .pattern("#*#")
      .pattern("###")
      .unlockedBy("has_item", RecipeProvider.has(smallItem))
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(largeItem.asItem())).toString())
      .save(consumer, this.wrap(largeItem.asItem(), folder, String.format("_from_%ss", smallName)));
    // block to ingot
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, smallItem, 9)
      .requires(largeItem)
      .unlockedBy("has_item", RecipeProvider.has(largeItem))
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(smallItem.asItem())).toString())
      .save(consumer, this.wrap(smallItem.asItem(), folder, String.format("_from_%s", largeName)));
  }

  /**
   * Adds recipes to convert a block to ingot, ingot to block, and for nuggets
   *
   * @param consumer Recipe consumer
   * @param metal    Metal object
   * @param folder   Folder for recipes
   */
  default void metalCrafting(Consumer<FinishedRecipe> consumer, MetalItemObject metal, String folder) {
    ItemLike ingot = metal.getIngot();
    this.packingRecipe(consumer, "block", metal.get(), "ingot", ingot, metal.getIngotTag(), folder);
    this.packingRecipe(consumer, "ingot", ingot, "nugget", metal.getNugget(), metal.getNuggetTag(), folder);
  }


  /* Building blocks */

  /**
   * Registers generic saveing block recipes for slabs and stairs
   *
   * @param consumer Recipe consumer
   * @param saveing  Building object instance
   */
  default void slabStairsCrafting(Consumer<FinishedRecipe> consumer, BuildingBlockObject saveing, String folder, boolean addStonecutter) {
    Item item = saveing.asItem();
    TriggerInstance hasBlock = RecipeProvider.has(item);
    // slab
    ItemLike slab = saveing.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
      .define('B', item)
      .pattern("BBB")
      .unlockedBy("has_item", hasBlock)
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(slab.asItem())).toString())
      .save(consumer, this.wrap(item, folder, "_slab"));
    // stairs
    ItemLike stairs = saveing.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
      .define('B', item)
      .pattern("B  ")
      .pattern("BB ")
      .pattern("BBB")
      .unlockedBy("has_item", hasBlock)
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(stairs.asItem())).toString())
      .save(consumer, this.wrap(item, folder, "_stairs"));

    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, slab, 2)
        .unlockedBy("has_item", hasBlock)
        .save(consumer, this.wrap(item, folder, "_slab_stonecutter"));
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, stairs)
        .unlockedBy("has_item", hasBlock)
        .save(consumer, this.wrap(item, folder, "_stairs_stonecutter"));
    }
  }

  /**
   * Registers generic saveing block recipes for slabs, stairs, and walls
   *
   * @param consumer Recipe consumer
   * @param saveing  Building object instance
   */
  default void stairSlabWallCrafting(Consumer<FinishedRecipe> consumer, WallBuildingBlockObject saveing, String folder, boolean addStonecutter) {
    this.slabStairsCrafting(consumer, saveing, folder, addStonecutter);
    // wall
    Item item = saveing.asItem();
    TriggerInstance hasBlock = RecipeProvider.has(item);
    ItemLike wall = saveing.getWall();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wall, 6)
      .define('B', item)
      .pattern("BBB")
      .pattern("BBB")
      .unlockedBy("has_item", hasBlock)
      .group(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(wall.asItem())).toString())
      .save(consumer, this.wrap(item, folder, "_wall"));
    // only add stonecutter if relevant
    if (addStonecutter) {
      Ingredient ingredient = Ingredient.of(item);
      SingleItemRecipeBuilder.stonecutting(ingredient, RecipeCategory.BUILDING_BLOCKS, wall)
        .unlockedBy("has_item", hasBlock)
        .save(consumer, this.wrap(item, folder, "_wall_stonecutter"));
    }
  }

  /**
   * Registers recipes relevant to wood
   *
   * @param consumer Recipe consumer
   * @param wood     Wood types
   * @param folder   Wood folder
   */
  default void woodCrafting(Consumer<FinishedRecipe> consumer, WoodBlockObject wood, String folder) {
    TriggerInstance hasPlanks = RecipeProvider.has(wood);

    // planks
    ShapelessRecipeBuilder.shapeless(RecipeCategory.BUILDING_BLOCKS, wood, 4).requires(wood.getLogItemTag())
      .group("planks")
      .unlockedBy("has_log", RecipeProvider.inventoryTrigger(ItemPredicate.Builder.item().of(wood.getLogItemTag()).build()))
      .save(consumer, this.modResource(folder + "planks"));
    // slab
    ItemLike slab = wood.getSlab();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, slab, 6)
      .define('#', wood)
      .pattern("###")
      .unlockedBy("has_planks", hasPlanks)
      .group("wooden_slab")
      .save(consumer, this.modResource(folder + "slab"));
    // stairs
    ItemLike stairs = wood.getStairs();
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, stairs, 4)
      .define('#', wood)
      .pattern("#  ")
      .pattern("## ")
      .pattern("###")
      .unlockedBy("has_planks", hasPlanks)
      .group("wooden_stairs")
      .save(consumer, this.modResource(folder + "stairs"));

    // log to stripped
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getWood(), 3)
      .define('#', wood.getLog())
      .pattern("##").pattern("##")
      .group("bark")
      .unlockedBy("has_log", RecipeProvider.has(wood.getLog()))
      .save(consumer, this.modResource(folder + "log_to_wood"));
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getStrippedWood(), 3)
      .define('#', wood.getStrippedLog())
      .pattern("##").pattern("##")
      .group("bark")
      .unlockedBy("has_log", RecipeProvider.has(wood.getStrippedLog()))
      .save(consumer, this.modResource(folder + "stripped_log_to_wood"));
    // doors
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getFence(), 3)
      .define('#', Tags.Items.RODS_WOODEN).define('W', wood)
      .pattern("W#W").pattern("W#W")
      .group("wooden_fence")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "fence"));
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getFenceGate())
      .define('#', Items.STICK).define('W', wood)
      .pattern("#W#").pattern("#W#")
      .group("wooden_fence_gate")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "fence_gate"));
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getDoor(), 3)
      .define('#', wood)
      .pattern("##").pattern("##").pattern("##")
      .group("wooden_door")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "door"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getTrapdoor(), 2)
      .define('#', wood)
      .pattern("###").pattern("###")
      .group("wooden_trapdoor")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "trapdoor"));
    // buttons
    ShapelessRecipeBuilder.shapeless(RecipeCategory.REDSTONE, wood.getButton())
      .requires(wood)
      .group("wooden_button")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "button"));
    ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, wood.getPressurePlate())
      .define('#', wood)
      .pattern("##")
      .group("wooden_pressure_plate")
      .unlockedBy("has_planks", hasPlanks)
      .save(consumer, this.modResource(folder + "pressure_plate"));
    // signs
    ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, wood.getSign(), 3)
      .group("sign")
      .define('#', wood).define('X', Tags.Items.RODS_WOODEN)
      .pattern("###").pattern("###").pattern(" X ")
      .unlockedBy("has_planks", RecipeProvider.has(wood))
      .save(consumer, this.modResource(folder + "sign"));

  }
}