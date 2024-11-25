package slimeknights.tconstruct.tools.data;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import io.github.tropheusj.milk.Milk;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.recipe.v1.ingredient.DefaultCustomIngredients;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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
import net.minecraft.world.level.block.Blocks;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.recipe.data.ItemNameIngredient;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.EntityIngredient;
import slimeknights.mantle.recipe.ingredient.FluidContainerIngredient;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.mantle.recipe.ingredient.SizedIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.data.BaseRecipeProvider;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.library.data.recipe.SpecialRecipeBuilder;
import slimeknights.tconstruct.library.json.predicate.modifier.ModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.SlotTypeModifierPredicate;
import slimeknights.tconstruct.library.json.predicate.modifier.TagModifierPredicate;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.casting.ItemCastingRecipeBuilder;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierMatch;
import slimeknights.tconstruct.library.recipe.modifiers.adding.IncrementalModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.ModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.MultilevelModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.OverslimeModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.adding.SwappableModifierRecipeBuilder;
import slimeknights.tconstruct.library.recipe.modifiers.severing.SeveringRecipeBuilder;
import slimeknights.tconstruct.library.recipe.tinkerstation.repairing.ModifierMaterialRepairRecipeBuilder;
import slimeknights.tconstruct.library.recipe.tinkerstation.repairing.ModifierRepairRecipeBuilder;
import slimeknights.tconstruct.library.recipe.worktable.ModifierSetWorktableRecipeBuilder;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.module.interaction.DualOptionInteraction;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.item.ArmorSlotType;
import slimeknights.tconstruct.tools.recipe.ArmorDyeingRecipe;
import slimeknights.tconstruct.tools.recipe.EnchantmentConvertingRecipe;
import slimeknights.tconstruct.tools.recipe.ModifierRemovalRecipe;
import slimeknights.tconstruct.tools.recipe.ModifierSortingRecipe;
import slimeknights.tconstruct.world.TinkerHeadType;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

@SuppressWarnings("removal")
public class ModifierRecipeProvider extends BaseRecipeProvider {

