package slimeknights.tconstruct.world;

import com.google.common.collect.ImmutableSet;
import io.github.fabricators_of_create.porting_lib.common.util.PlantType;
import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.crafting.FireworkStarRecipe;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.levelgen.GeodeBlockSettings;
import net.minecraft.world.level.levelgen.GeodeCrackSettings;
import net.minecraft.world.level.levelgen.GeodeLayerSettings;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.GeodeConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.mantle.registration.object.WoodBlockObject;
import slimeknights.mantle.registration.object.WoodBlockObject.WoodVariant;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.registration.GeodeItemObject;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.block.BloodSlimeBlock;
import slimeknights.tconstruct.world.block.CongealedSlimeBlock;
import slimeknights.tconstruct.world.block.PiglinWallHeadBlock;
import slimeknights.tconstruct.world.block.SlimeDirtBlock;
import slimeknights.tconstruct.world.block.SlimeFungusBlock;
import slimeknights.tconstruct.world.block.SlimeGrassBlock;
import slimeknights.tconstruct.world.block.SlimeLeavesBlock;
import slimeknights.tconstruct.world.block.SlimeNyliumBlock;
import slimeknights.tconstruct.world.block.SlimeSaplingBlock;
import slimeknights.tconstruct.world.block.SlimeTallGrassBlock;
import slimeknights.tconstruct.world.block.SlimeVineBlock;
import slimeknights.tconstruct.world.block.SlimeWartBlock;
import slimeknights.tconstruct.world.block.StickySlimeBlock;
import slimeknights.tconstruct.world.data.WorldRecipeProvider;
import slimeknights.tconstruct.world.entity.EarthSlimeEntity;
import slimeknights.tconstruct.world.entity.EnderSlimeEntity;
import slimeknights.tconstruct.world.entity.SkySlimeEntity;
import slimeknights.tconstruct.world.entity.SlimePlacementPredicate;
import slimeknights.tconstruct.world.entity.TerracubeEntity;
import slimeknights.tconstruct.world.item.SlimeGrassSeedItem;
import slimeknights.tconstruct.world.worldgen.trees.SlimeTree;
import slimeknights.tconstruct.world.worldgen.trees.SupplierBlockStateProvider;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Contains blocks and items relevant to structures and world gen
 */
@SuppressWarnings("unused")
public final class TinkerWorld extends TinkerModule {

  static final Logger log = Util.getLogger("tinker_world");

  public static final PlantType SLIME_PLANT_TYPE = PlantType.get("slime");

