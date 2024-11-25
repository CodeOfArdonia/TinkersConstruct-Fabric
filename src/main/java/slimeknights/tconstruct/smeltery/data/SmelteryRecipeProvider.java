package slimeknights.tconstruct.smeltery.data;

import io.github.fabricators_of_create.porting_lib.data.ConditionalRecipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.fabricators_of_create.porting_lib.util.TrueCondition;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.data.recipes.SingleItemRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.recipe.crafting.ShapedRetexturedRecipeBuilder;
import slimeknights.mantle.recipe.data.ConsumerWrapperBuilder;
import slimeknights.mantle.recipe.data.ICommonRecipeHelper;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.data.ItemNameOutput;
import slimeknights.mantle.recipe.data.NBTNameIngredient;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.registration.GeodeItemObject;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.data.recipe.ISmelteryRecipeHelper;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.PotionCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.casting.container.ContainerFillingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.entitymelting.EntityMeltingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.fuel.MeltingFuelBuilder;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.molding.MoldingRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.world.TinkerHeadType;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("removal")
public class SmelteryRecipeProvider extends BaseRecipeProvider implements ISmelteryRecipeHelper, ICommonRecipeHelper {

  public SmelteryRecipeProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Smeltery Recipes";
  }

  @Override
  public void buildRecipes(Consumer<FinishedRecipe> consumer) {
    this.addCraftingRecipes(consumer);
    this.addSmelteryRecipes(consumer);
    this.addFoundryRecipes(consumer);
    this.addMeltingRecipes(consumer);
    this.addCastingRecipes(consumer);
    this.addAlloyRecipes(consumer);
    this.addEntityMeltingRecipes(consumer);

    this.addCompatRecipes(consumer);
  }

  private void addCraftingRecipes(Consumer<FinishedRecipe> consumer) {
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.copperCan, 3)
      .define('c', Tags.Items.INGOTS_COPPER)
      .pattern("c c")
      .pattern(" c ")
      .unlockedBy("has_item", has(Tags.Items.INGOTS_COPPER))
      .save(consumer, this.prefix(TinkerSmeltery.copperCan.getRegistryName(), "smeltery/"));

    // sand casts
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerSmeltery.blankSandCast, 4)
      .requires(Tags.Items.SAND_COLORLESS)
      .unlockedBy("has_casting", has(TinkerSmeltery.searedTable))
      .save(consumer, this.modResource("smeltery/sand_cast"));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerSmeltery.blankRedSandCast, 4)
      .requires(Tags.Items.SAND_RED)
      .unlockedBy("has_casting", has(TinkerSmeltery.searedTable))
      .save(consumer, this.modResource("smeltery/red_sand_cast"));

    // pick up sand casts from the table
    MoldingRecipeBuilder.moldingTable(TinkerSmeltery.blankSandCast)
      .setMaterial(TinkerTags.Items.SAND_CASTS)
      .save(consumer, this.modResource("smeltery/sand_cast_pickup"));
    MoldingRecipeBuilder.moldingTable(TinkerSmeltery.blankRedSandCast)
      .setMaterial(TinkerTags.Items.RED_SAND_CASTS)
      .save(consumer, this.modResource("smeltery/red_sand_cast_pickup"));
  }

  private void addSmelteryRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "smeltery/seared/";
    // grout crafting
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerSmeltery.grout, 2)
      .requires(Items.CLAY_BALL)
      .requires(ItemTags.SAND)
      .requires(Blocks.GRAVEL)
      .unlockedBy("has_item", has(Items.CLAY_BALL))
      .save(consumer, this.prefix(TinkerSmeltery.grout.getRegistryName(), folder));
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerSmeltery.grout, 8)
      .requires(Blocks.CLAY)
      .requires(ItemTags.SAND).requires(ItemTags.SAND).requires(ItemTags.SAND).requires(ItemTags.SAND)
      .requires(Blocks.GRAVEL).requires(Blocks.GRAVEL).requires(Blocks.GRAVEL).requires(Blocks.GRAVEL)
      .unlockedBy("has_item", has(Blocks.CLAY))
      .save(consumer, this.wrap(TinkerSmeltery.grout.getRegistryName(), folder, "_multiple"));

    // seared bricks from grout
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.grout), RecipeCategory.MISC, TinkerSmeltery.searedBrick, 0.3f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.grout))
      .save(consumer, this.prefix(TinkerSmeltery.searedBrick.getRegistryName(), folder));
    Consumer<Consumer<FinishedRecipe>> fastGrout = c ->
      SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerSmeltery.grout), RecipeCategory.MISC, TinkerSmeltery.searedBrick, 0.3f, 100)
        .unlockedBy("has_item", has(TinkerSmeltery.grout)).save(c);

    ConditionalRecipe.builder()
      .addCondition(DefaultResourceConditions.allModsLoaded("ceramics"))
      .addRecipe(c -> fastGrout.accept(ConsumerWrapperBuilder.wrap(new ResourceLocation("ceramics", "kiln")).build(c)))
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(fastGrout)
      .generateAdvancement()
      .build(consumer, this.wrap(TinkerSmeltery.searedBrick, folder, "_kiln"));


    // block from bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedBricks)
      .define('b', TinkerSmeltery.searedBrick)
      .pattern("bb")
      .pattern("bb")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.wrap(TinkerSmeltery.searedBricks.getRegistryName(), folder, "_from_brick"));
    // ladder from bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedLadder, 4)
      .define('b', TinkerSmeltery.searedBrick)
      .define('B', TinkerTags.Items.SEARED_BRICKS)
      .pattern("b b")
      .pattern("b b")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.prefix(TinkerSmeltery.searedLadder.getRegistryName(), folder));

    // cobble -> stone
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.searedCobble.get()), RecipeCategory.MISC, TinkerSmeltery.searedStone, 0.1f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.searedCobble.get()))
      .save(consumer, this.wrap(TinkerSmeltery.searedStone.getRegistryName(), folder, "_smelting"));
    // stone -> paver
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.searedStone.get()), RecipeCategory.MISC, TinkerSmeltery.searedPaver, 0.1f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.searedStone.get()))
      .save(consumer, this.wrap(TinkerSmeltery.searedPaver.getRegistryName(), folder, "_smelting"));
    // stone -> bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedBricks, 4)
      .define('b', TinkerSmeltery.searedStone)
      .pattern("bb")
      .pattern("bb")
      .unlockedBy("has_item", has(TinkerSmeltery.searedStone))
      .save(consumer, this.wrap(TinkerSmeltery.searedBricks.getRegistryName(), folder, "_crafting"));
    // bricks -> cracked
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.searedBricks), RecipeCategory.MISC, TinkerSmeltery.searedCrackedBricks, 0.1f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.searedBricks))
      .save(consumer, this.wrap(TinkerSmeltery.searedCrackedBricks.getRegistryName(), folder, "_smelting"));
    // brick slabs -> fancy
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedFancyBricks)
      .define('s', TinkerSmeltery.searedBricks.getSlab())
      .pattern("s")
      .pattern("s")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBricks.getSlab()))
      .save(consumer, this.wrap(TinkerSmeltery.searedFancyBricks.getRegistryName(), folder, "_crafting"));
    // bricks or stone as input
    this.searedStonecutter(consumer, TinkerSmeltery.searedBricks, folder);
    this.searedStonecutter(consumer, TinkerSmeltery.searedFancyBricks, folder);
    this.searedStonecutter(consumer, TinkerSmeltery.searedTriangleBricks, folder);

    // seared glass
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedGlass)
      .define('b', TinkerSmeltery.searedBrick)
      .define('G', Tags.Items.GLASS_COLORLESS)
      .pattern(" b ")
      .pattern("bGb")
      .pattern(" b ")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.prefix(TinkerSmeltery.searedGlass.getRegistryName(), folder));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedGlassPane, 16)
      .define('#', TinkerSmeltery.searedGlass)
      .pattern("###")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.searedGlass))
      .save(consumer, this.prefix(TinkerSmeltery.searedGlassPane.getRegistryName(), folder));

    // stairs and slabs
    this.slabStairsCrafting(consumer, TinkerSmeltery.searedStone, folder, true);
    this.stairSlabWallCrafting(consumer, TinkerSmeltery.searedCobble, folder, true);
    this.slabStairsCrafting(consumer, TinkerSmeltery.searedPaver, folder, true);
    this.stairSlabWallCrafting(consumer, TinkerSmeltery.searedBricks, folder, true);

    // tanks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedTank.get(TankType.FUEL_TANK))
      .define('#', TinkerSmeltery.searedBrick)
      .define('B', Tags.Items.GLASS)
      .pattern("###")
      .pattern("#B#")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "fuel_tank"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedTank.get(TankType.FUEL_GAUGE))
      .define('#', TinkerSmeltery.searedBrick)
      .define('B', Tags.Items.GLASS)
      .pattern("#B#")
      .pattern("BBB")
      .pattern("#B#")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "fuel_gauge"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedTank.get(TankType.INGOT_TANK))
      .define('#', TinkerSmeltery.searedBrick)
      .define('B', Tags.Items.GLASS)
      .pattern("#B#")
      .pattern("#B#")
      .pattern("#B#")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "ingot_tank"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedTank.get(TankType.INGOT_GAUGE))
      .define('#', TinkerSmeltery.searedBrick)
      .define('B', Tags.Items.GLASS)
      .pattern("B#B")
      .pattern("#B#")
      .pattern("B#B")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "ingot_gauge"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedLantern.get(), 3)
      .define('C', Tags.Items.INGOTS_IRON)
      .define('B', TinkerSmeltery.searedBrick)
      .define('P', TinkerSmeltery.searedGlassPane)
      .pattern(" C ")
      .pattern("PPP")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "lantern"));

    // fluid transfer
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedFaucet.get(), 3)
      .define('#', TinkerSmeltery.searedBrick)
      .pattern("# #")
      .pattern(" # ")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "faucet"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedChannel.get(), 5)
      .define('#', TinkerSmeltery.searedBrick)
      .pattern("# #")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "channel"));

    // casting
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedBasin.get())
      .define('#', TinkerSmeltery.searedBrick)
      .pattern("# #")
      .pattern("# #")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "basin"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedTable.get())
      .define('#', TinkerSmeltery.searedBrick)
      .pattern("###")
      .pattern("# #")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "table"));

    // peripherals
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedDrain)
      .define('#', TinkerSmeltery.searedBrick)
      .define('C', Tags.Items.INGOTS_COPPER)
      .pattern("# #")
      .pattern("C C")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "drain"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedDrain)
          .define('#', TinkerTags.Items.SMELTERY_BRICKS)
          .define('C', Tags.Items.INGOTS_COPPER)
          .pattern("C#C")
          .unlockedBy("has_item", has(TinkerTags.Items.SMELTERY_BRICKS)))
      .setSource(TinkerTags.Items.SMELTERY_BRICKS)
      .build(consumer, this.modResource(folder + "drain_retextured"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedChute)
      .define('#', TinkerSmeltery.searedBrick)
      .define('C', Tags.Items.INGOTS_COPPER)
      .pattern("#C#")
      .pattern("   ")
      .pattern("#C#")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "chute"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedChute)
          .define('#', TinkerTags.Items.SMELTERY_BRICKS)
          .define('C', Tags.Items.INGOTS_COPPER)
          .pattern("C")
          .pattern("#")
          .pattern("C")
          .unlockedBy("has_item", has(TinkerTags.Items.SMELTERY_BRICKS)))
      .setSource(TinkerTags.Items.SMELTERY_BRICKS)
      .build(consumer, this.modResource(folder + "chute_retextured"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedDuct)
      .define('#', TinkerSmeltery.searedBrick)
      .define('C', TinkerMaterials.cobalt.getIngotTag())
      .pattern("# #")
      .pattern("C C")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerMaterials.cobalt.getIngotTag()))
      .save(consumer, this.modResource(folder + "duct"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedDuct)
          .define('#', TinkerTags.Items.SMELTERY_BRICKS)
          .define('C', TinkerMaterials.cobalt.getIngotTag())
          .pattern("C#C")
          .unlockedBy("has_item", has(TinkerTags.Items.SMELTERY_BRICKS)))
      .setSource(TinkerTags.Items.SMELTERY_BRICKS)
      .build(consumer, this.modResource(folder + "duct_retextured"));

    // controllers
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedMelter)
      .define('G', Ingredient.of(TinkerSmeltery.searedTank.get(TankType.FUEL_GAUGE), TinkerSmeltery.searedTank.get(TankType.INGOT_GAUGE)))
      .define('B', TinkerSmeltery.searedBrick)
      .pattern("BGB")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "melter"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.searedHeater)
      .define('B', TinkerSmeltery.searedBrick)
      .pattern("BBB")
      .pattern("B B")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.searedBrick))
      .save(consumer, this.modResource(folder + "heater"));

    // casting
    String castingFolder = "smeltery/casting/seared/";

    ItemCastingRecipeBuilder.basinRecipe(TinkerSmeltery.searedStone)
      .setFluidAndTime(TinkerFluids.searedStone, false, FluidValues.BRICK_BLOCK)
      .save(consumer, this.modResource(castingFolder + "stone/block_from_seared"));
    this.ingotCasting(consumer, TinkerFluids.searedStone, FluidValues.BRICK, TinkerSmeltery.searedBrick, castingFolder + "brick");
    ItemCastingRecipeBuilder.basinRecipe(TinkerSmeltery.searedGlass)
      .setFluidAndTime(TinkerFluids.searedStone, false, FluidValues.BRICK_BLOCK)
      .setCast(Tags.Items.GLASS_COLORLESS, true)
      .save(consumer, this.modResource(castingFolder + "glass"));
    // discount for casting panes
    ItemCastingRecipeBuilder.tableRecipe(TinkerSmeltery.searedGlassPane)
      .setFluidAndTime(TinkerFluids.searedStone, false, FluidValues.BRICK)
      .setCast(Tags.Items.GLASS_PANES_COLORLESS, true)
      .save(consumer, this.modResource(castingFolder + "glass_pane"));

    // smeltery controller
    ItemCastingRecipeBuilder.retexturedBasinRecipe(ItemOutput.fromItem(TinkerSmeltery.smelteryController))
      .setCast(TinkerTags.Items.SMELTERY_BRICKS, true)
      .setFluidAndTime(TinkerFluids.moltenCopper, true, FluidValues.INGOT * 4)
      .save(consumer, this.prefix(TinkerSmeltery.smelteryController.getRegistryName(), castingFolder));

    // craft seared stone from clay and stone
    // button is the closest we have to a single stone brick, just go with it, better than not having the recipe
    ItemCastingRecipeBuilder.tableRecipe(TinkerSmeltery.searedBrick)
      .setFluidAndTime(TinkerFluids.moltenClay, false, FluidValues.BRICK / 2)
      .setCast(Items.STONE_BUTTON, true)
      .save(consumer, this.modResource(castingFolder + "brick_composite"));
    // cobble
    this.searedCasting(consumer, TinkerSmeltery.searedCobble, DefaultCustomIngredients.any(Ingredient.of(Tags.Items.COBBLESTONE), Ingredient.of(Blocks.GRAVEL)), castingFolder + "cobble/block");
    this.searedSlabCasting(consumer, TinkerSmeltery.searedCobble.getSlab(), Ingredient.of(Blocks.COBBLESTONE_SLAB), castingFolder + "cobble/slab");
    this.searedCasting(consumer, TinkerSmeltery.searedCobble.getStairs(), Ingredient.of(Blocks.COBBLESTONE_STAIRS), castingFolder + "cobble/stairs");
    this.searedCasting(consumer, TinkerSmeltery.searedCobble.getWall(), Ingredient.of(Blocks.COBBLESTONE_WALL), castingFolder + "cobble/wall");
    // stone
    this.searedCasting(consumer, TinkerSmeltery.searedStone, Ingredient.of(Tags.Items.STONE), castingFolder + "stone/block_from_clay");
    this.searedSlabCasting(consumer, TinkerSmeltery.searedStone.getSlab(), Ingredient.of(Blocks.STONE_SLAB), castingFolder + "stone/slab");
    this.searedCasting(consumer, TinkerSmeltery.searedStone.getStairs(), Ingredient.of(Blocks.STONE_STAIRS), castingFolder + "stone/stairs");
    // stone bricks
    this.searedCasting(consumer, TinkerSmeltery.searedBricks, Ingredient.of(Blocks.STONE_BRICKS), castingFolder + "bricks/block");
    this.searedSlabCasting(consumer, TinkerSmeltery.searedBricks.getSlab(), Ingredient.of(Blocks.STONE_BRICK_SLAB), castingFolder + "bricks/slab");
    this.searedCasting(consumer, TinkerSmeltery.searedBricks.getStairs(), Ingredient.of(Blocks.STONE_BRICK_STAIRS), castingFolder + "bricks/stairs");
    this.searedCasting(consumer, TinkerSmeltery.searedBricks.getWall(), Ingredient.of(Blocks.STONE_BRICK_WALL), castingFolder + "bricks/wall");
    // other seared
    this.searedCasting(consumer, TinkerSmeltery.searedCrackedBricks, Ingredient.of(Blocks.CRACKED_STONE_BRICKS), castingFolder + "cracked");
    this.searedCasting(consumer, TinkerSmeltery.searedFancyBricks, Ingredient.of(Blocks.CHISELED_STONE_BRICKS), castingFolder + "chiseled");
    this.searedCasting(consumer, TinkerSmeltery.searedPaver, Ingredient.of(Blocks.SMOOTH_STONE), castingFolder + "paver");

    // seared blocks
    String meltingFolder = "smeltery/melting/seared/";

    // double efficiency when using smeltery for grout
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.grout), TinkerFluids.searedStone.get(), FluidValues.BRICK * 2, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "grout"));
    // seared stone
    // stairs are here since the cheapest stair recipe is stone cutter, 1 to 1
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(Ingredient.of(TinkerTags.Items.SEARED_BLOCKS),
          Ingredient.of(TinkerSmeltery.searedLadder, TinkerSmeltery.searedCobble.getWall(), TinkerSmeltery.searedBricks.getWall(),
            TinkerSmeltery.searedCobble.getStairs(), TinkerSmeltery.searedStone.getStairs(), TinkerSmeltery.searedBricks.getStairs(), TinkerSmeltery.searedPaver.getStairs())),
        TinkerFluids.searedStone.get(), FluidValues.BRICK_BLOCK, 2.0f)
      .save(consumer, this.modResource(meltingFolder + "block"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedCobble.getSlab(), TinkerSmeltery.searedStone.getSlab(), TinkerSmeltery.searedBricks.getSlab(), TinkerSmeltery.searedPaver.getSlab()),
        TinkerFluids.searedStone.get(), FluidValues.BRICK_BLOCK / 2, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "slab"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedBrick), TinkerFluids.searedStone.get(), FluidValues.BRICK, 1.0f)
      .save(consumer, this.modResource(meltingFolder + "brick"));

    // melt down smeltery components
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedFaucet, TinkerSmeltery.searedChannel), TinkerFluids.searedStone.get(), FluidValues.BRICK, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "faucet"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedBasin, TinkerSmeltery.searedTable), TinkerFluids.searedStone.get(), FluidValues.BRICK * 7, 2.5f)
      .save(consumer, this.modResource(meltingFolder + "casting"));
    // tanks
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.searedTank.get(TankType.FUEL_TANK)), true), TinkerFluids.searedStone.get(), FluidValues.BRICK * 8, 3f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK))
      .save(consumer, this.modResource(meltingFolder + "fuel_tank"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.searedTank.get(TankType.INGOT_TANK)), true), TinkerFluids.searedStone.get(), FluidValues.BRICK * 6, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK * 3))
      .save(consumer, this.modResource(meltingFolder + "ingot_tank"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.searedTank.get(TankType.FUEL_GAUGE)), true),
          DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.searedTank.get(TankType.INGOT_GAUGE)), true)),
        TinkerFluids.searedStone.get(), FluidValues.BRICK * 4, 2f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK * 5))
      .save(consumer, this.modResource(meltingFolder + "gauge"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.searedLantern), true), TinkerFluids.searedStone.get(), FluidValues.BRICK * 2, 1.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE))
      .addByproduct(new FluidStack(TinkerFluids.moltenIron.get(), FluidValues.INGOT / 3))
      .save(consumer, this.modResource(meltingFolder + "lantern"));
    // glass
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedGlass), TinkerFluids.searedStone.get(), FluidValues.BRICK * 4, 2f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK))
      .save(consumer, this.modResource(meltingFolder + "glass"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedGlassPane), TinkerFluids.searedStone.get(), FluidValues.BRICK, 1.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(meltingFolder + "pane"));
    // controllers
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedMelter), TinkerFluids.searedStone.get(), FluidValues.BRICK * 9, 3.5f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE * 5))
      .save(consumer, this.modResource(meltingFolder + "melter"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedHeater), TinkerFluids.searedStone.get(), FluidValues.BRICK * 8, 3f)
      .save(consumer, this.modResource(meltingFolder + "heater"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.smelteryController), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 4, 3.5f)
      .addByproduct(new FluidStack(TinkerFluids.searedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/metal/copper/smeltery_controller"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedDrain, TinkerSmeltery.searedChute), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 2, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.searedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/metal/copper/smeltery_io"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.searedDuct), TinkerFluids.moltenCobalt.get(), FluidValues.INGOT * 2, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.searedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/metal/cobalt/seared_duct"));
    // reinforcement - no seared stone as it can also be casted from scorched
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.searedReinforcement), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE)
      .save(consumer, this.modResource(meltingFolder + "reinforcement"));
  }

  private void addFoundryRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "smeltery/scorched/";
    // grout crafting
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TinkerSmeltery.netherGrout, 2)
      .requires(Items.MAGMA_CREAM)
      .requires(Ingredient.of(Blocks.SOUL_SAND, Blocks.SOUL_SOIL))
      .requires(Blocks.GRAVEL)
      .unlockedBy("has_item", has(Items.MAGMA_CREAM))
      .save(consumer, this.prefix(TinkerSmeltery.netherGrout.getRegistryName(), folder));

    // scorched bricks from grout
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.netherGrout), RecipeCategory.MISC, TinkerSmeltery.scorchedBrick, 0.3f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.netherGrout))
      .save(consumer, this.prefix(TinkerSmeltery.scorchedBrick.getRegistryName(), folder));
    Consumer<Consumer<FinishedRecipe>> fastGrout = c ->
      SimpleCookingRecipeBuilder.blasting(Ingredient.of(TinkerSmeltery.netherGrout), RecipeCategory.MISC, TinkerSmeltery.scorchedBrick, 0.3f, 100)
        .unlockedBy("has_item", has(TinkerSmeltery.netherGrout)).save(c);

    ConditionalRecipe.builder()
      .addCondition(DefaultResourceConditions.allModsLoaded("ceramics"))
      .addRecipe(c -> fastGrout.accept(ConsumerWrapperBuilder.wrap(new ResourceLocation("ceramics", "kiln")).build(c)))
      .addCondition(TrueCondition.INSTANCE)
      .addRecipe(fastGrout)
      .generateAdvancement()
      .build(consumer, this.wrap(TinkerSmeltery.scorchedBrick, folder, "_kiln"));

    // block from bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedBricks)
      .define('b', TinkerSmeltery.scorchedBrick)
      .pattern("bb")
      .pattern("bb")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.wrap(TinkerSmeltery.scorchedBricks.getRegistryName(), folder, "_from_brick"));
    // ladder from bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedLadder, 4)
      .define('b', TinkerSmeltery.scorchedBrick)
      .define('B', TinkerTags.Items.SCORCHED_BLOCKS)
      .pattern("b b")
      .pattern("b b")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.prefix(TinkerSmeltery.scorchedLadder.getRegistryName(), folder));

    // stone -> polished
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.polishedScorchedStone, 4)
      .define('b', TinkerSmeltery.scorchedStone)
      .pattern("bb")
      .pattern("bb")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedStone))
      .save(consumer, this.wrap(TinkerSmeltery.polishedScorchedStone.getRegistryName(), folder, "_crafting"));
    // polished -> bricks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedBricks, 4)
      .define('b', TinkerSmeltery.polishedScorchedStone)
      .pattern("bb")
      .pattern("bb")
      .unlockedBy("has_item", has(TinkerSmeltery.polishedScorchedStone))
      .save(consumer, this.wrap(TinkerSmeltery.scorchedBricks.getRegistryName(), folder, "_crafting"));
    // stone -> road
    SimpleCookingRecipeBuilder.smelting(Ingredient.of(TinkerSmeltery.scorchedStone), RecipeCategory.MISC, TinkerSmeltery.scorchedRoad, 0.1f, 200)
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedStone))
      .save(consumer, this.wrap(TinkerSmeltery.scorchedRoad.getRegistryName(), folder, "_smelting"));
    // brick slabs -> chiseled
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.chiseledScorchedBricks)
      .define('s', TinkerSmeltery.scorchedBricks.getSlab())
      .pattern("s")
      .pattern("s")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBricks.getSlab()))
      .save(consumer, this.wrap(TinkerSmeltery.chiseledScorchedBricks.getRegistryName(), folder, "_crafting"));
    // stonecutting
    this.scorchedStonecutter(consumer, TinkerSmeltery.polishedScorchedStone, folder);
    this.scorchedStonecutter(consumer, TinkerSmeltery.scorchedBricks, folder);
    this.scorchedStonecutter(consumer, TinkerSmeltery.chiseledScorchedBricks, folder);

    // scorched glass
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedGlass)
      .define('b', TinkerSmeltery.scorchedBrick)
      .define('G', Tags.Items.GEMS_QUARTZ)
      .pattern(" b ")
      .pattern("bGb")
      .pattern(" b ")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.prefix(TinkerSmeltery.scorchedGlass.getRegistryName(), folder));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedGlassPane, 16)
      .define('#', TinkerSmeltery.scorchedGlass)
      .pattern("###")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedGlass))
      .save(consumer, this.prefix(TinkerSmeltery.scorchedGlassPane.getRegistryName(), folder));

    // stairs, slabs, and fences
    this.slabStairsCrafting(consumer, TinkerSmeltery.scorchedBricks, folder, true);
    this.slabStairsCrafting(consumer, TinkerSmeltery.scorchedRoad, folder, true);
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedBricks.getFence(), 6)
      .define('B', TinkerSmeltery.scorchedBricks)
      .define('b', TinkerSmeltery.scorchedBrick)
      .pattern("BbB")
      .pattern("BbB")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBricks))
      .save(consumer, this.prefix(BuiltInRegistries.BLOCK.getKey(TinkerSmeltery.scorchedBricks.getFence()), folder));

    // tanks
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedTank.get(TankType.FUEL_TANK))
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('B', Tags.Items.GEMS_QUARTZ)
      .pattern("###")
      .pattern("#B#")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "fuel_tank"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedTank.get(TankType.FUEL_GAUGE))
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('B', Tags.Items.GEMS_QUARTZ)
      .pattern("#B#")
      .pattern("BBB")
      .pattern("#B#")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "fuel_gauge"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedTank.get(TankType.INGOT_TANK))
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('B', Tags.Items.GEMS_QUARTZ)
      .pattern("#B#")
      .pattern("#B#")
      .pattern("#B#")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "ingot_tank"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedTank.get(TankType.INGOT_GAUGE))
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('B', Tags.Items.GEMS_QUARTZ)
      .pattern("B#B")
      .pattern("#B#")
      .pattern("B#B")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "ingot_gauge"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedLantern.get(), 3)
      .define('C', Tags.Items.INGOTS_IRON)
      .define('B', TinkerSmeltery.scorchedBrick)
      .define('P', TinkerSmeltery.scorchedGlassPane)
      .pattern(" C ")
      .pattern("PPP")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "lantern"));

    // fluid transfer
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedFaucet.get(), 3)
      .define('#', TinkerSmeltery.scorchedBrick)
      .pattern("# #")
      .pattern(" # ")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "faucet"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedChannel.get(), 5)
      .define('#', TinkerSmeltery.scorchedBrick)
      .pattern("# #")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "channel"));

    // casting
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedBasin.get())
      .define('#', TinkerSmeltery.scorchedBrick)
      .pattern("# #")
      .pattern("# #")
      .pattern("###")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "basin"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedTable.get())
      .define('#', TinkerSmeltery.scorchedBrick)
      .pattern("###")
      .pattern("# #")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "table"));


    // peripherals
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedDrain)
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('C', TinkerCommons.obsidianPane)
      .pattern("# #")
      .pattern("C C")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "drain"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedDrain)
          .define('#', TinkerTags.Items.FOUNDRY_BRICKS)
          .define('C', TinkerCommons.obsidianPane)
          .pattern("C#C")
          .unlockedBy("has_item", has(TinkerTags.Items.FOUNDRY_BRICKS)))
      .setSource(TinkerTags.Items.FOUNDRY_BRICKS)
      .build(consumer, this.modResource(folder + "drain_retextured"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedChute)
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('C', TinkerCommons.obsidianPane)
      .pattern("#C#")
      .pattern("   ")
      .pattern("#C#")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "chute"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedChute)
          .define('#', TinkerTags.Items.FOUNDRY_BRICKS)
          .define('C', TinkerCommons.obsidianPane)
          .pattern("C")
          .pattern("#")
          .pattern("C")
          .unlockedBy("has_item", has(TinkerTags.Items.FOUNDRY_BRICKS)))
      .setSource(TinkerTags.Items.FOUNDRY_BRICKS)
      .build(consumer, this.modResource(folder + "chute_retextured"));
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedDuct)
      .define('#', TinkerSmeltery.scorchedBrick)
      .define('C', TinkerMaterials.cobalt.getIngotTag())
      .pattern("# #")
      .pattern("C C")
      .pattern("# #")
      .unlockedBy("has_item", has(TinkerMaterials.cobalt.getIngotTag()))
      .save(consumer, this.modResource(folder + "duct"));
    ShapedRetexturedRecipeBuilder.fromShaped(
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedDuct)
          .define('#', TinkerTags.Items.FOUNDRY_BRICKS)
          .define('C', TinkerMaterials.cobalt.getIngotTag())
          .pattern("C#C")
          .unlockedBy("has_item", has(TinkerTags.Items.FOUNDRY_BRICKS)))
      .setSource(TinkerTags.Items.FOUNDRY_BRICKS)
      .build(consumer, this.modResource(folder + "duct_retextured"));

    // controllers
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerSmeltery.scorchedAlloyer)
      .define('G', Ingredient.of(TinkerSmeltery.scorchedTank.get(TankType.INGOT_GAUGE), TinkerSmeltery.scorchedTank.get(TankType.FUEL_GAUGE)))
      .define('B', TinkerSmeltery.scorchedBrick)
      .pattern("BGB")
      .pattern("BBB")
      .unlockedBy("has_item", has(TinkerSmeltery.scorchedBrick))
      .save(consumer, this.modResource(folder + "alloyer"));

    // casting
    String castingFolder = "smeltery/casting/scorched/";
    ItemCastingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedStone)
      .setFluidAndTime(TinkerFluids.scorchedStone, false, FluidValues.BRICK_BLOCK)
      .save(consumer, this.modResource(castingFolder + "stone_from_scorched"));
    this.ingotCasting(consumer, TinkerFluids.scorchedStone, FluidValues.BRICK, TinkerSmeltery.scorchedBrick, castingFolder + "brick");
    ItemCastingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedGlass)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.GEM)
      .setCast(TinkerSmeltery.scorchedBricks, true)
      .save(consumer, this.modResource(castingFolder + "glass"));
    // discount for casting panes
    ItemCastingRecipeBuilder.tableRecipe(TinkerSmeltery.scorchedGlassPane)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.GEM_SHARD)
      .setCast(TinkerSmeltery.scorchedBrick, true)
      .save(consumer, this.modResource(castingFolder + "glass_pane"));
    // craft scorched stone from magma and basalt
    // flint is almost a brick
    ItemCastingRecipeBuilder.tableRecipe(TinkerSmeltery.scorchedBrick)
      .setFluidAndTime(TinkerFluids.magma, true, FluidValues.SLIMEBALL / 2)
      .setCast(Items.FLINT, true)
      .save(consumer, this.modResource(castingFolder + "brick_composite"));
    this.scorchedCasting(consumer, TinkerSmeltery.scorchedStone, Ingredient.of(Blocks.BASALT, Blocks.GRAVEL), castingFolder + "stone_from_magma");
    this.scorchedCasting(consumer, TinkerSmeltery.polishedScorchedStone, Ingredient.of(Blocks.POLISHED_BASALT), castingFolder + "polished_from_magma");
    // foundry controller
    ItemCastingRecipeBuilder.retexturedBasinRecipe(ItemOutput.fromItem(TinkerSmeltery.foundryController))
      .setCast(TinkerTags.Items.FOUNDRY_BRICKS, true)
      .setFluidAndTime(TinkerFluids.moltenObsidian, false, FluidValues.GLASS_BLOCK)
      .save(consumer, this.prefix(TinkerSmeltery.foundryController.getRegistryName(), castingFolder));


    // melting
    String meltingFolder = "smeltery/melting/scorched/";

    // double efficiency when using smeltery for grout
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.netherGrout), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 2, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "grout"));

    // scorched stone
    // stairs are here since the cheapest stair recipe is stone cutter, 1 to 1
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(Ingredient.of(TinkerTags.Items.SCORCHED_BLOCKS),
          Ingredient.of(TinkerSmeltery.scorchedLadder, TinkerSmeltery.scorchedBricks.getStairs(), TinkerSmeltery.scorchedRoad.getStairs())),
        TinkerFluids.scorchedStone.get(), FluidValues.BRICK_BLOCK, 2.0f)
      .save(consumer, this.modResource(meltingFolder + "block"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedBricks.getSlab(), TinkerSmeltery.scorchedBricks.getSlab(), TinkerSmeltery.scorchedRoad.getSlab()),
        TinkerFluids.scorchedStone.get(), FluidValues.BRICK_BLOCK / 2, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "slab"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedBrick), TinkerFluids.scorchedStone.get(), FluidValues.BRICK, 1.0f)
      .save(consumer, this.modResource(meltingFolder + "brick"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedBricks.getFence()), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 3, 1.0f)
      .save(consumer, this.modResource(meltingFolder + "fence"));

    // melt down foundry components
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedFaucet, TinkerSmeltery.scorchedChannel), TinkerFluids.scorchedStone.get(), FluidValues.BRICK, 1.5f)
      .save(consumer, this.modResource(meltingFolder + "faucet"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedBasin, TinkerSmeltery.scorchedTable), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 7, 2.5f)
      .save(consumer, this.modResource(meltingFolder + "casting"));
    // tanks
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.scorchedTank.get(TankType.FUEL_TANK)), true), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 8, 3f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM))
      .save(consumer, this.modResource(meltingFolder + "fuel_tank"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.scorchedTank.get(TankType.INGOT_TANK)), true), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 6, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 3))
      .save(consumer, this.modResource(meltingFolder + "ingot_tank"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.scorchedTank.get(TankType.FUEL_GAUGE)), true),
          DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.scorchedTank.get(TankType.INGOT_GAUGE)), true)),
        TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 4, 2f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 5))
      .save(consumer, this.modResource(meltingFolder + "gauge"));
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.scorchedLantern), true), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 2, 1.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM_SHARD))
      .addByproduct(new FluidStack(TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 3))
      .save(consumer, this.modResource(meltingFolder + "lantern"));
    // glass
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedGlass), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 4, 2f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM))
      .save(consumer, this.modResource(meltingFolder + "glass"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedGlassPane), TinkerFluids.scorchedStone.get(), FluidValues.BRICK, 1.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM_SHARD))
      .save(consumer, this.modResource(meltingFolder + "pane"));
    // controllers
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedAlloyer), TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 9, 3.5f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 5))
      .save(consumer, this.modResource(meltingFolder + "melter"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.foundryController), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK, 3.5f)
      .addByproduct(new FluidStack(TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/obsidian/foundry_controller"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedDrain, TinkerSmeltery.scorchedChute), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE * 2, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/obsidian/foundry_io"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.scorchedDuct), TinkerFluids.moltenCobalt.get(), FluidValues.INGOT * 2, 2.5f)
      .addByproduct(new FluidStack(TinkerFluids.scorchedStone.get(), FluidValues.BRICK * 4))
      .save(consumer, this.modResource("smeltery/melting/metal/cobalt/scorched_duct"));
  }

  private void addCastingRecipes(Consumer<FinishedRecipe> consumer) {
    // Pure Fluid Recipes
    String folder = "smeltery/casting/";

    // container filling
    ContainerFillingRecipeBuilder.tableRecipe(Items.BUCKET, FluidConstants.BUCKET)
      .save(consumer, this.modResource(folder + "filling/bucket"));
    ContainerFillingRecipeBuilder.tableRecipe(TinkerSmeltery.copperCan, FluidValues.INGOT)
      .save(consumer, this.modResource(folder + "filling/copper_can"));
    // potion filling
    PotionCastingRecipeBuilder.tableRecipe(Items.POTION)
      .setBottle(Items.GLASS_BOTTLE)
      .setFluid(TinkerTags.Fluids.POTION, FluidValues.BOTTLE)
      .save(consumer, this.modResource(folder + "filling/bottle"));
    PotionCastingRecipeBuilder.tableRecipe(Items.SPLASH_POTION)
      .setBottle(TinkerTags.Items.SPLASH_BOTTLE)
      .setFluid(TinkerTags.Fluids.POTION, FluidValues.BOTTLE)
      .save(consumer, this.modResource(folder + "filling/lingerng_bottle"));
    PotionCastingRecipeBuilder.tableRecipe(Items.LINGERING_POTION)
      .setBottle(TinkerTags.Items.LINGERING_BOTTLE)
      .setFluid(TinkerTags.Fluids.POTION, FluidValues.BOTTLE)
      .save(consumer, this.modResource(folder + "filling/splash_bottle"));
    PotionCastingRecipeBuilder.tableRecipe(Items.TIPPED_ARROW)
      .setBottle(Items.ARROW)
      .setFluid(TinkerTags.Fluids.POTION, FluidValues.BOTTLE / 10)
      .setCoolingTime(20)
      .save(consumer, this.modResource(folder + "filling/tipped_arrow"));
    // tank filling - seared
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.searedTank.get(TankType.INGOT_TANK), FluidValues.INGOT)
      .save(consumer, this.modResource(folder + "filling/seared_ingot_tank"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.searedTank.get(TankType.INGOT_GAUGE), FluidValues.INGOT)
      .save(consumer, this.modResource(folder + "filling/seared_ingot_gauge"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.searedTank.get(TankType.FUEL_TANK), FluidConstants.BUCKET / 4)
      .save(consumer, this.modResource(folder + "filling/seared_fuel_tank"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.searedTank.get(TankType.FUEL_GAUGE), FluidConstants.BUCKET / 4)
      .save(consumer, this.modResource(folder + "filling/seared_fuel_gauge"));
    ContainerFillingRecipeBuilder.tableRecipe(TinkerSmeltery.searedLantern, FluidValues.NUGGET)
      .save(consumer, this.modResource(folder + "filling/seared_lantern_pixel"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.searedLantern, FluidValues.LANTERN_CAPACITY)
      .save(consumer, this.modResource(folder + "filling/seared_lantern_full"));
    // tank filling - scorched
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedTank.get(TankType.INGOT_TANK), FluidValues.INGOT)
      .save(consumer, this.modResource(folder + "filling/scorched_ingot_tank"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedTank.get(TankType.INGOT_GAUGE), FluidValues.INGOT)
      .save(consumer, this.modResource(folder + "filling/scorched_ingot_gauge"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedTank.get(TankType.FUEL_TANK), FluidConstants.BUCKET / 4)
      .save(consumer, this.modResource(folder + "filling/scorched_fuel_tank"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedTank.get(TankType.FUEL_GAUGE), FluidConstants.BUCKET / 4)
      .save(consumer, this.modResource(folder + "filling/scorched_fuel_gauge"));
    ContainerFillingRecipeBuilder.tableRecipe(TinkerSmeltery.scorchedLantern, FluidValues.NUGGET)
      .save(consumer, this.modResource(folder + "filling/scorched_lantern_pixel"));
    ContainerFillingRecipeBuilder.basinRecipe(TinkerSmeltery.scorchedLantern, FluidValues.LANTERN_CAPACITY)
      .save(consumer, this.modResource(folder + "filling/scorched_lantern_full"));

    // Slime
    String slimeFolder = folder + "slime/";
    this.slimeCasting(consumer, TinkerFluids.blood, false, SlimeType.BLOOD, slimeFolder);
    ItemCastingRecipeBuilder.tableRecipe(TinkerMaterials.bloodbone)
      .setFluidAndTime(TinkerFluids.blood, false, FluidValues.SLIMEBALL)
      .setCast(Tags.Items.BONES, true)
      .save(consumer, this.modResource(slimeFolder + "blood/bone"));
    this.slimeCasting(consumer, TinkerFluids.earthSlime, true, SlimeType.EARTH, slimeFolder);
    this.slimeCasting(consumer, TinkerFluids.skySlime, false, SlimeType.SKY, slimeFolder);
    this.slimeCasting(consumer, TinkerFluids.enderSlime, false, SlimeType.ENDER, slimeFolder);
    // magma cream
    ItemCastingRecipeBuilder.basinRecipe(Blocks.MAGMA_BLOCK)
      .setFluidAndTime(TinkerFluids.magma, true, FluidValues.SLIME_CONGEALED)
      .save(consumer, this.modResource(slimeFolder + "magma_block"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerFluids.magmaBottle)
      .setFluid(TinkerFluids.magma.getForgeTag(), FluidValues.SLIMEBALL)
      .setCoolingTime(1)
      .setCast(Items.GLASS_BOTTLE, true)
      .save(consumer, this.modResource(slimeFolder + "magma_bottle"));

    // glass
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.clearGlass)
      .setFluidAndTime(TinkerFluids.moltenGlass, false, FluidValues.GLASS_BLOCK)
      .save(consumer, this.modResource(folder + "glass/block"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.clearGlassPane)
      .setFluidAndTime(TinkerFluids.moltenGlass, false, FluidValues.GLASS_PANE)
      .save(consumer, this.modResource(folder + "glass/pane"));
    // soul glass
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.soulGlass)
      .setFluidAndTime(TinkerFluids.liquidSoul, false, FluidValues.GLASS_BLOCK)
      .save(consumer, this.modResource(folder + "soul/glass"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.soulGlassPane)
      .setFluidAndTime(TinkerFluids.liquidSoul, false, FluidValues.GLASS_PANE)
      .save(consumer, this.modResource(folder + "soul/pane"));

    // clay
    ItemCastingRecipeBuilder.basinRecipe(Blocks.TERRACOTTA)
      .setFluidAndTime(TinkerFluids.moltenClay, false, FluidValues.SLIME_CONGEALED)
      .save(consumer, this.modResource(folder + "clay/block"));
    this.ingotCasting(consumer, TinkerFluids.moltenClay, false, FluidValues.SLIMEBALL, Items.BRICK, folder + "clay/brick");
    this.tagCasting(consumer, TinkerFluids.moltenClay, false, FluidValues.SLIMEBALL, TinkerSmeltery.plateCast, "plates/brick", folder + "clay/plate", true);

    // emeralds
    this.gemCasting(consumer, TinkerFluids.moltenEmerald, Items.EMERALD, folder + "emerald/gem");
    ItemCastingRecipeBuilder.basinRecipe(Blocks.EMERALD_BLOCK)
      .setFluidAndTime(TinkerFluids.moltenEmerald, false, FluidValues.LARGE_GEM_BLOCK)
      .save(consumer, this.modResource(folder + "emerald/block"));

    // quartz
    this.gemCasting(consumer, TinkerFluids.moltenQuartz, Items.QUARTZ, folder + "quartz/gem");
    ItemCastingRecipeBuilder.basinRecipe(Blocks.QUARTZ_BLOCK)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.SMALL_GEM_BLOCK)
      .save(consumer, this.modResource(folder + "quartz/block"));

    // amethyst
    this.gemCasting(consumer, TinkerFluids.moltenAmethyst, Items.AMETHYST_SHARD, folder + "amethyst/shard");
    ItemCastingRecipeBuilder.basinRecipe(Blocks.AMETHYST_BLOCK)
      .setFluidAndTime(TinkerFluids.moltenAmethyst, false, FluidValues.SMALL_GEM_BLOCK)
      .save(consumer, this.modResource(folder + "amethyst/block"));
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.clearTintedGlass)
      .setCast(Tags.Items.GLASS_COLORLESS, true)
      .setFluidAndTime(TinkerFluids.moltenAmethyst, false, FluidValues.GEM * 2)
      .save(consumer, this.modResource(folder + "amethyst/glass"));

    // diamond
    this.gemCasting(consumer, TinkerFluids.moltenDiamond, Items.DIAMOND, folder + "diamond/gem");
    ItemCastingRecipeBuilder.basinRecipe(Blocks.DIAMOND_BLOCK)
      .setFluidAndTime(TinkerFluids.moltenDiamond, false, FluidValues.LARGE_GEM_BLOCK)
      .save(consumer, this.modResource(folder + "diamond/block"));

    // ender pearls
    ItemCastingRecipeBuilder.tableRecipe(Items.ENDER_PEARL)
      .setFluidAndTime(TinkerFluids.moltenEnder, true, FluidValues.SLIMEBALL)
      .save(consumer, this.modResource(folder + "ender/pearl"));
    ItemCastingRecipeBuilder.tableRecipe(Items.ENDER_EYE)
      .setFluidAndTime(TinkerFluids.moltenEnder, true, FluidValues.SLIMEBALL)
      .setCast(Items.BLAZE_POWDER, true)
      .save(consumer, this.modResource(folder + "ender/eye"));

    // obsidian
    ItemCastingRecipeBuilder.basinRecipe(Blocks.OBSIDIAN)
      .setFluidAndTime(TinkerFluids.moltenObsidian, false, FluidValues.GLASS_BLOCK)
      .save(consumer, this.modResource(folder + "obsidian/block"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.obsidianPane)
      .setFluidAndTime(TinkerFluids.moltenObsidian, false, FluidValues.GLASS_PANE)
      .save(consumer, this.modResource(folder + "obsidian/pane"));
    // Molten objects with Bucket, Block, Ingot, and Nugget forms with standard values
    String metalFolder = folder + "metal/";
    this.metalCasting(consumer, TinkerFluids.moltenIron, true, Items.IRON_BLOCK, Items.IRON_INGOT, Items.IRON_NUGGET, metalFolder, "iron");
    this.metalCasting(consumer, TinkerFluids.moltenGold, true, Items.GOLD_BLOCK, Items.GOLD_INGOT, Items.GOLD_NUGGET, metalFolder, "gold");
    this.metalCasting(consumer, TinkerFluids.moltenCopper, true, Items.COPPER_BLOCK, Items.COPPER_INGOT, null, metalFolder, "copper");
    this.metalCasting(consumer, TinkerFluids.moltenNetherite, true, Blocks.NETHERITE_BLOCK, Items.NETHERITE_INGOT, null, metalFolder, "netherite");
    this.ingotCasting(consumer, TinkerFluids.moltenDebris, false, Items.NETHERITE_SCRAP, metalFolder + "netherite/scrap");
    this.tagCasting(consumer, TinkerFluids.moltenCopper, true, FluidValues.NUGGET, TinkerSmeltery.nuggetCast, TinkerTags.Items.NUGGETS_COPPER.location().getPath(), metalFolder + "copper/nugget", false);
    this.tagCasting(consumer, TinkerFluids.moltenNetherite, true, FluidValues.NUGGET, TinkerSmeltery.nuggetCast, TinkerTags.Items.NUGGETS_NETHERITE.location().getPath(), metalFolder + "netherite/nugget", false);
    this.tagCasting(consumer, TinkerFluids.moltenDebris, false, FluidValues.NUGGET, TinkerSmeltery.nuggetCast, TinkerTags.Items.NUGGETS_NETHERITE_SCRAP.location().getPath(), metalFolder + "netherite/debris_nugget", false);

    // anything common uses tag output, if its unique to us (slime metals mostly), use direct output
    // ores
    this.metalTagCasting(consumer, TinkerFluids.moltenCobalt, "cobalt", metalFolder, true);
    // tier 3 alloys
    this.metalTagCasting(consumer, TinkerFluids.moltenAmethystBronze, "amethyst_bronze", metalFolder, true);
    this.metalTagCasting(consumer, TinkerFluids.moltenRoseGold, "rose_gold", metalFolder, true);
    this.metalCasting(consumer, TinkerFluids.moltenSlimesteel, TinkerMaterials.slimesteel, metalFolder, "slimesteel");
    this.metalCasting(consumer, TinkerFluids.moltenPigIron, TinkerMaterials.pigIron, metalFolder, "pig_iron");
    // tier 4 alloys
    this.metalTagCasting(consumer, TinkerFluids.moltenManyullyn, "manyullyn", metalFolder, true);
    this.metalTagCasting(consumer, TinkerFluids.moltenHepatizon, "hepatizon", metalFolder, true);
    this.metalCasting(consumer, TinkerFluids.moltenQueensSlime, TinkerMaterials.queensSlime, metalFolder, "queens_slime");
    this.metalCasting(consumer, TinkerFluids.moltenSoulsteel, TinkerMaterials.soulsteel, metalFolder, "soulsteel");
    // tier 5 alloys
    this.metalCasting(consumer, TinkerFluids.moltenKnightslime, TinkerMaterials.knightslime, metalFolder, "knightslime");

    // compat
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      this.metalTagCasting(consumer, compat.getFluid(), compat.getName(), metalFolder, false);
    }

    // water
    String waterFolder = folder + "water/";
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.mudBricks)
      .setFluidAndTime(new FluidStack(Fluids.WATER, FluidConstants.BUCKET / 10))
      .setCast(Items.DIRT, true)
      .save(consumer, this.prefix(TinkerCommons.mudBricks.getRegistryName(), waterFolder));
    ItemCastingRecipeBuilder.tableRecipe(ItemOutput.fromStack(PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)))
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE * 2)
      .setCoolingTime(1)
      .setCast(Items.GLASS_BOTTLE, true)
      .save(consumer, this.modResource(waterFolder + "bottle"));
    ItemCastingRecipeBuilder.tableRecipe(ItemOutput.fromStack(PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.WATER)))
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE * 2)
      .setCoolingTime(1)
      .setCast(TinkerTags.Items.SPLASH_BOTTLE, true)
      .save(consumer, this.modResource(waterFolder + "splash"));
    ItemCastingRecipeBuilder.tableRecipe(ItemOutput.fromStack(PotionUtils.setPotion(new ItemStack(Items.LINGERING_POTION), Potions.WATER)))
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE * 2)
      .setCoolingTime(1)
      .setCast(TinkerTags.Items.LINGERING_BOTTLE, true)
      .save(consumer, this.modResource(waterFolder + "lingering"));
    // casting concrete
    BiConsumer<Block, Block> concreteCasting = (powder, block) ->
      ItemCastingRecipeBuilder.basinRecipe(block)
        .setFluidAndTime(new FluidStack(Fluids.WATER, FluidConstants.BUCKET / 10))
        .setCast(powder, true)
        .save(consumer, this.prefix(BuiltInRegistries.BLOCK.getKey(block), waterFolder));
    concreteCasting.accept(Blocks.WHITE_CONCRETE_POWDER, Blocks.WHITE_CONCRETE);
    concreteCasting.accept(Blocks.ORANGE_CONCRETE_POWDER, Blocks.ORANGE_CONCRETE);
    concreteCasting.accept(Blocks.MAGENTA_CONCRETE_POWDER, Blocks.MAGENTA_CONCRETE);
    concreteCasting.accept(Blocks.LIGHT_BLUE_CONCRETE_POWDER, Blocks.LIGHT_BLUE_CONCRETE);
    concreteCasting.accept(Blocks.YELLOW_CONCRETE_POWDER, Blocks.YELLOW_CONCRETE);
    concreteCasting.accept(Blocks.LIME_CONCRETE_POWDER, Blocks.LIME_CONCRETE);
    concreteCasting.accept(Blocks.PINK_CONCRETE_POWDER, Blocks.PINK_CONCRETE);
    concreteCasting.accept(Blocks.GRAY_CONCRETE_POWDER, Blocks.GRAY_CONCRETE);
    concreteCasting.accept(Blocks.LIGHT_GRAY_CONCRETE_POWDER, Blocks.LIGHT_GRAY_CONCRETE);
    concreteCasting.accept(Blocks.CYAN_CONCRETE_POWDER, Blocks.CYAN_CONCRETE);
    concreteCasting.accept(Blocks.PURPLE_CONCRETE_POWDER, Blocks.PURPLE_CONCRETE);
    concreteCasting.accept(Blocks.BLUE_CONCRETE_POWDER, Blocks.BLUE_CONCRETE);
    concreteCasting.accept(Blocks.BROWN_CONCRETE_POWDER, Blocks.BROWN_CONCRETE);
    concreteCasting.accept(Blocks.GREEN_CONCRETE_POWDER, Blocks.GREEN_CONCRETE);
    concreteCasting.accept(Blocks.RED_CONCRETE_POWDER, Blocks.RED_CONCRETE);
    concreteCasting.accept(Blocks.BLACK_CONCRETE_POWDER, Blocks.BLACK_CONCRETE);

    // misc
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.lavawood)
      .setFluidAndTime(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 10))
      .setCast(ItemTags.PLANKS, true)
      .save(consumer, this.prefix(TinkerCommons.lavawood.getRegistryName(), folder));
    ItemCastingRecipeBuilder.basinRecipe(TinkerCommons.blazewood)
      .setFluidAndTime(TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 10)
      .setCast(DefaultCustomIngredients.all(Ingredient.of(ItemTags.PLANKS), Ingredient.of(ItemTags.NON_FLAMMABLE_WOOD)), true)
      .save(consumer, this.prefix(TinkerCommons.blazewood.getRegistryName(), folder));

    // cast molten blaze into blazing stuff
    this.castingWithCast(consumer, TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 10, TinkerSmeltery.rodCast, Items.BLAZE_ROD, folder + "blaze/rod");
    ItemCastingRecipeBuilder.tableRecipe(Items.MAGMA_CREAM)
      .setFluidAndTime(TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 20)
      .setCast(Tags.Items.SLIMEBALLS, true)
      .save(consumer, this.modResource(folder + "blaze/cream"));
    ItemCastingRecipeBuilder.basinRecipe(Blocks.MAGMA_BLOCK)
      .setFluidAndTime(TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 5)
      .setCast(TinkerTags.Items.CONGEALED_SLIME, true)
      .save(consumer, this.modResource(folder + "blaze/congealed"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerMaterials.blazingBone)
      .setFluidAndTime(TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 5)
      .setCast(TinkerTags.Items.WITHER_BONES, true)
      .save(consumer, this.modResource(folder + "blaze/bone"));

    // honey
    ItemCastingRecipeBuilder.tableRecipe(Items.HONEY_BOTTLE)
      .setFluid(TinkerFluids.honey.getForgeTag(), FluidValues.BOTTLE)
      .setCoolingTime(1)
      .setCast(Items.GLASS_BOTTLE, true)
      .save(consumer, this.modResource(folder + "honey/bottle"));
    ItemCastingRecipeBuilder.basinRecipe(Items.HONEY_BLOCK)
      .setFluidAndTime(TinkerFluids.honey, true, FluidValues.BOTTLE * 4)
      .save(consumer, this.modResource(folder + "honey/block"));
    // soup
    ItemCastingRecipeBuilder.tableRecipe(Items.BEETROOT_SOUP)
      .setFluid(TinkerFluids.beetrootSoup.getForgeTag(), FluidValues.BOWL)
      .setCast(Items.BOWL, true)
      .setCoolingTime(1)
      .save(consumer, this.modResource(folder + "soup/beetroot"));
    ItemCastingRecipeBuilder.tableRecipe(Items.MUSHROOM_STEW)
      .setFluid(TinkerFluids.mushroomStew.getForgeTag(), FluidValues.BOWL)
      .setCast(Items.BOWL, true)
      .setCoolingTime(1)
      .save(consumer, this.modResource(folder + "soup/mushroom"));
    ItemCastingRecipeBuilder.tableRecipe(Items.RABBIT_STEW)
      .setFluid(TinkerFluids.rabbitStew.getForgeTag(), FluidValues.BOWL)
      .setCast(Items.BOWL, true)
      .setCoolingTime(1)
      .save(consumer, this.modResource(folder + "soup/rabbit"));
    // venom
    ItemCastingRecipeBuilder.tableRecipe(TinkerFluids.venomBottle)
      .setFluid(TinkerFluids.venom.getLocalTag(), FluidValues.BOTTLE)
      .setCoolingTime(1)
      .setCast(Items.GLASS_BOTTLE, true)
      .save(consumer, this.modResource(folder + "venom_bottle"));

    String castFolder = "smeltery/casts/";
    this.castCreation(consumer, Tags.Items.INGOTS, TinkerSmeltery.ingotCast, castFolder);
    this.castCreation(consumer, Tags.Items.NUGGETS, TinkerSmeltery.nuggetCast, castFolder);
    this.castCreation(consumer, Tags.Items.GEMS, TinkerSmeltery.gemCast, castFolder);
    this.castCreation(consumer, Tags.Items.RODS, TinkerSmeltery.rodCast, castFolder);
    // other casts are added if needed
    this.castCreation(this.withCondition(consumer, this.tagCondition("plates")), this.getItemTag("c", "plates"), TinkerSmeltery.plateCast, castFolder);
    this.castCreation(this.withCondition(consumer, this.tagCondition("gears")), this.getItemTag("c", "gears"), TinkerSmeltery.gearCast, castFolder);
    this.castCreation(this.withCondition(consumer, this.tagCondition("coins")), this.getItemTag("c", "coins"), TinkerSmeltery.coinCast, castFolder);
    this.castCreation(this.withCondition(consumer, this.tagCondition("wires")), this.getItemTag("c", "wires"), TinkerSmeltery.wireCast, castFolder);

    // misc casting - gold
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.goldBars)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET * 3)
      .save(consumer, this.modResource(metalFolder + "gold/bars"));
    ItemCastingRecipeBuilder.tableRecipe(Items.GOLDEN_APPLE)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.INGOT * 8)
      .setCast(Items.APPLE, true)
      .save(consumer, this.modResource(metalFolder + "gold/apple"));
    ItemCastingRecipeBuilder.tableRecipe(Items.GLISTERING_MELON_SLICE)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET * 8)
      .setCast(Items.MELON_SLICE, true)
      .save(consumer, this.modResource(metalFolder + "gold/melon"));
    ItemCastingRecipeBuilder.tableRecipe(Items.GOLDEN_CARROT)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET * 8)
      .setCast(Items.CARROT, true)
      .save(consumer, this.modResource(metalFolder + "gold/carrot"));
    ItemCastingRecipeBuilder.tableRecipe(Items.CLOCK)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.INGOT * 4)
      .setCast(Items.REDSTONE, true)
      .save(consumer, this.modResource(metalFolder + "gold/clock"));
    // misc casting - iron
    ItemCastingRecipeBuilder.tableRecipe(Blocks.IRON_BARS)  // cheaper by 6mb, not a duplication as the melting recipe was adjusted too (like panes)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.NUGGET * 3)
      .save(consumer, this.modResource(metalFolder + "iron/bars"));
    ItemCastingRecipeBuilder.tableRecipe(Items.LANTERN)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.NUGGET * 8)
      .setCast(Blocks.TORCH, true)
      .save(consumer, this.modResource(metalFolder + "iron/lantern"));
    ItemCastingRecipeBuilder.tableRecipe(Items.SOUL_LANTERN)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.NUGGET * 8)
      .setCast(Blocks.SOUL_TORCH, true)
      .save(consumer, this.modResource(metalFolder + "iron/soul_lantern"));
    ItemCastingRecipeBuilder.tableRecipe(Items.COMPASS)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.INGOT * 4)
      .setCast(Items.REDSTONE, true)
      .save(consumer, this.modResource(metalFolder + "iron/compass"));
    // ender chest
    ItemCastingRecipeBuilder.basinRecipe(Blocks.ENDER_CHEST)
      .setFluidAndTime(TinkerFluids.moltenObsidian, false, FluidValues.GLASS_BLOCK * 8)
      .setCast(Items.ENDER_EYE, true)
      .save(consumer, this.modResource(folder + "obsidian/chest"));
    ItemCastingRecipeBuilder.basinRecipe(TinkerMaterials.nahuatl)
      .setFluidAndTime(TinkerFluids.moltenObsidian, false, FluidConstants.BUCKET)
      .setCast(ItemTags.PLANKS, true)
      .save(consumer, this.modResource(folder + "obsidian/nahuatl"));
    // overworld stones from quartz
    ItemCastingRecipeBuilder.basinRecipe(Blocks.ANDESITE)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.GEM / 2)
      .setCast(Tags.Items.COBBLESTONE, true)
      .save(consumer, this.prefix(BuiltInRegistries.BLOCK.getKey(Blocks.ANDESITE), folder + "quartz/"));
    ItemCastingRecipeBuilder.basinRecipe(Blocks.DIORITE)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.GEM / 2)
      .setCast(Blocks.ANDESITE, true)
      .save(consumer, this.prefix(BuiltInRegistries.BLOCK.getKey(Blocks.DIORITE), folder + "quartz/"));
    ItemCastingRecipeBuilder.basinRecipe(Blocks.GRANITE)
      .setFluidAndTime(TinkerFluids.moltenQuartz, false, FluidValues.GEM)
      .setCast(Blocks.DIORITE, true)
      .save(consumer, this.prefix(BuiltInRegistries.BLOCK.getKey(Blocks.GRANITE), folder + "quartz/"));
  }

  private void addMeltingRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "smeltery/melting/";

    // water from ice
    MeltingRecipeBuilder.melting(Ingredient.of(Items.ICE), Fluids.WATER, FluidConstants.BUCKET, 1.0f)
      .save(consumer, this.modResource(folder + "water/ice"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.PACKED_ICE), Fluids.WATER, FluidConstants.BUCKET * 9, 3.0f)
      .save(consumer, this.modResource(folder + "water/packed_ice"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.BLUE_ICE), Fluids.WATER, FluidConstants.BUCKET * 81, 9.0f)
      .save(consumer, this.modResource(folder + "water/blue_ice"));
    // water from snow
    MeltingRecipeBuilder.melting(Ingredient.of(Items.SNOWBALL), Fluids.WATER, FluidConstants.BUCKET / 8, 0.5f)
      .save(consumer, this.modResource(folder + "water/snowball"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.SNOW_BLOCK), Fluids.WATER, FluidConstants.BUCKET / 2, 0.75f)
      .save(consumer, this.modResource(folder + "water/snow_block"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.SNOW), Fluids.WATER, FluidConstants.BUCKET / 8, 0.5f)
      .save(consumer, this.modResource(folder + "water/snow_layer"));

    // ores
    String metalFolder = folder + "metal/";
    this.metalMelting(consumer, TinkerFluids.moltenIron.get(), "iron", true, metalFolder, false, Byproduct.NICKEL, Byproduct.COPPER);
    this.metalMelting(consumer, TinkerFluids.moltenGold.get(), "gold", true, metalFolder, false, Byproduct.COPPER);
    this.metalMelting(consumer, TinkerFluids.moltenCopper.get(), "copper", true, metalFolder, false, Byproduct.SMALL_GOLD);
    this.metalMelting(consumer, TinkerFluids.moltenCobalt.get(), "cobalt", true, metalFolder, false, Byproduct.IRON);

    MeltingRecipeBuilder.melting(Ingredient.of(Tags.Items.ORES_NETHERITE_SCRAP), TinkerFluids.moltenDebris.get(), FluidValues.INGOT, 2.0f)
      .setOre(OreRateType.METAL, OreRateType.GEM, OreRateType.METAL)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM))
      .addByproduct(new FluidStack(TinkerFluids.moltenGold.get(), FluidValues.INGOT * 3))
      .save(consumer, this.modResource(metalFolder + "molten_debris/ore"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerTags.Items.INGOTS_NETHERITE_SCRAP), TinkerFluids.moltenDebris.get(), FluidValues.INGOT, 1.0f)
      .save(consumer, this.modResource(metalFolder + "molten_debris/scrap"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerTags.Items.NUGGETS_NETHERITE_SCRAP), TinkerFluids.moltenDebris.get(), FluidValues.NUGGET, 1 / 3f)
      .save(consumer, this.modResource(metalFolder + "molten_debris/debris_nugget"));

    // tier 3
    this.metalMelting(consumer, TinkerFluids.moltenSlimesteel.get(), "slimesteel", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenAmethystBronze.get(), "amethyst_bronze", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenRoseGold.get(), "rose_gold", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenPigIron.get(), "pig_iron", false, metalFolder, false);
    // tier 4
    this.metalMelting(consumer, TinkerFluids.moltenManyullyn.get(), "manyullyn", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenHepatizon.get(), "hepatizon", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenQueensSlime.get(), "queens_slime", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenSoulsteel.get(), "soulsteel", false, metalFolder, false);
    this.metalMelting(consumer, TinkerFluids.moltenNetherite.get(), "netherite", false, metalFolder, false);
    // tier 5
    this.metalMelting(consumer, TinkerFluids.moltenKnightslime.get(), "knightslime", false, metalFolder, false);

    // compat
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      this.metalMelting(consumer, compat.getFluid().get(), compat.getName(), compat.isOre(), compat.hasDust(), metalFolder, true, compat.getByproducts());
    }

    // blood
    MeltingRecipeBuilder.melting(Ingredient.of(Items.ROTTEN_FLESH), TinkerFluids.blood.get(), FluidValues.SLIMEBALL / 5, 1.0f)
      .save(consumer, this.modResource(folder + "slime/blood/flesh"));
    // venom
    MeltingRecipeBuilder.melting(Ingredient.of(Items.SPIDER_EYE), TinkerFluids.venom.get(), FluidValues.BOTTLE / 5, 1.0f)
      .save(consumer, this.modResource(folder + "venom/eye"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.FERMENTED_SPIDER_EYE), TinkerFluids.venom.get(), FluidValues.BOTTLE * 2 / 5, 1.0f)
      .save(consumer, this.modResource(folder + "venom/fermented_eye"));

    // glass
    MeltingRecipeBuilder.melting(Ingredient.of(Tags.Items.SAND), TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK, 1.5f)
      .save(consumer, this.modResource(folder + "glass/sand"));
    MeltingRecipeBuilder.melting(Ingredient.of(Tags.Items.GLASS_SILICA), TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK, 1.0f)
      .save(consumer, this.modResource(folder + "glass/block"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerTags.Items.GLASS_PANES_SILICA), TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE, 0.5f)
      .save(consumer, this.modResource(folder + "glass/pane"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GLASS_BOTTLE), TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK, 1.25f)
      .save(consumer, this.modResource(folder + "glass/bottle"));
    // melt extra sand casts back
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerSmeltery.blankSandCast, TinkerSmeltery.blankRedSandCast),
        TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE, 0.75f)
      .save(consumer, this.modResource(folder + "glass/sand_cast"));

    // liquid soul
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.SOUL_SAND, Blocks.SOUL_SOIL), TinkerFluids.liquidSoul.get(), FluidValues.GLASS_BLOCK, 1.5f)
      .save(consumer, this.modResource(folder + "soul/sand"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.soulGlass), TinkerFluids.liquidSoul.get(), FluidValues.GLASS_BLOCK, 1.0f)
      .save(consumer, this.modResource(folder + "soul/glass"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.soulGlassPane), TinkerFluids.liquidSoul.get(), FluidValues.GLASS_PANE, 0.5f)
      .save(consumer, this.modResource(folder + "soul/pane"));

    // clay
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.CLAY), TinkerFluids.moltenClay.get(), FluidValues.BRICK_BLOCK, 1.0f)
      .save(consumer, this.modResource(folder + "clay/block"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.CLAY_BALL), TinkerFluids.moltenClay.get(), FluidValues.BRICK, 0.5f)
      .save(consumer, this.modResource(folder + "clay/ball"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.FLOWER_POT), TinkerFluids.moltenClay.get(), FluidValues.BRICK * 3, 2.0f)
      .save(consumer, this.modResource(folder + "clay/pot"));
    this.tagMelting(consumer, TinkerFluids.moltenClay.get(), FluidValues.BRICK, "plates/brick", 1.0f, folder + "clay/plate", true);
    // terracotta
    Ingredient terracottaBlock = Ingredient.of(
      Blocks.TERRACOTTA, Blocks.BRICKS, Blocks.BRICK_WALL, Blocks.BRICK_STAIRS,
      Blocks.WHITE_TERRACOTTA, Blocks.ORANGE_TERRACOTTA, Blocks.MAGENTA_TERRACOTTA, Blocks.LIGHT_BLUE_TERRACOTTA,
      Blocks.YELLOW_TERRACOTTA, Blocks.LIME_TERRACOTTA, Blocks.PINK_TERRACOTTA, Blocks.GRAY_TERRACOTTA,
      Blocks.LIGHT_GRAY_TERRACOTTA, Blocks.CYAN_TERRACOTTA, Blocks.PURPLE_TERRACOTTA, Blocks.BLUE_TERRACOTTA,
      Blocks.BROWN_TERRACOTTA, Blocks.GREEN_TERRACOTTA, Blocks.RED_TERRACOTTA, Blocks.BLACK_TERRACOTTA,
      Blocks.WHITE_GLAZED_TERRACOTTA, Blocks.ORANGE_GLAZED_TERRACOTTA, Blocks.MAGENTA_GLAZED_TERRACOTTA, Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA,
      Blocks.YELLOW_GLAZED_TERRACOTTA, Blocks.LIME_GLAZED_TERRACOTTA, Blocks.PINK_GLAZED_TERRACOTTA, Blocks.GRAY_GLAZED_TERRACOTTA,
      Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA, Blocks.CYAN_GLAZED_TERRACOTTA, Blocks.PURPLE_GLAZED_TERRACOTTA, Blocks.BLUE_GLAZED_TERRACOTTA,
      Blocks.BROWN_GLAZED_TERRACOTTA, Blocks.GREEN_GLAZED_TERRACOTTA, Blocks.RED_GLAZED_TERRACOTTA, Blocks.BLACK_GLAZED_TERRACOTTA);
    MeltingRecipeBuilder.melting(terracottaBlock, TinkerFluids.moltenClay.get(), FluidValues.BRICK_BLOCK, 2.0f)
      .save(consumer, this.modResource(folder + "clay/terracotta"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.BRICK), TinkerFluids.moltenClay.get(), FluidValues.BRICK, 1.0f)
      .save(consumer, this.modResource(folder + "clay/brick"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.BRICK_SLAB),
        TinkerFluids.moltenClay.get(), FluidValues.BRICK_BLOCK / 2, 1.5f)
      .save(consumer, this.modResource(folder + "clay/brick_slab"));

    // slime
    String slimeFolder = folder + "slime/";
    this.slimeMelting(consumer, TinkerFluids.earthSlime, SlimeType.EARTH, slimeFolder);
    this.slimeMelting(consumer, TinkerFluids.skySlime, SlimeType.SKY, slimeFolder);
    this.slimeMelting(consumer, TinkerFluids.enderSlime, SlimeType.ENDER, slimeFolder);
    this.slimeMelting(consumer, TinkerFluids.blood, SlimeType.BLOOD, slimeFolder);
    this.slimeMelting(consumer, TinkerFluids.ichor, SlimeType.ICHOR, slimeFolder);
    // magma cream
    MeltingRecipeBuilder.melting(Ingredient.of(Items.MAGMA_CREAM), TinkerFluids.magma.get(), FluidValues.SLIMEBALL, 1.0f)
      .save(consumer, this.modResource(slimeFolder + "magma/ball"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.MAGMA_BLOCK), TinkerFluids.magma.get(), FluidValues.SLIME_CONGEALED, 3.0f)
      .save(consumer, this.modResource(slimeFolder + "magma/block"));

    // copper cans if empty
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.nbt(new ItemStack(TinkerSmeltery.copperCan), true), TinkerFluids.moltenCopper.get(), FluidValues.INGOT, 1.0f)
      .save(consumer, this.modResource(metalFolder + "copper/can"));
    // ender
    MeltingRecipeBuilder.melting(
        DefaultCustomIngredients.any(Ingredient.of(Tags.Items.ENDER_PEARLS), Ingredient.of(Items.ENDER_EYE)),
        TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL, 1.0f)
      .save(consumer, this.modResource(folder + "ender/pearl"));

    // obsidian
    MeltingRecipeBuilder.melting(Ingredient.of(Tags.Items.OBSIDIAN), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK, 2.0f)
      .save(consumer, this.modResource(folder + "obsidian/block"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.obsidianPane), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE, 1.5f)
      .save(consumer, this.modResource(folder + "obsidian/pane"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.ENDER_CHEST), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK * 8, 5.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL))
      .save(consumer, this.modResource(folder + "obsidian/chest"));
    this.tagMelting(consumer, TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE, "dusts/obsidian", 1.0f, folder + "obsidian/dust", true);

    // emerald
    this.gemMelting(consumer, TinkerFluids.moltenEmerald.get(), "emerald", "s", true, 9, folder, false, Byproduct.DIAMOND);
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.emeraldReinforcement), TinkerFluids.moltenEmerald.get(), FluidValues.GEM_SHARD)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "emerald/reinforcement"));

    // quartz
    this.gemMelting(consumer, TinkerFluids.moltenQuartz.get(), "quartz", "", true, 4, folder, false, Byproduct.AMETHYST);
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.SMOOTH_QUARTZ, Blocks.QUARTZ_PILLAR, Blocks.QUARTZ_BRICKS, Blocks.CHISELED_QUARTZ_BLOCK, Blocks.QUARTZ_STAIRS, Blocks.SMOOTH_QUARTZ_STAIRS),
        TinkerFluids.moltenQuartz.get(), FluidValues.SMALL_GEM_BLOCK, 2.0f)
      .save(consumer, this.modResource(folder + "quartz/decorative_block"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ_SLAB), TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 2, 1.5f)
      .save(consumer, this.modResource(folder + "quartz/slab"));

    // amethyst
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.AMETHYST_CLUSTER), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM * 4, 4.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 4))
      .setOre(OreRateType.GEM)
      .save(consumer, this.modResource(folder + "amethyst/cluster"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.SMALL_AMETHYST_BUD), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM, 1.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM))
      .setOre(OreRateType.GEM)
      .save(consumer, this.modResource(folder + "amethyst/bud_small"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.MEDIUM_AMETHYST_BUD), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM * 2, 2.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 2))
      .setOre(OreRateType.GEM)
      .save(consumer, this.modResource(folder + "amethyst/bud_medium"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.LARGE_AMETHYST_BUD), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM * 3, 3.0f)
      .addByproduct(new FluidStack(TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 3))
      .setOre(OreRateType.GEM)
      .save(consumer, this.modResource(folder + "amethyst/bud_large"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.AMETHYST_SHARD), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM, 1.0f)
      .save(consumer, this.modResource(folder + "amethyst/shard"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.AMETHYST_BLOCK), TinkerFluids.moltenAmethyst.get(), FluidValues.SMALL_GEM_BLOCK, 2.0f)
      .save(consumer, this.modResource(folder + "amethyst/block"));

    // diamond
    this.gemMelting(consumer, TinkerFluids.moltenDiamond.get(), "diamond", "s", true, 9, folder, false, Byproduct.QUARTZ);

    // iron melting - standard values
    MeltingRecipeBuilder.melting(Ingredient.of(Items.ACTIVATOR_RAIL, Items.DETECTOR_RAIL, Blocks.STONECUTTER, Blocks.PISTON, Blocks.STICKY_PISTON), TinkerFluids.moltenIron.get(), FluidValues.INGOT)
      .save(consumer, this.modResource(metalFolder + "iron/ingot_1"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.IRON_DOOR, Blocks.SMITHING_TABLE), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 2)
      .save(consumer, this.modResource(metalFolder + "iron/ingot_2"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.BUCKET), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 3)
      .save(consumer, this.modResource(metalFolder + "iron/bucket"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.COMPASS, Blocks.IRON_TRAPDOOR), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 4)
      .save(consumer, this.modResource(metalFolder + "iron/ingot_4"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.BLAST_FURNACE, Blocks.HOPPER, Items.MINECART), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 5)
      .save(consumer, this.modResource(metalFolder + "iron/ingot_5"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.CAULDRON), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 7)
      .save(consumer, this.modResource(metalFolder + "iron/cauldron"));
    // non-standard
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.CHAIN), TinkerFluids.moltenIron.get(), FluidValues.INGOT + FluidValues.NUGGET * 2)
      .save(consumer, this.modResource(metalFolder + "iron/chain"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 4 + FluidValues.METAL_BLOCK * 3)
      .save(consumer, this.modResource(metalFolder + "iron/anvil"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.IRON_BARS, Blocks.RAIL), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 3)
      .save(consumer, this.modResource(metalFolder + "iron/nugget_3"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.ironPlatform), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 10)
      .save(consumer, this.modResource(metalFolder + "iron/platform"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.TRIPWIRE_HOOK), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 4)
      .save(consumer, this.modResource(metalFolder + "iron/tripwire"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.LANTERN, Blocks.SOUL_LANTERN), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 8)
      .save(consumer, this.modResource(metalFolder + "iron/lantern"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.ironReinforcement), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "iron/reinforcement"));
    // armor
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_HELMET), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 5)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/helmet"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_CHESTPLATE), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 8)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/chestplate"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_LEGGINGS), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 7)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/leggings"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_BOOTS), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 4)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/boots"));
    // tools
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_AXE, Items.IRON_PICKAXE), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 3)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/axes"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_SWORD, Items.IRON_HOE, Items.SHEARS), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 2)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/weapon"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_SHOVEL, Items.FLINT_AND_STEEL, Items.SHIELD), TinkerFluids.moltenIron.get(), FluidValues.INGOT)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/small"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.CROSSBOW), TinkerFluids.moltenIron.get(), FluidValues.NUGGET * 13) // tripwire hook is 4 nuggets, ingot is 9 nuggets
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "iron/crossbow"));
    // unique melting
    MeltingRecipeBuilder.melting(Ingredient.of(Items.IRON_HORSE_ARMOR), TinkerFluids.moltenIron.get(), FluidValues.INGOT * 7)
      .save(consumer, this.modResource(metalFolder + "iron/horse_armor"));


    // gold melting
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerTags.Items.GOLD_CASTS), TinkerFluids.moltenGold.get(), FluidValues.INGOT)
      .save(consumer, this.modResource(metalFolder + "gold/cast"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.POWERED_RAIL), TinkerFluids.moltenGold.get(), FluidValues.INGOT)
      .save(consumer, this.modResource(metalFolder + "gold/powered_rail"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 2)
      .save(consumer, this.modResource(metalFolder + "gold/plate"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.CLOCK), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 4)
      .save(consumer, this.modResource(metalFolder + "gold/clock"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_APPLE), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 8)
      .save(consumer, this.modResource(metalFolder + "gold/apple"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GLISTERING_MELON_SLICE, Items.GOLDEN_CARROT), TinkerFluids.moltenGold.get(), FluidValues.NUGGET * 8)
      .save(consumer, this.modResource(metalFolder + "gold/produce"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.goldReinforcement), TinkerFluids.moltenGold.get(), FluidValues.NUGGET * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "gold/reinforcement"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.goldBars), TinkerFluids.moltenGold.get(), FluidValues.NUGGET * 3)
      .save(consumer, this.modResource(metalFolder + "gold/nugget_3"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.goldPlatform), TinkerFluids.moltenGold.get(), FluidValues.NUGGET * 10)
      .save(consumer, this.modResource(metalFolder + "gold/platform"));
    // armor
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_HELMET), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 5)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/helmet"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_CHESTPLATE), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 8)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/chestplate"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_LEGGINGS), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 7)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/leggings"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_BOOTS), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 4)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/boots"));
    // tools
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_AXE, Items.GOLDEN_PICKAXE), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 3)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/axes"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_SWORD, Items.GOLDEN_HOE), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 2)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/weapon"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_SHOVEL), TinkerFluids.moltenGold.get(), FluidValues.INGOT)
      .setDamagable(FluidValues.NUGGET)
      .save(consumer, this.modResource(metalFolder + "gold/shovel"));
    // unique melting
    MeltingRecipeBuilder.melting(Ingredient.of(Items.GOLDEN_HORSE_ARMOR), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 7)
      .save(consumer, this.modResource(metalFolder + "gold/horse_armor"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE), TinkerFluids.moltenGold.get(), FluidValues.METAL_BLOCK * 8)
      .save(consumer, this.modResource(metalFolder + "gold/enchanted_apple"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.GILDED_BLACKSTONE), TinkerFluids.moltenGold.get(), FluidValues.NUGGET * 6) // bit better than mining before ore bonus
      .setOre(OreRateType.METAL)
      .save(consumer, this.modResource(metalFolder + "gold/gilded_blackstone"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.BELL), TinkerFluids.moltenGold.get(), FluidValues.INGOT * 4) // bit arbitrary, I am happy to change the value if someone has a better one
      .save(consumer, this.modResource(metalFolder + "gold/bell"));


    // copper melting
    MeltingRecipeBuilder.melting(Ingredient.of(
          Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER,
          Blocks.WAXED_COPPER_BLOCK, Blocks.WAXED_EXPOSED_COPPER, Blocks.WAXED_WEATHERED_COPPER, Blocks.WAXED_OXIDIZED_COPPER),
        TinkerFluids.moltenCopper.get(), FluidValues.METAL_BLOCK)
      .save(consumer, this.modResource(metalFolder + "copper/decorative_block"));
    MeltingRecipeBuilder.melting(Ingredient.of(
          Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER,
          Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS,
          Blocks.WAXED_CUT_COPPER, Blocks.WAXED_EXPOSED_CUT_COPPER, Blocks.WAXED_WEATHERED_CUT_COPPER, Blocks.WAXED_OXIDIZED_CUT_COPPER,
          Blocks.WAXED_CUT_COPPER_STAIRS, Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS, Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS, Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS),
        TinkerFluids.moltenCopper.get(), FluidValues.NUGGET * 20)
      .save(consumer, this.modResource(metalFolder + "copper/cut_block"));
    MeltingRecipeBuilder.melting(Ingredient.of(
          Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB,
          Blocks.WAXED_CUT_COPPER_SLAB, Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB, Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB, Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB),
        TinkerFluids.moltenCopper.get(), FluidValues.NUGGET * 10)
      .save(consumer, this.modResource(metalFolder + "copper/cut_slab"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.LIGHTNING_ROD), TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 3)
      .save(consumer, this.modResource(metalFolder + "copper/lightning_rod"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerTags.Items.COPPER_PLATFORMS), TinkerFluids.moltenCopper.get(), FluidValues.NUGGET * 10)
      .save(consumer, this.modResource(metalFolder + "copper/platform"));

    // amethyst melting
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.TINTED_GLASS, TinkerCommons.clearTintedGlass), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM * 2)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK / 2))
      .save(consumer, this.modResource(folder + "amethyst/tinted_glass"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.SPYGLASS), TinkerFluids.moltenAmethyst.get(), FluidValues.GEM)
      .addByproduct(new FluidStack(TinkerFluids.moltenCopper.get(), FluidValues.INGOT * 2))
      .save(consumer, this.modResource(folder + "amethyst/spyglass"));

    // diamond melting
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.JUKEBOX), TinkerFluids.moltenDiamond.get(), FluidValues.GEM)
      .save(consumer, this.modResource(folder + "diamond/jukebox"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.ENCHANTING_TABLE), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 2)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK * 4))
      .save(consumer, this.modResource(folder + "diamond/enchanting_table"));
    // armor
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_HELMET), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 5)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/helmet"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_CHESTPLATE), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 8)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/chestplate"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_LEGGINGS), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 7)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/leggings"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_BOOTS), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 4)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/boots"));
    // tools
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 3)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/axes"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_SWORD, Items.DIAMOND_HOE), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 2)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/weapon"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_SHOVEL), TinkerFluids.moltenDiamond.get(), FluidValues.GEM)
      .setDamagable(FluidValues.GEM_SHARD)
      .save(consumer, this.modResource(folder + "diamond/shovel"));
    // unique melting
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DIAMOND_HORSE_ARMOR), TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 7)
      .save(consumer, this.modResource(folder + "diamond/horse_armor"));

    // netherite
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.LODESTONE), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .save(consumer, this.modResource(metalFolder + "netherite/lodestone"));
    // armor
    long[] netheriteSizes = {FluidValues.NUGGET, FluidValues.GEM_SHARD};
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_HELMET), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 5))
      .save(consumer, this.modResource(metalFolder + "netherite/helmet"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_CHESTPLATE), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 8))
      .save(consumer, this.modResource(metalFolder + "netherite/chestplate"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_LEGGINGS), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 7))
      .save(consumer, this.modResource(metalFolder + "netherite/leggings"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_BOOTS), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 4))
      .save(consumer, this.modResource(metalFolder + "netherite/boots"));
    // tools
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_AXE, Items.NETHERITE_PICKAXE), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 3))
      .save(consumer, this.modResource(metalFolder + "netherite/axes"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_SWORD, Items.NETHERITE_HOE), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM * 2))
      .save(consumer, this.modResource(metalFolder + "netherite/weapon"));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.NETHERITE_SHOVEL), TinkerFluids.moltenNetherite.get(), FluidValues.INGOT)
      .setDamagable(netheriteSizes)
      .addByproduct(new FluidStack(TinkerFluids.moltenDiamond.get(), FluidValues.GEM))
      .save(consumer, this.modResource(metalFolder + "netherite/shovel"));

    // quartz
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.OBSERVER, Blocks.COMPARATOR, TinkerGadgets.quartzShuriken), TinkerFluids.moltenQuartz.get(), FluidValues.GEM)
      .save(consumer, this.modResource(folder + "quartz/gem_1"));
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.DAYLIGHT_DETECTOR), TinkerFluids.moltenQuartz.get(), FluidValues.GEM * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK * 3))
      .save(consumer, this.modResource(folder + "quartz/daylight_detector"));

    // obsidian, if you are crazy i guess
    MeltingRecipeBuilder.melting(Ingredient.of(Blocks.BEACON), TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK * 5))
      .save(consumer, this.modResource(folder + "obsidian/beacon"));

    // ender
    MeltingRecipeBuilder.melting(Ingredient.of(Items.END_CRYSTAL), TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK * 7))
      .save(consumer, this.modResource(folder + "ender/end_crystal"));
    // it may be silky, but its still rose gold
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.silkyCloth), TinkerFluids.moltenRoseGold.get(), FluidValues.INGOT)
      .save(consumer, this.modResource(metalFolder + "rose_gold/silky_cloth"));

    // misc reinforcements
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.slimesteelReinforcement), TinkerFluids.moltenSlimesteel.get(), FluidValues.NUGGET * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "slimesteel/reinforcement"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.bronzeReinforcement), TinkerFluids.moltenAmethystBronze.get(), FluidValues.NUGGET * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "amethyst_bronze/reinforcement"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerModifiers.cobaltReinforcement), TinkerFluids.moltenCobalt.get(), FluidValues.NUGGET * 3)
      .addByproduct(new FluidStack(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_PANE))
      .save(consumer, this.modResource(metalFolder + "cobalt/reinforcement"));

    MeltingRecipeBuilder.melting(Ingredient.of(TinkerCommons.cobaltPlatform), TinkerFluids.moltenCobalt.get(), FluidValues.NUGGET * 10)
      .save(consumer, this.modResource(metalFolder + "cobalt/platform"));

    // slime
    TinkerGadgets.slimeSling.forEach((type, sling) -> {
      if (type != SlimeType.ICHOR) { // no ichor fluid
        MeltingRecipeBuilder.melting(Ingredient.of(sling), TinkerFluids.slime.get(type).get(), FluidValues.SLIMEBALL * 3 + FluidValues.SLIME_CONGEALED)
          .setDamagable(FluidValues.SLIMEBALL / 5)
          .save(consumer, this.modResource(slimeFolder + type.getSerializedName() + "/sling"));
      }
    });

    // geode stuff
    this.crystalMelting(consumer, TinkerWorld.earthGeode, TinkerFluids.earthSlime.get(), slimeFolder + "earth/");
    this.crystalMelting(consumer, TinkerWorld.skyGeode, TinkerFluids.skySlime.get(), slimeFolder + "sky/");
    this.crystalMelting(consumer, TinkerWorld.enderGeode, TinkerFluids.enderSlime.get(), slimeFolder + "ender/");

    // recycle saplings
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.slimeSapling.get(SlimeType.EARTH)), TinkerFluids.earthSlime.get(), FluidValues.SLIMEBALL)
      .save(consumer, this.modResource(slimeFolder + "earth/sapling"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.slimeSapling.get(SlimeType.SKY)), TinkerFluids.skySlime.get(), FluidValues.SLIMEBALL)
      .save(consumer, this.modResource(slimeFolder + "sky/sapling"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.slimeSapling.get(SlimeType.ENDER)), TinkerFluids.enderSlime.get(), FluidValues.SLIMEBALL)
      .save(consumer, this.modResource(slimeFolder + "ender/sapling"));

    // honey
    MeltingRecipeBuilder.melting(Ingredient.of(Items.HONEY_BLOCK), TinkerFluids.honey.get(), FluidValues.BOTTLE * 4)
      .save(consumer, this.modResource(slimeFolder + "honey_block"));
    // soup
    MeltingRecipeBuilder.melting(Ingredient.of(Items.BEETROOT), TinkerFluids.beetrootSoup.get(), FluidValues.BOTTLE / 5, 1)
      .save(consumer, this.modResource(slimeFolder + "beetroot_soup"));
    MeltingRecipeBuilder.melting(Ingredient.of(Tags.Items.MUSHROOMS), TinkerFluids.mushroomStew.get(), FluidValues.BOTTLE / 2, 1)
      .save(consumer, this.modResource(slimeFolder + "mushroom_stew"));

    // fuels
    MeltingFuelBuilder.fuel(new FluidStack(Fluids.LAVA, 50), 100)
      .save(consumer, this.modResource(folder + "fuel/lava"));
    MeltingFuelBuilder.fuel(new FluidStack(TinkerFluids.blazingBlood.get(), 50), 150)
      .save(consumer, this.modResource(folder + "fuel/blaze"));
  }


  private void addAlloyRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "smeltery/alloys/";

    // alloy recipes are in terms of ingots

    // tier 3

    // slimesteel: 1 iron + 1 skyslime + 1 seared brick = 2
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenSlimesteel.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenIron.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.skySlime.getLocalTag(), FluidValues.SLIMEBALL)
      .addInput(TinkerFluids.searedStone.getLocalTag(), FluidValues.BRICK)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenSlimesteel.get()), folder));

    // amethyst bronze: 1 copper + 1 amethyst = 1
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenAmethystBronze.get(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenAmethyst.getLocalTag(), FluidValues.GEM)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenAmethystBronze.get()), folder));

    // rose gold: 1 copper + 1 gold = 2
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenRoseGold.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenGold.getForgeTag(), FluidValues.INGOT)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenRoseGold.get()), folder));
    // pig iron: 1 iron + 2 blood + 1 honey = 2
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenPigIron.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenIron.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.blood.getLocalTag(), FluidValues.SLIMEBALL * 2)
      .addInput(TinkerFluids.honey.getForgeTag(), FluidValues.BOTTLE)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenPigIron.get()), folder));
    // obsidian: 1 water + 1 lava = 2
    // note this is not a progression break, as the same tier lets you combine glass and copper for same mining level
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK / 10)
      .addInput(Fluids.WATER, FluidConstants.BUCKET / 20)
      .addInput(Fluids.LAVA, FluidConstants.BUCKET / 10)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenObsidian.get()), folder));
    // nether obsidian recipe: when water is rare, use the soup
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenObsidian.get(), FluidValues.GLASS_BLOCK / 4)
      .addInput(TinkerFluids.mushroomStew.getForgeTag(), FluidValues.BOWL)
      .addInput(Fluids.LAVA, FluidConstants.BUCKET / 4)
      .save(consumer, this.wrap(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenObsidian.get()), folder, "_from_soup"));

    // tier 4

    // queens slime: 1 cobalt + 1 gold + 1 magma cream = 2
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenQueensSlime.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCobalt.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenGold.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.magma.getForgeTag(), FluidValues.SLIMEBALL)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenQueensSlime.get()), folder));

    // manyullyn: 3 cobalt + 1 debris = 3
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenManyullyn.get(), FluidValues.INGOT * 4)
      .addInput(TinkerFluids.moltenCobalt.getForgeTag(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenDebris.getLocalTag(), FluidValues.INGOT)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenManyullyn.get()), folder));

    // heptazion: 2 copper + 1 cobalt + 1/4 obsidian = 2
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenHepatizon.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCobalt.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenQuartz.getLocalTag(), FluidValues.GEM * 4)
      .save(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenHepatizon.get()), folder));

    // netherrite: 4 debris + 4 gold = 1 (why is this so dense vanilla?)
    ConditionalRecipe.builder()
      .addCondition(ConfigEnabledCondition.CHEAPER_NETHERITE_ALLOY)
      .addRecipe(
        AlloyRecipeBuilder.alloy(TinkerFluids.moltenNetherite.get(), FluidValues.NUGGET)
          .addInput(TinkerFluids.moltenDebris.getLocalTag(), FluidValues.NUGGET * 4)
          .addInput(TinkerFluids.moltenGold.getForgeTag(), FluidValues.NUGGET * 2)::save)
      .addCondition(TrueCondition.INSTANCE) // fallback
      .addRecipe(
        AlloyRecipeBuilder.alloy(TinkerFluids.moltenNetherite.get(), FluidValues.NUGGET)
          .addInput(TinkerFluids.moltenDebris.getLocalTag(), FluidValues.NUGGET * 4)
          .addInput(TinkerFluids.moltenGold.getForgeTag(), FluidValues.NUGGET * 4)::save)
      .build(consumer, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenNetherite.get()), folder));


    // tier 3 compat
    Consumer<FinishedRecipe> wrapped;

    // bronze
    wrapped = this.withCondition(consumer, this.tagCondition("bronze_ingots"), this.tagCondition("tin_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenBronze.get(), FluidValues.INGOT * 4)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenTin.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenBronze.get()), folder));

    // brass
    wrapped = this.withCondition(consumer, this.tagCondition("brass_ingots"), this.tagCondition("zinc_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenBrass.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenZinc.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenBrass.get()), folder));

    // electrum
    wrapped = this.withCondition(consumer, this.tagCondition("electrum_ingots"), this.tagCondition("silver_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenElectrum.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenGold.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenSilver.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenElectrum.get()), folder));

    // invar
    wrapped = this.withCondition(consumer, this.tagCondition("invar_ingots"), this.tagCondition("nickel_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenInvar.get(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenIron.getForgeTag(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenNickel.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenInvar.get()), folder));

    // constantan
    wrapped = this.withCondition(consumer, this.tagCondition("constantan_ingots"), this.tagCondition("nickel_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenConstantan.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenNickel.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenConstantan.get()), folder));

    // pewter
    wrapped = this.withCondition(consumer, this.tagCondition("pewter_ingots"), this.tagCondition("lead_ingots"));
    ConditionalRecipe.builder()
      // when available, alloy pewter with tin
      // we mainly add it to support Edilon which uses iron to reduce ores, but the author thinks tin is fine balance wise
      .addCondition(this.tagCondition("tin_ingots"))
      .addRecipe(
        // ratio from Allomancy mod
        AlloyRecipeBuilder.alloy(TinkerFluids.moltenPewter.get(), FluidValues.INGOT * 3)
          .addInput(TinkerFluids.moltenTin.getForgeTag(), FluidValues.INGOT * 2)
          .addInput(TinkerFluids.moltenLead.getForgeTag(), FluidValues.INGOT)::save)
      .addCondition(TrueCondition.INSTANCE) // fallback
      .addRecipe(
        // ratio from Edilon mod
        AlloyRecipeBuilder.alloy(TinkerFluids.moltenPewter.get(), FluidValues.INGOT * 2)
          .addInput(TinkerFluids.moltenIron.getForgeTag(), FluidValues.INGOT)
          .addInput(TinkerFluids.moltenLead.getForgeTag(), FluidValues.INGOT)::save)
      .build(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenPewter.get()), folder));

    // thermal alloys
    Function<String, ConditionJsonProvider> fluidTagLoaded = name -> DefaultResourceConditions.fluidTagsPopulated(TagKey.create(Registries.FLUID, new ResourceLocation("c", name)));
    Function<String, TagKey<Fluid>> fluidTag = name -> TagKey.create(Registries.FLUID, new ResourceLocation("c", name));
    // enderium
    wrapped = this.withCondition(consumer, this.tagCondition("enderium_ingots"), this.tagCondition("lead_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenEnderium.get(), FluidValues.INGOT * 2)
      .addInput(TinkerFluids.moltenLead.getForgeTag(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenDiamond.getLocalTag(), FluidValues.GEM)
      .addInput(TinkerFluids.moltenEnder.getForgeTag(), FluidValues.SLIMEBALL * 2)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenEnderium.get()), folder));
    // lumium
    wrapped = this.withCondition(consumer, this.tagCondition("lumium_ingots"), this.tagCondition("tin_ingots"), this.tagCondition("silver_ingots"), fluidTagLoaded.apply("glowstone"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenLumium.get(), FluidValues.INGOT * 4)
      .addInput(TinkerFluids.moltenTin.getForgeTag(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenSilver.getForgeTag(), FluidValues.INGOT)
      .addInput(FluidIngredient.of(fluidTag.apply("glowstone"), FluidValues.SLIMEBALL * 2))
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenLumium.get()), folder));
    // signalum
    wrapped = this.withCondition(consumer, this.tagCondition("signalum_ingots"), this.tagCondition("copper_ingots"), this.tagCondition("silver_ingots"), fluidTagLoaded.apply("redstone"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenSignalum.get(), FluidValues.INGOT * 4)
      .addInput(TinkerFluids.moltenCopper.getForgeTag(), FluidValues.INGOT * 3)
      .addInput(TinkerFluids.moltenSilver.getForgeTag(), FluidValues.INGOT)
      .addInput(FluidIngredient.of(fluidTag.apply("redstone"), 400))
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenSignalum.get()), folder));

    // refined obsidian, note glowstone is done as a composite
    wrapped = this.withCondition(consumer, this.tagCondition("refined_obsidian_ingots"), this.tagCondition("osmium_ingots"));
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenRefinedObsidian.get(), FluidValues.INGOT)
      .addInput(TinkerFluids.moltenObsidian.getLocalTag(), FluidValues.GLASS_PANE)
      .addInput(TinkerFluids.moltenDiamond.getLocalTag(), FluidValues.GEM)
      .addInput(TinkerFluids.moltenOsmium.getForgeTag(), FluidValues.INGOT)
      .save(wrapped, this.prefix(BuiltInRegistries.FLUID.getKey(TinkerFluids.moltenRefinedObsidian.get()), folder));
  }

  private void addEntityMeltingRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "smeltery/entity_melting/";
    String headFolder = "smeltery/entity_melting/heads/";

    // zombies give less blood, they lost a lot already
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.ZOMBIE, EntityType.HUSK, EntityType.ZOMBIFIED_PIGLIN, EntityType.ZOGLIN, EntityType.ZOMBIE_HORSE),
        new FluidStack(TinkerFluids.blood.get(), FluidValues.SLIMEBALL / 10), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE), folder));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.ZOMBIE_HEAD, TinkerWorld.heads.get(TinkerHeadType.HUSK), Blocks.PIGLIN_HEAD, TinkerWorld.heads.get(TinkerHeadType.PIGLIN_BRUTE), TinkerWorld.heads.get(TinkerHeadType.ZOMBIFIED_PIGLIN)), TinkerFluids.blood.get(), FluidValues.SLIMEBALL * 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ZOMBIE), headFolder));
    // drowned are weird, there is water flowing through their veins
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.DROWNED),
        new FluidStack(Fluids.WATER, FluidConstants.BUCKET / 50), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.DROWNED), folder));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.heads.get(TinkerHeadType.DROWNED)), Fluids.WATER, FluidConstants.BUCKET / 4)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.DROWNED), headFolder));
    // melt spiders into venom
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.SPIDER, EntityType.CAVE_SPIDER),
        new FluidStack(TinkerFluids.venom.get(), FluidValues.BOTTLE / 10), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SPIDER), folder));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.heads.get(TinkerHeadType.SPIDER), TinkerWorld.heads.get(TinkerHeadType.CAVE_SPIDER)), TinkerFluids.venom.get(), FluidValues.SLIMEBALL * 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SPIDER), headFolder));

    // creepers are based on explosives, tnt is explosive, tnt is made from sand, sand melts into glass. therefore, creepers melt into glass
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.CREEPER),
        new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_BLOCK / 20), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CREEPER), folder));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.CREEPER_HEAD), TinkerFluids.moltenGlass.get(), FluidConstants.BUCKET / 4)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.CREEPER), headFolder));

    // melt skeletons to get the milk out
    //TODO: Remove
