package slimeknights.tconstruct.library.data.recipe;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipeBuilder;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipeBuilder;
import slimeknights.tconstruct.library.recipe.melting.MaterialMeltingRecipeBuilder;

import javax.annotation.Nullable;
import java.util.function.Consumer;

/**
 * Interface for adding recipes for tool materials
 */
public interface IMaterialRecipeHelper extends IRecipeHelper {

  /**
   * Registers a material recipe
   *
   * @param consumer Recipe consumer
   * @param material Material ID
   * @param input    Recipe input
   * @param value    Material value
   * @param needed   Number of items needed
   * @param saveName Material save name
   */
  default void materialRecipe(Consumer<FinishedRecipe> consumer, MaterialVariantId material, Ingredient input, int value, int needed, String saveName) {
    this.materialRecipe(consumer, material, input, value, needed, null, saveName);
  }

  /**
   * Registers a material recipe
   *
   * @param consumer Recipe consumer
   * @param material Material ID
   * @param input    Recipe input
   * @param value    Material value
   * @param needed   Number of items needed
   * @param saveName Material save name
   */
  default void materialRecipe(Consumer<FinishedRecipe> consumer, MaterialVariantId material, Ingredient input, int value, int needed, @Nullable ItemOutput leftover, String saveName) {
    MaterialRecipeBuilder builder = MaterialRecipeBuilder.materialRecipe(material)
      .setIngredient(input)
      .setValue(value)
      .setNeeded(needed);
    if (leftover != null) {
      builder.setLeftover(leftover);
    }
    builder.save(consumer, this.modResource(saveName));
  }

  /**
   * Register ingots, nuggets, and blocks for a metal material
   *
   * @param consumer Consumer instance
   * @param material Material
   * @param name     Material name
   */
  default void metalMaterialRecipe(Consumer<FinishedRecipe> consumer, MaterialVariantId material, String folder, String name, boolean optional) {
    Consumer<FinishedRecipe> wrapped = optional ? this.withCondition(consumer, this.tagCondition(name + "_ingots")) : consumer;
    String matName = material.getLocation('/').getPath();
    // ingot
    TagKey<Item> ingotTag = this.getItemTag("c", name + "_ingots");
    this.materialRecipe(wrapped, material, Ingredient.of(ingotTag), 1, 1, folder + matName + "/ingot");
    // nugget
    wrapped = optional ? this.withCondition(consumer, this.tagCondition(name + "_nuggets")) : consumer;
    this.materialRecipe(wrapped, material, Ingredient.of(this.getItemTag("c", name + "_nuggets")), 1, 9, folder + matName + "/nugget");
    // block
    wrapped = optional ? this.withCondition(consumer, this.tagCondition(name + "_blocks")) : consumer;
    this.materialRecipe(wrapped, material, Ingredient.of(this.getItemTag("c", name + "_blocks")), 9, 1, ItemOutput.fromTag(ingotTag, 1), folder + matName + "/block");
  }

  /**
   * Adds recipes to melt a material
   */
  default void materialMelting(Consumer<FinishedRecipe> consumer, MaterialVariantId material, Fluid fluid, long fluidAmount, String folder) {
    MaterialMeltingRecipeBuilder.material(material, new FluidStack(fluid, fluidAmount))
      .save(consumer, this.modResource(folder + "melting/" + material.getLocation('_').getPath()));
  }

  /**
   * Adds recipes to melt and cast a material
   */
  default void materialMeltingCasting(Consumer<FinishedRecipe> consumer, MaterialVariantId material, FluidObject<?> fluid, boolean forgeTag, long fluidAmount, String folder) {
    MaterialFluidRecipeBuilder.material(material)
      .setFluid(forgeTag ? fluid.getForgeTag() : fluid.getLocalTag(), fluidAmount)
      .setTemperature(FluidVariantAttributes.getTemperature(FluidVariant.of(fluid.get())) - 300)
      .save(consumer, this.modResource(folder + "casting/" + material.getLocation('_').getPath()));
    this.materialMelting(consumer, material, fluid.get(), fluidAmount, folder);
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void materialMeltingCasting(Consumer<FinishedRecipe> consumer, MaterialVariantId material, FluidObject<?> fluid, boolean forgeTag, String folder) {
    this.materialMeltingCasting(consumer, material, fluid, forgeTag, FluidValues.INGOT, folder);
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void compatMeltingCasting(Consumer<FinishedRecipe> consumer, MaterialId material, FluidObject<?> fluid, String folder) {
    this.materialMeltingCasting(this.withCondition(consumer, this.tagCondition(material.getPath() + "_ingots")), material, fluid, true, folder);
  }

  /**
   * Adds recipes to melt and cast a material
   */
  default void materialMeltingCasting(Consumer<FinishedRecipe> consumer, MaterialVariantId material, FluidObject<?> fluid, long fluidAmount, String folder) {
    this.materialMeltingCasting(consumer, material, fluid, false, fluidAmount, folder);
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void materialMeltingCasting(Consumer<FinishedRecipe> consumer, MaterialVariantId material, FluidObject<?> fluid, String folder) {
    this.materialMeltingCasting(consumer, material, fluid, FluidValues.INGOT, folder);
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void materialMeltingComposite(Consumer<FinishedRecipe> consumer, MaterialVariantId input, MaterialVariantId output, FluidObject<?> fluid, boolean forgeTag, long amount, String folder) {
    this.materialMelting(consumer, output, fluid.get(), amount, folder);
    this.materialComposite(consumer, input, output, fluid, forgeTag, amount, folder);
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void materialComposite(Consumer<FinishedRecipe> consumer, MaterialVariantId input, MaterialVariantId output, FluidObject<?> fluid, boolean forgeTag, long amount, String folder, String name) {
    MaterialFluidRecipeBuilder.material(output)
      .setInputId(input)
      .setFluid(forgeTag ? fluid.getForgeTag() : fluid.getLocalTag(), amount)
      .setTemperature(FluidVariantAttributes.getTemperature(FluidVariant.of(fluid.get())) - 300)
      .save(consumer, this.modResource(folder + "composite/" + name));
  }

  /**
   * Adds recipes to melt and cast a material of ingot size
   */
  default void materialComposite(Consumer<FinishedRecipe> consumer, MaterialVariantId input, MaterialVariantId output, FluidObject<?> fluid, boolean forgeTag, long amount, String folder) {
    this.materialComposite(consumer, input, output, fluid, forgeTag, amount, folder, output.getLocation('_').getPath());
  }
}
