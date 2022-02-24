package slimeknights.tconstruct.tools.data.material;

import me.alphamode.forgetags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.lib.transfer.fluid.FluidAttributes;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.IngredientDifference;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.data.recipe.IMaterialRecipeHelper;
import slimeknights.tconstruct.library.json.TagDifferencePresentCondition;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.function.Consumer;

public class MaterialRecipeProvider extends BaseRecipeProvider implements IMaterialRecipeHelper {
  public MaterialRecipeProvider(FabricDataGenerator generator) {
    super(generator);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Material Recipe";
  }

  @Override
  protected void generateRecipes(Consumer<FinishedRecipe> consumer) {
    addMaterialItems(consumer);
    addMaterialSmeltery(consumer);
  }

  private void addMaterialItems(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/materials/";
    // tier 1
    materialRecipe(consumer, MaterialIds.wood, Ingredient.of(Tags.Items.RODS_WOODEN), 1, 2, folder + "wood/sticks");
    // planks
    materialRecipe(consumer, MaterialIds.oak,     Ingredient.of(Items.OAK_PLANKS),      1, 1, folder + "wood/planks/oak");
    materialRecipe(consumer, MaterialIds.spruce,  Ingredient.of(Items.SPRUCE_PLANKS),   1, 1, folder + "wood/planks/spruce");
    materialRecipe(consumer, MaterialIds.birch,   Ingredient.of(Items.BIRCH_PLANKS),    1, 1, folder + "wood/planks/birch");
    materialRecipe(consumer, MaterialIds.jungle,  Ingredient.of(Items.JUNGLE_PLANKS),   1, 1, folder + "wood/planks/jungle");
    materialRecipe(consumer, MaterialIds.darkOak, Ingredient.of(Items.DARK_OAK_PLANKS), 1, 1, folder + "wood/planks/dark_oak");
    materialRecipe(consumer, MaterialIds.acacia,  Ingredient.of(Items.ACACIA_PLANKS),   1, 1, folder + "wood/planks/acacia");
    materialRecipe(consumer, MaterialIds.crimson, Ingredient.of(Items.CRIMSON_PLANKS),  1, 1, folder + "wood/planks/crimson");
    materialRecipe(consumer, MaterialIds.warped,  Ingredient.of(Items.WARPED_PLANKS),   1, 1, folder + "wood/planks/warped");
    materialRecipe(withCondition(consumer, new TagDifferencePresentCondition(ItemTags.PLANKS.getName(), TinkerTags.Items.VARIANT_PLANKS.getName())), MaterialIds.wood,
                   IngredientDifference.difference(Ingredient.of(ItemTags.PLANKS), Ingredient.of(TinkerTags.Items.VARIANT_PLANKS)), 1, 1, folder + "wood/planks/default");
    // logs
    materialRecipe(consumer, MaterialIds.oak,     Ingredient.of(ItemTags.OAK_LOGS),      4, 1, ItemOutput.fromStack(new ItemStack(Blocks.OAK_PLANKS)),      folder + "wood/logs/oak");
    materialRecipe(consumer, MaterialIds.spruce,  Ingredient.of(ItemTags.SPRUCE_LOGS),   4, 1, ItemOutput.fromStack(new ItemStack(Blocks.SPRUCE_PLANKS)),   folder + "wood/logs/spruce");
    materialRecipe(consumer, MaterialIds.birch,   Ingredient.of(ItemTags.BIRCH_LOGS),    4, 1, ItemOutput.fromStack(new ItemStack(Blocks.BIRCH_PLANKS)),    folder + "wood/logs/birch");
    materialRecipe(consumer, MaterialIds.jungle,  Ingredient.of(ItemTags.JUNGLE_LOGS),   4, 1, ItemOutput.fromStack(new ItemStack(Blocks.JUNGLE_PLANKS)),   folder + "wood/logs/jungle");
    materialRecipe(consumer, MaterialIds.darkOak, Ingredient.of(ItemTags.DARK_OAK_LOGS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.DARK_OAK_PLANKS)), folder + "wood/logs/dark_oak");
    materialRecipe(consumer, MaterialIds.acacia,  Ingredient.of(ItemTags.ACACIA_LOGS),   4, 1, ItemOutput.fromStack(new ItemStack(Blocks.ACACIA_PLANKS)),   folder + "wood/logs/acacia");
    materialRecipe(consumer, MaterialIds.crimson, Ingredient.of(ItemTags.CRIMSON_STEMS), 4, 1, ItemOutput.fromStack(new ItemStack(Blocks.CRIMSON_PLANKS)),  folder + "wood/logs/crimson");
    materialRecipe(consumer, MaterialIds.warped,  Ingredient.of(ItemTags.WARPED_STEMS),  4, 1, ItemOutput.fromStack(new ItemStack(Blocks.WARPED_PLANKS)),   folder + "wood/logs/warped");
    materialRecipe(withCondition(consumer, new TagDifferencePresentCondition(ItemTags.LOGS.getName(), TinkerTags.Items.VARIANT_LOGS.getName())), MaterialIds.wood,
                   IngredientDifference.difference(Ingredient.of(ItemTags.LOGS), Ingredient.of(TinkerTags.Items.VARIANT_LOGS)), 4, 1,
                   ItemOutput.fromStack(new ItemStack(Items.STICK, 2)), folder + "wood/logs/default");
    // stone
    materialRecipe(consumer, MaterialIds.stone,      Ingredient.of(Blocks.STONE, Blocks.COBBLESTONE, Blocks.MOSSY_COBBLESTONE), 1, 1, folder + "rock/stone");
    materialRecipe(consumer, MaterialIds.andesite,   Ingredient.of(Blocks.ANDESITE),                                            1, 1, folder + "rock/andesite");
    materialRecipe(consumer, MaterialIds.diorite,    Ingredient.of(Blocks.DIORITE),                                             1, 1, folder + "rock/diorite");
    materialRecipe(consumer, MaterialIds.granite,    Ingredient.of(Blocks.GRANITE),                                             1, 1, folder + "rock/granite");
    materialRecipe(consumer, MaterialIds.deepslate,  Ingredient.of(Blocks.DEEPSLATE, Blocks.COBBLED_DEEPSLATE),                 1, 1, folder + "rock/deepslate");
    materialRecipe(consumer, MaterialIds.blackstone, Ingredient.of(Blocks.BLACKSTONE),                                          1, 1, folder + "rock/blackstone");
    // TODO: flint and basalt
    materialRecipe(consumer, MaterialIds.flint, Ingredient.of(Items.FLINT, Blocks.BASALT, Blocks.POLISHED_BASALT), 1, 1, folder + "flint");
    // other tier 1
    materialRecipe(consumer, MaterialIds.bone, Ingredient.of(Tags.Items.BONES), 1, 1, folder + "bone");
    materialRecipe(consumer, MaterialIds.necroticBone, Ingredient.of(TinkerTags.Items.WITHER_BONES), 1, 1, folder + "necrotic_bone");
    materialRecipe(consumer, MaterialIds.string, Ingredient.of(Tags.Items.STRING), 1, 4, folder + "string");
    materialRecipe(consumer, MaterialIds.leather, Ingredient.of(Tags.Items.LEATHER), 1, 1, folder + "leather");
    materialRecipe(consumer, MaterialIds.vine, Ingredient.of(Items.VINE, Items.TWISTING_VINES, Items.WEEPING_VINES), 1, 1, folder + "vine");