  /*
   * Block base properties
   */
  private static final Item.Properties WORLD_PROPS = new Item.Properties();
  private static final Function<Block, ? extends BlockItem> DEFAULT_BLOCK_ITEM = (b) -> new BlockItem(b, WORLD_PROPS);
  private static final Function<Block, ? extends BlockItem> TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, WORLD_PROPS);
  private static final Item.Properties HEAD_PROPS = new Item.Properties().rarity(Rarity.UNCOMMON);

  /*
   * Blocks
   */
  // ores
  public static final ItemObject<Block> cobaltOre = BLOCKS.register("cobalt_ore", () -> new Block(builder(MapColor.NETHER, SoundType.NETHER_ORE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(10.0F)), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<Block> rawCobaltBlock = BLOCKS.register("raw_cobalt_block", () -> new Block(builder(MapColor.COLOR_BLUE, SoundType.NETHER_ORE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(6.0f, 7.0f)), DEFAULT_BLOCK_ITEM);
  public static final ItemObject<Item> rawCobalt = ITEMS.register("raw_cobalt", WORLD_PROPS);

  // slime
  public static final EnumObject<SlimeType, SlimeBlock> slime = Util.make(() -> {
    Function<SlimeType, BlockBehaviour.Properties> slimeProps = type -> builder(type.getMapColor(), SoundType.SLIME_BLOCK).friction(0.8F).sound(SoundType.SLIME_BLOCK).noOcclusion();
    return new EnumObject.Builder<SlimeType, SlimeBlock>(SlimeType.class)
      .putDelegate(SlimeType.EARTH, (SlimeBlock) Blocks.SLIME_BLOCK)
      // sky slime: sticks to anything, but will not pull back
      .put(SlimeType.SKY, BLOCKS.register("sky_slime", () -> new StickySlimeBlock(slimeProps.apply(SlimeType.SKY), (state, other) -> true), TOOLTIP_BLOCK_ITEM))
      // ichor: does not stick to self, but sticks to anything else
      .put(SlimeType.ICHOR, BLOCKS.register("ichor_slime", () -> new StickySlimeBlock(slimeProps.apply(SlimeType.ICHOR).lightLevel(s -> SlimeType.ICHOR.getLightLevel()),
        (state, other) -> other.getBlock() != state.getBlock()), TOOLTIP_BLOCK_ITEM))
      // ender: only sticks to self
      .put(SlimeType.ENDER, BLOCKS.register("ender_slime", () -> new StickySlimeBlock(slimeProps.apply(SlimeType.ENDER), (state, other) -> other.getBlock() == state.getBlock()), TOOLTIP_BLOCK_ITEM))
      // blood slime: not sticky, and honey won't stick to it, good for bounce pads
      .put(SlimeType.BLOOD, BLOCKS.register("blood_slime", () -> new BloodSlimeBlock(slimeProps.apply(SlimeType.BLOOD)), TOOLTIP_BLOCK_ITEM))
      .build();
  });
  public static final EnumObject<SlimeType, CongealedSlimeBlock> congealedSlime = BLOCKS.registerEnum(SlimeType.values(), "congealed_slime", type -> new CongealedSlimeBlock(builder(type.getMapColor(), SoundType.SLIME_BLOCK).strength(0.5F).friction(0.5F).lightLevel(s -> type.getLightLevel())), TOOLTIP_BLOCK_ITEM);

  // island blocks
  public static final EnumObject<SlimeType, Block> slimeDirt = Util.make(() -> {
    Function<SlimeType, MapColor> color = type -> switch (type) {
      case SKY -> MapColor.WARPED_STEM;
      case ENDER -> MapColor.TERRACOTTA_LIGHT_BLUE;
      case ICHOR -> MapColor.TERRACOTTA_ORANGE;
      default -> MapColor.GRASS; // EARTH
    };
    return BLOCKS.registerEnum(SlimeType.TRUE_SLIME, "slime_dirt", (type) -> new SlimeDirtBlock(builder(color.apply(type), SoundType.SLIME_BLOCK).strength(1.9f)), TOOLTIP_BLOCK_ITEM);
  });
  public static final EnumObject<SlimeType, Block> allDirt = new EnumObject.Builder<SlimeType, Block>(SlimeType.class).put(SlimeType.BLOOD, () -> Blocks.DIRT).putAll(slimeDirt).build();

  // grass variants
  public static final EnumObject<SlimeType, Block> vanillaSlimeGrass, earthSlimeGrass, skySlimeGrass, enderSlimeGrass, ichorSlimeGrass;
  /**
   * Map of dirt type to slime grass type. Each slime grass is a map from foliage to grass type
   */
  public static final Map<SlimeType, EnumObject<SlimeType, Block>> slimeGrass = new EnumMap<>(SlimeType.class);

  static {
    Function<SlimeType, BlockBehaviour.Properties> slimeGrassProps = type -> builder(type.getMapColor(), SoundType.SLIME_BLOCK).strength(2.0f).requiresCorrectToolForDrops().randomTicks();
    Function<SlimeType, Block> slimeGrassRegister = type -> type.isNether() ? new SlimeNyliumBlock(slimeGrassProps.apply(type), type) : new SlimeGrassBlock(slimeGrassProps.apply(type), type);
    // blood is not an exact match for vanilla, but close enough
    vanillaSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "vanilla_slime_grass", slimeGrassRegister, TOOLTIP_BLOCK_ITEM);
    earthSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "earth_slime_grass", slimeGrassRegister, TOOLTIP_BLOCK_ITEM);
    skySlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "sky_slime_grass", slimeGrassRegister, TOOLTIP_BLOCK_ITEM);
    enderSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "ender_slime_grass", slimeGrassRegister, TOOLTIP_BLOCK_ITEM);
    ichorSlimeGrass = BLOCKS.registerEnum(SlimeType.values(), "ichor_slime_grass", slimeGrassRegister, TOOLTIP_BLOCK_ITEM);
    slimeGrass.put(SlimeType.BLOOD, vanillaSlimeGrass); // not an exact fit, but good enough
    slimeGrass.put(SlimeType.EARTH, earthSlimeGrass);
    slimeGrass.put(SlimeType.SKY, skySlimeGrass);
    slimeGrass.put(SlimeType.ENDER, enderSlimeGrass);
    slimeGrass.put(SlimeType.ICHOR, ichorSlimeGrass);
  }

  public static final EnumObject<SlimeType, SlimeGrassSeedItem> slimeGrassSeeds = ITEMS.registerEnum(SlimeType.values(), "slime_grass_seeds", type -> new SlimeGrassSeedItem(WORLD_PROPS, type));

  /**
   * Creates a wood variant properties function
   */
  private static Function<WoodVariant, BlockBehaviour.Properties> createSlimewood(MapColor planks, MapColor bark) {
    return type -> switch (type) {
      case WOOD -> BlockBehaviour.Properties.of().mapColor(bark).sound(SoundType.WOOD).requiresCorrectToolForDrops();
      case LOG ->
        BlockBehaviour.Properties.of().mapColor(state -> state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.Y ? planks : bark).sound(SoundType.WOOD).requiresCorrectToolForDrops();
      default -> BlockBehaviour.Properties.of().mapColor(planks).sound(SoundType.SLIME_BLOCK);
    };
  }

  // wood
  public static final WoodBlockObject greenheart = BLOCKS.registerWood("greenheart", createSlimewood(MapColor.COLOR_LIGHT_GREEN, MapColor.COLOR_GREEN), false);
  public static final WoodBlockObject skyroot = BLOCKS.registerWood("skyroot", createSlimewood(MapColor.COLOR_CYAN, MapColor.TERRACOTTA_CYAN), false);
  public static final WoodBlockObject bloodshroom = BLOCKS.registerWood("bloodshroom", createSlimewood(MapColor.COLOR_RED, MapColor.COLOR_ORANGE), false);

  // plants
  public static final EnumObject<SlimeType, SlimeTallGrassBlock> slimeFern, slimeTallGrass;

  static {
    Function<SlimeType, BlockBehaviour.Properties> props = type -> {
      BlockBehaviour.Properties properties;
      if (type.isNether()) {
        properties = builder(type.getMapColor(), SoundType.ROOTS).replaceable().pushReaction(PushReaction.DESTROY);
      } else {
        properties = builder(type.getMapColor(), SoundType.GRASS).replaceable().ignitedByLava().pushReaction(PushReaction.DESTROY);
      }
      return properties.instabreak().noCollission();
    };
    slimeFern = BLOCKS.registerEnum(SlimeType.values(), "slime_fern", type -> new SlimeTallGrassBlock(props.apply(type), type), DEFAULT_BLOCK_ITEM);
    slimeTallGrass = BLOCKS.registerEnum(SlimeType.values(), "slime_tall_grass", type -> new SlimeTallGrassBlock(props.apply(type), type), DEFAULT_BLOCK_ITEM);
  }

  public static final EnumObject<SlimeType, FlowerPotBlock> pottedSlimeFern = BLOCKS.registerPottedEnum(SlimeType.values(), "slime_fern", slimeFern);

  // trees
  public static final EnumObject<SlimeType, Block> slimeSapling = Util.make(() -> {
    Function<SlimeType, BlockBehaviour.Properties> props = type -> builder(type.getMapColor(), type.isNether() ? SoundType.FUNGUS : SoundType.GRASS).pushReaction(PushReaction.DESTROY).instabreak().noCollission();
    return new EnumObject.Builder<SlimeType, Block>(SlimeType.class)
      .putAll(BLOCKS.registerEnum(SlimeType.OVERWORLD, "slime_sapling", (type) -> new SlimeSaplingBlock(new SlimeTree(type), type, props.apply(type).randomTicks()), TOOLTIP_BLOCK_ITEM))
      .put(SlimeType.BLOOD, BLOCKS.register("blood_slime_sapling", () -> new SlimeFungusBlock(props.apply(SlimeType.BLOOD), ResourceKey.create(Registries.CONFIGURED_FEATURE, TConstruct.getResource("blood_slime_fungus"))), TOOLTIP_BLOCK_ITEM))
      .put(SlimeType.ICHOR, BLOCKS.register("ichor_slime_sapling", () -> new SlimeFungusBlock(props.apply(SlimeType.ICHOR), ResourceKey.create(Registries.CONFIGURED_FEATURE, TConstruct.getResource("ichor_slime_fungus"))), HIDDEN_BLOCK_ITEM))
      .build();
  });
  public static final EnumObject<SlimeType, FlowerPotBlock> pottedSlimeSapling = BLOCKS.registerPottedEnum(SlimeType.values(), "slime_sapling", slimeSapling);
  public static final EnumObject<SlimeType, Block> slimeLeaves = BLOCKS.registerEnum(SlimeType.values(), "slime_leaves", type -> {
    if (type.isNether()) {
      return new SlimeWartBlock(builder(type.getMapColor(), SoundType.WART_BLOCK).strength(1.5F).isValidSpawn((s, w, p, e) -> false), type);
    }
    return new SlimeLeavesBlock(builder(type.getMapColor(), SoundType.GRASS).ignitedByLava().pushReaction(PushReaction.DESTROY).strength(1.0f).randomTicks().noOcclusion().isValidSpawn((s, w, p, e) -> false), type);
  }, DEFAULT_BLOCK_ITEM);

  // slime vines
  public static final ItemObject<SlimeVineBlock> skySlimeVine, enderSlimeVine;

  static {
    Function<SlimeType, BlockBehaviour.Properties> props = type -> builder(type.getMapColor(), SoundType.GRASS).replaceable().ignitedByLava().pushReaction(PushReaction.DESTROY).strength(0.75F).noCollission().randomTicks();
    skySlimeVine = BLOCKS.register("sky_slime_vine", () -> new SlimeVineBlock(props.apply(SlimeType.SKY), SlimeType.SKY), DEFAULT_BLOCK_ITEM);
    enderSlimeVine = BLOCKS.register("ender_slime_vine", () -> new SlimeVineBlock(props.apply(SlimeType.ENDER), SlimeType.ENDER), DEFAULT_BLOCK_ITEM);
  }

  // geodes
  // earth
  public static final GeodeItemObject earthGeode = BLOCKS.registerGeode("earth_slime_crystal", MapColor.COLOR_LIGHT_GREEN, Sounds.EARTH_CRYSTAL, Sounds.EARTH_CRYSTAL_CHIME.getSound(), Sounds.EARTH_CRYSTAL_CLUSTER, 3, WORLD_PROPS);
  public static final ResourceKey<ConfiguredFeature<?, ?>> configuredEarthGeodeKey = configured("earth_geode");
  public static final ResourceKey<PlacedFeature> placedEarthGeodeKey = placed("earth_geode");
  // sky
  public static final GeodeItemObject skyGeode = BLOCKS.registerGeode("sky_slime_crystal", MapColor.COLOR_BLUE, Sounds.SKY_CRYSTAL, Sounds.SKY_CRYSTAL_CHIME.getSound(), Sounds.SKY_CRYSTAL_CLUSTER, 0, WORLD_PROPS);
  public static final ResourceKey<ConfiguredFeature<?, ?>> configuredSkyGeodeKey = configured("sky_geode");
  public static final ResourceKey<PlacedFeature> placedSkyGeodeKey = placed("sky_geode");
  // ichor
  public static final GeodeItemObject ichorGeode = BLOCKS.registerGeode("ichor_slime_crystal", MapColor.COLOR_ORANGE, Sounds.ICHOR_CRYSTAL, Sounds.ICHOR_CRYSTAL_CHIME.getSound(), Sounds.ICHOR_CRYSTAL_CLUSTER, 10, WORLD_PROPS);
  public static final ResourceKey<ConfiguredFeature<?, ?>> configuredIchorGeodeKey = configured("ichor_geode");
  public static final ResourceKey<PlacedFeature> placedIchorGeodeKey = placed("ichor_geode");
  // ender
  public static final GeodeItemObject enderGeode = BLOCKS.registerGeode("ender_slime_crystal", MapColor.COLOR_PURPLE, Sounds.ENDER_CRYSTAL, Sounds.ENDER_CRYSTAL_CHIME.getSound(), Sounds.ENDER_CRYSTAL_CLUSTER, 7, WORLD_PROPS);
  public static final ResourceKey<ConfiguredFeature<?, ?>> configuredEnderGeodeKey = configured("ender_geode");
  public static final ResourceKey<PlacedFeature> placedEnderGeodeKey = placed("ender_geode");

  // heads
  public static final EnumObject<TinkerHeadType, SkullBlock> heads = BLOCKS.registerEnumNoItem(TinkerHeadType.values(), "head", TinkerWorld::makeHead);
  public static final EnumObject<TinkerHeadType, WallSkullBlock> wallHeads = BLOCKS.registerEnumNoItem(TinkerHeadType.values(), "wall_head", TinkerWorld::makeWallHead);
  public static final EnumObject<TinkerHeadType, StandingAndWallBlockItem> headItems = ITEMS.registerEnum(TinkerHeadType.values(), "head", type -> new StandingAndWallBlockItem(heads.get(type), wallHeads.get(type), HEAD_PROPS, Direction.DOWN));

  /*
   * Entities
   */
  // our own copy of the slime to make spawning a bit easier
  public static final RegistryObject<EntityType<EarthSlimeEntity>> earthSlimeEntity = ENTITIES.registerWithEgg("earth_slime", () ->
      FabricEntityTypeBuilder.create(MobCategory.MONSTER, EarthSlimeEntity::new)
        .forceTrackedVelocityUpdates(true)
        .trackRangeChunks(10)
        .dimensions(EntityDimensions.scalable(2.04F, 2.04F))
    /*.entityFactory((spawnEntity, world) -> TinkerWorld.earthSlimeEntity.get().create(world))*/, 0x51a03e, 0x7ebf6e);
  public static final RegistryObject<EntityType<SkySlimeEntity>> skySlimeEntity = ENTITIES.registerWithEgg("sky_slime", () ->
      FabricEntityTypeBuilder.create(MobCategory.MONSTER, SkySlimeEntity::new)
        .forceTrackedVelocityUpdates(true)
        .trackRangeChunks(20)
        .dimensions(EntityDimensions.scalable(2.04F, 2.04F))
    /*.setCustomClientFactory((spawnEntity, world) -> TinkerWorld.skySlimeEntity.get().create(world))*/, 0x47eff5, 0xacfff4);
  public static final RegistryObject<EntityType<EnderSlimeEntity>> enderSlimeEntity = ENTITIES.registerWithEgg("ender_slime", () ->
      FabricEntityTypeBuilder.create(MobCategory.MONSTER, EnderSlimeEntity::new)
        .forceTrackedVelocityUpdates(true)
        .trackRangeChunks(32)
        .dimensions(EntityDimensions.scalable(2.04F, 2.04F))
    /*.setCustomClientFactory((spawnEntity, world) -> TinkerWorld.enderSlimeEntity.get().create(world))*/, 0x6300B0, 0xD37CFF);
  public static final RegistryObject<EntityType<TerracubeEntity>> terracubeEntity = ENTITIES.registerWithEgg("terracube", () ->
      FabricEntityTypeBuilder.create(MobCategory.MONSTER, TerracubeEntity::new)
        .forceTrackedVelocityUpdates(true)
        .trackRangeChunks(8)
        .dimensions(EntityDimensions.scalable(2.04F, 2.04F))
    /*.setCustomClientFactory((spawnEntity, world) -> TinkerWorld.terracubeEntity.get().create(world))*/, 0xAFB9D6, 0xA1A7B1);

  /*
   * Particles
   */
  public static final RegistryObject<SimpleParticleType> skySlimeParticle = PARTICLE_TYPES.register("sky_slime", () -> FabricParticleTypes.simple(false));
  public static final RegistryObject<SimpleParticleType> enderSlimeParticle = PARTICLE_TYPES.register("ender_slime", () -> FabricParticleTypes.simple(false));
  public static final RegistryObject<SimpleParticleType> terracubeParticle = PARTICLE_TYPES.register("terracube", () -> FabricParticleTypes.simple(false));

  /*
   * Features
   */
  // small veins, standard distribution
  public static ResourceKey<ConfiguredFeature<?, ?>> smallCobaltOreKey = configured("cobalt_ore_small");
  public static ResourceKey<PlacedFeature> placedSmallCobaltOreKey = placed("cobalt_ore_small");
  // large veins, around y=16, up to 48
  public static ResourceKey<ConfiguredFeature<?, ?>> largeCobaltOreKey = configured("cobalt_ore_large");
  public static ResourceKey<PlacedFeature> placedLargeCobaltOreKey = placed("cobalt_ore_large");

  public static ResourceKey<ConfiguredFeature<?, ?>> configured(String id) {
    return ResourceKey.create(Registries.CONFIGURED_FEATURE, TConstruct.getResource(id));
  }

  public static ResourceKey<PlacedFeature> placed(String id) {
    return ResourceKey.create(Registries.PLACED_FEATURE, TConstruct.getResource(id));
  }

  public static void bootstrapConfigured(BootstapContext<ConfiguredFeature<?, ?>> bootstapContext) {
    RuleTest netherrack = new BlockMatchTest(Blocks.NETHERRACK);
    HolderGetter<ConfiguredFeature<?, ?>> lookup = bootstapContext.lookup(Registries.CONFIGURED_FEATURE);
    FeatureUtils.register(bootstapContext, smallCobaltOreKey, Feature.ORE, new OreConfiguration(netherrack, cobaltOre.get().defaultBlockState(), 4));
    FeatureUtils.register(bootstapContext, largeCobaltOreKey, Feature.ORE, new OreConfiguration(netherrack, cobaltOre.get().defaultBlockState(), 6));

    configuredGeode(bootstapContext, configuredEarthGeodeKey, earthGeode, BlockStateProvider.simple(Blocks.CALCITE), BlockStateProvider.simple(Blocks.CLAY),
      new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 5.2D), new GeodeCrackSettings(0.95D, 2.0D, 2), UniformInt.of(6, 9), UniformInt.of(3, 4), UniformInt.of(1, 2), 16, 1);
    configuredGeode(bootstapContext, configuredSkyGeodeKey, skyGeode, BlockStateProvider.simple(Blocks.CALCITE), BlockStateProvider.simple(Blocks.MOSSY_COBBLESTONE),
      new GeodeLayerSettings(1.5D, 2.0D, 3.0D, 4.5D), new GeodeCrackSettings(0.55D, 0.5D, 2), UniformInt.of(3, 4), ConstantInt.of(2), ConstantInt.of(1), 8, 3);
    configuredGeode(bootstapContext, configuredIchorGeodeKey, ichorGeode, BlockStateProvider.simple(Blocks.CALCITE), BlockStateProvider.simple(Blocks.NETHERRACK),
      new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 4.2D), new GeodeCrackSettings(0.75D, 2.0D, 2), UniformInt.of(4, 6), UniformInt.of(3, 4), UniformInt.of(1, 2), 24, 20);
    configuredGeode(bootstapContext, configuredEnderGeodeKey, enderGeode, BlockStateProvider.simple(Blocks.CALCITE), BlockStateProvider.simple(Blocks.END_STONE),
      new GeodeLayerSettings(1.7D, 2.2D, 3.2D, 5.2D), new GeodeCrackSettings(0.45, 1.0D, 2), UniformInt.of(4, 10), UniformInt.of(3, 4), UniformInt.of(1, 2), 16, 10000);

    TinkerStructures.bootstrapConfigured(bootstapContext);
  }

  public static void bootstrap(BootstapContext<PlacedFeature> bootstapContext) {
    HolderGetter<ConfiguredFeature<?, ?>> lookup = bootstapContext.lookup(Registries.CONFIGURED_FEATURE);
    PlacementUtils.register(bootstapContext, placedSmallCobaltOreKey, lookup.getOrThrow(smallCobaltOreKey), CountPlacement.of(5), InSquarePlacement.spread(), PlacementUtils.RANGE_8_8, BiomeFilter.biome());
    PlacementUtils.register(bootstapContext, placedLargeCobaltOreKey, lookup.getOrThrow(largeCobaltOreKey), CountPlacement.of(3), InSquarePlacement.spread(), HeightRangePlacement.triangle(VerticalAnchor.absolute(8), VerticalAnchor.absolute(32)), BiomeFilter.biome());

    placedGeode(bootstapContext, placedEarthGeodeKey, lookup.getOrThrow(configuredEarthGeodeKey), RarityFilter.onAverageOnceEvery(128), HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(6), VerticalAnchor.aboveBottom(54)));
    placedGeode(bootstapContext, placedSkyGeodeKey, lookup.getOrThrow(configuredSkyGeodeKey), RarityFilter.onAverageOnceEvery(64), HeightRangePlacement.uniform(VerticalAnchor.absolute(16), VerticalAnchor.absolute(54)));
    placedGeode(bootstapContext, placedIchorGeodeKey, lookup.getOrThrow(configuredIchorGeodeKey), RarityFilter.onAverageOnceEvery(52), HeightRangePlacement.uniform(VerticalAnchor.belowTop(48), VerticalAnchor.belowTop(16)));
    placedGeode(bootstapContext, placedEnderGeodeKey, lookup.getOrThrow(configuredEnderGeodeKey), RarityFilter.onAverageOnceEvery(256), HeightRangePlacement.uniform(VerticalAnchor.aboveBottom(16), VerticalAnchor.aboveBottom(64)));
  }

  public static void configuredGeode(BootstapContext<ConfiguredFeature<?, ?>> bootstapContext, ResourceKey<ConfiguredFeature<?, ?>> name, GeodeItemObject geode,
                                     BlockStateProvider middleLayer, BlockStateProvider outerLayer, GeodeLayerSettings layerSettings, GeodeCrackSettings crackSettings,
                                     IntProvider outerWall, IntProvider distributionPoints, IntProvider pointOffset, int genOffset, int invalidBlocks) {
    FeatureUtils.register(bootstapContext, name, Feature.GEODE, new GeodeConfiguration(
      new GeodeBlockSettings(BlockStateProvider.simple(Blocks.AIR),
        BlockStateProvider.simple(geode.getBlock()),
        SupplierBlockStateProvider.ofBlock(geode::getBudding),
        middleLayer, outerLayer,
        Arrays.stream(GeodeItemObject.BudSize.values()).map(type -> geode.getBud(type).defaultBlockState()).toList(),
        BlockTags.FEATURES_CANNOT_REPLACE, BlockTags.GEODE_INVALID_BLOCKS),
      layerSettings, crackSettings, 0.335, 0.083, true, outerWall, distributionPoints, pointOffset, -genOffset, genOffset, 0.05D, invalidBlocks));
  }

  public static void placedGeode(BootstapContext<PlacedFeature> bootstapContext, ResourceKey<PlacedFeature> name, Holder<ConfiguredFeature<?, ?>> geode, RarityFilter rarity, HeightRangePlacement height) {
    PlacementUtils.register(bootstapContext, name, geode, rarity, InSquarePlacement.spread(), height, BiomeFilter.biome());
  }

  /*
   * Events
   */

  public static void init() {
    entityAttributes();
    commonSetup();
  }

  public static void entityAttributes() {
    FabricDefaultAttributeRegistry.register(earthSlimeEntity.get(), Monster.createMonsterAttributes());
    FabricDefaultAttributeRegistry.register(skySlimeEntity.get(), Monster.createMonsterAttributes());
    FabricDefaultAttributeRegistry.register(enderSlimeEntity.get(), Monster.createMonsterAttributes());
    FabricDefaultAttributeRegistry.register(terracubeEntity.get(), Monster.createMonsterAttributes());
  }

  /**
   * Sets all fire info for the given wood
   */
  private static void setWoodFireInfo(FireBlock fireBlock, WoodBlockObject wood) {
    // planks
    FlammableBlockRegistry.getDefaultInstance().add(wood.get(), 5, 20);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getSlab(), 5, 20);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getStairs(), 5, 20);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getFence(), 5, 20);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getFenceGate(), 5, 20);
    // logs
    FlammableBlockRegistry.getDefaultInstance().add(wood.getLog(), 5, 5);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getStrippedLog(), 5, 5);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getWood(), 5, 5);
    FlammableBlockRegistry.getDefaultInstance().add(wood.getStrippedWood(), 5, 5);
  }

  public static void commonSetup() {
    SpawnPlacements.register(earthSlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new SlimePlacementPredicate<>(TinkerTags.Blocks.EARTH_SLIME_SPAWN));
    SpawnPlacements.register(skySlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new SlimePlacementPredicate<>(TinkerTags.Blocks.SKY_SLIME_SPAWN));
    SpawnPlacements.register(enderSlimeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new SlimePlacementPredicate<>(TinkerTags.Blocks.ENDER_SLIME_SPAWN));
    SpawnPlacements.register(terracubeEntity.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, TerracubeEntity::canSpawnHere);

    // compostables
//    event.enqueueWork(() -> {
    slimeLeaves.forEach((type, block) -> CompostingChanceRegistry.INSTANCE.add(block, type.isNether() ? 0.85f : 0.35f));
    slimeSapling.forEach(block -> CompostingChanceRegistry.INSTANCE.add(block, 0.35f));
    slimeTallGrass.forEach(block -> CompostingChanceRegistry.INSTANCE.add(block, 0.35f));
    slimeFern.forEach(block -> CompostingChanceRegistry.INSTANCE.add(block, 0.65f));
    slimeGrassSeeds.forEach(block -> CompostingChanceRegistry.INSTANCE.add(block, 0.35F));
    CompostingChanceRegistry.INSTANCE.add(skySlimeVine, 0.5f);
    CompostingChanceRegistry.INSTANCE.add(enderSlimeVine, 0.5f);

    // head equipping
    DispenseItemBehavior dispenseArmor = new OptionalDispenseItemBehavior() {
      @Override
      protected ItemStack execute(BlockSource source, ItemStack stack) {
        this.setSuccess(ArmorItem.dispenseArmor(source, stack));
        return stack;
      }
    };
    TinkerWorld.heads.forEach(head -> DispenserBlock.registerBehavior(head, dispenseArmor));
    // heads in firework stars
    TinkerWorld.heads.forEach(head -> FireworkStarRecipe.SHAPE_BY_ITEM.put(head.asItem(), FireworkRocketItem.Shape.CREEPER));
    // inject heads into the tile entity type
//      event.enqueueWork(() -> {
    ImmutableSet.Builder<Block> builder = ImmutableSet.builder();
    builder.addAll(BlockEntityType.SKULL.validBlocks);
    TinkerWorld.heads.forEach(head -> builder.add(head));
    TinkerWorld.wallHeads.forEach(head -> builder.add(head));
    BlockEntityType.SKULL.validBlocks = builder.build();
//      });
//    });

    // flammability
//    event.enqueueWork(() -> {
    FireBlock fireblock = (FireBlock) Blocks.FIRE;
    // plants
    BiConsumer<SlimeType, Block> plantFireInfo = (type, block) -> {
      if (type != SlimeType.BLOOD && type != SlimeType.ICHOR) {
        fireblock.setFlammable(block, 30, 60);
      }
    };
    slimeLeaves.forEach(plantFireInfo);
    slimeTallGrass.forEach(plantFireInfo);
    slimeFern.forEach(plantFireInfo);
    // vines
    fireblock.setFlammable(skySlimeVine.get(), 15, 100);
    fireblock.setFlammable(enderSlimeVine.get(), 15, 100);
//    });
  }

  public static void gatherData(final FabricDataGenerator.Pack pack) {
    pack.addProvider(WorldRecipeProvider::new);
  }


  /* helpers */

  /**
   * Creates a skull block for the given head type
   */
  private static SkullBlock makeHead(TinkerHeadType type) {
    BlockBehaviour.Properties props = BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).strength(1.0F);
    return new SkullBlock(type, props);
  }

  /**
   * Creates a skull wall block for the given head type
   */
  private static WallSkullBlock makeWallHead(TinkerHeadType type) {
    BlockBehaviour.Properties props = BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).strength(1.0F).dropsLike(heads.get(type));
    if (type == TinkerHeadType.PIGLIN_BRUTE || type == TinkerHeadType.ZOMBIFIED_PIGLIN) {
      return new PiglinWallHeadBlock(type, props);
    }
    return new WallSkullBlock(type, props);
  }
}
