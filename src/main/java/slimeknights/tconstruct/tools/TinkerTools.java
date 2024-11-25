package slimeknights.tconstruct.tools;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import io.github.fabricators_of_create.porting_lib.util.ItemPredicateRegistry;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTabs;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.common.config.ConfigurableAction;
import slimeknights.tconstruct.common.data.tags.MaterialTagProvider;
import slimeknights.tconstruct.library.client.data.TinkerSpriteSourceGenerator;
import slimeknights.tconstruct.library.client.data.material.GeneratorPartTextureJsonGenerator;
import slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator;
import slimeknights.tconstruct.library.json.AddToolDataFunction;
import slimeknights.tconstruct.library.json.RandomMaterial;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.tools.IndestructibleItemEntity;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.ToolPredicate;
import slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider;
import slimeknights.tconstruct.library.tools.capability.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.ToolInventoryCapability;
import slimeknights.tconstruct.library.tools.definition.aoe.BoxAOEIterator;
import slimeknights.tconstruct.library.tools.definition.aoe.CircleAOEIterator;
import slimeknights.tconstruct.library.tools.definition.aoe.FallbackAOEIterator;
import slimeknights.tconstruct.library.tools.definition.aoe.IAreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.aoe.TreeAOEIterator;
import slimeknights.tconstruct.library.tools.definition.aoe.VeiningAOEIterator;
import slimeknights.tconstruct.library.tools.definition.harvest.FixedTierHarvestLogic;
import slimeknights.tconstruct.library.tools.definition.harvest.IHarvestLogic;
import slimeknights.tconstruct.library.tools.definition.harvest.ModifiedHarvestLogic;
import slimeknights.tconstruct.library.tools.definition.harvest.TagHarvestLogic;
import slimeknights.tconstruct.library.tools.definition.module.IToolModule;
import slimeknights.tconstruct.library.tools.definition.module.ToolModuleHooks;
import slimeknights.tconstruct.library.tools.definition.module.interaction.DualOptionInteraction;
import slimeknights.tconstruct.library.tools.definition.module.interaction.PreferenceSetInteraction;
import slimeknights.tconstruct.library.tools.definition.weapon.CircleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.weapon.IWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.weapon.ParticleWeaponAttack;
import slimeknights.tconstruct.library.tools.definition.weapon.SweepWeaponAttack;
import slimeknights.tconstruct.library.tools.helper.ModifierLootingHandler;
import slimeknights.tconstruct.library.tools.item.ModifiableArmorItem;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;
import slimeknights.tconstruct.library.tools.item.ModifiableLauncherItem;
import slimeknights.tconstruct.library.tools.item.ModifiableStaffItem;
import slimeknights.tconstruct.library.utils.BlockSideHitListener;
import slimeknights.tconstruct.tools.data.StationSlotLayoutProvider;
import slimeknights.tconstruct.tools.data.ToolDefinitionDataProvider;
import slimeknights.tconstruct.tools.data.ToolsRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRecipeProvider;
import slimeknights.tconstruct.tools.data.material.MaterialRenderInfoProvider;
import slimeknights.tconstruct.tools.data.material.MaterialStatsDataProvider;
import slimeknights.tconstruct.tools.data.material.MaterialTraitsDataProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerMaterialSpriteProvider;
import slimeknights.tconstruct.tools.data.sprite.TinkerPartSpriteProvider;
import slimeknights.tconstruct.tools.item.ArmorSlotType;
import slimeknights.tconstruct.tools.item.CrystalshotItem;
import slimeknights.tconstruct.tools.item.CrystalshotItem.CrystalshotEntity;
import slimeknights.tconstruct.tools.item.ModifiableBowItem;
import slimeknights.tconstruct.tools.item.ModifiableCrossbowItem;
import slimeknights.tconstruct.tools.item.ModifiableDaggerItem;
import slimeknights.tconstruct.tools.item.ModifiableSwordItem;
import slimeknights.tconstruct.tools.item.PlateArmorItem;
import slimeknights.tconstruct.tools.item.SlimelytraItem;
import slimeknights.tconstruct.tools.item.SlimeskullItem;
import slimeknights.tconstruct.tools.item.SlimesuitItem;
import slimeknights.tconstruct.tools.item.TravelersGearItem;
import slimeknights.tconstruct.tools.logic.EquipmentChangeWatcher;
import slimeknights.tconstruct.tools.menu.ToolContainerMenu;

/**
 * Contains all complete tool items
 */
public final class TinkerTools extends TinkerModule {

  public TinkerTools() {
    SlotType.init();
    BlockSideHitListener.init();
    ModifierLootingHandler.init();
    RandomMaterial.init();
    this.commonSetup();
    this.registerRecipeSerializers();
  }

  /**
   * Loot function type for tool add data
   */
  public static final RegistryObject<LootItemFunctionType> lootAddToolData = LOOT_FUNCTIONS.register("add_tool_data", () -> new LootItemFunctionType(AddToolDataFunction.SERIALIZER));

  /*
   * Items
   */
  private static final Item.Properties TOOL = new FabricItemSettings().stacksTo(1);

