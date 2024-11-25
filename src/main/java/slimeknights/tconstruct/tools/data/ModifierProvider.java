package slimeknights.tconstruct.tools.data;

import io.github.fabricators_of_create.porting_lib.attributes.PortingLibAttributes;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.enchantment.Enchantments;
import slimeknights.mantle.data.predicate.IJsonPredicate;
import slimeknights.mantle.data.predicate.block.BlockPredicate;
import slimeknights.mantle.data.predicate.entity.LivingEntityPredicate;
import slimeknights.mantle.data.predicate.entity.MobTypePredicate;
import slimeknights.mantle.data.predicate.entity.TagEntityPredicate;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierProvider;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.modifiers.dynamic.ComposableModifier.TooltipDisplay;
import slimeknights.tconstruct.library.modifiers.dynamic.InventoryMenuModifier;
import slimeknights.tconstruct.library.modifiers.modules.AttributeModule;
import slimeknights.tconstruct.library.modifiers.modules.ConditionalDamageModule;
import slimeknights.tconstruct.library.modifiers.modules.ConditionalMiningSpeedModule;
import slimeknights.tconstruct.library.modifiers.modules.EnchantmentModule;
import slimeknights.tconstruct.library.modifiers.modules.IncrementalModule;
import slimeknights.tconstruct.library.modifiers.modules.LootingModule;
import slimeknights.tconstruct.library.modifiers.modules.MobDisguiseModule;
import slimeknights.tconstruct.library.modifiers.modules.MobEffectModule;
import slimeknights.tconstruct.library.modifiers.modules.ModifierSlotModule;
import slimeknights.tconstruct.library.modifiers.modules.RarityModule;
import slimeknights.tconstruct.library.modifiers.modules.RepairModule;
import slimeknights.tconstruct.library.modifiers.modules.SwappableSlotModule;
import slimeknights.tconstruct.library.modifiers.modules.ToolStatModule;
import slimeknights.tconstruct.library.modifiers.modules.VolatileFlagModule;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay.UniqueForLevels;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.item.ModifiableArmorItem;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.ScalingValue;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.ability.armor.ToolBeltModifier;
import slimeknights.tconstruct.tools.modifiers.slotless.OverslimeModifier;

import static slimeknights.tconstruct.common.TinkerTags.Items.ARMOR;

public class ModifierProvider extends AbstractModifierProvider {

  public ModifierProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  protected void addModifiers() {
    EquipmentSlot[] handSlots = {EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND};
    EquipmentSlot[] armorSlots = ModifiableArmorMaterial.ARMOR_SLOTS;
    EquipmentSlot[] armorMainHand = {EquipmentSlot.MAINHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD};

    // extra modifier slots
    ModifierSlotModule UPGRADE = new ModifierSlotModule(SlotType.UPGRADE);
    this.buildModifier(ModifierIds.writable).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(UPGRADE);
    this.buildModifier(ModifierIds.recapitated).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(UPGRADE);
    this.buildModifier(ModifierIds.harmonious).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(UPGRADE);
    this.buildModifier(ModifierIds.resurrected).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(UPGRADE);
    this.buildModifier(ModifierIds.gilded).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(new ModifierSlotModule(SlotType.UPGRADE, 2));
    this.buildModifier(ModifierIds.draconic).tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL).addModule(new ModifierSlotModule(SlotType.ABILITY, 1));
    this.buildModifier(ModifierIds.rebalanced)
      .tooltipDisplay(TooltipDisplay.TINKER_STATION).levelDisplay(ModifierLevelDisplay.NO_LEVELS)
      .addModule(new SwappableSlotModule(1)).addModule(new SwappableSlotModule.BonusSlot(SlotType.ABILITY, SlotType.UPGRADE, -1));
    this.addRedirect(id("red_extra_upgrade"), this.redirect(ModifierIds.writable));
    this.addRedirect(id("green_extra_upgrade"), this.redirect(ModifierIds.recapitated));
    this.addRedirect(id("blue_extra_upgrade"), this.redirect(ModifierIds.harmonious));
    this.addRedirect(id("extra_ability"), this.redirect(ModifierIds.draconic));

