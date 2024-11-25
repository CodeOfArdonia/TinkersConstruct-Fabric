package slimeknights.tconstruct.tables.data;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipeBuilder;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.library.data.recipe.SpecialRecipeBuilder;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.recipe.PartBuilderToolRecycle;
import slimeknights.tconstruct.tables.recipe.TinkerStationDamagingRecipe;
import slimeknights.tconstruct.tools.TinkerTools;

import java.util.function.Consumer;

public class TableRecipeProvider extends BaseRecipeProvider {

  public TableRecipeProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Table Recipes";
  }

  @Override
  public void buildRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tables/";
    // pattern
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.pattern, 3)
      .define('s', Tags.Items.RODS_WOODEN)
      .define('p', ItemTags.PLANKS)
      .pattern("ps")
      .pattern("sp")
      .unlockedBy("has_item", has(Tags.Items.RODS_WOODEN))
      .save(consumer, this.prefix(TinkerTables.pattern.getRegistryName(), folder));

    // book from patterns and slime
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BOOK)
      .requires(Items.PAPER)
      .requires(Items.PAPER)
      .requires(Items.PAPER)
      .requires(Tags.Items.SLIMEBALLS)
      .requires(TinkerTables.pattern)
      .requires(TinkerTables.pattern)
      .unlockedBy("has_item", has(TinkerTables.pattern))
      .save(consumer, this.modResource(folder + "book_substitute"));

    // crafting station -> crafting table upgrade
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.craftingStation)
      .define('p', TinkerTables.pattern)
      .define('w', DefaultCustomIngredients.difference(DefaultCustomIngredients.any(Ingredient.of(TinkerTags.Items.WORKBENCHES), Ingredient.of(TinkerTags.Items.TABLES)),
        Ingredient.of(TinkerTables.craftingStation.get())))
      .pattern("p")
      .pattern("w")
      .unlockedBy("has_item", has(TinkerTables.pattern))
      .save(consumer, this.prefix(TinkerTables.craftingStation.getRegistryName(), folder));
    // station with log texture
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.craftingStation)
          .define('p', TinkerTables.pattern)
          .define('w', ItemTags.LOGS)
          .pattern("p")
          .pattern("w")
          .unlockedBy("has_item", has(TinkerTables.pattern)))
      .setSource(ItemTags.LOGS)
      .build(consumer, this.wrap(TinkerTables.craftingStation.getRegistryName(), folder, "_from_logs"));

    // part builder
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.partBuilder)
          .define('p', TinkerTables.pattern)
          .define('w', TinkerTags.Items.PLANKLIKE)
          .pattern("pp")
          .pattern("ww")
          .unlockedBy("has_item", has(TinkerTables.pattern)))
      .setSource(TinkerTags.Items.PLANKLIKE)
      .setMatchAll()
      .build(consumer, this.prefix(TinkerTables.partBuilder.getRegistryName(), folder));

    // tinker station
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.tinkerStation)
          .define('p', TinkerTables.pattern)
          .define('w', TinkerTags.Items.PLANKLIKE)
          .pattern("ppp")
          .pattern("w w")
          .pattern("w w")
          .unlockedBy("has_item", has(TinkerTables.pattern)))
      .setSource(TinkerTags.Items.PLANKLIKE)
      .setMatchAll()
      .build(consumer, this.prefix(TinkerTables.tinkerStation.getRegistryName(), folder));

    // part chest
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.partChest)
      .define('p', TinkerTables.pattern)
      .define('w', ItemTags.PLANKS)
      .define('s', Tags.Items.RODS_WOODEN)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" p ")
      .pattern("sCs")
      .pattern("sws")
      .unlockedBy("has_item", has(TinkerTables.pattern))
      .save(consumer, this.prefix(TinkerTables.partChest.getRegistryName(), folder));
    // modifier chest
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.tinkersChest)
      .define('p', TinkerTables.pattern)
      .define('w', ItemTags.PLANKS)
      .define('l', Tags.Items.GEMS_LAPIS)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" p ")
      .pattern("lCl")
      .pattern("lwl")
      .unlockedBy("has_item", has(TinkerTables.pattern))
      .save(consumer, this.prefix(TinkerTables.tinkersChest.getRegistryName(), folder));
    // cast chest
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.castChest)
      .define('c', TinkerTags.Items.GOLD_CASTS)
      .define('b', TinkerSmeltery.searedBrick)
      .define('B', TinkerSmeltery.searedBricks)
      .define('C', Tags.Items.CHESTS_WOODEN)
      .pattern(" c ")
      .pattern("bCb")
      .pattern("bBb")
      .unlockedBy("has_item", has(TinkerTags.Items.GOLD_CASTS))
      .save(consumer, this.prefix(TinkerTables.castChest.getRegistryName(), folder));

    // modifier worktable
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.modifierWorktable)
          .define('r', TinkerTags.Items.WORKSTATION_ROCK)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .pattern("sss")
          .pattern("r r")
          .pattern("r r")
          .unlockedBy("has_item", has(TinkerTags.Items.SEARED_BLOCKS)))
      .setSource(TinkerTags.Items.WORKSTATION_ROCK)
      .setMatchAll()
      .build(consumer, this.prefix(TinkerTables.modifierWorktable.getRegistryName(), folder));

    // tinker anvil
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.tinkersAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .pattern("mmm")
          .pattern(" s ")
          .pattern("sss")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource(TinkerTags.Items.ANVIL_METAL)
      .setMatchAll()
      .build(consumer, this.prefix(TinkerTables.tinkersAnvil.getRegistryName(), folder));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.tinkersAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SEARED_BLOCKS)
          .define('t', TinkerTables.tinkerStation)
          .pattern("mmm")
          .pattern("sts")
          .pattern("s s")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource(TinkerTags.Items.ANVIL_METAL)
      .setMatchAll()
      .build(consumer, this.modResource(folder + "tinkers_forge"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.scorchedAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
          .pattern("mmm")
          .pattern(" s ")
          .pattern("sss")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource(TinkerTags.Items.ANVIL_METAL)
      .setMatchAll()
      .build(consumer, this.prefix(TinkerTables.scorchedAnvil.getRegistryName(), folder));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerTables.scorchedAnvil)
          .define('m', TinkerTags.Items.ANVIL_METAL)
          .define('s', TinkerTags.Items.SCORCHED_BLOCKS)
          .define('t', TinkerTables.tinkerStation)
          .pattern("mmm")
          .pattern("sts")
          .pattern("s s")
          .unlockedBy("has_item", has(TinkerTags.Items.ANVIL_METAL)))
      .setSource(TinkerTags.Items.ANVIL_METAL)
      .setMatchAll()
      .build(consumer, this.modResource(folder + "scorched_forge"));

    // recycling singleton
    consumer.accept(new PartBuilderToolRecycle.Finished(
      this.modResource(folder + "tool_recycling"),
      SizedIngredient.of(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.MULTIPART_TOOL), Ingredient.of(TinkerTags.Items.UNSALVAGABLE))),
      Ingredient.of(TinkerTags.Items.PATTERNS)
    ));
    consumer.accept(new PartBuilderToolRecycle.Finished(
      this.modResource(folder + "dagger_recycling"),
      SizedIngredient.fromItems(2, TinkerTools.dagger),
      Ingredient.of(TinkerTags.Items.PATTERNS)
    ));

    // tool repair recipe
    SpecialRecipeBuilder.special(TinkerTables.tinkerStationRepairSerializer.get())
      .save(consumer, this.modPrefix(folder + "tinker_station_repair"));
    SpecialRecipeBuilder.special(TinkerTables.tinkerStationPartSwappingSerializer.get())
      .save(consumer, this.modPrefix(folder + "tinker_station_part_swapping"));
    SpecialRecipeBuilder.special(TinkerTables.craftingTableRepairSerializer.get())
      .save(consumer, this.modPrefix(folder + "crafting_table_repair"));
    // tool damaging
    String damageFolder = folder + "tinker_station_damaging/";
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.MUNDANE), true), 1)
      .save(consumer, this.modResource(damageFolder + "base_one"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.THICK), true), 5)
      .save(consumer, this.modResource(damageFolder + "base_two"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.HARMING), true), 25)
      .save(consumer, this.modResource(damageFolder + "potion_one"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_HARMING), true), 75)
      .save(consumer, this.modResource(damageFolder + "potion_two"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.HARMING), true), 150)
      .save(consumer, this.modResource(damageFolder + "splash_one"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.STRONG_HARMING), true), 400)
      .save(consumer, this.modResource(damageFolder + "splash_two"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.HARMING), true), 1000)
      .save(consumer, this.modResource(damageFolder + "lingering_one"));
    TinkerStationDamagingRecipe.Builder.damage(DefaultCustomIngredients.nbt(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.STRONG_HARMING), true), 2500)
      .save(consumer, this.modResource(damageFolder + "lingering_two"));
  }
}