  public ModifierRecipeProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Modifier Recipes";
  }

  @Override
  public void buildRecipes(Consumer<FinishedRecipe> consumer) {
    this.addItemRecipes(consumer);
    this.addModifierRecipes(consumer);
    this.addTextureRecipes(consumer);
    this.addHeadRecipes(consumer);
  }

  private void addItemRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/modifiers/";

    // reinforcements
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.ironReinforcement)
      .setFluidAndTime(TinkerFluids.moltenIron, true, FluidValues.NUGGET * 3)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.ironReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.slimesteelReinforcement)
      .setFluidAndTime(TinkerFluids.moltenSlimesteel, false, FluidValues.NUGGET * 3)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.slimesteelReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.searedReinforcement)
      .setFluid(FluidIngredient.of(FluidIngredient.of(TinkerFluids.searedStone.getLocalTag(), FluidValues.BRICK), FluidIngredient.of(TinkerFluids.scorchedStone.getLocalTag(), FluidValues.BRICK)))
      .setCoolingTime(FluidVariantAttributes.getTemperature(FluidVariant.of(TinkerFluids.searedStone.get())) - 300, FluidValues.BRICK)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.searedReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.goldReinforcement)
      .setFluidAndTime(TinkerFluids.moltenGold, true, FluidValues.NUGGET * 3)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.goldReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.emeraldReinforcement)
      .setFluidAndTime(TinkerFluids.moltenEmerald, false, FluidValues.GEM_SHARD)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.emeraldReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.bronzeReinforcement)
      .setFluidAndTime(TinkerFluids.moltenAmethystBronze, true, FluidValues.NUGGET * 3)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.bronzeReinforcement.getRegistryName(), folder));
    ItemCastingRecipeBuilder.tableRecipe(TinkerModifiers.cobaltReinforcement)
      .setFluidAndTime(TinkerFluids.moltenCobalt, true, FluidValues.NUGGET * 3)
      .setCast(TinkerCommons.obsidianPane, true)
      .save(consumer, this.prefix(TinkerModifiers.cobaltReinforcement.getRegistryName(), folder));

    // jeweled apple
    ItemCastingRecipeBuilder.tableRecipe(TinkerCommons.jeweledApple)
      .setFluidAndTime(TinkerFluids.moltenDiamond, false, FluidValues.GEM * 4)
      .setCast(Items.APPLE, true)
      .save(consumer, this.prefix(TinkerCommons.jeweledApple.getRegistryName(), folder));

    // silky cloth
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TinkerModifiers.silkyCloth)
      .define('s', Tags.Items.STRING)
      .define('g', TinkerMaterials.roseGold.getIngotTag())
      .pattern("sss")
      .pattern("sgs")
      .pattern("sss")
      .unlockedBy("has_item", has(Tags.Items.INGOTS_GOLD))
      .save(consumer, this.prefix(TinkerModifiers.silkyCloth.getRegistryName(), folder));

    // wither bone purifying
    ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.BONE)
      .requires(TinkerTags.Items.WITHER_BONES)
      .unlockedBy("has_bone", has(TinkerTags.Items.WITHER_BONES))
      .save(this.withCondition(consumer, ConfigEnabledCondition.WITHER_BONE_CONVERSION), this.modResource(folder + "wither_bone_conversion"));

    // modifier repair
    // stringy - from string
    ModifierMaterialRepairRecipeBuilder.repair(ModifierIds.stringy, MaterialIds.string)
      .saveCraftingTable(consumer, this.wrap(ModifierIds.stringy, folder, "_crafting_table"))
      .save(consumer, this.wrap(ModifierIds.stringy, folder, "_tinker_station"));
    // pig iron - from bacon, only in the tinker station
    ModifierRepairRecipeBuilder.repair(TinkerModifiers.tasty, Ingredient.of(TinkerCommons.bacon), 25)
      .save(consumer, this.prefix(TinkerModifiers.tasty, folder));
    // golden makes armor repair from gold
    ModifierMaterialRepairRecipeBuilder.repair(TinkerModifiers.golden, MaterialIds.gold)
      .saveCraftingTable(consumer, this.wrap(TinkerModifiers.golden, folder, "_crafting_table"))
      .save(consumer, this.wrap(TinkerModifiers.golden, folder, "_tinker_station"));
  }

  private void addModifierRecipes(Consumer<FinishedRecipe> consumer) {
    // upgrades
    String upgradeFolder = "tools/modifiers/upgrade/";
    String abilityFolder = "tools/modifiers/ability/";
    String slotlessFolder = "tools/modifiers/slotless/";
    String upgradeSalvage = "tools/modifiers/salvage/upgrade/";
    String abilitySalvage = "tools/modifiers/salvage/ability/";
    String defenseFolder = "tools/modifiers/defense/";
    String defenseSalvage = "tools/modifiers/salvage/defense/";
    String compatFolder = "tools/modifiers/compat/";
    String compatSalvage = "tools/modifiers/salvage/compat/";
    String worktableFolder = "tools/modifiers/worktable/";

    /*
     * durability
     */
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.reinforced)
      .setInput(TinkerModifiers.ironReinforcement, 1, 20)
      .setMaxLevel(5) // max 75% resistant to damage
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.DURABILITY)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.reinforced, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.reinforced, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.overforced)
      .setInput(TinkerModifiers.slimesteelReinforcement, 1, 20)
      .setMaxLevel(5) // +250 capacity
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.DURABILITY)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.overforced, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.overforced, upgradeFolder));
    // gems are special, I'd like them to be useful on all types of tools
    ModifierRecipeBuilder.modifier(ModifierIds.emerald)
      .setTools(TinkerTags.Items.DURABILITY)
      .addInput(Tags.Items.GEMS_EMERALD)
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.emerald, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.emerald, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.diamond)
      .setTools(TinkerTags.Items.DURABILITY)
      .addInput(Tags.Items.GEMS_DIAMOND)
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.diamond, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.diamond, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.worldbound)
      .addInput(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.worldbound, slotlessFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.soulbound)
      .addInput(Ingredient.of(Items.TOTEM_OF_UNDYING, Items.NETHER_STAR))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.soulbound, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.soulbound, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.netherite)
      .setTools(TinkerTags.Items.DURABILITY)
      .addInput(Tags.Items.INGOTS_NETHERITE)
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .setRequirements(ModifierMatch.tag(TinkerTags.Modifiers.GEMS))
      .setRequirementsError(makeRequirementsError("netherite_requirements"))
      .saveSalvage(consumer, this.prefix(ModifierIds.netherite, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.netherite, upgradeFolder));

    // overslime - earth
    OverslimeModifierRecipeBuilder.modifier(TinkerCommons.slimeball.get(SlimeType.EARTH), 10)
      .save(consumer, this.modResource(slotlessFolder + "overslime/earth_ball"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 45)
      .save(consumer, this.modResource(slotlessFolder + "overslime/earth_congealed"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.slime.get(SlimeType.EARTH), 108)
      .save(consumer, this.modResource(slotlessFolder + "overslime/earth_block"));
    // sky
    OverslimeModifierRecipeBuilder.modifier(TinkerCommons.slimeball.get(SlimeType.SKY), 40)
      .save(consumer, this.modResource(slotlessFolder + "overslime/sky_ball"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.congealedSlime.get(SlimeType.SKY), 180)
      .save(consumer, this.modResource(slotlessFolder + "overslime/sky_congealed"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.slime.get(SlimeType.SKY), 432)
      .save(consumer, this.modResource(slotlessFolder + "overslime/sky_block"));
    // ichor
    OverslimeModifierRecipeBuilder.modifier(TinkerCommons.slimeball.get(SlimeType.ICHOR), 100)
      .save(consumer, this.modResource(slotlessFolder + "overslime/ichor_ball"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.congealedSlime.get(SlimeType.ICHOR), 450)
      .save(consumer, this.modResource(slotlessFolder + "overslime/ichor_congealed"));
    OverslimeModifierRecipeBuilder.modifier(TinkerWorld.slime.get(SlimeType.ICHOR), 1080)
      .save(consumer, this.modResource(slotlessFolder + "overslime/ichor_block"));

    /*
     * general effects
     */
    ModifierRecipeBuilder.modifier(TinkerModifiers.experienced)
      .addInput(Items.EXPERIENCE_BOTTLE)
      .addInput(Items.EXPERIENCE_BOTTLE)
      .addInput(Items.EXPERIENCE_BOTTLE)
      .setMaxLevel(5) // max +250%
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_OR_UNARMED, TinkerTags.Items.HARVEST, TinkerTags.Items.RANGED, TinkerTags.Items.LEGGINGS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.experienced, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.experienced, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.magnetic)
      .addInput(Items.COMPASS)
      .setMaxLevel(5)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST))
      .save(consumer, this.prefix(TinkerModifiers.magnetic, upgradeFolder));
    // armor has a max level of 1 per piece, so 4 total
    ModifierRecipeBuilder.modifier(TinkerModifiers.magnetic)
      .addInput(Items.COMPASS)
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.WORN_ARMOR) // TODO: reconsider for shields
      .save(consumer, this.wrap(TinkerModifiers.magnetic, upgradeFolder, "_armor"));
    // salvage supports either
    ModifierRecipeBuilder.modifier(TinkerModifiers.magnetic)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST, TinkerTags.Items.WORN_ARMOR))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.magnetic, upgradeSalvage));
    // no salvage so we can potentially grant shiny in another way without being an apple farm, and no recipe as that leaves nothing to salvage
    ModifierRecipeBuilder.modifier(ModifierIds.shiny)
      .addInput(Items.ENCHANTED_GOLDEN_APPLE)
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.shiny, slotlessFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.farsighted)
      .setTools(TinkerTags.Items.MODIFIABLE)
      .setInput(Tags.Items.CROPS_CARROT, 1, 45)
      .save(consumer, this.prefix(TinkerModifiers.farsighted, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.nearsighted)
      .setTools(TinkerTags.Items.MODIFIABLE)
      .setInput(Items.INK_SAC, 1, 45)
      .save(consumer, this.prefix(TinkerModifiers.nearsighted, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.offhanded)
      .setTools(TinkerTags.Items.INTERACTABLE_RIGHT)
      .addInput(Items.LEATHER)
      .addInput(Items.FIRE_CHARGE)
      .addInput(SlimeType.ICHOR.getSlimeballTag())
      .setMaxLevel(2)
      .save(consumer, this.prefix(TinkerModifiers.offhanded, upgradeFolder));

    /*
     * Speed
     */

    // haste can use redstone or blocks
    this.hasteRecipes(consumer, TinkerModifiers.haste.getId(), ingredientFromTags(TinkerTags.Items.HARVEST, TinkerTags.Items.CHESTPLATES), 5, upgradeFolder, upgradeSalvage);
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.blasting)
      .setTools(TinkerTags.Items.STONE_HARVEST)
      .setInput(Tags.Items.GUNPOWDER, 1, 20)
      .setMaxLevel(5) // +50 mining speed at max, conditionally
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.blasting, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.blasting, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.hydraulic)
      .setTools(TinkerTags.Items.HARVEST)
      .setInput(Tags.Items.DUSTS_PRISMARINE, 1, 36) // stupid forge name
      .setMaxLevel(5)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.hydraulic, upgradeSalvage))
      .save(consumer, this.wrap(TinkerModifiers.hydraulic, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.hydraulic)
      .setTools(TinkerTags.Items.HARVEST)
      .setInput(Blocks.PRISMARINE, 4, 36)
      .setLeftover(new ItemStack(Items.PRISMARINE_SHARD))
      .setMaxLevel(5)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(TinkerModifiers.hydraulic, upgradeFolder, "_from_block"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.hydraulic)
      .setTools(TinkerTags.Items.HARVEST)
      .setInput(Blocks.PRISMARINE_BRICKS, 9, 36)
      .setLeftover(new ItemStack(Items.PRISMARINE_SHARD))
      .setMaxLevel(5)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(TinkerModifiers.hydraulic, upgradeFolder, "_from_bricks"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.lightspeed)
      .setTools(TinkerTags.Items.HARVEST)
      .setInput(Tags.Items.DUSTS_GLOWSTONE, 1, 64)
      .setMaxLevel(5) // +45 mining speed at max, conditionally
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.lightspeed, upgradeSalvage))
      .save(consumer, this.wrap(TinkerModifiers.lightspeed, upgradeFolder, "_from_dust"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.lightspeed)
      .setTools(TinkerTags.Items.HARVEST)
      .setInput(Blocks.GLOWSTONE, 4, 64)
      .setLeftover(new ItemStack(Items.GLOWSTONE_DUST))
      .setMaxLevel(5)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(TinkerModifiers.lightspeed, upgradeFolder, "_from_block"));

    /*
     * weapon
     */
    ModifierRecipeBuilder.modifier(TinkerModifiers.knockback)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.EARTH))
      .setMaxLevel(3) // max +2.5 knockback points (knockback 5) (whatever that number means in vanilla)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.MELEE)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.knockback, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.knockback, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.padded)
      .addInput(Items.LEATHER)
      .addInput(ItemTags.WOOL)
      .addInput(Items.LEATHER)
      .setMaxLevel(3) // max 12.5% knockback, or 6.25% on the dagger
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.padded, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.padded, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.severing)
      .addInput(TinkerTags.Items.WITHER_BONES)
      .addInput(Items.LIGHTNING_ROD)
      .addInput(TinkerTags.Items.WITHER_BONES)
      .addInput(Items.TNT)
      .setMaxLevel(3) // max +25% head drop chance, combine with +15% chance from luck
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.severing, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.severing, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.fiery)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_OR_UNARMED, TinkerTags.Items.BOWS))
      .setInput(Items.BLAZE_POWDER, 1, 25)
      .setMaxLevel(5) // +25 seconds fire damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.fiery, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.fiery, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.necrotic)
      .addInput(TinkerMaterials.necroticBone)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.BLOOD))
      .addInput(Items.GHAST_TEAR)
      .setMaxLevel(5) // +50% chance of heal, combine with +40% from traits for +90% total
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_OR_UNARMED, TinkerTags.Items.BOWS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.necrotic, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.necrotic, upgradeFolder));

    /*
     * damage boost
     */
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.piercing)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.BOWS))
      .setInput(Blocks.CACTUS, 1, 25)
      .setMaxLevel(5) // +2.5 pierce damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.piercing, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.piercing, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.smite)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Items.GLISTERING_MELON_SLICE, 1, 5)
      .setMaxLevel(5) // +12.5 undead damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.smite, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.smite, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.baneOfSssss)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Items.FERMENTED_SPIDER_EYE, 1, 15)
      .setMaxLevel(5) // +12.5 spider damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.baneOfSssss, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.baneOfSssss, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.antiaquatic)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Items.PUFFERFISH, 1, 20)
      .setMaxLevel(5) // +12.5 fish damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.antiaquatic, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.antiaquatic, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.cooling)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Items.PRISMARINE_CRYSTALS, 1, 25)
      .setMaxLevel(5) // +10 fire mob damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.cooling, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.cooling, upgradeFolder));
    // killager uses both types of lapis
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.killager)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Tags.Items.GEMS_LAPIS, 1, 45)
      .setMaxLevel(5) // +12.5 illager damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.killager, upgradeSalvage))
      .save(consumer, this.wrap(ModifierIds.killager, upgradeFolder, "_from_dust"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.killager)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Tags.Items.STORAGE_BLOCKS_LAPIS, 9, 45)
      .setMaxLevel(5) // +12.5 illager damage
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(ModifierIds.killager, upgradeFolder, "_from_block"));
    // sharpness can use shards or blocks
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sharpness)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Tags.Items.GEMS_QUARTZ, 1, 36)
      .setMaxLevel(5) // +5 damage
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.sharpness, upgradeSalvage))
      .save(consumer, this.wrap(ModifierIds.sharpness, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sharpness)
      .setTools(TinkerTags.Items.MELEE_OR_UNARMED)
      .setInput(Tags.Items.STORAGE_BLOCKS_QUARTZ, 4, 36)
      .setLeftover(new ItemStack(Items.QUARTZ))
      .setMaxLevel(5)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(ModifierIds.sharpness, upgradeFolder, "_from_block"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.sweeping)
      .setTools(TinkerTags.Items.SWORD)
      .setInput(Blocks.CHAIN, 1, 18) // every 9 is 11 ingots, so this is 22 ingots
      .setMaxLevel(3) // goes 25%, 50%, 75%
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.sweeping, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.sweeping, upgradeFolder));
    // swiftstrike works on blocks too, we are nice
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.swiftstrike)
      .setTools(TinkerTags.Items.MELEE)
      .setInput(Items.AMETHYST_SHARD, 1, 72)
      .setMaxLevel(5)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.swiftstrike, upgradeSalvage))
      .save(consumer, this.wrap(ModifierIds.swiftstrike, upgradeFolder, "_from_shard"));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.swiftstrike)
      .setTools(TinkerTags.Items.MELEE)
      .setInput(Blocks.AMETHYST_BLOCK, 4, 72)
      .setLeftover(new ItemStack(Items.AMETHYST_SHARD))
      .setMaxLevel(5)
      .disallowCrystal()
      .setSlots(SlotType.UPGRADE, 1)
      .save(consumer, this.wrap(ModifierIds.swiftstrike, upgradeFolder, "_from_block"));

    /*
     * ranged
     */
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.power)
      .setTools(TinkerTags.Items.LONGBOWS)
      .setInput(TinkerWorld.ichorGeode.asItem(), 1, 72)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(5)
      .saveSalvage(consumer, this.prefix(ModifierIds.power, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.power, upgradeFolder));
    this.hasteRecipes(consumer, ModifierIds.quickCharge, Ingredient.of(TinkerTags.Items.CROSSBOWS), 4, upgradeFolder, upgradeSalvage);
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.trueshot)
      .setInput(Items.TARGET, 1, 10)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .setTools(TinkerTags.Items.RANGED)
      .saveSalvage(consumer, this.prefix(ModifierIds.trueshot, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.trueshot, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.blindshot)
      .setInput(Items.DIRT, 1, 10)
      .setTools(TinkerTags.Items.RANGED)
      .save(consumer, this.prefix(ModifierIds.blindshot, slotlessFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.punch)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.SKY))
      .setMaxLevel(5) // vanilla caps at 2, that is boring
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.RANGED)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.punch, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.punch, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.impaling)
      .addInput(Items.END_ROD)
      .addInput(Items.END_ROD)
      .addInput(Items.END_ROD)
      .setMaxLevel(4) // same max as vanilla
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.BOWS) // impaling on longbows sounds fun in theory, may reconsider once ricochet is coded
      .saveSalvage(consumer, this.prefix(TinkerModifiers.impaling, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.impaling, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.freezing)
      .addInput(Items.POWDER_SNOW_BUCKET)
      .setMaxLevel(2)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.BOWS) // no elementals on fluid cannon
      .saveSalvage(consumer, this.prefix(TinkerModifiers.freezing, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.freezing, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.bulkQuiver)
      .addInput(Items.LEATHER)
      .addInput(TinkerWorld.skySlimeVine)
      .addInput(Items.LEATHER)
      .addInput(TinkerWorld.skySlimeVine)
      .addInput(TinkerWorld.skySlimeVine)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.BOWS)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.bulkQuiver, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.bulkQuiver, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.trickQuiver)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerWorld.skySlimeVine)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerWorld.skySlimeVine)
      .addInput(TinkerWorld.skySlimeVine)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.BOWS)
      .setMaxLevel(2)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.trickQuiver, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.trickQuiver, abilityFolder));
    BiConsumer<ItemLike, String> crystalshotRecipe = (item, variant) -> {
      SwappableModifierRecipeBuilder.modifier(TinkerModifiers.crystalshot, variant)
        .addInput(item)
        .addInput(Items.BLAZE_ROD)
        .addInput(item)
        .addInput(TinkerMaterials.manyullyn.getIngotTag())
        .addInput(TinkerMaterials.manyullyn.getIngotTag())
        .setTools(TinkerTags.Items.BOWS)
        .setSlots(SlotType.ABILITY, 1)
        .save(consumer, this.wrap(TinkerModifiers.crystalshot, abilityFolder, "_" + variant));
    };
    crystalshotRecipe.accept(Items.AMETHYST_CLUSTER, "amethyst");
    crystalshotRecipe.accept(TinkerWorld.earthGeode.getBud(BudSize.CLUSTER), "earthslime");
    crystalshotRecipe.accept(TinkerWorld.skyGeode.getBud(BudSize.CLUSTER), "skyslime");
    crystalshotRecipe.accept(TinkerWorld.ichorGeode.getBud(BudSize.CLUSTER), "ichor");
    crystalshotRecipe.accept(TinkerWorld.enderGeode.getBud(BudSize.CLUSTER), "enderslime");
    crystalshotRecipe.accept(Items.NETHER_QUARTZ_ORE, "quartz");
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.crystalshot, "random")
      .addInput(Ingredient.of(TinkerWorld.earthGeode.getBud(BudSize.CLUSTER), TinkerWorld.skyGeode.getBud(BudSize.CLUSTER)))
      .addInput(Ingredient.of(Items.AMETHYST_CLUSTER, Items.NETHER_QUARTZ_ORE))
      .addInput(Ingredient.of(TinkerWorld.ichorGeode.getBud(BudSize.CLUSTER), TinkerWorld.enderGeode.getBud(BudSize.CLUSTER)))
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .setTools(TinkerTags.Items.BOWS)
      .setSlots(SlotType.ABILITY, 1)
      .allowCrystal() // random is the coolest, and happens to be the easiest to enable
      .save(consumer, this.wrap(TinkerModifiers.crystalshot, abilityFolder, "_random"));
    ModifierRecipeBuilder.modifier(TinkerModifiers.crystalshot)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.BOWS)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.crystalshot, abilitySalvage));
    ModifierRecipeBuilder.modifier(TinkerModifiers.multishot)
      .addInput(Items.PISTON)
      .addInput(TinkerMaterials.amethystBronze.getIngotTag())
      .addInput(Items.PISTON)
      .addInput(SlimeType.ICHOR.getSlimeballTag())
      .addInput(SlimeType.ICHOR.getSlimeballTag())
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.BOWS)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.multishot, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.multishot, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.sinistral)
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .addInput(Items.NAUTILUS_SHELL)
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .addInput(SlimeType.SKY.getSlimeballTag())
      .addInput(SlimeType.SKY.getSlimeballTag())
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(DefaultCustomIngredients.all(Ingredient.of(TinkerTags.Items.CROSSBOWS), Ingredient.of(TinkerTags.Items.INTERACTABLE_LEFT))) // this is the same recipes as dual wielding, but crossbows do not interact on left
      .saveSalvage(consumer, this.prefix(TinkerModifiers.sinistral, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.sinistral, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.scope)
      .setTools(TinkerTags.Items.LONGBOWS)
      .addInput(Tags.Items.STRING)
      .addInput(Items.SPYGLASS)
      .addInput(Tags.Items.STRING)
      .addInput(Tags.Items.DUSTS_REDSTONE)
      .addInput(Tags.Items.DUSTS_REDSTONE)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.scope, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.scope, upgradeFolder));

    /*
     * armor
     */
    // protection
    // all held tools can receive defense slots, so give them something to use it for
    Ingredient protectableTools = ingredientFromTags(TinkerTags.Items.ARMOR, TinkerTags.Items.HELD);
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.meleeProtection)
      .setInput(TinkerModifiers.cobaltReinforcement, 1, 20)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(protectableTools)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.meleeProtection, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.meleeProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.projectileProtection)
      .setInput(TinkerModifiers.bronzeReinforcement, 1, 20)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(protectableTools)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.projectileProtection, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.projectileProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.blastProtection)
      .setInput(TinkerModifiers.emeraldReinforcement, 1, 20)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(protectableTools)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.blastProtection, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.blastProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.magicProtection)
      .setInput(TinkerModifiers.goldReinforcement, 1, 20)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(protectableTools)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.magicProtection, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.magicProtection, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.fireProtection)
      .setInput(TinkerModifiers.searedReinforcement, 1, 20)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(protectableTools)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.fireProtection, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.fireProtection, defenseFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.protection)
      .addInput(TinkerModifiers.goldReinforcement, 4)
      .addInput(TinkerModifiers.searedReinforcement, 4)
      .addInput(TinkerModifiers.bronzeReinforcement, 4)
      .addInput(TinkerModifiers.emeraldReinforcement, 4)
      .addInput(TinkerModifiers.cobaltReinforcement, 4)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.ARMOR)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.protection, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.protection, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.boundless)
      .addInput(TinkerCommons.obsidianPane, 4)
      .addInput(Items.WRITABLE_BOOK)
      .addInput(TinkerCommons.obsidianPane, 4)
      .addInput(TinkerWorld.ichorGeode, 2)
      .addInput(TinkerWorld.ichorGeode, 2)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.SHIELDS)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.boundless, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.boundless, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.knockbackResistance)
      .setTools(TinkerTags.Items.ARMOR)
      .addInput(SizedIngredient.fromItems(Blocks.ANVIL, Blocks.CHIPPED_ANVIL, Blocks.DAMAGED_ANVIL))
      .setSlots(SlotType.DEFENSE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(ModifierIds.knockbackResistance, defenseSalvage))
      .save(consumer, this.prefix(ModifierIds.knockbackResistance, defenseFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.golden)
      .setTools(TinkerTags.Items.WORN_ARMOR) // piglins ignore held items
      .addInput(Tags.Items.INGOTS_GOLD)
      .addInput(Tags.Items.INGOTS_GOLD)
      .addInput(Tags.Items.INGOTS_GOLD)
      .setSlots(SlotType.DEFENSE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.golden, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.golden, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.turtleShell)
      .setInput(Items.SCUTE, 1, 5)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(TinkerTags.Items.ARMOR)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.turtleShell, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.turtleShell, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.shulking)
      .setInput(Items.SHULKER_SHELL, 1, 5)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(TinkerTags.Items.ARMOR)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.shulking, defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.shulking, defenseFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.dragonborn)
      .setInput(TinkerModifiers.dragonScale, 1, 10)
      .setSlots(SlotType.DEFENSE, 1)
      .setTools(TinkerTags.Items.ARMOR)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.dragonScale.getRegistryName(), defenseSalvage))
      .save(consumer, this.prefix(TinkerModifiers.dragonScale.getRegistryName(), defenseFolder));
    // 3 each for chest and legs, 2 each for boots and helmet, leads to 10 total
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.revitalizing)
      .setTools(ingredientFromTags(TinkerTags.Items.WORN_ARMOR)) // revitalizing would suck on an item you constantly change
      .setInput(TinkerCommons.jeweledApple, 1, 2)
      .setSlots(SlotType.DEFENSE, 1)
      .setMaxLevel(5)
      .saveSalvage(consumer, this.prefix(ModifierIds.revitalizing, defenseSalvage))
      .save(consumer, this.prefix(ModifierIds.revitalizing, defenseFolder));

    // upgrade - counterattack
    Ingredient wornOrShield = ingredientFromTags(TinkerTags.Items.WORN_ARMOR, TinkerTags.Items.SHIELDS); // held armor may include things that cannot block
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.thorns)
      .setTools(wornOrShield)
      .setInput(Blocks.CACTUS, 1, 25)
      .setMaxLevel(3)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.thorns, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.thorns, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.sticky)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.WORN_ARMOR, TinkerTags.Items.SHIELDS))
      .setInput(Blocks.COBWEB, 1, 5)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, this.prefix(ModifierIds.sticky, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.sticky, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.springy)
      .setTools(wornOrShield)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.ICHOR))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.springy, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.springy, upgradeFolder));
    // upgrade - helmet
    ModifierRecipeBuilder.modifier(ModifierIds.respiration)
      .setTools(TinkerTags.Items.HELMETS)
      .addInput(ItemTags.FISHES)
      .addInput(Tags.Items.GLASS_COLORLESS)
      .addInput(ItemTags.FISHES)
      .addInput(Items.KELP)
      .addInput(Items.KELP)
      .setMaxLevel(3)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.respiration, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.respiration, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.itemFrame)
      .setTools(TinkerTags.Items.HELMETS)
      .addInput(Ingredient.of(Arrays.stream(FrameType.values())
        .filter(type -> type != FrameType.CLEAR)
        .map(type -> new ItemStack(TinkerGadgets.itemFrame.get(type)))))
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.itemFrame, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.itemFrame, upgradeFolder));
    // upgrade - chestplate
    ModifierRecipeBuilder.modifier(TinkerModifiers.knockback)
      .setTools(TinkerTags.Items.CHESTPLATES)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.EARTH))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, this.wrap(TinkerModifiers.knockback, upgradeSalvage, "_armor"))
      .save(consumer, this.wrap(TinkerModifiers.knockback, upgradeFolder, "_armor"));
    // upgrade - leggings
    this.hasteRecipes(consumer, ModifierIds.speedy, Ingredient.of(TinkerTags.Items.LEGGINGS), 3, upgradeFolder, upgradeSalvage);
    // leaping lets you disable skyslime geodes in case you don't like fun
    // if you are disabling both, you have a ton of recipes to fix anyways
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.leaping)
      .setTools(TinkerTags.Items.LEGGINGS)
      .setInput(TinkerWorld.skyGeode.asItem(), 1, 36)
      .setMaxLevel(2)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.leaping, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.leaping, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.stepUp)
      .setTools(TinkerTags.Items.LEGGINGS)
      .addInput(Items.LEATHER)
      .addInput(Items.GOLDEN_CARROT)
      .addInput(Items.LEATHER)
      .addInput(Items.SCAFFOLDING)
      .addInput(Items.SCAFFOLDING)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(2)
      .saveSalvage(consumer, this.prefix(ModifierIds.stepUp, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.stepUp, upgradeFolder));

    // upgrade - boots
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.featherFalling)
      .setTools(TinkerTags.Items.BOOTS)
      .setInput(Items.FEATHER, 1, 40)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(4)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.featherFalling, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.featherFalling, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.soulspeed)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(Items.MAGMA_BLOCK)
      .addInput(Items.CRYING_OBSIDIAN)
      .addInput(Items.MAGMA_BLOCK)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.soulspeed, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.soulspeed, upgradeFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.depthStrider)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(ItemTags.FISHES)
      .addInput(Blocks.PRISMARINE_BRICKS)
      .addInput(ItemTags.FISHES)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(3)
      .saveSalvage(consumer, this.prefix(ModifierIds.depthStrider, upgradeSalvage))
      .save(consumer, this.prefix(ModifierIds.depthStrider, upgradeFolder));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.lightspeedArmor)
      .setTools(TinkerTags.Items.BOOTS)
      .setInput(Tags.Items.DUSTS_GLOWSTONE, 1, 64)
      .setMaxLevel(3) // 45% running speed at max, conditionally
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.lightspeedArmor, upgradeSalvage))
      .save(consumer, this.wrap(TinkerModifiers.lightspeedArmor, upgradeFolder, "_from_dust"));
    IncrementalModifierRecipeBuilder.modifier(TinkerModifiers.lightspeedArmor)
      .setTools(TinkerTags.Items.BOOTS)
      .setInput(Blocks.GLOWSTONE, 4, 64)
      .setLeftover(new ItemStack(Items.GLOWSTONE_DUST))
      .setMaxLevel(3)
      .setSlots(SlotType.UPGRADE, 1)
      .disallowCrystal()
      .save(consumer, this.wrap(TinkerModifiers.lightspeedArmor, upgradeFolder, "_from_block"));
    // upgrade - all
    ModifierRecipeBuilder.modifier(TinkerModifiers.ricochet)
      .setTools(wornOrShield)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.SKY))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(2) // 2 per piece gives +160% total
      .saveSalvage(consumer, this.prefix(TinkerModifiers.ricochet, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.ricochet, upgradeFolder));

    // armor ability
    // helmet
    ModifierRecipeBuilder.modifier(TinkerModifiers.zoom)
      .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.INTERACTABLE_RIGHT))
      .addInput(Tags.Items.STRING)
      .addInput(Items.SPYGLASS)
      .addInput(Tags.Items.STRING)
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.zoom, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.zoom, upgradeFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.slurping)
      .addInput(Items.GLASS_BOTTLE)
      .addInput(TinkerTags.Items.TANKS)
      .addInput(Items.GLASS_BOTTLE)
      .addInput(Tags.Items.INGOTS_COPPER)
      .addInput(Tags.Items.INGOTS_COPPER)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.STAFFS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.slurping, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.slurping, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.aquaAffinity)
      .addInput(Blocks.PRISMARINE_BRICKS)
      .addInput(Items.HEART_OF_THE_SEA)
      .addInput(Blocks.PRISMARINE_BRICKS)
      .addInput(Blocks.DARK_PRISMARINE)
      .addInput(Blocks.DARK_PRISMARINE)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.HELMETS)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.aquaAffinity, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.aquaAffinity, abilityFolder));
    // chestplate
    ModifierRecipeBuilder.modifier(TinkerModifiers.ambidextrous)
      .setTools(TinkerTags.Items.UNARMED)
      .addInput(Items.LEATHER)
      .addInput(Tags.Items.GEMS_DIAMOND)
      .addInput(Items.LEATHER)
      .addInput(Tags.Items.STRING)
      .addInput(Tags.Items.STRING)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.ambidextrous, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.ambidextrous, abilityFolder));
    IncrementalModifierRecipeBuilder.modifier(ModifierIds.strength)
      .setTools(TinkerTags.Items.CHESTPLATES)
      .setInput(TinkerWorld.ichorGeode.asItem(), 1, 72)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.strength, abilitySalvage))
      .save(consumer, this.prefix(ModifierIds.strength, abilityFolder));

    // leggings
    ModifierRecipeBuilder.modifier(ModifierIds.pockets)
      .setTools(TinkerTags.Items.LEGGINGS)
      .addInput(Items.SHULKER_SHELL)
      .addInput(Tags.Items.INGOTS_IRON)
      .addInput(Items.SHULKER_SHELL)
      .addInput(Items.LEATHER)
      .addInput(Items.LEATHER)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.pockets, abilitySalvage))
      .save(consumer, this.prefix(ModifierIds.pockets, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.shieldStrap)
      .addInput(TinkerWorld.skySlimeVine)
      .addInput(TinkerMaterials.slimesteel.getIngotTag())
      .addInput(TinkerWorld.skySlimeVine)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(TinkerTags.Items.LEGGINGS)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.shieldStrap, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.shieldStrap, upgradeFolder));
    BiConsumer<Integer, TagKey<Item>> toolBeltRecipe = (level, ingot) -> {
      ModifierRecipeBuilder builder = ModifierRecipeBuilder
        .modifier(ModifierIds.toolBelt)
        .addInput(Items.LEATHER)
        .addInput(ingot)
        .addInput(Items.LEATHER)
        .setTools(TinkerTags.Items.LEGGINGS)
        .setMaxLevel(level);
      if (level == 1) {
        builder.setSlots(SlotType.ABILITY, 1);
        builder.saveSalvage(consumer, this.prefix(ModifierIds.toolBelt, abilitySalvage));
      } else {
        builder.setRequirements(ModifierMatch.entry(ModifierIds.toolBelt, level - 1));
        builder.setRequirementsError(TConstruct.makeTranslationKey("recipe", "modifier.tool_belt"));
      }
      builder.disallowCrystal(); // handled below
      builder.save(consumer, this.wrap(ModifierIds.toolBelt, abilityFolder, "_" + level));
    };
    toolBeltRecipe.accept(1, Tags.Items.INGOTS_IRON);
    toolBeltRecipe.accept(2, Tags.Items.INGOTS_GOLD);
    toolBeltRecipe.accept(3, TinkerMaterials.roseGold.getIngotTag());
    toolBeltRecipe.accept(4, TinkerMaterials.cobalt.getIngotTag());
    toolBeltRecipe.accept(5, TinkerMaterials.hepatizon.getIngotTag());
    toolBeltRecipe.accept(6, TinkerMaterials.manyullyn.getIngotTag());
    MultilevelModifierRecipeBuilder.modifier(ModifierIds.toolBelt)
      .setTools(TinkerTags.Items.LEGGINGS)
      .addLevel(SlotType.ABILITY, 1, 1)
      .addLevelRange(2, 6)
      .save(consumer, this.wrap(ModifierIds.toolBelt, abilityFolder, "_crystal"));

    ModifierRecipeBuilder.modifier(TinkerModifiers.wetting)
      .addInput(Tags.Items.DUSTS_REDSTONE)
      .addInput(TinkerTags.Items.TANKS)
      .addInput(Tags.Items.DUSTS_REDSTONE)
      .addInput(Tags.Items.INGOTS_COPPER)
      .addInput(Tags.Items.INGOTS_COPPER)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.LEGGINGS, TinkerTags.Items.SHIELDS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.wetting, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.wetting, abilityFolder));
    // boots
    ModifierRecipeBuilder.modifier(TinkerModifiers.doubleJump)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.SKY))
      .addInput(Items.PISTON)
      .addInput(Items.PHANTOM_MEMBRANE)
      .addInput(Items.PHANTOM_MEMBRANE)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.doubleJump, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.doubleJump, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.bouncy)
      .setTools(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.BOOTS), Ingredient.of(TinkerTools.slimesuit.get(ArmorSlotType.BOOTS))))
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.SKY), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.ICHOR), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.SKY), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.bouncy, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.bouncy, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.frostWalker)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(Items.BLUE_ICE)
      .addInput(TinkerWorld.heads.get(TinkerHeadType.STRAY))
      .addInput(Items.BLUE_ICE)
      .addInput(Items.BLUE_ICE)
      .addInput(Items.BLUE_ICE)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.frostWalker, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.frostWalker, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.snowdrift)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(Items.SNOW_BLOCK)
      .addInput(Items.CARVED_PUMPKIN)
      .addInput(Items.SNOW_BLOCK)
      .addInput(Items.SNOW_BLOCK)
      .addInput(Items.SNOW_BLOCK)
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.snowdrift, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.snowdrift, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.longFall)
      .setTools(TinkerTags.Items.BOOTS)
      .addInput(Items.PISTON)
      .addInput(Blocks.BLACK_WOOL)
      .addInput(Items.PISTON)
      .addInput(TinkerWorld.slime.get(SlimeType.SKY))
      .addInput(TinkerWorld.slime.get(SlimeType.SKY))
      .setSlots(SlotType.ABILITY, 1)
      .setMaxLevel(1)
      .setRequirements(ModifierMatch.entry(TinkerModifiers.featherFalling.getId(), 4))
      .setRequirementsError(makeRequirementsError("long_fall"))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.longFall, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.longFall, abilityFolder));

    // transform ingredients
    Ingredient bootsWithDuraibility = DefaultCustomIngredients.all(Ingredient.of(TinkerTags.Items.BOOTS), Ingredient.of(TinkerTags.Items.DURABILITY));
    SizedIngredient roundPlate = SizedIngredient.of(MaterialIngredient.fromItem(TinkerToolParts.roundPlate.get()));
    SizedIngredient smallBlade = SizedIngredient.of(MaterialIngredient.fromItem(TinkerToolParts.smallBlade.get()));
    SizedIngredient toolBinding = SizedIngredient.of(MaterialIngredient.fromItem(TinkerToolParts.toolBinding.get()));
    ModifierRecipeBuilder.modifier(TinkerModifiers.pathMaker)
      .setTools(bootsWithDuraibility)
      .addInput(roundPlate)
      .addInput(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
      .addInput(toolBinding)
      .addInput(roundPlate)
      .addInput(toolBinding)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.pathMaker, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.pathMaker, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.plowing)
      .setTools(bootsWithDuraibility)
      .addInput(smallBlade)
      .addInput(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
      .addInput(toolBinding)
      .addInput(smallBlade)
      .addInput(toolBinding)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.plowing, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.plowing, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.flamewake)
      .setTools(bootsWithDuraibility)
      .addInput(Items.FLINT)
      .addInput(TinkerTags.Items.INGOTS_NETHERITE_SCRAP)
      .addInput(Items.FLINT)
      .addInput(Items.FLINT)
      .addInput(Items.FLINT)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.flamewake, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.flamewake, abilityFolder));

    /*
     * ability
     */
    ModifierRecipeBuilder.modifier(ModifierIds.gilded)
      .addInput(Items.GOLDEN_APPLE)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.gilded, abilitySalvage))
      .save(consumer, this.prefix(ModifierIds.gilded, abilityFolder));
    // luck is 3 recipes
    // level 1 always requires a slot
    Ingredient luckSupporting = ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.HARVEST, TinkerTags.Items.RANGED);
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(luckSupporting)
      .addInput(Tags.Items.INGOTS_COPPER)
      .addInput(SizedIngredient.fromItems(Items.CORNFLOWER, Items.BLUE_ORCHID))
      .addInput(Tags.Items.INGOTS_COPPER)
      .addInput(Tags.Items.STORAGE_BLOCKS_LAPIS)
      .addInput(Tags.Items.STORAGE_BLOCKS_LAPIS)
      .setSalvageLevelRange(1, 1)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .disallowCrystal() // handled below
      .save(consumer, this.wrap(ModifierIds.luck, abilityFolder, "_level_1"));
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(luckSupporting)
      .addInput(Tags.Items.INGOTS_GOLD)
      .addInput(Items.GOLDEN_CARROT)
      .addInput(Tags.Items.INGOTS_GOLD)
      .addInput(Tags.Items.ENDER_PEARLS)
      .addInput(Tags.Items.ENDER_PEARLS)
      .setRequirements(ModifierMatch.entry(ModifierIds.luck, 1))
      .setRequirementsError(makeRequirementsError("luck.level_2"))
      .disallowCrystal() // handled below
      .setMaxLevel(2)
      .save(consumer, this.wrap(ModifierIds.luck, abilityFolder, "_level_2"));
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(luckSupporting)
      .addInput(TinkerMaterials.roseGold.getIngotTag())
      .addInput(Items.RABBIT_FOOT)
      .addInput(TinkerMaterials.roseGold.getIngotTag())
      .addInput(Tags.Items.GEMS_DIAMOND)
      .addInput(Items.NAME_TAG)
      .setRequirements(ModifierMatch.entry(ModifierIds.luck, 2))
      .setRequirementsError(makeRequirementsError("luck.level_3"))
      .disallowCrystal() // handled below
      .setMaxLevel(3)
      .save(consumer, this.wrap(ModifierIds.luck, abilityFolder, "_level_3"));
    // pants have just one level
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(TinkerTags.Items.LEGGINGS)
      .addInput(SizedIngredient.fromItems(Items.CORNFLOWER, Items.BLUE_ORCHID))
      .addInput(Items.RABBIT_FOOT)
      .addInput(Items.GOLDEN_CARROT)
      .addInput(Tags.Items.GEMS_DIAMOND)
      .addInput(Items.NAME_TAG)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.wrap(ModifierIds.luck, abilitySalvage, "_pants"))
      .save(consumer, this.wrap(ModifierIds.luck, abilityFolder, "_pants"));
    // extra crystal recipe
    MultilevelModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(luckSupporting)
      .addLevel(SlotType.ABILITY, 1, 1)
      .addLevelRange(2, 3)
      .save(consumer, this.wrap(ModifierIds.luck, abilityFolder, "_crystal"));
    // salvage lets you salvage from chestplates
    ModifierRecipeBuilder.modifier(ModifierIds.luck)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_OR_UNARMED, TinkerTags.Items.HARVEST, TinkerTags.Items.RANGED))
      .setSalvageLevelRange(1, 1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.luck, abilitySalvage));

    // silky: all the cloth
    ModifierRecipeBuilder.modifier(TinkerModifiers.silky)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerModifiers.silkyCloth)
      .addInput(TinkerModifiers.silkyCloth)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.HARVEST)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.silky, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.silky, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.exchanging)
      .addInput(Items.STICKY_PISTON)
      .addInput(TinkerMaterials.hepatizon.getIngotTag())
      .addInput(Items.STICKY_PISTON)
      .addInput(Tags.Items.ENDER_PEARLS)
      .addInput(Tags.Items.ENDER_PEARLS)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.HARVEST)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.exchanging, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.exchanging, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.autosmelt)
      .addInput(Tags.Items.RAW_MATERIALS)
      .addInput(Blocks.BLAST_FURNACE)
      .addInput(Tags.Items.INGOTS)
      .addInput(Tags.Items.STORAGE_BLOCKS_COAL)
      .addInput(Tags.Items.STORAGE_BLOCKS_COAL)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.HARVEST)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.autosmelt, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.autosmelt, abilityFolder));
    // fluid stuff
    ModifierRecipeBuilder.modifier(TinkerModifiers.melting)
      .addInput(Items.BLAZE_ROD)
      .addInput(Ingredient.of(TinkerSmeltery.searedMelter, TinkerSmeltery.smelteryController, TinkerSmeltery.foundryController))
      .addInput(Items.BLAZE_ROD)
      .addInput(Items.LAVA_BUCKET)
      .addInput(Items.LAVA_BUCKET)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE_OR_UNARMED, TinkerTags.Items.HARVEST))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.melting, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.melting, abilityFolder));
    SizedIngredient faucets = SizedIngredient.fromItems(TinkerSmeltery.searedFaucet, TinkerSmeltery.scorchedFaucet); // no salvage as don't want conversion between seared and scorched
    ModifierRecipeBuilder.modifier(TinkerModifiers.bucketing)
      .addInput(faucets)
      .addInput(Items.BUCKET)
      .addInput(faucets)
      .addInput(Tags.Items.ENDER_PEARLS)
      .addInput(Tags.Items.ENDER_PEARLS)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.INTERACTABLE)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.bucketing, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.bucketing, abilityFolder));
    SizedIngredient channels = SizedIngredient.fromItems(TinkerSmeltery.searedChannel, TinkerSmeltery.scorchedChannel);
    ModifierRecipeBuilder.modifier(TinkerModifiers.spilling)
      .addInput(channels)
      .addInput(TinkerTags.Items.TANKS)
      .addInput(channels)
      .addInput(Tags.Items.INGOTS_COPPER)
      .addInput(Tags.Items.INGOTS_COPPER)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.MELEE, TinkerTags.Items.CHESTPLATES, TinkerTags.Items.STAFFS, TinkerTags.Items.SHIELDS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.spilling, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.spilling, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.tank)
      .addInput(TinkerTags.Items.TANKS)
      .setSlots(SlotType.UPGRADE, 1)
      .setTools(ingredientFromTags(TinkerTags.Items.INTERACTABLE, TinkerTags.Items.HELMETS, TinkerTags.Items.CHESTPLATES, TinkerTags.Items.LEGGINGS, TinkerTags.Items.SHIELDS))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.tank, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.tank, upgradeFolder));
    // expanders
    ModifierRecipeBuilder.modifier(TinkerModifiers.expanded)
      .addInput(Items.PISTON)
      .addInput(TinkerMaterials.amethystBronze.getIngotTag())
      .addInput(Items.PISTON)
      .addInput(SlimeType.ICHOR.getSlimeballTag())
      .addInput(SlimeType.ICHOR.getSlimeballTag())
      .setSlots(SlotType.ABILITY, 1)
      .setTools(TinkerTags.Items.AOE)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.expanded, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.expanded, abilityFolder));
    // reach expander
    ModifierRecipeBuilder.modifier(ModifierIds.reach)
      .setTools(TinkerTags.Items.CHESTPLATES)
      .addInput(Items.PISTON)
      .addInput(TinkerMaterials.queensSlime.getIngotTag())
      .addInput(Items.PISTON)
      .addInput(SlimeType.ENDER.getSlimeballTag())
      .addInput(SlimeType.ENDER.getSlimeballTag())
      .setSlots(SlotType.ABILITY, 1)
      .save(consumer, this.prefix(ModifierIds.reach, abilityFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.reach)
      .setTools(ingredientFromTags(TinkerTags.Items.HARVEST, TinkerTags.Items.CHESTPLATES))
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(ModifierIds.reach, abilitySalvage));
    // block transformers
    Ingredient interactableWithDurability = DefaultCustomIngredients.all(Ingredient.of(TinkerTags.Items.DURABILITY), Ingredient.of(TinkerTags.Items.INTERACTABLE));
    ModifierRecipeBuilder.modifier(TinkerModifiers.pathing)
      .setTools(DefaultCustomIngredients.difference(interactableWithDurability, Ingredient.of(TinkerTools.pickadze, TinkerTools.excavator)))
      .addInput(roundPlate)
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(toolBinding)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.pathing, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.pathing, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.stripping)
      .setTools(DefaultCustomIngredients.difference(interactableWithDurability, Ingredient.of(TinkerTools.handAxe, TinkerTools.broadAxe)))
      .addInput(SizedIngredient.of(MaterialIngredient.fromItem(TinkerToolParts.smallAxeHead.get())))
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(toolBinding)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.stripping, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.stripping, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.tilling)
      .setTools(DefaultCustomIngredients.difference(interactableWithDurability, Ingredient.of(TinkerTools.mattock, TinkerTools.scythe)))
      .addInput(smallBlade)
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(toolBinding)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.tilling, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.tilling, abilityFolder));
    // glowing
    ModifierRecipeBuilder.modifier(TinkerModifiers.glowing)
      .setTools(interactableWithDurability)
      .addInput(Items.GLOWSTONE)
      .addInput(Items.DAYLIGHT_DETECTOR)
      .addInput(Items.SHROOMLIGHT)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.glowing, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.glowing, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.firestarter)
      .setTools(DefaultCustomIngredients.difference(interactableWithDurability, Ingredient.of(TinkerTools.flintAndBrick)))
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(Items.FLINT)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.firestarter, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.firestarter, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.fireprimer)
      .setTools(Ingredient.of(TinkerTools.flintAndBrick))
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(Items.FLINT)
      .setMaxLevel(1)
      .setSlots(SlotType.UPGRADE, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.fireprimer, upgradeSalvage))
      .save(consumer, this.prefix(TinkerModifiers.fireprimer, upgradeFolder));

    // unbreakable
    ModifierRecipeBuilder.modifier(TinkerModifiers.unbreakable)
      .setTools(TinkerTags.Items.DURABILITY)
      .addInput(Items.SHULKER_SHELL)
      .addInput(Items.DRAGON_BREATH)
      .addInput(Items.SHULKER_SHELL)
      .addInput(Tags.Items.INGOTS_NETHERITE)
      .addInput(Tags.Items.INGOTS_NETHERITE)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setRequirements(ModifierMatch.list(2, ModifierMatch.entry(ModifierIds.netherite, 1), ModifierMatch.entry(TinkerModifiers.reinforced, 5)))
      .setRequirementsError(makeRequirementsError("unbreakable_requirements"))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.unbreakable, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.unbreakable, abilityFolder));
    // weapon
    ModifierRecipeBuilder.modifier(TinkerModifiers.dualWielding)
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .addInput(Items.NAUTILUS_SHELL)
      .addInput(TinkerMaterials.manyullyn.getIngotTag())
      .addInput(SlimeType.SKY.getSlimeballTag())
      .addInput(SlimeType.SKY.getSlimeballTag())
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .setTools(DefaultCustomIngredients.difference(DefaultCustomIngredients.all(Ingredient.of(TinkerTags.Items.MELEE), Ingredient.of(TinkerTags.Items.INTERACTABLE_RIGHT)), Ingredient.of(TinkerTools.dagger)))
      .saveSalvage(consumer, this.prefix(TinkerModifiers.dualWielding, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.dualWielding, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.blocking)
      .setTools(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.INTERACTABLE_RIGHT), Ingredient.of(TinkerTags.Items.PARRY)))
      .addInput(ItemTags.PLANKS)
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(ItemTags.PLANKS)
      .addInput(ItemTags.PLANKS)
      .addInput(ItemTags.PLANKS)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.blocking, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.blocking, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.parrying)
      .setTools(TinkerTags.Items.PARRY)
      .addInput(ItemTags.PLANKS)
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(ItemTags.PLANKS)
      .setMaxLevel(1)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.parrying, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.parrying, abilityFolder));
    ModifierRecipeBuilder.modifier(TinkerModifiers.reflecting)
      .setTools(TinkerTags.Items.SHIELDS)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.SKY), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.ICHOR), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.SKY), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .addInput(TinkerWorld.congealedSlime.get(SlimeType.EARTH), 4)
      .setSlots(SlotType.ABILITY, 1)
      .saveSalvage(consumer, this.prefix(TinkerModifiers.reflecting, abilitySalvage))
      .save(consumer, this.prefix(TinkerModifiers.reflecting, abilityFolder));

    /*
     * extra modifiers
     */
    ModifierRecipeBuilder.modifier(ModifierIds.writable)
      .addInput(Items.WRITABLE_BOOK)
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.writable, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.harmonious)
      .addInput(ItemTags.MUSIC_DISCS)
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.harmonious, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.recapitated)
      .addInput(SizedIngredient.of(DefaultCustomIngredients.difference(Ingredient.of(Tags.Items.HEADS), Ingredient.of(Items.DRAGON_HEAD))))
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.recapitated, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.resurrected)
      .addInput(Items.END_CRYSTAL)
      .setMaxLevel(1)
      .save(consumer, this.prefix(ModifierIds.resurrected, slotlessFolder));
    ModifierRecipeBuilder.modifier(ModifierIds.draconic)
      .addInput(Items.DRAGON_HEAD)
      .setMaxLevel(1)
      .save(consumer, this.wrap(ModifierIds.draconic, slotlessFolder, "_from_head"));
    ModifierRecipeBuilder.modifier(ModifierIds.draconic)
      .addInput(Blocks.WITHER_ROSE)
      .addInput(TinkerModifiers.dragonScale)
      .addInput(Blocks.WITHER_ROSE)
      .addInput(TinkerModifiers.dragonScale)
      .addInput(TinkerModifiers.dragonScale)
      .setMaxLevel(1)
      .disallowCrystal()
      .save(consumer, this.wrap(ModifierIds.draconic, slotlessFolder, "_from_scales"));
    // rebalanced
    Ingredient rebalancedCommon = Ingredient.of(TinkerModifiers.dragonScale, Blocks.GILDED_BLACKSTONE);
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.UPGRADE.getName())
      .addInput(rebalancedCommon)
      .addInput(TinkerMaterials.roseGold.getIngotTag())
      .addInput(rebalancedCommon)
      .addInput(TinkerWorld.skyGeode.getBlock())
      .addInput(TinkerWorld.skyGeode.getBlock())
      .disallowCrystal()
      .save(consumer, this.wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.UPGRADE.getName()));
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.DEFENSE.getName())
      .setTools(ingredientFromTags(TinkerTags.Items.ARMOR, TinkerTags.Items.HELD))
      .addInput(rebalancedCommon)
      .addInput(TinkerMaterials.cobalt.getIngotTag())
      .addInput(rebalancedCommon)
      .addInput(TinkerWorld.earthGeode.getBlock())
      .addInput(TinkerWorld.earthGeode.getBlock())
      .disallowCrystal()
      .save(consumer, this.wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.DEFENSE.getName()));
    SwappableModifierRecipeBuilder.modifier(ModifierIds.rebalanced, SlotType.ABILITY.getName())
      .addInput(rebalancedCommon)
      .addInput(TinkerMaterials.queensSlime.getIngotTag())
      .addInput(rebalancedCommon)
      .addInput(TinkerWorld.ichorGeode.getBlock())
      .addInput(TinkerWorld.ichorGeode.getBlock())
      .disallowCrystal()
      .save(consumer, this.wrap(ModifierIds.rebalanced, slotlessFolder, "_" + SlotType.ABILITY.getName()));
    // creative
    SpecialRecipeBuilder.special(TinkerModifiers.creativeSlotSerializer.get()).save(consumer, this.modPrefix(slotlessFolder + "creative_slot"));

    // removal
    ModifierRemovalRecipe.Builder.removal()
      .addInput(Blocks.WET_SPONGE)
      .addLeftover(Blocks.SPONGE)
      .save(consumer, this.modResource(worktableFolder + "remove_modifier_sponge"));
    ModifierRemovalRecipe.Builder.removal()
      .addInput(DefaultCustomIngredients.any(FluidContainerIngredient.fromFluid(TinkerFluids.venom, false).toVanilla(),
        FluidContainerIngredient.fromIngredient(FluidIngredient.of(TinkerFluids.venom.getLocalTag(), FluidValues.BOTTLE),
          Ingredient.of(TinkerFluids.venomBottle)).toVanilla()))
      .save(consumer, this.modResource(worktableFolder + "remove_modifier_venom"));
    // non-dagger extracting
    ModifierRemovalRecipe.Builder.removal(TinkerModifiers.extractModifierSerializer.get())
      .setTools(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.MODIFIABLE), Ingredient.of(TinkerTags.Items.UNSALVAGABLE)))
      .addInput(TinkerWorld.enderGeode)
      .addInput(Items.DRAGON_BREATH, 5)
      .modifierPredicate(new TagModifierPredicate(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST).inverted())
      .save(consumer, this.modResource(worktableFolder + "extract_modifier_breath"));
    ModifierRemovalRecipe.Builder.removal(TinkerModifiers.extractModifierSerializer.get())
      .setTools(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.MODIFIABLE), Ingredient.of(TinkerTags.Items.UNSALVAGABLE)))
      .addInput(TinkerWorld.enderGeode)
      .addInput(Items.WET_SPONGE)
      .addLeftover(Items.SPONGE)
      .modifierPredicate(new TagModifierPredicate(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST).inverted())
      .save(consumer, this.modResource(worktableFolder + "extract_modifier_sponge"));
    // dagger extracting
    ModifierRemovalRecipe.Builder.removal(TinkerModifiers.extractModifierSerializer.get())
      .setTools(SizedIngredient.fromItems(2, TinkerTools.dagger))
      .addInput(TinkerWorld.enderGeode)
      .addInput(Items.DRAGON_BREATH, 5)
      .modifierPredicate(new TagModifierPredicate(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST).inverted())
      .save(consumer, this.modResource(worktableFolder + "extract_dagger_modifier_breath"));
    ModifierRemovalRecipe.Builder.removal(TinkerModifiers.extractModifierSerializer.get())
      .setTools(SizedIngredient.fromItems(2, TinkerTools.dagger))
      .addInput(TinkerWorld.enderGeode)
      .addInput(Items.WET_SPONGE)
      .addLeftover(Items.SPONGE)
      .modifierPredicate(new TagModifierPredicate(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST).inverted())
      .save(consumer, this.modResource(worktableFolder + "extract_dagger_modifier_sponge"));
    ModifierSortingRecipe.Builder.sorting()
      .addInput(Items.COMPASS)
      .save(consumer, this.modResource(worktableFolder + "modifier_sorting"));

    // invisible ink
    ResourceLocation hiddenModifiers = TConstruct.getResource("invisible_modifiers");
    IJsonPredicate<ModifierId> blacklist = new TagModifierPredicate(TinkerTags.Modifiers.INVISIBLE_INK_BLACKLIST).inverted();
    ModifierSetWorktableRecipeBuilder.setAdding(hiddenModifiers)
      .modifierPredicate(blacklist)
      .addInput(DefaultCustomIngredients.nbt(Ingredient.of(Items.POTION), PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.INVISIBILITY).getOrCreateTag(), false))
      .save(consumer, this.modResource(worktableFolder + "invisible_ink_adding"));
    ModifierSetWorktableRecipeBuilder.setRemoving(hiddenModifiers)
      .modifierPredicate(blacklist)
      .addInput(FluidContainerIngredient.fromIngredient(FluidIngredient.of(Milk.STILL_MILK, FluidConstants.BUCKET), Ingredient.of(Items.MILK_BUCKET)).toVanilla())
      .save(consumer, this.modResource(worktableFolder + "invisible_ink_removing"));

    // swapping hands
    IJsonPredicate<ModifierId> whitelist = new TagModifierPredicate(TinkerTags.Modifiers.DUAL_INTERACTION);
    ModifierSetWorktableRecipeBuilder.setAdding(DualOptionInteraction.KEY)
      .modifierPredicate(whitelist)
      .setTools(TinkerTags.Items.INTERACTABLE_DUAL)
      .addInput(Items.LEVER)
      .save(consumer, this.modResource(worktableFolder + "attack_modifier_setting"));
    ModifierSetWorktableRecipeBuilder.setRemoving(DualOptionInteraction.KEY)
      .modifierPredicate(whitelist)
      .setTools(TinkerTags.Items.INTERACTABLE_DUAL)
      .addInput(Items.LEVER)
      .addInput(Items.LEVER)
      .save(consumer, this.modResource(worktableFolder + "attack_modifier_clearing"));

    // conversion
    for (boolean matchBook : new boolean[]{false, true}) {
      String suffix = matchBook ? "_book" : "_tool";
      EnchantmentConvertingRecipe.Builder.converting("slotless", matchBook)
        .addInput(Items.AMETHYST_SHARD)
        .modifierPredicate(ModifierPredicate.AND.create(new SlotTypeModifierPredicate(null),
          new TagModifierPredicate(TinkerTags.Modifiers.EXTRACT_SLOTLESS_BLACKLIST).inverted()))
        .save(consumer, this.modResource(worktableFolder + "enchantment_converting/slotless" + suffix));
      EnchantmentConvertingRecipe.Builder.converting("upgrades", matchBook)
        .addInput(TinkerWorld.skyGeode.asItem())
        .addInput(Tags.Items.GEMS_LAPIS, 3)
        .modifierPredicate(new SlotTypeModifierPredicate(SlotType.UPGRADE))
        .save(consumer, this.modResource(worktableFolder + "enchantment_converting/upgrade" + suffix));
      EnchantmentConvertingRecipe.Builder.converting("defense", matchBook)
        .addInput(TinkerWorld.earthGeode.asItem())
        .addInput(Tags.Items.INGOTS_GOLD, 1)
        .modifierPredicate(new SlotTypeModifierPredicate(SlotType.DEFENSE))
        .save(consumer, this.modResource(worktableFolder + "enchantment_converting/defense" + suffix));
      EnchantmentConvertingRecipe.Builder.converting("abilities", matchBook)
        .addInput(TinkerWorld.ichorGeode.asItem())
        .addInput(Tags.Items.GEMS_DIAMOND)
        .modifierPredicate(new SlotTypeModifierPredicate(SlotType.ABILITY))
        .save(consumer, this.modResource(worktableFolder + "enchantment_converting/ability" + suffix));
      EnchantmentConvertingRecipe.Builder.converting("modifiers", matchBook)
        .addInput(TinkerWorld.enderGeode)
        .addInput(Items.DRAGON_BREATH, 5)
        .returnInput()
        .save(consumer, this.modResource(worktableFolder + "enchantment_converting/unenchant" + suffix));
    }

    // compatability
    String theOneProbe = "theoneprobe";
    ResourceLocation probe = new ResourceLocation(theOneProbe, "probe");
    Consumer<FinishedRecipe> topConsumer = this.withCondition(consumer, DefaultResourceConditions.allModsLoaded(theOneProbe));
    ModifierRecipeBuilder.modifier(TinkerModifiers.theOneProbe)
      .setTools(ingredientFromTags(TinkerTags.Items.HELMETS, TinkerTags.Items.HELD))
      .addInput(ItemNameIngredient.from(probe))
      .setSlots(SlotType.UPGRADE, 1)
      .setMaxLevel(1)
      .saveSalvage(topConsumer, this.prefix(TinkerModifiers.theOneProbe, compatSalvage))
      .save(topConsumer, this.prefix(TinkerModifiers.theOneProbe, compatFolder));

  }

  private void addTextureRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/modifiers/slotless/";

    // travelers gear //
    consumer.accept(new ArmorDyeingRecipe.Finished(this.modResource(folder + "travelers_dyeing"), Ingredient.of(TinkerTags.Items.DYEABLE)));

    // plate //
    // tier 2
    this.plateTexture(consumer, MaterialIds.iron, false, folder);
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.oxidizedIron.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_METAL)
      .addInput(Tags.Items.RAW_MATERIALS_IRON).addInput(Tags.Items.RAW_MATERIALS_IRON).addInput(Tags.Items.RAW_MATERIALS_IRON)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_iron_oxidized"));
    this.plateTexture(consumer, MaterialIds.copper, false, folder);
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.oxidizedCopper.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_METAL)
      .addInput(Tags.Items.RAW_MATERIALS_COPPER).addInput(Tags.Items.RAW_MATERIALS_COPPER).addInput(Tags.Items.RAW_MATERIALS_COPPER)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_copper_oxidized"));
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.gold.toString())
      .setTools(DefaultCustomIngredients.difference(Ingredient.of(TinkerTags.Items.EMBELLISHMENT_METAL), Ingredient.of(TinkerTags.Items.WORN_ARMOR)))
      .addInput(Tags.Items.INGOTS_GOLD).addInput(Tags.Items.INGOTS_GOLD).addInput(Tags.Items.INGOTS_GOLD)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_gold"));
    // tier 3
    this.plateTexture(consumer, MaterialIds.slimesteel, false, folder);
    this.plateTexture(consumer, MaterialIds.amethystBronze, false, folder);
    this.plateTexture(consumer, MaterialIds.roseGold, false, folder);
    this.plateTexture(consumer, MaterialIds.pigIron, false, folder);
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.obsidian.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_METAL)
      .addInput(TinkerCommons.obsidianPane).addInput(TinkerCommons.obsidianPane).addInput(TinkerCommons.obsidianPane)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_obsidian"));
    // does nothing by default, but helpful for addons
    this.plateTexture(consumer, MaterialIds.cobalt, false, folder);
    // tier 4
    this.plateTexture(consumer, MaterialIds.debris, "nuggets/netherite_scrap", false, folder);
    this.plateTexture(consumer, MaterialIds.manyullyn, false, folder);
    this.plateTexture(consumer, MaterialIds.hepatizon, false, folder);
    this.plateTexture(consumer, MaterialIds.netherite, "nuggets/netherite", false, folder);
    // tier 2 compat
    this.plateTexture(consumer, MaterialIds.osmium, true, folder);
    this.plateTexture(consumer, MaterialIds.tungsten, true, folder);
    this.plateTexture(consumer, MaterialIds.platinum, true, folder);
    this.plateTexture(consumer, MaterialIds.silver, true, folder);
    this.plateTexture(consumer, MaterialIds.lead, true, folder);
    this.plateTexture(consumer, MaterialIds.aluminum, true, folder);
    this.plateTexture(consumer, MaterialIds.nickel, true, folder);
    this.plateTexture(consumer, MaterialIds.tin, true, folder);
    this.plateTexture(consumer, MaterialIds.zinc, true, folder);
    this.plateTexture(consumer, MaterialIds.uranium, true, folder);
    // tier 3 compat
    this.plateTexture(consumer, MaterialIds.steel, true, folder);
    this.plateTexture(consumer, MaterialIds.bronze, true, folder);
    this.plateTexture(consumer, MaterialIds.constantan, true, folder);
    this.plateTexture(consumer, MaterialIds.invar, true, folder);
    this.plateTexture(consumer, MaterialIds.electrum, true, folder);
    this.plateTexture(consumer, MaterialIds.brass, true, folder);

    // slimesuit //
    this.slimeTexture(consumer, MaterialIds.earthslime, SlimeType.EARTH, folder);
    this.slimeTexture(consumer, MaterialIds.skyslime, SlimeType.SKY, folder);
    this.slimeTexture(consumer, MaterialIds.blood, SlimeType.BLOOD, folder);
    this.slimeTexture(consumer, MaterialIds.ichor, SlimeType.ICHOR, folder);
    this.slimeTexture(consumer, MaterialIds.enderslime, SlimeType.ENDER, folder);
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.clay.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_SLIME)
      .addInput(Blocks.CLAY).addInput(Items.CLAY_BALL).addInput(Blocks.CLAY)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_clay"));
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, MaterialIds.honey.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_SLIME)
      .addInput(Blocks.HONEY_BLOCK).addInput(Items.HONEY_BOTTLE).addInput(Blocks.HONEY_BLOCK)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_honey"));
  }

  private void addHeadRecipes(Consumer<FinishedRecipe> consumer) {
    String folder = "tools/severing/";
    // first, beheading
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.ZOMBIE), Items.ZOMBIE_HEAD)
      .save(consumer, this.modResource(folder + "zombie_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SKELETON), Items.SKELETON_SKULL)
      .save(consumer, this.modResource(folder + "skeleton_skull"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.WITHER_SKELETON, EntityType.WITHER), Items.WITHER_SKELETON_SKULL)
      .save(consumer, this.modResource(folder + "wither_skeleton_skull"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CREEPER), Items.CREEPER_HEAD)
      .save(consumer, this.modResource(folder + "creeper_head"));
    SpecialRecipeBuilder.special(TinkerModifiers.playerBeheadingSerializer.get()).save(consumer, this.modPrefix(folder + "player_head"));
    SpecialRecipeBuilder.special(TinkerModifiers.snowGolemBeheadingSerializer.get()).save(consumer, this.modPrefix(folder + "snow_golem_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.IRON_GOLEM), Blocks.CARVED_PUMPKIN)
      .save(consumer, this.modResource(folder + "iron_golem_head"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.ENDER_DRAGON), Items.DRAGON_HEAD)
      .save(consumer, this.modResource(folder + "ender_dragon_head"));
    TinkerWorld.headItems.forEach((type, head) ->
      SeveringRecipeBuilder.severing(EntityIngredient.of(type.getType()), head)
        .save(consumer, this.modResource(folder + type.getSerializedName() + "_head")));

    // other body parts
    // hostile
    // beeyeing
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SPIDER, EntityType.CAVE_SPIDER), Items.SPIDER_EYE)
      .save(consumer, this.modResource(folder + "spider_eye"));
    // be-internal-combustion-device
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CREEPER), Blocks.TNT)
      .save(consumer, this.modResource(folder + "creeper_tnt"));
    // bemembraning?
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.PHANTOM), Items.PHANTOM_MEMBRANE)
      .save(consumer, this.modResource(folder + "phantom_membrane"));
    // beshelling
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SHULKER), Items.SHULKER_SHELL)
      .save(consumer, this.modResource(folder + "shulker_shell"));
    // deboning
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SKELETON, EntityType.SKELETON_HORSE, EntityType.STRAY), ItemOutput.fromStack(new ItemStack(Items.BONE, 2)))
      .save(consumer, this.modResource(folder + "skeleton_bone"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.WITHER_SKELETON), ItemOutput.fromStack(new ItemStack(TinkerMaterials.necroticBone, 2)))
      .save(consumer, this.modResource(folder + "wither_skeleton_bone"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.BLAZE), ItemOutput.fromStack(new ItemStack(Items.BLAZE_ROD, 2)))
      .save(consumer, this.modResource(folder + "blaze_rod"));
    // desliming (you cut off a chunk of slime)
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.SLIME, TinkerWorld.earthSlimeEntity.get()), Items.SLIME_BALL)
      .save(consumer, this.modResource(folder + "earthslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.skySlimeEntity.get()), TinkerCommons.slimeball.get(SlimeType.SKY))
      .save(consumer, this.modResource(folder + "skyslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.enderSlimeEntity.get()), TinkerCommons.slimeball.get(SlimeType.ENDER))
      .save(consumer, this.modResource(folder + "enderslime_ball"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(TinkerWorld.terracubeEntity.get()), Items.CLAY_BALL)
      .save(consumer, this.modResource(folder + "terracube_clay"));
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.MAGMA_CUBE), Items.MAGMA_CREAM)
      .save(consumer, this.modResource(folder + "magma_cream"));
    // descaling? I don't know what to call those
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.GUARDIAN, EntityType.ELDER_GUARDIAN), ItemOutput.fromStack(new ItemStack(Items.PRISMARINE_SHARD, 2)))
      .save(consumer, this.modResource(folder + "guardian_shard"));

    // passive
    // befeating
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.RABBIT), Items.RABBIT_FOOT)
      .setChildOutput(null) // only adults
      .save(consumer, this.modResource(folder + "rabbit_foot"));
    // befeathering
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.CHICKEN), ItemOutput.fromStack(new ItemStack(Items.FEATHER, 2)))
      .setChildOutput(null) // only adults
      .save(consumer, this.modResource(folder + "chicken_feather"));
    // beshrooming
    SpecialRecipeBuilder.special(TinkerModifiers.mooshroomDemushroomingSerializer.get()).save(consumer, this.modPrefix(folder + "mooshroom_shroom"));
    // beshelling
    SeveringRecipeBuilder.severing(EntityIngredient.of(EntityType.TURTLE), Items.TURTLE_HELMET)
      .setChildOutput(ItemOutput.fromItem(Items.SCUTE))
      .save(consumer, this.modResource(folder + "turtle_shell"));
    // befleecing
    SpecialRecipeBuilder.special(TinkerModifiers.sheepShearing.get()).save(consumer, this.modPrefix(folder + "sheep_wool"));
  }

  /**
   * Adds recipes for a plate armor texture
   */
  private void plateTexture(Consumer<FinishedRecipe> consumer, MaterialId material, boolean optional, String folder) {
    this.plateTexture(consumer, material, material.getPath() + "_ingots", optional, folder);
  }

  /**
   * Adds recipes for a plate armor texture with a custom tag
   */
  private void plateTexture(Consumer<FinishedRecipe> consumer, MaterialVariantId material, String tag, boolean optional, String folder) {
    Ingredient ingot = Ingredient.of(this.getItemTag("c", tag));
    if (optional) {
      consumer = this.withCondition(consumer, this.tagCondition(tag));
    }
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, material.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_METAL)
      .addInput(ingot).addInput(ingot).addInput(ingot)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_" + material.getLocation('_').getPath()));
  }

  /**
   * Adds recipes for a slime armor texture
   */
  private void slimeTexture(Consumer<FinishedRecipe> consumer, MaterialId material, SlimeType slime, String folder) {
    ItemLike congealed = TinkerWorld.congealedSlime.get(slime);
    SwappableModifierRecipeBuilder.modifier(TinkerModifiers.embellishment, material.toString())
      .setTools(TinkerTags.Items.EMBELLISHMENT_SLIME)
      .addInput(congealed).addInput(TinkerWorld.slime.get(slime)).addInput(congealed)
      .save(consumer, this.wrap(TinkerModifiers.embellishment, folder, "_" + slime.getSerializedName()));
  }

  /**
   * Adds haste like recipes using redstone
   */
  public void hasteRecipes(Consumer<FinishedRecipe> consumer, ModifierId modifier, Ingredient tools, int maxLevel, @Nullable String recipeFolder, @Nullable String salvageFolder) {
    IncrementalModifierRecipeBuilder builder = IncrementalModifierRecipeBuilder
      .modifier(modifier)
      .setTools(tools)
      .setInput(Tags.Items.DUSTS_REDSTONE, 1, 45)
      .setMaxLevel(maxLevel)
      .setSlots(SlotType.UPGRADE, 1);
    if (salvageFolder != null) {
      builder.saveSalvage(consumer, this.prefix(modifier, salvageFolder));
    }
    if (recipeFolder != null) {
      builder.save(consumer, this.wrap(modifier, recipeFolder, "_from_dust"));
      IncrementalModifierRecipeBuilder.modifier(modifier)
        .setTools(tools)
        .setInput(Tags.Items.STORAGE_BLOCKS_REDSTONE, 9, 45)
        .setLeftover(new ItemStack(Items.REDSTONE))
        .setMaxLevel(maxLevel)
        .setSlots(SlotType.UPGRADE, 1)
        .disallowCrystal() // avoid redundancy, though in this case the end result is the same
        .save(consumer, this.wrap(modifier, recipeFolder, "_from_block"));
    }
  }

  /**
   * Prefixes the modifier ID with the given prefix
   */
  public ResourceLocation prefix(LazyModifier modifier, String prefix) {
    return this.prefix(modifier.getId(), prefix);
  }

  /**
   * Prefixes the modifier ID with the given prefix and suffix
   */
  public ResourceLocation wrap(LazyModifier modifier, String prefix, String suffix) {
    return this.wrap(modifier.getId(), prefix, suffix);
  }

  /**
   * Just a helper for consistency of requirements errors
   */
  private static String makeRequirementsError(String recipe) {
    return TConstruct.makeTranslationKey("recipe", "modifier." + recipe);
  }

  /**
   * Creates a compound ingredient from multiple tags
   *
   * @param tags Tags to use
   * @return Compound ingredient
   */
  @SafeVarargs
  private static Ingredient ingredientFromTags(TagKey<Item>... tags) {
    Ingredient[] tagIngredients = new Ingredient[tags.length];
    for (int i = 0; i < tags.length; i++) {
      tagIngredients[i] = Ingredient.of(tags[i]);
    }
    return DefaultCustomIngredients.any(tagIngredients);
  }
}