    // internal modifier migration
    this.addRedirect(id("shovel_flatten"), this.redirect(TinkerModifiers.pathing.getId()));
    this.addRedirect(id("axe_strip"), this.redirect(TinkerModifiers.stripping.getId()));
    this.addRedirect(id("hoe_till"), this.redirect(TinkerModifiers.tilling.getId()));
    this.addRedirect(id("firestarter_hidden"), this.redirect(TinkerModifiers.firestarter.getId()));

    // merged some armor modifiers
    this.addRedirect(id("haste_armor"), this.redirect(TinkerModifiers.haste.getId()));
    this.addRedirect(id("knockback_armor"), this.redirect(TinkerModifiers.knockback.getId()));

    // unarmed rework
    this.addRedirect(id("unarmed"), this.redirect(TinkerModifiers.ambidextrous.getId()));

    // tier upgrades
    // emerald
    this.buildModifier(ModifierIds.emerald)
      .levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL)
      .addModule(new RarityModule(Rarity.UNCOMMON))
      .addModule(ToolStatModule.multiplyBase(ToolStats.DURABILITY, 0.5f))
      .addModule(RepairModule.leveling(0.5f))
      // armor
      .addModule(ToolStatModule.add(ToolStats.KNOCKBACK_RESISTANCE, 0.05f))
      // melee harvest
      .addModule(ToolStatModule.multiplyConditional(ToolStats.ATTACK_DAMAGE, 0.25f))
      .addModule(ToolStatModule.multiplyConditional(ToolStats.MINING_SPEED, 0.25f))
      .addModule(ToolStatModule.update(ToolStats.HARVEST_TIER, Tiers.IRON))
      // ranged
      .addModule(ToolStatModule.add(ToolStats.ACCURACY, 0.1f));
    // diamond
    this.buildModifier(ModifierIds.diamond)
      .levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL)
      .addModule(new RarityModule(Rarity.UNCOMMON))
      .addModule(ToolStatModule.add(ToolStats.DURABILITY, 500))
      // armor grants less durability boost
      .addModule(ToolStatModule.add(ToolStats.DURABILITY, -250, ARMOR))
      .addModule(ToolStatModule.add(ToolStats.ARMOR, 1))
      // melee harvest
      .addModule(ToolStatModule.add(ToolStats.ATTACK_DAMAGE, 0.5f))
      .addModule(ToolStatModule.add(ToolStats.MINING_SPEED, 2))
      .addModule(ToolStatModule.update(ToolStats.HARVEST_TIER, Tiers.DIAMOND))
      // ranged
      .addModule(ToolStatModule.add(ToolStats.PROJECTILE_DAMAGE, 0.5f));
    // netherite
    this.buildModifier(ModifierIds.netherite)
      .levelDisplay(ModifierLevelDisplay.SINGLE_LEVEL)
      .addModule(new RarityModule(Rarity.RARE))
      .addModule(new VolatileFlagModule(IModifiable.INDESTRUCTIBLE_ENTITY))
      .addModule(ToolStatModule.multiplyBase(ToolStats.DURABILITY, 0.2f))
      // armor
      .addModule(ToolStatModule.add(ToolStats.ARMOR_TOUGHNESS, 1))
      .addModule(ToolStatModule.add(ToolStats.KNOCKBACK_RESISTANCE, 0.05f))
      // melee harvest
      .addModule(ToolStatModule.multiplyBase(ToolStats.ATTACK_DAMAGE, 0.2f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.MINING_SPEED, 0.25f))
      .addModule(ToolStatModule.update(ToolStats.HARVEST_TIER, Tiers.NETHERITE))
      // ranged
      .addModule(ToolStatModule.multiplyBase(ToolStats.VELOCITY, 0.1f));

    // general
    this.buildModifier(ModifierIds.worldbound).addModule(new VolatileFlagModule(IModifiable.INDESTRUCTIBLE_ENTITY)).addModule(new RarityModule(Rarity.UNCOMMON)).levelDisplay(ModifierLevelDisplay.NO_LEVELS);
    this.buildModifier(ModifierIds.shiny).addModule(new VolatileFlagModule(IModifiable.SHINY)).addModule(new RarityModule(Rarity.EPIC)).levelDisplay(ModifierLevelDisplay.NO_LEVELS);
    // general abilities
    this.buildModifier(ModifierIds.reach)
      .addModule(IncrementalModule.RECIPE_CONTROLLED)
      .addModule(new AttributeModule("tconstruct.modifier.reach", PortingLibAttributes.BLOCK_REACH, Operation.ADDITION, 1, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
      .addModule(new AttributeModule("tconstruct.modifier.range", PortingLibAttributes.ENTITY_REACH, Operation.ADDITION, 1, EquipmentSlot.MAINHAND, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET));

    // loot
    this.buildModifier(TinkerModifiers.silky).levelDisplay(ModifierLevelDisplay.NO_LEVELS).addModule(new EnchantmentModule.Harvest(Enchantments.SILK_TOUCH));
    EnchantmentModule.Harvest FORTUNE = new EnchantmentModule.Harvest(Enchantments.BLOCK_FORTUNE);
    LootingModule LOOTING = new LootingModule(1);
    this.buildModifier(ModifierIds.luck).levelDisplay(new UniqueForLevels(3)).addModule(FORTUNE).addModule(LOOTING);
    this.buildModifier(ModifierIds.fortune).addModule(FORTUNE);
    this.buildModifier(ModifierIds.looting).addModule(LOOTING);

    /// attack
    this.buildModifier(ModifierIds.sticky)
      .addModule(IncrementalModule.RECIPE_CONTROLLED)
      .addModule(MobEffectModule.builder(MobEffects.MOVEMENT_SLOWDOWN).level(ScalingValue.leveling(0, 0.5f)).time(ScalingValue.random(20, 10)).build());

    // damage boost
    // vanilla give +1, 1.5, 2, 2.5, 3, but that is low
    // we instead do +0.75, +1.5, +2.25, +3, +3.75
    UniqueForLevels uniqueForFive = new UniqueForLevels(5);
    this.buildModifier(ModifierIds.sharpness).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.add(ToolStats.ATTACK_DAMAGE, 0.75f)).levelDisplay(uniqueForFive);
    this.buildModifier(ModifierIds.swiftstrike).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.multiplyBase(ToolStats.ATTACK_SPEED, 0.05f)).levelDisplay(uniqueForFive);
    this.buildModifier(ModifierIds.smite).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(new ConditionalDamageModule(new MobTypePredicate(MobType.UNDEAD), 2.0f));
    this.buildModifier(ModifierIds.antiaquatic).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(new ConditionalDamageModule(new MobTypePredicate(MobType.WATER), 2.0f));
    this.buildModifier(ModifierIds.cooling).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(new ConditionalDamageModule(LivingEntityPredicate.FIRE_IMMUNE, 1.6f));
    IJsonPredicate<LivingEntity> baneSssssPredicate = LivingEntityPredicate.OR.create(new MobTypePredicate(MobType.ARTHROPOD), new TagEntityPredicate(TinkerTags.EntityTypes.CREEPERS));
    this.buildModifier(ModifierIds.baneOfSssss)
      .addModule(IncrementalModule.RECIPE_CONTROLLED)
      .addModule(new ConditionalDamageModule(baneSssssPredicate, 2.0f))
      .addModule(MobEffectModule.builder(MobEffects.MOVEMENT_SLOWDOWN).level(ScalingValue.flat(4)).time(ScalingValue.random(20, 10)).entity(baneSssssPredicate).build(), TinkerHooks.MELEE_HIT);
    this.buildModifier(ModifierIds.killager).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(new ConditionalDamageModule(LivingEntityPredicate.OR.create(
      new MobTypePredicate(MobType.ILLAGER),
      new TagEntityPredicate(TinkerTags.EntityTypes.VILLAGERS)
    ), 2.0f));
    this.addRedirect(id("fractured"), this.redirect(ModifierIds.sharpness));

    // ranged
    this.buildModifier(ModifierIds.power).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.add(ToolStats.PROJECTILE_DAMAGE, 0.5f));
    this.buildModifier(ModifierIds.quickCharge).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.multiplyBase(ToolStats.DRAW_SPEED, 0.25f));
    this.buildModifier(ModifierIds.trueshot).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.add(ToolStats.ACCURACY, 0.1f));
    this.buildModifier(ModifierIds.blindshot).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.add(ToolStats.ACCURACY, -0.1f));

    // armor
    this.buildModifier(TinkerModifiers.golden).addModule(new VolatileFlagModule(ModifiableArmorItem.PIGLIN_NEUTRAL)).levelDisplay(ModifierLevelDisplay.NO_LEVELS);
    this.buildModifier(ModifierIds.wings).addModule(new VolatileFlagModule(ModifiableArmorItem.ELYTRA)).levelDisplay(ModifierLevelDisplay.NO_LEVELS);
    this.buildModifier(ModifierIds.knockbackResistance).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(ToolStatModule.add(ToolStats.KNOCKBACK_RESISTANCE, 0.1f));
    // defense
    // TODO: floor?
    this.buildModifier(ModifierIds.revitalizing).addModule(IncrementalModule.RECIPE_CONTROLLED).addModule(new AttributeModule("tconstruct.modifier.revitalizing", Attributes.MAX_HEALTH, Operation.ADDITION, 2, armorSlots));
    // helmet
    this.buildModifier(ModifierIds.respiration).addModule(new EnchantmentModule.Constant(Enchantments.RESPIRATION));
    // chestplate
    this.buildModifier(ModifierIds.strength)
      .addModule(IncrementalModule.RECIPE_CONTROLLED)
      .addModule(new AttributeModule("tconstruct.modifier.strength", Attributes.ATTACK_DAMAGE, Operation.MULTIPLY_TOTAL, 0.1f, armorSlots));
    this.addRedirect(id("armor_power"), this.redirect(ModifierIds.strength));
    // leggings
    this.addModifier(ModifierIds.pockets, new InventoryMenuModifier(18));
    this.addModifier(ModifierIds.toolBelt, new ToolBeltModifier(new int[]{4, 5, 6, 7, 8, 9}));
    this.addRedirect(id("pocket_chain"), this.redirect(TinkerModifiers.shieldStrap.getId()));
    this.buildModifier(ModifierIds.stepUp).addModule(new AttributeModule("tconstruct.modifier.step_up", PortingLibAttributes.STEP_HEIGHT_ADDITION, Operation.ADDITION, 0.5f, armorSlots));
    this.buildModifier(ModifierIds.speedy).addModule(new AttributeModule("tconstruct.modifier.speedy", Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_TOTAL, 0.1f, armorMainHand));
    // boots
    this.buildModifier(ModifierIds.depthStrider).addModule(new EnchantmentModule.Constant(Enchantments.DEPTH_STRIDER));

    // internal
    this.buildModifier(ModifierIds.overslimeFriend).addModule(new VolatileFlagModule(OverslimeModifier.KEY_OVERSLIME_FRIEND)).tooltipDisplay(TooltipDisplay.NEVER);
    this.buildModifier(ModifierIds.snowBoots).addModule(new VolatileFlagModule(ModifiableArmorItem.SNOW_BOOTS)).tooltipDisplay(TooltipDisplay.NEVER);

    // traits - tier 1
    this.buildModifier(ModifierIds.cultivated).addModule(RepairModule.leveling(0.5f));
    this.addModifier(ModifierIds.stringy, new Modifier());
    this.buildModifier(ModifierIds.flexible).addModule(ToolStatModule.add(ToolStats.VELOCITY, 0.1f)).addModule(ToolStatModule.multiplyAll(ToolStats.PROJECTILE_DAMAGE, -0.1f));
    // traits - tier 2
    this.buildModifier(ModifierIds.sturdy).addModule(ToolStatModule.multiplyBase(ToolStats.DURABILITY, 0.15f));
    this.buildModifier(ModifierIds.scorching).addModule(new ConditionalDamageModule(LivingEntityPredicate.ON_FIRE, 2f));
    // traits - tier 2 compat
    this.addModifier(ModifierIds.lustrous, new Modifier());
    this.buildModifier(ModifierIds.sharpweight)
      .addModule(ToolStatModule.multiplyBase(ToolStats.MINING_SPEED, 0.1f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.DRAW_SPEED, 0.15f))
      .addModule(new AttributeModule("tconstruct.modifier.sharpweight", Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_BASE, -0.1f, handSlots));
    this.buildModifier(ModifierIds.heavy)
      .addModule(ToolStatModule.multiplyBase(ToolStats.ATTACK_DAMAGE, 0.1f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.PROJECTILE_DAMAGE, 0.1f))
      .addModule(new AttributeModule("tconstruct.modifier.heavy", Attributes.MOVEMENT_SPEED, Operation.MULTIPLY_BASE, -0.1f, handSlots));
    this.buildModifier(ModifierIds.featherweight)
      .addModule(ToolStatModule.multiplyBase(ToolStats.DRAW_SPEED, 0.07f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.ACCURACY, 0.07f));

    // traits - tier 3
    this.buildModifier(ModifierIds.crumbling).addModule(new ConditionalMiningSpeedModule(BlockPredicate.REQUIRES_TOOL.inverted(), false, 0.5f));
    this.buildModifier(ModifierIds.enhanced).priority(60).addModule(UPGRADE);
    this.addRedirect(id("maintained_2"), this.redirect(TinkerModifiers.maintained.getId()));
    // traits - tier 3 nether
    this.buildModifier(ModifierIds.lightweight)
      .addModule(ToolStatModule.multiplyBase(ToolStats.ATTACK_SPEED, 0.07f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.MINING_SPEED, 0.07f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.DRAW_SPEED, 0.03f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.VELOCITY, 0.03f));
    // traits - tier 3 compat
    this.buildModifier(ModifierIds.ductile)
      .addModule(ToolStatModule.multiplyBase(ToolStats.DURABILITY, 0.04f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.ATTACK_DAMAGE, 0.04f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.MINING_SPEED, 0.04f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.VELOCITY, 0.03f))
      .addModule(ToolStatModule.multiplyBase(ToolStats.PROJECTILE_DAMAGE, 0.03f));

    // mob disguise
    this.buildModifier(ModifierIds.creeperDisguise).addModule(new MobDisguiseModule(EntityType.CREEPER));
    this.buildModifier(ModifierIds.endermanDisguise).addModule(new MobDisguiseModule(EntityType.ENDERMAN));
    this.buildModifier(ModifierIds.skeletonDisguise).addModule(new MobDisguiseModule(EntityType.SKELETON));
    this.buildModifier(ModifierIds.strayDisguise).addModule(new MobDisguiseModule(EntityType.STRAY));
    this.buildModifier(ModifierIds.witherSkeletonDisguise).addModule(new MobDisguiseModule(EntityType.WITHER_SKELETON));
    this.buildModifier(ModifierIds.spiderDisguise).addModule(new MobDisguiseModule(EntityType.SPIDER));
    this.buildModifier(ModifierIds.caveSpiderDisguise).addModule(new MobDisguiseModule(EntityType.CAVE_SPIDER));
    this.buildModifier(ModifierIds.zombieDisguise).addModule(new MobDisguiseModule(EntityType.ZOMBIE));
    this.buildModifier(ModifierIds.huskDisguise).addModule(new MobDisguiseModule(EntityType.HUSK));
    this.buildModifier(ModifierIds.drownedDisguise).addModule(new MobDisguiseModule(EntityType.DROWNED));
    this.buildModifier(ModifierIds.blazeDisguise).addModule(new MobDisguiseModule(EntityType.BLAZE));
    this.buildModifier(ModifierIds.piglinDisguise).addModule(new MobDisguiseModule(EntityType.PIGLIN));
    this.buildModifier(ModifierIds.piglinBruteDisguise).addModule(new MobDisguiseModule(EntityType.PIGLIN_BRUTE));
    this.buildModifier(ModifierIds.zombifiedPiglinDisguise).addModule(new MobDisguiseModule(EntityType.ZOMBIFIED_PIGLIN));
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Modifiers";
  }

  /**
   * Short helper to get a modifier ID
   */
  private static ModifierId id(String name) {
    return new ModifierId(TConstruct.MOD_ID, name);
  }
}
