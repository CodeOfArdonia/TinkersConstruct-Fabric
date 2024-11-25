package slimeknights.tconstruct.library.data.recipe;

import io.github.fabricators_of_create.porting_lib.data.ConditionalRecipe;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.fabricators_of_create.porting_lib.util.TrueCondition;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.mantle.registration.object.MetalItemObject;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.library.json.TagDifferencePresentCondition;
import slimeknights.tconstruct.library.json.TagIntersectionPresentCondition;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.OreRateType;
import slimeknights.tconstruct.library.recipe.melting.MeltingRecipeBuilder;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Recipe helper for methods related to melting and casting
 */
public interface ISmelteryRecipeHelper extends ICastCreationHelper {
  /* Melting */

  /**
   * Base logic for {@link  #metalMelting(Consumer, Fluid, String, boolean, String, boolean, IByproduct...)}
   *
   * @param consumer   Recipe consumer
   * @param fluid      Fluid to melt into
   * @param amount     Amount to melt into
   * @param tagName    Input tag
   * @param factor     Melting factor
   * @param recipePath Recipe output name
   * @param isOptional If true, recipe is optional
   */
  default void tagMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, long amount, String tagName, float factor, String recipePath, boolean isOptional) {
    Consumer<FinishedRecipe> wrapped = isOptional ? this.withCondition(consumer, this.tagCondition(tagName)) : consumer;
    MeltingRecipeBuilder.melting(Ingredient.of(this.getItemTag("c", tagName)), fluid, amount, factor)
      .save(wrapped, this.modResource(recipePath));
  }

  /**
   * Base logic for {@link  #metalMelting(Consumer, Fluid, String, boolean, String, boolean, IByproduct...)}
   *
   * @param consumer   Recipe consumer
   * @param fluid      Fluid to melt into
   * @param amount     Amount to melt into
   * @param tagName    Input tag
   * @param factor     Melting factor
   * @param recipePath Recipe output name
   * @param oreRate    Ore rate for boosting
   * @param isOptional If true, recipe is optional
   * @param byproducts List of byproduct options for this metal, first one that is present will be used
   */
  default void oreMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, long amount, String tagName, @Nullable TagKey<Item> size, float factor, String recipePath, boolean isOptional, OreRateType oreRate, float byproductScale, IByproduct... byproducts) {
    Consumer<FinishedRecipe> wrapped;
    Ingredient baseIngredient = Ingredient.of(this.getItemTag("c", tagName));
    Ingredient ingredient;
    // not everyone sets size, so treat singular as the fallback, means we want anything in the tag that is not sparse or dense
    if (size == Tags.Items.ORE_RATES_SINGULAR) {
      ingredient = DefaultCustomIngredients.difference(baseIngredient, DefaultCustomIngredients.any(Ingredient.of(Tags.Items.ORE_RATES_SPARSE), Ingredient.of(Tags.Items.ORE_RATES_DENSE)));
      wrapped = this.withCondition(consumer, TagDifferencePresentCondition.ofKeys(this.getItemTag("c", tagName), Tags.Items.ORE_RATES_SPARSE, Tags.Items.ORE_RATES_DENSE));
      // size tag means we want an intersection between the tag and that size
    } else if (size != null) {
      ingredient = DefaultCustomIngredients.all(baseIngredient, Ingredient.of(size));
      wrapped = this.withCondition(consumer, TagIntersectionPresentCondition.ofKeys(this.getItemTag("c", tagName), size));
      // default only need it to be in the tag
    } else {
      ingredient = baseIngredient;
      wrapped = isOptional ? this.withCondition(consumer, this.tagCondition(tagName)) : consumer;
    }
    Supplier<MeltingRecipeBuilder> supplier = () -> MeltingRecipeBuilder.melting(ingredient, fluid, amount, factor).setOre(oreRate);
    ResourceLocation location = this.modResource(recipePath);

    // if no byproducts, just build directly
    if (byproducts.length == 0) {
      supplier.get().save(wrapped, location);
      // if first option is always present, only need that one
    } else if (byproducts[0].isAlwaysPresent()) {
      supplier.get()
        .addByproduct(new FluidStack(byproducts[0].getFluid(), (int) (byproducts[0].getAmount() * byproductScale)))
        .save(wrapped, location);
    } else {
      // multiple options, will need a conditonal recipe
      ConditionalRecipe.Builder builder = ConditionalRecipe.builder();
      boolean alwaysPresent = false;
      for (IByproduct byproduct : byproducts) {
        // found an always present byproduct? no need to tag and we are done
        alwaysPresent = byproduct.isAlwaysPresent();
        if (alwaysPresent) {
          builder.addCondition(TrueCondition.INSTANCE);
        } else {
          builder.addCondition(this.tagCondition(byproduct.getName() + "_ingots"));
        }
        builder.addRecipe(supplier.get().addByproduct(new FluidStack(byproduct.getFluid(), (int) (byproduct.getAmount() * byproductScale)))::save);

        if (alwaysPresent) {
          break;
        }
      }
      // not always present? add a recipe with no byproducts as a final fallback
      if (!alwaysPresent) {
        builder.addCondition(TrueCondition.INSTANCE);
        builder.addRecipe(supplier.get()::save);
      }
      builder.build(wrapped, location);
    }
  }

  /**
   * Mod compat for Geores, adds melting for geore shards and blocks
   *
   * @param consumer Recipe consumer
   * @param fluid    Fluid
   * @param name     Material name
   * @param folder   Output folder
   */
  default void georeMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, long unit, String name, String folder) {
    // base
    this.tagMelting(consumer, fluid, unit, "geore_shards/" + name, 1.0f, folder + "geore/shard", true);
    this.tagMelting(consumer, fluid, unit * 4, "geore_blocks/" + name, 2.0f, folder + "geore/block", true);
    // clusters
    this.tagMelting(consumer, fluid, unit * 4, "geore_clusters/" + name, 2.5f, folder + "geore/cluster", true);
    this.tagMelting(consumer, fluid, unit, "geore_small_buds/" + name, 1.0f, folder + "geore/bud_small", true);
    this.tagMelting(consumer, fluid, unit * 2, "geore_medium_buds/" + name, 1.5f, folder + "geore/bud_medium", true);
    this.tagMelting(consumer, fluid, unit * 3, "geore_large_buds/" + name, 2.0f, folder + "geore/bud_large", true);
  }

  /**
   * Adds a basic ingot, nugget, block, ore melting recipe set
   *
   * @param consumer   Recipe consumer
   * @param fluid      Fluid result
   * @param name       Resource name for tags
   * @param hasOre     If true, adds recipe for melting the ore
   * @param hasDust    If false, the dust form of this item does not correspond to the ingot form
   * @param folder     Recipe folder
   * @param isOptional If true, this recipe is entirely optional
   * @param byproducts List of byproduct options for this metal, first one that is present will be used
   */
  default void metalMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, String name, boolean hasOre, boolean hasDust, String folder, boolean isOptional, IByproduct... byproducts) {
    String prefix = folder + "/" + name + "/";
    this.tagMelting(consumer, fluid, FluidValues.METAL_BLOCK, name + "_blocks", 3.0f, prefix + "block", isOptional);
    this.tagMelting(consumer, fluid, FluidValues.INGOT, name + "_ingots", 1.0f, prefix + "ingot", isOptional);
    this.tagMelting(consumer, fluid, FluidValues.NUGGET, "nuggets/" + name, 1 / 3f, prefix + "nugget", isOptional);
    if (hasOre) {
      this.oreMelting(consumer, fluid, FluidValues.INGOT, "raw_" + name + "_ores", null, 1.5f, prefix + "raw", isOptional, OreRateType.METAL, 1.0f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.INGOT * 9, "raw_" + name + "_blocks", null, 6.0f, prefix + "raw_block", isOptional, OreRateType.METAL, 9.0f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.INGOT, name + "_ores", Tags.Items.ORE_RATES_SPARSE, 1.5f, prefix + "ore_sparse", isOptional, OreRateType.METAL, 1.0f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.INGOT * 2, name + "_ores", Tags.Items.ORE_RATES_SINGULAR, 2.5f, prefix + "ore_singular", isOptional, OreRateType.METAL, 2.0f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.INGOT * 6, name + "_ores", Tags.Items.ORE_RATES_DENSE, 4.5f, prefix + "ore_dense", isOptional, OreRateType.METAL, 6.0f, byproducts);
      this.georeMelting(consumer, fluid, FluidValues.INGOT, name, prefix);
    }
    // remaining forms are always optional as we don't ship them
    // allow disabling dust as some mods treat dust as distinct from ingots
    if (hasDust) {
      this.tagMelting(consumer, fluid, FluidValues.INGOT, name + "_dusts", 0.75f, prefix + "dust", true);
    }
    this.tagMelting(consumer, fluid, FluidValues.INGOT, name + "_plates", 1.0f, prefix + "plates", true);
    this.tagMelting(consumer, fluid, FluidValues.INGOT * 4, name + "_gears", 2.0f, prefix + "gear", true);
    this.tagMelting(consumer, fluid, FluidValues.NUGGET * 3, name + "_coins", 2 / 3f, prefix + "coin", true);
    this.tagMelting(consumer, fluid, FluidValues.INGOT / 2, name + "_rods", 1 / 5f, prefix + "rod", true);
    this.tagMelting(consumer, fluid, FluidValues.INGOT / 2, name + "_wires", 1 / 5f, prefix + "wire", true);
    this.tagMelting(consumer, fluid, FluidValues.INGOT, "sheetmetals/" + name, 1.0f, prefix + "sheetmetal", true);
  }

  /**
   * Adds a basic ingot, nugget, block, ore melting recipe set
   *
   * @param consumer   Recipe consumer
   * @param fluid      Fluid result
   * @param name       Resource name for tags
   * @param hasOre     If true, adds recipe for melting the ore
   * @param folder     Recipe folder
   * @param isOptional If true, this recipe is entirely optional
   * @param byproducts List of byproduct options for this metal, first one that is present will be used
   */
  default void metalMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, String name, boolean hasOre, String folder, boolean isOptional, IByproduct... byproducts) {
    this.metalMelting(consumer, fluid, name, hasOre, true, folder, isOptional, byproducts);
  }

  /**
   * Adds a basic gem, block, ore melting recipe set
   *
   * @param consumer   Recipe consumer
   * @param fluid      Fluid result
   * @param name       Resource name for tags
   * @param blockSize  Number of gems to make one block
   * @param folder     Recipe folder
   * @param isOptional If true, this recipe is entirely optional
   * @param byproducts List of byproduct options for this metal, first one that is present will be used
   */
  default void gemMelting(Consumer<FinishedRecipe> consumer, Fluid fluid, String name, String tagSuffix, boolean hasOre, int blockSize, String folder, boolean isOptional, IByproduct... byproducts) {
    String prefix = folder + "/" + name + "/";
    // basic
    this.tagMelting(consumer, fluid, (long) FluidValues.GEM * blockSize, name + "_blocks", (float) Math.sqrt(blockSize), prefix + "block", isOptional);
    this.tagMelting(consumer, fluid, FluidValues.GEM, name + tagSuffix, 1.0f, prefix + "gem", isOptional);
    // ores
    if (hasOre) {
      this.oreMelting(consumer, fluid, FluidValues.GEM / 2, name + "_ores", Tags.Items.ORE_RATES_SPARSE, 1.0f, prefix + "ore_sparse", isOptional, OreRateType.GEM, 0.5f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.GEM, name + "_ores", Tags.Items.ORE_RATES_SINGULAR, 1.5f, prefix + "ore_singular", isOptional, OreRateType.GEM, 1.0f, byproducts);
      this.oreMelting(consumer, fluid, FluidValues.GEM * 3, name + "_ores", Tags.Items.ORE_RATES_DENSE, 4.5f, prefix + "ore_dense", isOptional, OreRateType.GEM, 3.0f, byproducts);
      this.georeMelting(consumer, fluid, FluidValues.GEM, name, prefix);
    }
  }


  /* Casting */

  /**
   * Adds a recipe for casting using a cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Recipe fluid
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param amount   Fluid amount
   * @param cast     Cast used
   * @param output   Recipe output
   * @param location Recipe base
   */
  default void castingWithCast(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, long amount, CastItemObject cast, ItemOutput output, String location) {
    ItemCastingRecipeBuilder.tableRecipe(output)
      .setFluidAndTime(fluid, forgeTag, amount)
      .setCast(cast.getMultiUseTag(), false)
      .save(consumer, this.modResource(location + "_gold_cast"));
    ItemCastingRecipeBuilder.tableRecipe(output)
      .setFluidAndTime(fluid, forgeTag, amount)
      .setCast(cast.getSingleUseTag(), true)
      .save(consumer, this.modResource(location + "_sand_cast"));
  }

  /**
   * Adds a recipe for casting using a cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Recipe fluid
   * @param amount   Fluid amount
   * @param cast     Cast used
   * @param output   Recipe output
   * @param location Recipe base
   */
  default void castingWithCast(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, long amount, CastItemObject cast, ItemOutput output, String location) {
    this.castingWithCast(consumer, fluid, false, amount, cast, output, location);
  }

  /**
   * Adds a recipe for casting using a cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Recipe fluid
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param amount   Fluid amount
   * @param cast     Cast used
   * @param output   Recipe output
   * @param location Recipe base
   */
  default void castingWithCast(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, long amount, CastItemObject cast, ItemLike output, String location) {
    this.castingWithCast(consumer, fluid, forgeTag, amount, cast, ItemOutput.fromItem(output), location);
  }

  /**
   * Adds a recipe for casting using a cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Recipe fluid
   * @param amount   Fluid amount
   * @param cast     Cast used
   * @param output   Recipe output
   * @param location Recipe base
   */
  default void castingWithCast(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, long amount, CastItemObject cast, ItemLike output, String location) {
    this.castingWithCast(consumer, fluid, amount, cast, ItemOutput.fromItem(output), location);
  }

  /**
   * Adds a recipe for casting an item from a tag
   *
   * @param consumer   Recipe consumer
   * @param fluid      Input fluid
   * @param forgeTag   If true, uses the forge tag from the fluid instead of the local tag
   * @param amount     Recipe amount
   * @param cast       Cast for recipe
   * @param tagName    Tag for output
   * @param recipeName Name of the recipe for output
   * @param optional   If true, conditions the recipe on the tag
   */
  default void tagCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, long amount, CastItemObject cast, String tagName, String recipeName, boolean optional) {
    if (optional) {
      consumer = this.withCondition(consumer, this.tagCondition(tagName));
    }
    this.castingWithCast(consumer, fluid, forgeTag, amount, cast, ItemOutput.fromTag(this.getItemTag("c", tagName), 1), recipeName);
  }

  /**
   * Adds a recipe for casting an item from a tag
   *
   * @param consumer   Recipe consumer
   * @param fluid      Input fluid
   * @param amount     Recipe amount
   * @param cast       Cast for recipe
   * @param tagName    Tag for output
   * @param recipeName Name of the recipe for output
   * @param optional   If true, conditions the recipe on the tag
   */
  default void tagCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, int amount, CastItemObject cast, String tagName, String recipeName, boolean optional) {
    this.tagCasting(consumer, fluid, false, amount, cast, tagName, recipeName, optional);
  }


  /**
   * Adds a casting recipe using an ingot cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param amount   Recipe amount
   * @param ingot    Ingot output
   * @param location Recipe base
   */
  default void ingotCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, long amount, ItemLike ingot, String location) {
    this.castingWithCast(consumer, fluid, forgeTag, amount, TinkerSmeltery.ingotCast, ingot, location);
  }

  /**
   * Adds a casting recipe using an ingot cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param ingot    Ingot output
   * @param location Recipe base
   */
  default void ingotCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, ItemLike ingot, String location) {
    this.ingotCasting(consumer, fluid, forgeTag, FluidValues.INGOT, ingot, location);
  }

  /**
   * Adds a casting recipe using an ingot cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param amount   Recipe amount
   * @param ingot    Ingot output
   * @param location Recipe base
   */
  default void ingotCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, long amount, ItemLike ingot, String location) {
    this.ingotCasting(consumer, fluid, false, amount, ingot, location);
  }

  /**
   * Adds a casting recipe using an ingot cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param ingot    Ingot output
   * @param location Recipe base
   */
  default void ingotCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, ItemLike ingot, String location) {
    this.ingotCasting(consumer, fluid, FluidValues.INGOT, ingot, location);
  }

  /**
   * Adds a casting recipe using an ingot cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param gem      Gem output
   * @param location Recipe base
   */
  default void gemCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, ItemLike gem, String location) {
    this.castingWithCast(consumer, fluid, FluidValues.GEM, TinkerSmeltery.gemCast, gem, location);
  }

  /**
   * Adds a casting recipe using a nugget cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param nugget   Nugget output
   * @param location Recipe base
   */
  default void nuggetCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, ItemLike nugget, String location) {
    this.castingWithCast(consumer, fluid, forgeTag, FluidValues.NUGGET, TinkerSmeltery.nuggetCast, nugget, location);
  }

  /**
   * Adds a casting recipe using a nugget cast
   *
   * @param consumer Recipe consumer
   * @param fluid    Input fluid
   * @param nugget   Nugget output
   * @param location Recipe base
   */
  default void nuggetCastingRecipe(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, ItemLike nugget, String location) {
    this.nuggetCasting(consumer, fluid, false, nugget, location);
  }

  /**
   * Add recipes for a standard mineral, uses local tag
   *
   * @param consumer Recipe consumer
   * @param fluid    Fluid input
   * @param forgeTag If true, uses the forge tag from the fluid instead of the local tag
   * @param block    Block result
   * @param ingot    Ingot result
   * @param nugget   Nugget result
   * @param folder   Output folder
   */
  default void metalCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, boolean forgeTag, @Nullable ItemLike block, @Nullable ItemLike ingot, @Nullable ItemLike nugget, String folder, String metal) {
    String metalFolder = folder + metal + "/";
    if (block != null) {
      ItemCastingRecipeBuilder.basinRecipe(block)
        .setFluidAndTime(fluid, forgeTag, FluidValues.METAL_BLOCK)
        .save(consumer, this.modResource(metalFolder + "block"));
    }
    if (ingot != null) {
      this.ingotCasting(consumer, fluid, forgeTag, ingot, metalFolder + "ingot");
    }
    if (nugget != null) {
      this.nuggetCasting(consumer, fluid, forgeTag, nugget, metalFolder + "nugget");
    }
    // plates are always optional, we don't ship them
    this.tagCasting(consumer, fluid, forgeTag, FluidValues.INGOT, TinkerSmeltery.plateCast, metal + "_plates", folder + metal + "/plate", true);
    this.tagCasting(consumer, fluid, forgeTag, FluidValues.INGOT * 4, TinkerSmeltery.gearCast, metal + "_gears", folder + metal + "/gear", true);
    this.tagCasting(consumer, fluid, forgeTag, FluidValues.NUGGET * 3, TinkerSmeltery.coinCast, metal + "_coins", folder + metal + "/coin", true);
    this.tagCasting(consumer, fluid, forgeTag, FluidValues.INGOT / 2, TinkerSmeltery.rodCast, metal + "_rods", folder + metal + "/rod", true);
    this.tagCasting(consumer, fluid, forgeTag, FluidValues.INGOT / 2, TinkerSmeltery.wireCast, metal + "_wires", folder + metal + "/wire", true);
  }

  /**
   * Add recipes for a standard mineral, uses local tag
   *
   * @param consumer Recipe consumer
   * @param fluid    Fluid input
   * @param block    Block result
   * @param ingot    Ingot result
   * @param nugget   Nugget result
   * @param folder   Output folder
   */
  default void metalCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, @Nullable ItemLike block, @Nullable ItemLike ingot, @Nullable ItemLike nugget, String folder, String metal) {
    this.metalCasting(consumer, fluid, false, block, ingot, nugget, folder, metal);
  }

  /**
   * Add recipes for a standard mineral, uses local tag
   *
   * @param consumer Recipe consumer
   * @param fluid    Fluid input
   * @param metal    Metal object
   * @param folder   Output folder
   */
  default void metalCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, MetalItemObject metal, String folder, String name) {
    this.metalCasting(consumer, fluid, metal.get(), metal.getIngot(), metal.getNugget(), folder, name);
  }

  /**
   * Add recipes for a standard mineral, uses forge tag
   *
   * @param consumer      Recipe consumer
   * @param fluid         Fluid input
   * @param name          Name of ore
   * @param folder        Output folder
   * @param forceStandard If true, all default materials will always get a recipe, used for common materials provided by the mod (e.g. copper)
   */
  default void metalTagCasting(Consumer<FinishedRecipe> consumer, FluidObject<?> fluid, String name, String folder, boolean forceStandard) {
    // nugget and ingot
    this.tagCasting(consumer, fluid, true, FluidValues.NUGGET, TinkerSmeltery.nuggetCast, name + "_nuggets", folder + name + "/nugget", !forceStandard);
    this.tagCasting(consumer, fluid, true, FluidValues.INGOT, TinkerSmeltery.ingotCast, name + "_ingots", folder + name + "/ingot", !forceStandard);
    this.tagCasting(consumer, fluid, true, FluidValues.INGOT, TinkerSmeltery.plateCast, name + "_plates", folder + name + "/plate", true);
    this.tagCasting(consumer, fluid, true, FluidValues.INGOT * 4, TinkerSmeltery.gearCast, name + "_gears", folder + name + "/gear", true);
    this.tagCasting(consumer, fluid, true, FluidValues.NUGGET * 3, TinkerSmeltery.coinCast, name + "_coins", folder + name + "/coin", true);
    this.tagCasting(consumer, fluid, true, FluidValues.INGOT / 2, TinkerSmeltery.rodCast, name + "_rods", folder + name + "/rod", true);
    this.tagCasting(consumer, fluid, true, FluidValues.INGOT / 2, TinkerSmeltery.wireCast, name + "_wires", folder + name + "/wire", true);
    // block
    TagKey<Item> block = this.getItemTag("c", name + "_blocks");
    Consumer<FinishedRecipe> wrapped = forceStandard ? consumer : this.withCondition(consumer, this.tagCondition(name + "_blocks"));
    ItemCastingRecipeBuilder.basinRecipe(block)
      .setFluidAndTime(fluid, true, FluidValues.METAL_BLOCK)
      .save(wrapped, this.modResource(folder + name + "/block"));
  }
}
