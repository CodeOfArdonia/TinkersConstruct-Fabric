package slimeknights.tconstruct.common.data.loot;

import io.github.fabricators_of_create.porting_lib.loot.LootTableIdCondition;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.advancements.critereon.EntityEquipmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import slimeknights.mantle.data.GlobalLootModifierProvider;
import slimeknights.mantle.loot.AddEntryLootModifier;
import slimeknights.mantle.loot.ReplaceItemLootModifier;
import slimeknights.mantle.loot.condition.BlockTagLootCondition;
import slimeknights.mantle.loot.condition.ContainsItemModifierLootCondition;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.json.BlockOrEntityCondition;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.library.json.TagNotEmptyLootCondition;
import slimeknights.tconstruct.library.json.TagPreferenceLootEntry;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.smeltery.data.SmelteryCompat;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.modifiers.ModifierLootModifier;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteBonusFunction;
import slimeknights.tconstruct.tools.modifiers.loot.ChrysophiliteLootCondition;
import slimeknights.tconstruct.tools.modifiers.loot.HasModifierLootCondition;
import slimeknights.tconstruct.tools.modifiers.loot.ModifierBonusLootFunction;

public class GlobalLootModifiersProvider extends GlobalLootModifierProvider {

  public GlobalLootModifiersProvider(FabricDataOutput output) {
    super(output, TConstruct.MOD_ID);
  }

  @Override
  protected void start() {
    ReplaceItemLootModifier.builder(Ingredient.of(Items.BONE), ItemOutput.fromItem(TinkerMaterials.necroticBone))
      .addCondition(LootTableIdCondition.builder(new ResourceLocation("entities/wither_skeleton")).build())
      .addCondition(ConfigEnabledCondition.WITHER_BONE_DROP)
      .build("wither_bone", this);

    // generic modifier hook
    ItemPredicate.Builder meleeHarvest = ItemPredicate.Builder.item().of(TinkerTags.Items.MELEE_OR_HARVEST);
    ModifierLootModifier.builder()
      .addCondition(BlockOrEntityCondition.INSTANCE)
      .addCondition(MatchTool.toolMatches(meleeHarvest)
        .or(LootItemEntityPropertyCondition.hasProperties(EntityTarget.KILLER, EntityPredicate.Builder.entity().equipment(mainHand(meleeHarvest.build()))))
        .build())
      .build("modifier_hook", this);

    // chrysophilite modifier hook
    AddEntryLootModifier.builder(LootItem.lootTableItem(Items.GOLD_NUGGET))
      .addCondition(new BlockTagLootCondition(TinkerTags.Blocks.CHRYSOPHILITE_ORES))
      .addCondition(new ContainsItemModifierLootCondition(Ingredient.of(TinkerTags.Items.CHRYSOPHILITE_ORES)).inverted())
      .addCondition(ChrysophiliteLootCondition.INSTANCE)
      .addFunction(SetItemCountFunction.setCount(UniformGenerator.between(2, 6)).build())
      .addFunction(ChrysophiliteBonusFunction.oreDrops(false).build())
      .addFunction(ApplyExplosionDecay.explosionDecay().build())
      .build("chrysophilite_modifier", this);

    // lustrous implementation
    this.addLustrous("iron", false);
    this.addLustrous("gold", false);
    this.addLustrous("copper", false);
    this.addLustrous("cobalt", false);
    this.addLustrous("netherite_scrap", false);
    for (SmelteryCompat compat : SmelteryCompat.values()) {
      if (compat.isOre()) {
        this.addLustrous(compat.getName(), true);
      }
    }
  }

  /**
   * Adds lustrous for an ore
   */
  private void addLustrous(String name, boolean optional) {
    TagKey<Item> nuggets = TagKey.create(Registries.ITEM, new ResourceLocation("c", name + "_nuggets"));
    ResourceLocation ores = new ResourceLocation("c", name + "_ores");
    AddEntryLootModifier.Builder builder = AddEntryLootModifier.builder(TagPreferenceLootEntry.tagPreference(nuggets));
    builder.addCondition(new BlockTagLootCondition(TagKey.create(Registries.BLOCK, ores)))
      .addCondition(new ContainsItemModifierLootCondition(Ingredient.of(TagKey.create(Registries.ITEM, ores))).inverted());
    if (optional) {
      builder.addCondition(new TagNotEmptyLootCondition<>(nuggets));
    }
    builder.addCondition(new HasModifierLootCondition(ModifierIds.lustrous))
      .addFunction(SetItemCountFunction.setCount(UniformGenerator.between(2, 4)).build())
      .addFunction(ModifierBonusLootFunction.oreDrops(ModifierIds.lustrous, false).build())
      .addFunction(ApplyExplosionDecay.explosionDecay().build())
      .build("lustrous/" + name, this);
  }

  /**
   * Creates an equipment predicate for mainhand
   */
  private static EntityEquipmentPredicate mainHand(ItemPredicate mainHand) {
    EntityEquipmentPredicate.Builder builder = EntityEquipmentPredicate.Builder.equipment();
    builder.mainhand = mainHand;
    return builder.build();
  }
}
