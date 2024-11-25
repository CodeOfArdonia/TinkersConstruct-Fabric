package slimeknights.tconstruct.common.data;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.ConditionJsonProvider;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.RequirementsStrategy;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemDurabilityTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemUsedOnLocationTrigger;
import net.minecraft.advancements.critereon.LocationPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.advancements.critereon.NbtPredicate;
import net.minecraft.advancements.critereon.PlayerInteractTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.data.GenericDataProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.json.ConfigEnabledCondition;
import slimeknights.tconstruct.common.registration.CastItemObject;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierMatch;
import slimeknights.tconstruct.library.tools.ToolPredicate;
import slimeknights.tconstruct.library.tools.nbt.MaterialIdNBT;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.TinkerMaterials;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.shared.inventory.BlockContainerOpenedTrigger;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.SearedLanternBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.item.TankItem;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerToolParts;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.data.material.MaterialIds;
import slimeknights.tconstruct.tools.item.ArmorSlotType;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class AdvancementsProvider extends GenericDataProvider {

  /**
   * Advancment consumer instance
   */
  protected Consumer<Advancement> advancementConsumer;
  /**
   * Advancment consumer instance
   */
  protected BiConsumer<Advancement, ConditionJsonProvider> conditionalConsumer;

  public AdvancementsProvider(FabricDataOutput output) {
    super(output, "advancements");
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Advancements";
  }

  /**
   * Generates the advancements
   */
  protected void generate() {
    // tinkering path
    Advancement materialsAndYou = this.builder(TinkerCommons.materialsAndYou, this.resource("tools/materials_and_you"), this.resource("textures/gui/advancement_background.png"), FrameType.TASK, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.materialsAndYou)));
    Advancement partBuilder = this.builder(TinkerTables.partBuilder, this.resource("tools/part_builder"), materialsAndYou, FrameType.TASK, builder ->
      builder.addCriterion("crafted_block", this.hasItem(TinkerTables.partBuilder)));
    this.builder(TinkerToolParts.pickHead.get().withMaterialForDisplay(MaterialIds.wood), this.resource("tools/make_part"), partBuilder, FrameType.TASK, builder ->
      builder.addCriterion("crafted_part", this.hasTag(TinkerTags.Items.TOOL_PARTS)));
    Advancement tinkerStation = this.builder(TinkerTables.tinkerStation, this.resource("tools/tinker_station"), partBuilder, FrameType.TASK, builder ->
      builder.addCriterion("crafted_block", this.hasItem(TinkerTables.tinkerStation)));
    Advancement tinkerTool = this.builder(TinkerTools.pickaxe.get().getRenderTool(), this.resource("tools/tinker_tool"), tinkerStation, FrameType.TASK, builder ->
      builder.addCriterion("crafted_tool", this.hasTag(TinkerTags.Items.MULTIPART_TOOL)));
    this.builder(TinkerMaterials.manyullyn.getIngot(), this.resource("tools/material_master"), tinkerTool, FrameType.CHALLENGE, builder -> {
      Consumer<MaterialId> with = id -> builder.addCriterion(id.getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder().withMaterial(id).build()));
      // tier 1
      with.accept(MaterialIds.wood);
      with.accept(MaterialIds.flint);
      with.accept(MaterialIds.rock);
      with.accept(MaterialIds.bone);
      with.accept(MaterialIds.necroticBone);
      with.accept(MaterialIds.leather);
      with.accept(MaterialIds.string);
      with.accept(MaterialIds.vine);
      with.accept(MaterialIds.bamboo);
      // tier 2
      with.accept(MaterialIds.iron);
      with.accept(MaterialIds.searedStone);
      with.accept(MaterialIds.scorchedStone);
      with.accept(MaterialIds.copper);
      with.accept(MaterialIds.slimewood);
      with.accept(MaterialIds.chain);
      with.accept(MaterialIds.skyslimeVine);
      // tier 3
      with.accept(MaterialIds.roseGold);
      with.accept(MaterialIds.slimesteel);
      with.accept(MaterialIds.nahuatl);
      with.accept(MaterialIds.amethystBronze);
      with.accept(MaterialIds.pigIron);
      with.accept(MaterialIds.cobalt);
      with.accept(MaterialIds.darkthread);
      // tier 4
      with.accept(MaterialIds.manyullyn);
      with.accept(MaterialIds.hepatizon);
      with.accept(MaterialIds.queensSlime);
      with.accept(MaterialIds.blazingBone);
      with.accept(MaterialIds.ancientHide);
      with.accept(MaterialIds.enderslimeVine);
    });
    this.builder(TinkerTools.travelersGear.get(ArmorSlotType.HELMET).getRenderTool(), this.resource("tools/travelers_gear"), tinkerStation, FrameType.TASK, builder ->
      TinkerTools.travelersGear.forEach((type, armor) -> builder.addCriterion("crafted_" + type.getSerializedName(), this.hasItem(armor))));
    this.builder(TinkerTools.pickaxe.get().getRenderTool(), this.resource("tools/tool_smith"), tinkerTool, FrameType.CHALLENGE, builder -> {
      Consumer<Item> with = item -> builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).getPath(), this.hasItem(item));
      with.accept(TinkerTools.pickaxe.get());
      with.accept(TinkerTools.mattock.get());
      with.accept(TinkerTools.pickadze.get());
      with.accept(TinkerTools.handAxe.get());
      with.accept(TinkerTools.kama.get());
      with.accept(TinkerTools.dagger.get());
      with.accept(TinkerTools.sword.get());
    });
    Advancement modified = this.builder(Items.REDSTONE, this.resource("tools/modified"), tinkerTool, FrameType.TASK, builder ->
      builder.addCriterion("crafted_tool", InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder().hasUpgrades(true).build())));
    //    builder(TinkerTools.cleaver.get().buildToolForRendering(), location("tools/glass_cannon"), modified, FrameType.CHALLENGE, builder ->
    //      builder.addCriterion()("crafted_tool", InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder()
    //                                                                                                  .withStat(StatPredicate.max(ToolStats.DURABILITY, 100))
    //                                                                                                  .withStat(StatPredicate.min(ToolStats.ATTACK_DAMAGE, 20))
    //                                                                                                  .build())));
    this.builder(Items.WRITABLE_BOOK, this.resource("tools/upgrade_slots"), modified, FrameType.CHALLENGE, builder ->
      builder.addCriterion("has_modified", InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder().upgrades(
        ModifierMatch.list(5, ModifierMatch.entry(ModifierIds.writable),
          ModifierMatch.entry(ModifierIds.recapitated),
          ModifierMatch.entry(ModifierIds.harmonious),
          ModifierMatch.entry(ModifierIds.resurrected),
          ModifierMatch.entry(ModifierIds.gilded))).build()))
    );

    // smeltery path
    Advancement punySmelting = this.builder(TinkerCommons.punySmelting, this.resource("smeltery/puny_smelting"), materialsAndYou, FrameType.TASK, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.punySmelting)));
    Advancement melter = this.builder(TinkerSmeltery.searedMelter, this.resource("smeltery/melter"), punySmelting, FrameType.TASK, builder -> {
      Consumer<Block> with = block -> builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath(), ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
      with.accept(TinkerSmeltery.searedMelter.get());
      with.accept(TinkerSmeltery.searedTable.get());
      with.accept(TinkerSmeltery.searedBasin.get());
      with.accept(TinkerSmeltery.searedFaucet.get());
      with.accept(TinkerSmeltery.searedHeater.get());
      TinkerSmeltery.searedTank.forEach(with);
      // first 4 are required, and then any of the last 5
      builder.requirements(new CountRequirementsStrategy(1, 1, 1, 1, 1 + TankType.values().length));
    });
    this.builder(TinkerSmeltery.toolHandleCast.getSand(), this.resource("smeltery/sand_casting"), melter, FrameType.TASK, builder ->
      builder.addCriterion("crafted_cast", this.hasTag(TinkerTags.Items.BLANK_SINGLE_USE_CASTS)));
    Advancement goldCasting = this.builder(TinkerSmeltery.pickHeadCast, this.resource("smeltery/gold_casting"), melter, FrameType.TASK, builder ->
      builder.addCriterion("crafted_cast", this.hasTag(TinkerTags.Items.GOLD_CASTS)));
    this.builder(TinkerSmeltery.hammerHeadCast, this.resource("smeltery/cast_collector"), goldCasting, FrameType.GOAL, builder -> {
      Consumer<CastItemObject> with = cast -> builder.addCriterion(cast.getName().getPath(), this.hasItem(cast.get()));
      with.accept(TinkerSmeltery.ingotCast);
      with.accept(TinkerSmeltery.nuggetCast);
      with.accept(TinkerSmeltery.gemCast);
      with.accept(TinkerSmeltery.rodCast);
      with.accept(TinkerSmeltery.repairKitCast);
      // parts
      with.accept(TinkerSmeltery.pickHeadCast);
      with.accept(TinkerSmeltery.smallAxeHeadCast);
      with.accept(TinkerSmeltery.smallBladeCast);
      with.accept(TinkerSmeltery.hammerHeadCast);
      with.accept(TinkerSmeltery.broadBladeCast);
      with.accept(TinkerSmeltery.broadAxeHeadCast);
      with.accept(TinkerSmeltery.toolBindingCast);
      with.accept(TinkerSmeltery.roundPlateCast);
      with.accept(TinkerSmeltery.largePlateCast);
      with.accept(TinkerSmeltery.toolHandleCast);
      with.accept(TinkerSmeltery.toughHandleCast);
      with.accept(TinkerSmeltery.bowLimbCast);
      with.accept(TinkerSmeltery.bowGripCast);
    });
    Advancement mightySmelting = this.builder(TinkerCommons.mightySmelting, this.resource("smeltery/mighty_smelting"), melter, FrameType.TASK, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.mightySmelting)));
    Advancement smeltery = this.builder(TinkerSmeltery.smelteryController, this.resource("smeltery/structure"), mightySmelting, FrameType.TASK, builder ->
      builder.addCriterion("open_smeltery", BlockContainerOpenedTrigger.Instance.container(TinkerSmeltery.smeltery.get())));
    Advancement anvil = this.builder(TinkerTables.tinkersAnvil, this.resource("smeltery/tinkers_anvil"), smeltery, FrameType.GOAL, builder -> {
      builder.addCriterion("crafted_overworld", this.hasItem(TinkerTables.tinkersAnvil));
      builder.addCriterion("crafted_nether", this.hasItem(TinkerTables.scorchedAnvil));
      builder.requirements(RequirementsStrategy.OR);
    });
    this.builder(TinkerTools.veinHammer.get().getRenderTool(), this.resource("smeltery/tool_forge"), anvil, FrameType.CHALLENGE, builder -> {
      Consumer<Item> with = item -> builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item)).getPath(), this.hasItem(item));
      with.accept(TinkerTools.sledgeHammer.get());
      with.accept(TinkerTools.veinHammer.get());
      with.accept(TinkerTools.excavator.get());
      with.accept(TinkerTools.broadAxe.get());
      with.accept(TinkerTools.scythe.get());
      with.accept(TinkerTools.cleaver.get());
      with.accept(TinkerTools.longbow.get());
    });
    this.builder(TinkerModifiers.silkyCloth, this.resource("smeltery/abilities"), anvil, FrameType.CHALLENGE, builder -> {
      Consumer<ModifierId> with = modifier -> builder.addCriterion(modifier.getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder().modifiers(ModifierMatch.entry(modifier)).build()));
      Consumer<LazyModifier> withL = modifier -> with.accept(modifier.getId());

      // general
      with.accept(ModifierIds.gilded);
      with.accept(ModifierIds.luck);
      with.accept(ModifierIds.reach);
      withL.accept(TinkerModifiers.unbreakable);
      // armor
      withL.accept(TinkerModifiers.aquaAffinity);
      withL.accept(TinkerModifiers.bouncy);
      withL.accept(TinkerModifiers.doubleJump);
      withL.accept(TinkerModifiers.flamewake);
      withL.accept(TinkerModifiers.frostWalker);
      withL.accept(TinkerModifiers.pathMaker);
      withL.accept(TinkerModifiers.plowing);
      with.accept(ModifierIds.pockets);
      withL.accept(TinkerModifiers.slurping);
      withL.accept(TinkerModifiers.snowdrift);
      with.accept(ModifierIds.strength);
      with.accept(ModifierIds.toolBelt);
      withL.accept(TinkerModifiers.ambidextrous);
      withL.accept(TinkerModifiers.zoom);
      withL.accept(TinkerModifiers.longFall);
      withL.accept(TinkerModifiers.reflecting);
      // harvest
      withL.accept(TinkerModifiers.autosmelt);
      withL.accept(TinkerModifiers.exchanging);
      withL.accept(TinkerModifiers.expanded);
      withL.accept(TinkerModifiers.silky);
      // interact
      withL.accept(TinkerModifiers.bucketing);
      withL.accept(TinkerModifiers.firestarter);
      withL.accept(TinkerModifiers.glowing);
      withL.accept(TinkerModifiers.pathing);
      withL.accept(TinkerModifiers.stripping);
      withL.accept(TinkerModifiers.tilling);
      // weapon
      withL.accept(TinkerModifiers.dualWielding);
      withL.accept(TinkerModifiers.melting);
      withL.accept(TinkerModifiers.spilling);
      withL.accept(TinkerModifiers.blocking);
      withL.accept(TinkerModifiers.parrying);
      // ranged
      withL.accept(TinkerModifiers.crystalshot);
      withL.accept(TinkerModifiers.multishot);
      withL.accept(TinkerModifiers.bulkQuiver);
      withL.accept(TinkerModifiers.trickQuiver);
    });

    // foundry path
    Advancement fantasticFoundry = this.builder(TinkerCommons.fantasticFoundry, this.resource("foundry/fantastic_foundry"), materialsAndYou, FrameType.TASK, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.fantasticFoundry)));
    this.builder(TinkerCommons.encyclopedia, this.resource("foundry/encyclopedia"), fantasticFoundry, FrameType.GOAL, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.encyclopedia)));
    Advancement alloyer = this.builder(TinkerSmeltery.scorchedAlloyer, this.resource("foundry/alloyer"), fantasticFoundry, FrameType.TASK, builder -> {
      Consumer<Block> with = block -> builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath(), ItemUsedOnLocationTrigger.TriggerInstance.placedBlock(block));
      with.accept(TinkerSmeltery.scorchedAlloyer.get());
      with.accept(TinkerSmeltery.scorchedFaucet.get());
      with.accept(TinkerSmeltery.scorchedTable.get());
      with.accept(TinkerSmeltery.scorchedBasin.get());
      for (TankType type : TankType.values()) {
        with.accept(TinkerSmeltery.scorchedTank.get(type));
      }
      builder.requirements(new CountRequirementsStrategy(1, 1, 1, 1, 2, 2));
    });
    Advancement foundry = this.builder(TinkerSmeltery.foundryController, this.resource("foundry/structure"), alloyer, FrameType.TASK, builder ->
      builder.addCriterion("open_foundry", BlockContainerOpenedTrigger.Instance.container(TinkerSmeltery.foundry.get())));
    Advancement blazingBlood = this.builder(TankItem.setTank(new ItemStack(TinkerSmeltery.scorchedTank.get(TankType.FUEL_GAUGE)), getTankWith(TinkerFluids.blazingBlood.get(), TankType.FUEL_GAUGE.getCapacity())),
      this.resource("foundry/blaze"), foundry, FrameType.GOAL, builder -> {
        Consumer<SearedTankBlock> with = block -> {
          CompoundTag nbt = new CompoundTag();
          nbt.put(NBTTags.TANK, getTankWith(TinkerFluids.blazingBlood.get(), block.getCapacity()).writeToNBT(new CompoundTag()));
          builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath(),
            InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(block).hasNbt(nbt).build()));
          builder.requirements(RequirementsStrategy.OR);
        };
        TinkerSmeltery.searedTank.forEach(with);
        TinkerSmeltery.scorchedTank.forEach(with);
      });
    this.builder(TinkerTools.plateArmor.get(ArmorSlotType.CHESTPLATE).getRenderTool(), this.resource("foundry/plate_armor"), blazingBlood, FrameType.GOAL, builder ->
      TinkerTools.plateArmor.forEach((type, armor) -> builder.addCriterion("crafted_" + type.getSerializedName(), this.hasItem(armor))));
    this.builder(TankItem.setTank(new ItemStack(TinkerSmeltery.scorchedLantern), getTankWith(TinkerFluids.moltenManyullyn.get(), TinkerSmeltery.scorchedLantern.get().getCapacity())),
      this.resource("foundry/manyullyn_lanterns"), foundry, FrameType.CHALLENGE, builder -> {
        Consumer<SearedLanternBlock> with = block -> {
          CompoundTag nbt = new CompoundTag();
          nbt.put(NBTTags.TANK, getTankWith(TinkerFluids.moltenManyullyn.get(), block.getCapacity()).writeToNBT(new CompoundTag()));
          builder.addCriterion(Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(block)).getPath(),
            InventoryChangeTrigger.TriggerInstance.hasItems(new ItemPredicate(null, Collections.singleton(block.asItem()), MinMaxBounds.Ints.atLeast(64), MinMaxBounds.Ints.ANY,
              EnchantmentPredicate.NONE, EnchantmentPredicate.NONE, null, new NbtPredicate(nbt))));
          builder.requirements(RequirementsStrategy.OR);
        };
        with.accept(TinkerSmeltery.searedLantern.get());
        with.accept(TinkerSmeltery.scorchedLantern.get());
      });

    // exploration path
    Advancement tinkersGadgetry = this.builder(TinkerCommons.tinkersGadgetry, this.resource("world/tinkers_gadgetry"), materialsAndYou, FrameType.TASK, builder ->
      builder.addCriterion("crafted_book", this.hasItem(TinkerCommons.tinkersGadgetry)));
    this.builder(TinkerWorld.slimeSapling.get(SlimeType.EARTH), this.resource("world/earth_island"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("found_island", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(TinkerStructures.earthSlimeIslandKey))));
    this.builder(TinkerWorld.slimeSapling.get(SlimeType.SKY), this.resource("world/sky_island"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("found_island", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(TinkerStructures.skySlimeIslandKey))));
    this.builder(TinkerWorld.slimeSapling.get(SlimeType.BLOOD), this.resource("world/blood_island"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("found_island", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(TinkerStructures.bloodIslandKey))));
    Advancement enderslimeIsland = this.builder(TinkerWorld.slimeSapling.get(SlimeType.ENDER), this.resource("world/ender_island"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("found_island", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(TinkerStructures.endSlimeIslandKey))));
    this.builder(Items.CLAY_BALL, this.resource("world/clay_island"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("found_island", PlayerTrigger.TriggerInstance.located(LocationPredicate.inStructure(TinkerStructures.clayIslandKey))));
    Advancement slimes = this.builder(TinkerCommons.slimeball.get(SlimeType.ICHOR), this.resource("world/slime_collector"), tinkersGadgetry, FrameType.TASK, builder -> {
      for (SlimeType type : SlimeType.values()) {
        builder.addCriterion(type.getSerializedName(), this.hasTag(type.getSlimeballTag()));
      }
      builder.addCriterion("magma_cream", this.hasItem(Items.MAGMA_CREAM));
    });
    this.builder(TinkerGadgets.slimeSling.get(SlimeType.ENDER), this.resource("world/slime_sling"), slimes, FrameType.CHALLENGE, builder -> {
      JsonObject boundJSON = new JsonObject();
      boundJSON.addProperty("max", 150);
      MinMaxBounds.Ints mojangDeletedTheMaxMethods = MinMaxBounds.Ints.fromJson(boundJSON);
      TinkerGadgets.slimeSling.forEach((type, sling) -> builder.addCriterion(type.getSerializedName(), ItemDurabilityTrigger.TriggerInstance.changedDurability(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(sling).build(), mojangDeletedTheMaxMethods)));
    });
    this.builder(TinkerGadgets.piggyBackpack, this.resource("world/piggybackpack"), tinkersGadgetry, FrameType.GOAL, builder ->
      builder.addCriterion("used_pack", PlayerInteractTrigger.TriggerInstance.itemUsedOnEntity(ContextAwarePredicate.ANY, ItemPredicate.Builder.item().of(TinkerGadgets.piggyBackpack), EntityPredicate.wrap(EntityPredicate.Builder.entity().of(EntityType.PIG).build()))));
    Advancement slimesuit = this.builder(TinkerTools.slimesuit.get(ArmorSlotType.CHESTPLATE).getRenderTool(), this.resource("world/slimesuit"), enderslimeIsland, FrameType.GOAL, builder ->
      TinkerTools.slimesuit.forEach((type, armor) -> builder.addCriterion("crafted_" + type.getSerializedName(), this.hasItem(armor))));
    this.builder(new MaterialIdNBT(Collections.singletonList(MaterialIds.glass)).updateStack(new ItemStack(TinkerTools.slimesuit.get(ArmorSlotType.HELMET))),
      this.resource("world/slimeskull"), slimesuit, FrameType.CHALLENGE, builder -> {
        Item helmet = TinkerTools.slimesuit.get(ArmorSlotType.HELMET);
        Consumer<MaterialId> with = mat -> builder.addCriterion(mat.getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(ToolPredicate.builder(helmet).withMaterial(mat).build()));
        with.accept(MaterialIds.glass);
        with.accept(MaterialIds.bone);
        with.accept(MaterialIds.necroticBone);
        with.accept(MaterialIds.rottenFlesh);
        with.accept(MaterialIds.enderPearl);
        with.accept(MaterialIds.bloodbone);
        with.accept(MaterialIds.string);
        with.accept(MaterialIds.darkthread);
        with.accept(MaterialIds.iron);
        with.accept(MaterialIds.copper);
        with.accept(MaterialIds.blazingBone);
        with.accept(MaterialIds.gold);
        with.accept(MaterialIds.roseGold);
        with.accept(MaterialIds.pigIron);
      });

    // internal advancements
    this.hiddenBuilder(this.resource("internal/starting_book"), ConfigEnabledCondition.SPAWN_WITH_BOOK, builder -> {
      builder.addCriterion("tick", PlayerTrigger.TriggerInstance.tick());
      builder.rewards(AdvancementRewards.Builder.loot(TConstruct.getResource("gameplay/starting_book")));
    });
  }

  /**
   * Gets a tank filled with the given fluid
   */
  private static FluidTank getTankWith(Fluid fluid, long capacity) {
    FluidTank tank = new FluidTank(capacity);
    TransferUtil.insert(tank, FluidVariant.of(fluid), capacity);
    return tank;
  }

  /**
   * Creates an item predicate for a tag
   */
  private CriterionTriggerInstance hasTag(TagKey<Item> tag) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(tag).build());
  }

  /**
   * Creates an item predicate for an item
   */
  private CriterionTriggerInstance hasItem(ItemLike item) {
    return InventoryChangeTrigger.TriggerInstance.hasItems(ItemPredicate.Builder.item().of(item).build());
  }

  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    Set<ResourceLocation> set = Sets.newHashSet();
    List<CompletableFuture<?>> futures = new ArrayList<>();
    this.advancementConsumer = advancement -> {
      if (!set.add(advancement.getId())) {
        throw new IllegalStateException("Duplicate advancement " + advancement.getId());
      } else {
        futures.add(this.saveThing(cache, advancement.getId(), advancement.deconstruct().serializeToJson()));
      }
    };
    this.conditionalConsumer = (advancement, condition) -> {
      if (!set.add(advancement.getId())) {
        throw new IllegalStateException("Duplicate advancement " + advancement.getId());
      } else {
        JsonObject jsonObject = advancement.deconstruct().serializeToJson();
        ConditionJsonProvider.write(jsonObject, condition);
        futures.add(this.saveThing(cache, advancement.getId(), jsonObject));
      }
    };
    this.generate();
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }


  /* Helpers */

  /**
   * Gets a tinkers resource location
   */
  protected ResourceLocation resource(String name) {
    return TConstruct.getResource(name);
  }

  /**
   * Helper for making an advancement builder
   *
   * @param display Item to display
   * @param name    Advancement name
   * @param parent  Parent advancement
   * @param frame   Frame type
   * @return Builder
   */
  protected Advancement builder(ItemLike display, ResourceLocation name, Advancement parent, FrameType frame, Consumer<Advancement.Builder> consumer) {
    return this.builder(new ItemStack(display), name, parent, frame, consumer);
  }

  /**
   * Helper for making an advancement builder
   *
   * @param display Stack to display
   * @param name    Advancement name
   * @param parent  Parent advancement
   * @param frame   Frame type
   * @return Builder
   */
  protected Advancement builder(ItemStack display, ResourceLocation name, Advancement parent, FrameType frame, Consumer<Advancement.Builder> consumer) {
    return this.builder(display, name, (ResourceLocation) null, frame, builder -> {
      builder.parent(parent);
      consumer.accept(builder);
    });
  }

  /**
   * Helper for making an advancement builder
   *
   * @param display    Item to display
   * @param name       Advancement name
   * @param background Background image
   * @param frame      Frame type
   * @return Builder
   */
  protected Advancement builder(ItemLike display, ResourceLocation name, @Nullable ResourceLocation background, FrameType frame, Consumer<Advancement.Builder> consumer) {
    return this.builder(new ItemStack(display), name, background, frame, consumer);
  }

  /**
   * Makes an advancement translation key from the given ID
   */
  private static String makeTranslationKey(ResourceLocation advancement) {
    return "advancements." + advancement.getNamespace() + "." + advancement.getPath().replace('/', '.');
  }

  /**
   * Helper for making an advancement builder
   *
   * @param display    Stack to display
   * @param name       Advancement name
   * @param background Background image
   * @param frame      Frame type
   * @return Builder
   */
  protected Advancement builder(ItemStack display, ResourceLocation name, @Nullable ResourceLocation background, FrameType frame, Consumer<Advancement.Builder> consumer) {
    Advancement.Builder builder = Advancement.Builder
      .advancement().display(display,
        Component.translatable(makeTranslationKey(name) + ".title"),
        Component.translatable(makeTranslationKey(name) + ".description"),
        background, frame, true, frame != FrameType.TASK, false);
    consumer.accept(builder);
    return builder.save(this.advancementConsumer, name.toString());
  }

  /**
   * Helper for making an advancement builder
   *
   * @param name Advancement name
   */
  protected void hiddenBuilder(ResourceLocation name, ConditionJsonProvider condition, Consumer<Advancement.Builder> consumer) {
    Advancement.Builder builder = Advancement.Builder.advancement();
    consumer.accept(builder);
    builder.save(advancement -> this.conditionalConsumer.accept(advancement, condition), name.toString());
  }
}