//    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityIngredient.of(EntityTypeTags.SKELETONS), EntityIngredient.of(EntityType.SKELETON_HORSE)),
//                                       new FluidStack(Milk.STILL_MILK, FluidConstants.BUCKET / 10))
//                              .save(consumer, modResource(folder + "skeletons"));
//    MeltingRecipeBuilder.melting(Ingredient.of(Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, TinkerWorld.heads.get(TinkerHeadType.STRAY)), Milk.STILL_MILK, FluidConstants.BUCKET / 4)
//                        .save(consumer, prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SKELETON), headFolder));

    // slimes melt into slime, shocker
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.SLIME, TinkerWorld.earthSlimeEntity.get()), new FluidStack(TinkerFluids.earthSlime.get(), FluidValues.SLIMEBALL / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SLIME), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(TinkerWorld.skySlimeEntity.get()), new FluidStack(TinkerFluids.skySlime.get(), FluidValues.SLIMEBALL / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(TinkerWorld.skySlimeEntity.get()), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(TinkerWorld.enderSlimeEntity.get()), new FluidStack(TinkerFluids.enderSlime.get(), FluidValues.SLIMEBALL / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(TinkerWorld.enderSlimeEntity.get()), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(TinkerWorld.terracubeEntity.get()), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(TinkerWorld.terracubeEntity.get()), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.MAGMA_CUBE), new FluidStack(TinkerFluids.magma.get(), FluidValues.SLIMEBALL / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.MAGMA_CUBE), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.BEE), new FluidStack(TinkerFluids.honey.get(), FluidValues.BOTTLE / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BEE), folder));

    // iron golems can be healed using an iron ingot 25 health
    // 4 * 9 gives 36, which is larger
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.IRON_GOLEM), new FluidStack(TinkerFluids.moltenIron.get(), FluidValues.NUGGET), 4)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.IRON_GOLEM), folder));
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.SNOW_GOLEM), new FluidStack(Fluids.WATER, FluidConstants.BUCKET / 10))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SNOW_GOLEM), folder));

    // "melt" blazes to get fuel
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.BLAZE), new FluidStack(TinkerFluids.blazingBlood.get(), FluidConstants.BUCKET / 50), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BLAZE), folder));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.heads.get(TinkerHeadType.BLAZE)), new FluidStack(TinkerFluids.blazingBlood.get(), FluidConstants.BUCKET / 10), 1000, IMeltingRecipe.calcTime(1500, 1.0f))
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.BLAZE), headFolder));

    // guardians are rock, seared stone is rock, don't think about it too hard
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN), new FluidStack(TinkerFluids.searedStone.get(), FluidValues.BRICK / 5), 4)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.GUARDIAN), folder));
    // silverfish also seem like rock, sorta?
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.SILVERFISH), new FluidStack(TinkerFluids.searedStone.get(), FluidValues.BRICK / 5), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.SILVERFISH), folder));

    // villagers melt into emerald, but they die quite quick
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(TinkerTags.EntityTypes.VILLAGERS),
        new FluidStack(TinkerFluids.moltenEmerald.get(), FluidValues.GEM_SHARD), 5)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.VILLAGER), folder));
    // illagers are more resistant, they resist the villager culture afterall
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(TinkerTags.EntityTypes.ILLAGERS),
        new FluidStack(TinkerFluids.moltenEmerald.get(), FluidValues.GEM_SHARD), 2)
      .save(consumer, this.modResource(folder + "illager"));

    // melt ender for the molten ender
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.ENDERMAN, EntityType.ENDERMITE, EntityType.ENDER_DRAGON),
        new FluidStack(TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL / 10), 2)
      .save(consumer, this.modResource(folder + "ender"));
    MeltingRecipeBuilder.melting(Ingredient.of(TinkerWorld.heads.get(TinkerHeadType.ENDERMAN)), TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL * 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDERMAN), headFolder));
    MeltingRecipeBuilder.melting(Ingredient.of(Items.DRAGON_HEAD), TinkerFluids.moltenEnder.get(), FluidValues.SLIMEBALL * 4)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ENDER_DRAGON), headFolder));

    // if you can get him to stay, wither is a source of free liquid soul
    EntityMeltingRecipeBuilder.melting(EntityIngredient.of(EntityType.WITHER),
        new FluidStack(TinkerFluids.liquidSoul.get(), FluidValues.GLASS_BLOCK / 20), 2)
      .save(consumer, this.prefix(BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.WITHER), folder));
  }

  private void addCompatRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "compat/";
    // create - cast andesite alloy
    ItemOutput andesiteAlloy = ItemNameOutput.fromName(new ResourceLocation("create", "andesite_alloy"));
    Consumer<FinishedRecipe> createConsumer = this.withCondition(consumer, DefaultResourceConditions.allModsLoaded("create"));
    ItemCastingRecipeBuilder.basinRecipe(andesiteAlloy)
      .setCast(Blocks.ANDESITE, true)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.NUGGET)
      .save(createConsumer, this.modResource(folder + "create/andesite_alloy_iron"));
    ItemCastingRecipeBuilder.basinRecipe(andesiteAlloy)
      .setCast(Blocks.ANDESITE, true)
      .setFluidAndTime(TinkerFluids.moltenZinc, true, FluidValues.NUGGET)
      .save(createConsumer, this.modResource(folder + "create/andesite_alloy_zinc"));

    // immersive engineering - casting treated wood
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(new ResourceLocation("immersiveengineering", "treated_wood_horizontal")))
      .setCast(ItemTags.PLANKS, true)
      .setFluid(TagKey.create(Registries.FLUID, new ResourceLocation("c", "creosote")), 125)
      .setCoolingTime(100)
      .save(this.withCondition(consumer, DefaultResourceConditions.allModsLoaded("immersiveengineering")), this.modResource(folder + "immersiveengineering/treated_wood"));

    // ceramics compat: a lot of melting and some casting
    String ceramics = "ceramics";
    String ceramicsFolder = folder + ceramics + "/";
    Function<String, ResourceLocation> ceramicsId = name -> new ResourceLocation(ceramics, name);
    Function<String, TagKey<Item>> ceramicsTag = name -> TagKey.create(Registries.ITEM, new ResourceLocation(ceramics, name));
    Consumer<FinishedRecipe> ceramicsConsumer = this.withCondition(consumer, DefaultResourceConditions.allModsLoaded(ceramics));

    // fill clay and cracked clay buckets
    ContainerFillingRecipeBuilder.tableRecipe(ceramicsId.apply("clay_bucket"), FluidConstants.BUCKET)
      .save(ceramicsConsumer, this.modResource(ceramicsFolder + "filling_clay_bucket"));
    ContainerFillingRecipeBuilder.tableRecipe(ceramicsId.apply("cracked_clay_bucket"), FluidConstants.BUCKET)
      .save(ceramicsConsumer, this.modResource(ceramicsFolder + "filling_cracked_clay_bucket"));

    // porcelain for ceramics
    AlloyRecipeBuilder.alloy(TinkerFluids.moltenPorcelain.get(), FluidValues.BRICK * 4)
      .addInput(TinkerFluids.moltenClay.getLocalTag(), FluidValues.BRICK * 3)
      .addInput(TinkerFluids.moltenQuartz.getLocalTag(), FluidValues.GEM)
      .save(ceramicsConsumer, this.modResource(ceramicsFolder + "alloy_porcelain"));

    // melting clay
    String clayFolder = ceramicsFolder + "clay/";

    // unfired clay
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_clay_plate")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL), 0.5f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_1"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("clay_faucet"), ceramicsId.apply("clay_channel")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 2), 0.65f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_2"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_clay_bucket"), ceramicsId.apply("clay_cistern")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 3), 0.9f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_3"));

    // 2 bricks
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(
        ceramicsId.apply("dark_bricks_slab"), ceramicsId.apply("dragon_bricks_slab"),
        ceramicsId.apply("terracotta_faucet"), ceramicsId.apply("terracotta_channel")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 2), 1.33f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "bricks_2"));
    // 3 bricks
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(
          Ingredient.of(ceramicsTag.apply("terracotta_cisterns")),
          NBTNameIngredient.from(ceramicsId.apply("clay_bucket")),
          NBTNameIngredient.from(ceramicsId.apply("cracked_clay_bucket"))),
        new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 3), 1.67f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "bricks_3"));
    // 4 bricks
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(
        ceramicsId.apply("dark_bricks"), ceramicsId.apply("dark_bricks_stairs"), ceramicsId.apply("dark_bricks_wall"),
        ceramicsId.apply("dragon_bricks"), ceramicsId.apply("dragon_bricks_stairs"), ceramicsId.apply("dragon_bricks_wall")
      ), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 4), 2.0f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "block"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("kiln")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIME_CONGEALED * 3 + FluidValues.SLIMEBALL * 5), 4.0f)
      .save(ceramicsConsumer, this.modResource(clayFolder + "kiln"));
    // lava bricks, lava byproduct
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("lava_bricks_slab")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 2), 1.33f)
      .addByproduct(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 20))
      .save(ceramicsConsumer, this.modResource(clayFolder + "lava_bricks_slab"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(
        ceramicsId.apply("lava_bricks"), ceramicsId.apply("lava_bricks_stairs"), ceramicsId.apply("lava_bricks_wall")
      ), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 4), 2f)
      .addByproduct(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 10))
      .save(ceramicsConsumer, this.modResource(clayFolder + "lava_bricks_block"));
    // gauge, partially glass
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("terracotta_gauge")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL), 1f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE / 4))
      .save(ceramicsConsumer, this.modResource(clayFolder + "gauge"));
    // clay armor
    int slimeballPart = FluidValues.SLIMEBALL / 5;
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("clay_helmet")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 5), 2.25f)
      .setDamagable(slimeballPart)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_helmet"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("clay_chestplate")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 8), 3f)
      .setDamagable(slimeballPart)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_chestplate"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("clay_leggings")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 7), 2.75f)
      .setDamagable(slimeballPart)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_leggings"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("clay_boots")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 4), 2f)
      .setDamagable(slimeballPart)
      .save(ceramicsConsumer, this.modResource(clayFolder + "clay_boots"));

    // melting porcelain
    String porcelainFolder = ceramicsFolder + "porcelain/";
    // unfired
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_porcelain")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL), 0.5f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "unfired_1"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_faucet"), ceramicsId.apply("unfired_channel")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 2), 0.65f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "unfired_2"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_cistern")), new FluidStack(TinkerFluids.moltenClay.get(), FluidValues.SLIMEBALL * 3), 0.9f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "unfired_3"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("unfired_porcelain_block")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIME_CONGEALED), 1f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "unfired_4"));

    // 1 brick
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("porcelain_brick")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL), 1f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "bricks_1"));
    // 2 bricks
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(
        ceramicsId.apply("porcelain_bricks_slab"), ceramicsId.apply("monochrome_bricks_slab"), ceramicsId.apply("marine_bricks_slab"), ceramicsId.apply("rainbow_bricks_slab"),
        ceramicsId.apply("porcelain_faucet"), ceramicsId.apply("porcelain_channel")
      ), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL * 2), 1.33f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "bricks_2"));
    // 3 bricks
    MeltingRecipeBuilder.melting(Ingredient.of(ceramicsTag.apply("porcelain_cisterns")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL * 3), 1.67f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "bricks_3"));
    // 4 bricks
    MeltingRecipeBuilder.melting(DefaultCustomIngredients.any(
        Ingredient.of(ceramicsTag.apply("porcelain_block")),
        Ingredient.of(ceramicsTag.apply("rainbow_porcelain")),
        ItemNameIngredient.from(
          ceramicsId.apply("porcelain_bricks"), ceramicsId.apply("porcelain_bricks_stairs"), ceramicsId.apply("porcelain_bricks_wall"),
          ceramicsId.apply("monochrome_bricks"), ceramicsId.apply("monochrome_bricks_stairs"), ceramicsId.apply("monochrome_bricks_wall"),
          ceramicsId.apply("marine_bricks"), ceramicsId.apply("marine_bricks_stairs"), ceramicsId.apply("marine_bricks_wall"),
          ceramicsId.apply("rainbow_bricks"), ceramicsId.apply("rainbow_bricks_stairs"), ceramicsId.apply("rainbow_bricks_wall")
        )), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL * 4), 2.0f)
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "blocks"));
    // gold bricks, gold byproduct
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("golden_bricks_slab")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL * 2), 1.33f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGold.get(), FluidValues.NUGGET / 16)) // yep, exactly 1mb, such recycling
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "golden_bricks_slab"));
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(
        ceramicsId.apply("golden_bricks"), ceramicsId.apply("golden_bricks_stairs"), ceramicsId.apply("golden_bricks_wall")
      ), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL * 4), 2f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGold.get(), FluidValues.NUGGET / 8)) // 2mb is slightly better, but still not great
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "golden_bricks_block"));
    // gauge, partially glass
    MeltingRecipeBuilder.melting(ItemNameIngredient.from(ceramicsId.apply("porcelain_gauge")), new FluidStack(TinkerFluids.moltenPorcelain.get(), FluidValues.SLIMEBALL), 1f)
      .addByproduct(new FluidStack(TinkerFluids.moltenGlass.get(), FluidValues.GLASS_PANE / 4))
      .save(ceramicsConsumer, this.modResource(porcelainFolder + "gauge"));

    // casting bricks
    String castingFolder = ceramicsFolder + "casting/";
    this.castingWithCast(ceramicsConsumer, TinkerFluids.moltenPorcelain, FluidValues.SLIMEBALL, TinkerSmeltery.ingotCast, ItemNameOutput.fromName(ceramicsId.apply("porcelain_brick")), castingFolder + "porcelain_brick");
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("white_porcelain")))
      .setFluidAndTime(TinkerFluids.moltenPorcelain, false, FluidValues.SLIME_CONGEALED)
      .save(ceramicsConsumer, this.modResource(castingFolder + "porcelain"));
    // lava bricks
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("lava_bricks")))
      .setCast(Blocks.BRICKS, true)
      .setFluidAndTime(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 10))
      .save(ceramicsConsumer, this.modResource(castingFolder + "lava_bricks"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("lava_bricks_slab")))
      .setCast(Blocks.BRICK_SLAB, true)
      .setFluidAndTime(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 20))
      .save(ceramicsConsumer, this.modResource(castingFolder + "lava_bricks_slab"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("lava_bricks_stairs")))
      .setCast(Blocks.BRICK_STAIRS, true)
      .setFluidAndTime(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 10))
      .save(ceramicsConsumer, this.modResource(castingFolder + "lava_bricks_stairs"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("lava_bricks_wall")))
      .setCast(Blocks.BRICK_WALL, true)
      .setFluidAndTime(new FluidStack(Fluids.LAVA, FluidConstants.BUCKET / 10))
      .save(ceramicsConsumer, this.modResource(castingFolder + "lava_bricks_wall"));

    // golden bricks
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("golden_bricks")))
      .setCast(ItemNameIngredient.from(ceramicsId.apply("porcelain_bricks")), true)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET / 8)
      .save(ceramicsConsumer, this.modResource(castingFolder + "golden_bricks"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("golden_bricks_slab")))
      .setCast(ItemNameIngredient.from(ceramicsId.apply("porcelain_bricks_slab")), true)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET / 16)
      .save(ceramicsConsumer, this.modResource(castingFolder + "golden_bricks_slab"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("golden_bricks_stairs")))
      .setCast(ItemNameIngredient.from(ceramicsId.apply("porcelain_bricks_stairs")), true)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET / 8)
      .save(ceramicsConsumer, this.modResource(castingFolder + "golden_bricks_stairs"));
    ItemCastingRecipeBuilder.basinRecipe(ItemNameOutput.fromName(ceramicsId.apply("golden_bricks_wall")))
      .setCast(ItemNameIngredient.from(ceramicsId.apply("porcelain_bricks_wall")), true)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET / 8)
      .save(ceramicsConsumer, this.modResource(castingFolder + "golden_bricks_wall"));

    // refined glowstone composite
    Consumer<FinishedRecipe> wrapped = this.withCondition(consumer, this.tagCondition("refined_glowstone_ingots"), this.tagCondition("osmium_ingots"));
    ItemCastingRecipeBuilder.tableRecipe(ItemOutput.fromTag(this.getItemTag("c", "refined_glowstone_ingots"), 1))
      .setCast(Tags.Items.DUSTS_GLOWSTONE, true)
      .setFluidAndTime(TinkerFluids.moltenOsmium, FluidValues.INGOT)
      .save(wrapped, this.modResource(folder + "refined_glowstone_ingot"));
    wrapped = this.withCondition(consumer, this.tagCondition("refined_obsidian_ingots"), this.tagCondition("osmium_ingots"));
    ItemCastingRecipeBuilder.tableRecipe(ItemOutput.fromTag(this.getItemTag("c", "refined_obsidian_ingots"), 1))
      .setCast(this.getItemTag("c", "refined_obsidian_dusts"), true)
      .setFluidAndTime(TinkerFluids.moltenOsmium, FluidValues.INGOT)
      .save(wrapped, this.modResource(folder + "refined_obsidian_ingot"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerMaterials.necroniumBone)
      .setFluidAndTime(TinkerFluids.moltenUranium, true, FluidValues.INGOT)
      .setCast(TinkerTags.Items.WITHER_BONES, true)
      .save(this.withCondition(consumer, this.tagCondition("uranium_ingots")), this.modResource(folder + "necronium_bone"));
  }


  /* Seared casting */

  /**
   * Adds a stonecutting recipe with automatic name and criteria
   *
   * @param consumer Recipe consumer
   * @param output   Recipe output
   * @param folder   Recipe folder path
   */
  private void searedStonecutter(Consumer<FinishedRecipe> consumer, ItemLike output, String folder) {
    SingleItemRecipeBuilder.stonecutting(
        DefaultCustomIngredients.any(
          Ingredient.of(TinkerSmeltery.searedStone),
          DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.SEARED_BRICKS), Ingredient.of(output))), RecipeCategory.MISC, output, 1)
      .unlockedBy("has_stone", has(TinkerSmeltery.searedStone))
      .unlockedBy("has_bricks", has(TinkerTags.Items.SEARED_BRICKS))
      .save(consumer, this.wrap(output.asItem(), folder, "_stonecutting"));
  }

  /**
   * Adds a recipe to create the given seared block using molten clay on stone
   *
   * @param consumer Recipe consumer
   * @param block    Output block
   * @param cast     Cast item
   * @param location Recipe location
   */
  private void searedCasting(Consumer<FinishedRecipe> consumer, ItemLike block, Ingredient cast, String location) {
    this.searedCasting(consumer, block, cast, FluidValues.SLIMEBALL * 2, location);
  }

  /**
   * Adds a recipe to create the given seared slab block using molten clay on stone
   *
   * @param consumer Recipe consumer
   * @param block    Output block
   * @param cast     Cast item
   * @param location Recipe location
   */
  private void searedSlabCasting(Consumer<FinishedRecipe> consumer, ItemLike block, Ingredient cast, String location) {
    this.searedCasting(consumer, block, cast, FluidValues.SLIMEBALL, location);
  }

  /**
   * Adds a recipe to create the given seared block using molten clay on stone
   *
   * @param consumer Recipe consumer
   * @param block    Output block
   * @param cast     Cast item
   * @param amount   Amount of fluid needed
   * @param location Recipe location
   */
  private void searedCasting(Consumer<FinishedRecipe> consumer, ItemLike block, Ingredient cast, int amount, String location) {
    ItemCastingRecipeBuilder.basinRecipe(block)
      .setFluidAndTime(TinkerFluids.moltenClay, false, amount)
      .setCast(cast, true)
      .save(consumer, this.modResource(location));
  }


  /* Scorched casting */

  /**
   * Adds a stonecutting recipe with automatic name and criteria
   *
   * @param consumer Recipe consumer
   * @param output   Recipe output
   * @param folder   Recipe folder path
   */
  private void scorchedStonecutter(Consumer<FinishedRecipe> consumer, ItemLike output, String folder) {
    SingleItemRecipeBuilder.stonecutting(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.SCORCHED_BLOCKS), Ingredient.of(output)), RecipeCategory.MISC, output, 1)
      .unlockedBy("has_block", has(TinkerTags.Items.SCORCHED_BLOCKS))
      .save(consumer, this.wrap(output.asItem(), folder, "_stonecutting"));
  }

  /**
   * Adds a recipe to create the given seared block using molten clay on stone
   *
   * @param consumer Recipe consumer
   * @param block    Output block
   * @param cast     Cast item
   * @param location Recipe location
   */
  private void scorchedCasting(Consumer<FinishedRecipe> consumer, ItemLike block, Ingredient cast, String location) {
    this.scorchedCasting(consumer, block, cast, FluidValues.SLIMEBALL * 2, location);
  }

  /**
   * Adds a recipe to create the given seared block using molten clay on stone
   *
   * @param consumer Recipe consumer
   * @param block    Output block
   * @param cast     Cast item
   * @param amount   Amount of fluid needed
   * @param location Recipe location
   */
  private void scorchedCasting(Consumer<FinishedRecipe> consumer, ItemLike block, Ingredient cast, int amount, String location) {
    ItemCastingRecipeBuilder.basinRecipe(block)
      .setFluidAndTime(TinkerFluids.magma, true, amount)
      .setCast(cast, true)
      .save(consumer, this.modResource(location));
  }


  /* Casting */

  /**
   * Adds melting recipes for slime
   *
   * @param consumer      Consumer
   * @param fluidSupplier Fluid
   * @param type          Slime type
   * @param folder        Output folder
   */
  private void slimeMelting(Consumer<FinishedRecipe> consumer, Supplier<? extends Fluid> fluidSupplier, SlimeType type, String folder) {
    String slimeFolder = folder + type.getSerializedName() + "/";
    MeltingRecipeBuilder.melting(Ingredient.of(type.getSlimeballTag()), fluidSupplier.get(), FluidValues.SLIMEBALL, 1.0f)
      .save(consumer, this.modResource(slimeFolder + "ball"));
    ItemLike item = TinkerWorld.congealedSlime.get(type);
    MeltingRecipeBuilder.melting(Ingredient.of(item), fluidSupplier.get(), FluidValues.SLIME_CONGEALED, 2.0f)
      .save(consumer, this.modResource(slimeFolder + "congealed"));
    item = TinkerWorld.slime.get(type);
    MeltingRecipeBuilder.melting(Ingredient.of(item), fluidSupplier.get(), FluidValues.SLIME_BLOCK, 3.0f)
      .save(consumer, this.modResource(slimeFolder + "block"));
  }

  /**
   * Adds slime related casting recipes
   *
   * @param consumer  Recipe consumer
   * @param fluid     Fluid matching the slime type
   * @param slimeType SlimeType for this recipe
   * @param folder    Output folder
   */
  private void slimeCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, SlimeType slimeType, String folder) {
    String colorFolder = folder + slimeType.getSerializedName() + "/";
    ItemCastingRecipeBuilder.basinRecipe(TinkerWorld.congealedSlime.get(slimeType))
      .setFluidAndTime(fluid, forgeTag, FluidValues.SLIME_CONGEALED)
      .save(consumer, this.modResource(colorFolder + "congealed"));
    ItemCastingRecipeBuilder.basinRecipe(TinkerWorld.slime.get(slimeType))
      .setFluidAndTime(fluid, forgeTag, FluidValues.SLIME_BLOCK - FluidValues.SLIME_CONGEALED)
      .setCast(TinkerWorld.congealedSlime.get(slimeType), true)
      .save(consumer, this.modResource(colorFolder + "block"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.slimeball.get(slimeType))
      .setFluidAndTime(fluid, forgeTag, FluidValues.SLIMEBALL)
      .save(consumer, this.modResource(colorFolder + "slimeball"));
    ItemCastingRecipeBuilder.tableRecipe(TinkerFluids.slimeBottle.get(slimeType))
      .setFluid(forgeTag ? fluid.getForgeTag() : fluid.getLocalTag(), FluidValues.SLIMEBALL)
      .setCoolingTime(1)
      .setCast(Items.GLASS_BOTTLE, true)
      .save(consumer, this.modResource(colorFolder + "bottle"));
    if (slimeType != SlimeType.BLOOD) {
      ItemCastingRecipeBuilder.basinRecipe(TinkerWorld.slimeDirt.get(slimeType))
        .setFluidAndTime(fluid, forgeTag, FluidValues.SLIME_CONGEALED)
        .setCast(Blocks.DIRT, true)
        .save(consumer, this.modResource(colorFolder + "dirt"));
    }
  }

  /**
   * Adds recipes for melting slime crystals
   */
  private void crystalMelting(Consumer<FinishedRecipe> consumer, GeodeItemObject geode, Fluid fluid, String folder) {
    MeltingRecipeBuilder.melting(Ingredient.of(geode), fluid, FluidValues.SLIMEBALL, 1.0f).save(consumer, this.modResource(folder + "crystal"));
    MeltingRecipeBuilder.melting(Ingredient.of(geode.getBlock()), fluid, FluidValues.SLIMEBALL * 4, 2.0f).save(consumer, this.modResource(folder + "crystal_block"));
    for (BudSize bud : BudSize.values()) {
      int size = bud.getSize();
      MeltingRecipeBuilder.melting(Ingredient.of(geode.getBud(bud)), fluid, (long) FluidValues.SLIMEBALL * size, (size + 1) / 2f)
        .setOre(OreRateType.GEM)
        .save(consumer, this.modResource(folder + "bud_" + bud.getName()));
    }
  }
}
