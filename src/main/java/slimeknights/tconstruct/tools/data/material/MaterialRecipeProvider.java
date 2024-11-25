package slimeknights.tconstruct.tools.data.material;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.library.json.TagDifferencePresentCondition;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialFluidRecipeBuilder;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

@SuppressWarnings("removal")
public class MaterialRecipeProvider extends BaseRecipeProvider implements IMaterialRecipeHelper {

  public MaterialRecipeProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Material Recipe";
  }

  @Override
  public void buildRecipes(Consumer<FinishedRecipe> consumer) {
    this.addMaterialItems(consumer);
    this.addMaterialSmeltery(consumer);
  }

  private void addMaterialItems(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/materials/";
    // tier 1
    this.materialRecipe(consumer, MaterialIds.wood, Ingredient.of(Tags.Items.RODS_WOODEN), 1, 2, folder + "wood/sticks");
    this.materialRecipe(consumer, MaterialIds.bamboo, Ingredient.of(Items.BAMBOO), 1, 4, folder + "wood/bamboo");
    // planks
    this.materialRecipe(consumer, MaterialIds.oak, Ingredient.of(Items.OAK_PLANKS), 1, 1, folder + "wood/planks/oak");
    this.materialRecipe(consumer, MaterialIds.spruce, Ingredient.of(Items.SPRUCE_PLANKS), 1, 1, folder + "wood/planks/spruce");
    this.materialRecipe(consumer, MaterialIds.birch, Ingredient.of(Items.BIRCH_PLANKS), 1, 1, folder + "wood/planks/birch");
    this.materialRecipe(consumer, MaterialIds.jungle, Ingredient.of(Items.JUNGLE_PLANKS), 1, 1, folder + "wood/planks/jungle");
    this.materialRecipe(consumer, MaterialIds.darkOak, Ingredient.of(Items.DARK_OAK_PLANKS), 1, 1, folder + "wood/planks/dark_oak");
    this.materialRecipe(consumer, MaterialIds.acacia, Ingredient.of(Items.ACACIA_PLANKS), 1, 1, folder + "wood/planks/acacia");
    this.materialRecipe(consumer, MaterialIds.crimson, Ingredient.of(Items.CRIMSON_PLANKS), 1, 1, folder + "wood/planks/crimson");
    this.materialRecipe(consumer, MaterialIds.warped, Ingredient.of(Items.WARPED_PLANKS), 1, 1, folder + "wood/planks/warped");
    this.materialRecipe(consumer, MaterialIds.mangrove, Ingredient.of(Items.MANGROVE_PLANKS), 1, 1, folder + "wood/planks/mangrove");
    this.materialRecipe(consumer, MaterialIds.cherry, Ingredient.of(Items.CHERRY_PLANKS), 1, 1, folder + "wood/planks/cherry");
    this.materialRecipe(consumer, MaterialIds.bamboo, Ingredient.of(Items.BAMBOO_PLANKS), 1, 1, folder + "wood/planks/bamboo");
    this.materialRecipe(this.withCondition(consumer, TagDifferencePresentCondition.ofKeys(ItemTags.PLANKS, TinkerTags.Items.VARIANT_PLANKS)), MaterialIds.wood,
      DefaultCustomIngredients.difference(Ingredient.of(ItemTags.PLANKS), Ingredient.of(TinkerTags.Items.VARIANT_PLANKS)), 1, 1, folder + "wood/planks/default");
    // logs
    this.materialRecipe(consumer, MaterialIds.oak, Ingredient.of(ItemTags.OAK_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.OAK_PLANKS)), folder + "wood/logs/oak");
    this.materialRecipe(consumer, MaterialIds.spruce, Ingredient.of(ItemTags.SPRUCE_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.SPRUCE_PLANKS)), folder + "wood/logs/spruce");
    this.materialRecipe(consumer, MaterialIds.birch, Ingredient.of(ItemTags.BIRCH_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.BIRCH_PLANKS)), folder + "wood/logs/birch");
    this.materialRecipe(consumer, MaterialIds.jungle, Ingredient.of(ItemTags.JUNGLE_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.JUNGLE_PLANKS)), folder + "wood/logs/jungle");
    this.materialRecipe(consumer, MaterialIds.darkOak, Ingredient.of(ItemTags.DARK_OAK_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.DARK_OAK_PLANKS)), folder + "wood/logs/dark_oak");
    this.materialRecipe(consumer, MaterialIds.acacia, Ingredient.of(ItemTags.ACACIA_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.ACACIA_PLANKS)), folder + "wood/logs/acacia");
    this.materialRecipe(consumer, MaterialIds.crimson, Ingredient.of(ItemTags.CRIMSON_STEMS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.CRIMSON_PLANKS)), folder + "wood/logs/crimson");
    this.materialRecipe(consumer, MaterialIds.warped, Ingredient.of(ItemTags.WARPED_STEMS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.WARPED_PLANKS)), folder + "wood/logs/warped");
    this.materialRecipe(this.withCondition(consumer, TagDifferencePresentCondition.ofKeys(ItemTags.LOGS, TinkerTags.Items.VARIANT_LOGS)), MaterialIds.wood,
      DefaultCustomIngredients.difference(Ingredient.of(ItemTags.LOGS), Ingredient.of(TinkerTags.Items.VARIANT_LOGS)), 4, 1,
      ItemOutput.fromStack(new ItemStack(Items.STICK, 2)), folder + "wood/logs/default");
    // stone
    this.materialRecipe(consumer, MaterialIds.stone, Ingredient.of(TinkerTags.Items.STONE), 1, 1, folder + "rock/stone");
    this.materialRecipe(consumer, MaterialIds.andesite, Ingredient.of(TinkerTags.Items.ANDESITE), 1, 1, folder + "rock/andesite");
    this.materialRecipe(consumer, MaterialIds.diorite, Ingredient.of(TinkerTags.Items.DIORITE), 1, 1, folder + "rock/diorite");
    this.materialRecipe(consumer, MaterialIds.granite, Ingredient.of(TinkerTags.Items.GRANITE), 1, 1, folder + "rock/granite");
    this.materialRecipe(consumer, MaterialIds.deepslate, Ingredient.of(TinkerTags.Items.DEEPSLATE), 1, 1, folder + "rock/deepslate");
    this.materialRecipe(consumer, MaterialIds.blackstone, Ingredient.of(TinkerTags.Items.BLACKSTONE), 1, 1, folder + "rock/blackstone");
    this.materialRecipe(consumer, MaterialIds.flint, Ingredient.of(Items.FLINT), 1, 1, folder + "flint");
    this.materialRecipe(consumer, MaterialIds.basalt, Ingredient.of(TinkerTags.Items.BASALT), 1, 1, folder + "flint_basalt");
    // other tier 1
    this.materialRecipe(consumer, MaterialIds.bone, Ingredient.of(Tags.Items.BONES), 1, 1, folder + "bone");
    this.materialRecipe(consumer, MaterialIds.chorus, Ingredient.of(Items.POPPED_CHORUS_FRUIT), 1, 4, folder + "chorus_popped");
    this.metalMaterialRecipe(consumer, MaterialIds.copper, folder, "copper", false);
    // tier 1 binding
    this.materialRecipe(consumer, MaterialIds.string, Ingredient.of(Tags.Items.STRING), 1, 4, folder + "string");
    this.materialRecipe(consumer, MaterialIds.leather, Ingredient.of(Tags.Items.LEATHER), 1, 1, folder + "leather");
    this.materialRecipe(consumer, MaterialIds.leather, Ingredient.of(Items.RABBIT_HIDE), 1, 2, folder + "rabbit_hide");
    this.materialRecipe(consumer, MaterialIds.vine, Ingredient.of(Items.VINE, Items.TWISTING_VINES, Items.WEEPING_VINES), 1, 1, folder + "vine");

    // tier 2
    this.metalMaterialRecipe(consumer, MaterialIds.iron, folder, "iron", false);
    this.materialRecipe(consumer, MaterialIds.searedStone, Ingredient.of(TinkerSmeltery.searedBrick), 1, 2, folder + "seared_stone/brick");
    this.materialRecipe(consumer, MaterialIds.searedStone, Ingredient.of(TinkerTags.Items.SEARED_BLOCKS), 2, 1, ItemOutput.fromItem(TinkerSmeltery.searedBrick), folder + "seared_stone/block");
    this.materialRecipe(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerSmeltery.scorchedBrick), 1, 2, folder + "scorched_stone/brick");
    this.materialRecipe(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerTags.Items.SCORCHED_BLOCKS), 2, 1, ItemOutput.fromItem(TinkerSmeltery.scorchedBrick), folder + "scorched_stone/block");
    this.materialRecipe(consumer, MaterialIds.bloodbone, Ingredient.of(TinkerMaterials.bloodbone), 1, 1, folder + "bloodbone");
    this.metalMaterialRecipe(consumer, MaterialIds.roseGold, folder, "rose_gold", false);
    this.materialRecipe(consumer, MaterialIds.necroticBone, Ingredient.of(TinkerTags.Items.WITHER_BONES), 1, 1, folder + "necrotic_bone");
    this.materialRecipe(consumer, MaterialIds.endstone, Ingredient.of(Tags.Items.END_STONES), 1, 2, folder + "endstone");

    this.materialRecipe(consumer, MaterialIds.chain, Ingredient.of(Blocks.CHAIN), 1, 1, folder + "chain");
    this.materialRecipe(consumer, MaterialIds.skyslimeVine, Ingredient.of(TinkerWorld.skySlimeVine), 1, 1, folder + "skyslime_vine");
    // slimewood
    this.materialRecipe(consumer, MaterialIds.greenheart, Ingredient.of(TinkerWorld.greenheart), 1, 1, folder + "slimewood/greenheart_planks");
    this.materialRecipe(consumer, MaterialIds.skyroot, Ingredient.of(TinkerWorld.skyroot), 1, 1, folder + "slimewood/skyroot_planks");
    this.materialRecipe(consumer, MaterialIds.bloodshroom, Ingredient.of(TinkerWorld.bloodshroom), 1, 1, folder + "slimewood/bloodshroom_planks");
    this.materialRecipe(consumer, MaterialIds.greenheart, Ingredient.of(TinkerWorld.greenheart.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.greenheart), folder + "slimewood/greenheart_logs");
    this.materialRecipe(consumer, MaterialIds.skyroot, Ingredient.of(TinkerWorld.skyroot.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.skyroot), folder + "slimewood/skyroot_logs");
    this.materialRecipe(consumer, MaterialIds.bloodshroom, Ingredient.of(TinkerWorld.bloodshroom.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.bloodshroom), folder + "slimewood/bloodshroom_logs");

    // tier 3
    this.metalMaterialRecipe(consumer, MaterialIds.slimesteel, folder, "slimesteel", false);
    this.materialRecipe(consumer, MaterialIds.nahuatl, Ingredient.of(TinkerMaterials.nahuatl), 1, 1, folder + "nahuatl");
    this.metalMaterialRecipe(consumer, MaterialIds.amethystBronze, folder, "amethyst_bronze", false);
    this.metalMaterialRecipe(consumer, MaterialIds.pigIron, folder, "pig_iron", false);

    // tier 2 (nether)
    // tier 3 (nether)
    this.metalMaterialRecipe(consumer, MaterialIds.cobalt, folder, "cobalt", false);
    // tier 4
    this.metalMaterialRecipe(consumer, MaterialIds.queensSlime, folder, "queens_slime", false);
    this.metalMaterialRecipe(consumer, MaterialIds.manyullyn, folder, "manyullyn", false);
    this.metalMaterialRecipe(consumer, MaterialIds.hepatizon, folder, "hepatizon", false);
    this.materialRecipe(consumer, MaterialIds.blazingBone, Ingredient.of(TinkerMaterials.blazingBone), 1, 1, folder + "blazing_bone");
    //registerMetalMaterial(consumer, MaterialIds.soulsteel,   "soulsteel",    false);

    // tier 5
    this.materialRecipe(consumer, MaterialIds.enderslimeVine, Ingredient.of(TinkerWorld.enderSlimeVine), 1, 1, folder + "enderslime_vine");

    // tier 2 (mod compat)
    this.metalMaterialRecipe(consumer, MaterialIds.osmium, folder, "osmium", true);
    this.metalMaterialRecipe(consumer, MaterialIds.tungsten, folder, "tungsten", true);
    this.metalMaterialRecipe(consumer, MaterialIds.platinum, folder, "platinum", true);
    this.metalMaterialRecipe(consumer, MaterialIds.silver, folder, "silver", true);
    this.metalMaterialRecipe(consumer, MaterialIds.lead, folder, "lead", true);
    // no whitestone, use repair kits
    // tier 3 (mod integration)
    this.metalMaterialRecipe(consumer, MaterialIds.steel, folder, "steel", true);
    this.metalMaterialRecipe(consumer, MaterialIds.bronze, folder, "bronze", true);
    this.metalMaterialRecipe(consumer, MaterialIds.constantan, folder, "constantan", true);
    this.metalMaterialRecipe(consumer, MaterialIds.invar, folder, "invar", true);
    this.materialRecipe(this.withCondition(consumer, this.tagCondition("uranium_ingots")), MaterialIds.necronium, Ingredient.of(TinkerMaterials.necroniumBone), 1, 1, folder + "necronium");
    this.metalMaterialRecipe(consumer, MaterialIds.electrum, folder, "electrum", true);
    // no plated slimewood, use repair kits

    // slimeskull
    this.metalMaterialRecipe(consumer, MaterialIds.gold, folder, "gold", false);
    this.materialRecipe(consumer, MaterialIds.glass, Ingredient.of(Tags.Items.GLASS), 1, 1, folder + "glass");
    this.materialRecipe(consumer, MaterialIds.glass, Ingredient.of(Tags.Items.GLASS_PANES), 1, 4, folder + "glass_pane");
    this.materialRecipe(consumer, MaterialIds.enderPearl, Ingredient.of(Tags.Items.ENDER_PEARLS), 1, 1, folder + "ender_pearl");
    this.materialRecipe(consumer, MaterialIds.rottenFlesh, Ingredient.of(Items.ROTTEN_FLESH), 1, 1, folder + "rotten_flesh");
    // slimesuit
    this.materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerCommons.slimeball.get(SlimeType.ENDER)), 1, 1, folder + "enderslime/ball");
    this.materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.ENDER)), 4, 1, folder + "enderslime/congealed");
    this.materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerWorld.slime.get(SlimeType.ENDER)), 9, 1, folder + "enderslime/block");
    this.materialRecipe(consumer, MaterialIds.phantom, Ingredient.of(Items.PHANTOM_MEMBRANE), 1, 1, folder + "phantom_membrane");
  }

  private void addMaterialSmeltery(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/materials/";

    // melting and casting
    // tier 2
    this.materialMeltingCasting(consumer, MaterialIds.iron, TinkerFluids.moltenIron, true, folder);
    this.materialMeltingCasting(consumer, MaterialIds.copper, TinkerFluids.moltenCopper, true, folder);
    this.materialMeltingCasting(consumer, MaterialIds.searedStone, TinkerFluids.searedStone, false, FluidValues.BRICK * 2, folder);
    this.materialMeltingCasting(consumer, MaterialIds.scorchedStone, TinkerFluids.scorchedStone, false, FluidValues.BRICK * 2, folder);
    this.materialMelting(consumer, MaterialIds.chain, TinkerFluids.moltenIron.get(), FluidValues.INGOT + (FluidValues.NUGGET * 2), folder);
    // half a clay is 1 seared brick per grout amounts
    this.materialComposite(consumer, MaterialIds.rock, MaterialIds.searedStone, TinkerFluids.moltenClay, false, FluidValues.BRICK, folder);
    this.materialComposite(consumer, MaterialIds.wood, MaterialIds.slimewoodComposite, TinkerFluids.earthSlime, true, FluidValues.SLIMEBALL, folder);
    this.materialComposite(consumer, MaterialIds.flint, MaterialIds.scorchedStone, TinkerFluids.magma, true, FluidValues.SLIMEBALL, folder);
    this.materialComposite(consumer, MaterialIds.bone, MaterialIds.bloodbone, TinkerFluids.blood, false, FluidValues.SLIMEBALL, folder);
    // oxidize copper and iron via water, it does not rust iron because magic
    MaterialFluidRecipeBuilder.material(MaterialIds.oxidizedIron)
      .setInputId(MaterialIds.iron)
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE)
      .setTemperature(1)
      .save(consumer, this.modResource(folder + "composite/iron_oxidized"));
    MaterialFluidRecipeBuilder.material(MaterialIds.oxidizedCopper)
      .setInputId(MaterialIds.copper)
      .setFluid(MantleTags.Fluids.WATER, FluidValues.BOTTLE)
      .setTemperature(1)
      .save(consumer, this.modResource(folder + "composite/copper_oxidized"));

    // tier 3
    this.materialMeltingCasting(consumer, MaterialIds.slimesteel, TinkerFluids.moltenSlimesteel, false, folder);
    this.materialMeltingCasting(consumer, MaterialIds.amethystBronze, TinkerFluids.moltenAmethystBronze, false, folder);
    this.materialMeltingCasting(consumer, MaterialIds.roseGold, TinkerFluids.moltenRoseGold, true, folder);
    this.materialMeltingCasting(consumer, MaterialIds.pigIron, TinkerFluids.moltenPigIron, false, folder);
    this.materialMeltingCasting(consumer, MaterialIds.cobalt, TinkerFluids.moltenCobalt, true, folder);
    this.materialMeltingComposite(consumer, MaterialIds.wood, MaterialIds.nahuatl, TinkerFluids.moltenObsidian, false, FluidValues.GLASS_BLOCK, folder);
    this.materialMeltingComposite(consumer, MaterialIds.string, MaterialIds.darkthread, TinkerFluids.moltenObsidian, false, FluidValues.GLASS_PANE, folder);

    // tier 4
    this.materialMeltingCasting(consumer, MaterialIds.queensSlime, TinkerFluids.moltenQueensSlime, false, folder);
    this.materialMeltingCasting(consumer, MaterialIds.hepatizon, TinkerFluids.moltenHepatizon, true, folder);
    this.materialMeltingCasting(consumer, MaterialIds.manyullyn, TinkerFluids.moltenManyullyn, true, folder);
    this.materialComposite(consumer, MaterialIds.necroticBone, MaterialIds.blazingBone, TinkerFluids.blazingBlood, false, FluidConstants.BUCKET / 5, folder);
    this.materialMeltingComposite(consumer, MaterialIds.leather, MaterialIds.ancientHide, TinkerFluids.moltenDebris, false, FluidValues.INGOT, folder);

    // tier 2 compat
    this.compatMeltingCasting(consumer, MaterialIds.osmium, TinkerFluids.moltenOsmium, folder);
    this.compatMeltingCasting(consumer, MaterialIds.tungsten, TinkerFluids.moltenTungsten, folder);
    this.compatMeltingCasting(consumer, MaterialIds.platinum, TinkerFluids.moltenPlatinum, folder);
    this.compatMeltingCasting(consumer, MaterialIds.silver, TinkerFluids.moltenSilver, folder);
    this.compatMeltingCasting(consumer, MaterialIds.lead, TinkerFluids.moltenLead, folder);
    this.compatMeltingCasting(consumer, MaterialIds.aluminum, TinkerFluids.moltenAluminum, folder);
    this.materialComposite(this.withCondition(consumer, this.tagCondition("aluminum_ingots")), MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenAluminum, true, FluidValues.INGOT, folder, "whitestone_from_aluminum");
    this.materialComposite(this.withCondition(consumer, this.tagCondition("tin_ingots")), MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenTin, true, FluidValues.INGOT, folder, "whitestone_from_tin");
    this.materialComposite(this.withCondition(consumer, this.tagCondition("zinc_ingots")), MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenZinc, true, FluidValues.INGOT, folder, "whitestone_from_zinc");
    // tier 3 compat
    this.compatMeltingCasting(consumer, MaterialIds.steel, TinkerFluids.moltenSteel, folder);
    this.compatMeltingCasting(consumer, MaterialIds.constantan, TinkerFluids.moltenConstantan, folder);
    this.compatMeltingCasting(consumer, MaterialIds.invar, TinkerFluids.moltenInvar, folder);
    this.compatMeltingCasting(consumer, MaterialIds.electrum, TinkerFluids.moltenElectrum, folder);
    this.compatMeltingCasting(consumer, MaterialIds.bronze, TinkerFluids.moltenBronze, folder);
    this.materialMeltingComposite(this.withCondition(consumer, this.tagCondition("uranium_ingots")), MaterialIds.necroticBone, MaterialIds.necronium, TinkerFluids.moltenUranium, true, FluidValues.INGOT, folder);
    this.materialMeltingComposite(this.withCondition(consumer, this.tagCondition("brass_ingots")), MaterialIds.slimewood, MaterialIds.platedSlimewood, TinkerFluids.moltenBrass, true, FluidValues.INGOT, folder);

    // slimesuit
    this.materialMeltingCasting(consumer, MaterialIds.gold, TinkerFluids.moltenGold, true, folder);
    this.materialMeltingCasting(consumer, MaterialIds.enderPearl, TinkerFluids.moltenEnder, true, FluidValues.SLIMEBALL, folder);
    this.materialMeltingCasting(consumer, MaterialIds.glass, TinkerFluids.moltenGlass, false, FluidValues.GLASS_BLOCK, folder);
    this.materialMeltingCasting(consumer, MaterialIds.enderslime, TinkerFluids.enderSlime, FluidValues.SLIMEBALL, folder);
    //materialMeltingCasting(consumer, MaterialIds.venom, TinkerFluids.venom, FluidConstants.BUCKET / 4, folder);
  }
}