    // tier 2
    metalMaterialRecipe(consumer, MaterialIds.iron, folder, "iron", false);
    materialRecipe(consumer, MaterialIds.searedStone, Ingredient.of(TinkerSmeltery.searedBrick), 1, 2, folder + "seared_stone/brick");
    materialRecipe(consumer, MaterialIds.searedStone, Ingredient.of(TinkerTags.Items.SEARED_BLOCKS),     2, 1, ItemOutput.fromItem(TinkerSmeltery.searedBrick), folder + "seared_stone/block");
    materialRecipe(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerSmeltery.scorchedBrick),     1, 2, folder + "scorched_stone/brick");
    materialRecipe(consumer, MaterialIds.scorchedStone, Ingredient.of(TinkerTags.Items.SCORCHED_BLOCKS), 2, 1, ItemOutput.fromItem(TinkerSmeltery.scorchedBrick), folder + "scorched_stone/block");
    metalMaterialRecipe(consumer, MaterialIds.copper, folder, "copper", false);
    materialRecipe(consumer, MaterialIds.bloodbone, Ingredient.of(TinkerMaterials.bloodbone), 1, 1, folder + "bloodbone");
    metalMaterialRecipe(consumer, MaterialIds.roseGold, folder, "rose_gold", false);
    materialRecipe(consumer, MaterialIds.chain, Ingredient.of(Blocks.CHAIN), 1, 1, folder + "chain");
    materialRecipe(consumer, MaterialIds.skyslimeVine, Ingredient.of(TinkerWorld.skySlimeVine), 1, 1, folder + "skyslime_vine");
    // slimewood
    materialRecipe(consumer, MaterialIds.greenheart,  Ingredient.of(TinkerWorld.greenheart),  1, 1, folder + "slimewood/greenheart_planks");
    materialRecipe(consumer, MaterialIds.skyroot,     Ingredient.of(TinkerWorld.skyroot),     1, 1, folder + "slimewood/skyroot_planks");
    materialRecipe(consumer, MaterialIds.bloodshroom, Ingredient.of(TinkerWorld.bloodshroom), 1, 1, folder + "slimewood/bloodshroom_planks");
    materialRecipe(consumer, MaterialIds.greenheart,  Ingredient.of(TinkerWorld.greenheart.getLogItemTag()),  4, 1, ItemOutput.fromItem(TinkerWorld.greenheart), folder + "slimewood/greenheart_logs");
    materialRecipe(consumer, MaterialIds.skyroot,     Ingredient.of(TinkerWorld.skyroot.getLogItemTag()),     4, 1, ItemOutput.fromItem(TinkerWorld.skyroot),     folder + "slimewood/skyroot_logs");
    materialRecipe(consumer, MaterialIds.bloodshroom, Ingredient.of(TinkerWorld.bloodshroom.getLogItemTag()), 4, 1, ItemOutput.fromItem(TinkerWorld.bloodshroom), folder + "slimewood/bloodshroom_logs");

    // tier 3
    metalMaterialRecipe(consumer, MaterialIds.slimesteel, folder, "slimesteel", false);
    materialRecipe(consumer, MaterialIds.nahuatl, Ingredient.of(TinkerMaterials.nahuatl), 1, 1, folder + "nahuatl");
    metalMaterialRecipe(consumer, MaterialIds.siliconBronze, folder, "silicon_bronze", false);
    metalMaterialRecipe(consumer, MaterialIds.pigIron, folder, "pig_iron", false);

    // tier 2 (nether)
    // tier 3 (nether)
    metalMaterialRecipe(consumer, MaterialIds.cobalt, folder, "cobalt", false);
    // tier 4
    metalMaterialRecipe(consumer, MaterialIds.queensSlime, folder, "queens_slime", false);
    metalMaterialRecipe(consumer, MaterialIds.manyullyn, folder, "manyullyn", false);
    metalMaterialRecipe(consumer, MaterialIds.hepatizon, folder, "hepatizon", false);
    materialRecipe(consumer, MaterialIds.blazingBone, Ingredient.of(TinkerMaterials.blazingBone), 1, 1, folder + "blazing_bone");
    //registerMetalMaterial(consumer, MaterialIds.soulsteel,   "soulsteel",    false);

    // tier 5
    materialRecipe(consumer, MaterialIds.enderslimeVine, Ingredient.of(TinkerWorld.enderSlimeVine), 1, 1, folder + "enderslime_vine");

    // tier 2 (mod compat)
    metalMaterialRecipe(consumer, MaterialIds.osmium, folder, "osmium", true);
    metalMaterialRecipe(consumer, MaterialIds.tungsten, folder, "tungsten", true);
    metalMaterialRecipe(consumer, MaterialIds.platinum, folder, "platinum", true);
    metalMaterialRecipe(consumer, MaterialIds.silver, folder, "silver", true);
    metalMaterialRecipe(consumer, MaterialIds.lead, folder, "lead", true);
    // no whitestone, use repair kits
    // tier 3 (mod integration)
    metalMaterialRecipe(consumer, MaterialIds.steel, folder, "steel", true);
    metalMaterialRecipe(consumer, MaterialIds.tinBronze, folder, "bronze", true);
    metalMaterialRecipe(consumer, MaterialIds.constantan, folder, "constantan", true);
    metalMaterialRecipe(consumer, MaterialIds.invar, folder, "invar", true);
    materialRecipe(withCondition(consumer, tagCondition("ingots/uranium")), MaterialIds.necronium, Ingredient.of(TinkerMaterials.necroniumBone), 1, 1, folder + "necronium");
    metalMaterialRecipe(consumer, MaterialIds.electrum, folder, "electrum", true);
    // no plated slimewood, use repair kits

    // slimeskull
    metalMaterialRecipe(consumer, MaterialIds.gold, folder, "gold", false);
    materialRecipe(consumer, MaterialIds.gunpowder,   Ingredient.of(Tags.Items.GUNPOWDER),                      1, 2, folder + "gunpowder");
    materialRecipe(consumer, MaterialIds.enderPearl,  Ingredient.of(Tags.Items.ENDER_PEARLS),                   1, 1, folder + "ender_pearl");
    materialRecipe(consumer, MaterialIds.spider,      Ingredient.of(Items.SPIDER_EYE),                        1, 1, folder + "spider");
    materialRecipe(consumer, MaterialIds.venom,       FluidContainerIngredient.fromFluid(TinkerFluids.venom, false), 4, 1, folder + "venom_bucket");
    materialRecipe(consumer, MaterialIds.rottenFlesh, Ingredient.of(Items.ROTTEN_FLESH),                      1, 1, folder + "rotten_flesh");
    // slimesuit
    materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerCommons.slimeball.get(SlimeType.ENDER)),    1, 1, folder + "enderslime/ball");
    materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerWorld.congealedSlime.get(SlimeType.ENDER)), 4, 1, folder + "enderslime/congealed");
    materialRecipe(consumer, MaterialIds.enderslime, Ingredient.of(TinkerWorld.slime.get(SlimeType.ENDER)),          9, 1, folder + "enderslime/block");
    materialRecipe(consumer, MaterialIds.phantom,    Ingredient.of(Items.PHANTOM_MEMBRANE),    1, 1, folder + "phantom_membrane");
    materialRecipe(consumer, MaterialIds.chorus,     Ingredient.of(Items.POPPED_CHORUS_FRUIT), 1, 1, folder + "chorus_popped");
    materialRecipe(consumer, MaterialIds.rabbit,     Ingredient.of(Items.RABBIT_HIDE),         1, 1, folder + "rabbit_hide");
  }

  private void addMaterialSmeltery(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/materials/";

    // melting and casting
    // tier 2
    materialMeltingCasting(consumer, MaterialIds.iron, TinkerFluids.moltenIron, true, folder);
    materialMeltingCasting(consumer, MaterialIds.copper,        TinkerFluids.moltenCopper,  true,  folder);
    materialMeltingCasting(consumer, MaterialIds.searedStone, TinkerFluids.searedStone, false, FluidValues.BRICK * 2, folder);
    materialMeltingCasting(consumer, MaterialIds.scorchedStone, TinkerFluids.scorchedStone, false, FluidValues.BRICK * 2, folder);
    materialMelting(consumer, MaterialIds.chain, TinkerFluids.moltenIron.get(), FluidValues.INGOT + (FluidValues.NUGGET * 2), folder);
    // half a clay is 1 seared brick per grout amounts
    materialComposite(consumer, MaterialIds.rock,  MaterialIds.searedStone,        TinkerFluids.moltenClay, false, FluidValues.BRICK,     folder);
    materialComposite(consumer, MaterialIds.wood,  MaterialIds.slimewoodComposite, TinkerFluids.earthSlime, true,  FluidValues.SLIMEBALL, folder);
    materialComposite(consumer, MaterialIds.flint, MaterialIds.scorchedStone,      TinkerFluids.magma,      true,  FluidValues.SLIMEBALL, folder);
    materialComposite(consumer, MaterialIds.bone,  MaterialIds.bloodbone,          TinkerFluids.blood,      false, FluidValues.SLIMEBALL, folder);

    // tier 3
    materialMeltingCasting(consumer, MaterialIds.slimesteel,    TinkerFluids.moltenSlimesteel,    false, folder);
    materialMeltingCasting(consumer, MaterialIds.siliconBronze, TinkerFluids.moltenTinkersBronze, false, folder);
    materialMeltingCasting(consumer, MaterialIds.roseGold,      TinkerFluids.moltenRoseGold,      true,  folder);
    materialMeltingCasting(consumer, MaterialIds.pigIron,       TinkerFluids.moltenPigIron,       false, folder);
    materialMeltingCasting(consumer, MaterialIds.cobalt,        TinkerFluids.moltenCobalt,        true,  folder);
    materialMeltingComposite(consumer, MaterialIds.wood, MaterialIds.nahuatl, TinkerFluids.moltenObsidian, false, FluidValues.GLASS_BLOCK, folder);

    // tier 4
    materialMeltingCasting(consumer, MaterialIds.queensSlime, TinkerFluids.moltenQueensSlime, false, folder);
    materialMeltingCasting(consumer, MaterialIds.hepatizon,   TinkerFluids.moltenHepatizon,   true,  folder);
    materialMeltingCasting(consumer, MaterialIds.manyullyn,   TinkerFluids.moltenManyullyn,   true,  folder);
    materialComposite(consumer, MaterialIds.necroticBone, MaterialIds.blazingBone, TinkerFluids.blazingBlood, false, FluidAttributes.BUCKET_VOLUME / 5, folder);

    // tier 2 compat
    compatMeltingCasting(consumer, MaterialIds.osmium,   TinkerFluids.moltenOsmium,   folder);
    compatMeltingCasting(consumer, MaterialIds.tungsten, TinkerFluids.moltenTungsten, folder);
    compatMeltingCasting(consumer, MaterialIds.platinum, TinkerFluids.moltenPlatinum, folder);
    compatMeltingCasting(consumer, MaterialIds.silver,   TinkerFluids.moltenSilver,   folder);
    compatMeltingCasting(consumer, MaterialIds.lead,     TinkerFluids.moltenLead,     folder);
    materialComposite(withCondition(consumer, tagCondition("ingots/aluminum")), MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenAluminum, true, FluidValues.INGOT, folder, "whitestone_from_aluminum");
    materialComposite(withCondition(consumer, tagCondition("ingots/tin")),      MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenTin,      true, FluidValues.INGOT, folder, "whitestone_from_tin");
    materialComposite(withCondition(consumer, tagCondition("ingots/zinc")),     MaterialIds.rock, MaterialIds.whitestone, TinkerFluids.moltenZinc,     true, FluidValues.INGOT, folder, "whitestone_from_zinc");
    // tier 3 compat
    compatMeltingCasting(consumer, MaterialIds.steel,          TinkerFluids.moltenSteel,      folder);
    compatMeltingCasting(consumer, MaterialIds.constantan,     TinkerFluids.moltenConstantan, folder);
    compatMeltingCasting(consumer, MaterialIds.invar,          TinkerFluids.moltenInvar,      folder);
    compatMeltingCasting(consumer, MaterialIds.electrum,       TinkerFluids.moltenElectrum,   folder);
    materialMeltingCasting(withCondition(consumer, tagCondition("ingots/bronze")), MaterialIds.tinBronze, TinkerFluids.moltenBronze, true, folder);
    materialMeltingComposite(withCondition(consumer, tagCondition("ingots/uranium")), MaterialIds.necroticBone, MaterialIds.necronium,       TinkerFluids.moltenUranium, true, FluidValues.INGOT, folder);
    materialMeltingComposite(withCondition(consumer, tagCondition("ingots/brass")),   MaterialIds.slimewood,    MaterialIds.platedSlimewood, TinkerFluids.moltenBrass,   true, FluidValues.INGOT, folder);

    // slimesuit
    materialMeltingCasting(consumer, MaterialIds.gold, TinkerFluids.moltenGold, true, folder);
    materialMeltingCasting(consumer, MaterialIds.enderslime, TinkerFluids.enderSlime, FluidValues.SLIMEBALL, folder);
    materialMeltingCasting(consumer, MaterialIds.venom, TinkerFluids.venom, FluidAttributes.BUCKET_VOLUME / 4, folder);
  }
}