  public static final ItemObject<ModifiableItem> pickaxe = ITEMS.register("pickaxe", () -> new ModifiableItem(TOOL, ToolDefinitions.PICKAXE, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> sledgeHammer = ITEMS.register("sledge_hammer", () -> new ModifiableItem(TOOL, ToolDefinitions.SLEDGE_HAMMER, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> veinHammer = ITEMS.register("vein_hammer", () -> new ModifiableItem(TOOL, ToolDefinitions.VEIN_HAMMER, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableItem> mattock = ITEMS.register("mattock", () -> new ModifiableItem(TOOL, ToolDefinitions.MATTOCK, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> pickadze = ITEMS.register("pickadze", () -> new ModifiableItem(TOOL, ToolDefinitions.PICKADZE, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> excavator = ITEMS.register("excavator", () -> new ModifiableItem(TOOL, ToolDefinitions.EXCAVATOR, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableItem> handAxe = ITEMS.register("hand_axe", () -> new ModifiableItem(TOOL, ToolDefinitions.HAND_AXE, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> broadAxe = ITEMS.register("broad_axe", () -> new ModifiableItem(TOOL, ToolDefinitions.BROAD_AXE, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableItem> kama = ITEMS.register("kama", () -> new ModifiableItem(TOOL, ToolDefinitions.KAMA, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> scythe = ITEMS.register("scythe", () -> new ModifiableItem(TOOL, ToolDefinitions.SCYTHE, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableItem> dagger = ITEMS.register("dagger", () -> new ModifiableDaggerItem(TOOL, ToolDefinitions.DAGGER, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> sword = ITEMS.register("sword", () -> new ModifiableSwordItem(TOOL, ToolDefinitions.SWORD, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> cleaver = ITEMS.register("cleaver", () -> new ModifiableSwordItem(TOOL, ToolDefinitions.CLEAVER, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableLauncherItem> crossbow = ITEMS.register("crossbow", () -> new ModifiableCrossbowItem(TOOL, ToolDefinitions.CROSSBOW, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableLauncherItem> longbow = ITEMS.register("longbow", () -> new ModifiableBowItem(TOOL, ToolDefinitions.LONGBOW, TinkerTabs.TAB_TOOLS));

  public static final ItemObject<ModifiableItem> flintAndBrick = ITEMS.register("flint_and_brick", () -> new ModifiableItem(TOOL, ToolDefinitions.FLINT_AND_BRICK, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> skyStaff = ITEMS.register("sky_staff", () -> new ModifiableStaffItem(TOOL, ToolDefinitions.SKY_STAFF, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> earthStaff = ITEMS.register("earth_staff", () -> new ModifiableStaffItem(TOOL, ToolDefinitions.EARTH_STAFF, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> ichorStaff = ITEMS.register("ichor_staff", () -> new ModifiableStaffItem(TOOL, ToolDefinitions.ICHOR_STAFF, TinkerTabs.TAB_TOOLS));

  // armor
  public static final EnumObject<ArmorSlotType, ModifiableArmorItem> travelersGear = ITEMS.registerEnum("travelers", ArmorSlotType.values(), type -> new TravelersGearItem(ArmorDefinitions.TRAVELERS, type, TOOL, TinkerTabs.TAB_TOOLS));
  public static final EnumObject<ArmorSlotType, ModifiableArmorItem> plateArmor = ITEMS.registerEnum("plate", ArmorSlotType.values(), type -> new PlateArmorItem(ArmorDefinitions.PLATE, type, TOOL, TinkerTabs.TAB_TOOLS));
  public static final EnumObject<ArmorSlotType, ModifiableArmorItem> slimesuit = new EnumObject.Builder<ArmorSlotType, ModifiableArmorItem>(ArmorSlotType.class)
    .putAll(ITEMS.registerEnum("slime", new ArmorSlotType[]{ArmorSlotType.BOOTS, ArmorSlotType.LEGGINGS}, type -> new SlimesuitItem(ArmorDefinitions.SLIMESUIT, type, TOOL, TinkerTabs.TAB_TOOLS)))
    .put(ArmorSlotType.CHESTPLATE, ITEMS.register("slime_chestplate", () -> new SlimelytraItem(ArmorDefinitions.SLIMESUIT, TOOL, TinkerTabs.TAB_TOOLS)))
    .put(ArmorSlotType.HELMET, ITEMS.register("slime_helmet", () -> new SlimeskullItem(ArmorDefinitions.SLIMESUIT, TOOL, TinkerTabs.TAB_TOOLS)))
    .build();

  // shields
  public static final ItemObject<ModifiableItem> travelersShield = ITEMS.register("travelers_shield", () -> new ModifiableStaffItem(TOOL, ArmorDefinitions.TRAVELERS_SHIELD, TinkerTabs.TAB_TOOLS));
  public static final ItemObject<ModifiableItem> plateShield = ITEMS.register("plate_shield", () -> new ModifiableStaffItem(TOOL, ArmorDefinitions.PLATE_SHIELD, TinkerTabs.TAB_TOOLS));

  // arrows
  public static final ItemObject<ArrowItem> crystalshotItem = ITEMS.register("crystalshot", () -> new CrystalshotItem(new Item.Properties()/*.tab(TinkerTabs.TAB_TOOLS)*/));

  /* Particles */
  public static final RegistryObject<SimpleParticleType> hammerAttackParticle = PARTICLE_TYPES.register("hammer_attack", () -> FabricParticleTypes.simple(true));
  public static final RegistryObject<SimpleParticleType> axeAttackParticle = PARTICLE_TYPES.register("axe_attack", () -> FabricParticleTypes.simple(true));

  /* Entities */
  public static final RegistryObject<EntityType<IndestructibleItemEntity>> indestructibleItem = ENTITIES.register("indestructible_item", () ->
    FabricEntityTypeBuilder.<IndestructibleItemEntity>create(MobCategory.MISC, IndestructibleItemEntity::new)
      .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
      .fireImmune());
  public static final RegistryObject<EntityType<CrystalshotEntity>> crystalshotEntity = ENTITIES.register("crystalshot", () ->
    FabricEntityTypeBuilder.<CrystalshotEntity>create(MobCategory.MISC, CrystalshotEntity::new)
      .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
      .trackRangeChunks(4)
      .trackedUpdateRate(20));

  /* Containers */
  public static final RegistryObject<MenuType<ToolContainerMenu>> toolContainer = MENUS.register("tool_container", ToolContainerMenu::forClient);


  /*
   * Events
   */

  void commonSetup() {
    EquipmentChangeWatcher.register();
    ToolCapabilityProvider.register(ToolFluidCapability.Provider::new);
    ToolCapabilityProvider.register(ToolInventoryCapability.Provider::new);
    for (ConfigurableAction action : Config.COMMON.damageSourceTweaks) {
      action.run();
    }
    TinkerHooks.init();
    ToolModuleHooks.init();
  }

  void registerRecipeSerializers() {
    ItemPredicateRegistry.register(ToolPredicate.ID, ToolPredicate::deserialize);

    // tool definition components
    // harvest
    IHarvestLogic.LOADER.register(TConstruct.getResource("effective_tag"), TagHarvestLogic.LOADER);
    IHarvestLogic.LOADER.register(TConstruct.getResource("modified_tag"), ModifiedHarvestLogic.LOADER);
    IHarvestLogic.LOADER.register(TConstruct.getResource("fixed_tier"), FixedTierHarvestLogic.LOADER);
    // aoe
    IAreaOfEffectIterator.LOADER.register(TConstruct.getResource("box"), BoxAOEIterator.LOADER);
    IAreaOfEffectIterator.LOADER.register(TConstruct.getResource("circle"), CircleAOEIterator.LOADER);
    IAreaOfEffectIterator.LOADER.register(TConstruct.getResource("tree"), TreeAOEIterator.LOADER);
    IAreaOfEffectIterator.LOADER.register(TConstruct.getResource("vein"), VeiningAOEIterator.LOADER);
    IAreaOfEffectIterator.LOADER.register(TConstruct.getResource("fallback"), FallbackAOEIterator.LOADER);
    // attack
    IWeaponAttack.LOADER.register(TConstruct.getResource("sweep"), SweepWeaponAttack.LOADER);
    IWeaponAttack.LOADER.register(TConstruct.getResource("circle"), CircleWeaponAttack.LOADER);
    IWeaponAttack.LOADER.register(TConstruct.getResource("particle"), ParticleWeaponAttack.LOADER);
    // generic tool modules
    IToolModule.LOADER.register(TConstruct.getResource("dual_option_interaction"), DualOptionInteraction.LOADER);
    IToolModule.LOADER.register(TConstruct.getResource("preference_set_interaction"), PreferenceSetInteraction.LOADER);
  }

  public static void gatherData(FabricDataGenerator.Pack pack, ExistingFileHelper existingFileHelper) {
    pack.addProvider(ToolsRecipeProvider::new);
    pack.addProvider(MaterialRecipeProvider::new);
    MaterialDataProvider materials = pack.addProvider(MaterialDataProvider::new);
    pack.addProvider((output, registriesFuture) -> new MaterialStatsDataProvider(output, materials));
    pack.addProvider((output, registriesFuture) -> new MaterialTraitsDataProvider(output, materials));
    pack.addProvider(ToolDefinitionDataProvider::new);
    pack.addProvider(StationSlotLayoutProvider::new);
    pack.addProvider((output, registriesFuture) -> new MaterialTagProvider(output, existingFileHelper));

    TinkerMaterialSpriteProvider materialSprites = new TinkerMaterialSpriteProvider();
    TinkerPartSpriteProvider partSprites = new TinkerPartSpriteProvider();
    pack.addProvider((output, registriesFuture) -> new MaterialRenderInfoProvider(output, materialSprites));
    pack.addProvider((output, registriesFuture) -> new GeneratorPartTextureJsonGenerator(output, TConstruct.MOD_ID, partSprites));
    pack.addProvider((output, registriesFuture) -> new MaterialPartTextureGenerator(output, existingFileHelper, partSprites, materialSprites));
    pack.addProvider((output, registriesFuture) -> new TinkerSpriteSourceGenerator(output, existingFileHelper));
  }
}
